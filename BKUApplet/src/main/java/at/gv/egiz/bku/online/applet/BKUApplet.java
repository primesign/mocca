/*
 * Copyright 2011 by Graz University of Technology, Austria
 * MOCCA has been developed by the E-Government Innovation Center EGIZ, a joint
 * initiative of the Federal Chancellery Austria and Graz University of Technology.
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * http://www.osor.eu/eupl/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 *
 * This product combines work with different licenses. See the "NOTICE" text
 * file for details on the various modules and licenses.
 * The "NOTICE" text file is part of the distribution. Any derivative works
 * that you distribute must include a readable copy of the "NOTICE" text file.
 */


package at.gv.egiz.bku.online.applet;

import java.applet.AppletContext;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Point;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessController;
import java.security.AllPermission;
import java.security.PrivilegedAction;
import java.util.Locale;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import javax.accessibility.AccessibleContext;
import javax.net.ssl.HttpsURLConnection;
import javax.swing.JApplet;
import javax.swing.JLayeredPane;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.text.html.parser.ParserDelegator;
import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.gv.egiz.bku.gui.BKUGUIFacade;
import at.gv.egiz.bku.gui.BKUGUIFacade.Style;
import at.gv.egiz.bku.gui.BKUGUIImpl;
import at.gv.egiz.bku.gui.HelpListener;
import at.gv.egiz.bku.gui.viewer.FontProvider;
import at.gv.egiz.bku.online.applet.viewer.URLFontLoader;
import at.gv.egiz.smcc.SignatureCardFactory;
import at.gv.egiz.stal.service.STALPortType;
import at.gv.egiz.stal.service.STALService;
import at.gv.egiz.stal.service.translator.STALTranslator;

/**
 * Note: all swing code is executed by the event dispatch thread (see
 * BKUGUIFacade)
 */
public class BKUApplet extends JApplet {

  private static final long serialVersionUID = 1L;
  
  private final Logger log = LoggerFactory.getLogger(BKUApplet.class);
  
  /**
   * Applet parameter keys
   */
  public static final String GUI_STYLE = "GuiStyle";
  public final static String LOCALE = "Locale";
  public final static String WSDL_URL = "WSDL_URL";
  public static final String HASHDATA_DISPLAY = "HashDataDisplay";
  public final static String HASHDATA_URL = "HashDataURL";
  public final static String HELP_URL = "HelpURL";
  public final static String SESSION_ID = "SessionID";
  public static final String BACKGROUND_IMG = "Background";
  public static final String BACKGROUND_COLOR = "BackgroundColor";
  public static final String REDIRECT_URL = "RedirectURL";
  public static final String REDIRECT_TARGET = "RedirectTarget";
  public static final String ENFORCE_RECOMMENDED_PIN_LENGTH = "EnforceRecommendedPINLength";


  /**
   * STAL WSDL namespace and service name
   */
  public static final String STAL_WSDL_NS = "http://www.egiz.gv.at/wsdl/stal";
  public static final String STAL_SERVICE = "STALService";
  /**
   * Dummy session id, used if no sessionId parameter is provided
   */
  protected static final String TEST_SESSION_ID = "TestSession";

  public static final String VERSION;
  public static final String UNKNOWN_VERSION = "UNKNOWN";
  
  static {
    String tmp = UNKNOWN_VERSION;
    Logger log = LoggerFactory.getLogger(BKUApplet.class);
    try {
      String BKUAppletJar = BKUApplet.class.getProtectionDomain().getCodeSource().getLocation().toString();
      URL manifestURL = new URL("jar:" + BKUAppletJar + "!/META-INF/MANIFEST.MF");
      log.trace("Read version information from {}.", manifestURL);
      Manifest manifest = new Manifest(manifestURL.openStream());
      Attributes atts = manifest.getMainAttributes();
      if (atts != null) {
        tmp = atts.getValue("Implementation-Build");
      }
    } catch (IOException ex) {
      log.error("Failed to read version.", ex);
    } finally {
      VERSION = tmp;
      log.debug("BKU Applet {}.", VERSION);
    }
  }

  /**
   * STAL
   */
  protected AppletBKUWorker worker;
  protected Thread workerThread;
  protected HelpListener helpListener;

  /*
   * (non-Javadoc)
   *
   * @see java.applet.Applet#getParameterInfo()
   */
  @Override
  public String[][] getParameterInfo() {
    return new String[][]{
              {WSDL_URL, "url",
                "URL of the WSDL of the MOCCA server side STAL"},
              {REDIRECT_URL, "url",
                "URL to redirect the browser to when finished"},
              {REDIRECT_TARGET, "frame target",
                "name of the target frame for redirection when finished"},
              {LOCALE, "locale",
                "locale for UI localization (optional, default: system default)"},
              {GUI_STYLE, "simple, advanced, tiny",
                "GUI style (optional, default: simple)"},
              {BACKGROUND_COLOR, "#hhhhhh",
                "background color, e.g. '#333333' (optional, default: look and feel dependend)"},
              {BACKGROUND_IMG, "url",
                "URL of a background image for the GUI (optional, default: no image)"},
              {HELP_URL, "url",
                "URL for locating help files, e.g. '../help/' (no help provided if missing)"}};
  }

  /**
   * Factory method to create and wire HelpListener, GUI and BKUWorker.
   * (Config via applet parameters, see BKUApplet.* constants)
   */
  @Override
  public void init() {
    log.info("Welcome to MOCCA {}.", VERSION);
    log.trace("Called init().");
    showStatus("Initializing MOCCA applet.");

		// workaround to bug, cf.
		// http://forums.oracle.com/forums/thread.jspa?messageID=7432921
		// http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6993691
		// http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6993073
		// and http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=7001786
		@SuppressWarnings("unused")
		ParserDelegator workaround = new ParserDelegator();

    HttpsURLConnection.setDefaultSSLSocketFactory(InternalSSLSocketFactory.getInstance());

    String locale = getParameter(LOCALE);
    log.trace("requested locale: {}, jvm default locale: {}", locale, Locale.getDefault());
    if (locale != null) {
      if (locale.indexOf('_') > 0) {
        locale = locale.substring(0, locale.indexOf('_'));
      }
      this.setLocale(new Locale(locale));
    }
    log.trace("Applet locale set to: {} (will be used as a hint for resource bundle loading).", getLocale());

    if (Boolean.parseBoolean(getParameter(ENFORCE_RECOMMENDED_PIN_LENGTH))) {
      SignatureCardFactory.ENFORCE_RECOMMENDED_PIN_LENGTH = true;
    }
    log.debug("Enforce recommended pin length = {}.", SignatureCardFactory.ENFORCE_RECOMMENDED_PIN_LENGTH);
    
    BKUGUIFacade.Style guiStyle;
    String guiStyleParam = getParameter(GUI_STYLE);
    if ("advanced".equalsIgnoreCase(guiStyleParam)) {
      guiStyle = BKUGUIFacade.Style.advanced;
    } else if ("tiny".equalsIgnoreCase(guiStyleParam)) {
      guiStyle = BKUGUIFacade.Style.tiny;
    } else {
      guiStyle = BKUGUIFacade.Style.simple;
    }
    log.debug("Setting gui-style: {}.", guiStyle);

    URL backgroundImgURL = null;
    try {
      backgroundImgURL = getURLParameter(BACKGROUND_IMG);
      log.debug("Setting background: {}.", backgroundImgURL);
    } catch (MalformedURLException ex) {
      log.info("Cannot load applet background image. {}", ex.getMessage());
    }

    try {
      URL helpUrl = getURLParameter(HELP_URL);
      helpListener = new HelpListener(helpUrl, getLocale());
      log.debug("Setting help URL: {}", helpUrl);
    } catch (MalformedURLException e) {
      log.warn("Cannot set help URL. {}", e.getMessage());
    }

//ViewerHelpListener example:
//    try {
//      String absoluteHelpURL = new URL(getCodeBase(), getParameter(HELP_URL)).toString();
//      helpListener = new ViewerHelpListener(getAppletContext(), absoluteHelpURL, getLocale());
//    } catch (MalformedURLException ex) {
//      log.error("invalid help URL, help disabled", ex);
//    }
    
    // Note: We need a panel in order to be able to set the background
    // properly.
    // Setting the background without a panel has side effects with the
    // different java plugins.
    JPanel contentPanel = new JPanel();
    getContentPane().add(contentPanel);

    String backgroundColor = getParameter(BACKGROUND_COLOR);
    if (backgroundColor != null && backgroundColor.startsWith("#")) {
      try {
        Color color = new Color(Integer.parseInt(backgroundColor.substring(1), 16));
        log.debug("Setting background color to {}.", color);
        contentPanel.setBackground(color);
      } catch (NumberFormatException e) {
        log.debug("Failed to set background color '{}'.", backgroundColor);
      }
    }

    BKUGUIFacade gui = null;
    URLFontLoader fontProvider = null;
      
    try {
      fontProvider =  new URLFontLoader(getCodeBase());
      fontProvider.loadInBackground();
      
    } catch (MalformedURLException ex) {
      log.error("Failed to load font provider URL.", ex);
      System.err.println("invalid font provider URL " + ex.getMessage());
    }
    gui = createGUI(contentPanel, getLocale(), guiStyle,
              backgroundImgURL, fontProvider, helpListener);
    worker = createBKUWorker(this, gui);
  }

  @Override
  public void start() {
    log.trace("Called start().");
    if (worker != null) {
      showStatus("Starting MOCCA applet");
      workerThread = new Thread(worker);
      workerThread.start();
    } else {
      log.debug("Cannot start uninitialzed MOCCA applet.");
    }
  }

  @Override
  public void stop() {
    log.trace("Called stop().");
    showStatus("Stopping MOCCA applet");
    if ((workerThread != null) && (workerThread.isAlive())) {
      workerThread.interrupt();
    }
  }

  @Override
  public void destroy() {
    log.trace("Called destroy().");
  }

  public String getHelpURL() {
    return helpListener.getHelpURL();
  }

  // ///////////////////////////////////////////////////////////////////////////
  // factory methods for subclasses to inject different components
  // ///////////////////////////////////////////////////////////////////////////
  protected BKUGUIFacade createGUI(Container contentPane, Locale locale,
          Style guiStyle, URL backgroundImgURL,
          FontProvider fontProvider, HelpListener helpListener) {
    return new BKUGUIImpl(contentPane, locale, guiStyle, backgroundImgURL,
            fontProvider, helpListener);
  }

  protected AppletBKUWorker createBKUWorker(BKUApplet applet, BKUGUIFacade gui) {
    return new AppletBKUWorker(applet, gui);
  }

  // ///////////////////////////////////////////////////////////////////////////
  // callback for BKUWorker to allow extension
  // ///////////////////////////////////////////////////////////////////////////
  /**
   * Callback for BKUWorker to allow extension
   *
   * @return
   * @throws java.net.MalformedURLException
   */
  public STALPortType getSTALPort() throws MalformedURLException {
    URL wsdlURL = getURLParameter(WSDL_URL);
    if (wsdlURL.toString().contains(";jsessionid="))
        wsdlURL = new URL(wsdlURL.toString().replaceAll("\\;jsessionid=[^\\?#]*", ""));
    log.debug("Setting STAL WSDL: {}.", wsdlURL);
    QName endpointName = new QName(STAL_WSDL_NS, STAL_SERVICE);
    STALService stal = new STALService(wsdlURL, endpointName);
    return stal.getSTALPort();
  }

  /**
   * Callback for BKUWorker to allow extension (TODO STALPort could know its
   * STALTranslator)
   *
   * @return
   * @throws java.net.MalformedURLException
   */
  public STALTranslator getSTALTranslator() {
    return new STALTranslator();
  }

  /**
   * Callback for BKUWorker to keep applet context out of BKUWorker
   *
   * @return
   * @throws java.net.MalformedURLException
   */
  protected void sendRedirect() {
    try {
      AppletContext ctx = getAppletContext();
      if (ctx == null) {
        log.error("No applet context (applet might already have been destroyed).");
        return;
      }
      URL redirectURL = getURLParameter(REDIRECT_URL);
      String redirectTarget = getParameter(REDIRECT_TARGET);
      if (redirectTarget == null) {
        log.info("Done. Redirecting to {}.", redirectURL);
        ctx.showDocument(redirectURL);
      } else {
        log.info("Done. Redirecting to {} (target={}).", redirectURL, redirectTarget);
        ctx.showDocument(redirectURL, redirectTarget);
      }
    } catch (MalformedURLException ex) {
      log.warn("Failed to redirect.", ex);
    }
  }

	public void getFocusFromBrowser() {
		log.debug("Obtained focus from browser.");

		AccessController.doPrivileged(new PrivilegedAction<Void>() {
			@Override
			public Void run() {
				worker.getFocusFromBrowser();
				return null;
			}
		});
	}

private boolean checkPermissions() {
	try {
		AccessController.checkPermission(new AllPermission());
		return true;
	} catch (Exception e) {
		if (log.isDebugEnabled())
		{
			StackTraceElement[] st = e.getStackTrace();
			String method = "unknown";
			for (int i = 0; i < st.length; i++) {
				if (st[i].getClassName().equals(this.getClass().getName())) {
					method = st[i+1].getMethodName();
					break;
				}
			}
			log.debug("Not enough permissions for " + method, e);
		}
	}
	return false;
}

@Override
public AccessibleContext getAccessibleContext() {
	return checkPermissions() ? super.getAccessibleContext() : null;
}

@Override
public Container getContentPane() {
	return checkPermissions() ? super.getContentPane() : null;
}

@Override
public Component getGlassPane() {
	return checkPermissions() ? super.getGlassPane() : null;
}

@Override
public JMenuBar getJMenuBar() {
	return checkPermissions() ? super.getJMenuBar() : null;
}

@Override
public JLayeredPane getLayeredPane() {
	return checkPermissions() ? super.getLayeredPane() : null;
}

@Override
public JRootPane getRootPane() {
	return checkPermissions() ? super.getRootPane() : null;
}

@Override
public Component findComponentAt(int arg0, int arg1) {
	return checkPermissions() ? super.findComponentAt(arg0, arg1) : null;
}

@Override
public Component findComponentAt(Point arg0) {
	return checkPermissions() ? super.findComponentAt(arg0) : null;
}

@Override
public Component getComponent(int arg0) {
	return checkPermissions() ? super.getComponent(arg0) : null;
}

@Override
public Component getComponentAt(int arg0, int arg1) {
	return checkPermissions() ? super.getComponentAt(arg0, arg1) : null;
}

@Override
public Component getComponentAt(Point arg0) {
	return checkPermissions() ? super.getComponentAt(arg0) : null;
}

@Override
public Component[] getComponents() {
	return checkPermissions() ? super.getComponents() : null;
}

@SuppressWarnings("deprecation")
@Override
public Component locate(int arg0, int arg1) {
	return checkPermissions() ? super.locate(arg0, arg1) : null;
}

@Override
public Container getParent() {
	return checkPermissions() ? super.getParent() : null;
}

  // ///////////////////////////////////////////////////////////////////////////
  // utility methods
  // ///////////////////////////////////////////////////////////////////////////
  protected URL getURLParameter(String paramKey)
          throws MalformedURLException {
    String urlParam = getParameter(paramKey);
    if (urlParam != null && !urlParam.isEmpty()) {
      try {
        return new URL(getCodeBase(), urlParam);
      } catch (MalformedURLException ex) {
        log.error("Applet parameter {} ist not a valid URL. {}", urlParam, ex.getMessage());
        throw ex;
      }
    } else {
      log.info("Applet parameter {} not set.", paramKey);
      throw new MalformedURLException(paramKey + " not set");
    }
  }
}
