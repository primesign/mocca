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

import java.io.ByteArrayInputStream;

import javax.xml.crypto.Data;
import javax.xml.crypto.OctetStreamData;
import javax.xml.crypto.URIDereferencer;
import javax.xml.crypto.URIReference;
import javax.xml.crypto.URIReferenceException;
import javax.xml.crypto.XMLCryptoContext;

/**
 * An URIDereferencer implementation that dereferences the given
 * byte array.
 * 
 * @author mcentner
 */
public class ByteArrayDereferencer implements URIDereferencer {

  /**
   * The dereferenced data.
   */
  protected byte[] dereferencedData;
  
  /**
   * Creates a new instance of this ByteArrayDereferencer with
   * the given <code>dereferencedData</code>.
   * 
   * @param dereferencedData the octets to be returned by {@link #dereference(URIReference, XMLCryptoContext)}
   * 
   * @throws NullPointerException if <code>dereferencedData</code> is <code>null</code>
   */
  public ByteArrayDereferencer(byte[] dereferencedData) {
    if (dereferencedData == null) {
      throw new NullPointerException("Parameter 'dereferencedData' must not be null.");
    }
    this.dereferencedData = dereferencedData;
  }

  /* (non-Javadoc)
   * @see javax.xml.crypto.URIDereferencer#dereference(javax.xml.crypto.URIReference, javax.xml.crypto.XMLCryptoContext)
   */
  @Override
  public Data dereference(URIReference uriReference, XMLCryptoContext context)
      throws URIReferenceException {
    return new OctetStreamData(new ByteArrayInputStream(dereferencedData));
  }

}
