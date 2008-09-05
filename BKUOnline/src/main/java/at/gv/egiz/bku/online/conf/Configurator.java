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

import iaik.security.ecc.provider.ECCProvider;
import iaik.xml.crypto.XSecProvider;

import java.net.HttpURLConnection;
import java.security.Provider;
import java.security.Security;
import java.util.Properties;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import at.gv.egiz.bku.slcommands.impl.xsect.STALProvider;

/**
 * 
 * TODO currently only the code to get started.
 */
public class Configurator {

	private Log log = LogFactory.getLog(Configurator.class);

	protected Properties properties;

	public Configurator() {
	}

	protected void configUrlConnections() {
		HttpsURLConnection.setFollowRedirects(false);
		HttpURLConnection.setFollowRedirects(false);
	}

	protected void configureProviders() {
		log.debug("Registering security providers");
		Security.addProvider(new STALProvider());
		XSecProvider.addAsProvider(false);
		Security.insertProviderAt(new ECCProvider(false), 1);
		StringBuffer sb = new StringBuffer();
		sb.append("Following providers are now registered: ");
		int i = 1;
		for (Provider prov : Security.getProviders()) {
			sb.append((i++) + ". : " + prov);
		}
		log.debug("Configured provider" + sb.toString());
	}

	public void configure() {
		configureProviders();
		configUrlConnections();
	}

	public void setConfiguration(Properties props) {
		this.properties = props;
	}

	public String getProperty(String key) {
		if (properties != null) {
			return properties.getProperty(key);
		}
		return null;
	}
}
