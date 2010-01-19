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
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import at.gv.egiz.bku.binding.BindingProcessor;
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

  public static final String APPLET_PAGE_P = "appletPage";
  public static final String APPLET_PAGE_DEFAULT = "BKUApplet";
  
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

  public final static String REDIRECT_URL_SESSION_ATTRIBUTE = "redirectUrl";

  protected Log log = LogFactory.getLog(BKURequestHandler.class);

  private static String getStringFromStream(InputStream is, String encoding)
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

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, java.io.IOException {
    log.debug("Received SecurityLayer request");
    
    HttpSession session = req.getSession(false);
    if (session != null) {
      log.warn("Already a session with id: " + session.getId()
          + " active, trying to get Bindingprocessor");
      BindingProcessor bp = getBindingProcessorManager().getBindingProcessor(
          IdFactory.getInstance().createId(session.getId()));
      if (bp != null) {
        log.debug("Found binding processor, using this one");
        String appletPage = getStringFromStream(
                ((HTTPBindingProcessor) bp).getFormData(APPLET_PAGE_P),
                req.getCharacterEncoding());
        getDispatcher(appletPage).forward(req, resp);
        return;
      }
      log.debug("Did not find a binding processor, creating new ...");
    }
    session = req.getSession(true);
    if (log.isDebugEnabled()) {
      log.debug("Using session id: " + session.getId());
    }

    String acceptLanguage = req.getHeader("Accept-Language");
    Locale locale = AcceptLanguage.getLocale(acceptLanguage);
    log.debug("Accept-Language locale: " + locale);

    HTTPBindingProcessor bindingProcessor;
    bindingProcessor = (HTTPBindingProcessor) getBindingProcessorManager()
        .createBindingProcessor(req.getRequestURL().toString(),
            session.getId(), locale);

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

    // appletWidth
    String width = getStringFromStream(bindingProcessor
        .getFormData(PARAM_APPLET_WIDTH), charset);
    if (width != null) {
      try {
        // must be a valid integer
        session.setAttribute(ATTR_APPLET_WIDTH, Integer.parseInt(width));
        log.trace("Found parameter " + PARAM_APPLET_WIDTH + "='" + width +"'.");
      } catch (NumberFormatException nfe) {
        log.warn("Applet parameter " + PARAM_APPLET_WIDTH + 
            " does not contain a valid value.", nfe);
      }
    }
    
    // appletHeight
    String height = getStringFromStream(bindingProcessor
        .getFormData(PARAM_APPLET_HEIGHT), charset);
    if (height != null) {
      try {
        // must be a valid integer
        session.setAttribute(ATTR_APPLET_HEIGHT, Integer.parseInt(height));
        log.trace("Found parameter " + PARAM_APPLET_HEIGHT + "='" + height + "'.");
      } catch (NumberFormatException nfe) {
        log.warn("Applet parameter " + PARAM_APPLET_HEIGHT + 
            " does not contain a valid value.", nfe);
      }
    }
    
    // appletBackground
    String background = getStringFromStream(bindingProcessor
        .getFormData(PARAM_APPLET_BACKGROUND), charset);
    if (background != null) {
      session.setAttribute(ATTR_APPLET_BACKGROUND, background);
      try {
        // must be a valid http or https URL
        URI backgroundURL = new URI(background);
        if ("http".equals(backgroundURL.getScheme()) 
            || "https".equals(backgroundURL.getScheme())) {
          session.setAttribute(ATTR_APPLET_BACKGROUND, backgroundURL.toASCIIString());
          log.trace("Found parameter " + PARAM_APPLET_BACKGROUND + "='" 
              + backgroundURL.toASCIIString() + "'.");
        } else {
          log.warn("Applet parameter " + PARAM_APPLET_BACKGROUND + "='" 
              + background + "' is not a valid http/https URL.");
        }
      } catch (URISyntaxException e) {
        log.warn("Applet parameter " + PARAM_APPLET_BACKGROUND + "='" 
            + background + "' is not a valid http/https URL.", e);
      }
    }
    
    // appletBackgroundColor
    String backgroundColor = getStringFromStream(bindingProcessor
        .getFormData(PARAM_APPLET_BACKGROUND_COLOR), charset);
    if (backgroundColor != null) {
      // must be a valid color definition
      if (PATTERM_APPLET_BACKGROUND_COLOR.matcher(backgroundColor).matches()) {
        session.setAttribute(ATTR_APPLET_BACKGROUND_COLOR, backgroundColor);
        log.trace("Faund parameter " + PARAM_APPLET_BACKGROUND_COLOR + "='" 
            + backgroundColor + "'.");
      } else {
        log.warn("Applet parameter " + PARAM_APPLET_BACKGROUND_COLOR + "='" 
            + backgroundColor + "' is not a valid color definition (must be of form '#hhhhhh').");
      }
    }
    
    // appletGuiStyle
    String guiStyle = getStringFromStream(bindingProcessor
        .getFormData(PARAM_APPLET_GUI_STYLE), charset);
    if (guiStyle != null) {
      // must be one of VALUES_APPLET_GUI_STYLE
      String style = guiStyle.toLowerCase();
      if (Arrays.asList(VALUES_APPLET_GUI_STYLE).contains(style)) {
        session.setAttribute(ATTR_APPLET_GUI_STYLE, style);
        log.trace("Found parameter " + PARAM_APPLET_GUI_STYLE + "='" 
            + style + "'.");
      } else {
        StringBuilder sb = new StringBuilder();
        sb.append("Applet parameter ").append(PARAM_APPLET_GUI_STYLE).append(
            "='").append(guiStyle).append("' is not valid (must be one of ")
            .append(Arrays.toString(VALUES_APPLET_GUI_STYLE)).append(").");
        log.warn(sb);
      }
    }

    // appletExtension
    String extension = getStringFromStream(bindingProcessor
        .getFormData(PARAM_APPLET_EXTENSION), charset);
    if (extension != null) {
      // must be one of VALUES_APPLET_EXTENSION
      String ext = extension.toLowerCase();
      if (Arrays.asList(VALUES_APPLET_EXTENSION).contains(ext)) {
        session.setAttribute(ATTR_APPLET_EXTENSION, ext);
        log.trace("Found parameter " + PARAM_APPLET_EXTENSION + "='" 
            + ext + "'.");
      } else {
        StringBuilder sb = new StringBuilder();
        sb.append("Applet parameter ").append(PARAM_APPLET_EXTENSION).append(
            "='").append(extension).append("' is not valid (must be one of ")
            .append(Arrays.toString(VALUES_APPLET_EXTENSION)).append(").");
        log.warn(sb);
      }
    }

    // locale
    String localeFormParam = getStringFromStream(bindingProcessor
        .getFormData(PARAM_LOCALE), charset);
    if (localeFormParam != null) {
      // must be a valid locale
      if (PATTERN_LOCALE.matcher(localeFormParam).matches()) {
        locale = new Locale(localeFormParam);
        log.debug("Overrule accept-language header locale " + locale
            + " with form param " + localeFormParam + ".");
      } else {
        log.warn("Parameter " + PARAM_LOCALE + "='" + localeFormParam
            + "' is not a valid locale definition.");
      }
    }
    if (locale != null) {
      log.debug("Using locale " + locale);
      session.setAttribute(ATTR_LOCALE, locale.toString());
    }
    
    // handle server side redirect url after processing
    String redirectUrl = bindingProcessor.getRedirectURL(); 
    if ( redirectUrl != null) {
      log.info("Got redirect URL "+redirectUrl+". Deferring browser redirect.");
      session.setAttribute(REDIRECT_URL_SESSION_ATTRIBUTE, redirectUrl);
    }

    String appletPage = getStringFromStream(bindingProcessor
        .getFormData(APPLET_PAGE_P), charset);
    getDispatcher(appletPage).forward(req, resp);
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, java.io.IOException {
    doPost(req, resp);
  }

  private RequestDispatcher getDispatcher(String appletPage) {
    RequestDispatcher dispatcher = null;
    if (appletPage != null) {
      log.trace("requested appletPage " + appletPage);
      dispatcher = getServletContext().getNamedDispatcher(appletPage);
    }
    if (dispatcher == null) {
      log.debug("no appletPage requested or appletPage not configured, using default");
      appletPage = APPLET_PAGE_DEFAULT;
      dispatcher = getServletContext().getNamedDispatcher(appletPage);
    }
//    session.setAttribute(APPLET_PAGE_P, appletPage);
    log.debug("forward to applet " + appletPage);

    return dispatcher;
  }

}
