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