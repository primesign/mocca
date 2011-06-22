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

import org.w3c.css.properties.css1.CssMarginRight;
import org.w3c.css.properties.css1.CssMarginSide;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;

public class CssMarginRightSLXHTML extends CssMarginRight {

  public CssMarginRightSLXHTML() {
  }

  public CssMarginRightSLXHTML(CssMarginSide another) {
    super(another);
  }

  public CssMarginRightSLXHTML(ApplContext ac, CssExpression expression)
      throws InvalidParamException {
    this(ac, expression, false);
  }

  public CssMarginRightSLXHTML(ApplContext ac, CssExpression expression,
      boolean check) throws InvalidParamException {
    super(ac, expression, check);

    // The margin-top, margin-bottom, margin-left and margin-right properties
    // must be supported by a Citizen Card Environment. Values specified as
    // percentages (cf. section 3.5.1.2) should be supported.

    // The margin property may be supported by a Citizen Card Environment.

    // An instance document must not contain a negative value in the properties
    // mentioned above. Otherwise it must be rejected by the Citizen Card
    // Environment.

    if (CssMarginSLXHTML.isDisallowedMargin(getValue())) {
      throw new SLXHTMLInvalidParamException("margin", getValue(),
          getPropertyName(), ac);
    }

  }

}
