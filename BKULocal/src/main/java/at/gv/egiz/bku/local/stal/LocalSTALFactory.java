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
import java.net.MalformedURLException;
import java.util.Locale;

import javax.swing.JDialog;
import javax.swing.WindowConstants;

import at.gv.egiz.bku.gui.BKUGUIFacade;
import at.gv.egiz.bku.gui.BKUGUIImpl;
import at.gv.egiz.bku.gui.DefaultHelpListener;
import at.gv.egiz.bku.local.gui.LocalHelpListener;
import at.gv.egiz.stal.STAL;
import at.gv.egiz.stal.STALFactory;
import java.net.URL;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LocalSTALFactory implements STALFactory {

  protected static final Log log = LogFactory.getLog(LocalSTALFactory.class);
  protected String helpURL;
  protected Locale locale;

  @Override
  public STAL createSTAL() {

    LocalBKUWorker stal;
    JDialog dialog = new JDialog();
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
    BKUGUIFacade gui = new BKUGUIImpl(dialog.getContentPane(),
            dialog.getLocale(),
            BKUGUIFacade.Style.advanced,
            null,
            helpListener);
    stal = new LocalBKUWorker(new BKUGuiProxy(dialog, gui), dialog);
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

  public String getHelpURL() {
    return helpURL;
  }

  public void setHelpURL(String helpURL) {
    this.helpURL = helpURL;
  }
}
