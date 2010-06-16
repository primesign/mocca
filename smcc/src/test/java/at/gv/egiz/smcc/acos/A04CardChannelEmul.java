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


public class A04CardChannelEmul extends ACOSCardChannelEmul {

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
    
    if (command.getNe() == 256 || command.getNe() <= currentFile.file.length - offset) {
      int len = Math.min(command.getNe(), currentFile.file.length - offset);
      byte[] response = new byte[len + 2];
      System.arraycopy(currentFile.file, offset, response, 0, len);
      response[len] = (byte) 0x90;
      response[len + 1] = (byte) 0x00;
      return new ResponseAPDU(response);
    } else if (command.getNe() >= currentFile.file.length - offset) {
      return new ResponseAPDU(new byte[] {(byte) 0x62, (byte) 0x82});
    } else {
      return new ResponseAPDU(new byte[] {(byte) 0x67, (byte) 0x00});
    }
    
  }


}
