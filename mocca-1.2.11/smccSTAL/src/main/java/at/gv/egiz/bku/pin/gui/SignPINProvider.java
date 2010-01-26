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
import at.gv.egiz.smcc.pin.gui.PINProvider;
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
public class SignPINProvider extends AbstractPINProvider implements PINProvider {

  protected static final Log log = LogFactory.getLog(SignPINProvider.class);

  protected BKUGUIFacade gui;
  protected SecureViewer viewer;
  protected SignedInfoType signedInfo;
  private boolean retry = false;

  public SignPINProvider(BKUGUIFacade gui, SecureViewer viewer, SignedInfoType signedInfo) {
    this.gui = gui;
    this.viewer = viewer;
    this.signedInfo = signedInfo;
  }

  @Override
  public char[] providePIN(PINSpec spec, int retries)
          throws CancelledException, InterruptedException {

    gui.showSignaturePINDialog(spec, (retry) ? retries : -1,
            this, "sign",
            this, "cancel",
            this, "secureViewer");

    do {
      log.trace("[" + Thread.currentThread().getName() + "] wait for action");
      waitForAction();
      log.trace("[" + Thread.currentThread().getName() + "] received action " + action);

      if ("secureViewer".equals(action)) {
        try {
          viewer.displayDataToBeSigned(signedInfo, this, "pinEntry");
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
      } else if ("sign".equals(action)) {
        gui.showMessageDialog(BKUGUIFacade.TITLE_WAIT,
                BKUGUIFacade.MESSAGE_WAIT);
        retry = true;
        return gui.getPin();
      } else if ("pinEntry".equals(action)) {
        gui.showSignaturePINDialog(spec, (retry) ? retries : -1,
                this, "sign",
                this, "cancel",
                this, "secureViewer");
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
}
