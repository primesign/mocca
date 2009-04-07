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
package at.gv.egiz.bku.binding;

import java.net.MalformedURLException;

import org.junit.Test;
import static org.junit.Assert.*;

public class ExpiryRemoverTest {
  
  @Test
  public void testMe() throws InterruptedException, MalformedURLException {
    BindingProcessorManager manager = new BindingProcessorManagerImpl(new DummyStalFactory(),
        new SLCommandInvokerImpl());
    BindingProcessor bp = manager.createBindingProcessor("http://www.at", null);
    ExpiryRemover remover = new ExpiryRemover();
    remover.setBindingProcessorManager(manager);
    remover.execute();
    manager.process(bp);
    remover.execute();
    assertTrue(manager.getManagedIds().size() == 1);
    remover.setMaxAcceptedAge(1000);
    Thread.sleep(100);
    remover.execute();
    assertTrue(manager.getManagedIds().size() == 1);
    Thread.sleep(910);
    remover.execute();
    assertTrue(manager.getManagedIds().size() == 0);
  }
  
  @Test
  public void testMe2() throws InterruptedException, MalformedURLException {
    BindingProcessorManager manager = new BindingProcessorManagerImpl(new DummyStalFactory(),
        new SLCommandInvokerImpl());
    BindingProcessor bp = manager.createBindingProcessor("http://www.iaik.at", null);
    ExpiryRemover remover = new ExpiryRemover();
    remover.setBindingProcessorManager(manager);
    remover.execute();
    manager.process(bp);
    remover.execute();
    assertTrue(manager.getManagedIds().size() == 1);
    remover.setMaxAcceptedAge(1000);
    Thread.sleep(500);
    remover.execute();
    assertTrue(manager.getManagedIds().size() == 1);
    bp.updateLastAccessTime();
    Thread.sleep(510);
    remover.execute();
    assertTrue(manager.getManagedIds().size() == 1);
  }

}
