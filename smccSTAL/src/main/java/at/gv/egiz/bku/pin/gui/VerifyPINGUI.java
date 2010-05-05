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
