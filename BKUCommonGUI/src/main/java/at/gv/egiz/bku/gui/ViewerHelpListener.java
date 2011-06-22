/*
 * Copyright 2011 by Graz University of Technology, Austria
 * MOCCA has been developed by the E-Government Innovation Center EGIZ, a joint
 * initiative of the Federal Chancellery Austria and Graz University of Technology.
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * http://www.osor.eu/eupl/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 *
 * This product combines work with different licenses. See the "NOTICE" text
 * file for details on the various modules and licenses.
 * The "NOTICE" text file is part of the distribution. Any derivative works
 * that you distribute must include a readable copy of the "NOTICE" text file.
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

  public ViewerHelpListener(URL baseURL, Locale locale) {
    super(baseURL, locale);
  }

  public ViewerHelpListener(AppletContext ctx, URL baseURL, Locale locale) {
    super(baseURL, locale);
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
