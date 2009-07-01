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

import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.ResponseAPDU;

import at.gv.egiz.smcc.CancelledException;
import at.gv.egiz.smcc.ChangePINProvider;
import at.gv.egiz.smcc.ChangeReferenceDataAPDUSpec;
import at.gv.egiz.smcc.NewReferenceDataAPDUSpec;
import at.gv.egiz.smcc.PINOperationAbortedException;
import at.gv.egiz.smcc.PINProvider;
import at.gv.egiz.smcc.PINSpec;
import at.gv.egiz.smcc.SignatureCardException;
import at.gv.egiz.smcc.VerifyAPDUSpec;

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

  Byte FEATURE_VERIFY_PIN_START = new Byte((byte) 0x01);
  Byte FEATURE_VERIFY_PIN_FINISH = new Byte((byte) 0x02);
  Byte FEATURE_MODIFY_PIN_START = new Byte((byte) 0x03);
  Byte FEATURE_MODIFY_PIN_FINISH = new Byte((byte) 0x04);
  Byte FEATURE_GET_KEY_PRESSED = new Byte((byte) 0x05);
  Byte FEATURE_VERIFY_PIN_DIRECT = new Byte((byte) 0x06);
  Byte FEATURE_MODIFY_PIN_DIRECT = new Byte((byte) 0x07);
  Byte FEATURE_MCT_READER_DIRECT = new Byte((byte) 0x08);
  Byte FEATURE_MCT_UNIVERSAL = new Byte((byte) 0x09);
  Byte FEATURE_IFD_PIN_PROPERTIES = new Byte((byte) 0x0a);
  //TODO continue list

  String getName();

  Card connect() throws CardException;

  void setDisablePinpad(boolean disable);
  
  boolean hasFeature(Byte feature);

  ResponseAPDU verify(CardChannel channel, VerifyAPDUSpec apduSpec,
      PINSpec pinSpec, PINProvider provider, int retries)
      throws CancelledException, InterruptedException, CardException,
      SignatureCardException;

  ResponseAPDU modify(CardChannel channel,
      ChangeReferenceDataAPDUSpec apduSpec, PINSpec pinSpec,
      ChangePINProvider provider, int retries) throws CancelledException,
      InterruptedException, CardException, SignatureCardException;
  
  /**
   * not supported by OMNIKEY CardMan 3621 with ACOS card
   * @param PIN_VERIFY
   * @return
   * @throws at.gv.egiz.smcc.PINOperationAbortedException
   * @throws javax.smartcardio.CardException
   */
  byte[] verifyPin(byte[] PIN_VERIFY) throws PINOperationAbortedException, CardException;
  
  byte[] verifyPinDirect(byte[] PIN_VERIFY) throws CardException;

  /**
   * not supported by OMNIKEY CardMan 3621 with ACOS card
   * @param PIN_MODIFY
   * @return
   * @throws at.gv.egiz.smcc.PINOperationAbortedException
   * @throws javax.smartcardio.CardException
   */
  byte[] modifyPin(byte[] PIN_MODIFY) throws PINOperationAbortedException, CardException;

  byte[] modifyPinDirect(byte[] PIN_MODIFY) throws CardException;

  /**
   *
   * @param feature the corresponding control code will be transmitted
   * @param ctrlCommand
   * @return
   * @throws at.gv.egiz.smcc.SignatureCardException if feature is not supported
   * or card communication fails
   */
//  byte[] transmitControlCommand(Byte feature, byte[] ctrlCommand) throws SignatureCardException;

  /**
   * allow subclasses to override default (deal with reader bugs)
   * @return
   */
  byte getbTimeOut();
  byte getbTimeOut2();
  byte getwPINMaxExtraDigitL();
  byte getwPINMaxExtraDigitH();
  byte getbEntryValidationCondition();

  ResponseAPDU activate(CardChannel channel, NewReferenceDataAPDUSpec apduSpec,
      PINSpec pinSpec, PINProvider provider)
      throws CancelledException, InterruptedException, CardException,
      SignatureCardException;
}
