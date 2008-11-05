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

import at.gv.egiz.bku.gui.AbstractHelpListener;
import java.awt.Desktop;
import java.net.URL;
import java.util.Locale;

/**
 * Now uses java.awt.Desktop, which deprecates 
 * the distinction between local and applet help listener
 * TODO: integrate in AbstractHelpListener
 * 
 * @deprecated 
 * @author clemens
 */
public class AppletHelpListener extends AbstractHelpListener {

//  protected AppletContext ctx;
  protected Desktop desktop;

  public AppletHelpListener(URL helpURL, Locale locale) {
    super(helpURL, locale);
//    if (ctx == null) {
//      throw new RuntimeException("no applet context provided");
//    }
//    this.ctx = ctx;
    if (Desktop.isDesktopSupported()) {
      this.desktop = Desktop.getDesktop();
    }
  }

  @Override
  public void showDocument(URL helpDocument) throws Exception {
//    ctx.showDocument(helpDocument, "_blank");
    if (desktop == null) {
      log.error("Failed to open default browser: Desktop API not available (libgnome installed?)");
    } else {
      if (!desktop.isSupported(Desktop.Action.BROWSE)) {
        log.error("Failed to open default browser: The system provides the Desktop API, but does not support the BROWSE action");
      } else {
        Desktop.getDesktop().browse(helpDocument.toURI());
      }
    }
  }
}
