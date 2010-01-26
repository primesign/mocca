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

import at.gv.egiz.bku.conf.Configuration;
import at.gv.egiz.bku.conf.DummyConfiguration;
import static org.junit.Assert.*;

import java.net.MalformedURLException;

import org.junit.Before;
import org.junit.Test;

public class BindingProcessorManagerTest {
  
  @Before
  public void setUp() {
    IdFactory.getInstance().setNumberOfBits(24*10);
  }

  
  @Test(expected = MalformedURLException.class)
  public void basicCreationTest() throws MalformedURLException {
    //TODO for the moment empty config sufficient (currently only maxDataURLHops configured)
   BindingProcessorManager manager = new BindingProcessorManagerImpl(new DummyStalFactory(), new SLCommandInvokerImpl(), new DummyConfiguration());
   BindingProcessor bp = manager.createBindingProcessor("http://www.at/", null);
   assertNotNull(bp.getId().toString());
   assertEquals(40, bp.getId().toString().length());
   String hansi = "Hansi";
   bp = manager.createBindingProcessor("http://www.iaik.at",hansi);
   assertEquals(hansi, bp.getId().toString()); 
   bp = manager.createBindingProcessor("HtTp://www.iaik.at", null);
   assertNotNull(bp);
   manager.createBindingProcessor("seppl", null);
  }
  
}
