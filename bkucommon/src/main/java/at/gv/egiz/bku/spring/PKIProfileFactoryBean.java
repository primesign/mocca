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

import iaik.logging.LogConfigurationException;
import iaik.logging.LoggerConfig;
import iaik.logging.impl.TransactionIdImpl;
import iaik.pki.DefaultPKIConfiguration;
import iaik.pki.DefaultPKIProfile;
import iaik.pki.PKIException;
import iaik.pki.PKIFactory;
import iaik.pki.PKIProfile;
import iaik.pki.revocation.RevocationSourceTypes;
import iaik.pki.store.certstore.CertStoreParameters;
import iaik.pki.store.certstore.directory.DefaultDirectoryCertStoreParameters;
import iaik.pki.store.truststore.DefaultTrustStoreProfile;
import iaik.pki.store.truststore.TrustStoreProfile;
import iaik.pki.store.truststore.TrustStoreTypes;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.FileConfiguration;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import at.gv.egiz.bku.conf.IAIKLogAdapterFactory;
import at.gv.egiz.bku.conf.MoccaConfigurationFacade;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PKIProfileFactoryBean implements FactoryBean, ResourceLoaderAware {

  protected static final Logger log = LoggerFactory.getLogger(PKIProfileFactoryBean.class);

  /**
   * The configuration facade.
   */
  protected final ConfigurationFacade configurationFacade = new ConfigurationFacade();

  public class ConfigurationFacade implements MoccaConfigurationFacade {
    
    private Configuration configuration;
    
    public static final String SSL_CERT_DIRECTORY = "SSL.certDirectory";
    
    public static final String SSL_CERT_DIRECTORY_DEFAULT = "classpath:at/gv/egiz/bku/certs/certStore";
    
    public static final String SSL_CA_DIRECTORY = "SSL.caDirectory";
    
    public static final String SSL_CA_DIRECTORY_DEFAULT = "classpath:at/gv/egiz/bku/certs/trustStore";
    
    public static final String SSL_REVOCATION_SERVICE_ORDER = "SSL.revocationServiceOrder";

    public URL getCertDirectory() throws MalformedURLException {
      return getURL(SSL_CERT_DIRECTORY);
    }
    
    public URL getCaDirectory() throws MalformedURLException {
      return getURL(SSL_CA_DIRECTORY);
    }

    @SuppressWarnings("unchecked")
    public List<String> getRevocationServiceOrder() throws Exception {
      return configuration.getList(SSL_REVOCATION_SERVICE_ORDER);
    }

    private URL getURL(String key) throws MalformedURLException {
      String url = configuration.getString(key);
      if (url == null || url.isEmpty()) {
        return null;
      }
      return new URL(getBasePath(key), configuration.getString(key));
    }

    private URL getBasePath(String key) {
      Configuration configuration = this.configuration;
      if (configuration instanceof CompositeConfiguration) {
        CompositeConfiguration compositeConfiguration = (CompositeConfiguration) configuration;
        for (int i = 0; i < compositeConfiguration.getNumberOfConfigurations(); i++) {
          if (compositeConfiguration.getConfiguration(i).containsKey(key)) {
            configuration = compositeConfiguration.getConfiguration(i);
            break;
          }
        }
      }
      if (configuration instanceof FileConfiguration) {
        return ((FileConfiguration) configuration).getURL();
      }
      return null;
    }
    
  }

  
  private ResourceLoader resourceLoader;
  
  protected String trustProfileId;

  @Override
  public void setResourceLoader(ResourceLoader loader) {
    this.resourceLoader = loader;
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

  /**
   * @return the trustProfileId
   */
  public String getTrustProfileId() {
    return trustProfileId;
  }

  /**
   * @param trustProfileId the trustProfileId to set
   */
  public void setTrustProfileId(String trustProfileId) {
    this.trustProfileId = trustProfileId;
  }

  protected File getDirectory(String url) throws IOException {
    Resource resource = resourceLoader.getResource(url);
    File path = resource.getFile();
    if (!path.exists() && !path.isDirectory()) {
      throw new IOException("URL '" + url + "' is not a directory.");
    }
    return path;
  }
  
  protected void configureIAIKLogging() {
    // initialize IAIK logging for PKI module
    iaik.logging.LogFactory.configure(new LoggerConfig() {
      
      @Override
      public Properties getProperties() throws LogConfigurationException {
        return null;
      }
      
      @Override
      public String getNodeId() {
        return "pki";
      }
      
      @Override
      public String getFactory() {
        return IAIKLogAdapterFactory.class.getName();
      }
    });
  }
  
  protected void configurePkiFactory() throws MalformedURLException, PKIException, IOException {
    
    URL url = configurationFacade.getCertDirectory();
    File certDirectory = (url != null) 
        ? getDirectory(url.toString())
        : getDirectory(ConfigurationFacade.SSL_CERT_DIRECTORY_DEFAULT);
    
    CertStoreParameters[] certStoreParameters = { new DefaultDirectoryCertStoreParameters(
        "CS", certDirectory.getAbsolutePath(), true, false) };
    
    DefaultPKIConfiguration pkiConfiguration = new DefaultPKIConfiguration(certStoreParameters);
  
    
    PKIFactory pkiFactory = PKIFactory.getInstance();
    pkiFactory.configure(pkiConfiguration, new TransactionIdImpl("Configure-PKI"));
  }

  protected TrustStoreProfile createDirectoryTrustStoreProfile() throws MalformedURLException, IOException {
    
    URL url = configurationFacade.getCaDirectory();
    File caDirectory = (url != null) 
        ? getDirectory(url.toString())
        : getDirectory(ConfigurationFacade.SSL_CA_DIRECTORY_DEFAULT);        
    
    return new DefaultTrustStoreProfile(trustProfileId,
        TrustStoreTypes.DIRECTORY, caDirectory.getAbsolutePath());
    
  }

  protected String[] createRevocationServiceOrder() throws Exception {
    List<String> services = configurationFacade.getRevocationServiceOrder();

    if (services != null) {
      List<String> order = new ArrayList<String>(2);
      for (String service : services) {
        if ("OCSP".equals(service)) {
          order.add(RevocationSourceTypes.OCSP);
        } else if ("CRL".equals(service)) {
          order.add(RevocationSourceTypes.CRL);
        } else {
          throw new Exception("Unsupported revocation service type " + service);
        }
      }
      if (!order.isEmpty()) {
        log.info("configure revocation service type order: {}", order);
        return order.toArray(new String[order.size()]);
      }
    }
    log.info("configure default revocation service type order: [OCSP, CRL]");
    return new String[]
      { RevocationSourceTypes.OCSP, RevocationSourceTypes.CRL };
  }
  
  @Override
  public Object getObject() throws Exception {
    
    configureIAIKLogging();

    PKIFactory pkiFactory = PKIFactory.getInstance();
    
    if (!pkiFactory.isAlreadyConfigured()) {
      configurePkiFactory();
    }

    TrustStoreProfile trustProfile = createDirectoryTrustStoreProfile();

    DefaultPKIProfile pkiProfile = new DefaultPKIProfile(trustProfile);
    
    pkiProfile.setAutoAddCertificates(true);
    pkiProfile.setPreferredServiceOrder(createRevocationServiceOrder());
    
    return pkiProfile;
  }

  @Override
  public Class<?> getObjectType() {
    return PKIProfile.class;
  }

  @Override
  public boolean isSingleton() {
    return false;
  }
  
}
