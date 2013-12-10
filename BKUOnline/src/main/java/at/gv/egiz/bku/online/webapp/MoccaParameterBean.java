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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Locale;
import java.util.regex.Pattern;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.gv.egiz.bku.binding.HTTPBindingProcessor;
import at.gv.egiz.bku.utils.StreamUtil;

public class MoccaParameterBean {

  private static final Logger log = LoggerFactory.getLogger(MoccaParameterBean.class);

  public static final String PARAM_UI_PAGE_P = "appletPage";

  public static final String PARAM_APPLET_WIDTH = "appletWidth";

  public static final String PARAM_APPLET_HEIGHT = "appletHeight";

  public static final String PARAM_APPLET_BACKGROUND = "appletBackground";

  public static final String PARAM_REDIRECT_TARGET = "redirectTarget";

  public static final String PARAM_APPLET_BACKGROUND_COLOR = "appletBackgroundColor";
  public static final Pattern PATTERM_APPLET_BACKGROUND_COLOR = Pattern.compile("\\#[0-9a-fA-F]{6}");

  public static final String PARAM_APPLET_GUI_STYLE = "appletGuiStyle";
  public static final String[] VALUES_APPLET_GUI_STYLE = new String[] {"tiny", "simple", "advanced"};

  public static final String PARAM_APPLET_EXTENSION = "appletExtension";
  public static final String[] VALUES_APPLET_EXTENSION = new String[] {"pin", "activation", 
    "getcertificate", "hardwareinfo", "identity"};

  public static final String PARAM_LOCALE = "locale";
  public static final Pattern PATTERN_LOCALE = Pattern.compile("[a-zA-Z][a-zA-Z](_[a-zA-Z][a-zA-Z]){0,2}");

  private static final String P3P_POLICY = "policyref=\"w3c/p3p.xml\", CP=\"NON DSP COR CUR ADM DEV TAI PSA PSD OUR DEL IND UNI COM NAV INT CNT STA\"";
  private static final String ENABLE_P3P_HEADER = "EnableP3PHeader";

  private Charset charset = Charset.forName("ISO-8859-1");
  
  private HTTPBindingProcessor bindingProcessor;
  
  public MoccaParameterBean(HTTPBindingProcessor bindingProcessor) {
    this.bindingProcessor = bindingProcessor;
  }

  public Charset getCharset() {
    return charset;
  }

  public void setCharset(Charset charset) {
    this.charset = (charset != null) ? Charset.forName("ISO-8859-1") : charset;
  }

  public String getUIPage(String defaultValue) {
    String uiPage = getString(PARAM_UI_PAGE_P);
    return (uiPage != null) ? uiPage : defaultValue;
  }
  
  public Integer getAppletWidth() {
    return getInteger(PARAM_APPLET_WIDTH);
  }

  public Integer getAppletHeight() {
    return getInteger(PARAM_APPLET_HEIGHT);
  }

  public String getAppletBackground() {
    String background = getString(PARAM_APPLET_BACKGROUND);
    if (background != null && !background.isEmpty()) {
      try {
        // must be a valid http or https URL
        URI backgroundURL = new URI(background);
        if ("http".equals(backgroundURL.getScheme())
            || "https".equals(backgroundURL.getScheme())) {
          return background.toString();
        } else {
          log.warn("Parameter " + PARAM_APPLET_BACKGROUND
              + "='{}' is not a valid http/https URL.", background);
        }
      } catch (URISyntaxException e) {
        log.warn("Parameter " + PARAM_APPLET_BACKGROUND
            + "='{}' is not a valid http/https URL.", background, e);
      }
    }
    return null;
  }

  public String getAppletBackgroundColor() {
    String backgroundColor = getString(PARAM_APPLET_BACKGROUND_COLOR);
    if (backgroundColor != null && !backgroundColor.isEmpty()) {
      // must be a valid color definition
      if (PATTERM_APPLET_BACKGROUND_COLOR.matcher(backgroundColor).matches()) {
        return backgroundColor;
      } else {
        log.warn("Parameter " + PARAM_APPLET_BACKGROUND_COLOR
            + "='{}' is not a valid color definition "
            + "(must be of form '#hhhhhh').", backgroundColor);
      }
    }
    return null;
  }

  public String getRedirectTarget() {
    return getString(PARAM_REDIRECT_TARGET);
  }

  public String getGuiStyle() {
    String guiStyle = getString(PARAM_APPLET_GUI_STYLE);
    if (guiStyle != null && !guiStyle.isEmpty()) {
      // must be one of VALUES_APPLET_GUI_STYLE
      String style = guiStyle.toLowerCase();
      if (Arrays.asList(VALUES_APPLET_GUI_STYLE).contains(style)) {
        return style;
      } else {
        StringBuilder sb = new StringBuilder();
        sb.append("Parameter ").append(PARAM_APPLET_GUI_STYLE).append(
            "='").append(guiStyle).append("' is not valid (must be one of ")
            .append(Arrays.toString(VALUES_APPLET_GUI_STYLE)).append(").");
        log.warn(sb.toString());
      }
    }
    return null;
  }
  
  public String getExtension() {
    String extension = getString(PARAM_APPLET_EXTENSION);
    if (extension != null && !extension.isEmpty()) {
      // must be one of VALUES_APPLET_EXTENSION
      String ext = extension.toLowerCase();
      if (Arrays.asList(VALUES_APPLET_EXTENSION).contains(ext)) {
        log.debug("Found parameter " + PARAM_APPLET_EXTENSION + "='{}'.", ext);
        return ext;
      } else {
        StringBuilder sb = new StringBuilder();
        sb.append("Parameter ").append(PARAM_APPLET_EXTENSION).append(
            "='").append(extension).append("' is not valid (must be one of ")
            .append(Arrays.toString(VALUES_APPLET_EXTENSION)).append(").");
        log.warn(sb.toString());
      }
    }
    return null;
  }
  
  public Locale getLocale() {
    String locale = getString(PARAM_LOCALE);
    if (locale != null && !locale.isEmpty()) {
      // must be a valid locale 
      if (PATTERN_LOCALE.matcher(locale).matches()) {
        log.debug("Found parameter " + PARAM_LOCALE + "='{}'.", locale);
        return new Locale(locale);
      } else {
        log.warn("Parameter " + PARAM_LOCALE
            + "='{}' is not a valid locale definition.", locale);
      }
    }
    return bindingProcessor.getLocale();
  }
  
  public Integer getInteger(String parameterName) {
    String string = getString(parameterName);
    if (string == null || string.isEmpty()) {
      return null;
    } else {
      try {
        return Integer.parseInt(string);
      } catch (NumberFormatException e) {
        log.warn("Parameter {} does not contain a valid value.", parameterName,
            e);
        return null;
      }
    }
  }

  public String getString(String parameterName) {
    if (bindingProcessor != null) {
      InputStream formData = bindingProcessor.getFormData(parameterName);
      if (formData != null) {
        InputStreamReader reader = new InputStreamReader(formData, charset);
        StringWriter writer = new StringWriter();
        try {
          StreamUtil.copyStream(reader, writer);
        } catch (IOException e) {
          log.warn("Failed to get parameter {}.", parameterName, e);
        }
        return writer.toString();
      }
    }
    return null;
  }

  public static void setP3PHeader(Configuration config, HttpServletResponse response) {
    if (config.getBoolean(ENABLE_P3P_HEADER, false)) {
      // Set P3P Policy Header
      response.addHeader("P3P", P3P_POLICY);
    }
  }

  public static String getInitParameter(String name, ServletConfig config,
      ServletContext context) {
    String initVal = config.getInitParameter(name);
    String contextVal = context.getInitParameter(config.getServletName() + "." + name);
    log.debug("Reading init param " + name + ": " + initVal +
        " - context param " + (config.getServletName() + "." + name) + ": " + contextVal);
    if (contextVal != null)
      return contextVal;
    return initVal;
  }
}
