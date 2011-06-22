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
