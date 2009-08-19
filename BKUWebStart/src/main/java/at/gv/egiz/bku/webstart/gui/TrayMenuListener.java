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
package at.gv.egiz.bku.webstart.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.util.ResourceBundle;
import javax.swing.JFrame;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author Clemens Orthacker <clemens.orthacker@iaik.tugraz.at>
 */
public class TrayMenuListener implements ActionListener {

  /** action commands for tray menu */
  public static final String SHUTDOWN_COMMAND = "shutdown";
  public static final String PIN_COMMAND = "pin";
  public static final String ABOUT_COMMAND = "about";

  private static final Log log = LogFactory.getLog(TrayMenuListener.class);

  protected BKUControllerInterface bku;
  protected ResourceBundle messages;
  protected String version;
  protected AboutDialog aboutDialog;

  public TrayMenuListener(BKUControllerInterface bkuHook, ResourceBundle messages, String version) {
    this.messages = messages;
    this.version = version;
    this.bku = bkuHook;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (SHUTDOWN_COMMAND.equals(e.getActionCommand())) {
      log.debug("shutdown requested via tray menu");
      bku.shutDown();
    } else if (ABOUT_COMMAND.equals(e.getActionCommand())) {
      log.debug("about dialog requested via tray menu");
      if (aboutDialog == null) {
        aboutDialog = new AboutDialog(new JFrame(), true, version);
        aboutDialog.addWindowListener(new WindowAdapter() {
          @Override
          public void windowClosing(java.awt.event.WindowEvent e) {
            aboutDialog.setVisible(false);
          }
        });
      }
      aboutDialog.setLocationByPlatform(true);
      aboutDialog.setVisible(true);
    } else if (PIN_COMMAND.equals(e.getActionCommand())) {
      log.error("not implemented yet.");
    } else {
      log.error("unknown tray menu command: " + e.getActionCommand());
    }
  }
}
