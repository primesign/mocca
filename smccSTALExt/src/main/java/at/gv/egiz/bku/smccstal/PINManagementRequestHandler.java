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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.gv.egiz.bku.gui.BKUGUIFacade;
import at.gv.egiz.bku.gui.PINManagementGUIFacade;
import at.gv.egiz.bku.pin.gui.ManagementPINGUI;
import at.gv.egiz.bku.pin.gui.VerifyPINGUI;
import at.gv.egiz.smcc.CancelledException;
import at.gv.egiz.smcc.LockedException;
import at.gv.egiz.smcc.NotActivatedException;
import at.gv.egiz.smcc.PINConfirmationException;
import at.gv.egiz.smcc.PINFormatException;
import at.gv.egiz.smcc.PINMgmtSignatureCard;
import at.gv.egiz.smcc.PINOperationAbortedException;
import at.gv.egiz.smcc.PinInfo;
import at.gv.egiz.smcc.SignatureCardException;
import at.gv.egiz.smcc.TimeoutException;
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

  private final Logger log = LoggerFactory.getLogger(PINManagementRequestHandler.class);

  @Override
  public STALResponse handleRequest(STALRequest request) throws InterruptedException {
    if (request instanceof PINManagementRequest) {

    PINManagementGUIFacade gui = (PINManagementGUIFacade) this.gui;
      
    PinInfo selectedPIN = null;

    try {

      PinInfo[] pinInfos = null;
      
      if (card instanceof PINMgmtSignatureCard) {

        try {
          pinInfos = ((PINMgmtSignatureCard) card).getPinInfos();
          gui.showPINManagementDialog(pinInfos,  this, "activate_enterpin",
                  "change_enterpin", "unblock_enterpuk", "verify_enterpin",
                  this, "cancel");
        } catch (SignatureCardException ex) {
          log.error("Card not activated, pin management not available (STARCOS G3).");
          gui.showErrorDialog(PINManagementGUIFacade.ERR_CARD_NOTACTIVATED,
              null, this, "cancel");
        }
      } else {
        
        // card does not support PIN management
        gui.showErrorDialog(PINManagementGUIFacade.ERR_UNSUPPORTED_CARD,
              null, this, "cancel");
        
      }

      while (true) {

        waitForAction();

        if ("cancel".equals(actionCommand)) {
          log.debug("Pin management cancel.");
          return new PINManagementResponse();
        } else {
          selectedPIN = gui.getSelectedPinInfo();

          if (selectedPIN == null) {
            log.error("No PIN selected for activation/change.");
            gui.showErrorDialog(PINManagementGUIFacade.ERR_UNKNOWN_WITH_PARAM,
                    new Object[] {"no pin selected"}, this, "cancel");
            continue; 
          }

          try {
            if ("activate_enterpin".equals(actionCommand)) {
              activatePIN(selectedPIN);
            } else if ("change_enterpin".equals(actionCommand)) {
              changePIN(selectedPIN);
            } else if ("unblock_enterpuk".equals(actionCommand)) {
              unblockPIN(selectedPIN);
            } else if ("verify_enterpin".equals(actionCommand)) {
              verifyPIN(selectedPIN);
            }
          } catch (CancelledException ex) {
            log.trace("Cancelled.");
          } catch (TimeoutException ex) {
            log.error("Timeout during pin entry.");
            gui.showMessageDialog(BKUGUIFacade.TITLE_ENTRY_TIMEOUT,
                    BKUGUIFacade.ERR_PIN_TIMEOUT, 
                    new Object[] {selectedPIN.getLocalizedName()},
                    BKUGUIFacade.BUTTON_OK, this, null);
            waitForAction();
          } catch (LockedException ex) {
            log.error("{} locked.", selectedPIN.getLocalizedName());
//            updatePINState(selectedPIN, STATUS.BLOCKED);
            gui.showErrorDialog(PINManagementGUIFacade.ERR_LOCKED,
                    new Object[] {selectedPIN.getLocalizedName()},
                    this, null);
            waitForAction();
          } catch (NotActivatedException ex) {
            log.error("{} not active.", selectedPIN.getLocalizedName());
//            updatePINState(selectedPIN, STATUS.NOT_ACTIV);
            gui.showErrorDialog(PINManagementGUIFacade.ERR_NOT_ACTIVE,
                    new Object[] {selectedPIN.getLocalizedName()},
                    this, null);
            waitForAction();

            // inner loop for pinConfirmation and pinFormat ex
//          } catch (PINConfirmationException ex) {
//          } catch (PINFormatException ex) {

          } catch (PINOperationAbortedException ex) {
            log.error("Pin operation aborted without further details.");
            gui.showErrorDialog(PINManagementGUIFacade.ERR_PIN_OPERATION_ABORTED,
                    new Object[] {selectedPIN.getLocalizedName()},
                    this, null);
            waitForAction();
          }
        } // end if

        selectedPIN = null;
        gui.showPINManagementDialog(pinInfos,
                this, "activate_enterpin", "change_enterpin", "unblock_enterpuk", "verify_enterpin",
                this, "cancel");
      } // end while

//      } catch (GetPINStatusException ex) {
//        String pin = (selectedPIN != null) ? selectedPIN.getLocalizedName() : "pin";
//        log.error("failed to get " +  pin + " status: " + ex.getMessage());
//        gui.showErrorDialog(PINManagementGUIFacade.ERR_STATUS, null,
//                this, "ok");
//        waitForAction();
//        return new ErrorResponse(1000);
      } catch (SignatureCardException ex) {
        log.error(ex.getMessage(), ex);
        gui.showErrorDialog(PINManagementGUIFacade.ERR_UNKNOWN, null,
                this, "ok");
        waitForAction();
        ErrorResponse err = new ErrorResponse(1000);
        err.setErrorMessage(ex.getMessage());
        return err;
      }
    } else {
      log.error("Got unexpected STAL request: {}.", request);
      ErrorResponse err = new ErrorResponse(1000);
      err.setErrorMessage("Got unexpected STAL request: " + request);
      return err;
    }
  }

  private void activatePIN(PinInfo selectedPIN)
          throws InterruptedException, SignatureCardException {

    log.info("Activate {}.", selectedPIN.getLocalizedName());
    ManagementPINGUI pinGUI = new ManagementPINGUI((PINManagementGUIFacade) gui,
            PINManagementGUIFacade.DIALOG.ACTIVATE);

    boolean reentry = false;
    do {
      try {
        ((PINMgmtSignatureCard) card).activatePIN(selectedPIN, pinGUI);
      } catch (PINConfirmationException ex) {
        reentry = true;
        log.error("Confirmation pin does not match new {}.", selectedPIN
            .getLocalizedName());
        gui.showErrorDialog(PINManagementGUIFacade.ERR_PIN_CONFIRMATION,
                new Object[] {selectedPIN.getLocalizedName()},
                this, null);
        waitForAction();
      } catch (PINFormatException ex) {
        reentry = true;
        log.error("Wrong format of new {}.", selectedPIN.getLocalizedName());
        String pinSize = String.valueOf(selectedPIN.getMinLength());
        if (selectedPIN.getMinLength() != selectedPIN.getMaxLength()) {
            pinSize += "-" + selectedPIN.getMaxLength();
        }
        gui.showErrorDialog(PINManagementGUIFacade.ERR_PIN_FORMAT,
                new Object[] {selectedPIN.getLocalizedName(), pinSize},
                this, null);
        waitForAction();
      }
    } while (reentry);

//    updatePINState(selectedPIN, STATUS.ACTIV);
    gui.showMessageDialog(PINManagementGUIFacade.TITLE_ACTIVATE_SUCCESS,
            PINManagementGUIFacade.MESSAGE_ACTIVATE_SUCCESS,
            new Object[]{selectedPIN.getLocalizedName()},
            BKUGUIFacade.BUTTON_OK, this, "ok");
    waitForAction();
  }

  private void verifyPIN(PinInfo selectedPIN)
          throws InterruptedException, SignatureCardException {

    log.info("Verify {}.", selectedPIN.getLocalizedName());
    VerifyPINGUI pinGUI = new VerifyPINGUI(gui);

    boolean reentry = false;
    do {
      try {
        ((PINMgmtSignatureCard) card).verifyPIN(selectedPIN, pinGUI);
      } catch (PINFormatException ex) {
        reentry = true;
        log.error("Wrong format of new {}.", selectedPIN.getLocalizedName());
        String pinSize = String.valueOf(selectedPIN.getMinLength());
        if (selectedPIN.getMinLength() != selectedPIN.getMaxLength()) {
            pinSize += "-" + selectedPIN.getMaxLength();
        }
        gui.showErrorDialog(PINManagementGUIFacade.ERR_PIN_FORMAT,
                new Object[] {selectedPIN.getLocalizedName(), pinSize},
                this, null);
        waitForAction();
      }
    } while (reentry);

//    updatePINState(selectedPIN, STATUS.ACTIV);
  }

  private void changePIN(PinInfo selectedPIN)
          throws SignatureCardException, InterruptedException {

    log.info("Change {}.", selectedPIN.getLocalizedName());
    ManagementPINGUI pinGUI = new ManagementPINGUI((PINManagementGUIFacade) gui,
            PINManagementGUIFacade.DIALOG.CHANGE);

    boolean reentry = false;
    do {
      try {
        ((PINMgmtSignatureCard) card).changePIN(selectedPIN, pinGUI);
      } catch (PINConfirmationException ex) {
        reentry = true;
        log.error("Confirmation pin does not match new {}.", selectedPIN.getLocalizedName());
        gui.showErrorDialog(PINManagementGUIFacade.ERR_PIN_CONFIRMATION,
                new Object[] {selectedPIN.getLocalizedName()},
                this, null);
        waitForAction();
      } catch (PINFormatException ex) {
        reentry = true;
        log.error("Wrong format of new {}.", selectedPIN.getLocalizedName());
        String pinSize = String.valueOf(selectedPIN.getMinLength());
        if (selectedPIN.getMinLength() != selectedPIN.getMaxLength()) {
            pinSize += "-" + selectedPIN.getMaxLength();
        }
        gui.showErrorDialog(PINManagementGUIFacade.ERR_PIN_FORMAT,
                new Object[] {selectedPIN.getLocalizedName(), pinSize},
                this, null);
        waitForAction();
      }
    } while (reentry);

//    updatePINState(selectedPIN, STATUS.ACTIV);
    gui.showMessageDialog(PINManagementGUIFacade.TITLE_CHANGE_SUCCESS,
            PINManagementGUIFacade.MESSAGE_CHANGE_SUCCESS,
            new Object[]{selectedPIN.getLocalizedName()},
            BKUGUIFacade.BUTTON_OK, this, "ok");
    waitForAction();
  }

  private void unblockPIN(PinInfo selectedPIN)
          throws SignatureCardException, InterruptedException {

    log.info("Unblock {}.", selectedPIN.getLocalizedName());
    ManagementPINGUI pinGUI = new ManagementPINGUI((PINManagementGUIFacade) gui,
            PINManagementGUIFacade.DIALOG.UNBLOCK);

    boolean reentry = false;
    do {
      try {
        ((PINMgmtSignatureCard) card).unblockPIN(selectedPIN, pinGUI);
      } catch (PINConfirmationException ex) {
        reentry = true;
        log.error("Confirmation pin does not match new {}.", selectedPIN
            .getLocalizedName());
        gui.showErrorDialog(PINManagementGUIFacade.ERR_PIN_CONFIRMATION,
                new Object[] {selectedPIN.getLocalizedName()},
                this, null);
        waitForAction();
      } catch (PINFormatException ex) {
        reentry = true;
        log.error("Wrong format of new {}.", selectedPIN.getLocalizedName());
        String pinSize = String.valueOf(selectedPIN.getMinLength());
        if (selectedPIN.getMinLength() != selectedPIN.getMaxLength()) {
            pinSize += "-" + selectedPIN.getMaxLength();
        }
        gui.showErrorDialog(PINManagementGUIFacade.ERR_PIN_FORMAT,
                new Object[] {selectedPIN.getLocalizedName(), pinSize},
                this, null);
        waitForAction();
      }
    } while (reentry);

//    updatePINState(selectedPIN, STATUS.ACTIV);
    gui.showMessageDialog(PINManagementGUIFacade.TITLE_UNBLOCK_SUCCESS,
            PINManagementGUIFacade.MESSAGE_UNBLOCK_SUCCESS,
            new Object[]{selectedPIN.getLocalizedName()},
            BKUGUIFacade.BUTTON_OK, this, "ok");
    waitForAction();
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
//  private void updatePINState(PINSpec pinSpec, STATUS status)
//      throws GetPINStatusException {
//
//    PINMgmtSignatureCard pmCard = ((PINMgmtSignatureCard) card);
//    PIN_STATE pinState;
//    try {
//      pinState = pmCard.getPINState(pinSpec);
//    } catch (SignatureCardException e) {
//      String msg = "Failed to get PIN status for pin '"
//          + pinSpec.getLocalizedName() + "'.";
//      log.info(msg, e);
//      throw new GetPINStatusException(msg);
//    }
//    if (pinState == PIN_STATE.ACTIV) {
//      pinStates.put(pinSpec, STATUS.ACTIV);
//    } else if (pinState == PIN_STATE.NOT_ACTIV) {
//      pinStates.put(pinSpec, STATUS.NOT_ACTIV);
//    } else if (pinState == PIN_STATE.BLOCKED) {
//      pinStates.put(pinSpec, STATUS.BLOCKED);
//    } else {
//      pinStates.put(pinSpec, status);
//    }
//  }

}
