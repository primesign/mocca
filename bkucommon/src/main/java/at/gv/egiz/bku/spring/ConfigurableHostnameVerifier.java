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
