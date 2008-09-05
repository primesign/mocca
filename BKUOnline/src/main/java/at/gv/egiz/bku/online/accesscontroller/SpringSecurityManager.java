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
package at.gv.egiz.bku.online.accesscontroller;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import at.gv.egiz.bku.accesscontroller.SecurityManagerFacade;
import at.gv.egiz.bku.online.conf.Configurator;

public class SpringSecurityManager extends SecurityManagerFacade implements
		ResourceLoaderAware {

	private ResourceLoader resourceLoader;

	private static Log log = LogFactory.getLog(SpringSecurityManager.class);

	protected Configurator config;

	public void setConfig(Configurator config) {
		this.config = config;
	}

	public void init() {
		String noMatch = config.getProperty("AccessController.acceptNoMatch");
		if (noMatch != null) {
			log.debug("Setting allow now match to: " + noMatch);
			setAllowUnmatched(Boolean.getBoolean(noMatch));
		}
		String policy = config.getProperty("AccessController.policyResource");
		log.info("Loading resource: " + policy);
		try {
			Resource res = resourceLoader.getResource(policy);
			init(res.getInputStream());
		} catch (IOException e) {
			log.error(e);
		}
	}

	@Override
	public void setResourceLoader(ResourceLoader loader) {
		this.resourceLoader = loader;
	}

}
