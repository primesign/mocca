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
