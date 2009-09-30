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

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Locale;


import at.gv.egiz.bku.gui.BKUGUIFacade;
import at.gv.egiz.bku.gui.BKUGUIImpl;
import at.gv.egiz.bku.gui.PINManagementGUI;
import at.gv.egiz.bku.gui.PINManagementGUIFacade;
import at.gv.egiz.bku.local.gui.GUIProxy;
import at.gv.egiz.bku.local.gui.LocalHelpListener;
import at.gv.egiz.stal.STAL;
import at.gv.egiz.stal.STALFactory;
import java.awt.Image;
import java.awt.event.WindowAdapter;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JRootPane;
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
  protected static ArrayList<Image> icons = new ArrayList<Image>();
  static {
    String[] iconResources = new String[] {
      "/at/gv/egiz/bku/gui/chip16.png",
      "/at/gv/egiz/bku/gui/chip24.png",
      "/at/gv/egiz/bku/gui/chip32.png",
      "/at/gv/egiz/bku/gui/chip48.png",
      "/at/gv/egiz/bku/gui/chip128.png" };
    for (String ir : iconResources) {
      URL resource = LocalSTALFactory.class.getResource(ir);
      if (ir != null) {
        try {
          icons.add(ImageIO.read(resource));
        } catch (IOException ex) {
          log.warn("failed to set ui dialog icon", ex);
        }
      }
    }
  }
  protected String helpURL;
  protected Locale locale;

  @Override
  public STAL createSTAL() {

    LocalBKUWorker stal;
    //http://java.sun.com/docs/books/tutorial/uiswing/misc/focus.html
    // use undecorated JFrame instead of JWindow,
    // which creates an invisible owning frame and therefore cannot getFocusInWindow()
    JFrame dialog = new JFrame("Bürgerkarte");
    dialog.setIconImages(icons);
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
            helpListener);
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
