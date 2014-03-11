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


import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.ResponseAPDU;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.gv.egiz.smcc.ChangeReferenceDataAPDUSpec;
import at.gv.egiz.smcc.NewReferenceDataAPDUSpec;
import at.gv.egiz.smcc.PinInfo;
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

  private final Logger log = LoggerFactory.getLogger(DefaultCardReader.class);

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
          PINGUI pinGUI, PinInfo pinSpec, int retries)
        throws SignatureCardException, CardException, InterruptedException {

    log.debug("VERIFY");
    Card card = channel.getCard();
    boolean regain = dropExclusive(card);
    try {
      char[] pin = pinGUI.providePIN(pinSpec, retries);
      return channel.transmit(ISO7816Utils.createVerifyAPDU(apduSpec, pin));
    } finally {
      regainExclusive(card, regain);
    }
  }

  @Override
  public ResponseAPDU modify(CardChannel channel, ChangeReferenceDataAPDUSpec apduSpec,
          ModifyPINGUI pinGUI, PinInfo pinSpec, int retries)
        throws SignatureCardException, CardException, InterruptedException {
    log.debug("MODIFY (CHANGE_REFERENCE_DATA)");
    Card card = channel.getCard();
    boolean regain = dropExclusive(card);
    try {
      char[] oldPIN = pinGUI.provideCurrentPIN(pinSpec, retries);
      char[] newPIN = pinGUI.provideNewPIN(pinSpec);
      return channel.transmit(ISO7816Utils.createChangeReferenceDataAPDU(apduSpec, oldPIN, newPIN));
    } finally {
      regainExclusive(card, regain);
    }
  }

  @Override
  public ResponseAPDU modify(CardChannel channel, NewReferenceDataAPDUSpec apduSpec,
          ModifyPINGUI pinGUI, PinInfo pinSpec)
        throws SignatureCardException, CardException, InterruptedException {
    log.debug("MODIFY (NEW_REFERENCE_DATA)");
    Card card = channel.getCard();
    boolean regain = dropExclusive(card);
    try {
      char[] newPIN = pinGUI.provideNewPIN(pinSpec);
      return channel.transmit(ISO7816Utils.createNewReferenceDataAPDU(apduSpec, newPIN));
    } finally {
      regainExclusive(card, regain);
    }
  }

  @Override
  public ResponseAPDU modify(CardChannel channel, ResetRetryCounterAPDUSpec apduSpec,
          ModifyPINGUI pinGUI, PinInfo pinSpec, int retries)
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

  private boolean dropExclusive(Card card) throws CardException {
    log.debug("Dropping exclusive card access");
    try {
      card.endExclusive();
    } catch (IllegalStateException e) {
      log.debug("Didn't have exclusive access");
      return false;
    }
    return true;
  }

  private void regainExclusive(Card card, boolean doRegainExclusive) throws CardException {
    if (doRegainExclusive) {
      log.debug("Trying to regain exclusive card access");
      card.beginExclusive();
    }
  }
}
