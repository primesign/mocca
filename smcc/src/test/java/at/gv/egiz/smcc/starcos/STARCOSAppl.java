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
package at.gv.egiz.smcc.starcos;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Iterator;

import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

import at.gv.egiz.smcc.AbstractAppl;
import at.gv.egiz.smcc.CardAppl;
import at.gv.egiz.smcc.CardChannelEmul;
import at.gv.egiz.smcc.PIN;

@SuppressWarnings("restriction")
public abstract class STARCOSAppl extends AbstractAppl implements CardAppl {

  public static byte[] AID_SichereSignatur = new byte[] { (byte) 0xD0, (byte) 0x40,
        (byte) 0x00, (byte) 0x00, (byte) 0x17, (byte) 0x00, (byte) 0x12, (byte) 0x01 };
  
  public static byte[] FID_SichereSignatur = new byte[] { (byte) 0x3F, (byte) 0x04 };
  
  public static byte[] AID_Infobox = new byte[] { (byte) 0xD0, (byte) 0x40,
    (byte) 0x00, (byte) 0x00, (byte) 0x17, (byte) 0x00, (byte) 0x18, (byte) 0x01 };

  public static byte[] FID_Infobox = new byte[] { (byte) 0x3F, (byte) 0x06 };

  public static byte[] AID_GewoehnlicheSignatur = new byte[] { (byte) 0xD0, (byte) 0x40,
    (byte) 0x00, (byte) 0x00, (byte) 0x17, (byte) 0x00, (byte) 0x13, (byte) 0x01 };

  public static byte[] FID_GewoehnlicheSignatur = new byte[] { (byte) 0x3F, (byte) 0x05 };
  
  protected STARCOSCardChannelEmul channel;
  
  protected byte[] securityEnv;

  protected byte[] hash;

  public STARCOSAppl(STARCOSCardChannelEmul channel) {
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
