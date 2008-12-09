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
import at.gv.egiz.stal.STAL;
import at.gv.egiz.stal.dummy.DummySTAL;

//@Ignore
public class InfoboxReadComandImplTest {

  private static ApplicationContext appCtx;
  
  private SLCommandFactory factory;
  
  private STAL stal;
  
  @BeforeClass
  public static void setUpClass() {
    appCtx = new ClassPathXmlApplicationContext("at/gv/egiz/bku/slcommands/testApplicationContext.xml");
  }

  @Before
  public void setUp() {
    factory = SLCommandFactory.getInstance();
    stal = new DummySTAL();
  }
  
  @Test
  public void testInfboxReadRequest() throws SLCommandException, SLRuntimeException, SLRequestException {
    InputStream inputStream = getClass().getClassLoader().getResourceAsStream("at/gv/egiz/bku/slcommands/infoboxreadcommand/IdentityLink.Binary.xml");
    assertNotNull(inputStream);
    
    SLCommandContext context = new SLCommandContext();
    context.setSTAL(stal);
    SLCommand command = factory.createSLCommand(new StreamSource(inputStream), context);
    assertTrue(command instanceof InfoboxReadCommand);
    
    SLResult result = command.execute();
    result.writeTo(new StreamResult(System.out));
  }
  
  @Test(expected=SLCommandException.class)
  public void testInfboxReadRequestInvalid1() throws SLCommandException, SLRuntimeException, SLRequestException {
    InputStream inputStream = getClass().getClassLoader().getResourceAsStream("at/gv/egiz/bku/slcommands/infoboxreadcommand/IdentityLink.Binary.Invalid-1.xml");
    assertNotNull(inputStream);
    
    SLCommandContext context = new SLCommandContext();
    context.setSTAL(stal);
    SLCommand command = factory.createSLCommand(new StreamSource(inputStream), context);
    assertTrue(command instanceof InfoboxReadCommand);
  }

  public void testInfboxReadRequestInvalid2() throws SLCommandException, SLRuntimeException, SLRequestException {
    InputStream inputStream = getClass().getClassLoader().getResourceAsStream("at/gv/egiz/bku/slcommands/infoboxreadcommand/IdentityLink.Binary.Invalid-2.xml");
    assertNotNull(inputStream);
    
    SLCommandContext context = new SLCommandContext();
    context.setSTAL(stal);
    SLCommand command = factory.createSLCommand(new StreamSource(inputStream), context);
    assertTrue(command instanceof InfoboxReadCommand);
    
    SLResult result = command.execute();
    assertTrue(result instanceof ErrorResult);
  }

}
