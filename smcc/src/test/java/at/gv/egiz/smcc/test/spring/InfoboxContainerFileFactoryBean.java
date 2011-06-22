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



package at.gv.egiz.smcc.test.spring;

import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import org.springframework.beans.factory.FactoryBean;

import at.gv.egiz.smcc.File;

public class InfoboxContainerFileFactoryBean implements FactoryBean {

  private byte[] fid;
  
  private byte[] fcx;
  
  private int kid = -1;
  
  private PublicKey publicKey;
  
  private boolean container;
  
  private byte[] identityLink;
  
  private int fileSize;
  
  /**
   * @return the container
   */
  public boolean isContainer() {
    return container;
  }

  /**
   * @param container the container to set
   */
  public void setContainer(boolean container) {
    this.container = container;
  }

  /**
   * @return the identityLink
   */
  public byte[] getIdentityLink() {
    return identityLink;
  }

  /**
   * @param identityLink the identityLink to set
   */
  public void setIdentityLink(byte[] identityLink) {
    this.identityLink = identityLink;
  }

  /**
   * @return the fid
   */
  public byte[] getFid() {
    return fid;
  }

  /**
   * @param fid the fid to set
   */
  public void setFid(byte[] fid) {
    this.fid = fid;
  }

  /**
   * @return the fcx
   */
  public byte[] getFcx() {
    return fcx;
  }

  /**
   * @param fcx the fcx to set
   */
  public void setFcx(byte[] fcx) {
    this.fcx = fcx;
  }

  /**
   * @return the kid
   */
  public int getKid() {
    return kid;
  }

  /**
   * @param kid the kid to set
   */
  public void setKid(int kid) {
    this.kid = kid;
  }

  /**
   * @return the publicKey
   */
  public PublicKey getPublicKey() {
    return publicKey;
  }

  /**
   * @param publicKey the publicKey to set
   */
  public void setPublicKey(PublicKey publicKey) {
    this.publicKey = publicKey;
  }

  /**
   * @return the fileSize
   */
  public int getFileSize() {
    return fileSize;
  }

  /**
   * @param fileSize the fileSize to set
   */
  public void setFileSize(int fileSize) {
    this.fileSize = fileSize;
  }

  @Override
  public Object getObject() throws Exception {

    byte[] file = new byte[fileSize];
    
    if (container) {
      
      int offset = 0;
      
      // HEADER 'AIK' + version
      byte[] header = "AIK".getBytes(Charset.forName("ASCII"));
      System.arraycopy(header, 0, file, offset, header.length);
      offset += header.length;
      file[offset++] = 1; 
      
      // HEADER identity link
      file[offset++] = (byte) 0x01; // Personenbindung
      if (publicKey != null) {
        file[offset++] = (byte) 0x01; // Modifier

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
          cipherText = cipher.doFinal(identityLink);
          
          cipher = Cipher.getInstance(publicKey.getAlgorithm());
          cipher.init(Cipher.ENCRYPT_MODE, publicKey);
          encKey = cipher.doFinal(keyBytes);
          
        } catch (GeneralSecurityException e) {
          throw new RuntimeException(e);
        }
        
        int len = encKey.length + cipherText.length + 2;
        
        file[offset++] = (byte) (0xFF & len);
        file[offset++] = (byte) (0xFF & len >> 8);
        
        file[offset++] = (byte) (0xFF & encKey.length);
        file[offset++] = (byte) (0xFF & encKey.length >> 8);
        
        System.arraycopy(encKey, 0, file, offset, encKey.length);
        offset += encKey.length;
        
        System.arraycopy(cipherText, 0, file, offset, cipherText.length);
        
      } else {
        file[offset++] = (byte) 0x00; // Modifier
        file[offset++] = (byte) (0xFF & identityLink.length);
        file[offset++] = (byte) (0xFF & identityLink.length >> 8);
        System.arraycopy(identityLink, 0, file, offset, identityLink.length);
        offset += identityLink.length;
      }

    } else if (identityLink != null) {
      System.arraycopy(identityLink, 0, file, 0, Math.min(identityLink.length, file.length));
    }
    
    File f = new File();
    f.setFile(file);
    f.setFid(fid);
    f.setFcx(fcx);
    f.setKid(kid);
    
    return f;
   
  }

  @Override
  public Class<?> getObjectType() {
    return File.class;
  }

  @Override
  public boolean isSingleton() {
    return false;
  }

}
