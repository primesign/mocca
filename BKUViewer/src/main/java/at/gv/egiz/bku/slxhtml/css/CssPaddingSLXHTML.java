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
