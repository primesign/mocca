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

import iaik.me.asn1.ASN1;
import iaik.me.utils.Base64;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Vector;

import org.junit.Ignore;

@Ignore
public class ASN1Test {

  public static void main(String[] args) throws IOException {

    ClassLoader classLoader = ASN1Test.class.getClassLoader();

    InputStream stream = classLoader.getResourceAsStream("IdentityLink.bin");

    if (stream != null) {

      ASN1 identityLink = new ASN1(stream);
      System.out.println("BaseId:" + getBaseId(identityLink));
      identityLink = replaceBaseId(identityLink, "test");
      System.out.println("BaseId:" + getBaseId(identityLink));

      String bpk = "hansi";
      MessageDigest sha = null;
      try {
        sha = MessageDigest.getInstance("SHA");
      } catch (NoSuchAlgorithmException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      sha.update((identityLink + "+" + bpk).getBytes());
      String bpkStr = new String(Base64.encode(sha.digest()));
      System.out.println("bpk: "+bpkStr);
      identityLink = replaceBaseId(identityLink, bpkStr);
      System.out.println(getBaseId(identityLink));

    } else {
      System.out.println("Not found.");
    }

  }

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

}
