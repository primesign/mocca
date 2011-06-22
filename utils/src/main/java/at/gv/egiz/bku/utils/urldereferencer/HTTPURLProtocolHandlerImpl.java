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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.InvalidParameterException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HTTPURLProtocolHandlerImpl implements URLProtocolHandler {

  private final Logger log = LoggerFactory.getLogger(HTTPURLProtocolHandlerImpl.class);

  public final static String HTTP = "http";
  public final static String HTTPS = "https";

  public final static String[] PROTOCOLS = { HTTP, HTTPS };

  private HostnameVerifier hostnameVerifier;
  private SSLSocketFactory sslSocketFactory;

  public StreamData dereference(String url)
      throws IOException {
    URL u = new URL(url);
    if ((!HTTP.equalsIgnoreCase(u.getProtocol()) && (!HTTPS
        .equalsIgnoreCase(u.getProtocol())))) {
      throw new InvalidParameterException("Url " + url + " not supported");
    }
    return dereferenceHTTP(u);
  }

  protected StreamData dereferenceHTTP(URL url) throws IOException {
    log.info("Dereferencing URL: '{}'.", url);
    HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
    if (httpConn instanceof HttpsURLConnection) {
      log.trace("Detected ssl connection.");
      HttpsURLConnection https = (HttpsURLConnection) httpConn;
      if (sslSocketFactory != null) {
        log.debug("Setting custom ssl socket factory for ssl connection.");
        https.setSSLSocketFactory(sslSocketFactory);
      } else {
        log.trace("No custom socket factory set.");
      }
      if (hostnameVerifier != null) {
        log.debug("Setting custom hostname verifier.");
        https.setHostnameVerifier(hostnameVerifier);
      }
    } else {
      log.trace("No secure connection with: {} class={}.", url, httpConn.getClass());
    }
    log.trace("Successfully opened connection.");
    return new StreamData(url.toString(), httpConn.getContentType(), httpConn
        .getInputStream());
  }

  @Override
  public void setHostnameVerifier(HostnameVerifier hostnameVerifier) {
    this.hostnameVerifier = hostnameVerifier;
  }

  @Override
  public void setSSLSocketFactory(SSLSocketFactory socketFactory) {
    this.sslSocketFactory = socketFactory;
  }

}
