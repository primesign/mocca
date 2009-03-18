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
import at.gv.egiz.bku.gui.PINManagementGUIFacade.DIALOG;
import at.gv.egiz.bku.smccstal.AbstractPINProvider;
import at.gv.egiz.smcc.CancelledException;
import at.gv.egiz.smcc.PINProvider;
import at.gv.egiz.smcc.PINSpec;

/**
 *
 * @author Clemens Orthacker <clemens.orthacker@iaik.tugraz.at>
 */
public class SoftwarePINProviderFactory extends ManagementPINProviderFactory {

  protected SoftwarePINProviderFactory(PINManagementGUIFacade gui) {
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
    return new ChangePinProvider();
  }

  @Override
  public PINProvider getUnblockPINProvider() {
    return new SimplePinProvider(DIALOG.UNBLOCK);
  }

  class SimplePinProvider extends AbstractPINProvider {

//    protected PINManagementGUIFacade gui;
    protected PINManagementGUIFacade.DIALOG type;

    private SimplePinProvider(DIALOG type) {
      this.type = type;
    }

    @Override
    public char[] providePIN(PINSpec spec, int retries)
            throws CancelledException, InterruptedException {

      gui.showPINDialog(type, spec, (retry) ? retries : -1,
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

      gui.showPINDialog(PINManagementGUIFacade.DIALOG.CHANGE, spec,
              (retry) ? retries : -1,
              this, "exec",
              this, "back");

      waitForAction();

      if ("exec".equals(action)) {
        gui.showMessageDialog(BKUGUIFacade.TITLE_WAIT,
                BKUGUIFacade.MESSAGE_WAIT);
        retry = true;
        oldPin = gui.getOldPin();
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
