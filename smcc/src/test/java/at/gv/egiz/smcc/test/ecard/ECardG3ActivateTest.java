/*
* Copyright 2009 Federal Chancellery Austria and
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

package at.gv.egiz.smcc.test.ecard;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import at.gv.egiz.smcc.CancelledException;
import at.gv.egiz.smcc.CardNotSupportedException;
import at.gv.egiz.smcc.LockedException;
import at.gv.egiz.smcc.NotActivatedException;
import at.gv.egiz.smcc.PINFormatException;
import at.gv.egiz.smcc.PINMgmtSignatureCard;
import at.gv.egiz.smcc.PinInfo;
import at.gv.egiz.smcc.SignatureCardException;
import at.gv.egiz.smcc.pin.gui.ChangePINProvider;
import at.gv.egiz.smcc.pin.gui.SMCCTestPINProvider;
import at.gv.egiz.smcc.test.AbstractCardTestBase;

public class ECardG3ActivateTest extends AbstractCardTestBase {

  @Test
  public void testActivatePin() throws CardNotSupportedException,
      LockedException, NotActivatedException, CancelledException,
      PINFormatException, SignatureCardException, InterruptedException {

    PINMgmtSignatureCard signatureCard = (PINMgmtSignatureCard) this.signatureCard;
    
    for (PinInfo pinInfo : signatureCard.getPinInfos()) {

      char[] pin = "123456789".substring(0, pinInfo.getMinLength()).toCharArray();
      char[] transportPIN = "123456".toCharArray();

      boolean notActive = false;
      try {
        signatureCard.verifyPIN(pinInfo, new SMCCTestPINProvider(pin));
      } catch (NotActivatedException ex) {
        notActive = true;
      }
      assertTrue(notActive);

      signatureCard.activatePIN(pinInfo, new ChangePINProvider(transportPIN, pin));
      signatureCard.verifyPIN(pinInfo, new SMCCTestPINProvider(pin));
    }
  }

  
}
