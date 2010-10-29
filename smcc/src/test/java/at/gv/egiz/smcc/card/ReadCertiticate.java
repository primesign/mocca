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
