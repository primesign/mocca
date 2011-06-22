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



package at.gv.egiz.smcc;

import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.IvParameterSpec;


public class Infobox {

  private int tag;
  
  private byte[] data;
  
  private byte[] encryptedKey;
  
  public Infobox(int tag, int modifiers, byte[] data) throws InfoboxException {
    this.tag = tag;
    if ((modifiers & 0xFE) > 0) {
      throw new InfoboxException("Infobox modifiers " + Integer.toBinaryString(modifiers) + " not supported.");
    }
    if ((modifiers & 0x01) > 0) {
      int keyLenght = (0xFF & data[0]) + ((0xFF & data[1]) << 8);
      this.encryptedKey = new byte[keyLenght];
      System.arraycopy(data, 2, this.encryptedKey, 0, keyLenght);
      int dataLength = data.length - 2 - keyLenght;
      this.data = new byte[dataLength];
      System.arraycopy(data, 2 + encryptedKey.length, this.data, 0, dataLength);
    } else {
      this.data = data;
    }
  }
  
  public Infobox(byte[] data) {
    this.data = data;
  }
  
  public int getTag() {
    return tag;
  }

  public boolean isEncrypted() {
    return encryptedKey != null;
  }

  public byte[] getData() {
    return data;
  }
  
  public byte[] getEncryptedKey() {
    return encryptedKey;
  }
  
  public byte[] decipher(byte[] plainKey) throws InfoboxException {
    
    try {
      Cipher cipher = Cipher
          .getInstance("DESede/CBC/PKCS5Padding");
      byte[] iv = new byte[8];
      Arrays.fill(iv, (byte) 0x00);
      IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
      AlgorithmParameters parameters = AlgorithmParameters
          .getInstance("DESede");
      parameters.init(ivParameterSpec);

      DESedeKeySpec keySpec = new DESedeKeySpec(plainKey);
      SecretKeyFactory keyFactory = SecretKeyFactory
          .getInstance("DESede");
      SecretKey secretKey = keyFactory.generateSecret(keySpec);

      cipher.init(Cipher.DECRYPT_MODE, secretKey, parameters);

      return cipher.doFinal(data);

    } catch (GeneralSecurityException e) {
      throw new InfoboxException("Failed to decipher Infobox.", e);
    }
    
  }
  
  
}
