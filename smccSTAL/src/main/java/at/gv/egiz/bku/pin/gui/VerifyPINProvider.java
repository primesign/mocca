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
