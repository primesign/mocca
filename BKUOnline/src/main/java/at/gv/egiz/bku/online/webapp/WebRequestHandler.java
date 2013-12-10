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

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.gv.egiz.bku.binding.BindingProcessorManager;
import at.gv.egiz.bku.binding.BindingProcessorManagerImpl;
import at.gv.egiz.bku.binding.HTTPBindingProcessor;
import at.gv.egiz.bku.binding.HttpUtil;
import at.gv.egiz.bku.binding.Id;
import at.gv.egiz.bku.binding.InputDecoderFactory;
import at.gv.egiz.org.apache.tomcat.util.http.AcceptLanguage;

public class WebRequestHandler extends HttpServlet {

  private static final long serialVersionUID = 1L;

  private final Logger log = LoggerFactory.getLogger(WebRequestHandler.class);

  private String uiRedirectUrl = "ui";

  @Override
  public void init() throws ServletException {
    String url = MoccaParameterBean.getInitParameter("uiRedirectUrl", getServletConfig(), getServletContext());
    if (url != null) {
      uiRedirectUrl = url;
      log.info("Init uiRedirectUrl to: {}.", uiRedirectUrl);
    }
  }

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

    Configuration conf = ((BindingProcessorManagerImpl) bindingProcessorManager).getConfiguration();
    if (conf == null)
      log.error("No configuration");
    else
      MoccaParameterBean.setP3PHeader(conf, resp);

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
      log.info("Received request (Accept-Language locale: {}).", locale);
    }
    
    // create new binding processor
    String protocol = MoccaParameterBean.getInitParameter("protocol", getServletConfig(), getServletContext());
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
    resp.sendRedirect(resp.encodeRedirectURL(uiRedirectUrl));
      
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, java.io.IOException {
    doPost(req, resp);
  }
}
