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

import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.util.Locale;
import java.util.ResourceBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class HelpListener implements MouseListener, KeyListener, HelpURLProvider {

  public static final String MESSAGE_BUNDLE = "at/gv/egiz/bku/gui/Messages";

  private final Logger log = LoggerFactory.getLogger(HelpListener.class);
  private String helpURL;
  protected String helpTopic;
  protected ResourceBundle messageBundle;

  // localization in helpListener (pass message bundle, getLocale, add language to helpContext) or outside?
  public HelpListener(String helpURL, Locale locale) {
//    this.codebase = codebase;
    this.helpURL = helpURL;

    if (locale != null) {
      log.trace("Check for support of requested help locale {}.", locale.getLanguage().substring(0,2));
      messageBundle = ResourceBundle.getBundle(MESSAGE_BUNDLE,
              new Locale(locale.getLanguage().substring(0, 2)));
    } else {
      messageBundle = ResourceBundle.getBundle(MESSAGE_BUNDLE);
    }
    if (!"".equals(messageBundle.getLocale().getLanguage())) {
      log.trace("Using help locale '{}'.", messageBundle.getLocale().getLanguage().substring(0,2));
      helpURL += messageBundle.getLocale().getLanguage().substring(0,2) + '/';
    } else {
      log.trace("Using help locale 'default'.");
    }
    
    log.debug("Setting help context to {}.", helpURL);
  }

  @Override
  public synchronized void setHelpTopic(String topic) {
    log.trace("Setting help topic: {}.", topic);
    helpTopic = topic;
  }

  @Override
  public synchronized String getHelpURL() {
    if (helpTopic == null) {
      log.debug("No help topic set, return index.");
      return helpURL + "index.html";
    }
    String url = helpURL + helpTopic + ".html";
    log.debug("Return help topic: {}.", url);
    return url;
  }

  /**
   * By default, HelpListener cannot handle action events and acts as (deaf) help context only.
   * Subclasses may add listener functionality.
   *
   * Whether a listener is available so that GUI elements may be included to provide context help.
   * (whether a help icon shall be included)
   * @return true if this HelpListener implements the Mouse/KeyListeners 
   */
  public abstract boolean implementsListener();
  
}
