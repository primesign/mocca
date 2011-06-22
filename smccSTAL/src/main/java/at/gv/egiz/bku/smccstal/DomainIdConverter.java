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


package at.gv.egiz.bku.smccstal;

import iaik.me.asn1.ASN1;
import iaik.me.utils.Base64;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Vector;

public class DomainIdConverter {

  private static String getBaseId(ASN1 identityLink) throws IOException {

    if (identityLink.getType() == ASN1.TYPE_SEQUENCE) {
      ASN1 personData = identityLink.getElementAt(4);
      if (personData.getType() == ASN1.TAG_CONTEXT_SPECIFIC) {
        ASN1 physicalPersonData = personData.gvASN1();
        ASN1 baseId = physicalPersonData.getElementAt(0);
        return baseId.gvString();
      }
      throw new IOException("Invalid structure.");

    }
    throw new IOException("Invalid structure.");

  }

  private static ASN1 replaceBaseId(ASN1 identityLink, String newBaseId)
      throws IOException {

    ASN1 newIdentityLink = new ASN1(ASN1.TYPE_SEQUENCE, new Vector<ASN1>());
    for (int i = 0; i < identityLink.getSize(); i++) {
      ASN1 asn1 = identityLink.getElementAt(i);
      if (i == 4 && asn1.getType() == ASN1.TAG_CONTEXT_SPECIFIC) {
        ASN1 physicalPersonData = asn1.gvASN1();
        ASN1 newPhysicalPersonData = new ASN1(ASN1.TYPE_SEQUENCE,
            new Vector<ASN1>());
        newPhysicalPersonData.addElement(new ASN1(ASN1.TYPE_UTF8_STRING,
            newBaseId));
        for (int j = 1; j < physicalPersonData.getSize(); j++) {
          newPhysicalPersonData.addElement(physicalPersonData.getElementAt(j));
        }
        asn1 = new ASN1(ASN1.TAG_CONTEXT_SPECIFIC, newPhysicalPersonData);
      }
      newIdentityLink.addElement(asn1);
    }
    return newIdentityLink;

  }

  public static byte[] convertDomainId(byte[] data, String domainId)
      throws IOException, NoSuchAlgorithmException {
    if (domainId == null) {
      return data;
    }
    ASN1 identityLink = new ASN1(data);
    MessageDigest sha = null;
    sha = MessageDigest.getInstance("SHA");
    String base = getBaseId(identityLink);
    sha.update((base + "+" + domainId).getBytes());
    String bpkStr = new String(Base64.encode(sha.digest()));
    bpkStr = bpkStr.trim();
    identityLink = replaceBaseId(identityLink, bpkStr);
    System.out.println(getBaseId(identityLink));
    return identityLink.getEncoded();
  }

}
