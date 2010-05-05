/*
* Copyright 2009 Federal Chancellery Austria and
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
