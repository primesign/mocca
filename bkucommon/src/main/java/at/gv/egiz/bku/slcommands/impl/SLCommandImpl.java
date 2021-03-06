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

import javax.xml.bind.JAXBElement;

import at.gv.egiz.bku.slcommands.SLCommand;
import at.gv.egiz.bku.slexceptions.SLCommandException;

/**
 * This class serves as abstract base class for the implementation of a security
 * layer command.
 * 
 * @author mcentner
 * 
 * @param <T>
 *          the type of the corresponding request value
 */
public abstract class SLCommandImpl<T> implements SLCommand {

  /**
   * The request element of this command.
   */
  protected JAXBElement<T> request;

  @SuppressWarnings("unchecked")
  @Override
  public void init(Object request)
      throws SLCommandException {
    this.request = (JAXBElement<T>) request;
  }

  /**
   * Returns the request value.
   * 
   * It is a convenience method for <code>request.getValue()</code>.
   * 
   * @see JAXBElement#getValue()
   * @return the request value
   */
  protected T getRequestValue() {
    return request.getValue();
  }

  /**
   * @return the request
   */
  public JAXBElement<T> getRequest() {
    return request;
  }

}
