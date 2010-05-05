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
package at.gv.egiz.bku.utils.urldereferencer;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class used to dereference (external URLs).
 * 
 * @author wbauer
 * 
 */
public class URLDereferencerImpl implements URLDereferencer {

  private final Logger log = LoggerFactory.getLogger(URLDereferencerImpl.class);

  private static URLDereferencerImpl instance = new URLDereferencerImpl();

  private Map<String, URLProtocolHandler> handlerMap = new HashMap<String, URLProtocolHandler>();

  private HostnameVerifier hostnameVerifier;
  private SSLSocketFactory sslSocketFactory;
  
  private URLDereferencerImpl() {
    registerHandlers();
  }

  /**
   * 
   * @param aUrl
   *          must not be null
   * @throws MalformedURLException
   *           if the protocol is not supported
   * @throws IOException if the url cannot be dereferenced (e.g. formdata not provided)
   *           
   */
  @Override
  public StreamData dereference(String aUrl)
      throws IOException {
    log.debug("Looking for handler for URL: {}.", aUrl);
    int i = aUrl.indexOf(":");
    if (i == -1) {
      throw new MalformedURLException("Invalid url: " + aUrl);
    }
    String protocol = aUrl.substring(0, i).toLowerCase().trim();
    URLProtocolHandler handler = handlerMap.get(protocol);
    if (handler == null) {
      throw new MalformedURLException("No handler for protocol: " + protocol
          + " found");
    }
    handler.setHostnameVerifier(hostnameVerifier);
    handler.setSSLSocketFactory(sslSocketFactory);
    return handler.dereference(aUrl);
  }

  /**
   * Registers a handler for a protocol.
   * 
   * @param aProtocol
   * @param aHandler
   *          may be set to null to disable this protocol
   */
  public void registerHandler(String aProtocol, URLProtocolHandler aHandler) {
    handlerMap.put(aProtocol.toLowerCase(), aHandler);
  }

  public static URLDereferencerImpl getInstance() {
    return instance;
  }

  protected void registerHandlers() {
    URLProtocolHandler handler = new HTTPURLProtocolHandlerImpl();
    for (String proto : HTTPURLProtocolHandlerImpl.PROTOCOLS) {
      handlerMap.put(proto, handler);
    }
  }
  
  public void setHostnameVerifier(HostnameVerifier hostnameVerifier) {
    this.hostnameVerifier = hostnameVerifier;
  }

  public void setSSLSocketFactory(SSLSocketFactory socketFactory) {
    this.sslSocketFactory = socketFactory;
  }
}
