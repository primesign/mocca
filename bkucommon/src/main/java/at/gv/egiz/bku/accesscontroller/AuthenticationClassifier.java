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
package at.gv.egiz.bku.accesscontroller;

import static at.gv.egiz.bku.accesscontroller.AuthenticationClass.ANONYMOUS;
import static at.gv.egiz.bku.accesscontroller.AuthenticationClass.CERTIFIED;
import static at.gv.egiz.bku.accesscontroller.AuthenticationClass.CERTIFIED_GOV_AGENCY;
import static at.gv.egiz.bku.accesscontroller.AuthenticationClass.PSEUDO_ANONYMOUS;

import java.net.URL;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AuthenticationClassifier {
	private static AuthenticationClassifier instance = new AuthenticationClassifier();
	private static Log log = LogFactory.getLog(AuthenticationClassifier.class);
	private final static String GOV_DOMAIN = ".gv.at";

	private AuthenticationClassifier() {
	}

	public static boolean isGovAgency(X509Certificate cert) {
		String[] rdns = (cert.getSubjectX500Principal().getName()).split(",");
		for (String rdn : rdns) {
			if (rdn.startsWith("CN=")) {
				String dns = rdn.split("=")[1];
				log.trace("Analyzing cn dn: " + dns);
				if (dns.endsWith(GOV_DOMAIN)) {
					return true;
				}
			}
		}
		try {
			Collection<List<?>> sanList = cert.getSubjectAlternativeNames();
			if (sanList != null) {
				for (List<?> san : sanList) {
					log.trace("Analyzing subj. alt name: " + san);
					if ((Integer) san.get(0) == 2) {
						String dns = (String) san.get(1);
						if (dns.endsWith(GOV_DOMAIN)) {
							return true;
						}
					}
				}
			}
		} catch (CertificateParsingException e) {
			log.error(e);
		}
		if ((cert.getExtensionValue("1.2.40.0.10.1.1.1") != null)
        || (cert.getExtensionValue("1.2.40.0.10.1.1.2") != null)) {
			return true;
		}
		return false;
	}

	/**
	 * Client Certificates are currently not supported
	 * 
	 */
	protected AuthenticationClass getMyAuthenticationClass(boolean isDataUrl,
			URL url, X509Certificate cert) {
		if (isDataUrl) {
			if (url.getProtocol().equalsIgnoreCase("https")) {
				if (isGovAgency(cert)) {
					return CERTIFIED_GOV_AGENCY;
				}
				if (cert.getExtensionValue("1.2.40.0.10.1.1.1") != null) {
					return CERTIFIED_GOV_AGENCY;
				}
				return CERTIFIED;
			} else {
				return PSEUDO_ANONYMOUS;
			}
		} else {
			return ANONYMOUS;
		}
	}

	/**
	 * 
	 * @param isDataUrl
	 * @param url
	 *          if the url's protocol is https a cert parameter must be provided.
	 * @param cert
	 * @return
	 */
	public static AuthenticationClass getAuthenticationClass(boolean isDataUrl,
			URL url, X509Certificate cert) {
		return instance.getMyAuthenticationClass(isDataUrl, url, cert);
	}
}
