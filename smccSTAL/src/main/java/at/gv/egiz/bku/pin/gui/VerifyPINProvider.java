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
import at.gv.egiz.smcc.pin.gui.PINProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class VerifyPINProvider extends AbstractPINProvider implements PINProvider {

  private final Logger log = LoggerFactory.getLogger(VerifyPINProvider.class);

  protected BKUGUIFacade gui;
  private boolean retry = false;

  public VerifyPINProvider(BKUGUIFacade gui) {
    this.gui = gui;
  }

  @Override
  public char[] providePIN(PinInfo spec, int retries)
          throws CancelledException, InterruptedException {

    gui.showVerifyPINDialog(spec, (retry) ? retries : -1,
            this, "verify",
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
    return gui.getPin();
  }
}
