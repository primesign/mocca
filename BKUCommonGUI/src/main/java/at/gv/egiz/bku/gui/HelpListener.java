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

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HelpListener implements MouseListener, KeyListener, HelpURLProvider {

  public static final String MESSAGE_BUNDLE = "at/gv/egiz/bku/gui/Messages";

  private final Logger log = LoggerFactory.getLogger(HelpListener.class);

  private URL baseURL;
  
  protected String helpTopic;
  protected ResourceBundle messageBundle;

  public HelpListener(URL baseURL, Locale locale) {

    if (locale != null) {
      log.trace("Check for support of requested help locale {}.", locale);
      messageBundle = ResourceBundle.getBundle(MESSAGE_BUNDLE, locale);
    } else {
      messageBundle = ResourceBundle.getBundle(MESSAGE_BUNDLE);
    }

    String language = messageBundle.getLocale().getLanguage();
    log.trace("Using help language '{}'.", language);
    if (!language.isEmpty()) {
      try {
        baseURL = new URL(baseURL, language + "/");
      } catch (MalformedURLException e) {
        log.info("Failed to build baseURL using language {}. " +
        		"Using default language.", language, e);
      }
    } 
    log.debug("Setting help context to {}.", baseURL);
    this.baseURL = baseURL;
  }

  @Override
  public synchronized void setHelpTopic(String topic) {
    log.trace("Setting help topic: {}.", topic);
    helpTopic = topic;
  }

  @Override
  public synchronized String getHelpURL() {
    try {
      URL helpURL = new URL(baseURL, (helpTopic == null) ? "index.html" : helpTopic + ".html");
      log.debug("Return help url: {}.", helpURL);
      return helpURL.toString();
    } catch (MalformedURLException e) {
      log.info("Failed to build helpURL. Returning base URL: {}.", baseURL, e);
      return baseURL.toString();
    }
    
  }

  /**
   * By default, HelpListener cannot handle action events and acts as (deaf) help context only.
   * Subclasses may add listener functionality.
   *
   * Whether a listener is available so that GUI elements may be included to provide context help.
   * (whether a help icon shall be included)
   * @return true if this HelpListener implements the Mouse/KeyListeners 
   */
  public boolean implementsListener() {
    return false;
  }

  @Override
  public void mouseClicked(MouseEvent arg0) {
  }

  @Override
  public void keyPressed(KeyEvent arg0) {
  }

  @Override
  public void mousePressed(MouseEvent e) {
  }

  @Override
  public void mouseReleased(MouseEvent e) {
  }

  @Override
  public void mouseEntered(MouseEvent e) {
  }

  @Override
  public void mouseExited(MouseEvent e) {
  }

  @Override
  public void keyTyped(KeyEvent e) {
  }

  @Override
  public void keyReleased(KeyEvent e) {
  }
}
