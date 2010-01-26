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

import org.w3c.css.properties.css1.CssBorderCSS2;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssValue;

public class CssBorderSLXHTML extends CssBorderCSS2 {

  public CssBorderSLXHTML() {
  }

  public CssBorderSLXHTML(ApplContext ac, CssExpression value, boolean check)
      throws InvalidParamException {
    super(ac, value, check);
    
    // A Citizen Card Environment must support all the options for specifying a
    // colour listed in [CSS 2], section 4.3.6 for a CSS property, if such an
    // option is available for this property according to [CSS 2].

    // The exceptions are the system colours (cf. [CSS 2], section 18.2); these
    // must not be used in an instance document so as to prevent dependencies on
    // the system environment. Otherwise the instance document must be rejected
    // by the Citizen Card Environment.

    if (getTop() != null) {
      CssValue top = getTop().getColor();
      if (!isSoftlyInherited() && top != null) {
        if (CssColorSLXHTML.isDisallowedColor(top)) {
          throw new SLXHTMLInvalidParamException("color", top, getPropertyName(), ac);
        }
      }
    }

    if (getLeft() != null) {
      CssValue left = getLeft().getColor();
      if (!isSoftlyInherited() && left != null) {
        if (CssColorSLXHTML.isDisallowedColor(left)) {
          throw new SLXHTMLInvalidParamException("color", left, getPropertyName(), ac);
        }
      }
    }
     
    if (getRight() != null) {
      CssValue right = getRight().getColor();
      if (!isSoftlyInherited() && right != null) {
        if (CssColorSLXHTML.isDisallowedColor(right)) {
          throw new SLXHTMLInvalidParamException("color", right, getPropertyName(), ac);
        }
      }
    }

    if (getBottom() != null) {
      CssValue bottom = getBottom().getColor();
      if (!isSoftlyInherited() && bottom != null) {
        if (CssColorSLXHTML.isDisallowedColor(bottom)) {
          throw new SLXHTMLInvalidParamException("color", bottom, getPropertyName(), ac);
        }
      }
    }
  }

  public CssBorderSLXHTML(ApplContext ac, CssExpression expression)
      throws InvalidParamException {
    this(ac, expression, false);
  }

}
