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
import java.io.OutputStream;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

import org.junit.Ignore;

import sun.misc.HexDumpEncoder;

import at.gv.egiz.smcc.SignatureCard.KeyboxName;
import at.gv.egiz.smcc.util.SMCCHelper;

@Ignore
public class STARCOSCardTest {

  /**
   * @param args
   * @throws Exception 
   */
  public static void main(String[] args) throws Exception {
    
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
//      printJavaByteArray(
//          signatureCard.getCertificate(KeyboxName.SECURE_SIGNATURE_KEYPAIR), System.out);
//      printJavaByteArray(
//          signatureCard.getCertificate(KeyboxName.CERITIFIED_KEYPAIR), System.out);
//      System.out. println(new String(signatureCard.getInfobox("IdentityLink", new CommandLinePINProvider(), null)));
//        byte[] infobox = signatureCard.getInfobox("Status", new CommandLinePINProvider(), null);
//        printJavaByteArray(infobox, System.out);
      MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
      byte[] digest = messageDigest.digest("test".getBytes());
      byte[] signature = signatureCard.createSignature(digest, KeyboxName.SECURE_SIGNATURE_KEYPAIR, new CommandLinePINProvider());
      printJavaByteArray(signature, System.out);
    } catch (SignatureCardException e) {
      e.printStackTrace();
    }

  }
  
  public static void printJavaByteArray(byte[] bytes, OutputStream os) {
    
    PrintWriter w = new PrintWriter(os);
    
    w.write("new byte[] {");
    for (int i = 0; i < bytes.length;) {
      if (i % 8 == 0) { 
        w.write("\n  ");
      }
      w.write("(byte) 0x" + Integer.toHexString(0x0F & (bytes[i] >> 4)) + Integer.toHexString(0x0F & bytes[i]));
      if (++i < bytes.length) {
        w.write(", ");
      }
    }
    w.write("\n};");
    w.flush();
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
