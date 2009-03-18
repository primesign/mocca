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

package at.gv.egiz.bku.smccstal.ext;

import at.gv.egiz.smcc.ChangePINProvider;
import at.gv.egiz.bku.gui.PINManagementGUIFacade;
import at.gv.egiz.smcc.PINProvider;
import at.gv.egiz.smcc.SignatureCard;

/**
 *
 * @author Clemens Orthacker <clemens.orthacker@iaik.tugraz.at>
 */
public abstract class ManagementPINProviderFactory {
//        extends at.gv.egiz.bku.smccstal.PINProviderFactory {

  PINManagementGUIFacade gui;
  
  public static ManagementPINProviderFactory getInstance(SignatureCard forCard,
          PINManagementGUIFacade gui) {
//    if (forCard.ifdSupportsFeature(SignatureCard.FEATURE_VERIFY_PIN_DIRECT)) {
////      forCard.ifdSupportsFeature(SignatureCard.FEATURE_MODIFY_PIN_DIRECT)
//      return new PinpadPINProviderFactory(gui);
//
//    } else {
      return new SoftwarePINProviderFactory(gui);
//    }
  }

  public abstract PINProvider getVerifyPINProvider();

  public abstract PINProvider getActivatePINProvider();

  public abstract ChangePINProvider getChangePINProvider();

  public abstract PINProvider getUnblockPINProvider();

}
