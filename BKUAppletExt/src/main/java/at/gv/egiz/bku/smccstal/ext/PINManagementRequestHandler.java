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
import at.gv.egiz.bku.gui.PINManagementGUIFacade.DIALOG;
import at.gv.egiz.bku.gui.PINManagementGUIFacade.STATUS;
import at.gv.egiz.bku.smccstal.AbstractRequestHandler;
import at.gv.egiz.bku.smccstal.PINProviderFactory;
import at.gv.egiz.smcc.CancelledException;
import at.gv.egiz.smcc.LockedException;
import at.gv.egiz.smcc.NotActivatedException;
import at.gv.egiz.smcc.PINProvider;
import at.gv.egiz.smcc.PINSpec;
import at.gv.egiz.smcc.STARCOSCard;
import at.gv.egiz.smcc.SignatureCard;
import at.gv.egiz.smcc.SignatureCardException;
import at.gv.egiz.smcc.TimeoutException;
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

  protected Map<PINSpec, STATUS> pinStatuses;
  private ManagementPINProviderFactory pinProviderFactory;

  @Override
  public STALResponse handleRequest(STALRequest request) throws InterruptedException {
    if (request instanceof PINManagementRequest) {

      PINManagementGUIFacade gui = (PINManagementGUIFacade) this.gui;

      PINSpec selectedPIN = null;

      try {

      pinStatuses = getPINStatuses();
      
      gui.showPINManagementDialog(pinStatuses,
              this, "activate_enterpin", "change_enterpin", "unblock_enterpuk", "verify_enterpin",
              this, "cancel");

      while (true) {

        waitForAction();

        if ("cancel".equals(actionCommand)) {
          return new PINManagementResponse();
        } else {
          selectedPIN = gui.getSelectedPINSpec();

          if (selectedPIN == null) {
            throw new NullPointerException("no PIN selected for activation/change");
          }

          if (pinProviderFactory == null) {
            pinProviderFactory =
                    ManagementPINProviderFactory.getInstance(card, gui);
          }

          try {
            if ("activate_enterpin".equals(actionCommand)) {
              log.info("activate " + selectedPIN.getLocalizedName());
              card.activatePIN(selectedPIN, 
                      pinProviderFactory.getActivatePINProvider());
              updatePINStatus(selectedPIN, STATUS.ACTIV);
              gui.showMessageDialog(PINManagementGUIFacade.TITLE_ACTIVATE_SUCCESS,
                      PINManagementGUIFacade.MESSAGE_ACTIVATE_SUCCESS,
                      new Object[] {selectedPIN.getLocalizedName()},
                      BKUGUIFacade.BUTTON_OK, this, "ok");
              waitForAction();
            } else if ("change_enterpin".equals(actionCommand)) {
              log.info("change " + selectedPIN.getLocalizedName());
              card.changePIN(selectedPIN, 
                      pinProviderFactory.getChangePINProvider());
              updatePINStatus(selectedPIN, STATUS.ACTIV);
              gui.showMessageDialog(PINManagementGUIFacade.TITLE_CHANGE_SUCCESS,
                      PINManagementGUIFacade.MESSAGE_CHANGE_SUCCESS,
                      new Object[] {selectedPIN.getLocalizedName()},
                      BKUGUIFacade.BUTTON_OK, this, "ok");
              waitForAction();

            } else if ("unblock_enterpuk".equals(actionCommand)) {
              log.info("unblock " + selectedPIN.getLocalizedName());
              card.unblockPIN(selectedPIN,
                      pinProviderFactory.getUnblockPINProvider());
            } else if ("verify_enterpin".equals(actionCommand)) {
              log.info("verify " + selectedPIN.getLocalizedName());
              card.verifyPIN(selectedPIN,
                      pinProviderFactory.getVerifyPINProvider());
              updatePINStatus(selectedPIN, STATUS.ACTIV);
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
            updatePINStatus(selectedPIN, STATUS.BLOCKED);
            gui.showErrorDialog(PINManagementGUIFacade.ERR_LOCKED,
                    new Object[] {selectedPIN.getLocalizedName()},
                    this, null);
            waitForAction();
          } catch (NotActivatedException ex) {
            log.error(selectedPIN.getLocalizedName() + " not active");
            updatePINStatus(selectedPIN, STATUS.NOT_ACTIV);
            gui.showErrorDialog(PINManagementGUIFacade.ERR_NOT_ACTIVE,
                    new Object[] {selectedPIN.getLocalizedName()},
                    this, null);
            waitForAction();
          }
        } // end if

        selectedPIN = null;
        gui.showPINManagementDialog(pinStatuses,
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

  private Map<PINSpec, STATUS> getPINStatuses() throws GetPINStatusException {
    HashMap<PINSpec, STATUS> pinStatuses = new HashMap<PINSpec, STATUS>();
    List<PINSpec> pins = card.getPINSpecs();

    if (card instanceof STARCOSCard) {
      Card icc = card.getCard();
      try {
        icc.beginExclusive();
        CardChannel channel = icc.getBasicChannel();

        for (PINSpec pinSpec : pins) {
          byte kid = pinSpec.getKID();
          byte[] contextAID = pinSpec.getContextAID();

          if (contextAID != null) {
            CommandAPDU selectAPDU = new CommandAPDU(0x00, 0xa4, 0x04, 0x0c, contextAID);
            ResponseAPDU responseAPDU = channel.transmit(selectAPDU);
            if (responseAPDU.getSW() != 0x9000) {
              icc.endExclusive();
              String msg = "Select AID " + SMCCHelper.toString(pinSpec.getContextAID()) +
                  ": SW=" + Integer.toHexString(responseAPDU.getSW());
              log.error(msg);
              throw new GetPINStatusException(msg);
            }
          }

          CommandAPDU verifyAPDU = new CommandAPDU(new byte[] {
            (byte) 0x00, (byte) 0x20, (byte) 00, kid });
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
            log.debug("PIN " + pinSpec.getLocalizedName() +
                    " status: " + SMCCHelper.toString(responseAPDU.getBytes()));
          }
          pinStatuses.put(pinSpec, status);
        }
        return pinStatuses;

      } catch (CardException ex) {
        log.error("Failed to get PIN status: " + ex.getMessage(), ex);
        throw new GetPINStatusException(ex.getMessage());
      } finally {
        try {
          icc.endExclusive();
        } catch (CardException ex) {
          log.trace("failed to end exclusive card access: " + ex.getMessage());
        }
      }
    } else {
      for (PINSpec pinSpec : pins) {
        pinStatuses.put(pinSpec, STATUS.UNKNOWN);
      }
    }
    return pinStatuses;
  }

  /**
   * query status for STARCOS card,
   * assume provided status for ACOS card
   * @param pinSpec
   * @param status
   * @throws at.gv.egiz.smcc.SignatureCardException if query status fails
   */
  private void updatePINStatus(PINSpec pinSpec, STATUS status) throws GetPINStatusException {
    if (card instanceof STARCOSCard) {
      Card icc = card.getCard();
      try {
        icc.beginExclusive();
        CardChannel channel = icc.getBasicChannel();

          byte kid = pinSpec.getKID();
          byte[] contextAID = pinSpec.getContextAID();

          if (contextAID != null) {
            CommandAPDU selectAPDU = new CommandAPDU(0x00, 0xa4, 0x04, 0x0c, contextAID);
            ResponseAPDU responseAPDU = channel.transmit(selectAPDU);
            if (responseAPDU.getSW() != 0x9000) {
              icc.endExclusive();
              String msg = "Select AID " + SMCCHelper.toString(pinSpec.getContextAID()) +
                  ": SW=" + Integer.toHexString(responseAPDU.getSW());
              log.error(msg);
              throw new GetPINStatusException(msg);
            }
          }

          CommandAPDU verifyAPDU = new CommandAPDU(new byte[] {
            (byte) 0x00, (byte) 0x20, (byte) 00, kid });
          ResponseAPDU responseAPDU = channel.transmit(verifyAPDU);

          status = STATUS.UNKNOWN;
          if (responseAPDU.getSW() == 0x6984) {
            status = STATUS.NOT_ACTIV;
          } else if (responseAPDU.getSW() == 0x63c0) {
            status = STATUS.BLOCKED;
          } else if (responseAPDU.getSW1() == 0x63) {
            status = STATUS.ACTIV;
          }
          if (log.isDebugEnabled()) {
            log.debug(pinSpec.getLocalizedName() +
                    " status: " + SMCCHelper.toString(responseAPDU.getBytes()));
          }
          pinStatuses.put(pinSpec, status);

      } catch (CardException ex) {
        log.error("Failed to get PIN status: " + ex.getMessage(), ex);
        throw new GetPINStatusException(ex.getMessage());
      } finally {
        try {
          icc.endExclusive();
        } catch (CardException ex) {
          log.warn("failed to end exclusive card access: " + ex.getMessage());
        }
      }
    } else {
      pinStatuses.put(pinSpec, status);
    }
  }

//  /**
//   * provides oldPin and newPin from one dialog,
//   * and don't know whether providePIN() or provideOldPIN() is called first.
//   */
//  class SoftwarePinProvider implements PINProvider {
//
//    private PINManagementGUIFacade.DIALOG type;
//    private boolean retry = false;
//
//    private char[] newPin;
//    private char[] oldPin;
//
//    public SoftwarePinProvider(DIALOG type) {
//      this.type = type;
//    }
//
//    @Override
//    public char[] providePIN(PINSpec spec, int retries)
//            throws CancelledException, InterruptedException {
//      if (newPin == null) {
//        getPINs(spec, retries);
//      }
//      char[] pin = newPin;
//      newPin = null;
//      return pin;
//    }
//
//    @Override
//    public char[] provideOldPIN(PINSpec spec, int retries)
//            throws CancelledException, InterruptedException {
//      if (oldPin == null) {
//        getPINs(spec, retries);
//      }
//      char[] pin = oldPin;
//      oldPin = null;
//      return pin;
//    }
//
//    private void getPINs(PINSpec spec, int retries)
//            throws InterruptedException, CancelledException {
//      PINManagementGUIFacade gui =
//              (PINManagementGUIFacade) PINManagementRequestHandler.this.gui;
//
//      if (retry) {
//        gui.showPINDialog(type, spec, retries,
//              PINManagementRequestHandler.this, "exec",
//              PINManagementRequestHandler.this, "back");
//      } else {
//        gui.showPINDialog(type, spec,
//              PINManagementRequestHandler.this, "exec",
//              PINManagementRequestHandler.this, "back");
//      }
//      waitForAction();
//
//      if (actionCommand.equals("exec")) {
//        gui.showWaitDialog(null);
//        retry = true;
//        oldPin = gui.getOldPin();
//        newPin = gui.getPin();
//      } else if (actionCommand.equals("back")) {
//        throw new CancelledException();
//      } else {
//        log.error("unsupported command " + actionCommand);
//        throw new CancelledException();
//      }
//    }
//  }
//
//
//  class PinpadPinProvider implements PINProvider {
//
//    private PINManagementGUIFacade.DIALOG type;
//    private boolean retry = false;
//
//    public PinpadPinProvider(DIALOG type) {
//      this.type = type;
//    }
//
//    @Override
//    public char[] providePIN(PINSpec spec, int retries) {
//      log.debug("provide pin for " + type);
//      if (retry) {
//        ((PINManagementGUIFacade) gui).showPinpadPINDialog(type, spec, retries);
//      } else {
//        ((PINManagementGUIFacade) gui).showPinpadPINDialog(type, spec, -1);
//        retry = true;
//      }
//      return null;
//    }
//
//    @Override
//    public char[] provideOldPIN(PINSpec spec, int retries) {
//      return null;
//    }
//  }
}
