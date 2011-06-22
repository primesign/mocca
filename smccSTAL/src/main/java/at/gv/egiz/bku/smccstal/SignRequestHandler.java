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


package at.gv.egiz.bku.smccstal;

import at.gv.egiz.bku.gui.BKUGUIFacade;
import at.gv.egiz.bku.pin.gui.SignPINGUI;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.gv.egiz.smcc.CancelledException;
import at.gv.egiz.smcc.LockedException;
import at.gv.egiz.smcc.NotActivatedException;
import at.gv.egiz.smcc.SignatureCard;
import at.gv.egiz.smcc.SignatureCardException;
import at.gv.egiz.smcc.SignatureCard.KeyboxName;
import at.gv.egiz.smcc.TimeoutException;
import at.gv.egiz.stal.ErrorResponse;
import at.gv.egiz.stal.STALRequest;
import at.gv.egiz.stal.STALResponse;
import at.gv.egiz.stal.SignRequest;
import at.gv.egiz.stal.SignResponse;
import at.gv.egiz.stal.signedinfo.ObjectFactory;
import at.gv.egiz.stal.signedinfo.SignedInfoType;

public class SignRequestHandler extends AbstractRequestHandler {

    private final Logger log = LoggerFactory.getLogger(SignRequestHandler.class);
    private static JAXBContext jaxbContext;

    static {
        try {
            jaxbContext = JAXBContext.newInstance(ObjectFactory.class.getPackage().getName());
        } catch (JAXBException e) {
          Logger log = LoggerFactory.getLogger(SignRequestHandler.class);
          log.error("Cannot init jaxbContext", e);
        }
    }

    protected SecureViewer secureViewer;
    
    public SignRequestHandler(SecureViewer secureViewer) {
      this.secureViewer = secureViewer;
    }

    @SuppressWarnings("unchecked")
    @Override
    public STALResponse handleRequest(STALRequest request) throws InterruptedException {
        if (request instanceof SignRequest) {
            SignRequest signReq = (SignRequest) request;
            try {
                Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                InputStream is = new ByteArrayInputStream(signReq.getSignedInfo());
                JAXBElement<SignedInfoType> si = (JAXBElement<SignedInfoType>) unmarshaller.unmarshal(is);
                String signatureMethod = si.getValue().getSignatureMethod().getAlgorithm();
                log.debug("Found signature method: {}.", signatureMethod);
                KeyboxName kb = SignatureCard.KeyboxName.getKeyboxName(signReq.getKeyIdentifier());

                byte[] resp = card.createSignature(new ByteArrayInputStream(signReq.getSignedInfo()), kb,
                        new SignPINGUI(gui, secureViewer, si.getValue()), signatureMethod);
                if (resp == null) {
                    return new ErrorResponse(6001);
                }
                SignResponse stalResp = new SignResponse();
                stalResp.setSignatureValue(resp);
                return stalResp;
            } catch (NotActivatedException e) {
              log.info("Citizen card not activated.", e);
              gui.showErrorDialog(BKUGUIFacade.ERR_CARD_NOTACTIVATED, null, this, null);
              waitForAction();
              gui.showMessageDialog(BKUGUIFacade.TITLE_WAIT,
                      BKUGUIFacade.MESSAGE_WAIT);
              return new ErrorResponse(6001);
            } catch (LockedException e) {
              log.info("Citizen card locked.", e);
              gui.showErrorDialog(BKUGUIFacade.ERR_CARD_LOCKED, null, this, null);
              waitForAction();
              gui.showMessageDialog(BKUGUIFacade.TITLE_WAIT,
                      BKUGUIFacade.MESSAGE_WAIT);
              return new ErrorResponse(6001);
            } catch (CancelledException cx) {
                log.debug("User cancelled request.");
                return new ErrorResponse(6001);
            } catch (TimeoutException ex) {
              log.error("Timeout during pin entry");
              gui.showMessageDialog(BKUGUIFacade.TITLE_ENTRY_TIMEOUT,
                      BKUGUIFacade.ERR_PIN_TIMEOUT, null,
                      BKUGUIFacade.BUTTON_CANCEL, this, null);
              waitForAction();
              gui.showMessageDialog(BKUGUIFacade.TITLE_WAIT,
                      BKUGUIFacade.MESSAGE_WAIT);
              return new ErrorResponse(6001);
            } catch (SignatureCardException e) {
                log.error("Error while creating signature: " + e);
                return new ErrorResponse(4000);
            } catch (JAXBException e) {
                log.error("Cannot unmarshall signed info.", e);
                return new ErrorResponse(1000);
            } catch (IOException e) {
              log.error("Error while creating signature: " + e);
              return new ErrorResponse(4000);
            } 
        } else {
            log.error("Got unexpected STAL request: {}.", request);
            return new ErrorResponse(1000);
        }
    }

    @Override
    public boolean requireCard() {
        return true;
    }

}
