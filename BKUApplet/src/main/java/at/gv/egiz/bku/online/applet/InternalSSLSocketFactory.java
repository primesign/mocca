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

package at.gv.egiz.bku.online.applet;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InternalSSLSocketFactory extends SSLSocketFactory {

  private final static String GOV_DOMAIN = ".gv.at";

  private static InternalSSLSocketFactory instance = new InternalSSLSocketFactory();

  private final Logger log = LoggerFactory.getLogger(InternalSSLSocketFactory.class);

  private SSLSocket sslSocket;

  private SSLSocketFactory proxy;

  private InternalSSLSocketFactory() {
    proxy = HttpsURLConnection.getDefaultSSLSocketFactory();
  }

  public static InternalSSLSocketFactory getInstance() {
    return instance;
  }

  @Override
  public Socket createSocket() throws IOException {
    sslSocket = (SSLSocket) proxy.createSocket();
    return sslSocket;
  }

  @Override
  public Socket createSocket(String arg0, int arg1) throws IOException,
      UnknownHostException {
    sslSocket = (SSLSocket) proxy.createSocket(arg0, arg1);

    return sslSocket;
  }

  @Override
  public Socket createSocket(InetAddress arg0, int arg1) throws IOException {
    sslSocket = (SSLSocket) proxy.createSocket(arg0, arg1);
    return sslSocket;
  }

  @Override
  public Socket createSocket(String arg0, int arg1, InetAddress arg2, int arg3)
      throws IOException, UnknownHostException {
    sslSocket = (SSLSocket) proxy.createSocket(arg0, arg1, arg2, arg3);
    return sslSocket;
  }

  @Override
  public Socket createSocket(InetAddress arg0, int arg1, InetAddress arg2,
      int arg3) throws IOException {
    sslSocket = (SSLSocket) proxy.createSocket(arg0, arg1, arg2, arg3);
    return sslSocket;
  }

  @Override
  public Socket createSocket(Socket arg0, String arg1, int arg2, boolean arg3)
      throws IOException {
    sslSocket = (SSLSocket) proxy.createSocket(arg0, arg1, arg2, arg3);
    return sslSocket;
  }

  @Override
  public String[] getDefaultCipherSuites() {
    return proxy.getDefaultCipherSuites();
  }

  @Override
  public String[] getSupportedCipherSuites() {
    return proxy.getSupportedCipherSuites();
  }

  public boolean isEgovAgency() {
    log.info("Checking if server is egov agency.");
    if (sslSocket != null) {
      try {
        X509Certificate cert = (X509Certificate) sslSocket.getSession()
            .getPeerCertificates()[0];
        log.info("Server cert: {}.", cert);
        return isGovAgency(cert);
      } catch (SSLPeerUnverifiedException e) {
        log.error("Failed to check server cert.", e);
        return false;
      }
    }
    log.info("Not a SSL connection.");
    return false;
  }

  public static boolean isGovAgency(X509Certificate cert) {
    String[] rdns = (cert.getSubjectX500Principal().getName()).split(",");
    for (String rdn : rdns) {
      if (rdn.startsWith("CN=")) {
        String dns = rdn.split("=")[1];
        if (dns.endsWith(GOV_DOMAIN)) {
          return true;
        }
      }
    }
    try {
      Collection<List<?>> sanList = cert.getSubjectAlternativeNames();
      if (sanList != null) {
        for (List<?> san : sanList) {
          if ((Integer) san.get(0) == 2) {
            String dns = (String) san.get(1);
            if (dns.endsWith(GOV_DOMAIN)) {
              return true;
            }
          }
        }
      }
    } catch (CertificateParsingException e) {
      Logger log = LoggerFactory.getLogger(InternalSSLSocketFactory.class);
      log.error("Failed to parse certificate.", e);
    }
    if ((cert.getExtensionValue("1.2.40.0.10.1.1.1") != null)
        || (cert.getExtensionValue("1.2.40.0.10.1.1.2") != null)) {
      return true;
    }
    return false;
  }
}
