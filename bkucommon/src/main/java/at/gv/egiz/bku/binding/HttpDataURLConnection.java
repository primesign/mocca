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

import java.io.InputStream;
import java.net.URL;

/**
 * A HTTP DataURLConnection.
 * 
 * @author mcentner
 */
public abstract class HttpDataURLConnection extends DataUrlConnection {

  /**
   * Constructs a DataURL connection to the specified URL.
   * 
   * @param url
   *          the URL to send responses and retrieve any further requests
   */
  public HttpDataURLConnection(URL url) {
    super(url);
  }

  /**
   * Set a HTTP header.
   * 
   * @param key
   *          the key
   * @param value
   *          multiple values are assumed to have the correct formatting
   *          (comma-separated list)
   */
  public abstract void setHTTPHeader(String key, String value);

  /**
   * Set a HTTP form parameter to be transmitted with the SLResult.
   * 
   * @param name
   *          the name of the form parameter
   * @param data
   *          the content of the form parameter
   * @param contentType
   *          the content type (may be <code>null</code>)
   * @param charSet
   *          the character set (may be <code>null</code>)
   * @param transferEncoding
   *          the transfer encoding (may be <code>null</code>)
   */
  public abstract void setHTTPFormParameter(String name, InputStream data,
      String contentType, String charSet, String transferEncoding);

}
