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

import javax.smartcardio.Card;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;

public class CardTerminalEmul extends CardTerminal {
  
  private Card card;
  
  public CardTerminalEmul(Card card) {
    this.card = card;
  }

  @Override
  public Card connect(String protocol) throws CardException {
    if ("*".equals(protocol) || "T=1".equals(protocol)) {
      return card;
    } else {
      throw new CardException("Protocol '" + protocol + "' not supported.");
    }
  }

  @Override
  public String getName() {
    return "CardTerminal Emulation";
  }

  @Override
  public boolean isCardPresent() throws CardException {
    return true;
  }

  @Override
  public boolean waitForCardAbsent(long timeout) throws CardException {
    try {
      Thread.sleep(timeout);
    } catch (InterruptedException e) {
    }
    return false;
  }

  @Override
  public boolean waitForCardPresent(long timeout) throws CardException {
    return true;
  }

}
