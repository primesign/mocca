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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Implement the showDocument(URL) method to provide an actual HelpListener.
 * This class does not keep a GUI reference and subclasses should not interfere with the GUI.
 * Therefore, any errors occurring in showDocument() should be handled/displayed within
 * showDocument() and exceptions thrown from showDocument() are logged, not displayed in the GUI.
 * <br/>
 * The help URL is build as [baseURL]/[locale]/[helpTopic].html
 * (note that no session information is contained).
 * 
 * @author Clemens Orthacker <clemens.orthacker@iaik.tugraz.at>
 */
public abstract class AbstractHelpListener implements ActionListener {

  protected final static Log log = LogFactory.getLog(AbstractHelpListener.class);
  protected URL baseURL;
  protected Locale locale;
  protected ResourceBundle messages;

  public AbstractHelpListener(URL baseURL, Locale locale) {
    if (baseURL == null || "".equals(baseURL.toString())) {
      throw new RuntimeException("no help URL provided");
    }
    this.baseURL = baseURL;
    this.locale = locale;
    if (locale != null) {
      messages = ResourceBundle.getBundle(BKUGUIFacade.MESSAGES_BUNDLE, locale);
    } else {
      messages = ResourceBundle.getBundle(BKUGUIFacade.MESSAGES_BUNDLE);
    }
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    log.debug("received help action: " + e.getActionCommand());
    URL helpURL = constructHelpURL(baseURL, e.getActionCommand());
    try {
      showDocument(helpURL, e.getActionCommand());
    } catch (Exception ex) {
      log.error("could not display help document " + helpURL + ": " + ex.getMessage());
    }
  }

  private URL constructHelpURL(URL baseURL, String helpTopic) {
    URL helpURL = baseURL;
    log.trace("constructing help URL: " + helpURL);
    try {
      if (locale != null) {
        helpURL = new URL(helpURL, locale.toString() + "/");
        log.trace("constructing help URL: " + helpURL);
      }
      if (helpTopic != null && !"".equals(helpTopic)) {
        helpURL = new URL(helpURL, helpTopic + ".html");
        log.trace("constructing help URL: " + helpURL);
      }
    } catch (MalformedURLException ex) {
      log.error("Failed to construct help URL for help item " + helpTopic + ": " + ex.getMessage());
    }
    return helpURL;
  }

  /**
   * Errors from HelpListeners should not (are not) displayed in the applet, 
   * but should rather be in the HelpListener specific way.
   * Therefore, implementations SHOULD NOT throw exceptions (these are only logged).
   * @param helpDocument
   * @throws java.lang.Exception
   */
  public abstract void showDocument(URL helpDocument, String helpTopic) throws Exception;
}
