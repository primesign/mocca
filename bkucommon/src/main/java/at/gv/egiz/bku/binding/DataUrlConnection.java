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

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;

import at.gv.egiz.bku.slcommands.SLResult;

/**
 * Transmit a security layer result to DataURL via HTTP POST, encoded as
 * multipart/form-data. The HTTP header user-agent is set to
 * <em>citizen-card-environment/1.2 BKU2 1.0</em>. The form-parameter
 * ResponseType is set to <em>HTTP-Security-Layer-RESPONSE</em>. All other
 * headers/parameters are set by the caller.
 * 
 * @author clemens
 */
public abstract class DataUrlConnection {

  public static final String FORMPARAM_RESPONSETYPE = "ResponseType";
  public static final String DEFAULT_RESPONSETYPE = "HTTP-Security-Layer-RESPONSE";
  public static final String FORMPARAM_XMLRESPONSE = "XMLResponse";
  public static final String FORMPARAM_BINARYRESPONSE = "BinaryResponse";

  public static final String XML_RESPONSE_ENCODING = "UTF-8";

  /**
   * The URL to send responses and retrieve any further requests.
   */
  protected URL url;

  /**
   * Constructs a DataURL connection to the specified URL.
   * 
   * @param url
   *          the URL to send responses and retrieve any further requests
   */
  protected DataUrlConnection(URL url) {
    this.url = url;
  }

  /**
   * Returns the URL to send responses and retrieve any further requests.
   * 
   * @return the URL
   */
  public URL getURL() {
    return url;
  }

  /**
   * @see URLConnection#connect() 
   */
  public abstract void connect() throws SocketTimeoutException, IOException;

  /**
   * Transmit the given <code>SLResult</code> to the resource identified by this
   * URL.
   * 
   * @param slResult the <code>SLResult</code>
   * @throws IOException if an I/O exception occurs
   */
  public abstract void transmit(SLResult slResult) throws IOException;

  /**
   * Returns the <code>DataUrlResponse</code> received from the resource
   * identified by this URL.
   * 
   * @return the DataUrlResponse received 
   * 
   * @throws IOException if an I/O exception occurs
   */
  public abstract DataUrlResponse getResponse() throws IOException;
  
}