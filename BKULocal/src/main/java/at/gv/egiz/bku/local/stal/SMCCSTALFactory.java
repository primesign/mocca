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
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.JDialog;
import javax.swing.WindowConstants;

import at.gv.egiz.bku.gui.BKUGUIFacade;
import at.gv.egiz.bku.gui.BKUGUIFactory;
import at.gv.egiz.bku.online.applet.BKUApplet;
import at.gv.egiz.bku.online.applet.QuitHandler;
import at.gv.egiz.bku.smccstal.AbstractSMCCSTAL;
import at.gv.egiz.stal.QuitRequest;
import at.gv.egiz.stal.STAL;
import at.gv.egiz.stal.STALFactory;

public class SMCCSTALFactory implements STALFactory {

  private Locale locale;

  @Override
  public STAL createSTAL() {
    AbstractSMCCSTAL.addRequestHandler(QuitRequest.class, QuitHandler.getInstance());
    SMCCSTAL stal;
    JDialog dialog;
    ResourceBundle resourceBundle;
    if (locale != null) {
      resourceBundle = ResourceBundle.getBundle(BKUApplet.RESOURCE_BUNDLE_BASE,
          locale);
    } else {
      resourceBundle = ResourceBundle.getBundle(BKUApplet.RESOURCE_BUNDLE_BASE);
    }
    dialog = new JDialog();
    BKUGUIFacade gui = BKUGUIFactory.createGUI();
    gui.init(dialog.getContentPane(), locale.toString(), null);
    stal = new SMCCSTAL(new BKUGuiProxy(dialog, gui), dialog, resourceBundle);
    dialog.setPreferredSize(new Dimension(400, 200));
    dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    dialog.setTitle("MOCCA");
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
}
