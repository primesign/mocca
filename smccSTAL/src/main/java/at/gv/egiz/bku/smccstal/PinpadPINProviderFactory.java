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
import at.gv.egiz.stal.signedinfo.SignedInfoType;
import java.security.DigestException;

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
    protected ViewerThread viewerThread;
    protected SignedInfoType signedInfo;


    private SignaturePinProvider(SecureViewer viewer,
            SignedInfoType signedInfo) {
      this.viewer = viewer;
      this.signedInfo = signedInfo;
    }

    protected class ViewerThread extends Thread {

      PINSpec pinSpec;
      int retries;

      public ViewerThread(PINSpec pinSpec, int retries) {
        this.pinSpec = pinSpec;
        this.retries = retries;
      }

      @Override
      public void run() {

        try {

          gui.showPinpadSignaturePINDialog(pinSpec, retries,
              SignaturePinProvider.this, "secureViewer");

          while (true) {
            waitForAction();

            if ("secureViewer".equals(action)) {
              viewer.displayDataToBeSigned(signedInfo,
                      SignaturePinProvider.this, "pinEntry");
            } else if ("pinEntry".equals(action)) {
              gui.showPinpadSignaturePINDialog(pinSpec, retries,
                      SignaturePinProvider.this, "secureViewer");
            } else {
              log.error("unsupported action command: " + action);
            }
          }

        } catch (DigestException ex) {
          log.error("Bad digest value: " + ex.getMessage());
          gui.showErrorDialog(BKUGUIFacade.ERR_INVALID_HASH,
                  new Object[]{ex.getMessage()});
        } catch (InterruptedException ex) {
          log.info("pinpad secure viewer thread interrupted");
        } catch (Exception ex) {
          log.error("Could not display hashdata inputs: " +
                  ex.getMessage());
          gui.showErrorDialog(BKUGUIFacade.ERR_DISPLAY_HASHDATA,
                  new Object[]{ex.getMessage()});
        }
      }
    }

    @Override
    public char[] providePIN(PINSpec spec, int retries)
            throws CancelledException, InterruptedException {

      if (viewerThread != null) {
        updateViewerThread(retries);
      } else {
        viewerThread = new ViewerThread(spec, -1);
        viewerThread.start();
      }
//      if (viewerThread != null) {
//        log.trace("interrupt old secure viewer thread");
//        viewerThread.interrupt();
//      }
//      viewerThread = new ViewerThread(spec, (retry) ? retries : -1);
//      log.trace("start new secure viewer thread");
//      viewerThread.start();

      retry = true;
      return null;
    }

    private synchronized void updateViewerThread(int retries) {
      log.trace("update viewer thread");
      viewerThread.retries = retries;
      action = "pinEntry";
      actionPerformed = true;
      notify();
    }


//    @Override
//    protected void finalize() throws Throwable {
//      if (viewerThread != null) {
//        viewerThread.interrupt();
//      }
//      log.info("finalizing Pinpad SignaturePinProvider");
//      super.finalize();
//    }
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

