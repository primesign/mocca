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
package at.gv.egiz.bku.slcommands;

import static org.junit.Assert.assertTrue;

import java.io.Reader;
import java.io.StringReader;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.junit.Before;
import org.junit.Test;

import at.gv.egiz.bku.slexceptions.SLCommandException;
import at.gv.egiz.bku.slexceptions.SLRequestException;
import at.gv.egiz.bku.slexceptions.SLRuntimeException;

public class SLCommandFactoryTest {
  
  SLCommandFactory factory;
  SLCommandContext context;
  
  @Before
  public void setUp() {
    factory = SLCommandFactory.getInstance();
    context = new SLCommandContext();
  }
  
  @Test
  public void createNullOperationCommand() throws SLCommandException, SLRuntimeException, SLRequestException {
    Reader requestReader = new StringReader(
        "<NullOperationRequest xmlns=\"http://www.buergerkarte.at/namespaces/securitylayer/1.2#\"/>");
    Source source = new StreamSource(requestReader);
    
    SLCommand slCommand = factory.createSLCommand(source, context);
    
    assertTrue(slCommand instanceof NullOperationCommand);
  }

  @Test(expected=SLCommandException.class)
  public void createUnsupportedCommand() throws SLCommandException, SLRuntimeException, SLRequestException {
    Reader requestReader = new StringReader(
      "<CreateCMSSignatureRequest xmlns=\"http://www.buergerkarte.at/namespaces/securitylayer/1.2#\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.buergerkarte.at/namespaces/securitylayer/1.2# file:/home/clemens/IAIK/BKU2/svn/bku/utils/src/main/schema/Core-1.2.xsd\" Structure=\"detached\"><KeyboxIdentifier></KeyboxIdentifier><DataObject><MetaInfo><MimeType></MimeType></MetaInfo><Content><Base64Content></Base64Content></Content></DataObject></CreateCMSSignatureRequest>");
    Source source = new StreamSource(requestReader);
    
    factory.createSLCommand(source, context);
    
  }
  
  @Test(expected=SLRequestException.class)
  public void createMalformedCommand() throws SLCommandException, SLRuntimeException, SLRequestException {
    Reader requestReader = new StringReader(
        "<NullOperationRequest xmlns=\"http://www.buergerkarte.at/namespaces/securitylayer/1.2#\">" +
          "missplacedContent" +
        "</NullOperationRequest>");
    Source source = new StreamSource(requestReader);
    
    factory.createSLCommand(source, context);
    
  }
  
}
