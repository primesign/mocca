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

package at.gv.egiz.smcc;

import at.gv.egiz.smcc.ccid.CCID;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import java.util.Map;
import javax.smartcardio.Card;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author mcentner
 */
public class SWCard implements SignatureCard {
  
  private static final String BKU_USER_DIR = ".mocca";

  private static final String SWCARD_DIR = "smcc";
  
  private static final String KEYSTORE_CERTIFIED_KEYPAIR = "certified.p12";
  
  private static final String KEYSTORE_PASSWORD_CERTIFIED_KEYPAIR = "certified.pwd";
  
  private static final String CERTIFICATE_CERTIFIED_KEYPAIR = "certified.cer";
  
  private static final String KEYSTORE_SECURE_KEYPAIR = "secure.p12";
  
  private static final String KEYSTORE_PASSWORD_SECURE_KEYPAIR = "secure.pwd";

  private static final String CERTIFICATE_SECURE_KEYPAIR = "secure.cer";
  
  private static String swCardDir;

  private static Log log = LogFactory.getLog(SWCard.class);

  private KeyStore certifiedKeyStore;
  
  private char[] certifiedKeyStorePassword;
  
  private KeyStore secureKeyStore;
  
  private char[] secureKeyStorePassword;
  
  private Certificate certifiedCertificate;
  
  private Certificate secureCertificate;
  
  static {
    String userHome = System.getProperty("user.home");
    String fs = System.getProperty("file.separator");
    swCardDir = userHome + fs + BKU_USER_DIR + fs + SWCARD_DIR;
  }

  /**
   * @return the swCardDir
   */
  public static String getSwCardDir() {
    return swCardDir;
  }

  /**
   * @param swCardDir the swCardDir to set
   */
  public static void setSwCardDir(String swCardDir) {
    SWCard.swCardDir = swCardDir;
  }

  public void init(Card card, CardTerminal cardTerminal) {
  }
  
  @Override
  public Card getCard() {
    return null;
  }

  private String getFileName(String fileName) {
    String fs = System.getProperty("file.separator");
    return swCardDir + fs + fileName;
  }
  
  private Certificate loadCertificate(String certificateFileName) throws SignatureCardException {
    
    final String certificateType = "x509";
    CertificateFactory factory;
    try {
      factory = CertificateFactory.getInstance(certificateType);
    } catch (CertificateException e) {
      String msg = "Failed to get CertificateFactory instance for type '" + certificateType + "'.";
      log.error(msg, e);
      throw new SignatureCardException(msg, e);
    }
    
    // try to load Certificate file
    String fileName = getFileName(certificateFileName);
    log.info("Trying to load Certificate from file '" + fileName + "'.");
    
    FileInputStream certificateFile;
    try {
      certificateFile = new FileInputStream(fileName);
    } catch (FileNotFoundException e) {
      String msg = "Certificate file '" + fileName + "' not found.";
      log.info(msg, e);
      throw new SignatureCardException(msg, e);
    }
    
    Certificate certificate;
    try {
      certificate = factory.generateCertificate(certificateFile);
    } catch (CertificateException e) {
      String msg = "Failed to load Certificate from file '" + fileName + "'.";
      log.info(msg, e);
      throw new SignatureCardException(msg, e);
    }
    
    return certificate;
    
  }
  
  private KeyStore loadKeyStore(String keyStoreFileName, char[] password) throws SignatureCardException {
    
    final String keyStoreType = "pkcs12";
    KeyStore keyStore;
    try {
      keyStore = KeyStore.getInstance(keyStoreType);
    } catch (KeyStoreException e) {
      String msg = "Failed to get KeyStore instance for KeyStore type '" + keyStoreType + "'.";
      log.error(msg, e);
      throw new SignatureCardException(msg, e);
    }

    // try to load KeyStore file
    String fileName = getFileName(keyStoreFileName);
    log.info("Trying to load KeyStore from file '" + fileName + "'.");
    
    FileInputStream keyStoreFile;
    try {
      keyStoreFile = new FileInputStream(fileName);
    } catch (FileNotFoundException e) {
      String msg = "KeyStore file '"+ fileName + "' not found.";
      log.info(msg, e);
      throw new SignatureCardException(msg, e);
    }
    
    try {
      keyStore.load(keyStoreFile, password);
    } catch (Exception e) {
      String msg = "Failed to load KeyStore from file '" + fileName + "'.";
      log.info(msg, e);
      throw new SignatureCardException(msg, e);
    } 
    
    return keyStore;
  
  }
  
  private char[] loadKeyStorePassword(String passwordFileName) throws SignatureCardException {

    String fileName = getFileName(passwordFileName);
    FileInputStream keyStorePasswordFile;
    try {
      keyStorePasswordFile = new FileInputStream(fileName);
    } catch (FileNotFoundException e) {
      return null;
    }
    
    try {
      InputStreamReader reader = new InputStreamReader(keyStorePasswordFile, Charset.forName("UTF-8"));
      StringBuilder sb = new StringBuilder();
      char b[] = new char[16];
      for (int l; (l = reader.read(b)) != -1;) {
        sb.append(b, 0, l);
      }
      return sb.toString().toCharArray();
    } catch (IOException e) {
      throw new SignatureCardException("Failed to read file '" + passwordFileName + "'.");
    }
    
  }
  
  private KeyStore getKeyStore(KeyboxName keyboxName, char[] password) throws SignatureCardException {
    
    if (keyboxName == KeyboxName.CERITIFIED_KEYPAIR) {
      if (certifiedKeyStore == null) {
        certifiedKeyStore = loadKeyStore(KEYSTORE_CERTIFIED_KEYPAIR, password);
      }
      return certifiedKeyStore;
    } else if (keyboxName == KeyboxName.SECURE_SIGNATURE_KEYPAIR) {
      if (secureKeyStore == null) {
        secureKeyStore = loadKeyStore(KEYSTORE_SECURE_KEYPAIR, password);
      }
      return secureKeyStore;
    } else {
      throw new SignatureCardException("Keybox of type '" + keyboxName + "' not supported.");
    }
    
  }
  
  private char[] getPassword(KeyboxName keyboxName) throws SignatureCardException {
    
    if (keyboxName == KeyboxName.CERITIFIED_KEYPAIR) {
      if (certifiedKeyStorePassword == null) {
        certifiedKeyStorePassword = loadKeyStorePassword(KEYSTORE_PASSWORD_CERTIFIED_KEYPAIR);
      }
      return certifiedKeyStorePassword;
    } else if (keyboxName == KeyboxName.SECURE_SIGNATURE_KEYPAIR) {
      if (secureKeyStorePassword == null) {
        secureKeyStorePassword = loadKeyStorePassword(KEYSTORE_PASSWORD_SECURE_KEYPAIR);
      }
      return secureKeyStorePassword;
    } else {
      throw new SignatureCardException("Keybox of type '" + keyboxName + "' not supported.");
    }
    
  }
  
  public byte[] getCertificate(KeyboxName keyboxName)
      throws SignatureCardException {

    try {
      if (keyboxName == KeyboxName.CERITIFIED_KEYPAIR) {
        if (certifiedCertificate == null) {
          certifiedCertificate = loadCertificate(CERTIFICATE_CERTIFIED_KEYPAIR);
        }
        return certifiedCertificate.getEncoded();
      } else if (keyboxName == KeyboxName.SECURE_SIGNATURE_KEYPAIR) {
        if (secureCertificate == null) {
          secureCertificate = loadCertificate(CERTIFICATE_SECURE_KEYPAIR);
        }
        return secureCertificate.getEncoded();
      } else {
        throw new SignatureCardException("Keybox of type '" + keyboxName + "' not supported.");
      }
    } catch (CertificateEncodingException e) {
      throw new SignatureCardException("Failed to get encoded Certificate.", e);
    }

    
  }

  public byte[] getInfobox(String infobox, PINProvider provider, String domainId) throws SignatureCardException {
    
    String fileName = getFileName(infobox + ".ibx");
    FileInputStream file;
    try {
      file = new FileInputStream(fileName);
    } catch (FileNotFoundException e) {
      String msg = "Infobox '" + infobox + "' not found.";
      log.info(msg, e);
      throw new SignatureCardException(msg, e);
    }
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    try {
      byte[] b = new byte[512];
      for(int l; (l = file.read(b)) != -1;) {
        bytes.write(b, 0, l);
      }
      file.close();
    } catch (IOException e) {
      String msg = "Failed to read infobox '" + infobox + "'.";
      log.error(msg, e);
      throw new SignatureCardException(msg, e);
    }
    
    return bytes.toByteArray();
    
  }

  @Override
  public byte[] createSignature(byte[] hash, KeyboxName keyboxName, PINProvider provider) throws SignatureCardException, InterruptedException {

    // KeyStore password
    char[] password = getPassword(keyboxName);

    if (password == null) {

      PINSpec pinSpec = new PINSpec(0, -1, ".", "KeyStore-Password", (byte) 0x01, null);
      
      password = provider.providePIN(pinSpec, -1);
      
      if (password == null) {
        return null;
      }
      
    }

    KeyStore keyStore = getKeyStore(keyboxName, password);

    PrivateKey privateKey = null;
    
    try {
      for (Enumeration<String> aliases = keyStore.aliases(); aliases
          .hasMoreElements() && privateKey == null;) {
        String alias = aliases.nextElement();
        log.debug("Found alias '" + alias + "' in keystore");
        if (keyStore.isKeyEntry(alias)) {
          Key key = null;
          while (key == null) {
            try {
              key = keyStore.getKey(alias, password);
            } catch (UnrecoverableKeyException e) {
              log.info("Failed to get Key from KeyStore. Wrong password?", e);
            }
          }
          privateKey = (PrivateKey) key;
        }
      }
    } catch (Exception e) {
      String msg = "Failed to get certificate from KeyStore.";
      log.info(msg, e);
      throw new SignatureCardException(msg, e);
    }

    if (privateKey == null) {
      String msg = "No private key found in KeyStore.";
      log.info(msg);
      throw new SignatureCardException(msg);
    }

    String algorithm = privateKey.getAlgorithm();
    algorithm = "SHA1with" + algorithm;
    try {
      Signature signature = Signature.getInstance(algorithm);
      signature.initSign(privateKey);
      signature.update(hash);
      return signature.sign();
    } catch (NoSuchAlgorithmException e) {
      String msg = "Algorithm + '" + algorithm + "' not supported for signing.";
      log.info(msg, e);
      throw new SignatureCardException(msg, e);
    } catch (SignatureException e) {
      String msg = "Signing faild.";
      log.info(msg, e);
      throw new SignatureCardException(msg, e);
    } catch (InvalidKeyException e) {
      String msg = "Key not valid for algorithm + '" + algorithm + "'.";
      log.info(msg, e);
      throw new SignatureCardException(msg, e);
    }
    
  }

  @Override
  public void setLocale(Locale locale) {
  }

  @Override
  public void disconnect(boolean reset) {    
  }

  @Override
  public void reset() throws SignatureCardException {
  }

  @Override
  public List<PINSpec> getPINSpecs() {
    return new ArrayList<PINSpec>();
  }

  @Override
  public void verifyPIN(PINSpec pinSpec, PINProvider pinProvider)
          throws LockedException, NotActivatedException, SignatureCardException {
  }

  @Override
  public void activatePIN(PINSpec pinSpec, PINProvider pinProvider) throws SignatureCardException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void unblockPIN(PINSpec pinSpec, PINProvider pukProvider) throws CancelledException, SignatureCardException, InterruptedException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void changePIN(PINSpec pinSpec, ChangePINProvider pinProvider) throws LockedException, NotActivatedException, CancelledException, SignatureCardException, InterruptedException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public CCID getReader() {
    return new CCID() {

      @Override
      public boolean hasFeature(Byte feature) {
        return false;
      }

      @Override
      public byte getbTimeOut() {
        return 0;
      }

      @Override
      public byte getbTimeOut2() {
        return 0;
      }

      @Override
      public byte getwPINMaxExtraDigitL() {
        return 0x12;
      }

      @Override
      public byte getwPINMaxExtraDigitH() {
        return 0x00;
      }

      @Override
      public byte getbEntryValidationCondition() {
        return 0x02;
      }

      @Override
      public Card connect() {
        return null;
      }

      @Override
      public String getName() {
        return "Software CardReader";
      }

      @Override
      public byte[] verifyPin(byte[] PIN_VERIFY) throws CardException {
        throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public byte[] verifyPinDirect(byte[] PIN_VERIFY) throws CardException {
        throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public byte[] modifyPin(byte[] PIN_MODIFY) throws CardException {
        throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public byte[] modifyPinDirect(byte[] PIN_MODIFY) throws CardException {
        throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public void setDisablePinpad(boolean disable) {
        throw new UnsupportedOperationException("Not supported yet.");
      }
    };
  }
}
