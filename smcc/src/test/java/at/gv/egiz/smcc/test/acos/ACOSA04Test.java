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

package at.gv.egiz.smcc.test.acos;

import static org.junit.Assert.assertArrayEquals;

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
import at.gv.egiz.smcc.pin.gui.PINGUI;
import at.gv.egiz.smcc.pin.gui.SMCCTestPINProvider;


public class ACOSA04Test extends ACOSTest {

  @Test
  public void testChangePins() throws CardNotSupportedException,
      LockedException, NotActivatedException, CancelledException,
      PINFormatException, SignatureCardException, InterruptedException {

    byte[] referenceData = { (byte) 0x31, (byte) 0x32, (byte) 0x33,
        (byte) 0x34, (byte) 0x35, (byte) 0x36, (byte) 0x00, (byte) 0x00 };
    
    PINMgmtSignatureCard signatureCard = (PINMgmtSignatureCard) this.signatureCard;
    PIN signaturPIN = (PIN) applicationContext.getBean("signaturPIN");
    signaturPIN.setPin(referenceData);
    PIN infoboxPIN = (PIN) applicationContext.getBean("geheimhaltungsPIN");
    infoboxPIN.setPin(referenceData);
    
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
  public void testGetInfoboxIdentityLink() throws SignatureCardException, InterruptedException {

    PINGUI pinProvider = new SMCCTestPINProvider("0000".toCharArray());
    
    byte[] idlinkRef = (byte[]) applicationContext.getBean("identityLink", byte[].class);

    byte[] idlink = signatureCard.getInfobox("IdentityLink", pinProvider, null);
    
    assertArrayEquals(idlinkRef, idlink);
    
  }
  
}
