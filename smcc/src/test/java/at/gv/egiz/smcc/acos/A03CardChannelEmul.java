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


package at.gv.egiz.smcc.acos;


import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

import at.gv.egiz.smcc.PIN;


public class A03CardChannelEmul extends ACOSCardChannelEmul {

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
