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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import at.gv.egiz.bku.binding.HTTPBindingProcessor;
import at.gv.egiz.bku.binding.HttpUtil;
import at.gv.egiz.bku.binding.IdFactory;
import at.gv.egiz.bku.utils.StreamUtil;
import at.gv.egiz.org.apache.tomcat.util.http.AcceptLanguage;

/**
 * Handles SL requests and instantiates BindingProcessors
 * 
 */
public class BKURequestHandler extends SpringBKUServlet {

  private static final long serialVersionUID = 1L;

  public final static String REDIRECT_URL = "appletPage.jsp";

  protected Log log = LogFactory.getLog(BKURequestHandler.class);

  private static String getStringFromStream(InputStream is, String encoding) throws IOException {
    if (is == null) {
      return null;
    }
    if (encoding == null) {
      encoding = HttpUtil.DEFAULT_CHARSET;
    }
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    StreamUtil.copyStream(is, os);
    return new String(os.toByteArray(), encoding);
  }
  
  
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, java.io.IOException {
    log.debug("Got new request");
    HttpSession session = req.getSession();
    String lang = req.getHeader("Accept-Language");
    Locale locale = AcceptLanguage.getLocale(lang);
    log.debug("Using locale: " + locale);

    if (session != null) {
      log.warn("Already a session with id: " + session.getId()
          + " active, deleting this one");
      getBindingProcessorManager().removeBindingProcessor(
          IdFactory.getInstance().createId(session.getId()));
    }
    String id = req.getSession(true).getId();
    log.debug("Using session id: " + id);
    HTTPBindingProcessor bindingProcessor;

    bindingProcessor = (HTTPBindingProcessor) getBindingProcessorManager()
        .createBindingProcessor(req.getRequestURL().toString(), id, locale);

    Map<String, String> headerMap = new HashMap<String, String>();
    for (Enumeration<String> headerName = req.getHeaderNames(); headerName
        .hasMoreElements();) {
      String header = headerName.nextElement();
      if (header != null) {
        headerMap.put(header, req.getHeader(header));
      }
    }
    String charset = req.getCharacterEncoding();
    String contentType = req.getContentType();
    if (charset != null) {
      contentType += ";" + charset;
    }
    headerMap.put(HttpUtil.HTTP_HEADER_CONTENT_TYPE, contentType);
    bindingProcessor.setHTTPHeaders(headerMap);
    bindingProcessor.consumeRequestStream(req.getInputStream());
    req.getInputStream().close();
    getBindingProcessorManager().process(bindingProcessor);
    
    log.trace("Trying to find applet parameters in request");
    String width = getStringFromStream(bindingProcessor.getFormData("appletWidth"), charset);
    String height = getStringFromStream(bindingProcessor.getFormData("appletHeight"), charset);
    String background = getStringFromStream(bindingProcessor.getFormData("appletBackground"), charset);
    String guiStyle = getStringFromStream(bindingProcessor.getFormData("appletGuiStyle"), charset);
    String hashDataDisplay = getStringFromStream(bindingProcessor.getFormData("appletHashDataDisplay"), charset);
    if (width != null) {
      try {
        log.trace("Found applet width parameter: " + width);
        int wI = Integer.parseInt(width);
        session.setAttribute("appletWidth", wI);
      } catch (NumberFormatException nfe) {
        log.warn(nfe);
      }
    }
    if (height != null) {
      try {
        log.trace("Found applet height parameter: " + height);
        int hI = Integer.parseInt(height);
        session.setAttribute("appletHeight", hI);
      } catch (NumberFormatException nfe) {
        log.warn(nfe);
      }
    }
    if (background != null) {
      log.trace("Found applet background parameter: " + background);
      session.setAttribute("appletBackground", background);
    }
    if (guiStyle != null) {
      log.trace("Found applet GUI style parameter: " + guiStyle);
      session.setAttribute("appletGuiStyle", guiStyle);
    }
    if (hashDataDisplay != null) {
      log.trace("Found applet hash data display parameter: " + hashDataDisplay);
      session.setAttribute("appletHashDataDisplay", hashDataDisplay);
    }

    resp.sendRedirect(REDIRECT_URL);
  }

  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, java.io.IOException {
    doPost(req, resp);
  }
}
