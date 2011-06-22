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


public class ECardG2Test extends ECardTest {

  @Test
  public void testChangeCardPin() throws CardNotSupportedException,
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
  public void testGetInfoboxIdentityLink() throws SignatureCardException, InterruptedException {

    PINGUI pinProvider = new SMCCTestPINProvider("1234".toCharArray());
    
    byte[] idlinkRef = (byte[]) applicationContext.getBean("identityLink", byte[].class);

    byte[] idlink = signatureCard.getInfobox("IdentityLink", pinProvider, null);
    
    assertArrayEquals(idlinkRef, idlink);
    
  }
  
}
