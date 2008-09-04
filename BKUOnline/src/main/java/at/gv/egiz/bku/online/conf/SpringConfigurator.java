package at.gv.egiz.bku.online.conf;

import java.io.IOException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.Resource;

public class SpringConfigurator extends Configurator {

	private final static Log log = LogFactory.getLog(SpringConfigurator.class);

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
		}
	}

}
