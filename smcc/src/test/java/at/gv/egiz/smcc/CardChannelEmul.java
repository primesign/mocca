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

import java.nio.ByteBuffer;

import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;

public abstract class CardChannelEmul extends CardChannel {
  
  protected CardEmul cardEmul;

  protected AbstractAppl currentAppl = null;
  protected File currentFile = null;

  @Override
  public int getChannelNumber() {
    return 0;
  }

  @Override
  public void close() throws CardException {
    throw new IllegalStateException("Basic logical channel cannot be closed.");
  }

  @Override
  public int transmit(ByteBuffer command, ByteBuffer response) throws CardException {
    byte[] responseBytes = transmit(new CommandAPDU(command)).getBytes();
    response.put(responseBytes);
    return responseBytes.length;
  }

  public Card getCard() {
    return cardEmul;
  }

  public void setCardEmul(CardEmul card) {
    this.cardEmul = card;
  }

}