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
  public static final String HTTP_HEADER_SIGNATURE_LAYOUT = "SignatureLayout";
  public final static String CONTENT_TRANSFER_ENCODING = "Content-Transfer-Encoding";
  public final static String MULTIPART_FORMDATA = "multipart/form-data";
  public final static String MULTIPART_FORMDATA_BOUNDARY = "boundary";
  public final static String TXT_XML = "text/xml";
  public final static String TXT_PLAIN = "text/plain";
  public final static String TXT_HTML = "text/html";
  public final static String APPLICATION_URL_ENCODED = "application/x-www-form-urlencoded";
  public final static String HTTP_HEADER_LOCATION = "Location";

  public final static char[] SEPARATOR = { ';' };

  /**
   * Extracts charset from a content type header.
   * 
   * @param contentType
   * @param replaceNullWithDefault
   *          if true the method return the default charset if not set
   * @return charset String or null if not present
   */
  public static String getCharset(String contentType,
      boolean replaceNullWithDefault) {
    ParameterParser pf = new ParameterParser();
    pf.setLowerCaseNames(true);
    Map<?, ?> map = pf.parse(contentType, SEPARATOR);
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
