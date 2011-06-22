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


package at.gv.egiz.idlink;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.bind.JAXBException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import at.buergerkarte.namespaces.personenbindung._20020506_.CompressedIdentityLinkType;
import at.gv.egiz.idlink.ans1.IdentityLink;

public class CompressIdentityLink {

  /**
   * @param args
   * @throws JAXBException 
   * @throws IOException 
   */
  public static void main(String[] args) throws JAXBException, IOException {
    
    FileInputStream fis = new FileInputStream(args[0]);
    Source source = new StreamSource(fis);
    
    CompressedIdentityLinkFactory factory = CompressedIdentityLinkFactory.getInstance();
    
    CompressedIdentityLinkType compressedIdentity = factory.unmarshallCompressedIdentityLink(source);
    
    IdentityLink idLink = factory.createIdLink(compressedIdentity);
    
    FileOutputStream outputStream = new FileOutputStream("idlink.bin");
    outputStream.write(idLink.toByteArray());
    outputStream.close();
    
  }

}
