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


package at.gv.egiz.bku.online.applet;

import at.gv.egiz.bku.gui.ActivationGUI;
import at.gv.egiz.bku.gui.BKUGUIFacade;
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
    if (wsdlURL.toString().contains(";jsessionid="))
      wsdlURL = new URL(wsdlURL.toString().replaceAll("\\;jsessionid=[^\\?#]*", ""));
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
          HelpListener helpListener) {
    return new ActivationGUI(contentPane, locale, guiStyle, backgroundImgURL, fontProvider, helpListener);
  }
}
