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