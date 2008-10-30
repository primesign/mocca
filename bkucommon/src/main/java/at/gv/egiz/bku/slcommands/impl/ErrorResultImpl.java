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
  public void writeTo(Result result, Templates templates) {
    if (locale == null) {
      writeErrorTo(slException, result, templates);
    } else {
      writeErrorTo(slException, result, templates, locale);
    }
  }
  
}