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

import java.security.SignatureException;

/**
 * A SignatureException thrown by the {@link STALSignatureMethod}.
 * 
 * @author mcentner
 */
public class STALSignatureException extends SignatureException {

  private static final long serialVersionUID = 1L;
  
  /**
   * The STAL error code.
   */
  private int errorCode;
  
  /**
   * Creates a new instance of this STALSignatureException. 
   */
  public STALSignatureException() {
  }

  /**
   * Creates a new instance of this STALSigantureException with
   * the given <code>errorCode</code>.
   * 
   * @param errorCode the error code
   */
  public STALSignatureException(int errorCode) {
    this.errorCode = errorCode;
  }

  /**
   * Creates a new instance of this STALSignatureException with
   * the given error <code>msg</code>.
   * 
   * @param msg the error message
   * @see SignatureException#SignatureException(String)
   */
  public STALSignatureException(String msg) {
    super(msg);
  }

  /**
   * Creates a new instance of this STALSignatureException with
   * the given root <code>cause</code>.
   * 
   * @param cause the cause
   * @see SignatureException#SignatureException(Throwable)
   */
  public STALSignatureException(Throwable cause) {
    super(cause);
  }

  /**
   * Creates a new instance of this STALSignautureException with
   * the given error <code>message</code> and root <code>cause</code>.
   * 
   * @param message the error message
   * @param cause the cause
   * @see SignatureException#SignatureException(String, Throwable)
   */
  public STALSignatureException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * @return the error code
   */
  public int getErrorCode() {
    return errorCode;
  }
  
}
