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

import java.io.IOException;
import java.io.OutputStream;
//import java.net.MalformedURLException;
//import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.gv.egiz.bku.binding.BindingProcessor;
import at.gv.egiz.bku.binding.BindingProcessorManager;
import at.gv.egiz.bku.binding.BindingProcessorManagerImpl;
import at.gv.egiz.bku.binding.HTTPBindingProcessor;
import at.gv.egiz.bku.binding.Id;
import at.gv.egiz.bku.utils.NullOutputStream;

/**
 * Delivers the result to the browser
 * 
 */
public class ResultServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;

  private final Logger log = LoggerFactory.getLogger(ResultServlet.class);

  private String responseEncoding = "UTF-8";
  
  private String expiredPageUrl = "expired.jsp";

  @Override
  public void init() throws ServletException {
    String encoding = MoccaParameterBean.getInitParameter("responseEncoding", getServletConfig(), getServletContext());
    if (encoding != null) {
      log.info("Init default responseEncoding to: {}.", encoding);
      responseEncoding = encoding;
    }
    String url = MoccaParameterBean.getInitParameter("expiredPageUrl", getServletConfig(), getServletContext());
    if (url != null) {
//      try {
//        expiredPageUrl = new URL(url).toString();
//        log.info("Init expiredPageUrl to: {}.", expiredPageUrl);
//      } catch (MalformedURLException e) {
//        log.error("Failed to set expiredUrlPage '{}': {}.", url, e);
//      }
      expiredPageUrl = url;
      log.info("Init expiredPageUrl to: {}.", expiredPageUrl);
    }
  }

  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    doGet(req, resp);
  }

  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, java.io.IOException { 

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
    BindingProcessor bindingProcessor = null;
    if (id == null
        || !((bindingProcessor = bindingProcessorManager
            .getBindingProcessor(id)) instanceof HTTPBindingProcessor)) {
      resp.sendRedirect(expiredPageUrl);
      return;
    }
    
    HTTPBindingProcessor bp = (HTTPBindingProcessor) bindingProcessor;
    
    OutputStream outputStream = null;
    try {
      String redirectUrl = bp.getRedirectURL();
      if (redirectUrl != null && !redirectUrl.trim().isEmpty()) {
        log.info("Sending (deferred) redirect to {}.", redirectUrl);
        resp.sendRedirect(redirectUrl);
        // TODO Couldn't we simply discard the output?
        outputStream = new NullOutputStream();
      } else {
        log.debug("Setting HTTP status code {}.", bp.getResponseCode());
        resp.setStatus(bp.getResponseCode());
        resp.setHeader("Cache-Control", "no-store"); // HTTP 1.1
        resp.setHeader("Pragma", "no-cache"); // HTTP 1.0
        resp.setDateHeader("Expires", 0);
        Map<String, String> responseHeaders = bp.getResponseHeaders();
        for (Entry<String, String> header : responseHeaders.entrySet()) {
          String key = header.getKey();
          String value = header.getValue();
          log.debug("Setting response header {}: {}.", key, value);
          resp.setHeader(key, value);
        }
        resp.setContentType(bp.getResultContentType());
        log.info("Sending result.");
        outputStream = resp.getOutputStream();
      }
      bp.writeResultTo(outputStream, responseEncoding);
    } finally {
      if (outputStream != null)
        outputStream.close();
      bindingProcessorManager.removeBindingProcessor(id);
    }
  }
}
