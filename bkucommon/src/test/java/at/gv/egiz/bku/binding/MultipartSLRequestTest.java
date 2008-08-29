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

import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

public class MultipartSLRequestTest {

  protected String resourceName = "at/gv/egiz/bku/binding/MultipartFromTutorial.txt";

  protected BindingProcessor bindingProcessor;
  protected InputStream dataStream;
  protected BindingProcessorManager manager;

  @Before
  public void setUp() {
    manager = new BindingProcessorManagerImpl(new DummyStalFactory(),
        new SLCommandInvokerImpl());
    HTTPBindingProcessor http = (HTTPBindingProcessor) manager
        .createBindingProcessor("http", null);
    Map<String, String> headers = new HashMap<String, String>();
    headers.put("Content-Type", InputDecoderFactory.MULTIPART_FORMDATA
        + ";boundary=---------------------------2330864292941");
    http.setHTTPHeaders(headers);
    dataStream = getClass().getClassLoader().getResourceAsStream(resourceName);
    bindingProcessor = http;
  }

  @Test
  public void testBasicNop() {
    bindingProcessor.consumeRequestStream(dataStream);
    // manager.process(bindingProcessor);
    bindingProcessor.run();
  }

}
