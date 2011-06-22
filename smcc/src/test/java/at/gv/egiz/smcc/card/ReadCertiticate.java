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



package at.gv.egiz.smcc.card;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Locale;

import at.gv.egiz.smcc.SignatureCard;
import at.gv.egiz.smcc.SignatureCardException;
import at.gv.egiz.smcc.SignatureCard.KeyboxName;
import at.gv.egiz.smcc.util.SMCCHelper;

public class ReadCertiticate {

  public static void main(String[] args) throws Exception {
    
    SMCCHelper helper = new SMCCHelper();

    SignatureCard signatureCard = helper.getSignatureCard(Locale.getDefault());
    
    if (signatureCard == null) {
      return;
    }
    
    X509Certificate cert = readCertificate(signatureCard, KeyboxName.SECURE_SIGNATURE_KEYPAIR);
    System.out.println(cert.toString());
    
  }
  
  public static X509Certificate readCertificate(SignatureCard signatureCard,
      KeyboxName keyboxName) throws SignatureCardException,
      InterruptedException, CertificateException {
    byte[] certificate = signatureCard.getCertificate(KeyboxName.SECURE_SIGNATURE_KEYPAIR, null);
    CertificateFactory certificateFactory = CertificateFactory.getInstance("X509");
    return (X509Certificate) certificateFactory.generateCertificate(new ByteArrayInputStream(certificate));
  }
  
}
