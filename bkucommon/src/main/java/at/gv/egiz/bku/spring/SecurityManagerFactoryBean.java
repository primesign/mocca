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



package at.gv.egiz.bku.spring;

import org.apache.commons.configuration.Configuration;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import at.gv.egiz.bku.accesscontroller.SecurityManagerFacade;

public class SecurityManagerFactoryBean implements ResourceLoaderAware,
    FactoryBean {

  protected ResourceLoader resourceLoader;
  
  protected ConfigurationFacade configurationFacade = new ConfigurationFacade(); 
  
  public class ConfigurationFacade {
    
    protected ConfigurationFacade() {
    }
    
    public static final String ACCESSCONTROLLER_POLICYRESOURCE = "AccessController.PolicyResource";

    public static final String ACCESSCONTROLLER_DEFAULT_POLICYRESOURCE = "classpath:/at/gv/egiz/bku/accesscontrol/config/accessControlConfig.xml";

    public static final String ACCESSCONTROLLER_ACCEPTNOMATCH = "AccessController.AcceptNoMatch";

    public static final boolean ACCESSCONTROLLER_DEFAULT_ACCEPTNOMATCH = false;
    
    protected String getPolicyResource() {
      return configuration.getString(ACCESSCONTROLLER_POLICYRESOURCE, ACCESSCONTROLLER_DEFAULT_POLICYRESOURCE);
    }
    
    protected boolean getAcceptNoMatch() {
      return configuration.getBoolean(ACCESSCONTROLLER_ACCEPTNOMATCH, ACCESSCONTROLLER_DEFAULT_ACCEPTNOMATCH);
    }
    
  }
  
  protected Configuration configuration;
  
  /**
   * @return the configuration
   */
  public Configuration getConfiguration() {
    return configuration;
  }

  /**
   * @param configuration the configuration to set
   */
  public void setConfiguration(Configuration configuration) {
    this.configuration = configuration;
  }

  @Override
  public void setResourceLoader(ResourceLoader resourceLoader) {
    this.resourceLoader = resourceLoader;
  }

  @Override
  public Object getObject() throws Exception {
    
    SecurityManagerFacade sm = new SecurityManagerFacade();
    sm.setAllowUnmatched(configurationFacade.getAcceptNoMatch());

    Resource policyResource = resourceLoader.getResource(configurationFacade.getPolicyResource());
    sm.init(policyResource.getInputStream());

    return sm;
    
  }

  @Override
  public Class<?> getObjectType() {
    return SecurityManagerFacade.class;
  }

  @Override
  public boolean isSingleton() {
    return true;
  }

}
