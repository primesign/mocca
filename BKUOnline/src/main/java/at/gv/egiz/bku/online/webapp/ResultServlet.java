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
package at.gv.egiz.bku.online.webapp;

import java.io.IOException;
import java.util.Iterator;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import at.gv.egiz.bku.binding.BindingProcessor;
import at.gv.egiz.bku.binding.HTTPBindingProcessor;
import at.gv.egiz.bku.binding.Id;
import at.gv.egiz.bku.binding.IdFactory;
import at.gv.egiz.bku.utils.NullOutputStream;

/**
 * Delivers the result to the browser
 * 
 */
public class ResultServlet extends SpringBKUServlet {

  private static final long serialVersionUID = 1L;

  private final Logger log = LoggerFactory.getLogger(ResultServlet.class);

  private String encoding = "UTF-8";
  private String expiredPage = "./expiredError.jsp";

  public ResultServlet() {
  }

  private void myInit() {
    String enc = getServletContext().getInitParameter("responseEncoding");
    if (enc != null) {
      log.trace("Init default encoding to: {}.", enc);
      encoding = enc;
    }
    String expP = getServletConfig().getInitParameter("expiredPage");
    if (expP != null) {
      log.trace("Init expired page to: {}.", expP);
      expiredPage = expP;
    }
  }

  @Override
  public void init() throws ServletException {
    super.init();
    myInit();
  }

  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    myInit();
  }

  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    doGet(req, resp);
  }

  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, java.io.IOException { 

    HttpSession session = req.getSession(false);
    if (session == null) {
      resp.sendRedirect(expiredPage);
      return;
    }

    Id id = IdFactory.getInstance().createId(session.getId());

    HTTPBindingProcessor bp;
    BindingProcessor bindingProcessor = getBindingProcessorManager().getBindingProcessor(id);
    if (bindingProcessor instanceof HTTPBindingProcessor) {
      bp = (HTTPBindingProcessor) bindingProcessor;
    } else {
      session.invalidate();
      resp.sendRedirect(expiredPage);
      return;
    }
    MDC.put("id", id.toString());
    
    try {
      String redirectUrl = (String) session
          .getAttribute(AbstractWebRequestHandler.REDIRECT_URL_SESSION_ATTRIBUTE);
      if (redirectUrl == null) {
        redirectUrl = bp.getRedirectURL();
      }
      if (redirectUrl != null) {
        try {
          bp.writeResultTo(new NullOutputStream(), encoding);
          getBindingProcessorManager().removeBindingProcessor(bp.getId());
        } finally {
          log.info("Sending deferred redirect, RedirectURL={}.", redirectUrl);
          resp.sendRedirect(redirectUrl);
          session.invalidate();
        }
        return;
      }
  
      log.trace("Setting response code: {}.", bp.getResponseCode());
      resp.setStatus(bp.getResponseCode());
      resp.setHeader("Cache-Control", "no-store"); // HTTP 1.1
      resp.setHeader("Pragma", "no-cache"); // HTTP 1.0
      resp.setDateHeader("Expires", 0);
      for (Iterator<String> it = bp.getResponseHeaders().keySet().iterator(); it
          .hasNext();) {
        String header = it.next();
        log.trace("Setting response header {}: {}.", header, bp.getResponseHeaders().get(header));
        resp.setHeader(header, bp.getResponseHeaders().get(header));
      }
      resp.setContentType(bp.getResultContentType());
      resp.setCharacterEncoding(encoding);
      log.info("Sending result.");
      bp.writeResultTo(resp.getOutputStream(), encoding);
      resp.getOutputStream().flush();
      session.invalidate();
      getBindingProcessorManager().removeBindingProcessor(bp.getId());
      
    } finally {
      MDC.remove("id");
    }
  }
}
