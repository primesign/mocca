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
package at.gv.egiz.bku.smccstal;

import at.gv.egiz.bku.gui.BKUGUIFacade;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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

    private static Log log = LogFactory.getLog(SignRequestHandler.class);
    private static JAXBContext jaxbContext;

    static {
        try {
            jaxbContext = JAXBContext.newInstance(ObjectFactory.class.getPackage().getName());
        } catch (JAXBException e) {
            log.fatal("Cannot init jaxbContext", e);
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
            newSTALMessage("Message.RequestCaption", "Message.SignRequest");
            try {
                Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                InputStream is = new ByteArrayInputStream(signReq.getSignedInfo());
                JAXBElement<SignedInfoType> si = (JAXBElement<SignedInfoType>) unmarshaller.unmarshal(is);
                String signatureMethod = si.getValue().getSignatureMethod().getAlgorithm();
                log.debug("Found signature method: " + signatureMethod);
                KeyboxName kb = SignatureCard.KeyboxName.getKeyboxName(signReq.getKeyIdentifier());

                byte[] resp = card.createSignature(new ByteArrayInputStream(signReq.getSignedInfo()), kb,
                        new PINProviderFactory(card.getReader(), gui)
                        .getSignaturePINProvider(secureViewer, si.getValue()), signatureMethod);
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
                log.debug("User cancelled request");
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
                log.error("Cannot unmarshall signed info", e);
                return new ErrorResponse(1000);
            } catch (IOException e) {
              log.error("Error while creating signature: " + e);
              return new ErrorResponse(4000);
            } 
        } else {
            log.fatal("Got unexpected STAL request: " + request);
            return new ErrorResponse(1000);
        }
    }

    @Override
    public boolean requireCard() {
        return true;
    }

}
