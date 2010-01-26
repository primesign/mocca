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


import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

import at.gv.egiz.smcc.CardEmul;
import at.gv.egiz.smcc.PIN;


@SuppressWarnings("restriction")
public class A03CardChannelEmul extends ACOSCardChannelEmul {

  public A03CardChannelEmul(CardEmul cardEmul) {
    super(cardEmul);
  }

  @Override
  public ResponseAPDU cmdREAD_BINARY(CommandAPDU command) throws CardException {

    if (command.getINS() != 0xB0) {
      throw new IllegalArgumentException("INS has to be 0xB0.");
    }

    if (currentFile == null) {
      return new ResponseAPDU(new byte[] {(byte) 0x69, (byte) 0x86}); 
    }
    
    if ((command.getP1() & 0x80) > 0) {
      throw new CardException("Not implemented.");
    }
    
    int offset = command.getP2() + (command.getP1() << 8);
    if (offset > currentFile.file.length) {
      // Wrong length
      return new ResponseAPDU(new byte[] {(byte) 0x67, (byte) 0x00});
    }
    
    if (command.getNe() == 0) {
      throw new CardException("Not implemented.");
    }
    
    if (currentFile.kid != -1) {
      if ((currentFile.kid & 0x80) > 0) {
        PIN pin;
        if (currentAppl == null
            || (pin = currentAppl.pins.get(currentFile.kid)) == null
            || pin.state != PIN.STATE_PIN_VERIFIED) {
          return new ResponseAPDU(new byte[] {(byte) 0x69, (byte) 0x82});
        }
      } else {
        // Global PINs not implemented
        throw new CardException("Not implemented.");
      }
    }

    int len;
    if (command.getNe() == 256) {
      if (currentFile.file.length > 256) {
        return new ResponseAPDU(new byte[] {(byte) 0x67, (byte) 0x00});
      } else {
        len = Math.min(command.getNe(), currentFile.file.length - offset);
      }
    } else {
      if (command.getNe() >= currentFile.file.length - offset) {
        return new ResponseAPDU(new byte[] {(byte) 0x62, (byte) 0x82});
      } else {
        len = command.getNe();
      }
    }
    
    byte[] response = new byte[len + 2];
    System.arraycopy(currentFile.file, offset, response, 0, len);
    response[len] = (byte) 0x90;
    response[len + 1] = (byte) 0x00;
    return new ResponseAPDU(response);
    
  }


}
