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

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.w3c.css.util.ApplContext;

public class SLXHTMLInvalidParamException extends
    org.w3c.css.util.InvalidParamException {

  private static final long serialVersionUID = 1L;
  
  protected String message;
  
  public SLXHTMLInvalidParamException() {
  }

  public SLXHTMLInvalidParamException(String error, ApplContext ac) {
    setMessage(error, null, ac);
  }

  public SLXHTMLInvalidParamException(String error, Object message, ApplContext ac) {
    setMessage(error, new Object[] {message}, ac);
  }

  public SLXHTMLInvalidParamException(String error, Object message1, Object message2,
      ApplContext ac) {
    setMessage(error, new Object[] {message1, message2}, ac);
  }

  @Override
  public String getMessage() {
    return getLocalizedMessage();
  }

  @Override
  public String getLocalizedMessage() {
    return message;
  }

  protected void setMessage(String error, Object[] arguments, ApplContext ac) {
    Locale locale = new Locale(ac.getContentLanguage());
    ResourceBundle bundle = ResourceBundle.getBundle("at/gv/egiz/bku/slxhtml/css/Messages", locale);
    String pattern;
    try {
      pattern = bundle.getString(error);
    } catch (MissingResourceException e) {
      pattern = "Can't find error message for : " + error;
    }
    message = MessageFormat.format(pattern, arguments);
  }
  
}
