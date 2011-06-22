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



package at.gv.egiz.bku.binding;

import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.gv.egiz.bku.utils.urldereferencer.StreamData;
import at.gv.egiz.bku.utils.urldereferencer.URLDereferencer;

public class FormDataURLDereferencer implements URLDereferencer {
  
  public final static String PROTOCOL = "formdata";
  
  private final Logger log = LoggerFactory.getLogger(FormDataURLDereferencer.class);

  private URLDereferencer urlDereferencer;
  
  private FormDataURLSupplier formDataURLSupplier;
  
  public FormDataURLDereferencer(URLDereferencer urlDereferencer, FormDataURLSupplier formDataURLSupplier) {
    this.urlDereferencer = urlDereferencer;
    this.formDataURLSupplier = formDataURLSupplier;
  }

  @Override
  public StreamData dereference(String url)
      throws IOException {
    
    String urlString = url.toLowerCase().trim();
    if (urlString.startsWith(PROTOCOL)) {
      log.debug("Requested to dereference a formdata url.");
      return dereferenceFormData(url);
    } else {
      return urlDereferencer.dereference(url);
    }
    
  }

  protected StreamData dereferenceFormData(String url) throws IOException {
    log.debug("Dereferencing formdata url: {}.", url);
    String[] parts = url.split(":", 2);

    String contentType = formDataURLSupplier.getFormDataContentType(parts[1]);
    InputStream is = formDataURLSupplier.getFormData(parts[1]);
    if (is != null) {
      return new StreamData(url, contentType, is);
    }
    throw new IOException("Cannot dereference URL: '" + url + "' not found.");
  }

  
}
