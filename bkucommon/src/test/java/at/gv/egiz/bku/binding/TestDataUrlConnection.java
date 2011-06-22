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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSocketFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.Ignore;

import at.gv.egiz.bku.slcommands.SLResult;

@Ignore
public class TestDataUrlConnection extends HttpsDataURLConnection {
  
  protected final Logger log = LoggerFactory.getLogger(TestDataUrlConnection.class);
  protected X509Certificate serverCertificate;
  protected Map<String, String> responseHeaders = Collections.emptyMap();
  protected Map<String, String> requestHeaders = new HashMap<String, String>();
  protected String responseContent = "";
  protected int responseCode = 200;
    
  public TestDataUrlConnection(URL url) {
    super(url);
  }
  

  @Override
  public void connect() throws SocketTimeoutException, IOException {
    log.debug("Dummy connect to Testdataurlconnection to url: " + url);

  }

  @Override
  public DataUrlResponse getResponse() throws IOException {
    String ct = responseHeaders.get(HttpUtil.HTTP_HEADER_CONTENT_TYPE);
    if (ct != null) {
      ct = HttpUtil.getCharset(ct, true);
    } else {
      ct = HttpUtil.DEFAULT_CHARSET;
    }
    DataUrlResponse response = new DataUrlResponse("" + url, responseCode, new ByteArrayInputStream(responseContent.getBytes(ct)));
    response.setResponseHttpHeaders(responseHeaders);
    return response;
  }

  @Override
  public void setHTTPFormParameter(String name, InputStream data,
      String contentType, String charSet, String transferEncoding) {
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
  public void setHostnameVerifier(HostnameVerifier hostnameVerifier) {
  }

  @Override
  public void setSSLSocketFactory(SSLSocketFactory socketFactory) {
  }


  @Override
  public Certificate[] getServerCertificates()
      throws SSLPeerUnverifiedException, IllegalStateException {
    return new Certificate[] {serverCertificate};
  }
  
 }
