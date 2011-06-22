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
