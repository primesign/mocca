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
package at.gv.egiz.bku.utils.urldereferencer;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Adapter to make the Urldereferencer work as URIResolver for
 * Stylesheettransforms.
 * 
 * @author wbauer
 * 
 */
public class URIResolverAdapter implements URIResolver {
  
  private static Log log = LogFactory.getLog(URIResolverAdapter.class);

  private URLDereferencer urlDereferencer;
  private URLDereferencerContext ctx;

  /**
   * 
   * @param deferecencer
   *          must not be null
   * @param ctx may be null
   */
  public URIResolverAdapter(URLDereferencer deferecencer,
      URLDereferencerContext ctx) {
    if (deferecencer == null) {
      throw new NullPointerException("Urlderefencer must not be set to null");
    }
    this.urlDereferencer = deferecencer;
    this.ctx = ctx;
  }

  @Override
  public Source resolve(String href, String base) throws TransformerException {
    log.debug("Resolving href: "+href+" base: "+base);
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
      return new StreamSource(urlDereferencer.dereference(abs.toString(), ctx)
          .getStream());
    } catch (URISyntaxException e) {
      throw new TransformerException("Cannot resolve URI: base:" + base
          + " href:" + href, e);
    } catch (IOException iox) {
      throw new TransformerException("Cannot resolve URI: base:" + base
          + " href:" + href, iox);
    }
  }

  public URLDereferencerContext getCtx() {
    return ctx;
  }

  public void setCtx(URLDereferencerContext ctx) {
    this.ctx = ctx;
  }
}
