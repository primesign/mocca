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
import at.gv.egiz.bku.gui.PINManagementGUIFacade;
import at.gv.egiz.bku.gui.PINManagementGUIFacade.DIALOG;
import at.gv.egiz.smcc.CancelledException;
import at.gv.egiz.smcc.PinInfo;
import at.gv.egiz.smcc.pin.gui.ModifyPINGUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ManagementPINGUI extends ManagementPINProvider implements ModifyPINGUI {

  protected static final Logger log = LoggerFactory.getLogger(ManagementPINGUI.class);

  private boolean retry = false;

  public ManagementPINGUI(PINManagementGUIFacade gui, DIALOG type) {
    super(gui, type);
  }

  @Override
  public void modifyPINDirect(PinInfo spec, int retries)
          throws CancelledException, InterruptedException {    
    gui.showModifyPINDirect(type, spec, (retry) ? retries : -1);
    retry = true;
  }

  @Override
  public void finishDirect() {
    gui.showMessageDialog(BKUGUIFacade.TITLE_WAIT, BKUGUIFacade.MESSAGE_WAIT);
  }

  @Override
  public void enterCurrentPIN(PinInfo spec, int retries) {
    gui.showEnterCurrentPIN(type, spec, (retry) ? retries : -1);
    retry = true;
  }

  @Override
  public void enterNewPIN(PinInfo spec) {
    gui.showEnterNewPIN(type, spec);
    retry = true;
  }

  @Override
  public void confirmNewPIN(PinInfo spec) {
    gui.showConfirmNewPIN(type, spec);
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

  @Override
  public void finish() {
    gui.showMessageDialog(BKUGUIFacade.TITLE_WAIT, BKUGUIFacade.MESSAGE_WAIT);
  }
}
