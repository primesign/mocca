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
package at.gv.egiz.bku.slcommands.impl;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;

import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import at.gv.egiz.bku.slcommands.ErrorResult;
import at.gv.egiz.bku.slcommands.InfoboxReadCommand;
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
public class InfoboxReadComandImplTest {

  protected static ApplicationContext appCtx;
  private SLCommandFactory factory;
  
  private STAL stal;
  
  private URLDereferencer urlDereferencer;
  
  @BeforeClass
  public static void setUpClass() {
    appCtx = new ClassPathXmlApplicationContext("at/gv/egiz/bku/slcommands/testApplicationContext.xml");
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
  public void testInfboxReadRequest() throws SLCommandException, SLRuntimeException, SLRequestException, SLVersionException {
    InputStream inputStream = getClass().getClassLoader().getResourceAsStream("at/gv/egiz/bku/slcommands/infoboxreadcommand/IdentityLink.Binary.xml");
    assertNotNull(inputStream);
    
    SLCommandContext context = new SLCommandContext(stal, urlDereferencer);
    context.setSTAL(stal);
    SLCommand command = factory.createSLCommand(new StreamSource(inputStream));
    assertTrue(command instanceof InfoboxReadCommand);
    
    SLResult result = command.execute(context);
    result.writeTo(new StreamResult(System.out), false);
  }
  
  @Test(expected=SLCommandException.class)
  public void testInfboxReadRequestInvalid1() throws SLCommandException, SLRuntimeException, SLRequestException, SLVersionException {
    InputStream inputStream = getClass().getClassLoader().getResourceAsStream("at/gv/egiz/bku/slcommands/infoboxreadcommand/IdentityLink.Binary.Invalid-1.xml");
    assertNotNull(inputStream);
    
    SLCommand command = factory.createSLCommand(new StreamSource(inputStream));
    assertTrue(command instanceof InfoboxReadCommand);
  }

  public void testInfboxReadRequestInvalid2() throws SLCommandException, SLRuntimeException, SLRequestException, SLVersionException {
    InputStream inputStream = getClass().getClassLoader().getResourceAsStream("at/gv/egiz/bku/slcommands/infoboxreadcommand/IdentityLink.Binary.Invalid-2.xml");
    assertNotNull(inputStream);
    
    SLCommandContext context = new SLCommandContext(stal, urlDereferencer);
    SLCommand command = factory.createSLCommand(new StreamSource(inputStream));
    assertTrue(command instanceof InfoboxReadCommand);
    
    SLResult result = command.execute(context);
    assertTrue(result instanceof ErrorResult);
  }

}
