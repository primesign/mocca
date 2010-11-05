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

package at.gv.egiz.smcctest;

import iaik.security.ecc.provider.ECCProvider;
import iaik.security.provider.IAIK;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Locale;

import at.gv.egiz.smcc.CancelledException;
import at.gv.egiz.smcc.PinInfo;
import at.gv.egiz.smcc.SignatureCard;
import at.gv.egiz.smcc.SignatureCardException;
import at.gv.egiz.smcc.SignatureCard.KeyboxName;
import at.gv.egiz.smcc.pin.gui.PINGUI;
import at.gv.egiz.smcc.util.SMCCHelper;

public class CardTest {
  
  private static KeyboxName[] keyboxNames = { KeyboxName.SECURE_SIGNATURE_KEYPAIR, KeyboxName.CERITIFIED_KEYPAIR };
  
  private static String[] infoboxes = { "IdentityLink" };
  
  public static void main(String args[]) throws CertificateException, InterruptedException {
    
    IAIK.addAsJDK14Provider();
    ECCProvider.addAsProvider();
    
    SMCCHelper helper = new SMCCHelper();
    SignatureCard signatureCard = helper.getSignatureCard(Locale.getDefault());
    
    if (signatureCard != null) {

      CertificateFactory certificateFactory = CertificateFactory.getInstance("X509");
      for (KeyboxName keyboxName : keyboxNames) {
        
        // Certificates
        try {
          System.out.println("--- Certificate " + keyboxName + " ---");
          byte[] certificate = signatureCard.getCertificate(keyboxName, null);
          Certificate cert = certificateFactory.generateCertificate(new ByteArrayInputStream(certificate));
          System.out.println(cert);
        } catch (SignatureCardException e) {
          e.printStackTrace();
        } 

        // Signature
        
      
      }
      
      // Infoboxes
      for (String infobox : infoboxes) {
        try {
          System.out.println("--- Infobox " + infobox + " ---");
          byte[] box = signatureCard.getInfobox(infobox, new PINUI(), null);
          System.out.println(SMCCHelper.toString(box));
        } catch (SignatureCardException e) {
          e.printStackTrace();
        }
      }      

      
    } else {
      System.out.println("No signature card found.");
    }
    
  }
  
  public static class PINUI implements PINGUI {

    @Override
    public void allKeysCleared() {
      
    }

    @Override
    public void correctionButtonPressed() {
    }

    @Override
    public void enterPIN(PinInfo pinInfo, int retries)
        throws CancelledException, InterruptedException {
    }

    @Override
    public void enterPINDirect(PinInfo pinInfo, int retries)
        throws CancelledException, InterruptedException {
    }

    @Override
    public void validKeyPressed() {
    }

    @Override
    public char[] providePIN(PinInfo pinSpec, int retries)
        throws CancelledException, InterruptedException {
      
      System.out.print(pinSpec.getLocalizedName() + ":" );
      BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
      
      String pin;
      try {
        pin = reader.readLine();
      } catch (IOException e) {
        throw new CancelledException(e);
      }
      
      return pin.toCharArray();
    }
    
  }

}
