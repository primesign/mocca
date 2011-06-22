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


package at.gv.egiz.smcc.activation;

import iaik.security.provider.IAIK;
import iaik.utils.CryptoUtils;
import iaik.utils.Util;

import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Arrays;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import junit.framework.TestCase;

public class TestRetailCBCMac extends TestCase {
  
  
  final static String[] PLAIN_DATA = {
    "5A 10 D3 0E FC F6 5D 7F 89 FF B6 69 E4 8C 1C 88 C9 D3 AB "+
    "74 3D B0 2C DB 00 00 00 23 00 78 71 11 6C 3C FC 5F 53 6C 49 A9 F3 42 E0 "+
    "E1 32 EE AE 2F 21 79 06 C0 6E EE 69 1B B0 10 B7 54 53 FA 31 EB 92 D1 51 "+
    "0D 90 E3 FC E2 F6 1C 1E 15 68 0D A5 AB CA 5B E8 45 23 8D 87 7C 5F 7B F5 "+
    "A1 D8 89 ED 30",
    
    "5C AB 2E 55 2C 1D BD F1 9C F9 F6 B9 28 73 5B EC 11 2A 2B "+
    "FC E5 C8 C6 86 00 00 00 23 00 78 71 11 59 D0 0C 8B 7B 45 A2 1F 02 62 5D "+
    "F3 18 F8 F2 46 A5 14 99 60 77 55 25 2C 3D AF 19 C9 CC C1 C8 9B EA 39 A3 "+
    "32 7C 45 19 87 B1 8A 98 CA D7 E5 90 A7 3D 70 BE 74 36 E3 09 C5 F9 00 57 "+
    "82 AF 64 B9 7D",

    "E4 A9 1F F1 5A AB BF 2F E8 3D C5 AA 70 BF A4 48 EE 32 88 "+
    "4B 5A 0A B2 E7 00 00 00 23 00 78 71 11 8A B1 B3 1F D0 40 33 D8 0A 9C 21 "+
    "5C CB 47 63 69 2C EC D4 46 92 C7 07 1D EF B5 6D 28 89 68 36 BA E1 7D A4 "+
    "60 03 5A 2C 0D 53 EF 43 2D BA 09 30 CD F7 4A 8C 70 18 ED 57 EA C9 63 E6 "+
    "43 B5 6C 97 94",
    
    "FD A8 CA 31 D7 41 EA 1D 62 47 71 A8 C9 2F D8 3E 3C 3A F1 "+
    "D2 60 41 8F 2A 00 00 00 23 00 78 71 11 45 0D B9 4F AF A1 4D 0C 9B 62 FE "+
    "B9 7C 3F C0 B0 B0 C2 67 44 3F 5F 2B B5 EC 8A 0D D9 D6 33 9B A0 EC 97 C2 "+
    "40 FA A4 56 67 46 02 58 B1 0A AB 27 FE 25 38 23 21 74 F6 34 3C 81 D4 1A "+
    "17 DB C2 A2 E9",
    
    "FB 5E 02 2A 0C A0 F0 09 7F FF 93 05 D1 3D 14 B4 14 3A DC "+
    "21 A3 AB DE C6 00 00 00 23 00 78 71 11 84 21 A6 CC 84 8B 0A 07 64 2F B2 "+
    "A1 05 7B 08 9A FB CE 3B B6 A1 36 A8 03 0D 7B EA 2D 1E 7A D7 E2 C1 2D E8 "+
    "DF 82 CE 3F 43 D5 F6 21 DB D2 F7 31 5B A8 F2 65 E2 F4 E4 7E 1E 94 F4 E6 "+
    "28 14 01 CA 18",
  };
  
  final static String[] CRYPTO_GRAM = {
    "E6 5E D9 34 14 4D 4B 86 B5 40 FB 2A 06 29 44 2D 5F 41 14 "+
    "E5 95 A3 6D 07 B0 C4 6A 14 35 8D FE 72 C6 18 37 96 15 20 4B CE E7 A9 A1 "+
    "FD C6 85 3D F5 AE EA E4 92 95 0B EB 95 74 CA ED 38 E8 4B E0 FC 4C 55 1B "+
    "DE DE AD B3 13 7E ED AB EC B3 C2 FC A2 BE 72 A6 BE 50 D4 79 89 D1 70 A4 "+
    "4A 15 EC A0 D7",
    
    "D8 91 7C 06 60 DC 9B 74 28 2A 44 68 96 AF EE 93 AC D2 CF "+
    "DB CE 19 F6 73 73 F5 61 B0 AB 20 DF 63 F3 C7 4C 47 86 BD E0 7A 9B 04 64 "+
    "F9 87 2D F3 A6 FB 3F E5 B8 80 C5 F1 29 A9 0A 56 4E 7F 96 BE 30 88 FD 81 "+
    "86 7D 13 56 6E 17 4B 2A 31 36 D3 DA 24 FD 66 7D 21 B2 9E C9 2D 63 46 EF "+
    "97 06 E1 DA 15",

    "5A 4E F8 99 EE ED 02 1A A2 C8 A9 6B DD FB D6 CE 2A F0 5A "+
    "05 57 6A 79 66 3C B3 B7 CA 00 98 52 EE 35 72 AE 65 17 D4 0F A7 B3 20 F5 "+
    "25 A7 A5 7A 47 79 EB 65 FD DE 63 68 A5 C0 04 EF 3D DA 21 5E DB B7 83 FD "+
    "EF FD 52 28 91 D3 67 F4 9E 69 57 1C 19 08 5F 67 98 86 6E 99 2F 19 A7 54 "+
    "B2 CE E8 F1 C0",
    
    "AB D2 A6 ED C1 AA 2C ED 12 67 7E 38 B2 CB 5C 4E 06 A0 E0 "+
    "5A FC 59 59 11 25 8B 92 07 12 81 D3 FE 7E B8 4B 35 CD F6 A2 CD 98 C7 EF "+
    "FC EA 75 94 2C 55 6F 35 B5 4E 83 F8 82 7B B0 85 DD B5 8E B1 04 B2 F0 71 "+
    "79 42 FA A4 81 68 64 83 FB F8 5E 75 B3 C6 C8 CA 17 9C 94 45 EC A9 8A ED "+
    "73 58 F9 9B 97",
    
    "AE E7 4C D8 AA 2E EA C4 6C D9 19 48 3C 19 12 D0 EA E8 70 "+
    "00 39 2F AF FD 77 36 3B 87 AF ED 11 9E 54 74 F6 4B DA 68 32 12 D7 B0 76 "+
    "73 87 BE C3 74 08 0E DB 33 1E 66 AE 9E 23 56 DD DC 0D 61 FF 8B 15 4A 36 "+
    "4E BD E7 9F E5 3C 15 43 4F CC B2 7F FC 4B DD D6 39 17 EB FD 3D D6 11 45 "+
    "AA DD F0 2E 61",
    
  };
  
  final static String[] RETAIL_MAC = {
    "27 D0 4D E8 17 20 46 E8",
    
    "C2 31 EF 5A 99 AB FA 5F",

    "09 6C 40 EE A6 12 FA 1C",
    
    "55 FA 62 93 86 56 D0 38",
    
    "9D 86 EB 57 A2 58 F8 C8",
    
  };
  
 
  final static String[] CSK = {
    "54 3D 49 BF 31 51 25 94 67 8A 64 4F 1C B9 54 3D 31 46 43 79 E5 B9 A7 A7",
    "76 23 EC 10 91 61 7C 75 EF EF BF 9D 7F 7F 9E FD 07 1A 0E 6E 51 0B D3 D0",
    "76 23 EC 10 91 61 7C 75 EF EF BF 9D 7F 7F 9E FD 07 1A 0E 6E 51 0B D3 D0",
    "54 3D 49 BF 31 51 25 94 67 8A 64 4F 1C B9 54 3D 31 46 43 79 E5 B9 A7 A72",
    "10 57 9E F4 9D 34 4F B3 43 D0 62 92 45 D9 EF 45 16 2F CD 4F 4A D5 38 08"

  };
  
  

  public TestRetailCBCMac() {
    super();
  }

  public TestRetailCBCMac(String name) {
    super(name);
  }

  /* (non-Javadoc)
   * @see iaik.AbstractTest#setUp()
   */
  public void setUp() throws Exception {
    super.setUp();
    IAIK.addAsProvider();
  }
  

  
  public void testRetailMac() {
    String cbcCipherAlg = "DES";
    String cipherAlg = "DESede";
    int cbcCipherKeyLen = 8;
    int blockSize = 8;
    
    
    for (int i = 0; i < PLAIN_DATA.length; i++) {
      byte[] plainData = Util.toByteArray(PLAIN_DATA[i].trim());
      byte[] cryptoGram = Util.toByteArray(CRYPTO_GRAM[i].trim());
      byte[] retailMac = Util.toByteArray(RETAIL_MAC[i].trim());
      byte[] rawCsk = Util.toByteArray(CSK[i].trim());  
      byte[] calculatedMac = null;
      try {
        SecretKeySpec csk = new SecretKeySpec(rawCsk, cipherAlg);
        calculatedMac = retailMac(cryptoGram, cbcCipherAlg, cipherAlg, csk, cbcCipherKeyLen, blockSize);
        System.out.println("Expected Mac  : " + Util.toString(retailMac));
        System.out.println("Calculated Mac: " + Util.toString(calculatedMac));
      } catch (Exception e) {
        e.printStackTrace();
        fail("Unexpetced error: " + e.toString());
      }
      assertTrue("Different Mac values!", Arrays.equals(retailMac, calculatedMac));
    }
  }

//  public void testRetailMacDESTripleDES() {
//    testRetailMac("DES", "DESede", 8, 8);
//  }
  
  public void testRetailMac(String cbcCipherAlg, String cipherAlg, int cbcCipherKeyLen, int blockSize) {
    try {
      
      Random rand = new Random();
      KeyGenerator kg = KeyGenerator.getInstance(cipherAlg, "IAIK");
      SecretKey key = kg.generateKey();
      int[] dataLen = { 0, 3, 8, 15, 16, 20, 24, 31, 32, 39, 40, 48, 510, 512, 1111, 1024, 2000, 2048, 4003, 4096 };
      for (int i = 0; i < dataLen.length; i++) {
        int len = dataLen[i];
        byte[] data = new byte[len];
        rand.nextBytes(data);
        System.out.println("\n Data (" + len + " bytes):" + Util.toString(data));
        
        byte[] mac = retailMac(data, cbcCipherAlg, "DESede", key, cbcCipherKeyLen, blockSize);
        
        System.out.println("MAC: " + Util.toString(mac));
      }   
        

      
       
    } catch (Exception e) {
      fail("Unexpetced error: " + e.toString());
    }
  }
  
  /**
   * Calculates a Retail CBC Mac from the given message.
   * <p>
   * The retail CBC Mac is calculated according to the following 
   * algorithm:
   * <ul>
   *    <li>
   *       Pad the message to a multiple n of the CBC cipher block size with
   *       a leading one bit followed by as many zero bits as necessary.
   *    </li>
   *    <li>
   *       Create a CBC cipher key from the first <code>cbcCipherKeyLen</code>
   *       bytes of the <code>csk</code> key and use it to calculate a 
   *       CBC Mac value from the first n-1 blocks of the padded message.
   *       For CBC Mac calculation initialize the CBC Cipher with an
   *       IV of all zero bytes.
   *    </li>
   *    <li>
   *       XOR the last block of the padded message with the CBC mac value
   *       and calculate the final retail MAC by encrypting the XOR result
   *       with the given Cipher algorithm in ECB mode using no padding.
   *    </li>
   * </ul>
   * 
   * @param msg the message
   * @param cbcCipherAlg the name of the CBC Cipher algorithm to be used
   * @param cipherAlg the name of the final Cipher algorithm to be used
   * @param csk the secret key to be used
   * @param cbcCipherKeyLen the length of the CBC cipher key to be used
   * @param blockSize the block size of the CBC Cipher
   * 
   * @return the retail CBC Mac value
   * 
   * @throws NoSuchAlgorithmException if any of the requested Cipher algorithms
   *                                  is not available 
   * @throws NoSuchProviderException if the IAIK provider is not installed
   * @throws InvalidKeyException if the key cannot be used with the Ciphers 
   * @throws GeneralSecurityException if the Cipher operation(s) fails
   */
  static byte[] retailMac(byte[] msg, 
                          String cbcCipherAlg,
                          String cipherAlg,
                          SecretKey csk,
                          int cbcCipherKeyLen,
                          int blockSize) 
    throws NoSuchAlgorithmException, 
         NoSuchProviderException, 
         InvalidKeyException, 
         GeneralSecurityException {
    
    if (msg == null) {
      throw new NullPointerException("Message m must not be null!");
    }
    if (csk == null) {
      throw new NullPointerException("Key csk must not be null!");
    }
    
    // calculate key for CBC cipher
    byte[] rawCsk = csk.getEncoded();
    int cskLen = rawCsk.length;
    SecretKey cbcCipherKey;
    if (cskLen == cbcCipherKeyLen) {
      cbcCipherKey = csk;
    } else if (cskLen < cbcCipherKeyLen) {
      throw new InvalidKeyException("Key too short!");
    } else {
      byte[] rawCbcCipherKey = new byte[blockSize];
      System.arraycopy(rawCsk, 0, rawCbcCipherKey, 0, blockSize);
      cbcCipherKey = new SecretKeySpec(rawCbcCipherKey, cbcCipherAlg);
    }
    // if necessary pad message with zeros
    byte[] paddedMsg = pad(msg, blockSize);
    
    // calculate CBC Mac for the first n-1 blocks
    int n = paddedMsg.length;
    int n_1 = n - blockSize;
    byte[] cbcMac = cbcMac(paddedMsg, 0, n_1, cbcCipherKey, cbcCipherAlg, blockSize);

    // calculate retail mac
    byte[] xor = new byte[blockSize];
    CryptoUtils.xorBlock(paddedMsg, n_1, cbcMac, 0, xor, 0, blockSize);
    Cipher cipher = Cipher.getInstance(cipherAlg+"/ECB/NoPadding", "IAIK");
    cipher.init(Cipher.ENCRYPT_MODE, csk);
    byte[] retailMac = cipher.doFinal(xor);
    return retailMac;
  }
  
  /**
   * Calculates a simple CBC Mac from the given (already) padded message.
   * 
   * @param paddedMsg the (zero) padded message
   * @param off the start offset in the paddedMsg array
   * @param len the number of bytes to be processed, starting at <code>off</code>
   * @param key the Cipher key
   * @param cipherAlg the name of the CBC Cipher algorithm to be used
   * @param blockSize the block size of the CBC Cipher
   * 
   * @return the CBC Mac value
   * 
   * @throws NoSuchAlgorithmException if the requested Cipher algorithm
   *                                  is not available 
   * @throws NoSuchProviderException if the IAIK provider is not installed
   * @throws InvalidKeyException if the key cannot be used with the Ciphers 
   * @throws GeneralSecurityException if the Cipher operation fails
   */
  static byte[] cbcMac(byte[] paddedMsg,
                       int off,
                       int len,
                       SecretKey key, 
                       String cipherAlg, 
                       int blockSize) 
    throws NoSuchAlgorithmException, 
           NoSuchProviderException, 
           InvalidKeyException, 
           GeneralSecurityException {  
           
    if (paddedMsg == null) {
      throw new NullPointerException("Message must not be null!");
    }
    if (key == null) {
      throw new NullPointerException("Key csk must not be null!");
    }
    
   
    Cipher cbcCipher = Cipher.getInstance(cipherAlg+"/CBC/NoPadding", "IAIK");
    byte[] iv = new byte[blockSize];
    cbcCipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));
    int finOff = off;
    if (len > blockSize) {
      finOff = len - blockSize;
      cbcCipher.update(paddedMsg, 0, finOff);
    }
    byte[] mac = cbcCipher.doFinal(paddedMsg, finOff, blockSize);
    return mac;
  }
  
  /**
   * Pads the given message to a multiple of the given blocksize with
   * a leading one bit followed by as many zero bits as necessary
   * 
   * @param msg the message to be padded
   * @param blockSize the block size
   * 
   * @return the padded message
   */
  static byte[] pad(byte[] msg, int blockSize) {
    int paddingLen;
    byte[] paddedMsg;

    int msgLen = msg.length;
    if (msgLen == 0) {
      paddingLen = blockSize;
    } else {
      paddingLen = blockSize - msgLen % blockSize;
    }  
    if (paddingLen > 0) {
      paddedMsg = new byte[msgLen + paddingLen];
      System.arraycopy(msg, 0, paddedMsg, 0, msgLen);
      paddedMsg[msgLen] = (byte)0x80;
    } else {
      paddedMsg = msg;
    }
    return paddedMsg;
  }
  

  
  

}
