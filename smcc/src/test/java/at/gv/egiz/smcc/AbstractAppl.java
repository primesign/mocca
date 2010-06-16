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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.smartcardio.CommandAPDU;


public abstract class AbstractAppl implements CardAppl {
  
  /**
   * The Application Identifier.
   */
  private byte[] aid;
  
  /**
   * The File Identifier.
   */
  private byte[] fid;
  
  /**
   * The File Control (Information / Parameter)
   */
  private byte[] fcx;
  
  /**
   * The PINs used by this Application.
   */
  public HashMap<Integer, PIN> pins = new HashMap<Integer, PIN>();

  /**
   * The Files used by this Application.
   */
  protected List<File> files = new ArrayList<File>();

  public void checkINS(CommandAPDU command, int ins) {
    if (command.getINS() != ins) {
      throw new IllegalArgumentException("INS has to be 0x" + Integer.toHexString(ins) + ".");
    }
  }

  public void setAid(byte[] aID) {
    aid = aID;
  }

  @Override
  public byte[] getAID() {
    return aid;
  }

  public void setFid(byte[] fid) {
    this.fid = fid;
  }

  @Override
  public byte[] getFID() {
    return fid;
  }

  public void setFcx(byte[] fcx) {
    this.fcx = fcx;
  }

  @Override
  public byte[] getFCX() {
    return fcx;
  }
  
  /**
   * @return the pins
   */
  public HashMap<Integer, PIN> getPins() {
    return pins;
  }

  /**
   * @param pins the pins to set
   */
  public void setPins(HashMap<Integer, PIN> pins) {
    this.pins = pins;
  }

  /**
   * @param files the files to set
   */
  public void setFiles(List<File> files) {
    this.files = files;
  }

  public void putFile(File file) {
    files.add(file);
  }

  public List<File> getFiles() {
    return files;
  }

  @Override
  public abstract void setPin(int kid, char[] value);
  
}