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
