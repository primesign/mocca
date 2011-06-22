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



/**
 * 
 */
package at.gv.egiz.bku.online.webapp;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class TransactionIdResponseWrapper extends HttpServletResponseWrapper {
  
  private String sessionId;
  
  private String tidx;

  public TransactionIdResponseWrapper(HttpServletResponse response, String sessionId, String tidx) {
    super(response);
    this.sessionId = sessionId;
    this.tidx = tidx;
  }

  /* (non-Javadoc)
   * @see javax.servlet.http.HttpServletResponseWrapper#encodeRedirectURL(java.lang.String)
   */
  @Override
  public String encodeRedirectURL(String url) {
    // ensure jsessionid and tidx parameters
    String encodedUrl = super.encodeRedirectUrl(url);
    int i = encodedUrl.indexOf('?');
    StringBuilder u = new StringBuilder();
    if (i > 0) {
      u.append(encodedUrl.substring(0, i));
    } else {
      u.append(encodedUrl);
    }
    if (!encodedUrl.contains(";jsessionid=")) {
      u.append(";jsessionid=");
      u.append(sessionId);
    }
    if (i < 0) {
      u.append('?');
    } else if (i < encodedUrl.length() - 1) {
      u.append(encodedUrl.substring(i));
      u.append('&');
    }
    u.append("tidx=");
    u.append(tidx);
    
    return u.toString();
  }

  /* (non-Javadoc)
   * @see javax.servlet.http.HttpServletResponseWrapper#encodeURL(java.lang.String)
   */
  @Override
  public String encodeURL(String url) {
    // ensure tidx parameter
    String encodedUrl = super.encodeUrl(url);
    if (url.endsWith("?wsdl")) {
      // don't add parameters to ?wsdl URLs
      return encodedUrl;
    }
    int i = encodedUrl.indexOf('?');
    StringBuilder u = new StringBuilder();
    if (i > 0) {
      u.append(encodedUrl.substring(0, i));
    } else {
      u.append(encodedUrl);
    }
    if (i < 0) {
      u.append('?');
    } else if (i < encodedUrl.length() - 1) {
      u.append(encodedUrl.substring(i));
      u.append('&');
    }
    u.append("tidx=");
    u.append(tidx);
    
    return u.toString();
  }
  
}