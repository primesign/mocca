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
package at.gv.egiz.bku.online.conf;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import at.gv.egiz.bku.conf.Configurator;
import at.gv.egiz.bku.online.webapp.SpringBKUServlet;
import at.gv.egiz.bku.slexceptions.SLRuntimeException;
import at.gv.egiz.stal.service.impl.RequestBrokerSTALFactory;

public class SpringConfigurator extends Configurator implements
    ResourceLoaderAware {

  private final static Log log = LogFactory.getLog(SpringConfigurator.class);

  private ResourceLoader resourceLoader;

  public void setResource(Resource resource) {
    log.debug("Loading config from: " + resource);
    if (resource != null) {
      Properties props = new Properties();
      try {
        props.load(resource.getInputStream());
        super.setConfiguration(props);
      } catch (IOException e) {
        log.error("Cannot load config", e);
      }
    } else {
      log.warn("Cannot load properties, resource: " + resource);
    }
  }

  public void configureNetwork() {
    super.configureNetwork();
    String appletTimeout = getProperty("AppletTimeout");
    if ((appletTimeout != null)) {
      try {
        long ato = Long.parseLong(appletTimeout);
        log.debug("Setting applet timeout to:"+ato);
        RequestBrokerSTALFactory.setTimeout(ato);
      } catch (NumberFormatException nfe) {
        log.error("Cannot set Applettimeout", nfe);
      }

    }
  }
  
  public void configure() {
    super.configure();
    SpringBKUServlet.setConfigurator(this);
  }

  @Override
  public void setResourceLoader(ResourceLoader loader) {
    this.resourceLoader = loader;
  }

  private File getDirectory(String property) {
    if (property != null) {
      Resource certDirRes = resourceLoader.getResource(property);
      File certDir;
      try {
        certDir = certDirRes.getFile();
      } catch (IOException e) {
        log.error("Cannot get cert directory", e);
        throw new SLRuntimeException(e);
      }
      if (!certDir.isDirectory()) {
        log.error("Expecting directory as SSL.certDirectory parameter");
        throw new SLRuntimeException(
            "Expecting directory as SSL.certDirectory parameter");
      }
      return certDir;
    }
    return null;

  }

  @Override
  protected File getCADir() {
    String caDirectory = getProperty("SSL.caDirectory");
    return getDirectory(caDirectory);
  }

  @Override
  protected File getCertDir() {
    String certDirectory = getProperty("SSL.certDirectory");
    return getDirectory(certDirectory);
  }

  @Override
  protected InputStream getManifest() {
    Resource r = resourceLoader.getResource("META-INF/MANIFEST.MF");
    if (r != null) {
      try {
        return r.getInputStream();
      } catch (IOException e) {
        log.error("Cannot read manifest data:", e);
      }
    }
    return null;
  }
}