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



package at.gv.egiz.bku.binding;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import iaik.security.ecc.provider.ECCProvider;
import iaik.security.provider.IAIK;
import iaik.xml.crypto.XSecProvider;

import java.security.Security;

import org.junit.BeforeClass;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import at.gv.egiz.bku.slcommands.SLCommandInvoker;
import at.gv.egiz.stal.STAL;

public abstract class AbstractBindingProcessorTest {

  protected static AbstractApplicationContext ctx;

  @BeforeClass
  public static void setUpClass() {
    Security.insertProviderAt(new IAIK(), 1);
    Security.insertProviderAt(new ECCProvider(false), 2);
    XSecProvider.addAsProvider(false);

    ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(
        "at/gv/egiz/bku/slcommands/testApplicationContext.xml");
    assertNotNull(ctx);
    HttpBindingProcessorTest.ctx = ctx;
  }

  protected static BindingProcessorManager getBindingProcessorManager() {
    Object bean = ctx.getBean("bindingProcessorManager");
    assertTrue(bean instanceof BindingProcessorManagerImpl);
    BindingProcessorManagerImpl manager = (BindingProcessorManagerImpl) bean;

    assertNotNull(manager.getCommandInvoker());
    assertNotNull(manager.getStalFactory());
    return manager;
  }
  
  public static BindingProcessor createBindingProcessor(String protocol) {
    
    BindingProcessorManagerImpl manager = (BindingProcessorManagerImpl) getBindingProcessorManager();

    assertNotNull(manager.getCommandInvoker());
    assertNotNull(manager.getStalFactory());
    
    BindingProcessor bindingProcessor = manager.createBindingProcessor(protocol);
    SLCommandInvoker commandInvoker = manager.getCommandInvoker().newInstance();
    STAL stal = manager.getStalFactory().createSTAL();
    bindingProcessor.init("test", stal, commandInvoker);

    return bindingProcessor;
    
  }
  
}
