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

import org.w3c.css.properties.css1.CssPadding;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssLength;
import org.w3c.css.values.CssNumber;
import org.w3c.css.values.CssPercentage;
import org.w3c.css.values.CssValue;

public class CssPaddingSLXHTML extends CssPadding {

  public CssPaddingSLXHTML() {
  }

  public CssPaddingSLXHTML(ApplContext ac, CssExpression expression,
      boolean check) throws InvalidParamException {
    super(ac, expression, check);
    
    if (getTop() != null) {
      if (isDisallowedValue(getTop().getValue())) {
        throw new SLXHTMLInvalidParamException("padding", getTop().getValue(),
            getPropertyName(), ac);
      }
    }
    
    if (getRight() != null) {
      if (isDisallowedValue(getRight().getValue())) {
        throw new SLXHTMLInvalidParamException("padding", getRight().getValue(),
            getPropertyName(), ac);
      }
    }
    
    if (getLeft() != null) {
      if (isDisallowedValue(getLeft().getValue())) {
        throw new SLXHTMLInvalidParamException("padding", getLeft().getValue(),
            getPropertyName(), ac);
      }
    }

    if (getBottom() != null) {
      if (isDisallowedValue(getBottom().getValue())) {
        throw new SLXHTMLInvalidParamException("padding", getBottom().getValue(),
            getPropertyName(), ac);
      }
    }

  }

  public CssPaddingSLXHTML(ApplContext ac, CssExpression expression)
      throws InvalidParamException {
    this(ac, expression, false);
  }

  public static boolean isDisallowedValue(CssValue padding) {

    // The padding-top, padding-bottom, padding-left and padding-right
    // properties must be supported by a Citizen Card Environment. Values
    // specified as percentages (cf. section 3.5.1.2) should be supported.

    // The padding property may be supported by a Citizen Card Environment.

    // An instance document must not contain a negative value in the properties
    // mentioned above. Otherwise it must be rejected by the Citizen Card
    // Environment.
    
    if (padding instanceof CssLength) {
      Object value = ((CssLength) padding).get();
      if (value instanceof Float) {
        return ((Float) value).floatValue() < 0;
      }
    } else if (padding instanceof CssPercentage) {
      Object value = ((CssPercentage) padding).get();
      if (value instanceof Float) {
        return ((Float) value).floatValue() < 0;
      }
    } else if (padding instanceof CssNumber) {
      return ((CssNumber) padding).getValue() < 0;
    }
    
    return false;
    
  }
  
}
