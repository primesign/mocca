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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import at.gv.egiz.bku.gui.BKUGUIFacade;
import at.gv.egiz.bku.gui.PINManagementGUIFacade;
import at.gv.egiz.bku.gui.PINManagementGUIFacade.STATUS;
import at.gv.egiz.bku.smccstal.AbstractRequestHandler;
import at.gv.egiz.smcc.CancelledException;
import at.gv.egiz.smcc.LockedException;
import at.gv.egiz.smcc.NotActivatedException;
import at.gv.egiz.smcc.PINConfirmationException;
import at.gv.egiz.smcc.PINFormatException;
import at.gv.egiz.smcc.PINMgmtSignatureCard;
import at.gv.egiz.smcc.PINOperationAbortedException;
import at.gv.egiz.smcc.PINSpec;
import at.gv.egiz.smcc.SignatureCardException;
import at.gv.egiz.smcc.TimeoutException;
import at.gv.egiz.smcc.PINMgmtSignatureCard.PIN_STATE;
import at.gv.egiz.stal.ErrorResponse;
import at.gv.egiz.stal.STALRequest;
import at.gv.egiz.stal.STALResponse;
import at.gv.egiz.stal.ext.PINManagementRequest;
import at.gv.egiz.stal.ext.PINManagementResponse;

/**
 *
 * @author Clemens Orthacker <clemens.orthacker@iaik.tugraz.at>
 */
public class PINManagementRequestHandler extends AbstractRequestHandler {

  protected static final Log log = LogFactory.getLog(PINManagementRequestHandler.class);

  protected Map<PINSpec, STATUS> pinStates = new HashMap<PINSpec, STATUS>();

  @Override
  public STALResponse handleRequest(STALRequest request) throws InterruptedException {
    if (request instanceof PINManagementRequest) {

    PINManagementGUIFacade gui = (PINManagementGUIFacade) this.gui;
      
    PINSpec selectedPIN = null;

    try {

      if (card instanceof PINMgmtSignatureCard) {

        // update all PIN states
        for (PINSpec pinSpec : ((PINMgmtSignatureCard) card).getPINSpecs()) {
          updatePINState(pinSpec, STATUS.UNKNOWN);
        }
        
        gui.showPINManagementDialog(pinStates, this, "activate_enterpin",
              "change_enterpin", "unblock_enterpuk", "verify_enterpin", this,
              "cancel");
        
      } else {
        
        // card does not support PIN management
        gui.showErrorDialog(PINManagementGUIFacade.ERR_UNSUPPORTED_CARD,
              null, this, "cancel");
        
      }

      while (true) {

        waitForAction();

        if ("cancel".equals(actionCommand)) {
          log.debug("pin management cancel");
          return new PINManagementResponse();
        } else {
          selectedPIN = gui.getSelectedPINSpec();

          if (selectedPIN == null) {
            throw new NullPointerException("no PIN selected for activation/change");
          }

          ManagementPINProviderFactory ppfac =
                  new ManagementPINProviderFactory(card.getReader(), gui);

          try {
            if ("activate_enterpin".equals(actionCommand)) {
              log.info("activate " + selectedPIN.getLocalizedName());
              ((PINMgmtSignatureCard) card).activatePIN(selectedPIN,
                      ppfac.getActivatePINProvider());
              updatePINState(selectedPIN, STATUS.ACTIV);
              gui.showMessageDialog(PINManagementGUIFacade.TITLE_ACTIVATE_SUCCESS,
                      PINManagementGUIFacade.MESSAGE_ACTIVATE_SUCCESS,
                      new Object[] {selectedPIN.getLocalizedName()},
                      BKUGUIFacade.BUTTON_OK, this, "ok");
              waitForAction();
            } else if ("change_enterpin".equals(actionCommand)) {
              log.info("change " + selectedPIN.getLocalizedName());
              ((PINMgmtSignatureCard) card).changePIN(selectedPIN, 
                      ppfac.getChangePINProvider());
              updatePINState(selectedPIN, STATUS.ACTIV);
              gui.showMessageDialog(PINManagementGUIFacade.TITLE_CHANGE_SUCCESS,
                      PINManagementGUIFacade.MESSAGE_CHANGE_SUCCESS,
                      new Object[] {selectedPIN.getLocalizedName()},
                      BKUGUIFacade.BUTTON_OK, this, "ok");
              waitForAction();

            } else if ("unblock_enterpuk".equals(actionCommand)) {
              log.info("unblock " + selectedPIN.getLocalizedName());
              ((PINMgmtSignatureCard) card).unblockPIN(selectedPIN,
                      ppfac.getUnblockPINProvider());
            } else if ("verify_enterpin".equals(actionCommand)) {
              log.info("verify " + selectedPIN.getLocalizedName());
              ((PINMgmtSignatureCard) card).verifyPIN(selectedPIN,
                      ppfac.getVerifyPINProvider());
              updatePINState(selectedPIN, STATUS.ACTIV);
            }
          } catch (CancelledException ex) {
            log.trace("cancelled");
          } catch (TimeoutException ex) {
            log.error("Timeout during pin entry");
            gui.showMessageDialog(BKUGUIFacade.TITLE_ENTRY_TIMEOUT,
                    BKUGUIFacade.ERR_PIN_TIMEOUT, 
                    new Object[] {selectedPIN.getLocalizedName()},
                    BKUGUIFacade.BUTTON_OK, this, null);
            waitForAction();
          } catch (LockedException ex) {
            log.error(selectedPIN.getLocalizedName() + " locked");
            updatePINState(selectedPIN, STATUS.BLOCKED);
            gui.showErrorDialog(PINManagementGUIFacade.ERR_LOCKED,
                    new Object[] {selectedPIN.getLocalizedName()},
                    this, null);
            waitForAction();
          } catch (NotActivatedException ex) {
            log.error(selectedPIN.getLocalizedName() + " not active");
            updatePINState(selectedPIN, STATUS.NOT_ACTIV);
            gui.showErrorDialog(PINManagementGUIFacade.ERR_NOT_ACTIVE,
                    new Object[] {selectedPIN.getLocalizedName()},
                    this, null);
            waitForAction();
          } catch (PINConfirmationException ex) {
            log.error("confirmation pin does not match new " + selectedPIN.getLocalizedName());
            gui.showErrorDialog(PINManagementGUIFacade.ERR_PIN_CONFIRMATION,
                    new Object[] {selectedPIN.getLocalizedName()},
                    this, null);
            waitForAction();
          } catch (PINOperationAbortedException ex) {
            log.error("pin operation aborted without further details");
            gui.showErrorDialog(PINManagementGUIFacade.ERR_PIN_OPERATION_ABORTED,
                    new Object[] {selectedPIN.getLocalizedName()},
                    this, null);
            waitForAction();
          } catch (PINFormatException ex) {
            log.error("wrong format of new " + selectedPIN.getLocalizedName());
//            updatePINStatus(selectedPIN, STATUS.NOT_ACTIV);
            String pinSize = String.valueOf(selectedPIN.getMinLength());
            if (selectedPIN.getMinLength() != selectedPIN.getMaxLength()) {
                pinSize += "-" + selectedPIN.getMaxLength();
            }
            gui.showErrorDialog(PINManagementGUIFacade.ERR_PIN_FORMAT,
                    new Object[] {selectedPIN.getLocalizedName(), pinSize},
                    this, null);
            waitForAction();
          }
        } // end if

        selectedPIN = null;
        gui.showPINManagementDialog(pinStates,
                this, "activate_enterpin", "change_enterpin", "unblock_enterpuk", "verify_enterpin",
                this, "cancel");
      } // end while

      } catch (GetPINStatusException ex) {
        String pin = (selectedPIN != null) ? selectedPIN.getLocalizedName() : "pin";
        log.error("failed to get " +  pin + " status: " + ex.getMessage());
        gui.showErrorDialog(PINManagementGUIFacade.ERR_STATUS, null,
                this, "ok");
        waitForAction();
        return new ErrorResponse(1000);
      } catch (SignatureCardException ex) {
        log.error(ex.getMessage(), ex);
        gui.showErrorDialog(PINManagementGUIFacade.ERR_UNKNOWN, null,
                this, "ok");
        waitForAction();
        return new ErrorResponse(1000);
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
   * query status for STARCOS card,
   * assume provided status for ACOS card
   * @param pinSpec
   * @param status
   * @throws at.gv.egiz.smcc.SignatureCardException if query status fails
   */
  private void updatePINState(PINSpec pinSpec, STATUS status)
      throws GetPINStatusException {

    PINMgmtSignatureCard pmCard = ((PINMgmtSignatureCard) card);
    PIN_STATE pinState;
    try {
      pinState = pmCard.getPINState(pinSpec);
    } catch (SignatureCardException e) {
      String msg = "Failed to get PIN status for pin '"
          + pinSpec.getLocalizedName() + "'.";
      log.info(msg, e);
      throw new GetPINStatusException(msg);
    }
    if (pinState == PIN_STATE.ACTIV) {
      pinStates.put(pinSpec, STATUS.ACTIV);
    } else if (pinState == PIN_STATE.NOT_ACTIV) {
      pinStates.put(pinSpec, STATUS.NOT_ACTIV);
    } else if (pinState == PIN_STATE.BLOCKED) {
      pinStates.put(pinSpec, STATUS.BLOCKED);
    } else {
      pinStates.put(pinSpec, status);
    }
  }

}
