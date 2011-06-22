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

import java.util.HashSet;
import java.util.Set;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssValue;

public class CssColorSLXHTML extends org.w3c.css.properties.css1.CssColorCSS2 {

  private static Set<String> SLXHTML_DISSALLOWED_COLORS = new HashSet<String>();
  
  static {
    
    SLXHTML_DISSALLOWED_COLORS.add("activeborder");
    SLXHTML_DISSALLOWED_COLORS.add("activecaption");
    SLXHTML_DISSALLOWED_COLORS.add("appworkspace");
    SLXHTML_DISSALLOWED_COLORS.add("background");
    SLXHTML_DISSALLOWED_COLORS.add("buttonface");
    SLXHTML_DISSALLOWED_COLORS.add("buttonhighlight");
    SLXHTML_DISSALLOWED_COLORS.add("buttonshadow");
    SLXHTML_DISSALLOWED_COLORS.add("buttontext");
    SLXHTML_DISSALLOWED_COLORS.add("captiontext");
    SLXHTML_DISSALLOWED_COLORS.add("graytext");
    SLXHTML_DISSALLOWED_COLORS.add("highlight");
    SLXHTML_DISSALLOWED_COLORS.add("highlighttext");
    SLXHTML_DISSALLOWED_COLORS.add("inactiveborder");
    SLXHTML_DISSALLOWED_COLORS.add("inactivecaption");
    SLXHTML_DISSALLOWED_COLORS.add("inactivecaptiontext");
    SLXHTML_DISSALLOWED_COLORS.add("infobackground");
    SLXHTML_DISSALLOWED_COLORS.add("infotext");
    SLXHTML_DISSALLOWED_COLORS.add("menu");
    SLXHTML_DISSALLOWED_COLORS.add("menutext");
    SLXHTML_DISSALLOWED_COLORS.add("scrollbar");
    SLXHTML_DISSALLOWED_COLORS.add("threeddarkshadow");
    SLXHTML_DISSALLOWED_COLORS.add("threedface");
    SLXHTML_DISSALLOWED_COLORS.add("threedhighlight");
    SLXHTML_DISSALLOWED_COLORS.add("threedlightshadow");
    SLXHTML_DISSALLOWED_COLORS.add("threedshadow");
    SLXHTML_DISSALLOWED_COLORS.add("window");
    SLXHTML_DISSALLOWED_COLORS.add("windowframe");
    SLXHTML_DISSALLOWED_COLORS.add("windowtext");
    
  }
  
  public static boolean isDisallowedColor(CssValue cssValue) {
    return SLXHTML_DISSALLOWED_COLORS.contains(cssValue.toString().toLowerCase());
  }
  
  public CssColorSLXHTML() {
  }

  public CssColorSLXHTML(ApplContext ac, CssExpression expression, boolean check)
      throws InvalidParamException {
    
    super(ac, expression, check);

    // A Citizen Card Environment must support all the options for specifying a
    // colour listed in [CSS 2], section 4.3.6 for a CSS property, if such an
    // option is available for this property according to [CSS 2].

    // The exceptions are the system colours (cf. [CSS 2], section 18.2); these
    // must not be used in an instance document so as to prevent dependencies on
    // the system environment. Otherwise the instance document must be rejected
    // by the Citizen Card Environment.

    CssValue color = getColor();
    if (!isSoftlyInherited() && color != null) {
      if (CssColorSLXHTML.isDisallowedColor(color)) {
        throw new SLXHTMLInvalidParamException("color", color, getPropertyName(), ac);
      }
    }
    
  }

  public CssColorSLXHTML(ApplContext ac, CssExpression expression)
      throws InvalidParamException {
    this(ac, expression, false);
  }

}
