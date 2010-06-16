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

import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;


public interface CardAppl {
  
  public byte[] getAID();
  
  public byte[] getFID();
  
  public byte[] getFCX();
  
  public void leaveApplContext();

  public ResponseAPDU cmdMANAGE_SECURITY_ENVIRONMENT(CommandAPDU command, CardChannelEmul channel) throws CardException;

  public ResponseAPDU cmdPERFORM_SECURITY_OPERATION(CommandAPDU command, CardChannelEmul channel) throws CardException;

  public ResponseAPDU cmdINTERNAL_AUTHENTICATE(CommandAPDU command, CardChannelEmul channel) throws CardException;

  public void setPin(int kid, char[] value);

}
