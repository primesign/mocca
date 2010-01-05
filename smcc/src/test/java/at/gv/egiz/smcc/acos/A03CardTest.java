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
package at.gv.egiz.smcc.acos;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

import at.gv.egiz.smcc.CancelledException;
import at.gv.egiz.smcc.CardEmul;
import at.gv.egiz.smcc.CardNotSupportedException;
import at.gv.egiz.smcc.CardTerminalEmul;
import at.gv.egiz.smcc.pin.gui.ChangePINProvider;
import at.gv.egiz.smcc.LockedException;
import at.gv.egiz.smcc.NotActivatedException;
import at.gv.egiz.smcc.PINFormatException;
import at.gv.egiz.smcc.PINMgmtSignatureCard;
import at.gv.egiz.smcc.pin.gui.SMCCTestPINProvider;
import at.gv.egiz.smcc.PINSpec;
import at.gv.egiz.smcc.SignatureCard;
import at.gv.egiz.smcc.SignatureCardException;
import at.gv.egiz.smcc.SignatureCardFactory;

public class A03CardTest extends ACOSCardTest {

  @Override
  protected SignatureCard createSignatureCard()
      throws CardNotSupportedException {
    SignatureCardFactory factory = SignatureCardFactory.getInstance();
    CardEmul card = new A03CardEmul(new A03ApplSIG(), new A03ApplDEC());
    SignatureCard signatureCard = factory.createSignatureCard(card,
        new CardTerminalEmul(card));
    assertTrue(signatureCard instanceof PINMgmtSignatureCard);
    return signatureCard;
  }

  @Override
  protected int getVersion() {
    return 1;
  }

  @Test
  public void testChangePin() throws CardNotSupportedException,
      LockedException, NotActivatedException, CancelledException,
      PINFormatException, SignatureCardException, InterruptedException {

    char[] defaultPin = "123456".toCharArray();

    PINMgmtSignatureCard signatureCard = (PINMgmtSignatureCard) createSignatureCard();
    CardEmul card = (CardEmul) signatureCard.getCard();
    ACOSApplSIG applSIG = (ACOSApplSIG) card.getApplication(ACOSAppl.AID_SIG);
    applSIG.setPin(ACOSApplSIG.KID_PIN_SIG, defaultPin);
    ACOSApplDEC applDEC = (ACOSApplDEC) card.getApplication(ACOSAppl.AID_DEC);
    applDEC.setPin(ACOSApplDEC.KID_PIN_DEC, defaultPin);
    applDEC.setPin(A03ApplDEC.KID_PIN_INF, defaultPin);

    for (PINSpec pinSpec : signatureCard.getPINSpecs()) {

      char[] pin = defaultPin;

      for (int i = pinSpec.getMinLength(); i <= pinSpec.getMaxLength(); i++) {
        signatureCard.verifyPIN(pinSpec, new SMCCTestPINProvider(pin));
        char[] newPin = new char[i];
        Arrays.fill(newPin, '0');
        signatureCard
            .changePIN(pinSpec, new ChangePINProvider(pin, newPin));
        signatureCard.verifyPIN(pinSpec, new SMCCTestPINProvider(newPin));
        pin = newPin;
      }

    }

  }

  
}
