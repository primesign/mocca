/*
 * Copyright 2015 Datentechnik Innovation GmbH and Prime Sign GmbH, Austria
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

package at.gv.egiz.bku.pin.gui;

import at.gv.egiz.bku.gui.BKUGUIFacade;
import at.gv.egiz.bku.smccstal.SecureViewer;
import at.gv.egiz.smcc.CancelledException;
import at.gv.egiz.smcc.PinInfo;
import at.gv.egiz.smcc.pin.gui.PINGUI;
import at.gv.egiz.stal.signedinfo.SignedInfoType;

import java.security.DigestException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This PinProvider is used for BulkSignatureRequests.
 * The pin input field is called once and the pin is stored for further sign requests. 
 *
 *
 * @author szoescher
 */
public class BulkSignPINGUI extends SignPINGUI implements PINGUI {

  private final Logger log = LoggerFactory.getLogger(BulkSignPINGUI.class);

  private boolean retry = false;

  private char[] pin;
  
  boolean showSignaturePINDialog;

  public BulkSignPINGUI(BKUGUIFacade gui, SecureViewer viewer, SignedInfoType signedInfo) {
    super(gui, viewer, signedInfo);
    
    showSignaturePINDialog =true;
  }
  
  


  public boolean isShowSignaturePINDialog() {
    return showSignaturePINDialog;
  }




  public void setShowSignaturePINDialog(boolean showSignaturePINDialog) {
    this.showSignaturePINDialog = showSignaturePINDialog;
  }




  @Override
  public char[] providePIN(PinInfo spec, int retries) throws CancelledException, InterruptedException {

    if (showSignaturePINDialog) {

      gui.showSignaturePINDialog(spec, (retry) ? retries : -1, this, "sign", this, "cancel", this, "secureViewer");

      do {
        log.trace("[{}] wait for action.", Thread.currentThread().getName());
        waitForAction();
        log.trace("[{}] received action {}.", Thread.currentThread().getName(), action);

        if ("secureViewer".equals(action)) {
          try {
            viewer.displayDataToBeSigned(signedInfo, this, "pinEntry");
          } catch (DigestException ex) {
            log.error("Bad digest value: {}", ex.getMessage());
            gui.showErrorDialog(BKUGUIFacade.ERR_INVALID_HASH, new Object[] { ex.getMessage() }, this, "error");
          } catch (Exception ex) {
            log.error("Could not display hashdata inputs: {}", ex.getMessage());
            gui.showErrorDialog(BKUGUIFacade.ERR_DISPLAY_HASHDATA, new Object[] { ex.getMessage() }, this, "error");
          }
        } else if ("sign".equals(action)) {
          gui.showMessageDialog(BKUGUIFacade.TITLE_WAIT, BKUGUIFacade.MESSAGE_WAIT);
          retry = true;
          pin = gui.getPin();
          return pin;
        } else if ("pinEntry".equals(action)) {
          gui.showSignaturePINDialog(spec, (retry) ? retries : -1, this, "sign", this, "cancel", this, "secureViewer");
        } else if ("cancel".equals(action) || "error".equals(action)) {
          gui.showMessageDialog(BKUGUIFacade.TITLE_WAIT, BKUGUIFacade.MESSAGE_WAIT);
          throw new CancelledException(spec.getLocalizedName() + " entry cancelled");
        } else {
          log.error("Unknown action command {}.", action);
        }
      } while (true);
    } else {
      return pin;
    }
  }
}
