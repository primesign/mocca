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

  protected final static Log log = LogFactory.getLog(DefaultReader.class);

  private static int CTL_CODE(int code) {
    return 0x42000000 + code;
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

  public Card connect() throws CardException { //SignatureCardException {
//      try {
    icc = ct.connect("*");
    return icc;
//      } catch (CardException ex) {
//        log.error(ex.getMessage(), ex);
//        throw new SignatureCardException("Failed to connect to card: " + ex.getMessage());
//      }
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
                new byte[]{});

        if (log.isTraceEnabled()) {
          log.trace("Response TLV " + SMCCHelper.toString(resp));
        }
        // tag
        // length in bytes (always 4)
        // control code value for supported feature (in big endian)
        for (int i = 0; i < resp.length; i += 6) {
          Byte feature = new Byte(resp[i]);
          int ioctlBigEndian = (resp[i + 2] << 24) |
                  (resp[i + 3] << 16) | (resp[i + 4] << 8) | resp[i + 5];
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

//  public Integer getIOCTL(Byte feature) {
//    if (features != null) {
//      return features.get(feature);
//    }
//    return null;
//  }

  @Override
  public byte[] transmitControlCommand(Byte feature, byte[] ctrlCommand)
          throws SignatureCardException {
    try {
      if (!features.containsKey(feature)) {
        throw new SignatureCardException(FEATURES[feature.intValue()] + " not supported");
      }
      int ioctl = features.get(feature);
      if (log.isTraceEnabled()) {
        log.trace("CtrlCommand (" + Integer.toHexString(ioctl) +
                ")  " + SMCCHelper.toString(ctrlCommand));
      }
      byte[] resp = icc.transmitControlCommand(ioctl, ctrlCommand);
      if (log.isTraceEnabled()) {
        log.trace("CtrlCommand Response " + SMCCHelper.toString(resp));
      }
      return resp;
    } catch (CardException ex) {
      log.error(ex.getMessage());
      throw new SignatureCardException("Failed to transmit CtrlCommand for " +
              FEATURES[feature.intValue()]);
    }
  }


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
    return (byte) 0x12;
  }

  @Override
  public byte getwPINMaxExtraDigitH() {
    return (byte) 0x00;
  }

  @Override
  public byte getbEntryValidationCondition() {
    return (byte) 0x02;    // validation key pressed
  }


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
