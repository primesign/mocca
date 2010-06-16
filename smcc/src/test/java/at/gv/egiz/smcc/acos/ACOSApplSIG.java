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

import java.util.Random;

import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

import at.gv.egiz.smcc.CardChannelEmul;
import at.gv.egiz.smcc.PIN;

public abstract class ACOSApplSIG extends ACOSAppl {

  protected static final int KID_PIN_SIG = 0x81;

  @Override
  public ResponseAPDU cmdPERFORM_SECURITY_OPERATION(CommandAPDU command, CardChannelEmul channel) {
    
    checkINS(command, 0x2A);
  
    if (command.getP1() == 0x90 && command.getP2() == 0x81) {
      
      // PUT HASH
      hash = command.getData();
      return new ResponseAPDU(new byte[] {(byte) 0x90, (byte) 0x00});
      
    } else if (command.getP1() == 0x9E && command.getP2() == 0x9A) {
      
      // COMPUTE DIGITAL SIGNATURE
      if (securityEnv == null) {
        // No security environment
        return new ResponseAPDU(new byte[] {(byte) 0x6F, (byte) 0x05});
      }
      if (hash == null) {
        // Command sequence not correct
        return new ResponseAPDU(new byte[] {(byte) 0x6F, (byte) 0x03});
      }
      if (hash.length != 20) {
        // Invalid hash length
        return new ResponseAPDU(new byte[] {(byte) 0x6A, (byte) 0x80});
      }
      if (pins.get(KID_PIN_SIG).state != PIN.STATE_PIN_VERIFIED) {
        // Security Status not satisfied
        return new ResponseAPDU(new byte[] {(byte) 0x69, (byte) 0x82});
      }
      
      byte[] signature = new byte[48]; 
      
      // TODO replace by signature creation
      Random random = new Random();
      random.nextBytes(signature);
      
      byte[] response = new byte[signature.length + 2];
      System.arraycopy(signature, 0, response, 0, signature.length);
      response[signature.length] = (byte) 0x90;
      response[signature.length + 1] = (byte) 0x00;
      
      hash = null;
      pins.get(KID_PIN_SIG).state = PIN.STATE_RESET;
      
      return new ResponseAPDU(response);
      
    } else {
      return new ResponseAPDU(new byte[] {(byte) 0x6A, (byte) 0x00});
    }
    
  }
  
}