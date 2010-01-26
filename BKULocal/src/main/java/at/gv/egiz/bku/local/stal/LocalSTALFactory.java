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
import java.util.Locale;


import at.gv.egiz.bku.gui.BKUGUIFacade;
import at.gv.egiz.bku.gui.BKUIcons;
import at.gv.egiz.bku.gui.PINManagementGUI;
import at.gv.egiz.bku.gui.PINManagementGUIFacade;
import at.gv.egiz.bku.local.gui.GUIProxy;
import at.gv.egiz.bku.local.gui.LocalHelpListener;
import at.gv.egiz.stal.STAL;
import at.gv.egiz.stal.STALFactory;
import java.net.URL;
import javax.swing.JFrame;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Creates a PINManagementGUI and a LocalBKUWorker, which in turn registers
 * PINManagementRequestHandler from smccSTALExt.
 * The RequestHandler expects PINManagementGUIFacade, therefore BKUGUIProxy has to implement the extended GUI.
 * @author clemens
 */
public class LocalSTALFactory implements STALFactory {

  protected static final Log log = LogFactory.getLog(LocalSTALFactory.class);
  protected static final Dimension PREFERRED_SIZE = new Dimension(318, 200);
  protected String helpURL;
  protected Locale locale;

  @Override
  public STAL createSTAL() {

    LocalBKUWorker stal;
    //http://java.sun.com/docs/books/tutorial/uiswing/misc/focus.html
    // use undecorated JFrame instead of JWindow,
    // which creates an invisible owning frame and therefore cannot getFocusInWindow()
    JFrame dialog = new JFrame("Bürgerkarte");
    if (log.isTraceEnabled()) {
      log.debug("alwaysOnTop supported: " + dialog.isAlwaysOnTopSupported());
    }
    // [#439] make mocca dialog alwaysOnTop
    dialog.setAlwaysOnTop(true);
    dialog.setIconImages(BKUIcons.icons);
    dialog.setUndecorated(true);
//    dialog.getRootPane().setWindowDecorationStyle(JRootPane.NONE);
//    dialog.addWindowListener(new WindowAdapter() {
//
//      @Override
//      public void windowClosing(WindowEvent e) {
//        super.windowClosing(e);
//        log.debug("closing window ********************");
//      }
//
//    });
    if (locale != null) {
      dialog.setLocale(locale);
    }
    LocalHelpListener helpListener = null;
    try {
      if (helpURL != null) {
        helpListener = new LocalHelpListener(new URL(helpURL), locale);
      } else {
        log.warn("no HELP URL configured, help system disabled");
      }
    } catch (MalformedURLException ex) {
      log.error("failed to configure help listener: " + ex.getMessage(), ex);
    }
    PINManagementGUIFacade gui = new PINManagementGUI(dialog.getContentPane(),
            dialog.getLocale(),
            BKUGUIFacade.Style.advanced,
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

  public String getHelpURL() {
    return helpURL;
  }

  public void setHelpURL(String helpURL) {
    this.helpURL = helpURL;
  }
}
