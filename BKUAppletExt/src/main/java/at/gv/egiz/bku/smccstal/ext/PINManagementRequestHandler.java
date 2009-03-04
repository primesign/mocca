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
import at.gv.egiz.smcc.VerificationFailedException;
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

  protected static final Log log = LogFactory.getLog(PINManagementRequestHandler.class);

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
          PINSpec selectedPIN = gui.getSelectedPINSpec();

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
              card.activatePIN(selectedPIN.getKID(),
                      selectedPIN.getContextAID(),
                      String.valueOf(gui.getPin()));
              gui.showMessageDialog(PINManagementGUIFacade.TITLE_ACTIVATE_SUCCESS,
                      PINManagementGUIFacade.MESSAGE_ACTIVATE_SUCCESS,
                      new Object[] {selectedPIN.getLocalizedName()},
                      this, "ok");
              waitForAction();
              showPINManagementDialog(gui);
            } catch (SignatureCardException ex) {
              log.error("failed to activate " + selectedPIN.getLocalizedName() + ": " + ex.getMessage());
              gui.showErrorDialog(PINManagementGUIFacade.ERR_ACTIVATE, 
                      new Object[] {selectedPIN.getLocalizedName()},
                      this, "cancel");
            }
          } else if ("change".equals(actionCommand)) {
            try {
              card.changePIN(selectedPIN.getKID(),
                      selectedPIN.getContextAID(),
                      String.valueOf(gui.getOldPin()),
                      String.valueOf(gui.getPin()));
              gui.showMessageDialog(PINManagementGUIFacade.TITLE_CHANGE_SUCCESS,
                      PINManagementGUIFacade.MESSAGE_CHANGE_SUCCESS,
                      new Object[] {selectedPIN.getLocalizedName()},
                      this, "ok");
              waitForAction();
              showPINManagementDialog(gui);
            } catch (VerificationFailedException ex) {
              log.error("failed to change " + selectedPIN.getLocalizedName() + ": " + ex.getMessage());
              gui.showErrorDialog(PINManagementGUIFacade.ERR_RETRIES,
                      new Object[] {selectedPIN.getLocalizedName(), ex.getRetries()},
                      this, "back");
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

  public Map<PINSpec, STATUS> getPINStatuses() throws SignatureCardException {
    Card icc = card.getCard();
    try {
      icc.beginExclusive();
      CardChannel channel = icc.getBasicChannel();

      HashMap<PINSpec, STATUS> pinStatuses = new HashMap<PINSpec, STATUS>();
      List<PINSpec> pins = card.getPINSpecs();

      for (PINSpec pinSpec : pins) {
        byte kid = pinSpec.getKID();
        byte[] contextAID = pinSpec.getContextAID();

        if (contextAID != null) {
          CommandAPDU selectAPDU = new CommandAPDU(0x00, 0xa4, 0x04, 0x0c, contextAID);
          ResponseAPDU responseAPDU = channel.transmit(selectAPDU);
          if (responseAPDU.getSW() != 0x9000) {
            icc.endExclusive();
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
//      icc.endExclusive();

      return pinStatuses;

    } catch (CardException ex) {
      log.error("Failed to get PIN status: " + ex.getMessage());
      throw new SignatureCardException(ex.getMessage(), ex);
    } finally {
      try {
        icc.endExclusive();
      } catch (CardException ex) {
        log.trace("failed to end exclusive card access");
      }
    }
  }

  private void showPINManagementDialog(PINManagementGUIFacade gui) {
    try {
      Map<PINSpec, STATUS> pins = getPINStatuses();
      gui.showPINManagementDialog(pins, 
              this, "activate_enterpin", "change_enterpin", "unblock_enterpuk",
              this, "cancel");
    } catch (SignatureCardException ex) {
      gui.showErrorDialog(BKUGUIFacade.ERR_UNKNOWN_WITH_PARAM, 
              new Object[]{ex.getMessage()},
              this, "cancel");
    }
  }
}
