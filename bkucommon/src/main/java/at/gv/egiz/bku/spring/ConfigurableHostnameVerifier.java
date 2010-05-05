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

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

import org.apache.commons.configuration.Configuration;

import at.gv.egiz.bku.conf.MoccaConfigurationFacade;

public class ConfigurableHostnameVerifier implements HostnameVerifier {

  /**
   * The configuration facade.
   */
  protected final ConfigurationFacade configurationFacade = new ConfigurationFacade();
  
  public class ConfigurationFacade implements MoccaConfigurationFacade {
    
    private Configuration configuration;
    
    public static final String SSL_DISSABLE_HOSTNAME_VERIFICATION = "SSL.disableHostnameVerification";
    
    public static final String SSL_DISSABLE_ALL_CHECKS = "SSL.disableAllChecks";
    
    public boolean disableSslHostnameVerification() {
      return configuration.getBoolean(SSL_DISSABLE_HOSTNAME_VERIFICATION, false);
    }
    
    public boolean disableAllSslChecks() {
      return configuration.getBoolean(SSL_DISSABLE_ALL_CHECKS, false);
    }
    
  }

  /**
   * @return the configuration
   */
  public Configuration getConfiguration() {
    return configurationFacade.configuration;
  }

  /**
   * @param configuration the configuration to set
   */
  public void setConfiguration(Configuration configuration) {
    configurationFacade.configuration = configuration;
  }

  
  @Override
  public boolean verify(String hostname, SSLSession session) {
    if (configurationFacade.disableAllSslChecks() || configurationFacade.disableSslHostnameVerification()) {
      return true;
    } else {
      return HttpsURLConnection.getDefaultHostnameVerifier().verify(hostname, session);
    }
  }

}
