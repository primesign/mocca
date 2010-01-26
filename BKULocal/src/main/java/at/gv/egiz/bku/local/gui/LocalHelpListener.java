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
package at.gv.egiz.bku.local.gui;

import at.gv.egiz.bku.gui.AbstractHelpListener;
import at.gv.egiz.bku.gui.DefaultHelpListener;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Locale;

/**
 * Open help document in browser, fallback to default (swing dialog) if Java Desktop API not supported.
 * 
 * @author Clemens Orthacker <clemens.orthacker@iaik.tugraz.at>
 */
public class LocalHelpListener extends AbstractHelpListener {

  protected Desktop desktop;
  protected DefaultHelpListener fallback;

  public LocalHelpListener(URL baseURL, Locale locale) {
    super(baseURL, locale);
    if (Desktop.isDesktopSupported()) {
      desktop = Desktop.getDesktop();
    } else {
      log.info("Java Desktop API not available on current platform (libgnome installed?), falling back to DefaultHelpListener");
      fallback = new DefaultHelpListener(baseURL, locale);
    }
  }

  @Override
  public void showDocument(URL helpDocument, String helpTopic) throws IOException, URISyntaxException {
    if (desktop != null) {
      if (!desktop.isSupported(Desktop.Action.BROWSE)) {
        log.error("Failed to open default browser: The system provides the Desktop API, but does not support the BROWSE action");
      } else {
        Desktop.getDesktop().browse(helpDocument.toURI());
      }
    } else if (fallback != null) {
      fallback.showDocument(helpDocument, helpTopic);
    } else {
      log.error("failed to display help document");
    }
  }
}
