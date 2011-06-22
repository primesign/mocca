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
