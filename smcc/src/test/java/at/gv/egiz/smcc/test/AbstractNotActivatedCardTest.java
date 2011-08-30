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
      signatureCard.getCertificate(KeyboxName.SECURE_SIGNATURE_KEYPAIR, null);
      fail();
    } catch (NotActivatedException e) {
      // expected
    } catch (Exception e) {
      fail();
    }

    try {
      signatureCard.getCertificate(KeyboxName.CERTIFIED_KEYPAIR, null);
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
