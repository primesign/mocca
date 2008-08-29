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
package at.gv.egiz.idlink;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.bind.JAXBException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import at.buergerkarte.namespaces.personenbindung._20020506_.CompressedIdentityLinkType;
import at.gv.egiz.bku.utils.HexDump;
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
