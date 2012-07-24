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

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.MapConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import at.gv.egiz.bku.conf.MoccaConfigurationException;
import at.gv.egiz.bku.slcommands.impl.CreateXMLSignatureCommandImpl;

/**
 * This is a {@link FactoryBean} for the creation of a {@link Configuration}.
 * 
 * @author mcentner
 */
public class ConfigurationFactoryBean implements FactoryBean, ResourceLoaderAware {
  
  protected static final Logger log = LoggerFactory.getLogger(ConfigurationFactoryBean.class);

  public static final String DEFAULT_CONFIG = "/WEB-INF/conf/configuration.xml";

  public static final String MOCCA_IMPLEMENTATIONNAME_PROPERTY = "ProductName";

  public static final String MOCCA_IMPLEMENTATIONVERSION_PROPERTY = "ProductVersion";

  public static final String SIGNATURE_LAYOUT_PROPERTY = "SignatureLayout";

  /**
   * The URL of the configuration file.
   */
  protected Resource configurationResource;

  /**
   * Should building the configuration fail if the given configuration resource
   * is not available?
   */
  protected boolean failOnMissingResource = false;
  
  /**
   * The ResourceLoader.
   */
  protected ResourceLoader resourceLoader;
  
  @Override
  public void setResourceLoader(ResourceLoader resourceLoader) {
    this.resourceLoader = resourceLoader;
  }

  /**
   * @return the configurationURL
   */
  public Resource getConfigurationResource() {
    return configurationResource;
  }

  /**
   * @param configurationResource the configurationURL to set
   */
  public void setConfigurationResource(Resource configurationResource) {
    this.configurationResource = configurationResource;
  }

  /**
   * @param failOnMissingConfigurationResource the failOnMissingConfigurationResource to set
   */
  public void setFailOnMissingResource(
      Boolean failOnMissingResource) {
    this.failOnMissingResource = failOnMissingResource;
  }

  protected Configuration getDefaultConfiguration()
      throws ConfigurationException, IOException {
    Resource resource = resourceLoader.getResource(DEFAULT_CONFIG);
    XMLConfiguration xmlConfiguration = new XMLConfiguration();
    xmlConfiguration.load(resource.getInputStream());
    xmlConfiguration.setURL(resource.getURL());
    return xmlConfiguration;
  }
  
  protected Configuration getVersionConfiguration() throws IOException {
    
    Map<String, String> map = new HashMap<String, String>();
    map.put(MOCCA_IMPLEMENTATIONNAME_PROPERTY, "MOCCA");
    
    // implementation version
    String version = null;
    try {
      Resource resource = resourceLoader.getResource("META-INF/MANIFEST.MF");
      Manifest properties = new Manifest(resource.getInputStream());
      Attributes attributes = properties.getMainAttributes();
      // TODO: replace by Implementation-Version ?
      version = attributes.getValue("Implementation-Build");
    } catch (Exception e) {
      log.warn("Failed to get implemenation version from manifest. {}", e.getMessage());
    }
    
    if (version == null) {
      version="UNKNOWN";
    }
    map.put(MOCCA_IMPLEMENTATIONVERSION_PROPERTY, version);
    
    // signature layout
    try {
      String classContainer = CreateXMLSignatureCommandImpl.class.getProtectionDomain()
          .getCodeSource().getLocation().toString();
      URL manifestUrl = new URL("jar:" + classContainer
          + "!/META-INF/MANIFEST.MF");
      Manifest manifest = new Manifest(manifestUrl.openStream());
      Attributes attributes = manifest.getMainAttributes();
      String signatureLayout = attributes.getValue("SignatureLayout");
      if (signatureLayout != null) {
        map.put(SIGNATURE_LAYOUT_PROPERTY, signatureLayout);
      }
    } catch (Exception e) {
      log.warn("Failed to get signature layout from manifest.", e);
    }
    
    
    return new MapConfiguration(map);
    
  }
  
  @Override
  public Object getObject() throws Exception {
    
    log.info("Configuration resource is {}.", configurationResource);
    
    CompositeConfiguration configuration = null;
    if (configurationResource != null) {
      if (configurationResource.exists()) {
        // initialize with writable configuration
        URL url = configurationResource.getURL();
        XMLConfiguration writableConfiguration = new XMLConfiguration(url);
        configuration = new CompositeConfiguration(writableConfiguration);
        log.info("Initialized with configuration from '{}'.", url);
      } else if (failOnMissingResource) {
        StringBuilder message = new StringBuilder();
        message.append("ConfigurationResource '");
        message.append(configurationResource.getDescription());
        message.append("' does not exist!");
        log.error(message.toString());
        throw new MoccaConfigurationException(message.toString());
      }
    }

    if (configuration == null) {
      // initialize default configuration
      log.warn("Initializing with default configuration.");
      configuration = new CompositeConfiguration();
    }
    
    configuration.addConfiguration(getDefaultConfiguration());
    configuration.addConfiguration(getVersionConfiguration());
    return configuration;
  }

  @Override
  public Class<?> getObjectType() {
    return Configuration.class;
  }

  @Override
  public boolean isSingleton() {
    return true;
  }


}
