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


package at.gv.egiz.smcc.starcos;

import java.security.GeneralSecurityException;
import java.security.interfaces.RSAPrivateKey;
import java.util.Arrays;
import java.util.Random;

import javax.crypto.Cipher;
import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

import at.gv.egiz.smcc.CardChannelEmul;
import at.gv.egiz.smcc.PIN;

public class STARCOSApplGewoehnlicheSignatur extends STARCOSAppl {
  
  private static byte[] SE_DECIPHER = new byte[] {
    (byte) 0x84, (byte) 0x03, (byte) 0x80, (byte) 0x03, (byte) 0x00,
    (byte) 0x80, (byte) 0x01, (byte) 0x81};

  private RSAPrivateKey privateKey;
  
  protected byte[] dst;
  
  protected byte[] ct;

  public void setPrivateKey(RSAPrivateKey privateKey) {
    this.privateKey = privateKey;
  }

  public void setDst(byte[] dst) {
    this.dst = dst;
  }

  public void setCt(byte[] ct) {
    this.ct = ct;
  }

  @Override
  public ResponseAPDU cmdMANAGE_SECURITY_ENVIRONMENT(CommandAPDU command, CardChannelEmul channel) throws CardException {

    checkINS(command, 0x22);

    switch (command.getP2()) {
    case 0xA4:
      switch (command.getP1()) {
      case 0x41:
        // INTERNAL AUTHENTICATE
      case 0x81:
        // EXTERNAL AUTHENTICATE
      }
    case 0xAA:
      switch (command.getP1()) {
      case 0x41: 
        if (Arrays.equals(new byte[] {(byte) 0x80, (byte) 0x01, (byte) 0x10}, command.getData())) {
          return new ResponseAPDU(new byte[] {(byte) 0x90, (byte) 0x00});
        }
      default:
        return new ResponseAPDU(new byte[] {(byte) 0x6A, (byte) 0x80});
      }
    case 0xB6:
      switch (command.getP1()) {
      case 0x41: {
        // PSO - COMPUTE DIGITAL SIGNATURE
        if (Arrays.equals(dst, command.getData())) {
          securityEnv = command.getData();
          return new ResponseAPDU(new byte[] {(byte) 0x90, (byte) 0x00});
        } else {
          return new ResponseAPDU(new byte[] {(byte) 0x6A, (byte) 0x80});
        }
      }
      case 0x81:
        // PSO - VERIFY DGITAL SIGNATURE
      }
    case 0xB8:
      switch (command.getP1()) {
      case 0x41:
        // PSO - DECIPHER
        if (Arrays.equals(ct, command.getData())) {
          securityEnv = command.getData();
          return new ResponseAPDU(new byte[] {(byte) 0x90, (byte) 0x00});
        } else {
          return new ResponseAPDU(new byte[] {(byte) 0x6A, (byte) 0x80});
        }
      case 0x81:
        // PSO - ENCIPHER
      }
    default:
      return new ResponseAPDU(new byte[] {(byte) 0x6A, (byte) 0x81});
    }

    
  }

  @Override
  public ResponseAPDU cmdPERFORM_SECURITY_OPERATION(CommandAPDU command, CardChannelEmul channel) throws CardException {
    
    checkINS(command, 0x2A);
  
    if (command.getP1() == 0x90 && command.getP2() == 0xA0) {
      
      // HASH
      byte[] data = command.getData();
      if (data[0] == (byte) 0x90 && data[1] == (byte) 0x14) {
        hash = Arrays.copyOfRange(data, 2, data.length);
        return new ResponseAPDU(new byte[] {(byte) 0x90, (byte) 0x00});
      } else {
        throw new CardException("HASH command only supports complete hash.");
      }
      
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
      STARCOSCardChannelEmul c = (STARCOSCardChannelEmul) channel;
      if (c.globalPins.get(STARCOSCardChannelEmul.KID_PIN_Glob).state != PIN.STATE_PIN_VERIFIED) {
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
      
      return new ResponseAPDU(response);
    
    } else if (command.getP1() == 0x80 && command.getP2() == 0x86) {
      
      // DECIPHER
      if (!Arrays.equals(securityEnv, SE_DECIPHER)) {
        return new ResponseAPDU(new byte[] {(byte) 0x6F, (byte) 0x05});
      }

      byte[] data = command.getData();
      
      if (data.length != 193) {
        return new ResponseAPDU(new byte[] {(byte) 0x67, (byte) 0x00});
      }

      if (((STARCOSCardChannelEmul) channel).globalPins
          .get(STARCOSCardChannelEmul.KID_PIN_Glob).state != PIN.STATE_PIN_VERIFIED) {
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
  public void setPin(int kid, char[] value) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  
}