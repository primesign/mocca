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

import org.w3c.css.properties.css1.CssBorderBottomColorCSS2;
import org.w3c.css.properties.css1.CssBorderColorCSS2;
import org.w3c.css.properties.css1.CssBorderLeftColorCSS2;
import org.w3c.css.properties.css1.CssBorderRightColorCSS2;
import org.w3c.css.properties.css1.CssBorderTopColorCSS2;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;

public class CssBorderColorSLXHTML extends CssBorderColorCSS2 {

  public CssBorderColorSLXHTML() {
  }

  public CssBorderColorSLXHTML(ApplContext ac, CssExpression expression,
      boolean check) throws InvalidParamException {
    super(ac, expression, check);
    
    // A Citizen Card Environment must support all the options for specifying a
    // colour listed in [CSS 2], section 4.3.6 for a CSS property, if such an
    // option is available for this property according to [CSS 2].

    // The exceptions are the system colours (cf. [CSS 2], section 18.2); these
    // must not be used in an instance document so as to prevent dependencies on
    // the system environment. Otherwise the instance document must be rejected
    // by the Citizen Card Environment.
    
    CssBorderTopColorCSS2 top = getTop();
    if (!isSoftlyInherited() && top != null) {
      if (CssColorSLXHTML.isDisallowedColor(top.getColor())) {
        throw new SLXHTMLInvalidParamException("color", top.getColor(), getPropertyName(), ac);
      }
    }

    CssBorderLeftColorCSS2 left = getLeft();
    if (!isSoftlyInherited() && left != null) {
      if (CssColorSLXHTML.isDisallowedColor(left.getColor())) {
        throw new SLXHTMLInvalidParamException("color", left.getColor(), getPropertyName(), ac);
      }
    }

    CssBorderRightColorCSS2 right = getRight();
    if (!isSoftlyInherited() && right != null) {
      if (CssColorSLXHTML.isDisallowedColor(right.getColor())) {
        throw new SLXHTMLInvalidParamException("color", right.getColor(), getPropertyName(), ac);
      }
    }

    CssBorderBottomColorCSS2 bottom = getBottom();
    if (!isSoftlyInherited() && bottom != null) {
      if (CssColorSLXHTML.isDisallowedColor(bottom.getColor())) {
        throw new SLXHTMLInvalidParamException("color", bottom.getColor(), getPropertyName(), ac);
      }
    }
    
  }

  public CssBorderColorSLXHTML(ApplContext ac, CssExpression expression)
      throws InvalidParamException {
    this(ac, expression, false);
  }

}
