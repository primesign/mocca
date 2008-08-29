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
package at.gv.egiz.bku.local.webapp;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.HttpRequestHandler;

import at.gv.egiz.bku.binding.BindingProcessorManager;
import at.gv.egiz.bku.binding.HTTPBindingProcessor;
import at.gv.egiz.bku.binding.HttpUtil;
import at.gv.egiz.bku.utils.StreamUtil;
import at.gv.egiz.org.apache.tomcat.util.http.AcceptLanguage;

public abstract class BKURequestHandler extends HttpServlet {

  public final static String ENCODING = "UTF-8";

  protected Log log = LogFactory.getLog(BKURequestHandler.class);

  protected abstract BindingProcessorManager getBindingProcessorManager();

  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, java.io.IOException {
    log.debug("Got new request");
    String lang = req.getHeader("Accept-Language");
    Locale locale = AcceptLanguage.getLocale(lang);
    log.debug("Using locale: "+locale);
    HTTPBindingProcessor bindingProcessor;
    if (req.isSecure()) {
      bindingProcessor = (HTTPBindingProcessor) getBindingProcessorManager()
          .createBindingProcessor("https", null, locale);
    } else {
      bindingProcessor = (HTTPBindingProcessor) getBindingProcessorManager()
          .createBindingProcessor("http", null, locale);
    }
    Map<String, String> headerMap = new HashMap<String, String>();
    for (Enumeration<String> headerName = req.getHeaderNames(); headerName
        .hasMoreElements();) {
      String header = headerName.nextElement();
      if (header != null) {
        headerMap.put(header, req.getHeader(header));
      }
    }
    headerMap.put(HttpUtil.HTTP_HEADER_CONTENT_TYPE, req.getContentType()+";"+req.getCharacterEncoding());
    bindingProcessor.setHTTPHeaders(headerMap);
    bindingProcessor.consumeRequestStream(req.getInputStream());

    // fixxme just for testing
    bindingProcessor.run();
    if (bindingProcessor.getRedirectURL() != null) {
      resp.sendRedirect(bindingProcessor.getRedirectURL());
      return;
    }
    resp.setStatus(bindingProcessor.getResponseCode());
    for (Iterator<String> it = bindingProcessor.getResponseHeaders().keySet()
        .iterator(); it.hasNext();) {
      String header = it.next();
      resp.setHeader(header, bindingProcessor.getResponseHeaders().get(header));
    }
    resp.setContentType(bindingProcessor.getResultContentType());
    resp.setCharacterEncoding(ENCODING);
    bindingProcessor.writeResultTo(resp.getOutputStream(), ENCODING);
    req.getInputStream().close();
    resp.getOutputStream().flush();
    resp.getOutputStream().close();
    log.debug("Finished Request");
  }

  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, java.io.IOException {
    doPost(req, resp);
  }
}
