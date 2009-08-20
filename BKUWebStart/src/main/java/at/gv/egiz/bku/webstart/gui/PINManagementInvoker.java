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

import at.gv.egiz.bku.webstart.Launcher;
import java.awt.TrayIcon;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ResourceBundle;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * GUI is painted using SwingUtilities.invokeLater, but TrayIcon ActionListener Thread (== webstart thread) joined Jetty Thread
 * 
 * @author Clemens Orthacker <clemens.orthacker@iaik.tugraz.at>
 */
public class PINManagementInvoker implements Runnable {

  private static final Log log = LogFactory.getLog(PINManagementInvoker.class);
  
  TrayIcon trayIcon;
  ResourceBundle messages;

  public PINManagementInvoker(TrayIcon trayIcon, ResourceBundle messages) {
    this.trayIcon = trayIcon;
    this.messages = messages;
  }

  @Override
  public void run() {
    HttpURLConnection connection = null;
    try {
      log.debug("Connecting to: " + Launcher.PIN_MANAGEMENT_URL);

      connection = (HttpURLConnection) Launcher.PIN_MANAGEMENT_URL.openConnection();

      connection.setRequestMethod("GET");
      connection.setReadTimeout(0);
      connection.connect();

      if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
        log.debug("pin management dialog returned");
      } else {
        log.error("unexpected response from pin management: " + connection.getResponseMessage());
      }
    } catch (IOException ex) {
      log.error("Failed to connect to PIN Management", ex);
      trayIcon.displayMessage(messages.getString(Launcher.CAPTION_ERROR),
              messages.getString(Launcher.ERROR_PIN), TrayIcon.MessageType.ERROR);
    } finally {
      if (connection != null) {
        connection.disconnect();
      }
    }
  }
}
