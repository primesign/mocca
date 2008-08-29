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
