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
package at.gv.egiz.bku.webstart;

import at.gv.egiz.bku.webstart.Launcher;
import at.gv.egiz.bku.webstart.gui.StatusNotifier;
import java.io.IOException;
import java.net.HttpURLConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GUI is painted using SwingUtilities.invokeLater, but TrayIcon ActionListener Thread (== webstart thread) joined Jetty Thread
 * 
 * @author Clemens Orthacker <clemens.orthacker@iaik.tugraz.at>
 */
public class PINManagementInvoker implements Runnable {

  private static final Logger log = LoggerFactory.getLogger(PINManagementInvoker.class);
  
  StatusNotifier status;

  public PINManagementInvoker(StatusNotifier status) {
      this.status = status;
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
      status.error(StatusNotifier.ERROR_PIN);
    } finally {
      if (connection != null) {
        connection.disconnect();
      }
    }
  }
}
