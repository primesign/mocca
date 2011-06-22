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


package at.gv.egiz.bku.slxhtml.css;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

import org.w3c.css.css.CssParser;
import org.w3c.css.css.StyleSheet;
import org.w3c.css.css.StyleSheetParser;
import org.w3c.css.parser.CssError;
import org.w3c.css.parser.Errors;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.Util;
import org.w3c.css.util.Warning;
import org.w3c.css.util.Warnings;

import at.gv.egiz.bku.viewer.ValidationException;

public class CSSValidatorSLXHTML {
  
  public void validate(InputStream input, Locale locale, String title, int lineno) throws ValidationException {
    
    // disable imports
    Util.importSecurity = true;
    
    CssParser cssParser = new StyleSheetParser();
    
    ApplContext ac = new ApplContext(locale.getLanguage());
    ac.setCssVersion("slxhtml");
    ac.setMedium("all");
    
    URL url;
    try {
      url = new URL("http://test.xyz");
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
    
    cssParser.parseStyleElement(ac, input, title, "all", url, lineno);
    
    StyleSheet styleSheet = cssParser.getStyleSheet();

    // find conflicts
    styleSheet.findConflicts(ac);

    boolean valid = true;
    StringBuilder sb = new StringBuilder().append("CSS:");

    // look for errors
    Errors errors = styleSheet.getErrors();
    if (errors.getErrorCount() != 0) {
      valid = false;
      CssError[] cssErrors = errors.getErrors();
      for (CssError cssError : cssErrors) {
        Exception exception = cssError.getException();
        sb.append(" ");
        sb.append(exception.getMessage());
      }
    }

    // look for warnings
    Warnings warnings = styleSheet.getWarnings();
    if (warnings.getWarningCount() != 0) {
      valid = false;
      Warning[] cssWarnings = warnings.getWarnings();
      for (Warning warning : cssWarnings) {
        sb.append(" ");
        sb.append(warning.getWarningMessage());
      }
    }
    
    if (!valid) {
      throw new ValidationException(sb.toString());
    }
    
  }

}
