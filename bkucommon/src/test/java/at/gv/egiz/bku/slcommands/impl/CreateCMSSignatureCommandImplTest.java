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


package at.gv.egiz.bku.slcommands.impl;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import iaik.xml.crypto.XSecProvider;

import java.io.InputStream;
import java.io.InputStreamReader;

import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import at.gv.egiz.bku.slcommands.CreateCMSSignatureCommand;
import at.gv.egiz.bku.slcommands.SLCommand;
import at.gv.egiz.bku.slcommands.SLCommandContext;
import at.gv.egiz.bku.slcommands.SLCommandFactory;
import at.gv.egiz.bku.slcommands.SLResult;
import at.gv.egiz.bku.slexceptions.SLCommandException;
import at.gv.egiz.bku.slexceptions.SLRequestException;
import at.gv.egiz.bku.slexceptions.SLRuntimeException;
import at.gv.egiz.bku.slexceptions.SLVersionException;
import at.gv.egiz.bku.utils.urldereferencer.URLDereferencer;
import at.gv.egiz.stal.STAL;
import at.gv.egiz.stal.STALFactory;

//@Ignore
public class CreateCMSSignatureCommandImplTest {

  protected static ApplicationContext appCtx;
  private SLCommandFactory factory;

  private STAL stal;
  
  private URLDereferencer urlDereferencer;
  
  @BeforeClass
  public static void setUpClass() {
    appCtx = new ClassPathXmlApplicationContext("at/gv/egiz/bku/slcommands/testApplicationContext.xml");
    XSecProvider.addAsProvider(true);
  }
  
  @Before
  public void setUp() {
    Object bean = appCtx.getBean("slCommandFactory");
    assertTrue(bean instanceof SLCommandFactory);
    
    factory = (SLCommandFactory) bean;
    
    bean = appCtx.getBean("stalFactory");
    assertTrue(bean instanceof STALFactory);
    
    stal = ((STALFactory) bean).createSTAL();
    
    bean = appCtx.getBean("urlDereferencer");
    assertTrue(bean instanceof URLDereferencer);
    
    urlDereferencer = (URLDereferencer) bean;

  }
  
  @Test
  public void testCreateCMSSignatureRequest() throws SLCommandException, SLRuntimeException, SLRequestException, SLVersionException {
    InputStream inputStream = getClass().getClassLoader().getResourceAsStream("at/gv/egiz/bku/slcommands/createcmssignaturerequest/CreateCMSSignatureRequest.xml");
    assertNotNull(inputStream);
    
    SLCommand command = factory.createSLCommand(new StreamSource(new InputStreamReader(inputStream)));
    assertTrue(command instanceof CreateCMSSignatureCommand);
    
    SLCommandContext context = new SLCommandContext(stal, urlDereferencer, null);
    SLResult result = command.execute(context);
    result.writeTo(new StreamResult(System.out), false);
  }
}
