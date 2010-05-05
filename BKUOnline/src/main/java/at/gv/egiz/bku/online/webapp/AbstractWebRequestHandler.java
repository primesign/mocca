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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import at.gv.egiz.bku.binding.HTTPBindingProcessor;
import at.gv.egiz.bku.binding.HttpUtil;
import at.gv.egiz.bku.binding.Id;
import at.gv.egiz.bku.binding.IdFactory;
import at.gv.egiz.bku.binding.InputDecoderFactory;
import at.gv.egiz.bku.utils.StreamUtil;
import at.gv.egiz.org.apache.tomcat.util.http.AcceptLanguage;

public abstract class AbstractWebRequestHandler extends SpringBKUServlet {

  private static final long serialVersionUID = 1L;
  
  public static final String APPLET_PAGE_P = "appletPage";
  public static final String APPLET_PAGE_DEFAULT = "applet.jsp";
  
  public static final String PARAM_APPLET_WIDTH = "appletWidth";
  public static final String ATTR_APPLET_WIDTH = "appletWidth";
  
  public static final String PARAM_APPLET_HEIGHT = "appletHeight";
  public static final String ATTR_APPLET_HEIGHT = "appletHeight";
  
  public static final String PARAM_APPLET_BACKGROUND = "appletBackground";
  public static final String ATTR_APPLET_BACKGROUND = "appletBackground";
  
  public static final String PARAM_APPLET_BACKGROUND_COLOR = "appletBackgroundColor";
  public static final String ATTR_APPLET_BACKGROUND_COLOR = "appletBackgroundColor";
  
  public static final Pattern PATTERM_APPLET_BACKGROUND_COLOR = Pattern.compile("\\#[0-9a-fA-F]{6}");
  public static final String PARAM_APPLET_GUI_STYLE = "appletGuiStyle";
  public static final String ATTR_APPLET_GUI_STYLE = "appletGuiStyle";
  
  public static final String[] VALUES_APPLET_GUI_STYLE = new String[] {"tiny", "simple", "advanced"};
  public static final String PARAM_APPLET_EXTENSION = "appletExtension";
  public static final String ATTR_APPLET_EXTENSION = "appletExtension";
  
  public static final String[] VALUES_APPLET_EXTENSION = new String[] {"pin", "activation"};
  public static final String PARAM_LOCALE = "locale";
  public static final String ATTR_LOCALE = "locale";
  
  public static final Pattern PATTERN_LOCALE = Pattern.compile("[a-zA-Z][a-zA-Z](_[a-zA-Z][a-zA-Z]){0,2}");
  public static final String REDIRECT_URL_SESSION_ATTRIBUTE = "redirectUrl";
  
  private final Logger log = LoggerFactory.getLogger(BKURequestHandler.class);

  protected static String getStringFromStream(InputStream is, String encoding)
      throws IOException {
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
  
  protected abstract String getRequestProtocol(HttpServletRequest req);
  
  protected HTTPBindingProcessor getBindingProcessor(Id id, HttpServletRequest req, Locale locale) {
    
    // remove existing binding processor if present
    getBindingProcessorManager().removeBindingProcessor(id);
    
    // create new binding processor
    return (HTTPBindingProcessor) getBindingProcessorManager().createBindingProcessor(getRequestProtocol(req), locale);
    
  }
  
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
      java.io.IOException {

    String msg = (req.getSession(false) == null) ? "New session created."
        : "Session already established.";
    
    Id id = IdFactory.getInstance().createId(req.getSession().getId());
    MDC.put("id", id.toString());
    
    String acceptLanguage = req.getHeader("Accept-Language");
    Locale locale = AcceptLanguage.getLocale(acceptLanguage);
    
    if (log.isInfoEnabled()) {
      log.info("Recieved request (Accept-Language locale: {}). {}", locale, msg);
    }
        
    try {
    
      HTTPBindingProcessor bindingProcessor = getBindingProcessor(id, req, locale);
      
      Map<String, String> headerMap = new HashMap<String, String>();
      for (Enumeration<?> headerName = req.getHeaderNames(); headerName
          .hasMoreElements();) {
        String header = (String) headerName.nextElement();
        if (header != null) {
          headerMap.put(header, req.getHeader(header));
        }
      }
          
      InputStream inputStream;
      String charset;
      if (req.getMethod().equals("POST")) {
        charset = req.getCharacterEncoding();
        String contentType = req.getContentType();
        if (charset != null) {
          contentType += ";" + charset;
        }
        headerMap.put(HttpUtil.HTTP_HEADER_CONTENT_TYPE, contentType);
        inputStream = req.getInputStream();
      } else {
        charset = "UTF-8";
        headerMap.put(HttpUtil.HTTP_HEADER_CONTENT_TYPE,
            InputDecoderFactory.URL_ENCODED);
        String queryString = req.getQueryString();
        if (queryString != null) {
          inputStream = new ByteArrayInputStream(queryString.getBytes(charset));
        } else {
          inputStream = new ByteArrayInputStream(new byte[] {});
        }
      }
      bindingProcessor.setHTTPHeaders(headerMap);
      bindingProcessor.consumeRequestStream(req.getRequestURL().toString(),
          inputStream);
  
      req.getInputStream().close();
      getBindingProcessorManager().process(id, bindingProcessor);
  
      HttpSession session = req.getSession();
  
      log.trace("Looking for applet parameters in request.");
  
      // appletWidth
      String width = getStringFromStream(bindingProcessor
          .getFormData(PARAM_APPLET_WIDTH), charset);
      if (width != null && !width.isEmpty()) {
        try {
          // must be a valid integer
          session.setAttribute(ATTR_APPLET_WIDTH, Integer.parseInt(width));
          log.debug("Found parameter " + PARAM_APPLET_WIDTH + "='{}'.", width);
        } catch (NumberFormatException nfe) {
          log.warn("Parameter " + PARAM_APPLET_WIDTH
              + " does not contain a valid value.", nfe);
        }
      }
  
      // appletHeight
      String height = getStringFromStream(bindingProcessor
          .getFormData(PARAM_APPLET_HEIGHT), charset);
      if (height != null && !height.isEmpty()) {
        try {
          // must be a valid integer
          session.setAttribute(ATTR_APPLET_HEIGHT, Integer.parseInt(height));
          log.debug("Found parameter " + PARAM_APPLET_HEIGHT + "='{}'.", height);
        } catch (NumberFormatException nfe) {
          log.warn("Parameter " + PARAM_APPLET_HEIGHT
              + " does not contain a valid value.", nfe);
        }
      }
  
      // appletBackground
      String background = getStringFromStream(bindingProcessor
          .getFormData(PARAM_APPLET_BACKGROUND), charset);
      if (background != null && !background.isEmpty()) {
        session.setAttribute(ATTR_APPLET_BACKGROUND, background);
        try {
          // must be a valid http or https URL
          URI backgroundURL = new URI(background);
          if ("http".equals(backgroundURL.getScheme())
              || "https".equals(backgroundURL.getScheme())) {
            session.setAttribute(ATTR_APPLET_BACKGROUND, backgroundURL
                .toASCIIString());
            log.debug("Found parameter " + PARAM_APPLET_BACKGROUND + "='{}'.",
                backgroundURL.toASCIIString());
          } else {
            log.warn("Parameter " + PARAM_APPLET_BACKGROUND
                + "='{}' is not a valid http/https URL.", background);
          }
        } catch (URISyntaxException e) {
          log.warn("Parameter " + PARAM_APPLET_BACKGROUND
              + "='{}' is not a valid http/https URL.", background, e);
        }
      }
  
      // appletBackgroundColor
      String backgroundColor = getStringFromStream(bindingProcessor
          .getFormData(PARAM_APPLET_BACKGROUND_COLOR), charset);
      if (backgroundColor != null && !backgroundColor.isEmpty()) {
        // must be a valid color definition
        if (PATTERM_APPLET_BACKGROUND_COLOR.matcher(backgroundColor).matches()) {
          session.setAttribute(ATTR_APPLET_BACKGROUND_COLOR, backgroundColor);
          log.debug("Faund parameter " + PARAM_APPLET_BACKGROUND_COLOR
              + "='{}'.", backgroundColor);
        } else {
          log.warn("Parameter " + PARAM_APPLET_BACKGROUND_COLOR
              + "='{}' is not a valid color definition "
              + "(must be of form '#hhhhhh').", backgroundColor);
        }
      }
  
      // appletGuiStyle
      String guiStyle = getStringFromStream(bindingProcessor
          .getFormData(PARAM_APPLET_GUI_STYLE), charset);
      if (guiStyle != null && !guiStyle.isEmpty()) {
        // must be one of VALUES_APPLET_GUI_STYLE
        String style = guiStyle.toLowerCase();
        if (Arrays.asList(VALUES_APPLET_GUI_STYLE).contains(style)) {
          session.setAttribute(ATTR_APPLET_GUI_STYLE, style);
          log.debug("Found parameter " + PARAM_APPLET_GUI_STYLE + "='{}'.", style);
        } else {
          StringBuilder sb = new StringBuilder();
          sb.append("Parameter ").append(PARAM_APPLET_GUI_STYLE).append(
              "='").append(guiStyle).append("' is not valid (must be one of ")
              .append(Arrays.toString(VALUES_APPLET_GUI_STYLE)).append(").");
          log.warn(sb.toString());
        }
      }
  
      // appletExtension
      String extension = getStringFromStream(bindingProcessor
          .getFormData(PARAM_APPLET_EXTENSION), charset);
      if (extension != null && !extension.isEmpty()) {
        // must be one of VALUES_APPLET_EXTENSION
        String ext = extension.toLowerCase();
        if (Arrays.asList(VALUES_APPLET_EXTENSION).contains(ext)) {
          session.setAttribute(ATTR_APPLET_EXTENSION, ext);
          log.debug("Found parameter " + PARAM_APPLET_EXTENSION + "='{}'.", ext);
        } else {
          StringBuilder sb = new StringBuilder();
          sb.append("Parameter ").append(PARAM_APPLET_EXTENSION).append(
              "='").append(extension).append("' is not valid (must be one of ")
              .append(Arrays.toString(VALUES_APPLET_EXTENSION)).append(").");
          log.warn(sb.toString());
        }
      }
  
      // locale
      String localeFormParam = getStringFromStream(bindingProcessor
          .getFormData(PARAM_LOCALE), charset);
      if (localeFormParam != null && !localeFormParam.isEmpty()) {
        // must be a valid locale 
        if (PATTERN_LOCALE.matcher(localeFormParam).matches()) {
          locale = new Locale(localeFormParam);
          log.debug("Override accept-language header locale {} "
              + "with form param {}.", locale, localeFormParam);
        } else {
          log.warn("Parameter " + PARAM_LOCALE
              + "='{}' is not a valid locale definition.", localeFormParam);
        }
      }
      if (locale != null) {
        log.debug("Using locale {}.", locale);
        session.setAttribute(ATTR_LOCALE, locale.toString());
      }
          
      beforeAppletPage(req, bindingProcessor);
          
      String appletPage = getStringFromStream(bindingProcessor
          .getFormData(APPLET_PAGE_P), charset);
      if (appletPage == null || appletPage.isEmpty()) {
        appletPage = APPLET_PAGE_DEFAULT;
      }
      log.debug("Sending redirect to UI page '{}'.", appletPage);
      resp.sendRedirect(appletPage);
      
    } finally {
      MDC.remove("id");
    }
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, java.io.IOException {
    doPost(req, resp);
  }

  /**
   * Called before the request is forwarded or redirected to the Applet page.
   * 
   * @param req
   * @param bindingProcessor
   */
  protected void beforeAppletPage(HttpServletRequest req,
      HTTPBindingProcessor bindingProcessor) {
  }
  
}
