/*
* Copyright 2009 Federal Chancellery Austria and
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
