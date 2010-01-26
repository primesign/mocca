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


import javax.smartcardio.ATR;

import at.gv.egiz.smcc.CardChannelEmul;
import at.gv.egiz.smcc.CardEmul;
import at.gv.egiz.smcc.PIN;

@SuppressWarnings("restriction")
public class STARCOSCardEmul extends CardEmul {

  public static byte[] DEFAULT_SS_PIN = new byte[] { (byte) 0x26, (byte) 0x12, (byte) 0x34, (byte) 0x56, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff};
  public static byte[] DEFAULT_Glob_PIN = new byte[] { (byte) 0x24, (byte) 0x00, (byte) 0x00, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff};

  protected static ATR ATR = new ATR(new byte[] {
      (byte) 0x3b, (byte) 0xbd, (byte) 0x18, (byte) 0x00, (byte) 0x81, (byte) 0x31, (byte) 0xfe, (byte) 0x45, 
      (byte) 0x80, (byte) 0x51, (byte) 0x02, (byte) 0x67, (byte) 0x05, (byte) 0x18, (byte) 0xb1, (byte) 0x02, 
      (byte) 0x02, (byte) 0x02, (byte) 0x01, (byte) 0x81, (byte) 0x05, (byte) 0x31
  });

  public STARCOSCardEmul() {
    this(DEFAULT_SS_PIN, DEFAULT_Glob_PIN, PIN.STATE_RESET);
  }

  public STARCOSCardEmul(byte[] SS_PIN, byte[] Glob_PIN, int PIN_STATE) {
    channel = new STARCOSCardChannelEmul(this, Glob_PIN, PIN_STATE);
    applications.add(new STARCOSApplSichereSignatur((STARCOSCardChannelEmul) channel, SS_PIN, PIN_STATE));
    applications.add(new STARCOSApplInfobox((STARCOSCardChannelEmul) channel));
    applications.add(new STARCOSApplGewoehnlicheSignatur((STARCOSCardChannelEmul) channel,
            STARCOSApplGewoehnlicheSignatur.DST));
  }

  @Override
  public ATR getATR() {
    return ATR;
  }
}