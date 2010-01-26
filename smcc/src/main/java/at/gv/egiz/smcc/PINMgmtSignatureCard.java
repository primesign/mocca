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

import at.gv.egiz.smcc.pin.gui.ModifyPINGUI;

import at.gv.egiz.smcc.pin.gui.PINGUI;
import java.util.List;

public interface PINMgmtSignatureCard extends SignatureCard {

  public enum PIN_STATE {UNKNOWN, ACTIV, NOT_ACTIV, BLOCKED};
  
  public List<PINSpec> getPINSpecs();

  public PIN_STATE getPINState(PINSpec pinSpec) throws SignatureCardException;
  
  public void verifyPIN(PINSpec pinSpec, PINGUI pinGUI)
  throws LockedException, NotActivatedException, CancelledException, SignatureCardException, InterruptedException;

  public void changePIN(PINSpec pinSpec, ModifyPINGUI changePINGUI)
  throws LockedException, NotActivatedException, CancelledException, PINFormatException, SignatureCardException, InterruptedException;

  public void activatePIN(PINSpec pinSpec, ModifyPINGUI activatePINGUI)
  throws CancelledException, SignatureCardException, InterruptedException;

  public void unblockPIN(PINSpec pinSpec, ModifyPINGUI pukGUI)
  throws CancelledException, SignatureCardException, InterruptedException;

}
