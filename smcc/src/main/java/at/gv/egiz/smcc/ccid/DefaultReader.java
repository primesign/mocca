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
import java.util.logging.Level;
import java.util.logging.Logger;
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

  /**
   * supported features and respective control codes
   */
  protected Map<Byte, Integer> features;

  public DefaultReader(Card icc, CardTerminal ct) {
    if (icc == null || ct == null) {
      throw new NullPointerException("no card or card terminal provided");
    }
    this.icc = icc;
    this.ct = ct;
    features = queryFeatures(); 
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
          int ioctlBigEndian = ((0xff & resp[i + 2]) << 24) |
                  ((0xff & resp[i + 3]) << 16) |
                  ((0xff & resp[i + 4]) << 8) |
                  (0xff & resp[i + 5]);
          Integer ioctl = new Integer(ioctlBigEndian);
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
    if (features != null) {
      return features.containsKey(feature);
    }
    return false;
  }

//  protected byte[] transmitControlCommand(Byte feature, byte[] ctrlCommand)
//          throws CardException {
//    try {
//      if (!features.containsKey(feature)) {
//        throw new CardException(FEATURES[feature.intValue()] + " not supported");
//      }
//      int ioctl = features.get(feature);
//      if (log.isTraceEnabled()) {
//        log.trace("CtrlCommand (" + Integer.toHexString(ioctl) +
//                ")  " + SMCCHelper.toString(ctrlCommand));
//      }
//      byte[] resp = icc.transmitControlCommand(ioctl, ctrlCommand);
//      if (log.isTraceEnabled()) {
//        log.trace("CtrlCommand Response " + SMCCHelper.toString(resp));
//      }
//      return resp;
//    } catch (CardException ex) {
//      log.error(ex.getMessage());
//      throw new SignatureCardException("Failed to transmit CtrlCommand for " +
//              FEATURES[feature.intValue()]);
//    }
//  }


  @Override
  public byte getbTimeOut() {
    return (byte) 0x3c;    // (max 1min on ReinerSCT),
                           // 0x00=default, 0x1e = 30sec
  }

  @Override
  public byte getbTimeOut2() {
    return (byte) 0x00;    // default
  }

  @Override
  public byte getwPINMaxExtraDigitL() {
    return (byte) 0x12;     // signed int
  }

  @Override
  public byte getwPINMaxExtraDigitH() {
    return (byte) 0x00;
  }

  @Override
  public byte getbEntryValidationCondition() {
    return (byte) 0x02;    // validation key pressed
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
//    if (log.isTraceEnabled()) {
//      log.trace("GET_KEY_PRESSED (" + Integer.toHexString(ioctl) + ")");
//    }
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
            wait(200);
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
    } while (true); //resp != (byte) 0x0d);

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
            wait(200);
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
   * NOT SUPPORTED FOR ACOS ON OMNIKEY CardMan 3621
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


//[TRACE] SmartCardIO - terminal 'PC/SC terminal OMNIKEY CardMan 3621 0' card inserted : PC/SC card in OMNIKEY CardMan 3621 0, protocol T=1, state OK
//[TRACE] DefaultReader - GET_FEATURE_REQUEST 313520 on OMNIKEY CardMan 3621 0
//[TRACE] DefaultReader - Response TLV [01:04:00:31:30:00:02:04:00:31:2f:d4:03:04:00:31:30:04:04:04:00:31:2f:dc:05:04:00:31:2f:e0:0a:04
//00:31:30:08]
//[INFO] DefaultReader - CCID supports FEATURE_VERIFY_PIN_START: 313000
//[INFO] DefaultReader - CCID supports FEATURE_VERIFY_PIN_FINISH: ffffffd4
//[INFO] DefaultReader - CCID supports FEATURE_MODIFY_PIN_START: 313004
//[INFO] DefaultReader - CCID supports FEATURE_MODIFY_PIN_FINISH: ffffffdc
//[INFO] DefaultReader - CCID supports FEATURE_GET_KEY_PRESSED: ffffffe0
//[INFO] DefaultReader - CCID supports FEATURE_IFD_PIN_PROPERTIES: 313008
//[TRACE] AbstractSignatureCard - Setting IFS (information field size) to 254


  //  protected byte ifdGetKeyPressed() throws CardException {
//    if (ifdSupportsFeature(FEATURE_VERIFY_PIN_DIRECT)) {
//
//      Long controlCode = (Long) IFD_IOCTL.get(new Byte((byte) 0x05));
//
//      byte key = 0x00;
//      while (key == 0x00) {
//
//        byte[] resp = card_.transmitControlCommand(controlCode.intValue(), new byte[] {});
//
//        if (resp != null && resp.length > 0) {
//          key = resp[0];
//        }
//      }
//
//      System.out.println("Key: " + key);
//
//    }
//
//    return 0x00;
//  }
//
//  protected byte[] ifdVerifyPINFinish() throws CardException {
//    if (ifdSupportsFeature(FEATURE_VERIFY_PIN_DIRECT)) {
//
//      Long controlCode = (Long) IFD_IOCTL.get(new Byte((byte) 0x02));
//
//      byte[] resp = card_.transmitControlCommand(controlCode.intValue(), new byte[] {});
//
//      System.out.println("CommandResp: " + toString(resp));
//
//      return resp;
//
//    }
//
//    return null;
//  }


  /**
   * assumes ifdSupportsVerifyPIN() == true
   * @param pinVerifyStructure
   * @return
   * @throws javax.smartcardio.CardException
   */
//  protected byte[] ifdVerifyPIN(byte[] pinVerifyStructure) throws CardException {
//
////      Long ctrlCode = (Long) ifdFeatures.get(FEATURE_IFD_PIN_PROPERTIES);
////      if (ctrlCode != null) {
////        if (log.isTraceEnabled()) {
////          log.trace("PIN_PROPERTIES CtrlCode " + Integer.toHexString(ctrlCode.intValue()));
////        }
////        byte[] resp = card_.transmitControlCommand(ctrlCode.intValue(), new byte[] {});
////
////        if (log.isTraceEnabled()) {
////          log.trace("PIN_PROPERTIES Response " + SMCCHelper.toString(resp));
////        }
////      }
//
//
//      Long ctrlCode = (Long) ifdFeatures.get(FEATURE_VERIFY_PIN_DIRECT);
//      if (ctrlCode == null) {
//        throw new NullPointerException("no CtrlCode for FEATURE_VERIFY_PIN_DIRECT");
//      }
//
//      if (log.isTraceEnabled()) {
//        log.trace("VERIFY_PIN_DIRECT CtrlCode " + Integer.toHexString(ctrlCode.intValue()) +
//                ", PIN_VERIFY_STRUCTURE " + SMCCHelper.toString(pinVerifyStructure));
//      }
//      byte[] resp = card_.transmitControlCommand(ctrlCode.intValue(), pinVerifyStructure);
//
//      if (log.isTraceEnabled()) {
//        log.trace("VERIFY_PIN_DIRECT Response " + SMCCHelper.toString(resp));
//      }
//      return resp;
//  }

//  protected Long getControlCode(Byte feature) {
//    if (ifdFeatures != null) {
//      return ifdFeatures.get(feature);
//    }
//    return null;
//  }

  
}
