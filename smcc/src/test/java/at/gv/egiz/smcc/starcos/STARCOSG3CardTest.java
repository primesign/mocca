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
package at.gv.egiz.smcc.starcos;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;


import org.junit.Test;

import at.gv.egiz.smcc.CancelledException;
import at.gv.egiz.smcc.CardEmul;
import at.gv.egiz.smcc.CardNotSupportedException;
import at.gv.egiz.smcc.CardTerminalEmul;
import at.gv.egiz.smcc.CardTest;
import at.gv.egiz.smcc.pin.gui.ChangePINProvider;
import at.gv.egiz.smcc.LockedException;
import at.gv.egiz.smcc.NotActivatedException;
import at.gv.egiz.smcc.PIN;
import at.gv.egiz.smcc.PINFormatException;
import at.gv.egiz.smcc.PINMgmtSignatureCard;
import at.gv.egiz.smcc.PINSpec;
import at.gv.egiz.smcc.SignatureCard;
import at.gv.egiz.smcc.SignatureCardException;
import at.gv.egiz.smcc.SignatureCardFactory;
import at.gv.egiz.smcc.pin.gui.SMCCTestPINProvider;
import org.junit.Ignore;

public class STARCOSG3CardTest extends CardTest {

  @Override
  protected SignatureCard createSignatureCard()
      throws CardNotSupportedException {
    SignatureCardFactory factory = SignatureCardFactory.getInstance();
    STARCOSG3CardEmul card = new STARCOSG3CardEmul();
    SignatureCard signatureCard = factory.createSignatureCard(card,
        new CardTerminalEmul(card));
    assertTrue(signatureCard instanceof PINMgmtSignatureCard);
    return signatureCard;
  }

  protected SignatureCard createSignatureCard(byte[] SS_PIN, byte[] Glob_PIN, int pinState)
      throws CardNotSupportedException {
    SignatureCardFactory factory = SignatureCardFactory.getInstance();
    STARCOSG3CardEmul card = new STARCOSG3CardEmul(SS_PIN, Glob_PIN, pinState);
    SignatureCard signatureCard = factory.createSignatureCard(card,
        new CardTerminalEmul(card));
    assertTrue(signatureCard instanceof PINMgmtSignatureCard);
    return signatureCard;
  }

  @Test
  public void testChangePin() throws CardNotSupportedException,
      LockedException, NotActivatedException, CancelledException,
      PINFormatException, SignatureCardException, InterruptedException {

    PINMgmtSignatureCard signatureCard = (PINMgmtSignatureCard) createSignatureCard(
            STARCOSG3CardEmul.DEFAULT_SS_PIN, STARCOSG3CardEmul.DEFAULT_SS_PIN, PIN.STATE_RESET);
    
    for (PINSpec pinSpec : signatureCard.getPINSpecs()) {

      char[] pin = "123456".toCharArray();

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

  @Test
  @Override
  public void testActivatePin() throws CardNotSupportedException,
      LockedException, NotActivatedException, CancelledException,
      PINFormatException, SignatureCardException, InterruptedException {

    PINMgmtSignatureCard signatureCard = (PINMgmtSignatureCard) createSignatureCard(
            STARCOSG3CardEmul.TRANSPORT_SS_PIN, STARCOSG3CardEmul.TRANSPORT_SS_PIN, PIN.STATE_PIN_NOTACTIVE);
    
    for (PINSpec pinSpec : signatureCard.getPINSpecs()) {

      char[] pin = "123456789".substring(0, pinSpec.getMinLength()).toCharArray();
      char[] transportPIN = "123456".toCharArray();

      boolean notActive = false;
      try {
        signatureCard.verifyPIN(pinSpec, new SMCCTestPINProvider(pin));
      } catch (NotActivatedException ex) {
        notActive = true;
      }
      assertTrue(notActive);

      signatureCard.activatePIN(pinSpec, new ChangePINProvider(transportPIN, pin));
      signatureCard.verifyPIN(pinSpec, new SMCCTestPINProvider(pin));
    }
  }

  
}
