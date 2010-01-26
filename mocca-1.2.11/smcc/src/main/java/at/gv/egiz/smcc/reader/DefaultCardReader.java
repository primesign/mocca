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


import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.ResponseAPDU;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import at.gv.egiz.smcc.ChangeReferenceDataAPDUSpec;
import at.gv.egiz.smcc.NewReferenceDataAPDUSpec;
import at.gv.egiz.smcc.PINSpec;
import at.gv.egiz.smcc.ResetRetryCounterAPDUSpec;
import at.gv.egiz.smcc.SignatureCardException;
import at.gv.egiz.smcc.VerifyAPDUSpec;
import at.gv.egiz.smcc.pin.gui.ModifyPINGUI;
import at.gv.egiz.smcc.pin.gui.PINGUI;
import at.gv.egiz.smcc.util.ISO7816Utils;

/**
 *
 * @author Clemens Orthacker <clemens.orthacker@iaik.tugraz.at>
 */
public class DefaultCardReader implements CardReader {

  protected final static Log log = LogFactory.getLog(DefaultCardReader.class);

  protected CardTerminal ct;
  protected String name;

  public DefaultCardReader(CardTerminal ct) {
    if (ct == null) {
      throw new NullPointerException("no card or card terminal provided");
    }
    this.ct = ct;
    this.name = ct.getName();
  }

  @Override
  public ResponseAPDU verify(CardChannel channel, VerifyAPDUSpec apduSpec,
          PINGUI pinGUI, PINSpec pinSpec, int retries)
        throws SignatureCardException, CardException, InterruptedException {

    log.debug("VERIFY");
    return channel.transmit(ISO7816Utils.createVerifyAPDU(apduSpec, pinGUI.providePIN(pinSpec, retries)));
  }

  @Override
  public ResponseAPDU modify(CardChannel channel, ChangeReferenceDataAPDUSpec apduSpec,
          ModifyPINGUI pinGUI, PINSpec pinSpec, int retries)
        throws SignatureCardException, CardException, InterruptedException {
    log.debug("MODIFY (CHANGE_REFERENCE_DATA)");
    char[] oldPIN = pinGUI.provideCurrentPIN(pinSpec, retries);
    char[] newPIN = pinGUI.provideNewPIN(pinSpec);
    return channel.transmit(ISO7816Utils.createChangeReferenceDataAPDU(apduSpec, oldPIN, newPIN));
  }

  @Override
  public ResponseAPDU modify(CardChannel channel, NewReferenceDataAPDUSpec apduSpec,
          ModifyPINGUI pinGUI, PINSpec pinSpec)
        throws SignatureCardException, CardException, InterruptedException {
    log.debug("MODIFY (NEW_REFERENCE_DATA)");
    char[] newPIN = pinGUI.provideNewPIN(pinSpec);
    return channel.transmit(ISO7816Utils.createNewReferenceDataAPDU(apduSpec, newPIN));
  }

  @Override
  public ResponseAPDU modify(CardChannel channel, ResetRetryCounterAPDUSpec apduSpec,
          ModifyPINGUI pinGUI, PINSpec pinSpec, int retries)
          throws InterruptedException, CardException, SignatureCardException {
    log.debug("MODIFY (RESET_RETRY_COUNTER)");
    //TODO
    return modify(channel, (ChangeReferenceDataAPDUSpec) apduSpec, pinGUI, pinSpec, retries);
  }

  @Override
  public Card connect() throws CardException {
    log.debug("connect icc");
    return ct.connect("*");
  }

  @Override
  public boolean hasFeature(Byte feature) {
    return false;
  }

}
