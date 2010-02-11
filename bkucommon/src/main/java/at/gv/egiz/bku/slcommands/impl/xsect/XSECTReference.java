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

import iaik.xml.crypto.dsig.DigestMethodImpl;
import iaik.xml.crypto.dsig.DigestValueImpl;
import iaik.xml.crypto.dsig.ReferenceImpl;
import iaik.xml.crypto.dsig.TransformImpl;
import iaik.xml.crypto.dsig.TransformsImpl;

import javax.xml.crypto.Data;
import javax.xml.crypto.URIDereferencer;
import javax.xml.crypto.URIReferenceException;
import javax.xml.crypto.XMLCryptoContext;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.TransformException;

import at.gv.egiz.bku.slexceptions.SLCommandException;
import at.gv.egiz.bku.slexceptions.SLExceptionMessages;

/**
 * This class extends the XSECT ReferenceImpl to allow for the use
 * of already marshalled <code>ds:Transforms</code> elements for initialization.
 *  
 * @author mcentner
 */
public class XSECTReference extends ReferenceImpl {

  /**
   * The URIDereferencer to be used for dereferencing.
   */
  protected URIDereferencer dereferencer;

  /**
   * Creates a new instance of this XSECTReference with the given
   * <code>uri</code>, digest method, <code>transforms</code>, <code>type</code>
   * and <code>id</code> value.
   * 
   * @param uri
   *          the <code>URI</code>-attribute value (may be <code>null</code>)
   * @param dm
   *          the digest method
   * @param transforms
   *          a TransformsImpl element (may be <code>null</code>)
   * @param type
   *          the <code>Type</code>-attribute value (may be <code>null</code>)
   * @param id
   *          the <code>Id</code>-attribute value (may be <code>null</code>)
   * 
   * @throws NullPointerException
   *           if <code>digestMethod</code> is <code>null</code>
   * @throws IllegalArgumentException
   *           if <code>uri</code> is not RFC 2396 compliant
   * @throws ClassCastException
   *           if any of the <code>transforms</code> is not of type
   *           {@link TransformImpl}
   */
  public XSECTReference(String uri, DigestMethod dm, TransformsImpl transforms, String type,
      String id) {
    super(uri, transforms, type, id);
    digestMethod_ = (DigestMethodImpl) dm;
    digestValue_ = new DigestValueImpl();
  }

  /* (non-Javadoc)
   * @see iaik.xml.crypto.dsig.ReferenceType#dereference(javax.xml.crypto.XMLCryptoContext)
   */
  @Override
  public Data dereference(XMLCryptoContext context) throws TransformException,
      URIReferenceException {
    if (dereferencer != null) {
      Data result = dereferencer.dereference(this, context);
       // apply transforms if any
      if (transforms_ != null) {
        result = transforms_.applyTransforms(context, result);
      }
      return result;
    } else {
      try {
        return super.dereference(context);
      } catch (URIReferenceException e) {
        SLCommandException commandException = new SLCommandException(4003,
            SLExceptionMessages.EC4003_NOT_RESOLVED, new Object[] { getURI() });
        throw new URIReferenceException("Failed to dereference data to-be signed.", commandException);
      }
    }
  }

  /**
   * @return the dereferencer to be used for dereferencing this reference
   */
  public URIDereferencer getDereferencer() {
    return dereferencer;
  }

  /**
   * @param dereferencer the dereferencer to be used for dereferencing this reference
   */
  public void setDereferencer(URIDereferencer dereferencer) {
    this.dereferencer = dereferencer;
  }

}
