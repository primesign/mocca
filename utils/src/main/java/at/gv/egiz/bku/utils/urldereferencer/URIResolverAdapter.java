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


package at.gv.egiz.bku.utils.urldereferencer;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adapter to make the Urldereferencer work as URIResolver for
 * Stylesheettransforms.
 * 
 * @author wbauer
 * 
 */
public class URIResolverAdapter implements URIResolver {
  
  private final Logger log = LoggerFactory.getLogger(URIResolverAdapter.class);

  private URLDereferencer urlDereferencer;

  /**
   * 
   * @param deferecencer
   *          must not be null
   * @param ctx may be null
   */
  public URIResolverAdapter(URLDereferencer deferecencer) {
    if (deferecencer == null) {
      throw new NullPointerException("Urlderefencer must not be set to null");
    }
    this.urlDereferencer = deferecencer;
  }

  @Override
  public Source resolve(String href, String base) throws TransformerException {
    log.debug("Resolving href: {} base: {}", href, base);
    try {
      URI baseUri = null;
      URI hrefUri = new URI(href);
      if (base != null) {
        baseUri = new URI(base);
      }
      URI abs;
      if (baseUri != null) {
        abs = baseUri.resolve(hrefUri);
      } else {
        abs = hrefUri;
      }
      if (!abs.isAbsolute()) {
        throw new TransformerException("Only absolute URLs are supported");
      }
      return new StreamSource(urlDereferencer.dereference(abs.toString())
          .getStream());
    } catch (URISyntaxException e) {
      throw new TransformerException("Cannot resolve URI: base:" + base
          + " href:" + href, e);
    } catch (IOException iox) {
      throw new TransformerException("Cannot resolve URI: base:" + base
          + " href:" + href, iox);
    }
  }

}
