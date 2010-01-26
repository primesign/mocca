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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import iaik.security.provider.IAIK;

import java.security.Security;
import java.util.Arrays;

import org.junit.BeforeClass;
import org.junit.Test;

import at.gv.egiz.smcc.CancelledException;
import at.gv.egiz.smcc.CardEmul;
import at.gv.egiz.smcc.CardNotSupportedException;
import at.gv.egiz.smcc.CardTerminalEmul;
import at.gv.egiz.smcc.LockedException;
import at.gv.egiz.smcc.NotActivatedException;
import at.gv.egiz.smcc.PINFormatException;
import at.gv.egiz.smcc.PINMgmtSignatureCard;
import at.gv.egiz.smcc.PINSpec;
import at.gv.egiz.smcc.SignatureCard;
import at.gv.egiz.smcc.SignatureCardException;
import at.gv.egiz.smcc.SignatureCardFactory;
import at.gv.egiz.smcc.pin.gui.ChangePINProvider;
import at.gv.egiz.smcc.pin.gui.SMCCTestPINProvider;

public class A04CardTest extends ACOSCardTest {

  @Override
  protected SignatureCard createSignatureCard()
      throws CardNotSupportedException {
    SignatureCardFactory factory = SignatureCardFactory.getInstance();
    CardEmul card = new A04CardEmul(new A04ApplSIG(), new A04ApplDEC());
    SignatureCard signatureCard = factory.createSignatureCard(card,
        new CardTerminalEmul(card));
    assertTrue(signatureCard instanceof PINMgmtSignatureCard);
    return signatureCard;
  }

  @Override
  protected int getVersion() {
    return 2;
  }

  @BeforeClass
  public static void setupClass() {
    IAIK.addAsProvider();
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

    for (PINSpec pinSpec : signatureCard.getPINSpecs()) {

      char[] pin = defaultPin;

      for (int i = pinSpec.getMinLength(); i <= pinSpec.getMaxLength(); i++) {
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
  public void testGetInfoboxIdentityLinkEncrypted()
      throws CardNotSupportedException, SignatureCardException,
      InterruptedException {
    
    char[] pin = "0000".toCharArray();

    SignatureCardFactory factory = SignatureCardFactory.getInstance();
    A04ApplDEC applDEC = new A04ApplDEC(true);
    applDEC.setPin(A04ApplDEC.KID_PIN_DEC, pin);
    CardEmul card = new A04CardEmul(new A04ApplSIG(), applDEC);
    SignatureCard signatureCard = factory.createSignatureCard(card,
        new CardTerminalEmul(card));

    SMCCTestPINProvider pinProvider = new SMCCTestPINProvider(pin);

    byte[] idlink = signatureCard.getInfobox("IdentityLink",
        pinProvider, null);
    assertNotNull(idlink);
    assertTrue(Arrays.equals(idlink, A04ApplDEC.IDLINK));
    assertEquals(1, pinProvider.getProvided());

  }
  
  @Test
  public void testGetInfoboxIdentityLink() throws SignatureCardException,
      InterruptedException, CardNotSupportedException {

    final char[] pin = "0000".toCharArray();
    
    SignatureCard signatureCard = createSignatureCard();
    
    SMCCTestPINProvider pinProvider = new SMCCTestPINProvider(pin);

    byte[] idlink = signatureCard.getInfobox("IdentityLink",
        pinProvider, null);
    assertNotNull(idlink);
    assertTrue(Arrays.equals(idlink, A04ApplDEC.IDLINK));
    assertEquals(0, pinProvider.getProvided());

  }


}
