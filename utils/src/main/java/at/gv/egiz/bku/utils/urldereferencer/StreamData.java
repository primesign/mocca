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