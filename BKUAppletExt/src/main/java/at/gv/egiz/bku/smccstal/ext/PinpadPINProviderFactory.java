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

import at.gv.egiz.smcc.ChangePINProvider;
import at.gv.egiz.bku.gui.BKUGUIFacade;
import at.gv.egiz.bku.gui.PINManagementGUIFacade;
import at.gv.egiz.bku.gui.PINManagementGUIFacade.DIALOG;
import at.gv.egiz.bku.smccstal.AbstractPINProvider;
import at.gv.egiz.smcc.CancelledException;
import at.gv.egiz.smcc.PINProvider;
import at.gv.egiz.smcc.PINSpec;

/**
 *
 * @author Clemens Orthacker <clemens.orthacker@iaik.tugraz.at>
 */
public class PinpadPINProviderFactory extends ManagementPINProviderFactory {

  protected PinpadPINProviderFactory(PINManagementGUIFacade gui) {
    this.gui = gui;
  }

  @Override
  public PINProvider getVerifyPINProvider() {
    return new SimplePinProvider(DIALOG.VERIFY);
  }

  @Override
  public PINProvider getActivatePINProvider() {
    return new SimplePinProvider(DIALOG.ACTIVATE);
  }

  @Override
  public ChangePINProvider getChangePINProvider() {
    return new SimplePinProvider(DIALOG.CHANGE);
  }

  @Override
  public PINProvider getUnblockPINProvider() {
    return new SimplePinProvider(DIALOG.UNBLOCK);
  }


  class SimplePinProvider extends AbstractPINProvider
          implements ChangePINProvider {

//    protected PINManagementGUIFacade gui;
    protected PINManagementGUIFacade.DIALOG type;

    private SimplePinProvider(PINManagementGUIFacade.DIALOG type) {
      this.type = type;
    }

    @Override
    public char[] providePIN(PINSpec spec, int retries)
            throws CancelledException, InterruptedException {

      showPinpadPINDialog(retries, spec);
      retry = true;
      return null;
    }

    /**
     * do not call this method without calling providePIN()
     * (no message is displayed)
     * @param spec
     * @param retries
     * @return
     */
    @Override
    public char[] provideOldPIN(PINSpec spec, int retries) {
      return null;
    }

    private void showPinpadPINDialog(int retries, PINSpec pinSpec) {
      String title, message;
      Object[] params;
      if (retry) {
        title = BKUGUIFacade.TITLE_RETRY;
        message = BKUGUIFacade.MESSAGE_RETRIES;
        params = new Object[]{String.valueOf(retries)};
      } else if (type == DIALOG.VERIFY) {
        title = PINManagementGUIFacade.TITLE_VERIFY_PIN;
        message = BKUGUIFacade.MESSAGE_ENTERPIN_PINPAD;
        String pinSize = String.valueOf(pinSpec.getMinLength());
        if (pinSpec.getMinLength() != pinSpec.getMaxLength()) {
          pinSize += "-" + pinSpec.getMaxLength();
        }
        params = new Object[]{pinSpec.getLocalizedName(), pinSize};
      } else if (type == DIALOG.ACTIVATE) {
        title = PINManagementGUIFacade.TITLE_ACTIVATE_PIN;
        message = PINManagementGUIFacade.MESSAGE_ACTIVATEPIN_PINPAD;
        String pinSize = String.valueOf(pinSpec.getMinLength());
        if (pinSpec.getMinLength() != pinSpec.getMaxLength()) {
          pinSize += "-" + pinSpec.getMaxLength();
        }
        params = new Object[]{pinSpec.getLocalizedName(), pinSize};
      } else if (type == DIALOG.CHANGE) {
        title = PINManagementGUIFacade.TITLE_CHANGE_PIN;
        message = PINManagementGUIFacade.MESSAGE_CHANGEPIN_PINPAD;
        String pinSize = String.valueOf(pinSpec.getMinLength());
        if (pinSpec.getMinLength() != pinSpec.getMaxLength()) {
          pinSize += "-" + pinSpec.getMaxLength();
        }
        params = new Object[]{pinSpec.getLocalizedName(), pinSize};
      } else { //if (type == DIALOG.UNBLOCK) {
        title = PINManagementGUIFacade.TITLE_UNBLOCK_PIN;
        message = PINManagementGUIFacade.MESSAGE_UNBLOCKPIN_PINPAD;
        String pinSize = String.valueOf(pinSpec.getMinLength());
        if (pinSpec.getMinLength() != pinSpec.getMaxLength()) {
          pinSize += "-" + pinSpec.getMaxLength();
        }
        params = new Object[]{pinSpec.getLocalizedName(), pinSize};
      }
      gui.showMessageDialog(title, message, params);
    }
  }
}
