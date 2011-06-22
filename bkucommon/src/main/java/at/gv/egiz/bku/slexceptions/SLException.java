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


package at.gv.egiz.bku.slexceptions;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class SLException extends Exception {

  private static String RESOURCE_BUNDLE_BASE_NAME = "at.gv.egiz.bku.slexceptions.SLExceptionMessages";
  
  private static String MISSING_RESOURCE_PATTERN = "MISSING RESOURCE FOR ERROR MESSAGE: {0} ({1})";
  
  private static String ILLEGAL_ARGUMENT_MESSAGE = "MESSAGE FORMAT FAILED";
  
  private static final long serialVersionUID = 1L;

  private int errorCode;
  
  private String message;
  
  private Object[] arguments;
  
  public SLException(int errorCode) {
    this.errorCode = errorCode;
    this.message = SLExceptionMessages.STANDARD_PREFIX + Integer.toString(errorCode);
  }

  public SLException(int errorCode, String message, Object[] arguments) {
    this.errorCode = errorCode;
    this.message = message;
    this.arguments = arguments;
  }

  public int getErrorCode() {
    return errorCode;
  }

  public String getDetailedMsg() {
    return getLocalizedMessage();
  }

  @Override
  public String getLocalizedMessage() {
    return getLocalizedMessage(Locale.getDefault());
  }

  public String getLocalizedMessage(Locale locale) {
    
    String pattern;
    Object[] arguments = this.arguments;
    try {
      ResourceBundle bundle = ResourceBundle.getBundle(RESOURCE_BUNDLE_BASE_NAME, locale);
      pattern = bundle.getString(message);
    } catch (MissingResourceException e) {
      pattern = MISSING_RESOURCE_PATTERN;
      arguments = new Object[]{message, e.getMessage()};
    }
    
    String localizedMessage;
    try {
      localizedMessage = MessageFormat.format(pattern, arguments);
    } catch (IllegalArgumentException e) {
      localizedMessage = ILLEGAL_ARGUMENT_MESSAGE + ": " + pattern;
    }
    
    return localizedMessage;
    
  }

  /* (non-Javadoc)
   * @see java.lang.Throwable#getMessage()
   */
  @Override
  public String getMessage() {
    return getLocalizedMessage();
  }

}