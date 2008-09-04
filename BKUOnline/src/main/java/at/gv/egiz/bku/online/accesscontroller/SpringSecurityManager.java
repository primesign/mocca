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
