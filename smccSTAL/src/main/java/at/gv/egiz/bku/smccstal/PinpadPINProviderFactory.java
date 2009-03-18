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

import at.gv.egiz.bku.gui.BKUGUIFacade;
import at.gv.egiz.smcc.CancelledException;
import at.gv.egiz.smcc.PINProvider;
import at.gv.egiz.smcc.PINSpec;
import at.gv.egiz.stal.HashDataInput;
import at.gv.egiz.stal.signedinfo.SignedInfoType;
import java.util.List;

/**
 *
 * @author Clemens Orthacker <clemens.orthacker@iaik.tugraz.at>
 */
public class PinpadPINProviderFactory extends PINProviderFactory {

  protected PinpadPINProviderFactory(BKUGUIFacade gui) {
    this.gui = gui;
  }

  @Override
  public PINProvider getSignaturePINProvider(SecureViewer viewer,
          SignedInfoType signedInfo) {

    return new SignaturePinProvider(viewer, signedInfo);
  }

  @Override
  public PINProvider getCardPINProvider() {
    return new CardPinProvider();
  }

  class SignaturePinProvider extends AbstractPINProvider {

//    protected BKUGUIFacade gui;
    protected SecureViewer viewer;
    protected SignedInfoType signedInfo;
    protected List<HashDataInput> hashDataInputs;

    private SignaturePinProvider(SecureViewer viewer,
            SignedInfoType signedInfo) {
      this.viewer = viewer;
      this.signedInfo = signedInfo;
    }

    @Override
    public char[] providePIN(PINSpec spec, int retries)
            throws CancelledException, InterruptedException {

      showPinpadPINDialog(retries, spec);
      retry = true;
      return null;

//      do {
//        waitForAction();
//        gui.showWaitDialog(null);
//
//        if ("hashData".equals(action)) {
//          // show pin dialog in background
//          gui.showSignaturePINDialog(spec, (retry) ? retries : -1,
//                  this, "sign",
//                  this, "cancel",
//                  this, "hashData");
//
//          viewer.displayDataToBeSigned(signedInfo.getReference());
//
//        } else if ("sign".equals(action)) {
//          retry = true;
//          return gui.getPin();
//        } else if ("hashDataDone".equals(action)) {
//          gui.showSignaturePINDialog(spec, (retry) ? retries : -1,
//                  this, "sign",
//                  this, "cancel",
//                  this, "hashData");
//        } else if ("cancel".equals(action) ||
//                "error".equals(action)) {
//          throw new CancelledException(spec.getLocalizedName() +
//                  " entry cancelled");
//        }
//      } while (true);
    }

    private void showPinpadPINDialog(int retries, PINSpec pinSpec) {
      String title, message;
      Object[] params;
      if (retry) {
        title = BKUGUIFacade.TITLE_RETRY;
        message = BKUGUIFacade.MESSAGE_RETRIES;
        params = new Object[]{String.valueOf(retries)};
      } else {
        title = BKUGUIFacade.TITLE_SIGN;
        message = BKUGUIFacade.MESSAGE_ENTERPIN_PINPAD;
        String pinSize = String.valueOf(pinSpec.getMinLength());
        if (pinSpec.getMinLength() != pinSpec.getMaxLength()) {
          pinSize += "-" + pinSpec.getMaxLength();
        }
        params = new Object[]{pinSpec.getLocalizedName(), pinSize};
      }
      gui.showMessageDialog(title, message, params);
    }
  }

  class CardPinProvider extends AbstractPINProvider {

    private CardPinProvider() {
    }

    @Override
    public char[] providePIN(PINSpec spec, int retries)
            throws CancelledException, InterruptedException {

      showPinpadPINDialog(retries, spec);
      retry = true;
      return null;

    }

    private void showPinpadPINDialog(int retries, PINSpec pinSpec) {
      String title, message;
      Object[] params;
      if (retry) {
        title = BKUGUIFacade.TITLE_RETRY;
        message = BKUGUIFacade.MESSAGE_RETRIES;
        params = new Object[]{String.valueOf(retries)};
      } else {
        title = BKUGUIFacade.TITLE_CARDPIN;
        message = BKUGUIFacade.MESSAGE_ENTERPIN_PINPAD;
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

