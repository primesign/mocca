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
import at.gv.egiz.smcc.ChangePINProvider;
import at.gv.egiz.bku.gui.PINManagementGUIFacade;
import at.gv.egiz.bku.smccstal.AbstractPINProvider;
import at.gv.egiz.bku.smccstal.PINProviderFactory;
import at.gv.egiz.smcc.CancelledException;
import at.gv.egiz.smcc.ccid.CCID;
import at.gv.egiz.smcc.PINProvider;
import at.gv.egiz.smcc.PINSpec;
import at.gv.egiz.smcc.SignatureCard;

/**
 *
 * @author Clemens Orthacker <clemens.orthacker@iaik.tugraz.at>
 */
public class ManagementPINProviderFactory extends PINProviderFactory {

  public ManagementPINProviderFactory(CCID reader, PINManagementGUIFacade gui) {
    super(reader, gui);
  }
  
//  public static ManagementPINProviderFactory getInstance(SignatureCard forCard,
//          PINManagementGUIFacade gui) {
//    if (forCard.getReader().hasFeature(CCID.FEATURE_VERIFY_PIN_DIRECT)) {
//      return new PinpadPINProviderFactory(gui);
//
//    } else {
//      return new SoftwarePINProviderFactory(gui);
//    }
//  }

  public PINProvider getVerifyPINProvider() {
    if (reader.hasFeature(CCID.FEATURE_VERIFY_PIN_START)) {
      return new PinpadGenericPinProvider(PINManagementGUIFacade.DIALOG.VERIFY);
    } else if (reader.hasFeature(CCID.FEATURE_VERIFY_PIN_DIRECT)) {
      return new PinpadGenericPinProvider(PINManagementGUIFacade.DIALOG.VERIFY);
    } else {
      return new SoftwareGenericPinProvider(PINManagementGUIFacade.DIALOG.VERIFY);
    }
  }

  public PINProvider getActivatePINProvider() {
    if (reader.hasFeature(CCID.FEATURE_MODIFY_PIN_START)) {
      return new PinpadGenericPinProvider(PINManagementGUIFacade.DIALOG.ACTIVATE);
    } else if (reader.hasFeature(CCID.FEATURE_MODIFY_PIN_DIRECT)) {
      return new PinpadGenericPinProvider(PINManagementGUIFacade.DIALOG.ACTIVATE);
    } else {
      return new SoftwareGenericPinProvider(PINManagementGUIFacade.DIALOG.ACTIVATE);
    }
  }

  public ChangePINProvider getChangePINProvider() {
    if (reader.hasFeature(CCID.FEATURE_MODIFY_PIN_START)) {
      return new PinpadGenericPinProvider(PINManagementGUIFacade.DIALOG.CHANGE);
    } else if (reader.hasFeature(CCID.FEATURE_MODIFY_PIN_DIRECT)) {
      return new PinpadGenericPinProvider(PINManagementGUIFacade.DIALOG.CHANGE);
    } else {
      return new ChangePinProvider();
    }
  }

  public PINProvider getUnblockPINProvider() {
    if (reader.hasFeature(CCID.FEATURE_VERIFY_PIN_START)) {
      return new PinpadGenericPinProvider(PINManagementGUIFacade.DIALOG.UNBLOCK);
    } else if (reader.hasFeature(CCID.FEATURE_VERIFY_PIN_DIRECT)) {
      return new PinpadGenericPinProvider(PINManagementGUIFacade.DIALOG.UNBLOCK);
    } else {
      return new SoftwareGenericPinProvider(PINManagementGUIFacade.DIALOG.UNBLOCK);
    }
  }

  class PinpadGenericPinProvider extends AbstractPINProvider
          implements ChangePINProvider {

    protected PINManagementGUIFacade.DIALOG type;

    private PinpadGenericPinProvider(PINManagementGUIFacade.DIALOG type) {
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
      } else if (type == PINManagementGUIFacade.DIALOG.VERIFY) {
        title = PINManagementGUIFacade.TITLE_VERIFY_PIN;
        message = BKUGUIFacade.MESSAGE_ENTERPIN_PINPAD;
        String pinSize = String.valueOf(pinSpec.getMinLength());
        if (pinSpec.getMinLength() != pinSpec.getMaxLength()) {
          pinSize += "-" + pinSpec.getMaxLength();
        }
        params = new Object[]{pinSpec.getLocalizedName(), pinSize};
      } else if (type == PINManagementGUIFacade.DIALOG.ACTIVATE) {
        title = PINManagementGUIFacade.TITLE_ACTIVATE_PIN;
        message = PINManagementGUIFacade.MESSAGE_ACTIVATEPIN_PINPAD;
        String pinSize = String.valueOf(pinSpec.getMinLength());
        if (pinSpec.getMinLength() != pinSpec.getMaxLength()) {
          pinSize += "-" + pinSpec.getMaxLength();
        }
        params = new Object[]{pinSpec.getLocalizedName(), pinSize};
      } else if (type == PINManagementGUIFacade.DIALOG.CHANGE) {
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


  class SoftwareGenericPinProvider extends AbstractPINProvider {

//    protected PINManagementGUIFacade gui;
    protected PINManagementGUIFacade.DIALOG type;

    private SoftwareGenericPinProvider(PINManagementGUIFacade.DIALOG type) {
      this.type = type;
    }

    @Override
    public char[] providePIN(PINSpec spec, int retries)
            throws CancelledException, InterruptedException {

      ((PINManagementGUIFacade) gui).showPINDialog(type, spec,
              (retry) ? retries : -1,
              this, "exec",
              this, "back");

      waitForAction();

      if ("exec".equals(action)) {
        gui.showMessageDialog(BKUGUIFacade.TITLE_WAIT,
                BKUGUIFacade.MESSAGE_WAIT);
        retry = true;
        return gui.getPin();
      } else if ("back".equals(action)) {
        throw new CancelledException();
      } else {
        log.error("unsupported command " + action);
        throw new CancelledException();
      }
    }
  }

  class ChangePinProvider extends AbstractPINProvider
          implements ChangePINProvider {

//    protected PINManagementGUIFacade gui;

    private char[] oldPin;
    private char[] newPin;

    private ChangePinProvider() {
    }

    @Override
    public char[] providePIN(PINSpec spec, int retries)
            throws CancelledException, InterruptedException {
      if (newPin == null) {
        getPINs(spec, retries);
      }
      char[] pin = newPin;
      newPin = null;
      return pin;
    }

    @Override
    public char[] provideOldPIN(PINSpec spec, int retries)
            throws CancelledException, InterruptedException {
      if (oldPin == null) {
        getPINs(spec, retries);
      }
      char[] pin = oldPin;
      oldPin = null;
      return pin;
    }

    private void getPINs(PINSpec spec, int retries)
            throws InterruptedException, CancelledException {

      ((PINManagementGUIFacade) gui).showPINDialog(
              PINManagementGUIFacade.DIALOG.CHANGE, spec,
              (retry) ? retries : -1,
              this, "exec",
              this, "back");

      waitForAction();

      if ("exec".equals(action)) {
        gui.showMessageDialog(BKUGUIFacade.TITLE_WAIT,
                BKUGUIFacade.MESSAGE_WAIT);
        retry = true;
        oldPin = ((PINManagementGUIFacade) gui).getOldPin();
        newPin = gui.getPin();
      } else if ("back".equals(action)) {
        throw new CancelledException();
      } else {
        log.error("unsupported command " + action);
        throw new CancelledException();
      }
    }
  }
}
