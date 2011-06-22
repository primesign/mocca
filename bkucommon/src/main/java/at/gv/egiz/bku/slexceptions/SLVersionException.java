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

public class SLVersionException extends SLException {

  private static final long serialVersionUID = 1L;

  protected String namespaceURI;
  
  public SLVersionException(String namespaceURI) {
    super(2901, SLExceptionMessages.LEC2901_NOTIMPLEMENTED, new Object[] {namespaceURI});
    this.namespaceURI = namespaceURI;
  }
  
  public SLVersionException(int errorCode, String namespaceURI) {
    super(errorCode);
    this.namespaceURI = namespaceURI;
  }

  public SLVersionException(int errorCode, String namespaceURI, String message, Object[] arguments) {
    super(errorCode, message, arguments);
    this.namespaceURI = namespaceURI;
  }

  public String getNamespaceURI() {
    return namespaceURI;
  }

}
