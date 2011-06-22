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

public class File {
  
  public byte[] fid;
  public byte[] file;
  public byte[] fcx;
  public int kid = -1;
  
  public File() {
  }

  public File(byte[] fid, byte[] file, byte[] fcx) {
    this.fid = fid;
    this.file = file;
    this.fcx = fcx;
  }

  public File(byte[] fid, byte[] file, byte[] fcx, int kid) {
    this.fid = fid;
    this.file = file;
    this.fcx = fcx;
    this.kid = kid;
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
   * @return the file
   */
  public byte[] getFile() {
    return file;
  }

  /**
   * @param file the file to set
   */
  public void setFile(byte[] file) {
    this.file = file;
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

  
  
}