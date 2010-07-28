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
package at.gv.egiz.bku.local.stal;

import at.gv.egiz.bku.viewer.ResourceFontLoader;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;


import at.gv.egiz.bku.gui.BKUGUIFacade;
import at.gv.egiz.bku.gui.BKUIcons;
import at.gv.egiz.bku.gui.PINManagementGUI;
import at.gv.egiz.bku.gui.PINManagementGUIFacade;
import at.gv.egiz.bku.local.gui.GUIProxy;
import at.gv.egiz.bku.local.gui.LocalHelpListener;
import at.gv.egiz.stal.STAL;
import at.gv.egiz.stal.STALFactory;
import javax.swing.JFrame;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates a PINManagementGUI and a LocalBKUWorker, which in turn registers
 * PINManagementRequestHandler from smccSTALExt.
 * The RequestHandler expects PINManagementGUIFacade, therefore BKUGUIProxy has to implement the extended GUI.
 * @author clemens
 */
public class LocalSTALFactory implements STALFactory {

  private final Logger log = LoggerFactory.getLogger(LocalSTALFactory.class);
  protected static final Dimension PREFERRED_SIZE = new Dimension(318, 200);
  protected URL helpURL;
  protected Locale locale;
  
  protected Configuration configuration;
  
  

  @Override
  public STAL createSTAL() {

    final LocalBKUWorker stal;
    //http://java.sun.com/docs/books/tutorial/uiswing/misc/focus.html
    // use undecorated JFrame instead of JWindow,
    // which creates an invisible owning frame and therefore cannot getFocusInWindow()
    JFrame dialog = new JFrame("Bürgerkarte");
    log.debug("AlwaysOnTop supported: {}.", dialog.isAlwaysOnTopSupported());
    // [#439] make mocca dialog alwaysOnTop
    dialog.setAlwaysOnTop(true);
    dialog.setIconImages(BKUIcons.icons);
//    dialog.setUndecorated(true);
//    dialog.getRootPane().setWindowDecorationStyle(JRootPane.NONE);

    if (locale != null) {
      dialog.setLocale(locale);
    }
    LocalHelpListener helpListener = null;
    if (helpURL != null) {
      helpListener = new LocalHelpListener(helpURL, locale);
    } else {
      log.warn("No HELP URL configured, help system disabled.");
    }
    PINManagementGUIFacade gui = new PINManagementGUI(dialog.getContentPane(),
            dialog.getLocale(),
            null,
            new ResourceFontLoader(),
            helpListener,
            null);
    BKUGUIFacade proxy = (BKUGUIFacade) GUIProxy.newInstance(gui, dialog, new Class[] { PINManagementGUIFacade.class} );
    stal = new LocalBKUWorker(proxy, dialog);
    dialog.setPreferredSize(PREFERRED_SIZE);
    dialog.pack();
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension frameSize = dialog.getSize();
    if (frameSize.height > screenSize.height) {
      frameSize.height = screenSize.height;
    }
    if (frameSize.width > screenSize.width) {
      frameSize.width = screenSize.width;
    }
    dialog.setLocation((screenSize.width - frameSize.width) / 2,
            (screenSize.height - frameSize.height) / 2);
    return stal;
  }

  @Override
  public void setLocale(Locale locale) {
    this.locale = locale;
  }

  /**
   * spring injects helpURL
   * @param helpURL
   * @throws MalformedURLException if helpURL is not a valid URL
   */
  public void setHelpURL(String helpURL) throws MalformedURLException {
    this.helpURL = new URL(helpURL);
  }

  /**
   * @return the configuration
   */
  public Configuration getConfiguration() {
    return configuration;
  }

  /**
   * @param configuration the configuration to set
   */
  public void setConfiguration(Configuration configuration) {
    this.configuration = configuration;
  }
  
}
