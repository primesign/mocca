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

import at.gv.egiz.bku.gui.BKUGUIFacade;
import at.gv.egiz.bku.gui.PINManagementGUIFacade;
import at.gv.egiz.bku.gui.PINManagementGUIFacade.STATUS;
import at.gv.egiz.bku.smccstal.AbstractRequestHandler;
import at.gv.egiz.smcc.PINSpec;
import at.gv.egiz.smcc.SignatureCardException;
import at.gv.egiz.smcc.util.SMCCHelper;
import at.gv.egiz.stal.ErrorResponse;
import at.gv.egiz.stal.STALRequest;
import at.gv.egiz.stal.STALResponse;
import at.gv.egiz.stal.ext.PINManagementRequest;
import at.gv.egiz.stal.ext.PINManagementResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
public class PINManagementRequestHandler extends AbstractRequestHandler {

  public static final String ERR_NOPIN_SELECTED = "err.no.pin.selected";
  protected static final Log log = LogFactory.getLog(PINManagementRequestHandler.class);

//  protected ResourceBundle messages;

//  public PINManagementRequestHandler(ResourceBundle messages) {
//    this.messages = messages;
//  }
  @Override
  public STALResponse handleRequest(STALRequest request) throws InterruptedException {
    if (request instanceof PINManagementRequest) {

      PINManagementGUIFacade gui = (PINManagementGUIFacade) this.gui;

      showPINManagementDialog(gui);

      while (true) {

        waitForAction();

        if ("cancel".equals(actionCommand)) {
          return new PINManagementResponse();
        } else if ("back".equals(actionCommand)) {
          showPINManagementDialog(gui);
        } else {
          PINSpec selectedPIN = gui.getSelectedPIN();

          if (selectedPIN == null) {
            throw new RuntimeException("no PIN selected for activation/change");
          }

          if ("activate_enterpin".equals(actionCommand)) {
            gui.showActivatePINDialog(selectedPIN, this, "activate", this, "back");
          } else if ("change_enterpin".equals(actionCommand)) {
            gui.showChangePINDialog(selectedPIN, this, "change", this, "back");
          } else if ("unblock_enterpuk".equals(actionCommand)) {
            gui.showUnblockPINDialog(selectedPIN, this, "unblock", this, "back");
          } else if ("activate".equals(actionCommand)) {
            try {
              byte[] pin = encodePIN(gui.getPin());
              activatePIN(selectedPIN.getKID(), selectedPIN.getContextAID(), pin);
              showPINManagementDialog(gui);
            } catch (SignatureCardException ex) {
              log.error("failed to activate " + selectedPIN.getLocalizedName() + ": " + ex.getMessage());
              gui.showErrorDialog(PINManagementGUIFacade.ERR_ACTIVATE, 
                      new Object[] {selectedPIN.getLocalizedName()},
                      this, "cancel");
            }
          } else if ("change".equals(actionCommand)) {
            try {
              byte[] oldPin = encodePIN(gui.getOldPin()); //new byte[]{(byte) 0x25, (byte) 0x40, (byte) 0x01};
              byte[] pin = encodePIN(gui.getPin()); //new byte[]{(byte) 0x25, (byte) 0x40};
              changePIN(selectedPIN.getKID(), selectedPIN.getContextAID(), oldPin, pin);
              showPINManagementDialog(gui);
            } catch (SignatureCardException ex) {
              log.error("failed to change " + selectedPIN.getLocalizedName() + ": " + ex.getMessage());
              gui.showErrorDialog(PINManagementGUIFacade.ERR_CHANGE, 
                      new Object[] {selectedPIN.getLocalizedName()},
                      this, "cancel");
            }
          } else if ("unblock".equals(actionCommand)) {
            log.error("unblock PIN not implemented");
            gui.showErrorDialog(PINManagementGUIFacade.ERR_UNBLOCK, null, this, "cancel");
          } else {
            throw new RuntimeException("unsupported action " + actionCommand);
          }
        }
      }
    } else {
      log.error("Got unexpected STAL request: " + request);
      return new ErrorResponse(1000);
    }
  }

  @Override
  public boolean requireCard() {
    return true;
  }

  /**
   * pin.length &lt; 4bit
   * @param kid
   * @param contextAID
   * @param pin
   * @throws at.gv.egiz.smcc.SignatureCardException
   */
  private void activatePIN(byte kid, byte[] contextAID, byte[] pin) throws SignatureCardException {
    try {
      Card icc = card.getCard();
      icc.beginExclusive();
      CardChannel channel = icc.getBasicChannel();

      if (contextAID != null) {
        CommandAPDU selectAPDU = new CommandAPDU(0x00, 0xa4, 0x04, 0x0c, contextAID);
        ResponseAPDU responseAPDU = channel.transmit(selectAPDU);
        if (responseAPDU.getSW() != 0x9000) {
          String msg = "Failed to activate PIN " + SMCCHelper.toString(new byte[]{kid}) +
                  ": Failed to select AID " + SMCCHelper.toString(contextAID) +
                  ": " + SMCCHelper.toString(responseAPDU.getBytes());
          log.error(msg);
          throw new SignatureCardException(msg);
        }
      }

      if (pin.length > 7) {
        log.error("Invalid PIN");
        throw new SignatureCardException("Invalid PIN");
      }
      byte length = (byte) (0x20 | pin.length * 2);

      byte[] apdu = new byte[]{
        (byte) 0x00, (byte) 0x24, (byte) 0x01, kid, (byte) 0x08,
        (byte) length, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
      for (int i = 0; i < pin.length; i++) {
        apdu[i + 6] = pin[i];
      }

      CommandAPDU verifyAPDU = new CommandAPDU(apdu);
      ResponseAPDU responseAPDU = channel.transmit(verifyAPDU);

      if (responseAPDU.getSW() != 0x9000) {
        String msg = "Failed to activate PIN " + SMCCHelper.toString(new byte[]{kid}) + ": " + SMCCHelper.toString(responseAPDU.getBytes());
        log.error(msg);
        throw new SignatureCardException(msg);
      }


      icc.endExclusive();


    } catch (CardException ex) {
      log.error("Failed to get PIN status: " + ex.getMessage());
      throw new SignatureCardException("Failed to get PIN status", ex);
    }
  }

  private void changePIN(byte kid, byte[] contextAID, byte[] oldPIN, byte[] newPIN) throws SignatureCardException {
    try {
      Card icc = card.getCard();
      icc.beginExclusive();
      CardChannel channel = icc.getBasicChannel();

      if (contextAID != null) {
        CommandAPDU selectAPDU = new CommandAPDU(0x00, 0xa4, 0x04, 0x0c, contextAID);
        ResponseAPDU responseAPDU = channel.transmit(selectAPDU);
        if (responseAPDU.getSW() != 0x9000) {
          String msg = "Failed to change PIN " + SMCCHelper.toString(new byte[]{kid}) +
                  ": Failed to select AID " + SMCCHelper.toString(contextAID) +
                  ": " + SMCCHelper.toString(responseAPDU.getBytes());
          log.error(msg);
          throw new SignatureCardException(msg);
        }
      }

      if (oldPIN.length > 7 || newPIN.length > 7) {
        log.error("Invalid PIN");
        throw new SignatureCardException("Invalid PIN");
      }
      byte oldLength = (byte) (0x20 | oldPIN.length * 2);
      byte newLength = (byte) (0x20 | newPIN.length * 2);

      byte[] apdu = new byte[]{
        (byte) 0x00, (byte) 0x24, (byte) 0x00, kid, (byte) 0x10,
        oldLength, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
        newLength, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
      for (int i = 0; i < oldPIN.length; i++) {
        apdu[i + 6] = oldPIN[i];
      }
      for (int i = 0; i < newPIN.length; i++) {
        apdu[i + 14] = newPIN[i];
      }

      CommandAPDU verifyAPDU = new CommandAPDU(apdu);
      ResponseAPDU responseAPDU = channel.transmit(verifyAPDU);

      if (responseAPDU.getSW() != 0x9000) {
        String msg = "Failed to change PIN " + SMCCHelper.toString(new byte[]{kid}) + ": " + SMCCHelper.toString(responseAPDU.getBytes());
        log.error(msg);
        throw new SignatureCardException(msg);
      }


      icc.endExclusive();


    } catch (CardException ex) {
      log.error("Failed to get PIN status: " + ex.getMessage());
      throw new SignatureCardException("Failed to get PIN status", ex);
    }
  }

  public Map<PINSpec, STATUS> getPINStatuses() throws SignatureCardException {
    try {
      Card icc = card.getCard();
      icc.beginExclusive();
      CardChannel channel = icc.getBasicChannel();

      HashMap<PINSpec, STATUS> pinStatuses = new HashMap<PINSpec, STATUS>();
      List<PINSpec> pins = card.getPINSpecs();

      //select DF_SichereSignatur 00 A4 04 0C 08 D0 40 00 00 17 00 12 01
//      CommandAPDU selectAPDU = new CommandAPDU(new byte[]{(byte) 0x00, (byte) 0xa4, (byte) 0x04, (byte) 0x0c, (byte) 0x08,
//                (byte) 0xd0, (byte) 0x40, (byte) 0x00, (byte) 0x00, (byte) 0x17, (byte) 0x00, (byte) 0x12, (byte) 0x01});
//      ResponseAPDU rAPDU = channel.transmit(selectAPDU);
//      log.debug("SELECT FILE DF_SichereSignatur: " + SMCCHelper.toString(rAPDU.getBytes()));

      //select DF_SIG DF 70
//      CommandAPDU selectAPDU = new CommandAPDU(new byte[]{(byte) 0x00, (byte) 0xa4, (byte) 0x00, (byte) 0x0c, (byte) 0x02,
//                (byte) 0xdf, (byte) 0x70 });
//      ResponseAPDU rAPDU = channel.transmit(selectAPDU);
//      log.debug("SELECT FILE DF_SIG: " + SMCCHelper.toString(rAPDU.getBytes()));

      //select DF_DEC DF 71
//      CommandAPDU selectAPDU = new CommandAPDU(new byte[]{(byte) 0x00, (byte) 0xa4, (byte) 0x04, (byte) 0x0c, (byte) 0x08,
//                (byte) 0xd0, (byte) 0x40, (byte) 0x00, (byte) 0x00, (byte) 0x17, (byte) 0x00, (byte) 0x12, (byte) 0x01});
//      ResponseAPDU rAPDU = channel.transmit(selectAPDU);
//      log.debug("SELECT FILE DF_SichereSignatur: " + SMCCHelper.toString(rAPDU.getBytes()));

      for (PINSpec pinSpec : pins) {
        byte kid = pinSpec.getKID();
        byte[] contextAID = pinSpec.getContextAID();

        if (contextAID != null) {
          CommandAPDU selectAPDU = new CommandAPDU(0x00, 0xa4, 0x04, 0x0c, contextAID);
          ResponseAPDU responseAPDU = channel.transmit(selectAPDU);
          if (responseAPDU.getSW() != 0x9000) {
            String msg = "Failed to activate PIN " + SMCCHelper.toString(new byte[]{kid}) +
                    ": Failed to select AID " + SMCCHelper.toString(contextAID) +
                    ": " + SMCCHelper.toString(responseAPDU.getBytes());
            log.error(msg);
            throw new SignatureCardException(msg);
          }
        }

        CommandAPDU verifyAPDU = new CommandAPDU(new byte[]{(byte) 0x00, (byte) 0x20, (byte) 00, kid});
        ResponseAPDU responseAPDU = channel.transmit(verifyAPDU);

        STATUS status = STATUS.UNKNOWN;
        if (responseAPDU.getSW() == 0x6984) {
          status = STATUS.NOT_ACTIV;
        } else if (responseAPDU.getSW() == 0x63c0) {
          status = STATUS.BLOCKED;
        } else if (responseAPDU.getSW1() == 0x63) {
          status = STATUS.ACTIV;
        }
        if (log.isDebugEnabled()) {
          log.debug("PIN " + pinSpec.getLocalizedName() + " status: " + SMCCHelper.toString(responseAPDU.getBytes()));
        }

        pinStatuses.put(pinSpec, status);
      }
      icc.endExclusive();

      return pinStatuses;

    } catch (CardException ex) {
      log.error("Failed to get PIN status: " + ex.getMessage());
      throw new SignatureCardException("Failed to get PIN status", ex);
    }
  }

  private byte[] encodePIN(char[] pinChars) {
    int length = (int) Math.ceil(pinChars.length/2);
    byte[] pin = new byte[length];
    for (int i = 0; i < length; i++) {
      pin[i] = (byte) (16*Character.digit(pinChars[i*2], 16) + Character.digit(pinChars[i*2+1], 16));
    }
    log.trace("***** "  + SMCCHelper.toString(pin) + " ******");
    return pin;
  }

  private void showPINManagementDialog(PINManagementGUIFacade gui) {
    try {
      Map<PINSpec, STATUS> pins = getPINStatuses();
      gui.showPINManagementDialog(pins, 
              this, "activate_enterpin", "change_enterpin", "unblock_enterpuk",
              this, "cancel");
    } catch (SignatureCardException ex) {
      gui.showErrorDialog(BKUGUIFacade.ERR_UNKNOWN_WITH_PARAM, 
              new Object[]{"FAILED TO GET PIN STATUSES: " + ex.getMessage()},
              this, "cancel");
    }
  }
}
