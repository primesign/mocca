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


import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;


import at.gv.egiz.bku.utils.binding.Protocol;

public class HTTPBindingProcessorFactory extends AbstractBindingProcessorFactory implements BindingProcessorFactory {
  
  private HostnameVerifier hostnameVerifier;
  
  private SSLSocketFactory sslSocketFactory;
  
  public HTTPBindingProcessorFactory() {
    Set<Protocol> sp = new HashSet<Protocol>();
    Collections.addAll(sp, Protocol.HTTP, Protocol.HTTPS);
    supportedProtocols = Collections.unmodifiableSet(sp);
  }
  
  /**
   * @return the hostnameVerifier
   */
  public HostnameVerifier getHostnameVerifier() {
    return hostnameVerifier;
  }

  /**
   * @param hostnameVerifier the hostnameVerifier to set
   */
  public void setHostnameVerifier(HostnameVerifier hostnameVerifier) {
    this.hostnameVerifier = hostnameVerifier;
  }

  /**
   * @return the sslSocketFactory
   */
  public SSLSocketFactory getSslSocketFactory() {
    return sslSocketFactory;
  }

  /**
   * @param sslSocketFactory the sslSocketFactory to set
   */
  public void setSslSocketFactory(SSLSocketFactory sslSocketFactory) {
    this.sslSocketFactory = sslSocketFactory;
  }

  @Override
  public BindingProcessor createBindingProcessor() {
    HTTPBindingProcessorImpl httpBindingProcessor = new HTTPBindingProcessorImpl();
    configureBindingProcessor(httpBindingProcessor);
    httpBindingProcessor.setHostnameVerifier(hostnameVerifier);
    httpBindingProcessor.setSslSocketFactory(sslSocketFactory);
    return httpBindingProcessor;
  }

}
