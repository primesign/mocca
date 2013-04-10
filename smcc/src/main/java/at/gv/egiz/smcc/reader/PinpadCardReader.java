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


package at.gv.egiz.smcc.reader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.ResponseAPDU;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.gv.egiz.smcc.CancelledException;
import at.gv.egiz.smcc.ChangeReferenceDataAPDUSpec;
import at.gv.egiz.smcc.NewReferenceDataAPDUSpec;
import at.gv.egiz.smcc.PINConfirmationException;
import at.gv.egiz.smcc.PINFormatException;
import at.gv.egiz.smcc.PINOperationAbortedException;
import at.gv.egiz.smcc.PinInfo;
import at.gv.egiz.smcc.ResetRetryCounterAPDUSpec;
import at.gv.egiz.smcc.SignatureCardException;
import at.gv.egiz.smcc.TimeoutException;
import at.gv.egiz.smcc.VerifyAPDUSpec;
import at.gv.egiz.smcc.pin.gui.ModifyPINGUI;
import at.gv.egiz.smcc.pin.gui.PINGUI;
import at.gv.egiz.smcc.util.SMCCHelper;

/**
 *
 * @author Clemens Orthacker <clemens.orthacker@iaik.tugraz.at>
 */
public class PinpadCardReader extends DefaultCardReader {

  public static final int PIN_ENTRY_POLLING_INTERVAL = 10;

  private final Logger log = LoggerFactory.getLogger(PinpadCardReader.class);

  protected byte bEntryValidationCondition = 0x02;  // validation key pressed
  protected byte bTimeOut = 0x3c;                   // 60sec (= max on ReinerSCT)
  protected byte bTimeOut2 = 0x00;                  // default (attention with SCM)
  protected byte wPINMaxExtraDigitMin = 0x00;         // min pin length zero digits
  protected byte wPINMaxExtraDigitMax = 0x0c;         // max pin length 12 digits
  protected byte bNumberMessage = 0x01;
  
  /**
   * supported features and respective control codes
   */
  protected Map<Byte, Integer> features;
  protected boolean VERIFY, MODIFY, VERIFY_DIRECT, MODIFY_DIRECT;

  public PinpadCardReader(CardTerminal ct, Map<Byte, Integer> features) {
    super(ct);
    if (features == null) {
      throw new NullPointerException("Pinpad card reader does not support any features");
    }
    this.features = features;

    if (features.containsKey(FEATURE_VERIFY_PIN_START) &&
        features.containsKey(FEATURE_GET_KEY_PRESSED) &&
        features.containsKey(FEATURE_VERIFY_PIN_FINISH)) {
      VERIFY = true;
    }
    if (features.containsKey(FEATURE_MODIFY_PIN_START) &&
        features.containsKey(FEATURE_GET_KEY_PRESSED) &&
        features.containsKey(FEATURE_MODIFY_PIN_FINISH)) {
      MODIFY = true;
    }
    if (features.containsKey(FEATURE_VERIFY_PIN_DIRECT)) {
      VERIFY_DIRECT = true;
    }
    if (features.containsKey(FEATURE_MODIFY_PIN_DIRECT)) {
      MODIFY_DIRECT = true;
    }

    if (name != null) {
      name = name.toLowerCase();
      //ReinerSCT: http://support.reiner-sct.de/downloads/LINUX
      //           http://www.linux-club.de/viewtopic.php?f=61&t=101287&start=0
      //old: REINER SCT CyberJack 00 00
      //new (CCID): 0C4B/0300 Reiner-SCT cyberJack pinpad(a) 00 00
      //Snow Leopard: Reiner-SCT cyberJack pinpad(a) 00 00
      //display: REINER SCT CyberJack 00 00
      if(name.startsWith("gemplus gempc pinpad") || name.startsWith("gemalto gempc pinpad")) {
          // win7(microsoft driver) GemPlus USB GemPC Pinpad Smartcardreader 0 -> no pinpad
          // win7(gemalto4.0.7.5) Gemalto GemPC Pinpad USB Smart Card Read 0 -> transmitControlCommand failed (0x7a)
          //     (same with timeouts set to 0000 and 3c0f)
          // winXP (verify failed, sw=d2(ecard) sw=92(acos), cf. wiki):
          // winXP (without setting wPINMax: sw=6b:80)
          // linux (ok): Gemplus GemPC Pinpad 00 00
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
          log.trace("Disabling direct pin entry as workaround for Windows");
          VERIFY_DIRECT = false;
          MODIFY_DIRECT = false;
        }
        log.trace("Setting custom wPINMaxExtraDigitH (0x04) for {}.", name);
        wPINMaxExtraDigitMin = 0x04;
        log.trace("Setting custom wPINMaxExtraDigitL (0x08) for {}.", name);
        wPINMaxExtraDigitMax = 0x08;
      } else if (name.startsWith("omnikey cardman 3621")) {
        log.trace("Setting custom wPINMaxExtraDigitH (0x01) for {}.", name);
        wPINMaxExtraDigitMin = 0x01;
      } else if (name.startsWith("scm spr 532") || name.startsWith("scm microsystems inc. sprx32 usb smart card reader")) {
        log.trace("Setting custom bTimeOut (0x3c) for {}.", name);
        bTimeOut = 0x3c;
        log.trace("Setting custom bTimeOut2 (0x0f) for {}.", name);
        bTimeOut2 = 0x0f;
      } else if (name.startsWith("cherry smartboard xx44")) {
        log.trace("Setting custom wPINMaxExtraDigitH (0x01) for {}.", name);
        wPINMaxExtraDigitMin = 0x01;
      } else if (name.startsWith("cherry gmbh smartterminal st-2xxx")) {
        // Win: Cherry GmbH SmartTerminal ST-2xxx 0
        // Linux(?): Cherry SmartTerminal ST-2XXX (21121010102014) 00 00
        log.trace("Setting custom bTimeOut (0x3c) for {}.", name);
        bTimeOut = 0x3c;
        log.trace("Setting custom bTimeOut2 (0x0f) for {}.", name);
        bTimeOut2 = 0x0f;
      }
      //TODO Kobil KAAN Advanced seems to have an issue,
      //cf. http://www.buergerkarte.at/mvnforum/mvnforum/viewthread?thread=255
      //CHANGE REFERENCE DATA failed. SW=6a80 on activate STARCOS card
    }

  }

  @Override
  public boolean hasFeature(Byte feature) {
    return features.containsKey(feature);
  }
  
  private void VERIFY_PIN_START(Card icc, byte[] PIN_VERIFY) throws CardException {
    int ioctl = features.get(FEATURE_VERIFY_PIN_START);
    if (log.isTraceEnabled()) {
      log.trace("VERIFY_PIN_START ({}) {}", Integer.toHexString(ioctl),
          SMCCHelper.toString(PIN_VERIFY));
    }
    byte[] resp = icc.transmitControlCommand(ioctl, PIN_VERIFY);
    if (resp != null && resp.length > 0) {
      if (resp[0] == (byte) 0x57) {
        log.error("Invalid parameter in PIN_VERIFY structure.");
        throw new CardException("ERROR_INVALID_PARAMETER");
      } else {
        log.error("Unexpected response to VERIFY_PIN_START: {}.", SMCCHelper
            .toString(resp));
        throw new CardException("unexpected response to VERIFY_PIN_START: " +
                SMCCHelper.toString(resp));
      }
    }
  }

  private byte GET_KEY_PRESSED(Card icc) throws CardException {
    int ioctl = features.get(FEATURE_GET_KEY_PRESSED);
    byte[] resp = icc.transmitControlCommand(ioctl, new byte[0]);
    if (resp != null && resp.length == 1) {
//      if (log.isTraceEnabled()) {
//        log.trace("response " + SMCCHelper.toString(resp));
//      }
      return resp[0];
    }
    log.error("Unexpected response to GET_KEY_PRESSED: {}.",
            SMCCHelper.toString(resp));
    throw new CardException("unexpected response to GET_KEY_PRESSED: " +
            SMCCHelper.toString(resp));
  }

  private byte[] VERIFY_PIN_FINISH(Card icc) throws CardException {
    int ioctl = features.get(FEATURE_VERIFY_PIN_FINISH);
    if (log.isTraceEnabled()) {
      log.trace("VERIFY_PIN_FINISH ({})", Integer.toHexString(ioctl));
    }
    byte[] resp = icc.transmitControlCommand(ioctl, new byte[0]);
    if (resp != null && resp.length == 2) {
      if (log.isTraceEnabled()) {
        log.trace("response {}", SMCCHelper.toString(resp));
      }
      return resp;
    }
    log.error("Unexpected response to VERIFY_PIN_FINISH: {}.",
            SMCCHelper.toString(resp));
    throw new CardException("unexpected response to VERIFY_PIN_FINISH: " +
            SMCCHelper.toString(resp));
  }

  private void MODIFY_PIN_START(Card icc, byte[] PIN_MODIFY) throws CardException {
    int ioctl = features.get(FEATURE_MODIFY_PIN_START);
    if (log.isTraceEnabled()) {
      log.trace("MODFIY_PIN_START (" + Integer.toHexString(ioctl) +
              ")  " + SMCCHelper.toString(PIN_MODIFY));
    }
    byte[] resp = icc.transmitControlCommand(ioctl, PIN_MODIFY);
    if (resp != null && resp.length > 0) {
      if (resp[0] == (byte) 0x57) {
        log.error("Invalid parameter in PIN_MODIFY structure.");
        throw new CardException("ERROR_INVALID_PARAMETER");
      } else {
        log.error("Unexpected response to MODIFY_PIN_START: {}.",
                SMCCHelper.toString(resp));
        throw new CardException("unexpected response to MODIFY_PIN_START: " +
                SMCCHelper.toString(resp));
      }
    }
  }

  private byte[] MODIFY_PIN_FINISH(Card icc) throws CardException {
    int ioctl = features.get(FEATURE_MODIFY_PIN_FINISH);
    if (log.isTraceEnabled()) {
      log.trace("MODIFY_PIN_FINISH ({})", Integer.toHexString(ioctl));
    }
    byte[] resp = icc.transmitControlCommand(ioctl, new byte[0]);
    if (resp != null && resp.length == 2) {
      if (log.isTraceEnabled()) {
        log.trace("response {}", SMCCHelper.toString(resp));
      }
      return resp;
    }
    log.error("Unexpected response to MODIFY_PIN_FINISH: {}",
            SMCCHelper.toString(resp));
    throw new CardException("unexpected response to MODIFY_PIN_FINISH: " +
            SMCCHelper.toString(resp));
  }

  private byte[] VERIFY_PIN_DIRECT(Card icc, byte[] PIN_VERIFY) throws CardException {
    int ioctl = features.get(FEATURE_VERIFY_PIN_DIRECT);
    if (log.isTraceEnabled()) {
      log.trace("VERIFY_PIN_DIRECT ({}) {}", Integer.toHexString(ioctl),
          SMCCHelper.toString(PIN_VERIFY));
    }
    byte[] resp = icc.transmitControlCommand(ioctl, PIN_VERIFY);
    if (log.isTraceEnabled()) {
      log.trace("response {}", SMCCHelper.toString(resp));
    }
    return resp;
  }

  private byte[] verifyPin(Card icc, byte[] PIN_VERIFY, PINGUI pinGUI) 
          throws SignatureCardException, CardException, InterruptedException {

//    pinGUI.enterPIN(pinSpec, retries);

    log.debug("VERIFY_PIN_START [{}]", FEATURES[FEATURE_VERIFY_PIN_START]);
    VERIFY_PIN_START(icc, PIN_VERIFY);

    byte resp;
    do {
      resp = GET_KEY_PRESSED(icc);
      if (resp == (byte) 0x00) {
        synchronized(this) {
          try {
            wait(PIN_ENTRY_POLLING_INTERVAL);
          } catch (InterruptedException ex) {
            log.error("interrupted in VERIFY_PIN");
          }
        }
      } else if (resp == (byte) 0x0d) {
        log.trace("GET_KEY_PRESSED: 0x0d (user confirmed)");
        break;
      } else if (resp == (byte) 0x2b) {
        log.trace("GET_KEY_PRESSED: 0x2b (user entered valid key 0-9)");
        pinGUI.validKeyPressed();
      } else if (resp == (byte) 0x1b) {
        log.trace("GET_KEY_PRESSED: 0x1b (user cancelled VERIFY_PIN via cancel button)");
        break; // returns 0x6401
      } else if (resp == (byte) 0x08) {
        log.trace("GET_KEY_PRESSED: 0x08 (user pressed correction/backspace button)");
        pinGUI.correctionButtonPressed();
      } else if (resp == (byte) 0x0e) {
        log.trace("GET_KEY_PRESSED: 0x0e (timeout occured)");
        break; // return 0x6400
      } else if (resp == (byte) 0x40) {
        log.trace("GET_KEY_PRESSED: 0x40 (PIN_Operation_Aborted)");
        throw new PINOperationAbortedException("PIN_Operation_Aborted (0x40)");
      } else if (resp == (byte) 0x0a) {
        log.trace("GET_KEY_PRESSED: 0x0a (all keys cleared");
        pinGUI.allKeysCleared();
      } else {
        log.error("Unexpected response to GET_KEY_PRESSED: {}.", Integer
            .toHexString(resp));
        throw new CardException("unexpected response to GET_KEY_PRESSED: " +
            Integer.toHexString(resp));
      }
    } while (true); 

    return VERIFY_PIN_FINISH(icc);
  }

  /**
   * does not display the first pin dialog (enterCurrentPIN or enterNewPIN, depends on bConfirmPIN),
   * since this is easier to do in calling modify()
   */
  private byte[] modifyPin(Card icc, byte[] PIN_MODIFY, ModifyPINGUI pinGUI, PinInfo pINSpec)
          throws PINOperationAbortedException, CardException {

    byte pinConfirmations = (byte) 0x00; //b0: new pin not entered (0) / entered (1)
                                         //b1: current pin not entered (0) / entered (1)
    byte bConfirmPIN = PIN_MODIFY[9];
    
//    if ((bConfirmPIN & (byte) 0x02) == 0) {
//      log.debug("no current PIN entry requested");
//      pinGUI.enterNewPIN(pINSpec);
//    } else {
//      log.debug("current PIN entry requested");
//      pinGUI.enterCurrentPIN(pINSpec, retries);
//    }

    log.debug("MODIFY_PIN_START [{}]", FEATURES[FEATURE_MODIFY_PIN_START]);
    MODIFY_PIN_START(icc, PIN_MODIFY);

    byte resp;
    while (true) {
      resp = GET_KEY_PRESSED(icc);
      if (resp == (byte) 0x00) {
        synchronized(this) {
          try {
            wait(PIN_ENTRY_POLLING_INTERVAL);
          } catch (InterruptedException ex) {
            log.error("Interrupted in MODIFY_PIN");
          }
        }
      } else if (resp == (byte) 0x0d) {
        if (log.isTraceEnabled()) {
          log.trace("requested pin confirmations: 0b{}", Integer.toBinaryString(bConfirmPIN & 0xff));
          log.trace("performed pin confirmations: 0b{}", Integer.toBinaryString(pinConfirmations & 0xff));
        }
        log.debug("GET_KEY_PRESSED: 0x0d (user confirmed)");
        if (pinConfirmations == bConfirmPIN) {
          break;
        } else if ((bConfirmPIN & (byte) 0x02) == 0 ||
            (pinConfirmations & (byte) 0x02) == (byte) 0x02) {
          // no current pin entry or current pin entry already performed
          if ((pinConfirmations & (byte) 0x01) == 0) {
            // new pin
            pinConfirmations |= (byte) 0x01;
            pinGUI.confirmNewPIN(pINSpec);
          } // else: new pin confirmed
        } else {
          // current pin entry
          pinConfirmations |= (byte) 0x02;
          pinGUI.enterNewPIN(pINSpec);
        }
      } else if (resp == (byte) 0x2b) {
        log.trace("GET_KEY_PRESSED: 0x2b (user entered valid key 0-9)");
        pinGUI.validKeyPressed();
      } else if (resp == (byte) 0x1b) {
        log.trace("GET_KEY_PRESSED: 0x1b (user cancelled VERIFY_PIN via cancel button)");
        break; // returns 0x6401
      } else if (resp == (byte) 0x08) {
        log.trace("GET_KEY_PRESSED: 0x08 (user pressed correction/backspace button)");
        pinGUI.correctionButtonPressed();
      } else if (resp == (byte) 0x0e) {
        log.trace("GET_KEY_PRESSED: 0x0e (timeout occured)");
        break; // return 0x6400
      } else if (resp == (byte) 0x40) {
        log.trace("GET_KEY_PRESSED: 0x40 (PIN_Operation_Aborted)");
        throw new PINOperationAbortedException("PIN_Operation_Aborted (0x40)");
      } else if (resp == (byte) 0x0a) {
        log.trace("GET_KEY_PRESSED: 0x0a (all keys cleared");
        pinGUI.allKeysCleared();
      } else {
        log.error("Unexpected response to GET_KEY_PRESSED: {}.", Integer
            .toHexString(resp));
        throw new CardException("unexpected response to GET_KEY_PRESSED: " +
            Integer.toHexString(resp));
      }

    }

    pinGUI.finish();
    return MODIFY_PIN_FINISH(icc);
  }

  private byte[] MODIFY_PIN_DIRECT(Card icc, byte[] PIN_MODIFY) throws CardException {
    int ioctl = features.get(FEATURE_MODIFY_PIN_DIRECT);
    if (log.isTraceEnabled()) {
      log.trace("MODIFY_PIN_DIRECT ({}) {}", Integer.toHexString(ioctl),
          SMCCHelper.toString(PIN_MODIFY));
    }
    byte[] resp = icc.transmitControlCommand(ioctl, PIN_MODIFY);
    if (log.isTraceEnabled()) {
      log.trace("response {}", SMCCHelper.toString(resp));
    }
    return resp;
  }
  
  protected byte[] createPINModifyStructure(NewReferenceDataAPDUSpec apduSpec, PinInfo pinSpec) {

    ByteArrayOutputStream s = new ByteArrayOutputStream();
    // bTimeOut
    s.write(bTimeOut);
    // bTimeOut2
    s.write(bTimeOut2);
    // bmFormatString
    s.write(1 << 7 // system unit = byte
        | (0xF & apduSpec.getPinPosition()) << 3
        | (0x1 & apduSpec.getPinJustification() << 2)
        | (0x3 & apduSpec.getPinFormat()));
    // bmPINBlockString
    s.write((0xF & apduSpec.getPinLengthSize()) << 4
        | (0xF & apduSpec.getPinLength()));
    // bmPINLengthFormat
    s.write(// system unit = bit
        (0xF & apduSpec.getPinLengthPos()));
    // bInsertionOffsetOld
    s.write(0x00);
    // bInsertionOffsetNew
    s.write(apduSpec.getPinInsertionOffsetNew());
    // wPINMaxExtraDigit (little endian)
    s.write(Math.min(pinSpec.getMaxLength(), wPINMaxExtraDigitMax));
    s.write(Math.max(pinSpec.getMinLength(), wPINMaxExtraDigitMin));
    // bConfirmPIN
    s.write(0x01);
    // bEntryValidationCondition
    s.write(bEntryValidationCondition);
    // bNumberMessage
    s.write(0x02);
    // wLangId (little endian)
    // English (United States), see http://www.usb.org/developers/docs/USB_LANGIDs.pdf
    s.write(0x09);
    s.write(0x04);
    // bMsgIndex1
    s.write(0x01);
    // bMsgIndex2
    s.write(0x02);
    // bMsgIndex3
    s.write(0x00);

    // bTeoPrologue
    s.write(0x00);
    s.write(0x00);
    s.write(0x00);
    // ulDataLength
    s.write(apduSpec.getApdu().length);
    s.write(0x00);
    s.write(0x00);
    s.write(0x00);
    // abData
    try {
      s.write(apduSpec.getApdu());
    } catch (IOException e) {
      // As we are dealing with ByteArrayOutputStreams no exception is to be
      // expected.
      throw new RuntimeException(e);
    }

    return s.toByteArray();

  }
  
  protected byte[] createPINModifyStructure(ChangeReferenceDataAPDUSpec apduSpec, PinInfo pinSpec) {
    //TODO bInsertionOffsetOld (0x00), bConfirmPIN (0x01), bNumberMessage (0x02), bMsgIndex1/2/3

    ByteArrayOutputStream s = new ByteArrayOutputStream();
    // bTimeOut
    s.write(bTimeOut);
    // bTimeOut2
    s.write(bTimeOut2);
    // bmFormatString
    s.write(1 << 7 // system unit = byte
        | (0xF & apduSpec.getPinPosition()) << 3
        | (0x1 & apduSpec.getPinJustification() << 2)
        | (0x3 & apduSpec.getPinFormat()));
    // bmPINBlockString
    s.write((0xF & apduSpec.getPinLengthSize()) << 4
        | (0xF & apduSpec.getPinLength()));
    // bmPINLengthFormat
    s.write(// system unit = bit
        (0xF & apduSpec.getPinLengthPos()));
    // bInsertionOffsetOld (0x00 for no old pin?)
    s.write(apduSpec.getPinInsertionOffsetOld());
    // bInsertionOffsetNew
    s.write(apduSpec.getPinInsertionOffsetNew());
    // wPINMaxExtraDigit
    s.write(Math.min(pinSpec.getMaxLength(), wPINMaxExtraDigitMax));
    s.write(Math.max(pinSpec.getMinLength(), wPINMaxExtraDigitMin));
    // bConfirmPIN
    s.write(0x03);
    // bEntryValidationCondition
    s.write(bEntryValidationCondition);
    // bNumberMessage
    s.write(0x03);
    // wLangId English (United States), see http://www.usb.org/developers/docs/USB_LANGIDs.pdf
    s.write(0x09);
    s.write(0x04);
    // bMsgIndex1
    s.write(0x00);
    // bMsgIndex2
    s.write(0x01);
    // bMsgIndex3
    s.write(0x02);
    
    // bTeoPrologue
    s.write(0x00);
    s.write(0x00);
    s.write(0x00);
    // ulDataLength
    s.write(apduSpec.getApdu().length);
    s.write(0x00);
    s.write(0x00);
    s.write(0x00);
    // abData
    try {
      s.write(apduSpec.getApdu());
    } catch (IOException e) {
      // As we are dealing with ByteArrayOutputStreams no exception is to be
      // expected.
      throw new RuntimeException(e);
    }
    
    return s.toByteArray();

  }
  
  protected byte[] createPINVerifyStructure(VerifyAPDUSpec apduSpec, PinInfo pinSpec) {
    
    ByteArrayOutputStream s = new ByteArrayOutputStream();
    // bTimeOut
    s.write(bTimeOut);
    // bTimeOut2
    s.write(bTimeOut2);
    // bmFormatString
    s.write(1 << 7 // system unit = byte
        | (0xF & apduSpec.getPinPosition()) << 3
        | (0x1 & apduSpec.getPinJustification() << 2)
        | (0x3 & apduSpec.getPinFormat()));
    // bmPINBlockString
    s.write((0xF & apduSpec.getPinLengthSize()) << 4
        | (0xF & apduSpec.getPinLength()));
    // bmPINLengthFormat
    s.write(// system unit = byte
        (0xF & apduSpec.getPinLengthPos()));
    // wPINMaxExtraDigit (little endian)
    s.write(Math.min(pinSpec.getMaxLength(), wPINMaxExtraDigitMax)); // max PIN length
    s.write(Math.max(pinSpec.getMinLength(), wPINMaxExtraDigitMin)); // min PIN length
    // bEntryValidationCondition
    s.write(bEntryValidationCondition);
    // bNumberMessage
    s.write(bNumberMessage);
    // wLangId (little endian)
    s.write(0x09);
    s.write(0x04);
    // bMsgIndex
    s.write(0x00);
    // bTeoPrologue
    s.write(0x00);
    s.write(0x00);
    s.write(0x00);
    // ulDataLength
    s.write(apduSpec.getApdu().length);
    s.write(0x00);
    s.write(0x00);
    s.write(0x00);
    // abData
    try {
      s.write(apduSpec.getApdu());
    } catch (IOException e) {
      // As we are dealing with ByteArrayOutputStreams no exception is to be
      // expected.
      throw new RuntimeException(e);
    }
    
    return s.toByteArray();
    
  }

  @Override
  public ResponseAPDU verify(CardChannel channel, VerifyAPDUSpec apduSpec,
          PINGUI pinGUI, PinInfo pinSpec, int retries)
        throws SignatureCardException, CardException, InterruptedException {

    ResponseAPDU resp = null;

    byte[] s = createPINVerifyStructure(apduSpec, pinSpec);
    Card icc = channel.getCard();
    boolean regain;

    if (VERIFY) {
      regain = dropExclusive(icc);
      pinGUI.enterPIN(pinSpec, retries);
      resp = new ResponseAPDU(verifyPin(icc, s, pinGUI));
    } else if (VERIFY_DIRECT) {
      regain = dropExclusive(icc);
      pinGUI.enterPINDirect(pinSpec, retries);
      log.debug("VERIFY_PIN_DIRECT [{}]", FEATURES[FEATURE_VERIFY_PIN_DIRECT]);
      resp = new ResponseAPDU(VERIFY_PIN_DIRECT(icc, s));
    } else {
      log.warn("Falling back to default pin-entry.");
      return super.verify(channel, apduSpec, pinGUI, pinSpec, retries);
    }
    regainExclusive(icc, regain);

    switch (resp.getSW()) {
      case 0x6400:
        log.debug("SPE operation timed out.");
        throw new TimeoutException();
      case 0x6401:
        log.debug("SPE operation was cancelled by the 'Cancel' button.");
        throw new CancelledException();
      case 0x6403:
        log.debug("User entered too short or too long PIN "
            + "regarding MIN/MAX PIN length.");
        throw new PINFormatException();
      case 0x6480:
        log.debug("SPE operation was aborted by the 'Cancel' operation "
            + "at the host system.");
      case 0x6b80:
        log.info("Invalid parameter in passed structure.");
      default:
        return resp;
    }
  }

  @Override
  public ResponseAPDU modify(CardChannel channel, ChangeReferenceDataAPDUSpec apduSpec,
          ModifyPINGUI pinGUI, PinInfo pinSpec, int retries)
        throws SignatureCardException, CardException, InterruptedException {
    
    ResponseAPDU resp = null;

    byte[] s = createPINModifyStructure(apduSpec, pinSpec);
    Card icc = channel.getCard();
    boolean regain;

    if (MODIFY) {
      regain = dropExclusive(icc);
      pinGUI.enterCurrentPIN(pinSpec, retries);
      resp = new ResponseAPDU(modifyPin(icc, s, pinGUI, pinSpec));
    } else if (MODIFY_DIRECT) {
      regain = dropExclusive(icc);
      pinGUI.modifyPINDirect(pinSpec, retries);
      log.debug("MODIFY_PIN_DIRECT [{}]", FEATURES[FEATURE_MODIFY_PIN_DIRECT]);
      resp = new ResponseAPDU(MODIFY_PIN_DIRECT(icc, s));
    } else {
      log.warn("Falling back to default pin-entry.");
      return super.modify(channel, apduSpec, pinGUI, pinSpec, retries);
    }
    regainExclusive(icc, regain);

    switch (resp.getSW()) {
      case 0x6400:
        log.debug("SPE operation timed out.");
        throw new TimeoutException();
      case 0x6401:
        log.debug("SPE operation was cancelled by the 'Cancel' button.");
        throw new CancelledException();
      case 0x6402:
        log.debug("Modify PIN operation failed because two 'new PIN' " +
                "entries do not match");
        throw new PINConfirmationException();
      case 0x6403:
        log.debug("User entered too short or too long PIN "
              + "regarding MIN/MAX PIN length.");
        throw new PINFormatException();
      case 0x6480:
        log.debug("SPE operation was aborted by the 'Cancel' operation "
            + "at the host system.");
      case 0x6b80:
        log.info("Invalid parameter in passed structure.");
      default:
        return resp;
    }
  }

  @Override
  public ResponseAPDU modify(CardChannel channel, NewReferenceDataAPDUSpec apduSpec,
          ModifyPINGUI pinGUI, PinInfo pinSpec)
        throws SignatureCardException, CardException, InterruptedException {

    ResponseAPDU resp = null;

    byte[] s = createPINModifyStructure(apduSpec, pinSpec);
    Card icc = channel.getCard();
    boolean regain;

    if (MODIFY) {
      regain = dropExclusive(icc);
      pinGUI.enterNewPIN(pinSpec);
      resp = new ResponseAPDU(modifyPin(icc, s, pinGUI, pinSpec));
    } else if (MODIFY_DIRECT) {
      regain = dropExclusive(icc);
      pinGUI.modifyPINDirect(pinSpec, -1);
      log.debug("MODIFY_PIN_DIRECT [{}]", FEATURES[FEATURE_MODIFY_PIN_DIRECT]);
      resp = new ResponseAPDU(MODIFY_PIN_DIRECT(icc, s));
    } else {
      log.warn("Falling back to default pin-entry.");
      return super.modify(channel, apduSpec, pinGUI, pinSpec);
    }
    regainExclusive(icc, regain);

    switch (resp.getSW()) {
      case 0x6400:
        log.debug("SPE operation timed out.");
        throw new TimeoutException();
      case 0x6401:
        log.debug("SPE operation was cancelled by the 'Cancel' button.");
        throw new CancelledException();
      case 0x6402:
        log.debug("Modify PIN operation failed because two 'new PIN' " +
                "entries do not match");
        throw new PINConfirmationException();
      case 0x6403:
        log.debug("User entered too short or too long PIN "
              + "regarding MIN/MAX PIN length.");
        throw new PINFormatException();
      case 0x6480:
        log.debug("SPE operation was aborted by the 'Cancel' operation "
            + "at the host system.");
      case 0x6b80:
        log.info("Invalid parameter in passed structure.");
      default:
        return resp;
    }
  }

  @Override
  public ResponseAPDU modify(CardChannel channel, ResetRetryCounterAPDUSpec apduSpec,
          ModifyPINGUI pinGUI, PinInfo pinSpec, int retries)
          throws InterruptedException, CardException, SignatureCardException {
    //TODO
    return modify(channel, (ChangeReferenceDataAPDUSpec) apduSpec, pinGUI, pinSpec, retries);
  }

  private boolean dropExclusive(Card card) throws CardException {
    if (SMCCHelper.isWindows8()) {
      log.debug("Win8 - giving up exclusive acess");
      try {
        card.endExclusive();
      } catch (IllegalStateException e) {
        log.debug("Didn't have exclusive access");
        return false;
      }
    }
    return true;
  }

  private void regainExclusive(Card card, boolean doRegainExclusive) throws CardException {
    if (SMCCHelper.isWindows8() && doRegainExclusive) {
      log.debug("Win8 - trying to regain exclusive acess");
      card.beginExclusive();
    }
  }
}
