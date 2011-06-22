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

import java.io.InputStream;
import java.util.Collections;
import java.util.Iterator;

import org.apache.commons.fileupload.FileItemHeaders;

/**
 * Simple wrapper to read data while consuming an stream within the http
 * processor.
 * 
 * 
 */
public class FormParameterImpl implements FormParameter {

  protected InputStream dataStream;
  protected String contentType;
  protected String formName;
  protected FileItemHeaders headers;

  public FormParameterImpl(String contentType, String formName, InputStream is,
      FileItemHeaders header) {
    this.contentType = contentType;
    this.formName = formName;
    this.dataStream = is;
    this.headers = header;
  }

  @Override
  public String getFormParameterContentType() {
    return contentType;
  }

  @Override
  public String getFormParameterName() {
    return formName;
  }

  @Override
  public InputStream getFormParameterValue() {
    return dataStream;
  }

  @Override
  public String getHeaderValue(String headerName) {
    if (headers == null) {
      return null;
    }
    return headers.getHeader(headerName);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Iterator<String> getHeaderNames() {
    if (headers == null) {
      return Collections.EMPTY_LIST.iterator();
    }
    return headers.getHeaderNames();
  }

  public FileItemHeaders getHeaders() {
    return headers;
  }

  public boolean equals(Object other) {
    if (other instanceof FormParameter) {
      FormParameter fp = (FormParameter) other;
      return fp.getFormParameterName().equals(getFormParameterName());
    }
    return false;
  }
  
  public int hashCode() {
    return getFormParameterName().hashCode();
  }
}
