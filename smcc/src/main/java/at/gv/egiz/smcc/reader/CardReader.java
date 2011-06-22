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

import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.ResponseAPDU;

import at.gv.egiz.smcc.CancelledException;
import at.gv.egiz.smcc.ChangeReferenceDataAPDUSpec;
import at.gv.egiz.smcc.NewReferenceDataAPDUSpec;
import at.gv.egiz.smcc.PinInfo;
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


  static final String[] FEATURES = new String[]{"NO_FEATURE",
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
    "FEATURE_IFD_DISPLAY_PROPERTIES",
    "FEATURE_GET_TLV_PROPERTIES",
    "FEATURE_CCID_ESC_COMMAND"};

  static final Byte FEATURE_VERIFY_PIN_START = new Byte((byte) 0x01);
  static final Byte FEATURE_VERIFY_PIN_FINISH = new Byte((byte) 0x02);
  static final Byte FEATURE_MODIFY_PIN_START = new Byte((byte) 0x03);
  static final Byte FEATURE_MODIFY_PIN_FINISH = new Byte((byte) 0x04);
  static final Byte FEATURE_GET_KEY_PRESSED = new Byte((byte) 0x05);
  static final Byte FEATURE_VERIFY_PIN_DIRECT = new Byte((byte) 0x06);
  static final Byte FEATURE_MODIFY_PIN_DIRECT = new Byte((byte) 0x07);
  static final Byte FEATURE_MCT_READER_DIRECT = new Byte((byte) 0x08);
  static final Byte FEATURE_MCT_UNIVERSAL = new Byte((byte) 0x09);
  static final Byte FEATURE_IFD_PIN_PROPERTIES = new Byte((byte) 0x0a);
  static final Byte FEATURE_ABORT = new Byte((byte) 0x0b);
  static final Byte FEATURE_SET_SPE_MESSAGE = new Byte((byte) 0x0c);
  static final Byte FEATURE_VERIFY_PIN_DIRECT_APP_ID = new Byte((byte) 0x0d);
  static final Byte FEATURE_MODIFY_PIN_DIRECT_APP_ID = new Byte((byte) 0x0e);
  static final Byte FEATURE_WRITE_DISPLAY = new Byte((byte) 0x0f);
  static final Byte FEATURE_GET_KEY = new Byte((byte) 0x10);
  static final Byte FEATURE_IFD_DISPLAY_PROPERTIES = new Byte((byte) 0x11);
  static final Byte FEATURE_GET_TLV_PROPERTIES = new Byte((byte) 0x12);
  static final Byte FEATURE_CCID_ESC_COMMAND = new Byte((byte) 0x13);


  Card connect() throws CardException;

  boolean hasFeature(Byte feature);

  ResponseAPDU verify(CardChannel channel, VerifyAPDUSpec apduSpec,
          PINGUI pinGUI, PinInfo pinInfo, int retries)
      throws CancelledException, InterruptedException, CardException, SignatureCardException;

  ResponseAPDU modify(CardChannel channel, ChangeReferenceDataAPDUSpec apduSpec,
          ModifyPINGUI pinGUI, PinInfo pinInfo, int retries)
      throws CancelledException, InterruptedException, CardException, SignatureCardException;

  ResponseAPDU modify(CardChannel channel, NewReferenceDataAPDUSpec apduSpec,
          ModifyPINGUI pinGUI, PinInfo pinInfo)
      throws CancelledException, InterruptedException, CardException, SignatureCardException;

  ResponseAPDU modify(CardChannel channel, ResetRetryCounterAPDUSpec apduSpec,
          ModifyPINGUI pinGUI, PinInfo pinInfo, int retries)
      throws CancelledException, InterruptedException, CardException, SignatureCardException;
}
