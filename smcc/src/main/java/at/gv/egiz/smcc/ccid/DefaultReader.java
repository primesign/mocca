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
package at.gv.egiz.smcc.ccid;

import at.gv.egiz.smcc.*;
import at.gv.egiz.smcc.util.SMCCHelper;
import java.util.HashMap;
import java.util.Map;
import javax.smartcardio.Card;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author Clemens Orthacker <clemens.orthacker@iaik.tugraz.at>
 */
public class DefaultReader implements CCID {
  public static final int PIN_ENTRY_POLLING_INTERVAL = 20;

  public static final byte bEntryValidationCondition = 0x02;  // validation key pressed
  public static final byte bTimeOut = 0x3c;                   // 60sec (= max on ReinerSCT)
  public static final byte bTimeOut2 = 0x00;                  // default (attention with SCM)
  public static final byte wPINMaxExtraDigitH = 0x00;         // min pin length zero digits
  public static final byte wPINMaxExtraDigitL = 0x0c;         // max pin length 12 digits
  
  protected final static Log log = LogFactory.getLog(DefaultReader.class);

  private static int CTL_CODE(int code) {
    String os_name = System.getProperty("os.name").toLowerCase();
    if (os_name.indexOf("windows") > -1) {
      // cf. WinIOCTL.h
      return (0x31 << 16 | (code) << 2);
    }
    // cf. reader.h
    return 0x42000000 + (code);
  }

  int IOCTL_GET_FEATURE_REQUEST = CTL_CODE(3400);

  protected Card icc;
  protected CardTerminal ct;
  protected boolean disablePinpad = false;

  /**
   * supported features and respective control codes
   */
  protected Map<Byte, Integer> features;

  public DefaultReader(Card icc, CardTerminal ct) {
    if (icc == null || ct == null) {
      throw new NullPointerException("no card or card terminal provided");
    }
    log.info("Initializing " + ct.getName());

    this.icc = icc;
    this.ct = ct;
    features = queryFeatures();

    log.debug("setting pin timeout: " + getbTimeOut());
    log.debug("setting pin timeout (after key pressed): " + getbTimeOut2());
    log.debug("setting pin entry validation condition: " + getbEntryValidationCondition());
    log.debug("setting min pin length: " + getwPINMaxExtraDigitH());
    log.debug("setting max pin length: " + getwPINMaxExtraDigitL());
  }

  @Override
  public void setDisablePinpad(boolean disable) {
    disablePinpad = disable;
  }
  
  /**
   *
   * @return the card terminals name
   */
  @Override
  public String getName() {
    return ct.getName();
  }

  @Override
  public Card connect() throws CardException { 
    icc = ct.connect("*");
    return icc;
  }

  Map<Byte, Integer> queryFeatures() {
    Map<Byte, Integer> features = new HashMap<Byte, Integer>();

    if (icc == null) {
      log.warn("invalid card handle, cannot query ifd features");
    } else {
      try {
        if (log.isTraceEnabled()) {
          log.trace("GET_FEATURE_REQUEST " +
                  Integer.toHexString(IOCTL_GET_FEATURE_REQUEST) +
                  " on " + ct.getName());
        }
        byte[] resp = icc.transmitControlCommand(IOCTL_GET_FEATURE_REQUEST,
                new byte[0]);

        if (log.isTraceEnabled()) {
          log.trace("Response TLV " + SMCCHelper.toString(resp));
        }
        // tag
        // length in bytes (always 4)
        // control code value for supported feature (in big endian)
        for (int i = 0; i < resp.length; i += 6) {
          Byte feature = new Byte(resp[i]);
          Integer ioctl = new Integer((0xff & resp[i + 2]) << 24) |
                  ((0xff & resp[i + 3]) << 16) |
                  ((0xff & resp[i + 4]) << 8) |
                  (0xff & resp[i + 5]);
          if (log.isInfoEnabled()) {
            log.info("CCID supports " + FEATURES[feature.intValue()] +
                    ": " + Integer.toHexString(ioctl.intValue()));
          }
          features.put(feature, ioctl);
        }
      } catch (CardException ex) {
        log.debug("Failed to query CCID features: " + ex.getMessage());
        log.trace(ex);
        log.info("CCID does not support PINPad");
      }
    }
    return features;
  }

  @Override
  public boolean hasFeature(Byte feature) {
    if (features != null && !disablePinpad) {
      return features.containsKey(feature);
    }
    return false;
  }

  @Override
  public byte getbTimeOut() {
    return bTimeOut;    
  }

  @Override
  public byte getbTimeOut2() {
    return bTimeOut2;    
  }

  @Override
  public byte getwPINMaxExtraDigitL() {
    return wPINMaxExtraDigitL;
  }

  @Override
  public byte getwPINMaxExtraDigitH() {
    return wPINMaxExtraDigitH;
  }

  @Override
  public byte getbEntryValidationCondition() {
    return bEntryValidationCondition;
  }

  void verifyPinStart(byte[] PIN_VERIFY) throws CardException {
    if (!features.containsKey(FEATURE_VERIFY_PIN_START)) {
      throw new CardException("FEATURE_VERIFY_PIN_START not supported");
    }
    int ioctl = features.get(FEATURE_VERIFY_PIN_START);
    if (log.isTraceEnabled()) {
      log.trace("VERIFY_PIN_START (" + Integer.toHexString(ioctl) +
              ")  " + SMCCHelper.toString(PIN_VERIFY));
    }
    byte[] resp = icc.transmitControlCommand(ioctl, PIN_VERIFY);
    if (resp != null && resp.length > 0) {
      if (resp[0] == (byte) 0x57) {
        log.error("Invalid parameter in PIN_VERIFY structure");
        throw new CardException("ERROR_INVALID_PARAMETER");
      } else {
        log.error("unexpected response to VERIFY_PIN_START: " +
                SMCCHelper.toString(resp));
        throw new CardException("unexpected response to VERIFY_PIN_START: " +
                SMCCHelper.toString(resp));
      }
    }
  }

  byte[] verifyPinFinish() throws CardException {
    if (!features.containsKey(FEATURE_VERIFY_PIN_FINISH)) {
      throw new CardException("FEATURE_VERIFY_FINISH_FINISH not supported");
    }
    int ioctl = features.get(FEATURE_VERIFY_PIN_FINISH);
    if (log.isTraceEnabled()) {
      log.trace("VERIFY_PIN_FINISH (" + Integer.toHexString(ioctl) + ")");
    }
    byte[] resp = icc.transmitControlCommand(ioctl, new byte[0]);
    if (resp != null && resp.length == 2) {
      if (log.isTraceEnabled()) {
        log.trace("response " + SMCCHelper.toString(resp));
      }
      return resp;
    }
    log.error("unexpected response to VERIFY_PIN_FINISH: " +
            SMCCHelper.toString(resp));
    throw new CardException("unexpected response to VERIFY_PIN_FINISH: " +
            SMCCHelper.toString(resp));
  }

  void modifyPinStart(byte[] PIN_MODIFY) throws CardException {
    if (!features.containsKey(FEATURE_MODIFY_PIN_START)) {
      throw new CardException("FEATURE_MODIFY_PIN_START not supported");
    }
    int ioctl = features.get(FEATURE_MODIFY_PIN_START);
    if (log.isTraceEnabled()) {
      log.trace("MODFIY_PIN_START (" + Integer.toHexString(ioctl) +
              ")  " + SMCCHelper.toString(PIN_MODIFY));
    }
    byte[] resp = icc.transmitControlCommand(ioctl, PIN_MODIFY);
    if (resp != null && resp.length > 0) {
      if (resp[0] == (byte) 0x57) {
        log.error("Invalid parameter in PIN_MODIFY structure");
        throw new CardException("ERROR_INVALID_PARAMETER");
      } else {
        log.error("unexpected response to MODIFY_PIN_START: " +
                SMCCHelper.toString(resp));
        throw new CardException("unexpected response to MODIFY_PIN_START: " +
                SMCCHelper.toString(resp));
      }
    }
  }

  byte[] modifyPinFinish() throws CardException {
    if (!features.containsKey(FEATURE_MODIFY_PIN_FINISH)) {
      throw new CardException("FEATURE_MODIFY_FINISH_FINISH not supported");
    }
    int ioctl = features.get(FEATURE_MODIFY_PIN_FINISH);
    if (log.isTraceEnabled()) {
      log.trace("MODIFY_PIN_FINISH (" + Integer.toHexString(ioctl) + ")");
    }
    byte[] resp = icc.transmitControlCommand(ioctl, new byte[0]);
    if (resp != null && resp.length == 2) {
      if (log.isTraceEnabled()) {
        log.trace("response " + SMCCHelper.toString(resp));
      }
      return resp;
    }
    log.error("unexpected response to MODIFY_PIN_FINISH: " +
            SMCCHelper.toString(resp));
    throw new CardException("unexpected response to MODIFY_PIN_FINISH: " +
            SMCCHelper.toString(resp));
  }


  byte getKeyPressed() throws CardException {
    if (!features.containsKey(FEATURE_GET_KEY_PRESSED)) {
      throw new CardException("FEATURE_GET_KEY_PRESSED not supported");
    }
    int ioctl = features.get(FEATURE_GET_KEY_PRESSED);
    byte[] resp = icc.transmitControlCommand(ioctl, new byte[0]);
    if (resp != null && resp.length == 1) {
//      if (log.isTraceEnabled()) {
//        log.trace("response " + SMCCHelper.toString(resp));
//      }
      return resp[0];
    }
    log.error("unexpected response to GET_KEY_PRESSED: " +
            SMCCHelper.toString(resp));
    throw new CardException("unexpected response to GET_KEY_PRESSED: " +
            SMCCHelper.toString(resp));
  }



  @Override
  public byte[] verifyPinDirect(byte[] PIN_VERIFY) throws CardException {
    if (!features.containsKey(FEATURE_VERIFY_PIN_DIRECT)) {
      throw new CardException("FEATURE_VERIFY_PIN_DIRECT not supported");
    }
    int ioctl = features.get(FEATURE_VERIFY_PIN_DIRECT);
    if (log.isTraceEnabled()) {
      log.trace("VERIFY_PIN_DIRECT (" + Integer.toHexString(ioctl) +
              ")  " + SMCCHelper.toString(PIN_VERIFY));
    }
    byte[] resp = icc.transmitControlCommand(ioctl, PIN_VERIFY);
    if (log.isTraceEnabled()) {
      log.trace("response " + SMCCHelper.toString(resp));
    }
    return resp;
  }

  @Override
  public byte[] verifyPin(byte[] PIN_VERIFY) throws PINOperationAbortedException, CardException {
    verifyPinStart(PIN_VERIFY);
    byte resp;
    do {
      resp = getKeyPressed();
      if (resp == (byte) 0x00) {
        synchronized(this) {
          try {
            wait(PIN_ENTRY_POLLING_INTERVAL);
          } catch (InterruptedException ex) {
            log.error("interrupted in VERIFY_PIN");
          }
        }
      } else if (resp == (byte) 0x0d) {
        log.trace("user confirmed");
        break;
      } else if (resp == (byte) 0x2b) {
        log.trace("user entered valid key (0-9)");
      } else if (resp == (byte) 0x1b) {
        log.info("user cancelled VERIFY_PIN via cancel button");
        return verifyPinFinish();
//        return new byte[] { (byte) 0x64, (byte) 0x01 };
      } else if (resp == (byte) 0x08) {
        log.trace("user pressed correction/backspace button");
      } else if (resp == (byte) 0x0e) {
        log.trace("timeout occured");
        return verifyPinFinish(); // return 0x64 0x00
      } else if (resp == (byte) 0x40) {
        log.trace("PIN_Operation_Aborted");
        throw new PINOperationAbortedException("PIN_Operation_Aborted");
      } else if (resp == (byte) 0x0a) {
        log.trace("all keys cleared");
      } else {
        log.error("unexpected response to GET_KEY_PRESSED: " +
            Integer.toHexString(resp));
        throw new CardException("unexpected response to GET_KEY_PRESSED: " +
            Integer.toHexString(resp));
      }
    } while (true); 

    return verifyPinFinish();
  }

  @Override
  public byte[] modifyPin(byte[] PIN_MODIFY) throws PINOperationAbortedException, CardException {
    modifyPinStart(PIN_MODIFY);
    log.debug(PIN_MODIFY[9] + " pin confirmations expected");

    byte resp;
    short pinConfirmations = 0;
    do {
      resp = getKeyPressed();
      if (resp == (byte) 0x00) {
        synchronized(this) {
          try {
            wait(PIN_ENTRY_POLLING_INTERVAL);
          } catch (InterruptedException ex) {
            log.error("interrupted in MODIFY_PIN");
          }
        }
      } else if (resp == (byte) 0x0d) {
        log.trace("user confirmed");
        pinConfirmations++;
        continue;
      } else if (resp == (byte) 0x2b) {
        log.trace("user entered valid key (0-9)");
      } else if (resp == (byte) 0x1b) {
        log.info("user cancelled MODIFY_PIN via cancel button");
//        return verifyPinFinish();
        return new byte[] { (byte) 0x64, (byte) 0x01 };
      } else if (resp == (byte) 0x08) {
        log.trace("user pressed correction/backspace button");
      } else if (resp == (byte) 0x0e) {
        log.trace("timeout occured");
        return new byte[] { (byte) 0x64, (byte) 0x00 };
//        return verifyPinFinish(); // return 0x64 0x00
      } else if (resp == (byte) 0x40) {
        log.trace("PIN_Operation_Aborted");
        throw new PINOperationAbortedException("PIN_Operation_Aborted");
      } else if (resp == (byte) 0x0a) {
        log.trace("all keys cleared");
      } else {
        log.error("unexpected response to GET_KEY_PRESSED: " +
            Integer.toHexString(resp));
        throw new CardException("unexpected response to GET_KEY_PRESSED: " +
            Integer.toHexString(resp));
      }
    } while (pinConfirmations < PIN_MODIFY[9]); 

    return modifyPinFinish();
  }

  /**
   * 
   * @param PIN_MODIFY
   * @return
   * @throws javax.smartcardio.CardException
   */
  @Override
  public byte[] modifyPinDirect(byte[] PIN_MODIFY) throws CardException {
    if (!features.containsKey(FEATURE_MODIFY_PIN_DIRECT)) {
      throw new CardException("FEATURE_MODIFY_PIN_DIRECT not supported");
    }
    int ioctl = features.get(FEATURE_MODIFY_PIN_DIRECT);
    if (log.isTraceEnabled()) {
      log.trace("MODIFY_PIN_DIRECT (" + Integer.toHexString(ioctl) +
              ")  " + SMCCHelper.toString(PIN_MODIFY));
    }
    byte[] resp = icc.transmitControlCommand(ioctl, PIN_MODIFY);
    if (log.isTraceEnabled()) {
      log.trace("response " + SMCCHelper.toString(resp));
    }
    return resp;
  }
}
