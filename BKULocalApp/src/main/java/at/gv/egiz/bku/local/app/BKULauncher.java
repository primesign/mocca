package at.gv.egiz.bku.local.app;

import java.awt.SplashScreen;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyStore;
import java.util.Enumeration;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import at.gv.egiz.bku.local.ui.BKUControllerInterface;
import at.gv.egiz.bku.local.ui.TrayIconDialog;
import at.gv.egiz.bku.utils.StreamUtil;

public class BKULauncher implements BKUControllerInterface {
  private static Log log = LogFactory.getLog(BKULauncher.class);

  private ResourceBundle resourceBundle = null;
  private Container server;
  private SplashScreen splash = SplashScreen.getSplashScreen();

  private void startUpServer() throws Exception {
    server = new Container();
    // XmlConfiguration xcfg = new XmlConfiguration(getClass().getClassLoader()
    // .getResourceAsStream("at/gv/egiz/bku/local/app/jetty.xml"));
    // xcfg.configure(server);
    server.init();
    server.start();
  }

  private void initTrayIcon() {
    Locale loc = Locale.getDefault();
    try {
      resourceBundle = ResourceBundle.getBundle(
          "at/gv/egiz/bku/local/ui/UIMessages", loc);
    } catch (MissingResourceException mx) {
      resourceBundle = ResourceBundle.getBundle(
          "at/gv/egiz/bku/local/ui/UIMessages", Locale.ENGLISH);
    }
    TrayIconDialog.getInstance().init(resourceBundle);
    TrayIconDialog.getInstance().setShutdownHook(this);
    TrayIconDialog.getInstance().displayInfo("Greetings.Caption",
        "Greetings.Message");
  }

  private void initStart() {

  }

  private void initFinished() {
    try {
      if (splash != null) {
        splash.close();
      }
      server.join();
    } catch (InterruptedException e) {
      log.info(e);
    }
  }

  private void copyDirs(File srcDir, File dstDir) {
    for (File cf : srcDir.listFiles()) {
      File of = new File(dstDir, cf.getName());
      if (cf.isDirectory()) {
        log.debug("Creating directory: " + of);
        of.mkdir();
        copyDirs(cf, of);
      } else {
        log.debug("Writing file: " + of);
        try {
          FileInputStream fis = new FileInputStream(cf);
          FileOutputStream fos = new FileOutputStream(of);
          StreamUtil.copyStream(fis, fos);
          fis.close();
          fos.close();
        } catch (IOException e) {
          log.error("Cannot copy default configuration", e);
        }
      }
    }
  }

  private void unzip(File zipfile) throws IOException {
    File dir = zipfile.getParentFile();
    ZipFile zipFile = new ZipFile(zipfile);
    Enumeration<? extends ZipEntry> entries = zipFile.entries();
    while (entries.hasMoreElements()) {
      ZipEntry entry = entries.nextElement();
      File eF = new File(dir, entry.getName());
      if (entry.isDirectory()) {
        eF.mkdirs();
        continue;
      }
      File f = new File(eF.getParent());
      f.mkdirs();
      StreamUtil.copyStream(zipFile.getInputStream(entry),
          new FileOutputStream(eF));
    }
    zipFile.close();
  }

  private void checkConfig(String[] args) {
    CommandLineParser parser = new PosixParser();
    Options options = new Options();
    options.addOption("c", true, "the configuration's base directory");
    options.addOption("h", false, "print this message");
    try {
      File cfgDir = new File(System.getProperty("user.home") + "/.mocca/conf");
      CommandLine cmd = parser.parse(options, args);
      if (cmd.hasOption("h")) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("BKULauncher", options);
        System.exit(0);
      }

      if (cmd.hasOption("c")) {
        cfgDir = new File(cmd.getOptionValue("c"));
      }
      log.debug("using config directory: " + cfgDir);
      if (cfgDir.exists() && cfgDir.isFile()) {
        log.error("Configuration directory must not be a file");
      }
      if (!cfgDir.exists()) {
        log.debug("Creating config directory: " + cfgDir);
        cfgDir.mkdirs();
        try {
          InputStream is = getClass().getClassLoader().getResourceAsStream(
              "at/gv/egiz/bku/local/defaultConf/template.zip");
          OutputStream os = new FileOutputStream(new File(cfgDir,
              "template.zip"));
          StreamUtil.copyStream(is, os);
          os.close();
          unzip(new File(cfgDir, "template.zip"));
        } catch (IOException iox) {
          log.error("Cannot create user directory", iox);
          return;
        }
        CA ca = new CA();
        char[] password = "changeMe".toCharArray();
        KeyStore ks = ca.generateKeyStore(password);
        if (ks != null) {
          File ksdir = new File(cfgDir, "keystore");
          ksdir.mkdirs();
          FileOutputStream fos;
          try {
            fos = new FileOutputStream(new File(ksdir, "keystore.ks"));
            ks.store(fos, password);
            fos.close();
          } catch (Exception e) {
            log.error("Cannot store keystore", e);
          }
        } else {
          log.error("Cannot create ssl certificate");
        }
      }
    } catch (ParseException e1) {
      log.error(e1);
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp("BKULauncher", options);
      System.exit(0);
    }
  }

  /**
   * @param args
   */
  public static void main(String[] args) {

    try {

      BKULauncher launcher = new BKULauncher();
      launcher.initStart();
      launcher.checkConfig(args);
      launcher.startUpServer();
      launcher.initTrayIcon();
      launcher.initFinished();
    } catch (Exception e) {
      log.fatal("Cannot launch BKU", e);
      System.exit(-1000);
    }

  }

  public void shutDown() {
    log.info("Shutting down server");
    if ((server != null) && (server.isRunning())) {
      try {
        if (server.isRunning()) {
          server.stop();
        }
      } catch (Exception e) {
        log.debug(e.toString());
      } finally {
        if (server.isRunning()) {
          server.destroy();
        }
      }
    }
    System.exit(0);
  }

}
