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
  
  private static KeyboxName[] keyboxNames = { KeyboxName.SECURE_SIGNATURE_KEYPAIR, KeyboxName.CERTIFIED_KEYPAIR };
  
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
