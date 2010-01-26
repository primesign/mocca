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
