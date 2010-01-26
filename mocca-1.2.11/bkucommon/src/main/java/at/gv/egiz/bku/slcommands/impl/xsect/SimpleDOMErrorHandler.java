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
package at.gv.egiz.bku.slcommands.impl.xsect;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.DOMError;
import org.w3c.dom.DOMErrorHandler;

/**
 * A simple DOMErrorHandler implementation.
 *  
 * @author mcentner
 */
public class SimpleDOMErrorHandler implements DOMErrorHandler {

  /**
   * Have there been errors reported?
   */
  private boolean errors = false;
  
  /**
   * Have there been fatal error reported?
   */
  private boolean fatalErrors = false;
  
  /**
   * The list of error messages of reported errors.
   */
  private List<String> errorMessages = new ArrayList<String>();
  
  /**
   * @return <code>true</code> if errors have been reported, or <code>false</code> otherwise
   */
  public boolean hasErrors() {
    return errors;
  }

  /**
   * @return <code>true</code> if fatal errors have been reported, or <code>false</code> otherwise
   */
  public boolean hasFatalErrors() {
    return fatalErrors;
  }

  /**
   * @return a list of error messages that have been reported
   */
  public List<String> getErrorMessages() {
    return errorMessages;
  }

  /* (non-Javadoc)
   * @see org.w3c.dom.DOMErrorHandler#handleError(org.w3c.dom.DOMError)
   */
  @Override
  public boolean handleError(DOMError error) {

    switch (error.getSeverity()) {
    
      case DOMError.SEVERITY_WARNING : 
//        log.debug("[warning] " + error.getMessage());
        return true;
        
      case DOMError.SEVERITY_ERROR : 
//        log.debug("[error] " + error.getMessage());
        errorMessages.add(error.getMessage());
        errors = true;
        return false;

      case DOMError.SEVERITY_FATAL_ERROR : 
//        log.debug("[fatal error] " + error.getMessage());
        errorMessages.add(error.getMessage());
        fatalErrors = true;
        return false;

      default:
        return false;
    }
    
  }
  
}
