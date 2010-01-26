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

import org.w3c.css.properties.css1.CssFontCSS2;
import org.w3c.css.properties.css1.CssFontConstantCSS2;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssValue;

public class CssFontSLXHTML extends CssFontCSS2 {

  public CssFontSLXHTML() {
  }

  public CssFontSLXHTML(ApplContext ac, CssExpression expression, boolean check)
      throws InvalidParamException {
    super(ac, checkExpression(expression, ac), check);
  }

  public CssFontSLXHTML(ApplContext ac, CssExpression expression)
      throws InvalidParamException {
    this(ac, expression, false);
  }

  protected static CssExpression checkExpression(CssExpression expression,
      ApplContext ac) throws InvalidParamException {
    
    CssValue value = expression.getValue();
    
    if (value instanceof CssIdent) {
      for (String font : CssFontConstantCSS2.FONT) {
        if (font.equalsIgnoreCase(value.toString())) {
          throw new SLXHTMLInvalidParamException("font", value.toString(), ac);
        }
      }
    }
    
    return expression;
    
  }
  
}
