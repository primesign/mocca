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
package at.gv.egiz.smcc;

import at.gv.egiz.smcc.reader.CardReader;
import at.gv.egiz.smcc.reader.ReaderFactory;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractSignatureCard implements SignatureCard {

  private final Logger log = LoggerFactory.getLogger(AbstractSignatureCard.class);

  private Locale locale = Locale.getDefault();

  private Card card_;
  
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
  }
  
  @Override
  public Card getCard() {
    return card_;
  }

  protected CardChannel getCardChannel() {
	  
	  return new LogCardChannel(card_.getBasicChannel());
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
