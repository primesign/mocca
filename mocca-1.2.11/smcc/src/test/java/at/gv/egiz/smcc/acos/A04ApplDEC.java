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

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

import at.gv.egiz.smcc.CardChannelEmul;
import at.gv.egiz.smcc.File;
import at.gv.egiz.smcc.PIN;


@SuppressWarnings("restriction")
public class A04ApplDEC extends ACOSApplDEC {
  
  private static final byte[] SEC_ENV_INTERNAL_AUTHENTICATE = new byte[] { (byte) 0x84,
      (byte) 0x01, (byte) 0x88, (byte) 0x80, (byte) 0x01, (byte) 0x01 };
  
  private static final byte[] SEC_ENV_DECIPHER = new byte[] { (byte) 0x84,
      (byte) 0x01, (byte) 0x88, (byte) 0x80, (byte) 0x01, (byte) 0x02 };
  
  private static final RSAPrivateKey SK_CH_EKEY;
  
  private static final RSAPublicKey PK_CH_EKEY;
  
  static {
    try {
      KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
      gen.initialize(1536);
      KeyPair keyPair = gen.generateKeyPair();
      SK_CH_EKEY = (RSAPrivateKey) keyPair.getPrivate();
      PK_CH_EKEY = (RSAPublicKey) keyPair.getPublic();
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }
  
  public A04ApplDEC() {
    this(false);
  }

  public A04ApplDEC(boolean encrypt) {
    
    int offset = 0;
    
    // HEADER 'AIK' + version
    byte[] header;
    try {
      header = "AIK".getBytes("ASCII");
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
    System.arraycopy(header, 0, EF_INFOBOX, offset, header.length);
    offset += header.length;
    EF_INFOBOX[offset++] = 1; 
    
    // HEADER identity link
    EF_INFOBOX[offset++] = (byte) 0x01; // Personenbindung
    if (encrypt) {
      EF_INFOBOX[offset++] = (byte) 0x01; // Modifier

      byte[] cipherText;
      byte[] encKey;
      try {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("DESede");
        SecretKey secretKey = keyGenerator.generateKey();
        
        byte[] keyBytes = secretKey.getEncoded();
        
        Cipher cipher = Cipher.getInstance("DESede/CBC/PKCS5Padding");
        byte[] iv = new byte[8];
        Arrays.fill(iv, (byte) 0x00);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec);
        cipherText = cipher.doFinal(IDLINK);
        
        cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, PK_CH_EKEY);
        encKey = cipher.doFinal(keyBytes);
        
      } catch (GeneralSecurityException e) {
        throw new RuntimeException(e);
      }
      
      int len = encKey.length + cipherText.length + 2;
      
      EF_INFOBOX[offset++] = (byte) (0xFF & len);
      EF_INFOBOX[offset++] = (byte) (0xFF & len >> 8);
      
      EF_INFOBOX[offset++] = (byte) (0xFF & encKey.length);
      EF_INFOBOX[offset++] = (byte) (0xFF & encKey.length >> 8);
      
      System.arraycopy(encKey, 0, EF_INFOBOX, offset, encKey.length);
      offset += encKey.length;
      
      System.arraycopy(cipherText, 0, EF_INFOBOX, offset, cipherText.length);
      
    } else {
      EF_INFOBOX[offset++] = (byte) 0x00; // Modifier
      EF_INFOBOX[offset++] = (byte) (0xFF & IDLINK.length);
      EF_INFOBOX[offset++] = (byte) (0xFF & IDLINK.length >> 8);
      System.arraycopy(IDLINK, 0, EF_INFOBOX, offset, IDLINK.length);
      offset += IDLINK.length;
    }
    
    putFile(new File(FID_EF_INFOBOX, EF_INFOBOX, FCI_EF_INFOBOX));
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
        cipher.init(Cipher.DECRYPT_MODE, SK_CH_EKEY);
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
        Signature signature = Signature.getInstance("RSA");
        signature.initSign(SK_CH_EKEY);
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
