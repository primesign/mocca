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
package at.gv.egiz.smcc.pin.gui;

import at.gv.egiz.smcc.CancelledException;
import at.gv.egiz.smcc.PinInfo;


/**
 * Display messages for pinpad pin-entry.
 * Provides an interface for two types of pinpad pin-entry: pinpad-direct and pinpad-start/finish
 * @author clemens.orthacker@iaik.tugraz.at
 */
public interface PINGUI extends PINProvider {

  void enterPINDirect(PinInfo pinInfo, int retries)
          throws CancelledException, InterruptedException;
  
  /**
   * @throws CancelledException, InterruptedException if signature-data dialog is interrupted or cancelled
   */
  void enterPIN(PinInfo pinInfo, int retries)
          throws CancelledException, InterruptedException;
  void validKeyPressed();
  void correctionButtonPressed();
  void allKeysCleared();
  
}
