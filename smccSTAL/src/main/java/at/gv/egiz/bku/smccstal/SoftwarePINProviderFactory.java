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
import at.gv.egiz.smcc.*;
import at.gv.egiz.stal.HashDataInput;
import at.gv.egiz.stal.signedinfo.SignedInfoType;
import java.security.DigestException;
import java.util.List;

/**
 *
 * @author Clemens Orthacker <clemens.orthacker@iaik.tugraz.at>
 */
public class SoftwarePINProviderFactory extends PINProviderFactory {

  protected SoftwarePINProviderFactory(BKUGUIFacade gui) {
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

      gui.showSignaturePINDialog(spec, (retry) ? retries : -1,
              this, "sign",
              this, "cancel",
              this, "hashData");

      do {
        waitForAction();
        gui.showMessageDialog(BKUGUIFacade.TITLE_WAIT,
                BKUGUIFacade.MESSAGE_WAIT);

        if ("hashData".equals(action)) {
          // show pin dialog in background
          gui.showSignaturePINDialog(spec, (retry) ? retries : -1,
                  this, "sign",
                  this, "cancel",
                  this, "hashData");

          try {
            viewer.displayDataToBeSigned(signedInfo.getReference());
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
          retry = true;
          return gui.getPin();
        } else if ("hashDataDone".equals(action)) {
          gui.showSignaturePINDialog(spec, (retry) ? retries : -1,
                  this, "sign",
                  this, "cancel",
                  this, "hashData");
        } else if ("cancel".equals(action) ||
                "error".equals(action)) {
          throw new CancelledException(spec.getLocalizedName() +
                  " entry cancelled");
        }
      } while (true);
    }
  }

  class CardPinProvider extends AbstractPINProvider {

//    protected BKUGUIFacade gui;

    private CardPinProvider() {
    }

    @Override
    public char[] providePIN(PINSpec spec, int retries)
            throws CancelledException, InterruptedException {

      gui.showCardPINDialog(spec, (retry) ? retries : -1,
              this, "ok",
              this, "cancel");

      waitForAction();
      
      gui.showMessageDialog(BKUGUIFacade.TITLE_WAIT,
              BKUGUIFacade.MESSAGE_WAIT);

      if ("cancel".equals(action)) {
        throw new CancelledException(spec.getLocalizedName() +
                  " entry cancelled");
      }
      retry = true;
      return gui.getPin();
    }
  }
}
