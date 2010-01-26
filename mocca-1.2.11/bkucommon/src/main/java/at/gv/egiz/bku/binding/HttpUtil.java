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

import java.util.Map;

import org.apache.commons.fileupload.ParameterParser;

/**
 * Placeholder for some HTTP related constants and helper method to extract the charset for a request. 
 *
 */
public class HttpUtil {

  public final static String CHAR_SET = "charset";
  public final static String DEFAULT_CHARSET = "ISO-8859-1";
  public final static String HTTP_HEADER_CONTENT_TYPE = "Content-Type";
  public static final String HTTP_HEADER_USER_AGENT = "User-Agent";
  public static final String HTTP_HEADER_SERVER = "Server";
  public final static String HTTP_HEADER_REFERER = "Referer";
  public final static String CONTENT_TRANSFER_ENCODING = "Content-Transfer-Encoding";
  public final static String MULTIPART_FOTMDATA = "multipart/form-data";
  public final static String MULTIPART_FOTMDATA_BOUNDARY = "boundary";
  public final static String TXT_XML = "text/xml";
  public final static String TXT_PLAIN = "text/plain";
  public final static String TXT_HTML = "text/html";
  public final static String APPLICATION_URL_ENCODED = "application/x-www-form-urlencoded";
  public final static String HTTP_HEADER_LOCATION = "Location";

  public final static char[] SEPERATOR = { ';' };

  /**
   * Extracts charset from a content type header.
   * 
   * @param contentType
   * @param replaceNullWithDefault
   *          if true the method return the default charset if not set
   * @return charset String or null if not present
   */
  @SuppressWarnings("unchecked")
  public static String getCharset(String contentType,
      boolean replaceNullWithDefault) {
    ParameterParser pf = new ParameterParser();
    pf.setLowerCaseNames(true);
    Map map = pf.parse(contentType, SEPERATOR);
    String retVal = (String) map.get(CHAR_SET);
    if ((retVal == null) && (replaceNullWithDefault)) {
      if (map.containsKey(APPLICATION_URL_ENCODED)) {
        // default charset for url encoded data
        return "UTF-8";
      }
      retVal = getDefaultCharset();
    }
    return retVal;
  }

  /**
   * 
   * Not to be used for url encoded requests.
   */
  public static String getDefaultCharset() {
    return DEFAULT_CHARSET;
  }

}
