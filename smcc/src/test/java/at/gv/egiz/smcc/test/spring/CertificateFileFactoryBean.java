/*
* Copyright 2009 Federal Chancellery Austria and
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
