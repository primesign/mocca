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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.smartcardio.ATR;
import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;

public class CardEmul extends Card {

  protected ATR atr;
  
  protected Thread exclThread;
  protected CardChannel basicChannel;
  protected List<AbstractAppl> applications = new ArrayList<AbstractAppl>();

  public CardEmul() {
    super();
  }

  public void setAtr(byte[] atr) {
    this.atr = new ATR(atr);
  }

  /**
   * @param basicChannel
   *          the basicChannel to set
   */
  public void setBasicChannel(CardChannel basicChannel) {
    this.basicChannel = basicChannel;
  }

  /**
   * @param applications
   *          the applications to set
   */
  public void setApplications(List<AbstractAppl> applications) {
    this.applications = applications;
  }

  @Override
  public void beginExclusive() throws CardException {

    if (exclThread == Thread.currentThread()) {
      throw new CardException(
          "Exclusive access already assigned to current thread.");
    } else if (exclThread != null) {
      throw new CardException(
          "Exclusive access already assigned to another thread.");
    }

    exclThread = Thread.currentThread();

  }

  @Override
  public void endExclusive() throws CardException {

    if (exclThread == Thread.currentThread()) {
      exclThread = null;
    } else if (exclThread == null) {
      throw new CardException("Exclusive access has not been assigned.");
    } else {
      throw new CardException(
          "Exclusive access has not been assigned to current thread.");
    }

  }

  @Override
  public CardChannel getBasicChannel() {
    return basicChannel;
  }

  @Override
  public void disconnect(boolean reset) throws CardException {
    // if (reset) {
    // channel = newCardChannel(this);
    // }
  }

  @Override
  public CardChannel openLogicalChannel() throws CardException {
    throw new CardException("Logical channels not supported.");
  }

  @Override
  public String getProtocol() {
    return "T1";
  }

  @Override
  public byte[] transmitControlCommand(int arg0, byte[] arg1)
      throws CardException {
    throw new CardException("transmitControlCommand() not supported.");
  }

  @Override
  public ATR getATR() {
    return atr;
  }

  public AbstractAppl getApplication(byte[] fid) {

    for (AbstractAppl appl : applications) {
      if (Arrays.equals(appl.getAID(), fid)
          || Arrays.equals(appl.getFID(), fid)) {
        return appl;
      }
    }
    return null;

  }

}