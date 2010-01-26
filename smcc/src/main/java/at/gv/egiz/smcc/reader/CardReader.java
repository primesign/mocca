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
package at.gv.egiz.smcc.reader;

import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.ResponseAPDU;

import at.gv.egiz.smcc.CancelledException;
import at.gv.egiz.smcc.ChangeReferenceDataAPDUSpec;
import at.gv.egiz.smcc.NewReferenceDataAPDUSpec;
import at.gv.egiz.smcc.PINSpec;
import at.gv.egiz.smcc.ResetRetryCounterAPDUSpec;
import at.gv.egiz.smcc.SignatureCardException;
import at.gv.egiz.smcc.VerifyAPDUSpec;
import at.gv.egiz.smcc.pin.gui.ModifyPINGUI;
import at.gv.egiz.smcc.pin.gui.PINGUI;
import javax.smartcardio.Card;

/**
 *
 * @author Clemens Orthacker <clemens.orthacker@iaik.tugraz.at>
 */
public interface CardReader {


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


  Card connect() throws CardException;

  boolean hasFeature(Byte feature);

  ResponseAPDU verify(CardChannel channel, VerifyAPDUSpec apduSpec,
          PINGUI pinGUI, PINSpec pinSpec, int retries)
      throws CancelledException, InterruptedException, CardException, SignatureCardException;

  ResponseAPDU modify(CardChannel channel, ChangeReferenceDataAPDUSpec apduSpec,
          ModifyPINGUI pinGUI, PINSpec pinSpec, int retries)
      throws CancelledException, InterruptedException, CardException, SignatureCardException;

  ResponseAPDU modify(CardChannel channel, NewReferenceDataAPDUSpec apduSpec,
          ModifyPINGUI pinGUI, PINSpec pinSpec)
      throws CancelledException, InterruptedException, CardException, SignatureCardException;

  ResponseAPDU modify(CardChannel channel, ResetRetryCounterAPDUSpec apduSpec,
          ModifyPINGUI pinGUI, PINSpec pinSpec, int retries)
      throws CancelledException, InterruptedException, CardException, SignatureCardException;
}
