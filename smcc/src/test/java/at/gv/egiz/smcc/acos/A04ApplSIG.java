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
package at.gv.egiz.smcc.acos;

import java.util.Arrays;

import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

import at.gv.egiz.smcc.CardChannelEmul;
import at.gv.egiz.smcc.File;


@SuppressWarnings("restriction")
public class A04ApplSIG extends ACOSApplSIG {
  
  private static byte[] FID_EF_INFO = new byte[] { (byte) 0xd0, (byte) 0x02 };

  private static byte[] FCI_EF_INFO = new byte[] { (byte) 0x6f, (byte) 0x07,
      (byte) 0x80, (byte) 0x02, (byte) 0x00, (byte) 0x08, (byte) 0x82,
      (byte) 0x01, (byte) 0x01 };

  private static byte[] EF_INFO = new byte[] { (byte) 0x02, (byte) 0x00,
      (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
      (byte) 0x00, (byte) 0x90, (byte) 0x00 };

  public A04ApplSIG() {
    putFile(new File(FID_EF_INFO, EF_INFO, FCI_EF_INFO));
  }

  @Override
  public ResponseAPDU cmdMANAGE_SECURITY_ENVIRONMENT(CommandAPDU command, CardChannelEmul channel) {

    checkINS(command, 0x22);

    switch (command.getP2()) {
    case 0xA4:
      switch (command.getP1()) {
      case 0x41:
        // INTERNAL AUTHENTICATE
      case 0x81:
        // EXTERNAL AUTHENTICATE
      }
    case 0xB6:
      switch (command.getP1()) {
      case 0x41: {
        // PSO - COMPUTE DIGITAL SIGNATURE
        byte[] dst = new byte[] { (byte) 0x84, (byte) 0x01, (byte) 0x88, (byte) 0x80, (byte) 0x01, (byte) 0x14 };
        if (Arrays.equals(dst, command.getData())) {
          securityEnv = command.getData();
          return new ResponseAPDU(new byte[] {(byte) 0x90, (byte) 0x00});
        } else {
          return new ResponseAPDU(new byte[] {(byte) 0x6F, (byte) 0x05});
        }
      }
      case 0x81:
        // PSO - VERIFY DGITAL SIGNATURE
      }
    case 0xB8:
      switch (command.getP1()) {
      case 0x41:
        // PSO � DECIPHER
      case 0x81:
        // PSO � ENCIPHER
      }
    default:
      return new ResponseAPDU(new byte[] {(byte) 0x6A, (byte) 0x81});
    }

  }
  
  
}
