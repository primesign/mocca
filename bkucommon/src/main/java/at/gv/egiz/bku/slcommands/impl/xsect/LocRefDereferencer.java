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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.crypto.Data;
import javax.xml.crypto.OctetStreamData;
import javax.xml.crypto.URIDereferencer;
import javax.xml.crypto.URIReference;
import javax.xml.crypto.URIReferenceException;
import javax.xml.crypto.XMLCryptoContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.gv.egiz.bku.utils.urldereferencer.StreamData;
import at.gv.egiz.bku.utils.urldereferencer.URLDereferencer;

/**
 * An URIDereferencer implementation that dereferences <code>LocRef</code>
 * references.
 * 
 * @author mcentner
 */
public class LocRefDereferencer implements URIDereferencer {

  /**
   * Logging facility.
   */
  private final Logger log = LoggerFactory.getLogger(LocRefDereferencer.class);

  /**
   * The <code>LocRef</code>-reference to be dereferenced by
   * {@link #dereference(URIReference, XMLCryptoContext)}.
   */
  protected String locRef;

  /**
   * The URLDereferencer to be used for dereferencing.
   */
  protected URLDereferencer dereferencer;

  /**
   * Creates a new instance of this LocRefDereferencer with the given
   * <code>dereferencerContext</code> and <code>locRef</code> reference.
   * 
   * @param dereferencer
   *          the context to be used for dereferencing
   * @param locRef
   *          the <code>LocRef</code>-reference (must be an absolute URI)
   * 
   * @throws URISyntaxException
   *           if <code>LocRef</code> is not an absolute URI
   */
  public LocRefDereferencer(URLDereferencer dereferencer,
      String locRef) throws URISyntaxException {

    this.dereferencer = dereferencer;

    URI locRefUri = new URI(locRef);
    if (locRefUri.isAbsolute()) {
      this.locRef = locRef;
    } else {
      throw new IllegalArgumentException(
          "Parameter 'locRef' must be an absolut URI.");
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * javax.xml.crypto.URIDereferencer#dereference(javax.xml.crypto.URIReference,
   * javax.xml.crypto.XMLCryptoContext)
   */
  @Override
  public Data dereference(URIReference uriReference, XMLCryptoContext context)
      throws URIReferenceException {

    StreamData streamData;
    try {
      streamData = dereferencer.dereference(locRef);
    } catch (IOException e) {
      log.info("Failed to dereference URI '{}'.", locRef, e);
      throw new URIReferenceException("Failed to dereference URI '" + locRef
          + "'. " + e.getMessage(), e);
    }
    return new OctetStreamData(streamData.getStream(), locRef, streamData
        .getContentType());
  }

}
