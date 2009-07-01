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

import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;


public abstract class AbstractAppl implements CardAppl {
  
  public final HashMap<Integer, PIN> pins = new HashMap<Integer, PIN>();

  protected List<File> files = new ArrayList<File>();

  public void checkINS(CommandAPDU command, int ins) {
    if (command.getINS() != ins) {
      throw new IllegalArgumentException("INS has to be 0x" + Integer.toHexString(ins) + ".");
    }
  }

  @Override
  public abstract byte[] getAID();

  @Override
  public abstract byte[] getFCI();
  
  public void putFile(File file) {
    files.add(file);
  }

  public List<File> getFiles() {
    return files;
  }

  public abstract void setPin(int kid, char[] value);
  
}