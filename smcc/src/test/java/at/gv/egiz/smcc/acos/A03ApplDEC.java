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

import java.util.Arrays;
import java.util.Random;

import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

import at.gv.egiz.smcc.CardChannelEmul;
import at.gv.egiz.smcc.PIN;


public class A03ApplDEC extends ACOSApplDEC {
  
  @Override
  public ResponseAPDU cmdMANAGE_SECURITY_ENVIRONMENT(CommandAPDU command, CardChannelEmul channel) {

    checkINS(command, 0x22);

    switch (command.getP2()) {
    case 0xA4:
      switch (command.getP1()) {
      case 0x41: {
        // INTERNAL AUTHENTICATE
        byte[] dst = new byte[] { (byte) 0x84, (byte) 0x01, (byte) 0x88, (byte) 0x80, (byte) 0x01, (byte) 0x01 };
        if (Arrays.equals(dst, command.getData())) {
          securityEnv = command.getData();
          return new ResponseAPDU(new byte[] {(byte) 0x90, (byte) 0x00});
        } else {
          return new ResponseAPDU(new byte[] {(byte) 0x6F, (byte) 0x05});
        }
      }
      case 0x81:
        // EXTERNAL AUTHENTICATE
      }
    case 0xB6:
      switch (command.getP1()) {
      case 0x41:
        // PSO - COMPUTE DIGITAL SIGNATURE
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

  @Override
  public ResponseAPDU cmdPERFORM_SECURITY_OPERATION(CommandAPDU command, CardChannelEmul channel) {
    
    checkINS(command, 0x2A);

    return new ResponseAPDU(new byte[] {(byte) 0x6A, (byte) 0x00});
    
  }

  @Override
  public ResponseAPDU cmdINTERNAL_AUTHENTICATE(CommandAPDU command, CardChannelEmul channel) {
    
    checkINS(command, 0x88);
    
    if (command.getP1() == 0x10 && command.getP2() == 0x00) {
      
      byte[] data = command.getData();
      
      if (securityEnv == null) {
        // Security Environment not set or wrong
        return new ResponseAPDU(new byte[] {(byte) 0x6F, (byte) 0x05});
      }

      byte[] digestInfo = new byte[] {
          (byte) 0x30, (byte) 0x21, (byte) 0x30, (byte) 0x09, (byte) 0x06, (byte) 0x05, (byte) 0x2B, (byte) 0x0E, 
          (byte) 0x03, (byte) 0x02, (byte) 0x1A, (byte) 0x05, (byte) 0x00, (byte) 0x04, (byte) 0x14
      };
      
      if (data.length != 35 || !Arrays.equals(digestInfo, Arrays.copyOf(data, 15))) {
        return new ResponseAPDU(new byte[] {(byte) 0x67, (byte) 0x00});
      }
        

      if (pins.get(KID_PIN_DEC).state != PIN.STATE_PIN_VERIFIED) {
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
      pins.get(KID_PIN_DEC).state = PIN.STATE_RESET;

      return new ResponseAPDU(response);
        
      
    } else {
      return new ResponseAPDU(new byte[] {(byte) 0x6A, (byte) 0x81});
    }

  }

}
