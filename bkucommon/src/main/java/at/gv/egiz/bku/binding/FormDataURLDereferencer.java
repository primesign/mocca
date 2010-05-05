/*
* Copyright 2009 Federal Chancellery Austria and
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
