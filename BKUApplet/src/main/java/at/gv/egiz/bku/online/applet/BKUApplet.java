/*
 * Copyright 2008 Federal Chancellery Austria and
 * Graz University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package at.gv.egiz.bku.online.applet;

import at.gv.egiz.bku.gui.BKUGUIFacade.Style;
import at.gv.egiz.bku.gui.DefaultHelpListener;
import at.gv.egiz.bku.gui.AbstractHelpListener;
import at.gv.egiz.stal.service.translator.STALTranslator;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;
import javax.swing.JApplet;
import javax.swing.JPanel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import at.gv.egiz.bku.gui.BKUGUIFacade;
import at.gv.egiz.bku.gui.BKUGUIImpl;
import at.gv.egiz.stal.service.STALPortType;
import at.gv.egiz.stal.service.STALService;
import java.applet.AppletContext;
import java.awt.Color;
import java.awt.Container;
import javax.xml.namespace.QName;

/**
 * Note: all swing code is executed by the event dispatch thread (see
 * BKUGUIFacade)
 */
public class BKUApplet extends JApplet {

  private static final long serialVersionUID = 1L;
  
  private static Log log = LogFactory.getLog(BKUApplet.class);
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
  public static final String HASHDATA_DISPLAY_FRAME = "frame";
  /**
   * STAL WSDL namespace and service name
   */
  public static final String STAL_WSDL_NS = "http://www.egiz.gv.at/wsdl/stal";
  public static final String STAL_SERVICE = "STALService";
  /**
   * Dummy session id, used if no sessionId parameter is provided
   */
  protected static final String TEST_SESSION_ID = "TestSession";

  static {
    if (log.isTraceEnabled()) {
      log.trace("enabling webservice communication dump");
      System.setProperty("com.sun.xml.ws.transport.http.client.HttpTransportPipe.dump", "true");
    }
  }

  /**
   * STAL
   */
  protected AppletBKUWorker worker;
  protected Thread workerThread;

  /* (non-Javadoc)
   * @see java.applet.Applet#getParameterInfo()
   */
  @Override
  public String[][] getParameterInfo() {
    return new String[][] {
        {WSDL_URL,          "url",                      "URL of the WSDL of the MOCCA server side STAL"},
        {REDIRECT_URL,      "url",                      "URL to redirect the browser to when finished"},
        {REDIRECT_TARGET,   "frame target",             "name of the target frame for redirection when finished"},
        {LOCALE,            "locale",                   "locale for UI localization (optional, default: system default)"},
        {GUI_STYLE,         "simple, advanced, tiny",   "GUI style (optional, default: simple)"},
        {BACKGROUND_COLOR,  "#hhhhhh",                  "background color, e.g. '#333333' (optional, default: look and feel dependend)"},
        {BACKGROUND_IMG,    "url",                      "URL of a background image for the GUI (optional, default: no image)"},
        {HELP_URL,          "url",                      "URL for locating help files, e.g. '../help/' (no help provided if missing)"}
    };
  }

  /**
   * Factory method to create and wire HelpListener, GUI and BKUWorker.
   * (Config via applet parameters, see BKUApplet.* constants)
   */
  @Override
  public void init() {
    log.info("Welcome to MOCCA");
    log.trace("Called init()");

    HttpsURLConnection.setDefaultSSLSocketFactory(InternalSSLSocketFactory.getInstance());

    String locale = getParameter(LOCALE);
    if (locale != null) {
      this.setLocale(new Locale(locale));
    }
    log.trace("default locale: " + Locale.getDefault());
    log.debug("setting locale: " + getLocale());

    BKUGUIFacade.Style guiStyle;
    String guiStyleParam = getParameter(GUI_STYLE);
    if ("advanced".equals(guiStyleParam)) {
      guiStyle = BKUGUIFacade.Style.advanced;
    } else if ("tiny".equals(guiStyleParam)) {
      guiStyle = BKUGUIFacade.Style.tiny;
    } else {
      guiStyle = BKUGUIFacade.Style.simple;
    }
    log.debug("setting gui-style: " + guiStyle);

    URL backgroundImgURL = null;
    try {
      backgroundImgURL = getURLParameter(BACKGROUND_IMG, null);
      log.debug("setting background: " + backgroundImgURL);
    } catch (MalformedURLException ex) {
      log.warn("cannot load applet background image: " + ex.getMessage());
    }

    AbstractHelpListener helpListener = null;
    try {
      helpListener = new DefaultHelpListener(getAppletContext(), 
              getURLParameter(HELP_URL, null), getLocale());
      if (log.isDebugEnabled()) {
        log.debug("setting helpURL: " + getURLParameter(HELP_URL, null));
      }
    } catch (MalformedURLException ex) {
      log.warn("failed to load help URL: " + ex.getMessage() + ", disabling help");
    }

    // Note: We need a panel in order to be able to set the background properly.
    // Setting the background without a panel has side effects with the
    // different java plugins.
    JPanel contentPanel = new JPanel();
    getContentPane().add(contentPanel);
    
    String backgroundColor = getParameter(BACKGROUND_COLOR);
    if (backgroundColor != null && backgroundColor.startsWith("#")) {
      try {
        Color color = new Color(Integer.parseInt(backgroundColor.substring(1), 16));
        log.debug("setting background color to " + color);
        contentPanel.setBackground(color);
      } catch (NumberFormatException e) {
        log.debug("failed to set background color '" + backgroundColor + "'");
      }
    }
    
    BKUGUIFacade gui = createGUI(contentPanel, getLocale(),
            guiStyle,
            backgroundImgURL,
            helpListener);

    worker = createBKUWorker(this, gui);
  }

  @Override
  public void start() {
    log.trace("Called start()");
    workerThread = new Thread(worker);
    workerThread.start();
  }

  @Override
  public void stop() {
    log.trace("Called stop()");
    if ((workerThread != null) && (workerThread.isAlive())) {
      workerThread.interrupt();
    }
  }

  @Override
  public void destroy() {
    log.trace("Called destroy()");
  }

  /////////////////////////////////////////////////////////////////////////////
  // factory methods for subclasses to inject different components
  /////////////////////////////////////////////////////////////////////////////
  
  protected BKUGUIFacade createGUI(Container contentPane,
          Locale locale,
          Style guiStyle,
          URL backgroundImgURL,
          AbstractHelpListener helpListener) {
    return new BKUGUIImpl(contentPane, locale, guiStyle, backgroundImgURL, helpListener);
  }

  protected AppletBKUWorker createBKUWorker(BKUApplet applet, BKUGUIFacade gui) {
    return new AppletBKUWorker(applet, gui);
  }


  /////////////////////////////////////////////////////////////////////////////
  // callback for BKUWorker to allow extension
  /////////////////////////////////////////////////////////////////////////////

  /**
   * Callback for BKUWorker to allow extension
   * @return
   * @throws java.net.MalformedURLException
   */
  public STALPortType getSTALPort() throws MalformedURLException {
    URL wsdlURL = getURLParameter(WSDL_URL, null);
    log.debug("setting STAL WSDL: " + wsdlURL);
    QName endpointName = new QName(STAL_WSDL_NS, STAL_SERVICE);
    STALService stal = new STALService(wsdlURL, endpointName);
    return stal.getSTALPort();
  }

  /**
   * Callback for BKUWorker to allow extension
   * (TODO STALPort could know its STALTranslator)
   * @return
   * @throws java.net.MalformedURLException
   */
  public STALTranslator getSTALTranslator() {
    return new STALTranslator();
  }

  /**
   * Callback for BKUWorker to keep applet context out of BKUWorker
   * @return
   * @throws java.net.MalformedURLException
   */
  protected void sendRedirect(String sessionId) {
    try {
      AppletContext ctx = getAppletContext();
      if (ctx == null) {
        log.error("no applet context (applet might already have been destroyed)");
        return;
      }
      URL redirectURL = getURLParameter(REDIRECT_URL, sessionId);
      String redirectTarget = getParameter(REDIRECT_TARGET);
      if (redirectTarget == null) {
        log.info("Done. Redirecting to " + redirectURL + " ...");
        ctx.showDocument(redirectURL);
      } else {
        log.info("Done. Redirecting to " + redirectURL + " (target=" + redirectTarget + ") ...");
        ctx.showDocument(redirectURL, redirectTarget);
      }
    } catch (MalformedURLException ex) {
      log.warn("Failed to redirect: " + ex.getMessage(), ex);
    // gui.showErrorDialog(errorMsg, okListener, actionCommand)
    }
  }


  /////////////////////////////////////////////////////////////////////////////
  // utility methods
  /////////////////////////////////////////////////////////////////////////////

  protected URL getURLParameter(String paramKey, String sessionId) throws MalformedURLException {
    String urlParam = getParameter(paramKey);
    if (urlParam != null && !"".equals(urlParam)) {
      URL codebase = getCodeBase();
      try {
        URL url;
        if (codebase.getProtocol().equalsIgnoreCase("file")) {
          // for debugging in appletrunner
          url = new URL(urlParam);
        } else {
          if (sessionId != null) {
            urlParam = urlParam + ";jsessionid=" + sessionId;
          }
          url = new URL(codebase, urlParam);
        }
        return url;
      } catch (MalformedURLException ex) {
        log.error("applet paremeter " + urlParam + " is not a valid URL: " + ex.getMessage());
        throw ex;
      }
    } else {
      log.error("applet paremeter " + paramKey + " not set");
      throw new MalformedURLException(paramKey + " not set");
    }
  }
}
