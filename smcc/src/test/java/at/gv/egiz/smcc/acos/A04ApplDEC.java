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

import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.Signature;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

import at.gv.egiz.smcc.CardChannelEmul;
import at.gv.egiz.smcc.PIN;


public class A04ApplDEC extends ACOSApplDEC {
  
  private static final byte[] SEC_ENV_INTERNAL_AUTHENTICATE = new byte[] { (byte) 0x84,
      (byte) 0x01, (byte) 0x88, (byte) 0x80, (byte) 0x01, (byte) 0x01 };
  
  private static final byte[] SEC_ENV_DECIPHER = new byte[] { (byte) 0x84,
      (byte) 0x01, (byte) 0x88, (byte) 0x80, (byte) 0x01, (byte) 0x02 };
  
  protected PrivateKey privateKey;

  public void setPrivateKey(PrivateKey privateKey) {
    this.privateKey = privateKey;
  }

  @Override
  public ResponseAPDU cmdMANAGE_SECURITY_ENVIRONMENT(CommandAPDU command, CardChannelEmul channel) {

    checkINS(command, 0x22);

    switch (command.getP2()) {
    case 0xA4:
      switch (command.getP1()) {
      case 0x41: {
        // INTERNAL AUTHENTICATE
        if (Arrays.equals(SEC_ENV_INTERNAL_AUTHENTICATE, command.getData())) {
          securityEnv = command.getData();
          return new ResponseAPDU(new byte[] {(byte) 0x90, (byte) 0x00});
        } else {
          return new ResponseAPDU(new byte[] {(byte) 0x69, (byte) 0x85});
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
        if (Arrays.equals(SEC_ENV_DECIPHER, command.getData())) {
          securityEnv = command.getData();
          return new ResponseAPDU(new byte[] {(byte) 0x90, (byte) 0x00});
        } else {
          return new ResponseAPDU(new byte[] {(byte) 0x69, (byte) 0x85});
        }
      case 0x81:
        // PSO � ENCIPHER
      }
    default:
      return new ResponseAPDU(new byte[] {(byte) 0x6A, (byte) 0x81});
    }

  }

  @Override
  public ResponseAPDU cmdPERFORM_SECURITY_OPERATION(CommandAPDU command, CardChannelEmul channel) throws CardException {
    
    checkINS(command, 0x2A);
    
    if (command.getP1() == 0x80 && command.getP2() == 0x86) {
      
      // DECIPHER
      
      byte[] data = command.getData();
      
      if (!Arrays.equals(securityEnv, SEC_ENV_DECIPHER)) {
        return new ResponseAPDU(new byte[] {(byte) 0x6F, (byte) 0x05});
      }
      
      if (data.length != 193) {
        return new ResponseAPDU(new byte[] {(byte) 0x67, (byte) 0x00});
      }
      
      if (pins.get(KID_PIN_DEC).state != PIN.STATE_PIN_VERIFIED) {
        // Security Status not satisfied
        return new ResponseAPDU(new byte[] {(byte) 0x69, (byte) 0x82});
      }

      byte[] cipherText = Arrays.copyOfRange(data, 1, data.length);
      
      byte[] plainText;
      try {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        plainText = cipher.doFinal(cipherText);
      } catch (GeneralSecurityException e) {
        throw new CardException(e);
      }
      
      byte[] response = new byte[plainText.length + 2];
      System.arraycopy(plainText, 0, response, 0, plainText.length);
      response[plainText.length] = (byte) 0x90;
      response[plainText.length + 1] = (byte) 0x00;
      
      return new ResponseAPDU(response);
      
    } else {
      return new ResponseAPDU(new byte[] {(byte) 0x6A, (byte) 0x00});
    }
    
  }

  @Override
  public ResponseAPDU cmdINTERNAL_AUTHENTICATE(CommandAPDU command, CardChannelEmul channel) throws CardException {
    
    checkINS(command, 0x88);
    
    if (command.getP1() == 0x10 && command.getP2() == 0x00) {
      
      byte[] data = command.getData();
      
      if (!Arrays.equals(securityEnv, SEC_ENV_INTERNAL_AUTHENTICATE)) {
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
      
      byte[] digest = Arrays.copyOfRange(data, 15, 35);
      
      byte[] sig;
      try {
        Signature signature = Signature.getInstance(privateKey.getAlgorithm());
        signature.initSign(privateKey);
        signature.update(digest);
        sig = signature.sign();
      } catch (GeneralSecurityException e) {
        throw new CardException(e);
      }
      
      byte[] response = new byte[sig.length + 2];
      System.arraycopy(sig, 0, response, 0, sig.length);
      response[sig.length] = (byte) 0x90;
      response[sig.length + 1] = (byte) 0x00;
      
      hash = null;
      pins.get(KID_PIN_DEC).state = PIN.STATE_RESET;

      return new ResponseAPDU(response);
        
      
    } else {
      return new ResponseAPDU(new byte[] {(byte) 0x6A, (byte) 0x81});
    }

  }

}
