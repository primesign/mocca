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

import iaik.xml.crypto.utils.URIDereferencerImpl;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.crypto.Data;
import javax.xml.crypto.OctetStreamData;
import javax.xml.crypto.URIDereferencer;
import javax.xml.crypto.URIReference;
import javax.xml.crypto.URIReferenceException;
import javax.xml.crypto.XMLCryptoContext;

import at.gv.egiz.bku.utils.urldereferencer.StreamData;
import at.gv.egiz.bku.utils.urldereferencer.URLDereferencer;

/**
 * An URIDereferencer implementation that uses an {@link URLDereferencer} to
 * dereference.
 * 
 * @author mcentner
 */
public class URIDereferncerAdapter implements URIDereferencer {

  /**
   * The context for dereferencing.
   */
  protected URLDereferencer dereferencer;

  /**
   * Creates a new URIDereferencerAdapter instance with the given
   * <code>urlDereferencerContext</code>.
   * 
   * @param urlDereferencer the context to be used for dereferencing
   */
  public URIDereferncerAdapter(URLDereferencer urlDereferencer) {
    super();
    this.dereferencer = urlDereferencer;
  }

  /* (non-Javadoc)
   * @see javax.xml.crypto.URIDereferencer#dereference(javax.xml.crypto.URIReference, javax.xml.crypto.XMLCryptoContext)
   */
  @Override
  public Data dereference(URIReference uriReference, XMLCryptoContext context)
      throws URIReferenceException {
    
    String uriString = uriReference.getURI();
    if (uriString == null) {
      return null;
    }
    
    URI uri;
    try {
      uri = new URI(uriString);
    } catch (URISyntaxException e) {
      throw new URIReferenceException(e.getMessage(), e);
    }
    
    if (uri.isAbsolute()) {

      StreamData streamData;
      try {
        streamData = dereferencer.dereference(uriString);
      } catch (IOException e) {
        throw new URIReferenceException(e.getMessage(), e);
      }
      return new OctetStreamData(streamData.getStream(), uriString, streamData.getContentType());
      
    } else {
      
      URIDereferencer uriDereferencer = context.getURIDereferencer();
      if (uriDereferencer == null || uriDereferencer == this) {
        uriDereferencer = new URIDereferencerImpl();
      }
        
      return uriDereferencer.dereference(uriReference, context);
      
    }
    
  }

}
