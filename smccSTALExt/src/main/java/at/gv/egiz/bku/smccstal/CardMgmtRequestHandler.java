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


/**
 * 
 */
package at.gv.egiz.bku.smccstal;

import at.gv.egiz.bku.gui.ActivationGUIFacade;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.gv.egiz.smcc.SignatureCardException;
import at.gv.egiz.stal.ErrorResponse;
import at.gv.egiz.stal.STALRequest;
import at.gv.egiz.stal.STALResponse;
import at.gv.egiz.stal.ext.APDUScriptRequest;
import at.gv.egiz.stal.ext.APDUScriptResponse;
import at.gv.egiz.stal.ext.APDUScriptRequest.Command;
import at.gv.egiz.stal.ext.APDUScriptRequest.RequestScriptElement;
import at.gv.egiz.stal.ext.APDUScriptRequest.Reset;
import at.gv.egiz.stal.ext.APDUScriptResponse.Response;
import at.gv.egiz.stal.ext.APDUScriptResponse.ATR;
import at.gv.egiz.stal.ext.APDUScriptResponse.ResponseScriptElement;
import java.awt.event.ActionListener;

/**
 * @author mcentner
 *
 */
public class CardMgmtRequestHandler extends AbstractRequestHandler implements ActionListener {

  /**
   * Logging facility.
   */
  private final Logger log = LoggerFactory.getLogger(CardMgmtRequestHandler.class);
  
  /**
   * The sequence counter.
   */
  private int sequenceNum = 0;

  /**
   * display script num
   */
  private int currentActivationScript = 0;

  private ErrorResponse errorResponse(int errorCode, String errorMessage)
  {
    log.error(errorMessage);
    ErrorResponse err = new ErrorResponse(errorCode);
    err.setErrorMessage(errorMessage);
    return err;
  }

  @Override
  public STALResponse handleRequest(STALRequest request)
      throws InterruptedException {

    // APDU Script Request
    if (request instanceof APDUScriptRequest) {

      currentActivationScript++;
      log.debug("Handling APDU script {}.", currentActivationScript);
      
      Card icc = card.getCard();

      if (icc == null) {
        return errorResponse(1000, "SignatureCard instance '" +
            card.getClass().getName() + "' does not support card management requests.");
      }

      List<RequestScriptElement> script = ((APDUScriptRequest) request).getScript();
      ArrayList<ResponseScriptElement> responses = new ArrayList<ResponseScriptElement>(script.size());

      ((ActivationGUIFacade) gui).showActivationProgressDialog(currentActivationScript, script.size(), this, "cancel");

      try {
        log.trace("Begin exclusive.");
        icc.beginExclusive();

        for (RequestScriptElement scriptElement : script) {
          ((ActivationGUIFacade) gui).incrementProgress();
          
          if (scriptElement instanceof Command) {
            log.trace("Handling APDU script element COMMAND.");
            Command command = (Command) scriptElement;
            CommandAPDU commandAPDU = new CommandAPDU(command.getCommandAPDU());

            log.trace("Get basicchannel.");
            CardChannel channel = icc.getBasicChannel();
            
            sequenceNum = command.getSequence();
            log.debug("Transmit APDU (sequence={}).", sequenceNum);
            log.trace(commandAPDU.toString());
            ResponseAPDU responseAPDU = channel.transmit(commandAPDU);
            log.trace(responseAPDU.toString());
            
            byte[] sw = new byte[] { 
                (byte) (0xFF & responseAPDU.getSW1()),
                (byte) (0xFF & responseAPDU.getSW2()) }; 
            
            responses.add(new Response(sequenceNum, responseAPDU.getData(), sw, 0));
            
            if (command.getExpectedSW() != null && 
              !Arrays.equals(sw, command.getExpectedSW())) {
              // unexpected SW
              log.warn("Got unexpected SW. APDU-script execution stopped.");
              break;
            }
            
          } else if (scriptElement instanceof Reset) {

            log.trace("Handling APDU script element RESET.");
            sequenceNum = 0;
            card.reset();
            javax.smartcardio.ATR atr = icc.getATR();
            log.trace("Got ATR: {}.", atr.toString());
            responses.add(new ATR(atr.getBytes()));

            log.trace("Regain exclusive access to card.");
            icc = card.getCard();
            icc.beginExclusive();
          }
          
        }

      } catch (CardException e) {
        log.info("Failed to execute APDU script.", e);
        responses.add(new Response(sequenceNum, null, null, Response.RC_UNSPECIFIED));
      } catch (SignatureCardException e) {
        log.info("Failed to reset smart card.", e);
        responses.add(new Response(sequenceNum, null, null, Response.RC_UNSPECIFIED));
      } catch (RuntimeException e) {
        log.error(e.getMessage(), e);
        throw e;
      } finally {
        try {
          icc.endExclusive();
        } catch (CardException e) {
          log.info(e.getMessage(), e);
        }
      }

      log.trace("Done handling APDU script {}, return response containing {} elements.",
          currentActivationScript, responses.size());
      ((ActivationGUIFacade) gui).showIdleDialog(this, "cancel");
      return new APDUScriptResponse(responses);
      
    } else {
      return errorResponse(1000, "Got unexpected STAL request: " + request);
    }
    
  }

  @Override
  public boolean requireCard() {
    return true;
  }

}
