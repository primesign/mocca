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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import at.gv.egiz.bku.binding.MultiTestDataUrlConnection.DataSourceProvider;

public class HttpBindingProcessorTest extends AbstractBindingProcessorTest {

  public static class TestDataSource implements DataSourceProvider {

    private List<Integer> responseCodes = new ArrayList<Integer>();
    private List<String> content = new ArrayList<String>();
    private List<Map<String, String>> responseHeaders = new ArrayList<Map<String, String>>();
    private int counter = -1;

    public void resetCounter() {
      counter = -1;
    }

    public void addResponse(int responseCode, String content,
        Map<String, String> headerMap) {
      responseCodes.add(new Integer(responseCode));
      this.content.add(content);
      this.responseHeaders.add(headerMap);
    }

    @Override
    public int getResponseCode() {
      return responseCodes.get(counter);
    }

    @Override
    public String getResponseContent() {
      return content.get(counter);
    }

    @Override
    public Map<String, String> getResponseHeaders() {
      return responseHeaders.get(counter);
    }

    @Override
    public void nextEvent() {
      if (++counter >= responseCodes.size()) {
        counter = 0;
      }
    }
  }

  protected static String requestUrl = "http://localhost:3495/http-security-layer-request";
  protected static String dataUrl = "http://localhost:8080/dataUrl";

  protected HTTPBindingProcessorImpl bindingProcessor;
  protected Map<String, String> serverHeaderMap;
  protected Map<String, String> clientHeaderMap;
  protected TestDataUrlConnection server;

  @Before
  public void setUp() throws IOException {

    DataUrl.setConnectionFactory(new DataURLConnectionFactory() {
      @Override
      public DataUrlConnection openConnection(URL url) {
        return server;
      }
    });
    serverHeaderMap = new HashMap<String, String>();
    serverHeaderMap.put("Content-Type", HttpUtil.TXT_XML);
    server = new TestDataUrlConnection(new URL(dataUrl));
    server.setResponseCode(200);
    server.setResponseContent("<ok/>");
    server.setResponseHeaders(serverHeaderMap);

    bindingProcessor = (HTTPBindingProcessorImpl) createBindingProcessor("http");
    
    clientHeaderMap = new HashMap<String, String>();
    clientHeaderMap.put("Content-Type",
        "application/x-www-form-urlencoded;charset=utf8");
    bindingProcessor.setHTTPHeaders(clientHeaderMap);
  }

  protected String resultAsString(String encoding) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    bindingProcessor.writeResultTo(baos, encoding);
    return new String(baos.toByteArray(), encoding);
  }

  @Test
  public void testWithoutDataUrlWithoutStylesheet() throws IOException {
    RequestFactory rf = new RequestFactory();
    rf.addForm("Haßnsi", "Wüurzel");
    bindingProcessor.consumeRequestStream(requestUrl, rf.getURLencoded());
    bindingProcessor.run();
    assertEquals(HttpUtil.TXT_XML, bindingProcessor.getResultContentType());
    String result = resultAsString("UTF-8");
    System.out.println(result);
    assertTrue(resultAsString("UTF-8").indexOf("NullOperationResponse") != -1);
    assertEquals(200, bindingProcessor.getResponseCode());
    assertEquals(2, bindingProcessor.getResponseHeaders().size());
  }

  @Test
  public void testWithoutDataUrlWithStylesheet() throws IOException {
    RequestFactory rf = new RequestFactory();
    rf.addForm("Hansi", "Wurzel");
    rf.addFormAsResource("Styleshit", "at/gv/egiz/bku/binding/stylesheet.xslt");
    rf.addForm(RequestFactory.STYLESHEETURL, "formdata:Styleshit");
    bindingProcessor.consumeRequestStream(requestUrl, rf.getURLencoded());
    bindingProcessor.run();
    assertEquals(HttpUtil.TXT_HTML, bindingProcessor.getResultContentType());
    assertTrue(resultAsString("UTF-8").indexOf("NullKommaJosef") != -1);
    assertEquals(200, bindingProcessor.getResponseCode());
    assertEquals(2, bindingProcessor.getResponseHeaders().size());
  }

  @Test
  public void testWithDataUrl301WithStylesheet() throws IOException {
    RequestFactory rf = new RequestFactory();
    rf.addForm(RequestFactory.DATAURL, "http://localhost:8080");
    bindingProcessor.consumeRequestStream(requestUrl, rf.getURLencoded());
    server.setResponseCode(301);
    rf = new RequestFactory();
    rf.addFormAsResource("Styleshit", "at/gv/egiz/bku/binding/stylesheet.xslt");
    rf.addForm(RequestFactory.STYLESHEETURL, "formdata:Styleshit");
    server.setResponseContent(rf.getURLencodedAsString());
    bindingProcessor.run();
    assertEquals(HttpUtil.TXT_XML, bindingProcessor.getResultContentType());
    assertTrue(resultAsString("UTF-8").indexOf("NullOperationRequest") != -1);
    assertEquals(301, bindingProcessor.getResponseCode());
    assertTrue(bindingProcessor.getResponseHeaders().size() > 0);
  }

  @Test
  public void testWithDataUrl302WithStylesheet() throws IOException {
    RequestFactory rf = new RequestFactory();
    rf.addForm(RequestFactory.DATAURL, "http://localhost:8080");
    bindingProcessor.consumeRequestStream(requestUrl, rf.getURLencoded());
    server.setResponseCode(302);
    rf = new RequestFactory();
    rf.addFormAsResource("Styleshit", "at/gv/egiz/bku/binding/stylesheet.xslt");
    rf.addForm(RequestFactory.STYLESHEETURL, "formdata:Styleshit");
    server.setResponseContent(rf.getURLencodedAsString());
    bindingProcessor.run();
    assertEquals(HttpUtil.TXT_XML, bindingProcessor.getResultContentType());
    assertTrue(resultAsString("UTF-8").indexOf("NullOperationRequest") != -1);
    assertEquals(302, bindingProcessor.getResponseCode());
    assertTrue(bindingProcessor.getResponseHeaders().size() > 0);
  }

  @Test
  public void testWithDataUrl303WithStylesheet() throws IOException {
    RequestFactory rf = new RequestFactory();
    rf.addForm(RequestFactory.DATAURL, "http://localhost:8080");
    bindingProcessor.consumeRequestStream(requestUrl, rf.getURLencoded());
    server.setResponseCode(303);
    rf = new RequestFactory();
    rf.addFormAsResource("Styleshit", "at/gv/egiz/bku/binding/stylesheet.xslt");
    rf.addForm(RequestFactory.STYLESHEETURL, "formdata:Styleshit");
    server.setResponseContent(rf.getURLencodedAsString());
    bindingProcessor.run();
    assertEquals(HttpUtil.TXT_XML, bindingProcessor.getResultContentType());
    assertTrue(resultAsString("UTF-8").indexOf("NullOperationRequest") != -1);
    assertEquals(303, bindingProcessor.getResponseCode());
    assertTrue(bindingProcessor.getResponseHeaders().size() > 0);
  }

  @Test
  public void testWithDataUrl306WithStylesheet() throws IOException {
    RequestFactory rf = new RequestFactory();
    rf.addForm(RequestFactory.DATAURL, "http://localhost:8080");
    bindingProcessor.consumeRequestStream(requestUrl, rf.getURLencoded());
    server.setResponseCode(306);
    rf = new RequestFactory();
    rf.addFormAsResource("Styleshit", "at/gv/egiz/bku/binding/stylesheet.xslt");
    rf.addForm(RequestFactory.STYLESHEETURL, "formdata:Styleshit");
    server.setResponseContent(rf.getURLencodedAsString());
    bindingProcessor.run();
    assertEquals(HttpUtil.TXT_XML, bindingProcessor.getResultContentType());
    assertTrue(resultAsString("UTF-8").indexOf("ErrorResponse") != -1);
    assertEquals(200, bindingProcessor.getResponseCode());
    assertTrue(bindingProcessor.getResponseHeaders().size() == 2);
  }

  @Test
  public void testWithDataUrl307NonXML() throws IOException {
    RequestFactory rf = new RequestFactory();
    rf.addForm(RequestFactory.DATAURL, "http://localhost:8080");
    bindingProcessor.consumeRequestStream(requestUrl, rf.getURLencoded());
    server.setResponseCode(307);
    serverHeaderMap.put("Content-Type", HttpUtil.TXT_PLAIN);
    server.setResponseHeaders(serverHeaderMap);
    rf = new RequestFactory();
    rf.addFormAsResource("Styleshit", "at/gv/egiz/bku/binding/stylesheet.xslt");
    rf.addForm(RequestFactory.STYLESHEETURL, "formdata:Styleshit");
    server.setResponseContent(rf.getURLencodedAsString());
    bindingProcessor.run();
    assertEquals(HttpUtil.TXT_PLAIN, bindingProcessor.getResultContentType());
    assertTrue(resultAsString("UTF-8").indexOf("NullOperationRequest") != -1);
    assertEquals(307, bindingProcessor.getResponseCode());
    assertTrue(bindingProcessor.getResponseHeaders().size() > 2);
  }

  @Test
  public void testWithInvalidDataUrl307XML() throws IOException {
    RequestFactory rf = new RequestFactory();
    rf.addForm(RequestFactory.DATAURL, "http://localhost:8080");
    bindingProcessor.consumeRequestStream(requestUrl, rf.getURLencoded());
    server.setResponseCode(307);
    serverHeaderMap.put("Content-Type", HttpUtil.TXT_XML);
    serverHeaderMap.put("Location", "noUrl");
    server.setResponseHeaders(serverHeaderMap);
    rf = new RequestFactory();
    server.setResponseContent(rf.getNullOperationXML());
    bindingProcessor.run();
    assertEquals(HttpUtil.TXT_XML, bindingProcessor.getResultContentType());
    assertTrue(resultAsString("UTF-8").indexOf("ErrorResponse") != -1);
    assertEquals(200, bindingProcessor.getResponseCode());
    assertTrue(bindingProcessor.getResponseHeaders().size() == 2);
  }
  
  @Test
  public void testWithValidDataUrl307XML() throws IOException, InterruptedException {
    server = new MultiTestDataUrlConnection(null);
    TestDataSource tds = new TestDataSource();
    ((MultiTestDataUrlConnection)server).setDataSource(tds);
    
    // first server response with 307 xml and location 
    RequestFactory rf = new RequestFactory();
    serverHeaderMap = new HashMap<String, String>();
    serverHeaderMap.put("Location", "http://localhost:8080");
    serverHeaderMap.put("Content-Type", HttpUtil.TXT_XML);
    tds.addResponse(307, rf.getNullOperationXML(), serverHeaderMap);
    
    // 2nd response with 200 text/plain and != <ok/>
    String testString = "CheckMe";
    serverHeaderMap = new HashMap<String, String>();
    serverHeaderMap.put("Content-Type", HttpUtil.TXT_PLAIN);
    String testHeader ="DummyHeader";
    String testHeaderVal ="DummyHeaderVal";
    serverHeaderMap.put(testHeader, testHeaderVal);
    tds.addResponse(200, testString, serverHeaderMap);
    
    rf = new RequestFactory();
    rf.addForm(RequestFactory.DATAURL, "http://localhost:8080");
    bindingProcessor.consumeRequestStream(requestUrl, rf.getURLencoded());
    bindingProcessor.run();
    
    assertTrue(bindingProcessor.getResponseHeaders().size()>0);
    assertEquals(testHeaderVal, bindingProcessor.getResponseHeaders().get(testHeader));
    assertEquals(200,bindingProcessor.getResponseCode());
    assertEquals(HttpUtil.TXT_PLAIN, bindingProcessor.getResultContentType());
    assertEquals(testString ,resultAsString("UTF-8"));
  }
  
  @Test
  public void testWithValidDataUrl200Urlencoded() throws IOException {
    RequestFactory rf = new RequestFactory();
    rf = new RequestFactory();
    rf.addForm(RequestFactory.DATAURL, "http://localhost:8080");
    bindingProcessor.consumeRequestStream(requestUrl, rf.getURLencoded());
    server.setResponseCode(200);
    rf = new RequestFactory();
    rf.addFormAsResource("Styleshit", "at/gv/egiz/bku/binding/stylesheet.xslt");
    serverHeaderMap.put("Content-Type", HttpUtil.APPLICATION_URL_ENCODED);
    server.setResponseHeaders(serverHeaderMap);
    server.setResponseContent(rf.getURLencodedAsString());
    bindingProcessor.run();
    assertTrue(bindingProcessor.getResponseHeaders().size() == 2);
    assertEquals(200,bindingProcessor.getResponseCode());
    assertEquals(HttpUtil.TXT_XML, bindingProcessor.getResultContentType());
    assertTrue(resultAsString("UTF-8").indexOf("NullOperationResponse") != -1);
  }
  
  @Test
  public void testWithValidDataUrl200UrlencodedAndStylesheet() throws IOException {
    RequestFactory rf = new RequestFactory();
    rf = new RequestFactory();
    rf.addForm(RequestFactory.DATAURL, "http://localhost:8080");
    bindingProcessor.consumeRequestStream(requestUrl, rf.getURLencoded());
    server.setResponseCode(200);
    rf = new RequestFactory();
    rf.addFormAsResource("Styleshit", "at/gv/egiz/bku/binding/stylesheet.xslt");
    rf.addForm(RequestFactory.STYLESHEETURL, "formdata:Styleshit"); 
    serverHeaderMap.put("Content-Type", HttpUtil.APPLICATION_URL_ENCODED);
    server.setResponseHeaders(serverHeaderMap);
    server.setResponseContent(rf.getURLencodedAsString());
    bindingProcessor.run();
    assertTrue(bindingProcessor.getResponseHeaders().size() == 2);
    assertEquals(200,bindingProcessor.getResponseCode());
    assertEquals(HttpUtil.TXT_HTML, bindingProcessor.getResultContentType());
    assertTrue(resultAsString("UTF-8").indexOf("NullKommaJosef") != -1);
  }
  

}
