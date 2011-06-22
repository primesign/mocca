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
