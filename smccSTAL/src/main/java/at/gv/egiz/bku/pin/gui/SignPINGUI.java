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
 * The number of retries is not fixed and there is no way (?) to obtain this value.
 * A PINProvider should therefore maintain an internal retry counter or flag
 * to decide whether or not to warn the user (num retries passed in providePIN).
 *
 * Therefore PINProvider objects should not be reused.
 *
 * (ACOS: reload counter: between 0 and 15, where 15 meens deactivated)
 *
 * @author Clemens Orthacker <clemens.orthacker@iaik.tugraz.at>
 */
public class SignPINGUI extends SignPINProvider implements PINGUI {

  private final Logger log = LoggerFactory.getLogger(SignPINGUI.class);

  private boolean retry = false;

  public SignPINGUI(BKUGUIFacade gui, SecureViewer viewer, SignedInfoType signedInfo) {
    super(gui, viewer, signedInfo);
  }

  @Override
  public void enterPINDirect(PinInfo spec, int retries)
          throws CancelledException, InterruptedException {
    if (retry) {
      gui.showEnterPINDirect(spec, retries);
    } else {
      showSignatureData(spec);
      gui.showEnterPINDirect(spec, -1);
      retry = true;
    }
  }

  @Override
  public void enterPIN(PinInfo spec, int retries)
          throws CancelledException, InterruptedException {
    if (retry) {
      gui.showEnterPIN(spec, retries);
    } else {
      showSignatureData(spec);
      gui.showEnterPIN(spec, -1);
      retry = true;
    }
  }

  private void showSignatureData(PinInfo spec)
          throws CancelledException, InterruptedException {

    gui.showSignatureDataDialog(spec,
            this, "enterPIN",
            this, "cancel",
            this, "secureViewer");

    do {
      log.trace("[{}] wait for action.", Thread.currentThread().getName());
      waitForAction();
      log.trace("[{}] received action {}.", Thread.currentThread().getName(), action);

      if ("secureViewer".equals(action)) {
        try {
          viewer.displayDataToBeSigned(signedInfo, this, "signatureData");
        } catch (DigestException ex) {
          log.error("Bad digest value: {}", ex.getMessage());
          gui.showErrorDialog(BKUGUIFacade.ERR_INVALID_HASH,
                  new Object[]{ex.getMessage()},
                  this, "error");
        } catch (Exception ex) {
          log.error("Could not display hashdata inputs: {}.",
                  ex.getMessage());
          gui.showErrorDialog(BKUGUIFacade.ERR_DISPLAY_HASHDATA,
                  new Object[]{ex.getMessage()},
                  this, "error");
        }
      } else if ("signatureData".equals(action)) {
        gui.showSignatureDataDialog(spec,
                this, "enterPIN",
                this, "cancel",
                this, "secureViewer");
      } else if ("enterPIN".equals(action)) {
        return;
      } else if ("cancel".equals(action) ||
              "error".equals(action)) {
        gui.showMessageDialog(BKUGUIFacade.TITLE_WAIT,
                BKUGUIFacade.MESSAGE_WAIT);
        throw new CancelledException(spec.getLocalizedName() +
                " entry cancelled");
      } else {
        log.error("Unknown action command {}.", action);
      }
    } while (true);
  }

  @Override
  public void validKeyPressed() {
    gui.validKeyPressed();
  }

  @Override
  public void correctionButtonPressed() {
    gui.correctionButtonPressed();
  }

  @Override
  public void allKeysCleared() {
    gui.allKeysCleared();
  }

}
