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


package at.gv.egiz.smcc.starcos;

import java.util.Iterator;

import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

import at.gv.egiz.smcc.AbstractAppl;
import at.gv.egiz.smcc.CardAppl;
import at.gv.egiz.smcc.CardChannelEmul;
import at.gv.egiz.smcc.PIN;

public abstract class STARCOSAppl extends AbstractAppl implements CardAppl {

  protected STARCOSCardChannelEmul channel;
  
  protected byte[] securityEnv;

  protected byte[] hash;

  public void setCardChannel(STARCOSCardChannelEmul channel) {
    this.channel = channel;
  }

  @Override
  public ResponseAPDU cmdINTERNAL_AUTHENTICATE(CommandAPDU command, CardChannelEmul channel) {
    return new ResponseAPDU(new byte[] {(byte) 0x6D, (byte) 0x00});
  }

  @Override
  public void leaveApplContext() {
    Iterator<PIN> pin = pins.values().iterator();
    while (pin.hasNext()) {
      pin.next().state = PIN.STATE_RESET;
    }
  }
}
