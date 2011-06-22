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
