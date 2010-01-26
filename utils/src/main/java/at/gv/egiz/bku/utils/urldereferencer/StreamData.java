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

import java.io.InputStream;

/**
 * This class models the result when dereferencing an URL. 
 *
 */
public class StreamData {

  protected InputStream inputStream;
  protected String url;
  protected String contentType;

  /**
   * 
   * @param url
   * @param contentType
   * @param stream must not be null
   */
  public StreamData(String url, String contentType, InputStream stream) {
    if (stream == null) {
      throw new NullPointerException("Parameter inputstream must not be null");
    }
    inputStream = stream;
    this.contentType = contentType;
    this.url = url;
  }

  public String getUrl() {
    return url;
  }

  /**
   * 
   * @return the returned stream must be closed
   */
  public InputStream getStream() {
    return inputStream;
  }

  public String getContentType() {
    return contentType;
  } 
}