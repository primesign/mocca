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
/**
 * 
 */
package at.gv.egiz.bku.smccstal.ext;

import at.gv.egiz.bku.gui.ActivationGUIFacade;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import at.gv.egiz.bku.smccstal.AbstractRequestHandler;
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
  private static Log log = LogFactory.getLog(CardMgmtRequestHandler.class);
  
  /**
   * The sequence counter.
   */
  private int sequenceNum = 0;

  /**
   * display script num
   */
  private int currentActivationScript = 0;

  @Override
  public STALResponse handleRequest(STALRequest request)
      throws InterruptedException {

    // APDU Script Request
    if (request instanceof APDUScriptRequest) {

      currentActivationScript++;
      log.debug("handling APDU script " + currentActivationScript);
      
      Card icc = card.getCard();

      if (icc == null) {
        log.error("SignatureCard instance '" + card.getClass().getName() + "' does not support card management requests.");
        return new ErrorResponse(1000);
      }

      List<RequestScriptElement> script = ((APDUScriptRequest) request).getScript();
      ArrayList<ResponseScriptElement> responses = new ArrayList<ResponseScriptElement>(script.size());

      ((ActivationGUIFacade) gui).showActivationProgressDialog(currentActivationScript, script.size(), this, "cancel");

      try {
        log.trace("begin exclusive");
        icc.beginExclusive();

        for (RequestScriptElement scriptElement : script) {
          ((ActivationGUIFacade) gui).incrementProgress();
          
          if (scriptElement instanceof Command) {
            log.trace("handling APDU script element COMMAND");
            Command command = (Command) scriptElement;
            CommandAPDU commandAPDU = new CommandAPDU(command.getCommandAPDU());

            log.trace("get basicchannel");
            CardChannel channel = icc.getBasicChannel();
            
            sequenceNum = command.getSequence();
            log.debug("Transmit APDU (sequence=" + sequenceNum + ")");
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

            log.trace("handling APDU script element RESET");
            sequenceNum = 0;
            card.reset();
            javax.smartcardio.ATR atr = icc.getATR();
            log.trace("got ATR: " + atr.toString());
            responses.add(new ATR(atr.getBytes()));

            log.trace("regain exclusive access to card");
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
        log.error(e);
        throw e;
      } finally {
        try {
          icc.endExclusive();
        } catch (CardException e) {
          log.info(e);
        }
      }

      log.trace("done handling APDU script " + currentActivationScript + ", return response containing " + responses.size() + " elements");
      ((ActivationGUIFacade) gui).showIdleDialog(this, "cancel");
      return new APDUScriptResponse(responses);
      
    } else {
      log.error("Got unexpected STAL request: " + request);
      return new ErrorResponse(1000);
    }
    
  }

  @Override
  public boolean requireCard() {
    return true;
  }

}
