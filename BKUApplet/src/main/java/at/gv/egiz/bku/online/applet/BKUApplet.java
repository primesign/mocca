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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.HttpsURLConnection;
import javax.swing.JApplet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import at.gv.egiz.bku.gui.BKUGUIFacade;
import at.gv.egiz.bku.gui.BKUGUIFactory;

/**
 * Note: all swing code is executed by the event dispatch thread (see
 * BKUGUIFacade)
 */
public class BKUApplet extends JApplet {

  private static Log log = LogFactory.getLog(BKUApplet.class);
  public static final String GUI_STYLE = "GuiStyle";
  public final static String LOCALE_PARAM_KEY = "Locale";
  public final static String LOGO_URL_KEY = "LogoURL";
  public final static String WSDL_URL = "WSDL_URL";
  public static final String HASHDATA_DISPLAY = "HashDataDisplay";
  public final static String HASHDATA_URL = "HashDataURL";
  public final static String HELP_URL = "HelpURL";
  public final static String SESSION_ID = "SessionID";
  public static final String BACKGROUND_PARAM = "Background";
  public static final String REDIRECT_URL = "RedirectURL";
  public static final String REDIRECT_TARGET = "RedirectTarget";
  public static final String HASHDATA_DISPLAY_INTERNAL = "internal";
  protected BKUWorker worker;
  protected Thread workerThread;

  public BKUApplet() {
  }

  @Override
  public void init() {
    log.info("Welcome to MOCCA\n");
    log.debug("Called init()");
    HttpsURLConnection.setDefaultSSLSocketFactory(InternalSSLSocketFactory.getInstance());
    String locale = getMyAppletParameter(LOCALE_PARAM_KEY);
    if (locale != null) {
      this.setLocale(new Locale(locale));
    }
    String backgroundString = getMyAppletParameter(BACKGROUND_PARAM);
    URL background = null;
    if (backgroundString != null) {
      try {
        background = new URL(backgroundString);
      } catch (MalformedURLException ex) {
        log.warn(ex.getMessage() + ", using default background");
      }
    }
    String guiStyle = getMyAppletParameter(GUI_STYLE);
    BKUGUIFacade gui = BKUGUIFactory.createGUI(guiStyle);
    log.debug("setting GUI locale to " + getLocale());
    AppletHelpListener helpListener = null;
    try {
      URL helpURL = getMyAppletParameterURL(HELP_URL);
      helpListener = new AppletHelpListener(getAppletContext(), helpURL, getLocale());
    } catch (MalformedURLException ex) {
      log.error("invalid help URL: " + ex.getMessage());
    }
    gui.init(getContentPane(), getLocale(), background, helpListener);
    worker = new BKUWorker(gui, this);
  }

  @Override
  public void start() {
    log.debug("Called start()");
    workerThread = new Thread(worker);
    workerThread.start();
  }

  @Override
  public void stop() {
    log.debug("Called stop()");
    if ((workerThread != null) && (workerThread.isAlive())) {
      workerThread.interrupt();
    }
  }

  @Override
  public void destroy() {
    log.debug("Called destroy()");
  }

  /**
   * Applet configuration parameters
   * 
   * @param paramKey
   * @return
   */
  String getMyAppletParameter(String paramKey) {
    log.info("Getting parameter: " + paramKey + ": " + getParameter(paramKey));
    return getParameter(paramKey);
  }

  URL getMyAppletParameterURL(String param) throws MalformedURLException {
    String hashDataParam = getMyAppletParameter(param); //BKUApplet.HASHDATA_URL);
    if (hashDataParam != null) {
      URL codebase = getCodeBase();
      try {
        return new URL(codebase, hashDataParam);
      } catch (MalformedURLException ex) {
        log.error("Paremeter " + param + " is not a valid URL.", ex);
        throw new MalformedURLException(ex.getMessage());
      }
    } else {
      log.error("Paremeter " + param + " not set");
      throw new MalformedURLException(param + " not set");
    }
  }
}
