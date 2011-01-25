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

package at.gv.egiz.bku.online.webapp;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Locale;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.gv.egiz.bku.binding.BindingProcessorManager;
import at.gv.egiz.bku.binding.HTTPBindingProcessor;
import at.gv.egiz.bku.binding.HttpUtil;
import at.gv.egiz.bku.binding.Id;
import at.gv.egiz.bku.binding.InputDecoderFactory;
import at.gv.egiz.org.apache.tomcat.util.http.AcceptLanguage;

public class WebRequestHandler extends HttpServlet {

  private static final long serialVersionUID = 1L;
  
  private final Logger log = LoggerFactory.getLogger(WebRequestHandler.class);

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
      java.io.IOException {

    BindingProcessorManager bindingProcessorManager = (BindingProcessorManager) getServletContext()
        .getAttribute("bindingProcessorManager");
    if (bindingProcessorManager == null) {
      String msg = "Configuration error: BindingProcessorManager missing!";
      log.error(msg);
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, msg);
      return;
    }

    Id id = (Id) req.getAttribute("id");
    if (id == null) {
      String msg = "No request id! Configuration error: ServletFilter missing?"; 
      log.error(msg);
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, msg);
      return;
    }
    
    // if binding processor with same id is present: remove
    bindingProcessorManager.removeBindingProcessor(id);
    
    Locale locale = AcceptLanguage.getLocale(req.getHeader("Accept-Language"));
    if (log.isInfoEnabled()) {
      log.info("Recieved request (Accept-Language locale: {}).", locale);
    }
    
    // create new binding processor
    String protocol = getServletConfig().getInitParameter("protocol");
    if (protocol == null || protocol.isEmpty()) {
      protocol = req.getScheme();
    }
    HTTPBindingProcessor bindingProcessor = (HTTPBindingProcessor) bindingProcessorManager
        .createBindingProcessor(protocol, locale);    

    // set headers
    LinkedHashMap<String, String> headerMap = new LinkedHashMap<String, String>();
    if (req.getHeaderNames() != null) {
      for (Enumeration<?> headerName = req.getHeaderNames(); headerName
          .hasMoreElements();) {
        String name = (String) headerName.nextElement();
        // Account for multiple headers with the same field-name, but
        // they are very rare, so we are not using a StringBuffer.
        Enumeration<?> headers = req.getHeaders(name);
        String value = null;
        while (headers.hasMoreElements()) {
          value = (value == null) 
              ? (String) headers.nextElement() 
              : value + ", " + headers.nextElement();
        }
        headerMap.put(name, value);
      }
    }
    
    // set request stream 
    InputStream inputStream;
    if (req.getMethod().equals("POST")) {
      inputStream = req.getInputStream();
    } else {
      headerMap.put(HttpUtil.HTTP_HEADER_CONTENT_TYPE, InputDecoderFactory.URL_ENCODED);
      String queryString = req.getQueryString();
      if (queryString != null) {
        inputStream = new ByteArrayInputStream(queryString.getBytes("UTF-8"));
      } else {
        inputStream = new ByteArrayInputStream(new byte[] {});
      }
    }

    bindingProcessor.setHTTPHeaders(headerMap);
    bindingProcessor.consumeRequestStream(req.getRequestURL().toString(), inputStream);
    inputStream.close();

    // process
    bindingProcessorManager.process(id, bindingProcessor);
  
    log.debug("Sending redirect to user interface.");
    resp.sendRedirect(resp.encodeRedirectURL("ui"));
      
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, java.io.IOException {
    doPost(req, resp);
  }
  
}
