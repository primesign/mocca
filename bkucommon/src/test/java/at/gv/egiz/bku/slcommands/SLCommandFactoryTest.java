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


package at.gv.egiz.bku.slcommands;

import static org.junit.Assert.assertTrue;

import java.io.Reader;
import java.io.StringReader;

import javax.xml.transform.stream.StreamSource;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import at.gv.egiz.bku.slexceptions.SLCommandException;
import at.gv.egiz.bku.slexceptions.SLRequestException;
import at.gv.egiz.bku.slexceptions.SLRuntimeException;
import at.gv.egiz.bku.slexceptions.SLVersionException;

public class SLCommandFactoryTest {
  
  protected static ApplicationContext appCtx;
  protected SLCommandFactory factory;
  
  @BeforeClass
  public static void setUpClass() {
    appCtx = new ClassPathXmlApplicationContext("at/gv/egiz/bku/slcommands/testApplicationContext.xml");
  }
  
  @Before
  public void setUp() {
    Object bean = appCtx.getBean("slCommandFactory");
    assertTrue(bean instanceof SLCommandFactory);
    
    factory = (SLCommandFactory) bean;
  }
  
  @Test
  public void createNullOperationCommand() throws SLCommandException, SLRuntimeException, SLRequestException, SLVersionException {
    Reader requestReader = new StringReader(
        "<NullOperationRequest xmlns=\"http://www.buergerkarte.at/namespaces/securitylayer/1.2#\"/>");
    StreamSource source = new StreamSource(requestReader);
    
    SLCommand slCommand = factory.createSLCommand(source);
    
    assertTrue(slCommand instanceof NullOperationCommand);
  }

  @Test(expected=SLCommandException.class)
  public void createUnsupportedCommand() throws SLCommandException, SLRuntimeException, SLRequestException, SLVersionException {
    Reader requestReader = new StringReader(
      "<CreateHashRequest xmlns=\"http://www.buergerkarte.at/namespaces/securitylayer/1.2#\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.buergerkarte.at/namespaces/securitylayer/1.2# file:/home/clemens/IAIK/BKU2/svn/bku/utils/src/main/schema/Core-1.2.xsd\"><HashInfo RespondHashData=\"true\"><HashData><MetaInfo><MimeType></MimeType></MetaInfo><Content><XMLContent></XMLContent></Content></HashData><HashAlgorithm></HashAlgorithm></HashInfo></CreateHashRequest>");
    StreamSource source = new StreamSource(requestReader);
    
    factory.createSLCommand(source);
    
  }
  
  @Test(expected=SLRequestException.class)
  public void createMalformedCommand() throws SLCommandException, SLRuntimeException, SLRequestException, SLVersionException {
    Reader requestReader = new StringReader(
        "<NullOperationRequest xmlns=\"http://www.buergerkarte.at/namespaces/securitylayer/1.2#\">" +
          "missplacedContent" +
        "</NullOperationRequest>");
    StreamSource source = new StreamSource(requestReader);
    
    factory.createSLCommand(source);
    
  }
  
}
