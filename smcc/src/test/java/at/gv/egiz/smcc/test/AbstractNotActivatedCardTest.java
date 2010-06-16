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

import static org.junit.Assert.fail;

import org.junit.Test;

import at.gv.egiz.smcc.NotActivatedException;
import at.gv.egiz.smcc.PINMgmtSignatureCard;
import at.gv.egiz.smcc.SignatureCardException;
import at.gv.egiz.smcc.SignatureCard.KeyboxName;

public abstract class AbstractNotActivatedCardTest extends AbstractCardTestBase {

  @Test(expected = NotActivatedException.class)
  public void testGetInfoboxIdentityLink() throws SignatureCardException, InterruptedException {

    signatureCard.getInfobox("IdentityLink", null, null);
    
  }
  
  @Test
  public void testGetCertificates() throws SignatureCardException, InterruptedException {
    
    try {
      signatureCard.getCertificate(KeyboxName.SECURE_SIGNATURE_KEYPAIR);
      fail();
    } catch (NotActivatedException e) {
      // expected
    } catch (Exception e) {
      fail();
    }

    try {
      signatureCard.getCertificate(KeyboxName.CERITIFIED_KEYPAIR);
      fail();
    } catch (NotActivatedException e) {
      // expected
    } catch (Exception e) {
      fail();
    }
    
  }

  @Test (expected = NotActivatedException.class)
  public void getPINInfos() throws SignatureCardException {

    PINMgmtSignatureCard signatureCard = (PINMgmtSignatureCard) this.signatureCard;
    signatureCard.getPinInfos();

  }
  
}
