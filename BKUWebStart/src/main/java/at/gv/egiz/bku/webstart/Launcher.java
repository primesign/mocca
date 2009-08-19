package at.gv.egiz.bku.webstart;

import at.gv.egiz.bku.webstart.gui.BKUControllerInterface;
import at.gv.egiz.bku.webstart.gui.TrayMenuListener;
import iaik.asn1.CodingException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.jnlp.UnavailableServiceException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sun.javaws.security.JavaWebStartSecurity;
import java.awt.AWTException;
import java.awt.Desktop;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SplashScreen;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.net.BindException;
import java.net.URI;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import javax.imageio.ImageIO;
import javax.jnlp.BasicService;
import javax.jnlp.ServiceManager;
import org.mortbay.util.MultiException;

public class Launcher implements BKUControllerInterface {

  public static final String WEBAPP_RESOURCE = "BKULocal.war";
  public static final String CERTIFICATES_RESOURCE = "BKUCertificates.jar";
  public static final String WEBAPP_FILE = "BKULocal.war";
  public static final String MESSAGES_RESOURCE = "at/gv/egiz/bku/webstart/messages";
  public static final String TRAYICON_RESOURCE = "at/gv/egiz/bku/webstart/logo_";
  /** resource bundle messages */
  public static final String CAPTION_DEFAULT = "tray.caption.default";
  public static final String CAPTION_ERROR = "tray.caption.error";
  public static final String MESSAGE_START = "tray.message.start";
  public static final String MESSAGE_START_OFFLINE = "tray.message.start.offline";
  public static final String MESSAGE_CONFIG = "tray.message.config";
  public static final String MESSAGE_CERTS = "tray.message.certs";
  public static final String MESSAGE_FINISHED = "tray.message.finished";
  public static final String MESSAGE_SHUTDOWN = "tray.message.shutdown";
  public static final String ERROR_START = "tray.error.start";
  public static final String ERROR_CONFIG = "tray.error.config";
  public static final String ERROR_BIND = "tray.error.bind";
  public static final String LABEL_SHUTDOWN = "tray.label.shutdown";
  public static final String LABEL_PIN = "tray.label.pin";
  public static final String LABEL_ABOUT = "tray.label.about";
  public static final String TOOLTIP_DEFAULT = "tray.tooltip.default";
  
  /** local bku uri */
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
  private TrayIcon trayIcon;
  private ResourceBundle messages;
  
  public Launcher() {
    if (log.isTraceEnabled()) {
      SecurityManager sm = System.getSecurityManager();
      if (sm instanceof JavaWebStartSecurity) {
        System.setSecurityManager(new LogSecurityManager((JavaWebStartSecurity) sm));
      }
    }
    messages = ResourceBundle.getBundle(MESSAGES_RESOURCE, Locale.getDefault());
    trayIcon = initTrayIcon();
  }

  public void launch() throws Exception {
    initStart();
    try {
      initConfig();
    } catch (Exception ex) {
      log.fatal("Failed to initialize configuration", ex);
      trayIcon.displayMessage(messages.getString(CAPTION_ERROR),
              messages.getString(ERROR_CONFIG), TrayIcon.MessageType.ERROR);
      throw ex;
    }
    try {
      startServer();
      initFinished();
    } catch (BindException ex) {
      log.fatal("Failed to launch server, " + ex.getMessage(), ex);
      trayIcon.displayMessage(messages.getString(CAPTION_ERROR),
              messages.getString(ERROR_BIND), TrayIcon.MessageType.ERROR);
      throw ex;
    } catch (MultiException ex) {
      log.fatal("Failed to launch server, " + ex.getMessage(), ex);
      if (ex.getThrowable(0) instanceof BindException) {
        trayIcon.displayMessage(messages.getString(CAPTION_ERROR),
                messages.getString(ERROR_BIND), TrayIcon.MessageType.ERROR);
      } else {
        trayIcon.displayMessage(messages.getString(CAPTION_ERROR),
                messages.getString(ERROR_START), TrayIcon.MessageType.ERROR);
      }
      throw ex;
    } catch (Exception ex) {
      log.fatal("Failed to launch server, " + ex.getMessage(), ex);
      trayIcon.displayMessage(messages.getString(CAPTION_ERROR),
              messages.getString(ERROR_START), TrayIcon.MessageType.ERROR);
      throw ex;
    }
  }
  
  private TrayIcon initTrayIcon() { //ResourceBundle messages, BKUControllerInterface bkuHook) {
    if (SystemTray.isSupported()) {
      try {
        // get the SystemTray instance
        SystemTray tray = SystemTray.getSystemTray();
        log.debug("TrayIcon size: " + tray.getTrayIconSize());
        String iconResource = (tray.getTrayIconSize().height < 25)
                ? TRAYICON_RESOURCE + "24.png"
                : TRAYICON_RESOURCE + "32.png";
        Image image = ImageIO.read(Launcher.class.getClassLoader().getResourceAsStream(iconResource));

        TrayMenuListener listener = new TrayMenuListener(this, messages, version);
        PopupMenu popup = new PopupMenu();
        
        MenuItem shutdownItem = new MenuItem(messages.getString(LABEL_SHUTDOWN));
        shutdownItem.addActionListener(listener);
        shutdownItem.setActionCommand(TrayMenuListener.SHUTDOWN_COMMAND);
        popup.add(shutdownItem);

        MenuItem aboutItem = new MenuItem(messages.getString(LABEL_ABOUT));
        aboutItem.setActionCommand(TrayMenuListener.ABOUT_COMMAND);
        aboutItem.addActionListener(listener);
        popup.add(aboutItem);

        TrayIcon ti = new TrayIcon(image, messages.getString(TOOLTIP_DEFAULT), popup);
        ti.addActionListener(listener);
        tray.add(ti);
        return ti;
      } catch (AWTException ex) {
        log.error("Failed to init tray icon", ex);
      } catch (IOException ex) {
        log.error("Failed to load tray icon image", ex);
      }
    } else {
      log.error("No system tray support");
    }
    return null;
  }

  private void initStart() {
    try {
      trayIcon.displayMessage(messages.getString(CAPTION_DEFAULT),
            messages.getString(MESSAGE_START), TrayIcon.MessageType.INFO);
      basicService = (BasicService) ServiceManager.lookup("javax.jnlp.BasicService");
      if (basicService.isOffline()) {
        log.info("launching MOCCA Web Start offline");
        trayIcon.displayMessage(messages.getString(CAPTION_DEFAULT),
                messages.getString(MESSAGE_START_OFFLINE), TrayIcon.MessageType.INFO);
      } else {
        log.info("launching MOCCA Web Start online");
      }
    } catch (UnavailableServiceException ex) {
      log.info("Failed to obtain JNLP service: " + ex.getMessage());
    }
  }

  private void initConfig() throws IOException, CodingException, GeneralSecurityException {
    trayIcon.displayMessage(messages.getString(CAPTION_DEFAULT),
              messages.getString(MESSAGE_CONFIG), TrayIcon.MessageType.INFO);
    config = new Configurator();
    config.ensureConfiguration();
    trayIcon.displayMessage(messages.getString(CAPTION_DEFAULT),
              messages.getString(MESSAGE_CERTS), TrayIcon.MessageType.INFO);
    config.ensureCertificates();
  }

  private void startServer() throws Exception {
    log.info("init servlet container and MOCCA webapp");
//    trayIcon.displayMessage(messages.getString(CAPTION_DEFAULT),
//              messages.getString(MESSAGE_START), TrayIcon.MessageType.INFO);
    server = new Container();
    server.init();
    server.start();
  }

  private void initFinished() {
    try {
      trayIcon.displayMessage(messages.getString(CAPTION_DEFAULT),
              messages.getString(MESSAGE_FINISHED), TrayIcon.MessageType.INFO);
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
    trayIcon.displayMessage(messages.getString(CAPTION_DEFAULT), 
            messages.getString(MESSAGE_SHUTDOWN), TrayIcon.MessageType.INFO);
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
    try {
      Launcher launcher = new Launcher();
      launcher.launch();
    } catch (Exception ex) {
      log.info("waiting to shutdown...");
      Thread.sleep(5000);
      log.info("exit");
      System.exit(-1000);
    }
  }
}
