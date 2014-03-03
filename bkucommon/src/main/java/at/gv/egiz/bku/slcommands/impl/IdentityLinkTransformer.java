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


package at.gv.egiz.bku.slcommands.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.Templates;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import at.gv.egiz.bku.conf.MoccaConfigurationFacade;

public class IdentityLinkTransformer extends at.gv.egiz.idlink.IdentityLinkTransformer implements ResourceLoaderAware {

  private final Logger log = LoggerFactory.getLogger(IdentityLinkTransformer.class);

  /**
   * The configuration facade.
   */
  protected final ConfigurationFacade configurationFacade = new ConfigurationFacade();

  private class ConfigurationFacade implements MoccaConfigurationFacade {

    private Configuration configuration;
    private ResourceLoader resLoader;

    private final String ISSUER_TEMPLATE_CACHE =
        "IssuerTemplateCache";
    private final String ISSUER_TEMPLATE_CACHE_ENTRY =
        ISSUER_TEMPLATE_CACHE + ".entry";
    private final String ISSUER_TEMPLATE_CACHE_URLS =
        ISSUER_TEMPLATE_CACHE_ENTRY + ".url";
    private final String ISSUER_TEMPLATE_CACHE_RESOURCES =
        ISSUER_TEMPLATE_CACHE_ENTRY + ".resource";

    public Map<String, Templates> getIssuerTemplateCacheMap(
        TransformerFactory factory) {
      Map<String, Templates> templates =
          Collections.synchronizedMap(new HashMap<String, Templates>());
      List<Object> urls = configuration.getList(ISSUER_TEMPLATE_CACHE_URLS);
      List<Object> resources = configuration.getList(ISSUER_TEMPLATE_CACHE_RESOURCES);
      if (!urls.isEmpty() && (urls.size() == resources.size())) {
        for (int i = 0; i < urls.size(); ++i) {
          if ((urls.get(i) == null) || (resources.get(i) == null))
            continue;
          String url = urls.get(i).toString();
          String resource = resources.get(i).toString();
          Resource templRes = resLoader.getResource(resource);
          try {
            Templates template = factory.newTemplates(new StreamSource(templRes.getInputStream()));
            templates.put(url, template);
          } catch (Exception e) {
            log.error("Error initializing issuer template cache", e);
          }
        }
      }
      return templates;
    }

    public void setResourceLoader(ResourceLoader resLoader) {
      this.resLoader = resLoader;
    }
  }

  @Override
  protected Map<String, Templates> getInitialTemplateMap() {
    Map<String, Templates> templateMap = configurationFacade.getIssuerTemplateCacheMap(factory);
    log.debug("IssuerTemplate cache initialized with {} elements", templateMap.size());
    return templateMap;
  }

  /**
   * @param configuration the configuration to set
   */
  public void setConfiguration(Configuration configuration) {
    configurationFacade.configuration = configuration;
  }

  @Override
  public void setResourceLoader(ResourceLoader resLoader) {
    configurationFacade.setResourceLoader(resLoader);
  }
}
