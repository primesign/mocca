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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package at.gv.egiz.bku.online.applet;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

public class InternalSSLSocketFactory {

  private SSLSocketFactory factory;

  public static SSLSocketFactory getSocketFactory() throws InternalSSLSocketFactoryException {
    return new InternalSSLSocketFactory().factory;
  }
  
  public static HostnameVerifier getHostNameVerifier() throws InternalSSLSocketFactoryException {
   return (new HostnameVerifier() {
    @Override
    public boolean verify(String hostname, SSLSession session) {
      return true;
    }    
   });
  }

  public InternalSSLSocketFactory() throws InternalSSLSocketFactoryException {
    SSLContext sslContext;
    try {
      sslContext = SSLContext.getInstance("TLSv1");
      sslContext.getClientSessionContext().setSessionTimeout(0);
      KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");

      KeyStore keyStore = KeyStore.getInstance("JKS");
      keyStore.load(null, null);
      keyManagerFactory.init(keyStore, null);

      sslContext.init(keyManagerFactory.getKeyManagers(), 
        new X509TrustManager[] { new AcceptAllTrustManager() },
        null);
    } catch (NoSuchAlgorithmException e) {
      throw new InternalSSLSocketFactoryException(e);
    } catch (CertificateException e) {
      throw new InternalSSLSocketFactoryException(e);
    } catch (IOException e) {
      throw new InternalSSLSocketFactoryException(e);
    } catch (KeyStoreException e) {
      throw new InternalSSLSocketFactoryException(e);
    } catch (UnrecoverableKeyException e) {
      throw new InternalSSLSocketFactoryException(e);
    } catch (KeyManagementException e) {
      throw new InternalSSLSocketFactoryException(e);
    }

    this.factory = sslContext.getSocketFactory();
  }

  class AcceptAllTrustManager implements X509TrustManager {

    public X509Certificate[] getAcceptedIssuers() {
      return null;
    }

    public void checkClientTrusted(X509Certificate[] chain, String authType) {
    }

    public void checkServerTrusted(X509Certificate[] chain, String authType) {
      //FIXME
    }
  }
};
