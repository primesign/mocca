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
