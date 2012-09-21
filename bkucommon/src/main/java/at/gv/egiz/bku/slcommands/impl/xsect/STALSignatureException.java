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
    super();
    this.errorCode = errorCode;
  }

  /**
   * Creates a new instance of this STALSigantureException with
   * the given <code>errorCode</code>.
   * 
   * @param errorCode the error code
   */
  public STALSignatureException(int errorCode, String msg) {
    super(msg);
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
