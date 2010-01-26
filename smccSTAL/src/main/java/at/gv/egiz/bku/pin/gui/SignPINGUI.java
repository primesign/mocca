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
package at.gv.egiz.bku.pin.gui;

import at.gv.egiz.bku.gui.BKUGUIFacade;
import at.gv.egiz.bku.smccstal.SecureViewer;
import at.gv.egiz.smcc.CancelledException;
import at.gv.egiz.smcc.PINSpec;
import at.gv.egiz.smcc.pin.gui.PINGUI;
import at.gv.egiz.stal.signedinfo.SignedInfoType;
import java.security.DigestException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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

  protected static final Log log = LogFactory.getLog(SignPINGUI.class);

  private boolean retry = false;

  public SignPINGUI(BKUGUIFacade gui, SecureViewer viewer, SignedInfoType signedInfo) {
    super(gui, viewer, signedInfo);
  }

  @Override
  public void enterPINDirect(PINSpec spec, int retries)
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
  public void enterPIN(PINSpec spec, int retries)
          throws CancelledException, InterruptedException {
    if (retry) {
      gui.showEnterPIN(spec, retries);
    } else {
      showSignatureData(spec);
      gui.showEnterPIN(spec, -1);
      retry = true;
    }
  }

  private void showSignatureData(PINSpec spec)
          throws CancelledException, InterruptedException {

    gui.showSignatureDataDialog(spec,
            this, "enterPIN",
            this, "cancel",
            this, "secureViewer");

    do {
      log.trace("[" + Thread.currentThread().getName() + "] wait for action");
      waitForAction();
      log.trace("[" + Thread.currentThread().getName() + "] received action " + action);

      if ("secureViewer".equals(action)) {
        try {
          viewer.displayDataToBeSigned(signedInfo, this, "signatureData");
        } catch (DigestException ex) {
          log.error("Bad digest value: " + ex.getMessage());
          gui.showErrorDialog(BKUGUIFacade.ERR_INVALID_HASH,
                  new Object[]{ex.getMessage()},
                  this, "error");
        } catch (Exception ex) {
          log.error("Could not display hashdata inputs: " +
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
        log.error("unknown action command " + action);
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
