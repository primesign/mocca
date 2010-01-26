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
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.Iterator;
import java.util.Map;

import at.gv.egiz.bku.utils.urldereferencer.StreamData;

/**
 * The response of a dataurl server.
 * Additionally holds return code and response headers.
 */
public class DataUrlResponse extends StreamData {

  public final static String OK = "<ok/>";

  protected Map<String, String> responseHttpHeaders;

  protected int responseCode = -1;

  public DataUrlResponse(String url, int responseCode, InputStream stream) {
    super(url, null, new PushbackInputStream(stream, 10));
    this.responseCode = responseCode;
  }

  public String getContentType() {
    if (contentType != null) {
      return contentType;
    }
    if (responseHttpHeaders == null) {
      return null;
    }
    for (Iterator<String> keyIt = responseHttpHeaders.keySet().iterator(); keyIt
        .hasNext();) {
      String key = keyIt.next();
      if (HttpUtil.HTTP_HEADER_CONTENT_TYPE.equalsIgnoreCase(key)) {
        contentType = responseHttpHeaders.get(key);
        return contentType;
      }
    }
    return contentType;
  }

  public void setResponseHttpHeaders(Map<String, String> responseHttpHeaders) {
    this.responseHttpHeaders = responseHttpHeaders;
  }

  public Map<String, String> getResponseHeaders() {
    return responseHttpHeaders;
  }

  public int getResponseCode() {
    return responseCode;
  }

  /**
   * Checks if the http response equals "<ok/>"
   * 
   * @throws IOException
   */
  public boolean isHttpResponseXMLOK() throws IOException {
    String charset = HttpUtil.getCharset(contentType, true);
    byte[] buffer = new byte[10];
    int i = 0;
    int read = 0;
    while ((i < 10) && (read != -1)) {
      read = inputStream.read(buffer, i, 10 - i);
      if (read != -1) {
        i += read;
      }
    }
    PushbackInputStream pbis = (PushbackInputStream) inputStream;
    pbis.unread(buffer, 0, i);
    if (i < 5) {
      return false;
    }
    String ok = new String(buffer, 0, i, charset);
    return (OK.equals(ok));
  }
}
