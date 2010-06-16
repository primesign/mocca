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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

import at.gv.egiz.smcc.CancelledException;
import at.gv.egiz.smcc.CardNotSupportedException;
import at.gv.egiz.smcc.LockedException;
import at.gv.egiz.smcc.NotActivatedException;
import at.gv.egiz.smcc.PIN;
import at.gv.egiz.smcc.PINFormatException;
import at.gv.egiz.smcc.PINMgmtSignatureCard;
import at.gv.egiz.smcc.PinInfo;
import at.gv.egiz.smcc.SignatureCardException;
import at.gv.egiz.smcc.pin.gui.ChangePINProvider;
import at.gv.egiz.smcc.pin.gui.InvalidChangePINProvider;
import at.gv.egiz.smcc.pin.gui.InvalidPINProvider;
import at.gv.egiz.smcc.pin.gui.PINGUI;
import at.gv.egiz.smcc.pin.gui.SMCCTestPINProvider;


public class ECardG3Test extends ECardTest {

  @Test
  public void testGetInfoboxIdentityLink() throws SignatureCardException, InterruptedException {

    PINGUI pinProvider = new SMCCTestPINProvider("1234".toCharArray());
    
    byte[] idlinkRef = (byte[]) applicationContext.getBean("identityLink", byte[].class);

    byte[] idlink = signatureCard.getInfobox("IdentityLink", pinProvider, null);
    
    assertArrayEquals(idlinkRef, idlink);
    
  }
  
  @Test
  public void testChangePin() throws CardNotSupportedException,
      LockedException, NotActivatedException, CancelledException,
      PINFormatException, SignatureCardException, InterruptedException {

    byte[] referenceData = { (byte) 0x26, (byte) 0x12, (byte) 0x34,
        (byte) 0x56, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff };
    PINMgmtSignatureCard signatureCard = (PINMgmtSignatureCard) this.signatureCard;
    PIN signaturPIN = (PIN) applicationContext.getBean("signaturPIN");
    signaturPIN.setPin(referenceData);
    PIN kartenPIN = (PIN) applicationContext.getBean("kartenPIN");
    kartenPIN.setPin(referenceData);

    for (PinInfo pinInfo : signatureCard.getPinInfos()) {

      char[] pin = "123456".toCharArray();

      for (int i = pinInfo.getMinLength(); i <= pinInfo.getMaxLength(); i++) {
        signatureCard.verifyPIN(pinInfo, new SMCCTestPINProvider(pin));
        char[] newPin = new char[i];
        Arrays.fill(newPin, '0');
        signatureCard
            .changePIN(pinInfo, new ChangePINProvider(pin, newPin));
        signatureCard.verifyPIN(pinInfo, new SMCCTestPINProvider(newPin));
        pin = newPin;
      }
    }
  }

  @Test
  public void testVerifyInvalidPin() throws CardNotSupportedException,
      LockedException, NotActivatedException, CancelledException,
      PINFormatException, SignatureCardException, InterruptedException {

    PINMgmtSignatureCard signatureCard = (PINMgmtSignatureCard) this.signatureCard;

    for (PinInfo pinInfo : signatureCard.getPinInfos()) {

      char[] invalidPin = "999999".toCharArray();
      int numInvalidTries = 2;
      InvalidPINProvider invalidPinProvider = new InvalidPINProvider(invalidPin, numInvalidTries);
      try {
        signatureCard.verifyPIN(pinInfo, invalidPinProvider);
      } catch (CancelledException ex) {
      } finally {
        assertTrue(invalidPinProvider.getProvided() == numInvalidTries);
      }
    }
  }

  @Test
  public void testChangeInvalidPin() throws CardNotSupportedException,
      LockedException, NotActivatedException, CancelledException,
      PINFormatException, SignatureCardException, InterruptedException {

    PINMgmtSignatureCard signatureCard = (PINMgmtSignatureCard) this.signatureCard;
    
    for (PinInfo pinInfo : signatureCard.getPinInfos()) {

      char[] invalidPin = "999999".toCharArray();
      int numInvalidTries = 2;
      InvalidChangePINProvider invalidPinProvider =
              new InvalidChangePINProvider(invalidPin, invalidPin, numInvalidTries);

      try {
        signatureCard.changePIN(pinInfo, invalidPinProvider);
      } catch (CancelledException ex) {
      } finally {
        assertTrue(invalidPinProvider.getProvided() == numInvalidTries);
      }
    }
  }

  
}
