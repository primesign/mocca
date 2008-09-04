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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package at.gv.egiz.bku.binding;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/**
 * 
 * @author clemens
 */
public class DataUrlConnectionTest {

  public static final String REQUEST_RESOURCE = "at/gv/egiz/bku/binding/NOPMultipartDataUrl.txt";

  private static final Log log = LogFactory.getLog(DataUrlConnectionTest.class);

  static HttpServer server;
  static BindingProcessor bindingProcessor;
  static BindingProcessorManager manager;

  protected InputStream requestStream;

  @BeforeClass
  public static void setUpHTTPServer() throws IOException {
    log.debug("setting up HTTPServer");
    InetSocketAddress addr = new InetSocketAddress("localhost", 8081);
    server = HttpServer.create(addr, 0);
    server.createContext("/", new DataUrlHandler());
    server.start();

    log.debug("setting up HTTPBindingProcessor");
    manager = new BindingProcessorManagerImpl(new DummyStalFactory(),
        new SLCommandInvokerImpl());
    bindingProcessor = (HTTPBindingProcessor) manager.createBindingProcessor(
        "http://www.iaik.at", null);
    Map<String, String> headers = new HashMap<String, String>();
    headers.put("Content-Type", InputDecoderFactory.MULTIPART_FORMDATA
        + ";boundary=---------------------------2330864292941");
    ((HTTPBindingProcessor) bindingProcessor).setHTTPHeaders(headers);
  }

  @Before
  public void setUp() {
    requestStream = getClass().getClassLoader().getResourceAsStream(
        REQUEST_RESOURCE);
  }

  @AfterClass
  public static void stopServer() {
    if (server != null) {
      log.debug("stopping HTTPServer");
      server.stop(0);
    }
  }

  @Test
  public void testBasicNop() {
    bindingProcessor.consumeRequestStream(requestStream);
    // manager.process(bindingProcessor);
    bindingProcessor.run();
  }

//  @Test
  public void openConnectionTest() throws Exception {

    URL dataUrl = new URL("http://localhost:8081/");

    log.debug("creating DataUrlConnection " + dataUrl.toString());
    DataUrlConnectionImpl c = new DataUrlConnectionImpl();
    c.init(dataUrl);

    c.setHTTPHeader("httpHeader_1", "001");
    ByteArrayInputStream bais = new ByteArrayInputStream("Hello, world!"
        .getBytes());
    c.setHTTPFormParameter("formParam_1", bais, "text/plain", "UTF-8", null);

    log.debug("open dataUrl connection");
    c.connect();
    //TODO mock SLResult and c.transmit(result);
  }

  static class DataUrlHandler implements HttpHandler {

    public DataUrlHandler() {
      log.debug("setting up DataUrlHandler");
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
      log.debug("handling incoming request");
      logHTTPHeaders(exchange.getRequestHeaders());
      logRequest(exchange.getRequestBody());

      log.debug("sending dummy response");
      exchange.getResponseHeaders().add("Content-type", "text/html");
      String response = "<b>" + new Date() + "</b> for "
          + exchange.getRequestURI();
      exchange.sendResponseHeaders(200, response.length());

      OutputStream os = exchange.getResponseBody();
      os.write(response.getBytes());
      os.close();
    }

    private void logRequest(InputStream in) throws IOException {
      StringBuilder reqLogMsg = new StringBuilder("HTTP request: \n");
      int c = 0;
      while ((c = in.read()) != -1) {
        reqLogMsg.append((char) c);
      }
      log.debug(reqLogMsg.toString());
      in.close();
    }

    private void logHTTPHeaders(Headers headers) {
      StringBuilder headersLogMsg = new StringBuilder("HTTP headers: \n");
      Set<String> keys = headers.keySet();
      Iterator<String> keysIt = keys.iterator();
      while (keysIt.hasNext()) {
        String key = keysIt.next();
        List<String> values = headers.get(key);
        Iterator<String> valuesIt = values.iterator();
        headersLogMsg.append(' ');
        headersLogMsg.append(key);
        headersLogMsg.append(": ");
        while (valuesIt.hasNext()) {
          headersLogMsg.append(valuesIt.next());
          headersLogMsg.append(' ');
        }
        headersLogMsg.append('\n');
      }
      log.debug(headersLogMsg.toString());
    }
  }
}
