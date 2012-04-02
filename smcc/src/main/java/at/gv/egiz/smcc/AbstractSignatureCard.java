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


package at.gv.egiz.smcc;

import at.gv.egiz.smcc.reader.CardReader;
import at.gv.egiz.smcc.reader.ReaderFactory;
import java.util.Locale;

import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractSignatureCard implements SignatureCard {

  private final Logger log = LoggerFactory.getLogger(AbstractSignatureCard.class);

  protected Locale locale = Locale.getDefault();

  private Card card_;
  private String cardterminalname;
  
  protected CardReader reader;

  protected AbstractSignatureCard() {
  }

  protected String toString(byte[] b) {
    StringBuffer sb = new StringBuffer();
    if (b != null && b.length > 0) {
      sb.append(Integer.toHexString((b[0] & 240) >> 4));
      sb.append(Integer.toHexString(b[0] & 15));
    }
    for (int i = 1; i < b.length; i++) {
      sb.append(':');
      sb.append(Integer.toHexString((b[i] & 240) >> 4));
      sb.append(Integer.toHexString(b[i] & 15));
    }
    return sb.toString();
  }

  @Override
  public void init(Card card, CardTerminal cardTerminal) {
    this.card_ = card;
    this.reader = ReaderFactory.getReader(card, cardTerminal);
    this.cardterminalname = cardTerminal.getName();
  }
  
  @Override
  public Card getCard() {
    return card_;
  }

  public String getTerminalName() {
	  return(cardterminalname);
  }
  
  protected CardChannel getCardChannel() {
	  
	  if(card_.getProtocol().equalsIgnoreCase("T=0")) {
		  
		  return new T0CardChannel(card_.getBasicChannel());
	  } else {
	  
		  return new LogCardChannel(card_.getBasicChannel());
	  }
  }

  @Override
  public void setLocale(Locale locale) {
    if (locale == null) {
      throw new NullPointerException("Locale must not be set to null");
    }
    this.locale = locale;
  }

  @Override
  public void disconnect(boolean reset) {
    log.debug("Disconnect called");
    if (card_ != null) {
      try {
        card_.disconnect(reset);
      } catch (Exception e) {
        log.info("Error while resetting card", e);
      }
    }
  }

  @Override
  public void reset() throws SignatureCardException {
    try {
      log.debug("Disconnect and reset smart card.");
      card_.disconnect(true);
      log.debug("Reconnect smart card.");
      card_ = reader.connect();
    } catch (CardException e) {
      throw new SignatureCardException("Failed to reset card.", e);
    }
  }

  public void interfaceMethod(PinInfoTest pinInfo) {
    
  }

  int testMember = 3;

  public static class PinInfoTest {

    void setStatus(int status) {
    }

  }
}
