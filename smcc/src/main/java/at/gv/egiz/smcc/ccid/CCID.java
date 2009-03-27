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
import javax.smartcardio.Card;
import javax.smartcardio.CardException;

/**
 *
 * @author Clemens Orthacker <clemens.orthacker@iaik.tugraz.at>
 */
public interface CCID {


  String[] FEATURES = new String[]{"NO_FEATURE",
    "FEATURE_VERIFY_PIN_START",
    "FEATURE_VERIFY_PIN_FINISH",
    "FEATURE_MODIFY_PIN_START",
    "FEATURE_MODIFY_PIN_FINISH",
    "FEATURE_GET_KEY_PRESSED",
    "FEATURE_VERIFY_PIN_DIRECT",
    "FEATURE_MODIFY_PIN_DIRECT",
    "FEATURE_MCT_READER_DIRECT",
    "FEATURE_MCT_UNIVERSAL",
    "FEATURE_IFD_PIN_PROPERTIES",
    "FEATURE_ABORT",
    "FEATURE_SET_SPE_MESSAGE",
    "FEATURE_VERIFY_PIN_DIRECT_APP_ID",
    "FEATURE_MODIFY_PIN_DIRECT_APP_ID",
    "FEATURE_WRITE_DISPLAY",
    "FEATURE_GET_KEY",
    "FEATURE_IFD_DISPLAY_PROPERTIES"};
  
  Byte FEATURE_IFD_PIN_PROPERTIES = new Byte((byte) 10);
  Byte FEATURE_MCT_READER_DIRECT = new Byte((byte) 8);
  Byte FEATURE_MODIFY_PIN_DIRECT = new Byte((byte) 7);
  Byte FEATURE_VERIFY_PIN_DIRECT = new Byte((byte) 6);
  
  Card connect() throws CardException;

  boolean hasFeature(Byte feature);

  /**
   *
   * @param feature the corresponding control code will be transmitted
   * @param ctrlCommand
   * @return
   * @throws at.gv.egiz.smcc.SignatureCardException if feature is not supported
   * or card communication fails
   */
  byte[] transmitControlCommand(Byte feature, byte[] ctrlCommand) throws SignatureCardException;

  /**
   * allow subclasses to override default (deal with reader bugs)
   * @return
   */
  byte getbTimeOut();
  byte getbTimeOut2();
  byte getwPINMaxExtraDigitL();
  byte getwPINMaxExtraDigitH();
  byte getbEntryValidationCondition();
}
