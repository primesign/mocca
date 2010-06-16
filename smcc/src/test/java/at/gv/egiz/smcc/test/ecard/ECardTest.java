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

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import org.junit.Test;

import at.gv.egiz.smcc.CardNotSupportedException;
import at.gv.egiz.smcc.PINMgmtSignatureCard;
import at.gv.egiz.smcc.PinInfo;
import at.gv.egiz.smcc.SignatureCardException;
import at.gv.egiz.smcc.pin.gui.CancelChangePINProvider;
import at.gv.egiz.smcc.pin.gui.ModifyPINGUI;
import at.gv.egiz.smcc.test.AbstractCardTest;

public abstract class ECardTest extends AbstractCardTest {

  @Test
  public abstract void testGetInfoboxIdentityLink() throws SignatureCardException, InterruptedException;
  
  @Test(expected = SignatureCardException.class)
  public void testActivatePin() throws SignatureCardException,
      InterruptedException, CardNotSupportedException,
      NoSuchAlgorithmException, UnsupportedEncodingException {

    assertTrue(signatureCard instanceof PINMgmtSignatureCard);

    ModifyPINGUI pinProvider = new CancelChangePINProvider();

    PinInfo[] specs = ((PINMgmtSignatureCard) signatureCard).getPinInfos();

    ((PINMgmtSignatureCard) signatureCard).activatePIN(specs[0], pinProvider);
  }
  
}
