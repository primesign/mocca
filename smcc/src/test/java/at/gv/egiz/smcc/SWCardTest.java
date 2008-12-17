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
package at.gv.egiz.smcc;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.junit.Ignore;

import at.gv.egiz.smcc.SignatureCard.KeyboxName;

@Ignore
public class SWCardTest implements PINProvider {

  SWCard swCard = new SWCard();
  
  public static void main(String[] args) throws Exception {
    
    SWCardTest swCardTest = new SWCardTest();
    swCardTest.test();
    
  }
  
  public void test() throws SignatureCardException, NoSuchAlgorithmException, InterruptedException {
    
    swCard.getCertificate(KeyboxName.CERITIFIED_KEYPAIR);
    swCard.getCertificate(KeyboxName.SECURE_SIGNATURE_KEYPAIR);
    
    BigInteger t = BigInteger.valueOf(System.currentTimeMillis());

    MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
    byte[] hash = messageDigest.digest(t.toByteArray());
    
    byte[] signature; 
    signature = swCard.createSignature(hash, KeyboxName.CERITIFIED_KEYPAIR, this);
    System.out.println(SignatureCardFactory.toString(signature));

    signature = swCard.createSignature(hash, KeyboxName.SECURE_SIGNATURE_KEYPAIR, this);
    System.out.println(SignatureCardFactory.toString(signature));
    
    byte[] infobox = swCard.getInfobox("IdentityLink", this, null);
    System.out.println(SignatureCardFactory.toString(infobox));

  }

  @Override
  public String providePIN(PINSpec spec, int retries) {
    return "buerger";
  }
  
}
