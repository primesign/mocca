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

import org.w3c.css.properties.css1.CssTextDecoration;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssValue;

public class CssTextDecorationSLXHTML extends CssTextDecoration {

  public CssTextDecorationSLXHTML() {
  }

  public CssTextDecorationSLXHTML(ApplContext ac, CssExpression expression,
      boolean check) throws InvalidParamException {
    super(ac, expression, check);
    
    if (get() instanceof CssValue) {
      if ("blink".equalsIgnoreCase(((CssValue) get()).toString())) {
        throw new SLXHTMLInvalidParamException("text-decoration", "blink", ac);
      }
    } else if (get() instanceof String) {
      if ("blink".equalsIgnoreCase((String) get())) {
        throw new SLXHTMLInvalidParamException("text-decoration", "blink", ac);
      }
    }
    
  }

  public CssTextDecorationSLXHTML(ApplContext ac, CssExpression expression)
      throws InvalidParamException {
    this(ac, expression, false);
  }

}
