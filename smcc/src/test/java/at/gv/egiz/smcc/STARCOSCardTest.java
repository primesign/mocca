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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

import at.gv.egiz.smcc.SignatureCard.KeyboxName;
import at.gv.egiz.smcc.util.SMCCHelper;

public class STARCOSCardTest {

  /**
   * @param args
   * @throws CardException 
   * @throws NoSuchAlgorithmException 
   */
  public static void main(String[] args) throws CardException, NoSuchAlgorithmException, InterruptedException {
    
    SMCCHelper helper = new SMCCHelper();
    while (helper.getResultCode() != SMCCHelper.CARD_FOUND) {
      System.out.println("Did not get a signature card ... " + helper.getResultCode());
      helper.update();
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    
    SignatureCard signatureCard = helper.getSignatureCard(Locale.getDefault());
    
    System.out.println("Found '" + signatureCard + "'.");
    
    try {
//      signatureCard.getCertificate(KeyboxName.SECURE_SIGNATURE_KEYPAIR);
//      signatureCard.getCertificate(KeyboxName.CERITIFIED_KEYPAIR);
//      signatureCard.getInfobox("IdentityLink", new CommandLinePINProvider(), null);
      MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
      byte[] digest = messageDigest.digest("test".getBytes());
      signatureCard.createSignature(digest, KeyboxName.CERITIFIED_KEYPAIR, new CommandLinePINProvider());
    } catch (SignatureCardException e) {
      e.printStackTrace();
    }

  }
  
  private static class CommandLinePINProvider implements PINProvider {

    @Override
    public String providePIN(PINSpec spec, int retries) {
      
      InputStreamReader inputStreamReader = new InputStreamReader(System.in);
      BufferedReader in = new BufferedReader(inputStreamReader);
      
      System.out.print("Enter " + spec.getLocalizedName() + " ["
          + spec.getMinLength() + "-" + spec.getMaxLength() + "] (" + retries
          + " retries):");
      
      try {
        return in.readLine();
      } catch (IOException e) {
        return null;
      }
      
    }
    
  }

}
