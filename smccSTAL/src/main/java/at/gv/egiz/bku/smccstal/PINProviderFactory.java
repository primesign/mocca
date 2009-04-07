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
import at.gv.egiz.smcc.ccid.CCID;
import at.gv.egiz.smcc.PINProvider;
import at.gv.egiz.smcc.PINSpec;
import at.gv.egiz.stal.signedinfo.SignedInfoType;
import java.security.DigestException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * don't reuse the instance if the card reader might have changed!
 * @author Clemens Orthacker <clemens.orthacker@iaik.tugraz.at>
 */
public class PINProviderFactory {

  protected static final Log log = LogFactory.getLog(PINProviderFactory.class);

  protected CCID reader;
  protected BKUGUIFacade gui;

  /**
   * don't reuse the instance if the card reader might have changed!
   * @param reader
   * @param gui
   */
  public PINProviderFactory(CCID reader, BKUGUIFacade gui) {
    log.trace("PINProviderFactory for " + reader.getName());
    this.reader = reader;
    this.gui = gui;
  }


  
//  public static PINProviderFactory getInstance(SignatureCard forCard,
//          BKUGUIFacade gui) {
//    if (forCard.getReader().hasFeature(CCID.FEATURE_VERIFY_PIN_DIRECT) ||
//            forCard.getReader().hasFeature(CCID.FEATURE_VERIFY_PIN_DIRECT)) {
//      return new PinpadPINProviderFactory(gui);
//    } else {
//      return new SoftwarePINProviderFactory(gui);
//    }
//  }

  /**
   * don't reuse the instance if the card reader might have changed!
   * @param reader
   * @param gui
   * @return
   */
//  public static PINProviderFactory getInstance(CCID reader, BKUGUIFacade gui) {
//    log.trace("PINProviderFactory for " + reader.getName());
//    return new PINProviderFactory(reader, gui);
//  }

  public PINProvider getSignaturePINProvider(SecureViewer viewer,
          SignedInfoType signedInfo) {
    if (reader.hasFeature(CCID.FEATURE_VERIFY_PIN_START) ||
            reader.hasFeature(CCID.FEATURE_VERIFY_PIN_DIRECT)) {
      log.debug("pinpad signature-pin provider");
      return new PinpadSignaturePinProvider(viewer, signedInfo);
    } else {
      log.debug("software signature-pin provider");
      return new SoftwareSignaturePinProvider(viewer, signedInfo);
    }
  }

  public PINProvider getCardPINProvider() {
    if (reader.hasFeature(CCID.FEATURE_VERIFY_PIN_START) ||
            reader.hasFeature(CCID.FEATURE_VERIFY_PIN_DIRECT)) {
      log.debug("pinpad card-pin provider");
      return new PinpadCardPinProvider();
    } else {
      log.debug("software card-pin provider");
      return new SoftwareCardPinProvider();
    }
  }

  class SoftwareSignaturePinProvider extends AbstractPINProvider {

    protected SecureViewer viewer;
    protected SignedInfoType signedInfo;

    private SoftwareSignaturePinProvider(SecureViewer viewer,
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
              this, "secureViewer");

      do {
        waitForAction();

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

  class SoftwareCardPinProvider extends AbstractPINProvider {

    private SoftwareCardPinProvider() {
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

    class PinpadSignaturePinProvider extends AbstractPINProvider {

//    protected BKUGUIFacade gui;
    protected SecureViewer viewer;
    protected ViewerThread viewerThread;
    protected SignedInfoType signedInfo;


    private PinpadSignaturePinProvider(SecureViewer viewer,
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
              PinpadSignaturePinProvider.this, "secureViewer");

          while (true) {
            waitForAction();

            if ("secureViewer".equals(action)) {
              viewer.displayDataToBeSigned(signedInfo,
                      PinpadSignaturePinProvider.this, "pinEntry");
            } else if ("pinEntry".equals(action)) {
              gui.showPinpadSignaturePINDialog(pinSpec, retries,
                      PinpadSignaturePinProvider.this, "secureViewer");
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

  class PinpadCardPinProvider extends AbstractPINProvider {

    private PinpadCardPinProvider() {
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
