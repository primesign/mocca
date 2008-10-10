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
import at.gv.egiz.bku.smccstal.AbstractSMCCSTAL;
import at.gv.egiz.stal.QuitRequest;

import java.net.URL;

/**
 * Note: all swing code is executed by the event dispatch thread (see
 * BKUGUIFacade)
 */
public class BKUApplet extends JApplet {

    private static Log log = LogFactory.getLog(BKUApplet.class);
    public final static String RESOURCE_BUNDLE_BASE = "at/gv/egiz/bku/online/applet/Messages";
    public final static String LOCALE_PARAM_KEY = "Locale";
    public final static String LOGO_URL_KEY = "LogoURL";
    public final static String WSDL_URL = "WSDL_URL";
    public final static String SESSION_ID = "SessionID";
    public static final String BACKGROUND_PARAM = "background";
    
    protected ResourceBundle resourceBundle;
    protected BKUWorker worker;
    protected Thread workerThread;

    public BKUApplet() {
    }

    public void init() {
      log.info("Welcome to MOCCA\n");
        log.debug("Called init()");
        AbstractSMCCSTAL.addRequestHandler(QuitRequest.class, QuitHandler.getInstance());
        HttpsURLConnection.setDefaultSSLSocketFactory(InternalSSLSocketFactory.getInstance());
        String localeString = getMyAppletParameter(LOCALE_PARAM_KEY);
        if (localeString != null) {
            resourceBundle = ResourceBundle.getBundle(RESOURCE_BUNDLE_BASE,
              new Locale(localeString));
        } else {
            resourceBundle = ResourceBundle.getBundle(RESOURCE_BUNDLE_BASE);
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
        BKUGUIFacade gui = BKUGUIFactory.createGUI();
        gui.init(getContentPane(), localeString, background);
        worker = new BKUWorker(gui, this, resourceBundle);
    }

    public void start() {
        log.debug("Called start()");
        workerThread = new Thread(worker);
        workerThread.start();
    }

    public void stop() {
        log.debug("Called stop()");
        if ((workerThread != null) && (workerThread.isAlive())) {
            workerThread.interrupt();
        }
    }

    public void destroy() {
        log.debug("Called destroy()");
    }

    /**
     * Applet configuration parameters
     * 
     * @param paramKey
     * @return
     */
    public String getMyAppletParameter(String paramKey) {
        log.info("Getting parameter: " + paramKey + ": " + getParameter(paramKey));
        return getParameter(paramKey);
    }
}
