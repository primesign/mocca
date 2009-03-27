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
package at.gv.egiz.smcc.ccid;

import javax.smartcardio.Card;
import javax.smartcardio.CardTerminal;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author Clemens Orthacker <clemens.orthacker@iaik.tugraz.at>
 */
public class GemplusGemPCPinpad extends DefaultReader {

  protected final static Log log = LogFactory.getLog(GemplusGemPCPinpad.class);

  public GemplusGemPCPinpad(Card icc, CardTerminal ct) {
    super(icc, ct);
    log.info("Initializing Gemplus GemPC Pinpad reader");
    log.info("Gemplus GemPC Pinpad allows PINs to have 4-8 digits");

  }

  @Override
  public byte getbTimeOut() {
    return (byte) 0x3c;    // 0x00 default = 15sec
                           // max 40sec (?)
  }

  @Override
  public byte getbTimeOut2() {
    return (byte) 0x00;    // 0x00 default = 15sec
  }

  @Override
  public byte getwPINMaxExtraDigitL() {
    return (byte) 0x08; 
  }

  @Override
  public byte getwPINMaxExtraDigitH() {
    return (byte) 0x04;
  }

  @Override
  public byte getbEntryValidationCondition() {
    return (byte) 0x02;    // validation key pressed
  }

}
