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


package at.gv.egiz.bku.slcommands.impl;

import java.util.Locale;

import javax.xml.transform.Result;
import javax.xml.transform.Templates;

import at.gv.egiz.bku.slcommands.ErrorResult;
import at.gv.egiz.bku.slexceptions.SLException;

/**
 * This class implements the security layer result <code>ErrorResponse</code>.
 * 
 * @author mcentner
 */
public class ErrorResultImpl extends SLResultImpl implements ErrorResult {

  /**
   * The exception containing information provided in the <code>ErrorResponse</code>.
   */
  protected SLException slException;
  
  /**
   * The locale to be used for rendering an <code>ErrorResponse</code>.
   */
  protected Locale locale;
  
  /**
   * Creates a new instance of this ErrorResultImpl with the given
   * <code>slException</code> containing information provided in the
   * <code>ErrorResponse</code> and the <code>locale</code> for rendering
   * the <code>ErrorResponse</code>.
   * 
   * @param slException the exception
   * @param locale the locale
   */
  public ErrorResultImpl(SLException slException, Locale locale) {
    this.slException = slException;
    this.locale = locale;
  }

  @Override
  public void writeTo(Result result, Templates templates, boolean fragment) {
    if (locale == null) {
      writeErrorTo(slException, result, templates, fragment);
    } else {
      writeErrorTo(slException, result, templates, locale, fragment);
    }
  }

  @Override
  public int getErrorCode() {
    if (slException != null) {
      return slException.getErrorCode();
    } else {
      return -1;
    }
  }

  @Override
  public String getInfo() {
    if (slException != null) {
      return slException.getLocalizedMessage(locale);
    } else {
      return null;
    }
  }
  
}