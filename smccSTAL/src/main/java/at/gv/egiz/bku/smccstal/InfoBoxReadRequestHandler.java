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
import at.gv.egiz.bku.pin.gui.VerifyPINGUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.gv.egiz.smcc.CancelledException;
import at.gv.egiz.smcc.LockedException;
import at.gv.egiz.smcc.NotActivatedException;
import at.gv.egiz.smcc.SignatureCard;
import at.gv.egiz.smcc.SignatureCardException;
import at.gv.egiz.stal.ErrorResponse;
import at.gv.egiz.stal.InfoboxReadRequest;
import at.gv.egiz.stal.InfoboxReadResponse;
import at.gv.egiz.stal.STALRequest;
import at.gv.egiz.stal.STALResponse;

public class InfoBoxReadRequestHandler extends AbstractRequestHandler {

  private final Logger log = LoggerFactory.getLogger(InfoBoxReadRequestHandler.class);

//  protected PINProviderFactory pinProviderFactory;

  @Override
  public STALResponse handleRequest(STALRequest request) throws InterruptedException {
    if (request instanceof InfoboxReadRequest) {
      InfoboxReadRequest infoBox = (InfoboxReadRequest) request;
      
      try {
        if (infoBox.getInfoboxIdentifier().equals("IdentityLink")) {
          newSTALMessage("Message.RequestCaption", "Message.IdentityLink");
          log.debug("Handling identitylink infobox.");
          byte[] resp = card.getInfobox(infoBox.getInfoboxIdentifier(),
                  new VerifyPINGUI(gui),
                  infoBox.getDomainIdentifier());
          if (resp == null) {
            log.info("Infobox doesn't contain any data. Assume card is not activated.");
            throw new NotActivatedException();
          } else {
            try {
              resp = DomainIdConverter.convertDomainId(resp, infoBox
                  .getDomainIdentifier());
            } catch (Exception e) {
              log.error("Cannot convert domain specific id.", e);
              return new ErrorResponse(1000);
            }
          }
          InfoboxReadResponse stalResp = new InfoboxReadResponse();
          stalResp.setInfoboxValue(resp);
          return stalResp;
        } else if (SignatureCard.KeyboxName.CERITIFIED_KEYPAIR.equals(infoBox
            .getInfoboxIdentifier())) {
          newSTALMessage("Message.RequestCaption", "Message.CertifiedKeypair");
          log.debug("Handling certified keypair infobox.");
          byte[] resp = card
              .getCertificate(SignatureCard.KeyboxName.CERITIFIED_KEYPAIR);
          if (resp == null) {
            return new ErrorResponse(6001);
          }
          InfoboxReadResponse stalResp = new InfoboxReadResponse();
          stalResp.setInfoboxValue(resp);
          return stalResp;
        } else if (SignatureCard.KeyboxName.SECURE_SIGNATURE_KEYPAIR
            .equals(infoBox.getInfoboxIdentifier())) {
          newSTALMessage("Message.RequestCaption",
              "Message.SecureSignatureKeypair");
          log.debug("Handling secure signature keypair infobox.");
          byte[] resp = card
              .getCertificate(SignatureCard.KeyboxName.SECURE_SIGNATURE_KEYPAIR);
          if (resp == null) {
            return new ErrorResponse(6001);
          }
          InfoboxReadResponse stalResp = new InfoboxReadResponse();
          stalResp.setInfoboxValue(resp);
          return stalResp;
        } else {
          newSTALMessage("Message.RequestCaption", "Message.InfoboxReadRequest");
          log.warn("Unknown infobox identifier: {} trying generic request.",
              infoBox.getInfoboxIdentifier());
          byte[] resp = card.getInfobox(infoBox.getInfoboxIdentifier(),
                  new VerifyPINGUI(gui),
                  infoBox.getDomainIdentifier());
          if (resp == null) {
            return new ErrorResponse(6001);
          }
          InfoboxReadResponse stalResp = new InfoboxReadResponse();
          stalResp.setInfoboxValue(resp);
          return stalResp;
        }
      } catch (IllegalArgumentException e) {
        log.info("Infobox {} not supported.", infoBox.getInfoboxIdentifier());
        return new ErrorResponse(4002);
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
        log.debug("User cancelled request.", cx);
        return new ErrorResponse(6001);
      } catch (SignatureCardException e) {
        log.info("Error while reading infobox. " + e);
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
