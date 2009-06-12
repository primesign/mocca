package at.gv.egiz.bku.webstart;

import iaik.asn1.CodingException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Enumeration;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

//import org.apache.commons.cli.CommandLine;
//import org.apache.commons.cli.CommandLineParser;
//import org.apache.commons.cli.HelpFormatter;
//import org.apache.commons.cli.Options;
//import org.apache.commons.cli.ParseException;
//import org.apache.commons.cli.PosixParser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import at.gv.egiz.bku.webstart.ui.BKUControllerInterface;
import at.gv.egiz.bku.webstart.ui.TrayIconDialog;
import at.gv.egiz.bku.utils.StreamUtil;
import java.awt.Desktop;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.URI;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.UUID;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.zip.ZipOutputStream;

public class BKULauncher implements BKUControllerInterface {

  /** configurations with less than this (major) version will be backuped and updated */
  public static final String MIN_CONFIG_VERSION = "1.0.3";
  public static final String CONFIG_DIR = ".mocca/conf/";
  public static final String CONF_TEMPLATE_FILE = "template.zip";
  public static final String CONF_TEMPLATE_RESOURCE = "at/gv/egiz/bku/webstart/conf/template.zip";
  public static final String WEBAPP_RESOURCE = "BKULocal.war";
  public static final String WEBAPP_FILE = "BKULocal.war";
  public static final String KEYSTORE_FILE = "keystore.ks";
  public static final String MESSAGES_RESOURCE = "at/gv/egiz/bku/webstart/ui/UIMessages";
  public static final String PASSWD_FILE = ".secret";
  /** resource bundle messages */
  public static final String GREETING_CAPTION = "Greetings.Caption";
  public static final String GREETING_MESSAGE = "Greetings.Message";
  public static final String VERSION_FILE = ".version";
  private static Log log = LogFactory.getLog(BKULauncher.class);
  private ResourceBundle resourceBundle = null;
  private Container server;

  private void createConfig(File configDir, File versionFile, String version) throws IOException, CertificateException, GeneralSecurityException, KeyStoreException, FileNotFoundException, NoSuchAlgorithmException {
    log.debug("creating config directory: " + configDir);
    configDir.mkdirs();
    InputStream is = getClass().getClassLoader().getResourceAsStream(CONF_TEMPLATE_RESOURCE);
    OutputStream os = new FileOutputStream(new File(configDir, CONF_TEMPLATE_FILE));
    StreamUtil.copyStream(is, os);
    os.close();
    File confTemplateFile = new File(configDir, CONF_TEMPLATE_FILE);
    unzip(confTemplateFile);
    confTemplateFile.delete();
    writeVersionFile(versionFile, version);
  }

  private void createCertificates(File configDir) throws IOException, GeneralSecurityException, CodingException {
    char[] password = UUID.randomUUID().toString().toCharArray();
    File passwdFile = new File(configDir, PASSWD_FILE);
    FileWriter passwdWriter = new FileWriter(passwdFile);
    passwdWriter.write(password);
    passwdWriter.close();
    if (!passwdFile.setReadable(true, true)) {
      passwdFile.delete();
      throw new IOException("failed to make " + passwdFile + " owner readable only, deleting file");
    }
    TLSServerCA ca = new TLSServerCA();
    KeyStore ks = ca.generateKeyStore(password);
    FileOutputStream fos = new FileOutputStream(new File(configDir, KEYSTORE_FILE));
    ks.store(fos, password);
    fos.close();
  }

  private String getFileVersion(File versionFile) throws FileNotFoundException, IOException {
    //TODO no file?
    if (versionFile.exists() && versionFile.canRead()) {
      BufferedReader versionReader = new BufferedReader(new FileReader(versionFile));
      String versionString = null;
      while ((versionString = versionReader.readLine().trim()) != null) {
        if (versionString.length() > 0 && !versionString.startsWith("#")) {
          log.debug("found existing configuration version " + versionString);
          break;
        }
      }
      return versionString;
    }
    return null;
  }

  private String getManifestVersion() throws MalformedURLException, IOException {
    String bkuWebStartJar = BKULauncher.class.getProtectionDomain().getCodeSource().getLocation().toString();
    URL manifestURL = new URL("jar:" + bkuWebStartJar + "!/META-INF/MANIFEST.MF");
    String version = null;
    if (manifestURL != null) {
      Manifest manifest = new Manifest(manifestURL.openStream());
      if (log.isTraceEnabled()) {
        log.trace("read version information from " + manifestURL);
      }
      Attributes atts = manifest.getMainAttributes();
      if (atts != null) {
        version = atts.getValue("Implementation-Build");
      }
    }
    if (version == null) {
      version = "UNKNOWN";
    }
    log.debug("config version: " + version);
    return version;
  }

  /**
   * change the
   * @param oldVersion
   * @param newVersion
   * @return
   */
  private boolean updateRequired(String oldVersion, String newVersion) {
    if (oldVersion != null) {
      int majorEnd = oldVersion.indexOf('-');
      if (majorEnd > 0) {
        oldVersion = oldVersion.substring(0, majorEnd);
      }
      return (oldVersion.compareTo(MIN_CONFIG_VERSION) < 0);
    }
    log.debug("no old version, update required");
    return true;
  }

  private boolean updateRequiredStrict(String oldVersion, String newVersion) {
    String[] oldV = oldVersion.split("-");
    String[] newV = newVersion.split("-");
    log.debug("comparing " + oldV[0] + " to " + newV[0]);
    if (oldV[0].compareTo(newV[0]) < 0) {
      log.debug("update required");
      return true;
    } else {
      log.debug("comparing " + oldV[oldV.length - 1] + " to " + newV[newV.length - 1]);
      if (oldV[oldV.length - 1].compareTo(newV[newV.length - 1]) < 0) {
        log.debug("update required");
        return true;
      } else {
        log.debug("no update required");
        return false;
      }
    }
  }

  private void writeVersionFile(File versionFile, String version) throws IOException {
    BufferedWriter versionWriter = new BufferedWriter(new FileWriter(versionFile));
    versionWriter.write("# MOCCA Web Start configuration version\n");
    versionWriter.write("# DO NOT MODIFY THIS FILE\n\n");
    versionWriter.write(version);
    versionWriter.close();
  }

//  private SplashScreen splash = SplashScreen.getSplashScreen();
  private void startUpServer() throws Exception {
    log.info("init servlet container and MOCCA webapp");
    server = new Container();
    // XmlConfiguration xcfg = new XmlConfiguration(getClass().getClassLoader()
    // .getResourceAsStream("at/gv/egiz/bku/local/app/jetty.xml"));
    // xcfg.configure(server);
    server.init();
    server.start();
  }

  private void initTrayIcon() {
    log.debug("init MOCCA tray icon");
    Locale loc = Locale.getDefault();
    try {
      resourceBundle = ResourceBundle.getBundle(
              MESSAGES_RESOURCE, loc);
    } catch (MissingResourceException mx) {
      resourceBundle = ResourceBundle.getBundle(
              MESSAGES_RESOURCE, Locale.ENGLISH);
    }
    TrayIconDialog.getInstance().init(resourceBundle);
    TrayIconDialog.getInstance().setShutdownHook(this);
    TrayIconDialog.getInstance().displayInfo(GREETING_CAPTION, GREETING_MESSAGE);
  }

  private void initStart() {
  }

  private void initFinished(boolean installCert) {
    try {
//      if (splash != null) {
//        try {
//          splash.close();
//        } catch (IllegalStateException ex) {
//          log.warn("Failed to close splash screen: " + ex.getMessage());
//        }
//      }

      log.debug("trying install MOCCA certificate on system browser");
      if (installCert) {
        if (Desktop.isDesktopSupported()) {
          Desktop desktop = Desktop.getDesktop();
          if (desktop.isSupported(Desktop.Action.BROWSE)) {
            try {
              desktop.browse(new URI("https://localhost:" +
                      Integer.getInteger(Container.HTTPS_PORT_PROPERTY, 3496).intValue()));
            } catch (Exception ex) {
              log.error("failed to open system browser, install MOCCA certificate manually", ex);
            }
          } else {
            log.error("failed to open system browser, install MOCCA certificate manually");
          }
        } else {
          log.error("failed to open system browser, install MOCCA certificate manually");
        }
      }

      log.info("init completed, joining server");
      server.join();
    } catch (InterruptedException e) {
      log.warn("failed to join MOCCA server: " + e.getMessage(), e);
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

  /**
   * @param args
   */
  public static void main(String[] args) {
    try {
      log.warn("***** DISABLING SECURITY MANAGER *******");
      System.setSecurityManager(null);
      BKULauncher launcher = new BKULauncher();
      launcher.initStart();

      File configDir = new File(System.getProperty("user.home") + '/' + CONFIG_DIR);
      boolean installCert = launcher.ensureConfig(configDir);
      launcher.startUpServer();
      launcher.initTrayIcon();
      launcher.initFinished(installCert);
    } catch (Exception e) {
      log.fatal("Failed to launch BKU: " + e.getMessage(), e);
      System.exit(-1000);
    }
  }

  private void backupAndDelete(File dir, URI relativeTo, ZipOutputStream zip) throws IOException {
    if (dir.isDirectory()) {
      File[] subDirs = dir.listFiles();
      for (File subDir : subDirs) {
        backupAndDelete(subDir, relativeTo, zip);
        subDir.delete();
      }
    } else {
      URI relativePath = relativeTo.relativize(dir.toURI());
      ZipEntry entry = new ZipEntry(relativePath.toString());
      zip.putNextEntry(entry);
      BufferedInputStream entryIS = new BufferedInputStream(new FileInputStream(dir));
      StreamUtil.copyStream(entryIS, zip);
      entryIS.close();
      zip.closeEntry();
      dir.delete();
    }
  }

  /**
   * Checks whether the config directory already exists and creates it otherwise.
   * @param configDir the config directory to be created
   * @return true if a new MOCCA cert was created (and needs to be installed in the browser)
   */
  private boolean ensureConfig(File configDir) throws IOException, GeneralSecurityException, CodingException {
    log.debug("config directory: " + configDir);
    String manifestVersion = getManifestVersion();
    File versionFile = new File(configDir, VERSION_FILE);

    if (configDir.exists()) {
      if (configDir.isFile()) {
        log.error("invalid config directory: " + configDir);
        throw new IOException("invalid config directory: " + configDir);
      } else {
        String fileVersion = getFileVersion(versionFile);
        if (updateRequired(fileVersion, manifestVersion)) {
          if (fileVersion == null) {
            fileVersion = "unknown";
          }
          log.info("updating configuration from " + fileVersion + " to " + manifestVersion);
          File moccaDir = configDir.getParentFile();
          File zipFile = new File(moccaDir, "conf-" + fileVersion + ".zip");
          ZipOutputStream zipOS = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile)));
          backupAndDelete(configDir, moccaDir.toURI(), zipOS);
          zipOS.close();
          createConfig(configDir, versionFile, manifestVersion);
          createCertificates(configDir);
          return true;
        }
      }
    } else {
      createConfig(configDir, versionFile, manifestVersion);
      createCertificates(configDir);
      return true;
    }
    return false;
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
