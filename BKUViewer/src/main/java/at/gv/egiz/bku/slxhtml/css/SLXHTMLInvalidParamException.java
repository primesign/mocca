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
