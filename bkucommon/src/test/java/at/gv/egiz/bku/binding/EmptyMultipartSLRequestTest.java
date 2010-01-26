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
import iaik.security.ecc.provider.ECCProvider;
import iaik.security.provider.IAIK;
import iaik.xml.crypto.XSecProvider;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.security.Provider;
import java.security.Security;
import java.security.Provider.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;

import at.gv.egiz.bku.conf.Configurator;
import at.gv.egiz.bku.conf.DummyConfiguration;
import at.gv.egiz.bku.slcommands.SLCommandFactory;
import at.gv.egiz.bku.slcommands.impl.xsect.STALProvider;

public class EmptyMultipartSLRequestTest {
  
  private static Log log = LogFactory.getLog(EmptyMultipartSLRequestTest.class);

  protected String resourceName = "at/gv/egiz/bku/binding/MultipartEmpty.txt";

  protected BindingProcessor bindingProcessor;
  protected InputStream dataStream;
  protected BindingProcessorManager manager;

  @Before
  public void setUp() throws MalformedURLException, ClassNotFoundException {
    manager = new BindingProcessorManagerImpl(new DummyStalFactory(),
        new SLCommandInvokerImpl(), new DummyConfiguration());
    HTTPBindingProcessor http = (HTTPBindingProcessor) manager
        .createBindingProcessor("http://www.at/", null);
    Map<String, String> headers = new HashMap<String, String>();
    headers.put("Content-Type", InputDecoderFactory.MULTIPART_FORMDATA
        + ";boundary=uW10q_I9UeqKyw-1o5EW4jtEAaGs7-mC6o");
    http.setHTTPHeaders(headers);
    dataStream = getClass().getClassLoader().getResourceAsStream(resourceName);
    bindingProcessor = http;
    Map<String, String> commandMap = new HashMap<String, String>();
    commandMap
        .put(
            "http://www.buergerkarte.at/namespaces/securitylayer/1.2#:CreateXMLSignatureRequest",
            "at.gv.egiz.bku.slcommands.impl.CreateXMLSignatureCommandImpl");
    commandMap
        .put(
            "http://www.buergerkarte.at/namespaces/securitylayer/1.2#:InfoboxReadRequest",
            "at.gv.egiz.bku.slcommands.impl.InfoboxReadCommandImpl");
    SLCommandFactory.getInstance().setCommandImpl(commandMap);
    Security.insertProviderAt(new IAIK(), 1);
    Security.insertProviderAt(new ECCProvider(false), 2);
    XSecProvider.addAsProvider(false);
    // registering STALProvider as delegation provider for XSECT
    STALProvider stalProvider = new STALProvider();
    Security.addProvider(stalProvider);
    Set<Service> services = stalProvider.getServices();
    StringBuilder sb = new StringBuilder();
    for (Service service : services) {
      String algorithm = service.getType() + "." + service.getAlgorithm();
      XSecProvider.setDelegationProvider(algorithm, stalProvider.getName());
      sb.append("\n" + algorithm);
    }
    log.debug(sb);
  }

  @Test
  public void testBasicNop() {
    bindingProcessor.consumeRequestStream(dataStream);
    // manager.process(bindingProcessor);
    bindingProcessor.run();
  }

}
