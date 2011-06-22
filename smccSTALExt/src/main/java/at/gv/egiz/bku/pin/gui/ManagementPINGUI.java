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
