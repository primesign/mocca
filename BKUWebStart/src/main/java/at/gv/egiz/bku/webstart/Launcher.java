package at.gv.egiz.bku.webstart;

import iaik.asn1.CodingException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.jnlp.UnavailableServiceException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import at.gv.egiz.bku.webstart.ui.BKUControllerInterface;
import at.gv.egiz.bku.webstart.ui.TrayIconDialog;
import com.sun.javaws.security.JavaWebStartSecurity;
import java.awt.Desktop;
import java.awt.SplashScreen;
import java.net.BindException;
import java.net.URI;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import javax.jnlp.BasicService;
import javax.jnlp.ServiceManager;
import org.mortbay.util.MultiException;

public class Launcher implements BKUControllerInterface {

  public static final String WEBAPP_RESOURCE = "BKULocal.war";
  public static final String CERTIFICATES_RESOURCE = "BKUCertificates.jar";
  public static final String WEBAPP_FILE = "BKULocal.war";
  public static final String MESSAGES_RESOURCE = "at/gv/egiz/bku/webstart/ui/UIMessages";
  /** resource bundle messages */
  public static final String GREETING_CAPTION = "Greetings.Caption";
  public static final String GREETING_MESSAGE = "Greetings.Message";
  public static final String CONFIG_CAPTION = "Config.Caption";
  public static final String CONFIG_MESSAGE = "Config.Message";
  public static final String STARTUP_CAPTION = "Startup.Caption";
  public static final String STARTUP_MESSAGE = "Startup.Message";
  public static final String ERROR_CAPTION = "Error.Caption";
  public static final String ERROR_STARTUP_MESSAGE = "Error.Startup.Message";
  public static final String ERROR_CONF_MESSAGE = "Error.Conf.Message";
  public static final String ERROR_BIND_MESSAGE = "Error.Bind.Message";
  public static final URI HTTPS_SECURITY_LAYER_URI;
  private static Log log = LogFactory.getLog(Launcher.class);

  static {
    URI tmp = null;
    try {
      tmp = new URI("https://localhost:" + Integer.getInteger(Container.HTTPS_PORT_PROPERTY, 3496).intValue());
    } catch (URISyntaxException ex) {
      log.error(ex);
    } finally {
      HTTPS_SECURITY_LAYER_URI = tmp;
    }
  }

  public static final String version;
  static {
    String tmp = Configurator.UNKOWN_VERSION;
    try {
      String bkuWebStartJar = Launcher.class.getProtectionDomain().getCodeSource().getLocation().toString();
      URL manifestURL = new URL("jar:" + bkuWebStartJar + "!/META-INF/MANIFEST.MF");
      if (log.isTraceEnabled()) {
        log.trace("read version information from " + manifestURL);
      }
      Manifest manifest = new Manifest(manifestURL.openStream());
      Attributes atts = manifest.getMainAttributes();
      if (atts != null) {
        tmp = atts.getValue("Implementation-Build");
      }
    } catch (IOException ex) {
      log.error("failed to read version", ex);
    } finally {
      version = tmp;
      log.info("BKU Web Start " + version);
    }
  }

  private Configurator config;
  private Container server;
  private BasicService basicService;

  private void initStart() {
    try {
      basicService = (BasicService) ServiceManager.lookup("javax.jnlp.BasicService");
      if (basicService.isOffline()) {
        log.info("launching MOCCA Web Start offline");
      } else {
        log.info("launching MOCCA Web Start online");
      }
    } catch (UnavailableServiceException ex) {
      log.info("Failed to obtain JNLP service: " + ex.getMessage());
    }
  }

  private void initTrayIcon() {
    log.debug("init MOCCA tray icon");
    Locale loc = Locale.getDefault();
    ResourceBundle resourceBundle;
    try {
      resourceBundle = ResourceBundle.getBundle(
              MESSAGES_RESOURCE, loc);
    } catch (MissingResourceException mx) {
      resourceBundle = ResourceBundle.getBundle(
              MESSAGES_RESOURCE, Locale.ENGLISH);
    }
    TrayIconDialog.getInstance().init(resourceBundle);
    TrayIconDialog.getInstance().setShutdownHook(this);
//    TrayIconDialog.getInstance().displayInfo(GREETING_CAPTION, GREETING_MESSAGE);
  }

  private void initConfig() throws IOException, CodingException, GeneralSecurityException {
    config = new Configurator();
    config.ensureConfiguration();
    config.ensureCertificates();
  }

  private void startServer() throws Exception {
    log.info("init servlet container and MOCCA webapp");
    server = new Container();
    server.init();
    server.start();
  }

  private void initFinished() {
    try {
      // standalone (non-webstart) version has splashscreen
      if (SplashScreen.getSplashScreen() != null) {
        try {
          SplashScreen.getSplashScreen().close();
        } catch (IllegalStateException ex) {
          log.warn("Failed to close splash screen: " + ex.getMessage());
        }
      }
      if (config.isCertRenewed()) {
        // don't use basicService.showDocument(), which causes a java ssl warning dialog
        if (Desktop.isDesktopSupported()) {
          Desktop desktop = Desktop.getDesktop();
          if (desktop.isSupported(Desktop.Action.BROWSE)) {
            try {
              desktop.browse(HTTPS_SECURITY_LAYER_URI);
            } catch (Exception ex) {
              log.error("failed to open system browser, install TLS certificate manually: " + HTTPS_SECURITY_LAYER_URI, ex);
            }
          } else {
            log.error("failed to open system browser, install TLS certificate manually: " + HTTPS_SECURITY_LAYER_URI);
          }
        } else {
          log.error("failed to open system browser, install TLS certificate manually: " + HTTPS_SECURITY_LAYER_URI);
        }
      }
      log.info("BKU successfully started");
      server.join();
    } catch (InterruptedException e) {
      log.warn("failed to join server: " + e.getMessage(), e);
    }
  }

  @Override
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

  public static void main(String[] args) throws InterruptedException, IOException {

    if (log.isTraceEnabled()) {
      SecurityManager sm = System.getSecurityManager();
      if (sm instanceof JavaWebStartSecurity) {
        System.setSecurityManager(new LogSecurityManager((JavaWebStartSecurity) sm));
      }
    }

    Launcher launcher = new Launcher();
    launcher.initStart();
    launcher.initTrayIcon(); //keep reference? BKULauncher not garbage collected after main()
    
    try {
      TrayIconDialog.getInstance().displayInfo(CONFIG_CAPTION, CONFIG_MESSAGE);
      launcher.initConfig();
    } catch (Exception ex) {
      log.fatal("Failed to initialize configuration", ex);
      TrayIconDialog.getInstance().displayError(ERROR_CAPTION, ERROR_CONF_MESSAGE);
      Thread.sleep(5000);
      System.exit(-1000);
    }

    try {
      TrayIconDialog.getInstance().displayInfo(STARTUP_CAPTION, STARTUP_MESSAGE);
      launcher.startServer();
      TrayIconDialog.getInstance().displayInfo(GREETING_CAPTION, GREETING_MESSAGE);
      launcher.initFinished();
    } catch (BindException ex) {
      log.fatal("Failed to launch server, " + ex.getMessage(), ex);
      TrayIconDialog.getInstance().displayError(ERROR_CAPTION, ERROR_BIND_MESSAGE);
      Thread.sleep(5000);
      System.exit(-1000);
    } catch (MultiException ex) {
      log.fatal("Failed to launch server, " + ex.getMessage(), ex);
      if (ex.getThrowable(0) instanceof BindException) {
        TrayIconDialog.getInstance().displayError(ERROR_CAPTION, ERROR_BIND_MESSAGE);
      } else {
        TrayIconDialog.getInstance().displayError(ERROR_CAPTION, ERROR_STARTUP_MESSAGE);
      }
      Thread.sleep(5000);
      System.exit(-1000);
    } catch (Exception e) {
      log.fatal("Failed to launch server, " + e.getMessage(), e);
      TrayIconDialog.getInstance().displayError(ERROR_CAPTION, ERROR_STARTUP_MESSAGE);
      Thread.sleep(5000);
      System.exit(-1000);
    }
  }
}
