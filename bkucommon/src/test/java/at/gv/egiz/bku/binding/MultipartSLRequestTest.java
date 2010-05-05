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

import static org.junit.Assert.*;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class MultipartSLRequestTest extends AbstractBindingProcessorTest {

  @Test
  public void testMultipartFromTutorial() throws MalformedURLException {

    HTTPBindingProcessorImpl http = (HTTPBindingProcessorImpl) createBindingProcessor("http");

    Map<String, String> headers = new HashMap<String, String>();
    headers.put("Content-Type", InputDecoderFactory.MULTIPART_FORMDATA
        + ";boundary=---------------------------2330864292941");
    http.setHTTPHeaders(headers);

    InputStream dataStream = getClass().getClassLoader().getResourceAsStream(
        "at/gv/egiz/bku/binding/MultipartFromTutorial.txt");

    http.consumeRequestStream("http://localhost:3495/http-security-layer-request", dataStream);
    http.run();
    
    assertNotNull(http.bindingProcessorError);
    assertEquals(4011, http.bindingProcessorError.getErrorCode());

  }

  @Test
  public void testMultipartEmpty() throws MalformedURLException, ClassNotFoundException {

    HTTPBindingProcessorImpl http = (HTTPBindingProcessorImpl) createBindingProcessor("http");
    
    Map<String, String> headers = new HashMap<String, String>();
    headers.put("Content-Type", InputDecoderFactory.MULTIPART_FORMDATA
        + ";boundary=uW10q_I9UeqKyw-1o5EW4jtEAaGs7-mC6o");
    http.setHTTPHeaders(headers);
   
    InputStream dataStream = getClass().getClassLoader().getResourceAsStream(
        "at/gv/egiz/bku/binding/MultipartEmpty.txt");
    
    http.consumeRequestStream("http://localhost:3495/http-security-layer-request", dataStream);
    http.run();
    
    if (http.bindingProcessorError != null) {
      fail(http.bindingProcessorError.getMessage());
    }
    
  }

  @Test
  public void testNulloperationRequest() throws MalformedURLException, ClassNotFoundException {

    HTTPBindingProcessorImpl http = (HTTPBindingProcessorImpl) createBindingProcessor("http");
    
    Map<String, String> headers = new HashMap<String, String>();
    headers.put("Content-Type", "application/x-www-form-urlencoded");
    http.setHTTPHeaders(headers);
   
    InputStream dataStream = getClass().getClassLoader().getResourceAsStream(
        "at/gv/egiz/bku/binding/NulloperationRequest.txt.bin");
    
    http.consumeRequestStream("http://localhost:3495/http-security-layer-request", dataStream);
    http.run();
    
    if (http.bindingProcessorError != null) {
      fail(http.bindingProcessorError.getMessage());
    }
    
  }

  
}
