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
package at.gv.egiz.bku.gui;

import java.applet.AppletContext;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ViewerHelpListener extends HelpListener {
  
  private final Logger log = LoggerFactory.getLogger(ViewerHelpListener.class);

  protected AppletContext appletCtx;

  public ViewerHelpListener(String helpURL, Locale locale) {
    super(helpURL, locale);
  }

  public ViewerHelpListener(AppletContext ctx, String helpURL, Locale locale) {
    super(helpURL, locale);
    this.appletCtx = ctx;
  }

  protected void displayHelpViewer(final String helpURL) {
    log.debug("Schedule help viewer.");

    SwingUtilities.invokeLater(new Runnable() {

      @Override
      public void run() {

        log.debug("Show help viewer for {}.", helpURL);
        try {
          HelpViewer.showHelpDialog(appletCtx, new URL(helpURL), messageBundle);
        } catch (MalformedURLException ex) {
          log.error("Failed to construct help context URL.", ex);
        }
      }
    });
  }

  @Override
  public boolean implementsListener() {
    return true;
  }

  @Override
  public void mouseClicked(MouseEvent arg0) {
    displayHelpViewer(getHelpURL());
  }

  @Override
  public void keyPressed(KeyEvent arg0) {
    displayHelpViewer(getHelpURL());
  }
}
