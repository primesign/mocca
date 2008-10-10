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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package at.gv.egiz.smcc;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
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
import java.util.Enumeration;
import java.util.Locale;

import javax.smartcardio.Card;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author mcentner
 */
public class SWCard implements SignatureCard {
  
  private static final String BKU_USER_DIR = ".bku";

  private static final String SWCARD_DIR = "smcc";
  
  private static final String KEYSTORE_CERTIFIED_KEYPAIR = "certified.p12";
  
  private static final String CERTIFICATE_CERTIFIED_KEYPAIR = "certified.cer";
  
  private static final String KEYSTORE_SECURE_KEYPAIR = "secure.p12";
  
  private static final String CERTIFICATE_SECURE_KEYPAIR = "secure.cer";
  
  private static String swCardDir;

  private static Log log = LogFactory.getLog(SWCard.class);

  private KeyStore certifiedKeyStore;
  
  private KeyStore secureKeyStore;
  
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

  public void init(Card card) {
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
      keyStore.load(keyStoreFile, null);
    } catch (Exception e) {
      String msg = "Failed to load KeyStore from file '" + fileName + "'.";
      log.info(msg, e);
      throw new SignatureCardException(msg, e);
    } 
    
    return keyStore;

  
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

  public byte[] createSignature(byte[] hash, KeyboxName keyboxName, PINProvider provider) throws SignatureCardException {

    // KeyStore password
    PINSpec pinSpec = new PINSpec(0, -1, ".", "KeyStore-Password");
    
    KeyStore keyStore = getKeyStore(keyboxName, null);

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
              String pin = provider.providePIN(pinSpec, -1);
              key = keyStore.getKey(alias, pin.toCharArray());
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
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void disconnect(boolean reset) {    
  }

}
