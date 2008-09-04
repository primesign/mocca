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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Ignore;

import at.gv.egiz.bku.slcommands.SLResult;

@Ignore
public class TestDataUrlConnection implements DataUrlConnectionSPI {
  

  protected Log log = LogFactory.getLog(TestDataUrlConnection.class);
  protected X509Certificate serverCertificate;
  protected Map<String, String> responseHeaders = Collections.EMPTY_MAP;
  protected Map<String, String> requestHeaders = new HashMap<String, String>();
  protected String responseContent = "";
  protected int responseCode = 200;
    
  protected URL url;
  
  @Override
  public void init(URL url) {
    log.debug("Init Testdataurlconnection to url: " + url);
    this.url = url;
  }

  @Override
  public void connect() throws SocketTimeoutException, IOException {
    log.debug("Dummy connect to Testdataurlconnection to url: " + url);

  }

  @Override
  public String getProtocol() {
    return url.getProtocol();
  }

  @Override
  public DataUrlResponse getResponse() throws IOException {
    String ct = responseHeaders.get(HttpUtil.HTTP_HEADER_CONTENT_TYPE);
    if (ct != null) {
      ct = HttpUtil.getCharset(ct, true);
    } else {
      ct = HttpUtil.DEFAULT_CHARSET;
    }
    DataUrlResponse response = new DataUrlResponse(url.toString(), responseCode, new ByteArrayInputStream(responseContent.getBytes(ct)));
    response.setResponseHttpHeaders(responseHeaders);
    return response;
  }

  @Override
  public X509Certificate getServerCertificate() {
    return serverCertificate;
  }

  @Override
  public void setHTTPFormParameter(String name, InputStream data,
      String contentType, String charSet, String transferEncoding) {
    // TODO Auto-generated method stub
  }

  @Override
  public void setHTTPHeader(String key, String value) {
    requestHeaders.put(key, value);
  }

  @Override
  public void transmit(SLResult slResult) throws IOException {
    log.debug("Dummy transmit to url: " + url);
  }

  public void setServerCertificate(X509Certificate serverCertificate) {
    this.serverCertificate = serverCertificate;
  }

  public void setResponseHeaders(Map<String, String> responseHeaders) {
    this.responseHeaders = responseHeaders;
  }

  public void setResponseContent(String responseContent) {
    this.responseContent = responseContent;
  }

  public void setResponseCode(int responseCode) {
    this.responseCode = responseCode;
  }

  public Map<String, String> getRequestHeaders() {
    return requestHeaders;
  }

  @Override
  public DataUrlConnectionSPI newInstance() {
    return this;
  }

	@Override
	public URL getUrl() {
		return url;
	}
 }
