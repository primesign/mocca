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
import iaik.security.provider.IAIK;
import iaik.xml.crypto.XSecProvider;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.Provider;
import java.security.Security;
import java.util.Properties;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import at.gv.egiz.bku.binding.DataUrl;
import at.gv.egiz.bku.binding.DataUrlConnection;
import at.gv.egiz.bku.slcommands.impl.xsect.DataObject;
import at.gv.egiz.bku.slcommands.impl.xsect.STALProvider;

/**
 * 
 * TODO currently only the code to get started.
 */
public abstract class Configurator {

	private Log log = LogFactory.getLog(Configurator.class);
	
	private static Configurator instance = new SpringConfigurator();

	protected Properties properties;

	protected Configurator() {
	}

	public static Configurator getInstance() {
	  return instance;
	}
	
	protected void configUrlConnections() {
		HttpsURLConnection.setFollowRedirects(false);
		HttpURLConnection.setFollowRedirects(false);
	}

	protected void configureProviders() {
		log.debug("Registering security providers");
		Security.insertProviderAt(new IAIK(), 1);
		Security.insertProviderAt(new ECCProvider(false), 2);
		Security.addProvider(new STALProvider());
		XSecProvider.addAsProvider(false);
		StringBuilder sb = new StringBuilder();
		sb.append("Registered providers: ");
		int i = 1;
		for (Provider prov : Security.getProviders()) {
			sb.append((i++) + ". : " + prov);
		}
		log.debug(sb.toString());
	}

	protected void configViewer() {
	  DataObject.enableHashDataInputValidation(Boolean.parseBoolean(properties.getProperty("ValidateHashDataInputs")));
	}
	
	public void configure() {
		configureProviders();
		configUrlConnections();
		configViewer();
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
