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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author Clemens Orthacker <clemens.orthacker@iaik.tugraz.at>
 */
public abstract class AbstractHelpListener implements ActionListener {

  protected final static Log log = LogFactory.getLog(AbstractHelpListener.class);
  protected String helpURLBase;
  protected Locale locale;

  public AbstractHelpListener(URL baseURL, Locale locale) {
    if (baseURL == null || "".equals(baseURL)) {
      throw new RuntimeException("no help URL provided");
    }
    this.helpURLBase = baseURL.toString();
    this.locale = locale;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    log.debug("received help action: " + e.getActionCommand());
    URL helpURL;
    try {
      String urlString = helpURLBase;
      if (locale != null) {
        urlString = appendParameter(urlString, "locale", locale.toString());
      } 
      if (e.getActionCommand() != null && !"".equals(e.getActionCommand())) {
        urlString = appendParameter(urlString, "topic", e.getActionCommand());
      }
      helpURL = new URL(urlString);
    } catch (MalformedURLException ex) {
      try {
        log.error("failed to create help URL: " + ex.getMessage());
        helpURL = new URL(helpURLBase);
      } catch (MalformedURLException ex1) {
        log.error("failed to create default help URL, requested help will not be displayed");
        return;
      }
    }
    try {
      showDocument(helpURL);
    } catch (Exception ex) {
      log.error("could not display help document " + helpURL + ": " + ex.getMessage());
    }
  }
  
  private String appendParameter(String url, String paramName, String paramValue) {
    if (url.indexOf('?') < 0) {
      return url + "?" + paramName + "=" + paramValue;
    } else {
      return url + "&" + paramName + "=" + paramValue;
    }
  }
  
  public abstract void showDocument(URL helpDocument) throws Exception;
  
}
