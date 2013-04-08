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

import at.gv.egiz.bku.gui.viewer.FontProvider;
import java.awt.Container;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common superclass for Activation and PinManagement GUIs
 * 
 * @author Clemens Orthacker <clemens.orthacker@iaik.tugraz.at>
 */
public class CardMgmtGUI extends BKUGUIImpl {

  public static final String CARDMGMT_MESSAGES_BUNDLE = "at/gv/egiz/bku/gui/ActivationMessages";

  protected ResourceBundle cardmgmtMessages;

  public CardMgmtGUI(Container contentPane,
          Locale locale,
          Style guiStyle,
          URL backgroundImgURL,
          FontProvider fontProvider,
          HelpListener helpListener) {
	  super(contentPane, locale, guiStyle, backgroundImgURL, fontProvider, helpListener);  
  }

  @Override
  protected void loadMessageBundle(Locale locale) {
    super.loadMessageBundle(locale);

    if (locale != null) {
        Locale lang = new Locale(locale.getLanguage().substring(0,2));
        Logger log = LoggerFactory.getLogger(CardMgmtGUI.class);
        log.debug("Loading applet resources for language: {}.", lang);
        cardmgmtMessages = ResourceBundle.getBundle(CARDMGMT_MESSAGES_BUNDLE, lang);
    } else {
        cardmgmtMessages = ResourceBundle.getBundle(CARDMGMT_MESSAGES_BUNDLE);
    }
  }

  @Override
  protected String getMessage(String key) {
    if (super.hasMessage(key)) {
      return super.getMessage(key);
    }
    return cardmgmtMessages.getString(key);
  }

  @Override
  protected boolean hasMessage(String key) {
    return (cardmgmtMessages.containsKey(key) || super.hasMessage(key));
  }
}
