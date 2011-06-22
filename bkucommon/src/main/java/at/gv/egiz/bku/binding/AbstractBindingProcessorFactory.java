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

import java.util.Set;

import org.apache.commons.configuration.Configuration;

import at.gv.egiz.bku.slcommands.SLCommandFactory;
import at.gv.egiz.bku.utils.binding.Protocol;
import at.gv.egiz.bku.utils.urldereferencer.URLDereferencer;


public abstract class AbstractBindingProcessorFactory implements BindingProcessorFactory {

  protected Set<Protocol> supportedProtocols;
  protected SLCommandFactory slCommandFactory;
  protected Configuration configuration;
  protected URLDereferencer urlDereferencer;

  @Override
  public Set<Protocol> getSupportedProtocols() {
    return supportedProtocols;
  }

  @Override
  public SLCommandFactory getSlCommandFactory() {
    return slCommandFactory;
  }

  @Override
  public void setSlCommandFactory(SLCommandFactory slCommandFactory) {
    this.slCommandFactory = slCommandFactory;    
  }

  @Override
  public Configuration getConfiguration() {
    return configuration;
  }

  @Override
  public void setConfiguration(Configuration configuration) {
    this.configuration = configuration;
  }

  /**
   * @return the urlDereferencer
   */
  public URLDereferencer getUrlDereferencer() {
    return urlDereferencer;
  }

  /**
   * @param urlDereferencer the urlDereferencer to set
   */
  public void setUrlDereferencer(URLDereferencer urlDereferencer) {
    this.urlDereferencer = urlDereferencer;
  }

  protected void configureBindingProcessor(AbstractBindingProcessor bindingProcessor) {
    bindingProcessor.setConfiguration(configuration);
    bindingProcessor.setSlCommandFactory(slCommandFactory);
    bindingProcessor.setUrlDereferencer(urlDereferencer);
  }

}
