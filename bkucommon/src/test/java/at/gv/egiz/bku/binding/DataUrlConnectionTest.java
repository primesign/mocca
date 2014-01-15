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


/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package at.gv.egiz.bku.binding;

import static org.junit.Assert.*;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import at.gv.egiz.bku.slexceptions.SLException;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/**
 * 
 * @author clemens
 */
public class DataUrlConnectionTest extends AbstractBindingProcessorTest {

  public static final String REQUEST_RESOURCE = "at/gv/egiz/bku/binding/NOPMultipartDataUrl.txt";

  private final Logger log = LoggerFactory.getLogger(DataUrlConnectionTest.class);

  private static final int PORT = 8082;

  static HttpServer server;
  static HTTPBindingProcessorImpl bindingProcessor;

  protected InputStream requestStream;

  @BeforeClass
  public static void setUpHTTPServer() throws IOException {
    Logger log = LoggerFactory.getLogger(DataUrlConnectionTest.class);
    log.debug("setting up HTTPServer");
    InetSocketAddress addr = new InetSocketAddress("localhost", PORT);
    server = HttpServer.create(addr, 0);
    server.createContext("/", new DataUrlHandler());
    server.start();

    log.debug("setting up HTTPBindingProcessor");
    bindingProcessor = (HTTPBindingProcessorImpl) createBindingProcessor("http");
    
    Map<String, String> headers = new HashMap<String, String>();
    headers.put("Content-Type", InputDecoderFactory.MULTIPART_FORMDATA
        + ";boundary=---------------------------2330864292941");
    ((HTTPBindingProcessorImpl) bindingProcessor).setHTTPHeaders(headers);
  }

  @Before
  public void setUp() {
    requestStream = getClass().getClassLoader().getResourceAsStream(
        REQUEST_RESOURCE);
  }

  @AfterClass
  public static void stopServer() {
    if (server != null) {
      Logger log = LoggerFactory.getLogger(DataUrlConnectionTest.class);
      log.debug("stopping HTTPServer");
      server.stop(0);
    }
  }

  @Test
  public void testBasicNop() {
    bindingProcessor.consumeRequestStream("http://localhost:3495/http-security-layer-request", requestStream);
    bindingProcessor.run();
    SLException e = bindingProcessor.bindingProcessorError;
    if (e != null) {
      fail(e.getMessage());
    }
  }

//  @Test
  public void openConnectionTest() throws Exception {

    URL dataUrl = new URL("http://localhost:" + PORT + "/");

    log.debug("creating DataUrlConnection " + dataUrl.toString());
    DataUrlConnectionImpl c = new DataUrlConnectionImpl(dataUrl);

    c.setHTTPHeader("httpHeader_1", "001");
    ByteArrayInputStream bais = new ByteArrayInputStream("Hello, world!"
        .getBytes());
    c.setHTTPFormParameter("formParam_1", bais, "text/plain", "UTF-8", null);

    log.debug("open dataUrl connection");
    c.connect();
    //TODO mock SLResult and c.transmit(result);
  }

  static class DataUrlHandler implements HttpHandler {

    private final Logger log = LoggerFactory.getLogger(DataUrlConnectionTest.class);
    
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
