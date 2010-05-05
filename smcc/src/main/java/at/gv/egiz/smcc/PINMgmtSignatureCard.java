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

public interface PINMgmtSignatureCard extends SignatureCard {

  /**
   * PinInfo declares protected methods to be used from within card implementations.
   * DO NOT REFACTOR CARD INTERFACE AND IMPLEMENTATIONS TO SEPARATE PACKAGES
   * @throws SignatureCardException if the card is STARCOS G3 and not activated (G3 pin activation fails if card not active)
   */
  public PinInfo[] getPinInfos() throws SignatureCardException;

  public void verifyPIN(PinInfo pinInfo, PINGUI pinGUI)
  throws LockedException, NotActivatedException, CancelledException, SignatureCardException, InterruptedException;

  public void changePIN(PinInfo pinInfo, ModifyPINGUI changePINGUI)
  throws LockedException, NotActivatedException, CancelledException, PINFormatException, SignatureCardException, InterruptedException;

  public void activatePIN(PinInfo pinInfo, ModifyPINGUI activatePINGUI)
  throws CancelledException, SignatureCardException, InterruptedException;

  public void unblockPIN(PinInfo pinInfo, ModifyPINGUI pukGUI)
  throws CancelledException, SignatureCardException, InterruptedException;

}
