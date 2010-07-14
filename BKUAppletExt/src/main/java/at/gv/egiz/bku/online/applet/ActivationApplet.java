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

import at.gv.egiz.bku.gui.ActivationGUI;
import at.gv.egiz.bku.gui.BKUGUIFacade;
import at.gv.egiz.bku.gui.SwitchFocusListener;
import at.gv.egiz.bku.gui.BKUGUIFacade.Style;
import at.gv.egiz.bku.gui.HelpListener;
import at.gv.egiz.bku.gui.viewer.FontProvider;
import at.gv.egiz.bku.smccstal.AbstractSMCCSTAL;
import at.gv.egiz.bku.smccstal.CardMgmtRequestHandler;
import at.gv.egiz.stal.ext.APDUScriptRequest;
import at.gv.egiz.stal.service.STALPortType;
import at.gv.egiz.stal.service.translator.STALTranslator;
import at.gv.egiz.stalx.service.STALService;
import at.gv.egiz.stalx.service.translator.STALXTranslationHandler;
import java.awt.Container;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import javax.xml.namespace.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Clemens Orthacker <clemens.orthacker@iaik.tugraz.at>
 */
public class ActivationApplet extends BKUApplet {

  private static final long serialVersionUID = 1L;
  private final Logger log = LoggerFactory.getLogger(ActivationApplet.class);

  @Override
  public void init() {
    super.init();
    if (worker instanceof AbstractSMCCSTAL) {
      CardMgmtRequestHandler handler = new CardMgmtRequestHandler();
      ((AbstractSMCCSTAL) worker).addRequestHandler(APDUScriptRequest.class, handler);
      log.debug("Registered CardMgmtRequestHandler.");
    } else {
      log.warn("Cannot register CardMgmtRequestHandler.");
    }
  }

  /**
   * creates a STAL-X enabled webservice port
   * @return
   * @throws java.net.MalformedURLException
   */
  @Override
  public STALPortType getSTALPort() throws MalformedURLException {
    URL wsdlURL = getURLParameter(WSDL_URL);
    log.debug("Setting STAL WSDL: {}.", wsdlURL);
    QName endpointName = new QName(STAL_WSDL_NS, STAL_SERVICE);
    log.info("Creating STAL-X enabled webservice port.");
    STALService stal = new STALService(wsdlURL, endpointName);
    return stal.getSTALPort();
  }

  @Override
  public STALTranslator getSTALTranslator() {
    STALTranslator translator = super.getSTALTranslator();
    translator.registerTranslationHandler(new STALXTranslationHandler());
    return translator;
  }

  @Override
  protected BKUGUIFacade createGUI(Container contentPane,
          Locale locale,
          Style guiStyle,
          URL backgroundImgURL,
          FontProvider fontProvider,
          HelpListener helpListener,
          SwitchFocusListener switchFocusListener) {
    return new ActivationGUI(contentPane, locale, guiStyle, backgroundImgURL, fontProvider, helpListener, switchFocusListener);
  }
}
