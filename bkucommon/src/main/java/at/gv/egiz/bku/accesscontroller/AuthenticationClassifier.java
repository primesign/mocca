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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthenticationClassifier {
	private static AuthenticationClassifier instance = new AuthenticationClassifier();
	private final static String GOV_DOMAIN = ".gv.at";

	private final Logger log = LoggerFactory.getLogger(AuthenticationClassifier.class);
	
	private AuthenticationClassifier() {
	}

	public static boolean isGovAgency(X509Certificate cert) {
	    Logger log = LoggerFactory.getLogger(AuthenticationClassifier.class);
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
			log.error("Failed to parse certificate.", e);
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
  			    if (cert == null) {
  			      log.warn("HTTPS connection does not provide certificate. " +
  			      		"Therefore, assuming authentication class '" + PSEUDO_ANONYMOUS + "'.");
  			      return PSEUDO_ANONYMOUS;
  			    }
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
