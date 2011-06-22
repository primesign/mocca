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

import java.io.IOException;
import java.net.URL;
import java.security.cert.Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSocketFactory;

public abstract class HttpsDataURLConnection extends HttpDataURLConnection {
  
  /**
   * Construct a new 
   * 
   * @param url
   * @throws IOException 
   */
  public HttpsDataURLConnection(URL url) {
    super(url);
  }

  /**
   * Sets the <code>SSLSocketFactory</code> to be used when this instance
   * creates sockets for secure https URL connections.
   * 
   * @param socketFactory
   *          the SSL socket factory
   */
  public abstract void setSSLSocketFactory(SSLSocketFactory socketFactory);

  /**
   * Sets the <code>HostnameVerifier</code> for this instance.
   * 
   * @param hostnameVerifier
   *          the host name verifier
   */
  public abstract void setHostnameVerifier(HostnameVerifier hostnameVerifier);

  /**
   * Returns the server's certificate chain which was established as part of
   * defining the session.
   * 
   * @return an ordered array of server certificates, with the peer's own
   *         certificate first followed by any certificate authorities.
   * 
   * @throws SSLPeerUnverifiedException
   *           if the peer is not verified.
   * @throws IllegalStateException
   *           if this method is called before the connection has been
   *           established.
   */
  public abstract Certificate[] getServerCertificates() throws SSLPeerUnverifiedException, IllegalStateException;
  
}
