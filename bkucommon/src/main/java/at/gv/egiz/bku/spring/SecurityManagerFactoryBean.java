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
