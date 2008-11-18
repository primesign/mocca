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
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Locale;
import javax.swing.SwingUtilities;

/**
 * 
 * @author Clemens Orthacker <clemens.orthacker@iaik.tugraz.at>
 */
public class DefaultHelpListener extends AbstractHelpListener {

  protected AppletContext ctx;

  /**
   * 
   * @param ctx open external links via applet context
   * @param helpURL
   * @param locale
   */
  public DefaultHelpListener(AppletContext ctx, URL helpURL, Locale locale) {
    super(helpURL, locale);
    this.ctx = ctx;
  }

  /**
   * external links in help document are not opened
   * @param helpURL
   * @param locale
   */
  public DefaultHelpListener(URL helpURL, Locale locale) {
    super(helpURL, locale);
    this.ctx = null;
  }

  /**
   * blocks until help viewer returns (is closed)
   * @param helpURL
   * @param helpTopic
   */
  @Override
  public void showDocument(final URL helpURL, final String helpTopic)  { 
//    try {
      log.debug("schedule help dialog");

//      SwingUtilities.invokeAndWait(new Runnable() {
      SwingUtilities.invokeLater(new Runnable() {

        @Override
        public void run() {

          log.debug("show help dialog");

          if (ctx == null) {
            HelpViewer.showHelpDialog(helpURL, helpTopic, messages);
          } else {
            HelpViewer.showHelpDialog(ctx, helpURL, helpTopic, messages);
          }
        }
      });
//    } catch (InterruptedException ex) {
//      log.error("Failed to display HelpViewer: " + ex.getMessage(), ex);
//    } catch (InvocationTargetException ex) {
//      log.error("Failed to display HelpViewer: " + ex.getMessage(), ex);
//    }
  }
}
