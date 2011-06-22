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


package at.gv.egiz.bku.pin.gui;

import at.gv.egiz.bku.gui.BKUGUIFacade;
import at.gv.egiz.smcc.CancelledException;
import at.gv.egiz.smcc.PinInfo;
import at.gv.egiz.smcc.pin.gui.PINGUI;

/**
 * The number of retries is not fixed and there is no way (?) to obtain this value.
 * A PINProvider should therefore maintain an internal retry counter or flag
 * to decide whether or not to warn the user (num retries passed in providePIN).
 *
 * Therefore PINProvider objects should not be reused.
 *
 * (ACOS: reload counter: between 0 and 15, where 15 meens deactivated)
 *
 * @author Clemens Orthacker <clemens.orthacker@iaik.tugraz.at>
 */
public class VerifyPINGUI extends VerifyPINProvider implements PINGUI {

  private boolean retry = false;

  public VerifyPINGUI(BKUGUIFacade gui) {
    super(gui);
  }

  @Override
  public void enterPINDirect(PinInfo pinInfo, int retries)
          throws CancelledException, InterruptedException {    
    gui.showEnterPINDirect(pinInfo, (retry) ? retries : -1);
    retry = true;
  }

  @Override
  public void enterPIN(PinInfo pinInfo, int retries) {
    gui.showEnterPIN(pinInfo, (retry) ? retries : -1);
    retry = true;
  }


  @Override
  public void validKeyPressed() {
    gui.validKeyPressed();
  }

  @Override
  public void correctionButtonPressed() {
    gui.correctionButtonPressed();
  }

  @Override
  public void allKeysCleared() {
    gui.allKeysCleared();
  }

}
