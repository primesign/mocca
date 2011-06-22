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
