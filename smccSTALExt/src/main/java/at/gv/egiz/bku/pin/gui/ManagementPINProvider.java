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
import at.gv.egiz.smcc.CancelledException;
import at.gv.egiz.smcc.PinInfo;
import at.gv.egiz.smcc.pin.gui.ModifyPINProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ManagementPINProvider extends AbstractPINProvider implements ModifyPINProvider {

  private final Logger log = LoggerFactory.getLogger(ManagementPINProvider.class);
  protected PINManagementGUIFacade gui;
  protected PINManagementGUIFacade.DIALOG type;
  private boolean retry = false;

  public ManagementPINProvider(PINManagementGUIFacade gui, PINManagementGUIFacade.DIALOG type) {
    this.gui = gui;
    this.type = type;
  }

  @Override
  public char[] provideCurrentPIN(PinInfo spec, int retries)
          throws CancelledException, InterruptedException {

    gui.showPINDialog(type, spec, (retry) ? retries : -1,
            this, "change",
            this, "cancel");

    log.trace("[{}] wait for action.", Thread.currentThread().getName());
    waitForAction();
    log.trace("[{}] received action {}.", Thread.currentThread().getName(), action);

    gui.showMessageDialog(BKUGUIFacade.TITLE_WAIT,
            BKUGUIFacade.MESSAGE_WAIT);

    if ("cancel".equals(action)) {
      throw new CancelledException(spec.getLocalizedName() +
              " entry cancelled");
    }
    retry = true;
    return gui.getOldPin();
  }

  @Override
  public char[] provideNewPIN(PinInfo spec)
          throws CancelledException, InterruptedException {
    
    char[] pin = gui.getPin();
    if (pin != null) {
      // change pin dialog also returns new pin
      return pin;
    }

    gui.showPINDialog(type, spec, -1,
            this, "activate",
            this, "cancel");

    log.trace("[{}] wait for action.", Thread.currentThread().getName());
    waitForAction();
    log.trace("[{}] received action {}.", Thread.currentThread().getName(), action);

    gui.showMessageDialog(BKUGUIFacade.TITLE_WAIT,
              BKUGUIFacade.MESSAGE_WAIT);

    if ("cancel".equals(action)) {
      throw new CancelledException(spec.getLocalizedName() +
              " entry cancelled");
    }
    return gui.getPin();
  }

@Override
public char[] providePUK(PinInfo pinInfo, PinInfo pukInfo, int retries)
		throws CancelledException, InterruptedException {
	gui.showPUKDialog(type, pinInfo, pukInfo, (retry) ? retries : -1,
            this, "change",
            this, "cancel");

    log.trace("[{}] wait for action.", Thread.currentThread().getName());
    waitForAction();
    log.trace("[{}] received action {}.", Thread.currentThread().getName(), action);

    gui.showMessageDialog(BKUGUIFacade.TITLE_WAIT,
            BKUGUIFacade.MESSAGE_WAIT);

    if ("cancel".equals(action)) {
      throw new CancelledException(pukInfo.getLocalizedName() +
              " entry cancelled");
    }
    retry = true;
    return gui.getOldPin();
}

}
