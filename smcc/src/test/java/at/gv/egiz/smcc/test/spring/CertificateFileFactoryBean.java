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

import org.springframework.beans.factory.FactoryBean;

import at.gv.egiz.smcc.File;

public class CertificateFileFactoryBean implements FactoryBean {

  private byte[] fid;
  
  private byte[] fcx;
  
  private byte[] certificate;
  
  private int fileSize;
  
  /**
   * @return the certificate
   */
  public byte[] getCertificate() {
    return certificate;
  }

  /**
   * @param certificate the certificate to set
   */
  public void setCertificate(byte[] certificate) {
    this.certificate = certificate;
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
    
    if (certificate != null) {
      System.arraycopy(certificate, 0, file, 0, Math.min(certificate.length, file.length));
    }
    
    File f = new File();
    f.setFile(file);
    f.setFid(fid);
    f.setFcx(fcx);
    
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
