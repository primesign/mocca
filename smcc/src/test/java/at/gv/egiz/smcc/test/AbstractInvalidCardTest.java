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

package at.gv.egiz.smcc.test;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.junit.Test;

import at.gv.egiz.smcc.SignatureCardException;
import at.gv.egiz.smcc.SignatureCard.KeyboxName;
import at.gv.egiz.smcc.pin.gui.PINGUI;
import at.gv.egiz.smcc.pin.gui.SMCCTestPINProvider;

public abstract class AbstractInvalidCardTest extends AbstractCardTestBase {

  @Test
  public void testGetCertificates() throws SignatureCardException, InterruptedException {
    
    try {
      signatureCard.getCertificate(KeyboxName.SECURE_SIGNATURE_KEYPAIR, null);
      fail();
    } catch (SignatureCardException e) {
      // expected
    } catch (Exception e) {
      fail();
    }
    
    try {
      signatureCard.getCertificate(KeyboxName.CERITIFIED_KEYPAIR, null);
      fail();
    } catch (SignatureCardException e) {
      // expected
    } catch (Exception e) {
      fail();
    }
    
  }

  @Test(expected = SignatureCardException.class)
  public void testGetInfoboxIdentityLink() throws SignatureCardException, InterruptedException {

    PINGUI pinProvider = new SMCCTestPINProvider("0000".toCharArray());
    
    byte[] idlink = signatureCard.getInfobox("IdentityLink", pinProvider, null);
    
    assertNull(idlink);
    
  }
  
}
