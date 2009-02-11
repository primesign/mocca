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

package at.gv.egiz.bku.smccstal.ext;

import at.gv.egiz.bku.gui.PINStatusProvider;
import at.gv.egiz.bku.smccstal.AbstractRequestHandler;
import at.gv.egiz.smcc.SignatureCardException;
import at.gv.egiz.stal.ErrorResponse;
import at.gv.egiz.stal.STALRequest;
import at.gv.egiz.stal.STALResponse;
import at.gv.egiz.stal.ext.ActivatePINRequest;
import at.gv.egiz.stal.ext.ChangePINRequest;
import at.gv.egiz.stal.ext.UnblockPINRequest;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author Clemens Orthacker <clemens.orthacker@iaik.tugraz.at>
 */
public class PINMgmtRequestHandler extends AbstractRequestHandler implements PINStatusProvider {

  protected static final Log log = LogFactory.getLog(PINMgmtRequestHandler.class);

  @Override
  public STALResponse handleRequest(STALRequest request) throws InterruptedException {
    if (request instanceof ActivatePINRequest) {
      log.error("not implemented yet");
      return new ErrorResponse(1000);

    } else if (request instanceof ChangePINRequest) {
      log.error("not implemented yet");
      return new ErrorResponse(1000);

    } else if (request instanceof UnblockPINRequest) {
      log.error("not implemented yet");
      return new ErrorResponse(1000);

    } else {
      log.error("Got unexpected STAL request: " + request);
      return new ErrorResponse(1000);
    }
  }

  @Override
  public boolean requireCard() {
    return true;
  }

  @Override
  public STATUS getPINStatus(int pin) throws SignatureCardException {
    try {
      Card icc = card.getCard();
      icc.beginExclusive();
      CardChannel channel = icc.getBasicChannel();
      CommandAPDU verifyAPDU = new CommandAPDU(new byte[] {(byte) 0x00} );
      ResponseAPDU responseAPDU = channel.transmit(verifyAPDU);
      byte sw1 = (byte) responseAPDU.getSW1();
      byte[] sw = new byte[] {
                (byte) (0xFF & responseAPDU.getSW1()),
                (byte) (0xFF & responseAPDU.getSW2()) };

      icc.endExclusive();
      return STATUS.ACTIV;
    } catch (CardException ex) {
      log.error("Failed to get PIN status: " + ex.getMessage());
      throw new SignatureCardException("Failed to get PIN status", ex);
    }
  }

}
