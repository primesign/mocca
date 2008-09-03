package at.gv.egiz.bku.accesscontroller;

import static at.gv.egiz.bku.accesscontroller.AuthenticationClass.ANONYMOUS;
import static at.gv.egiz.bku.accesscontroller.AuthenticationClass.CERTIFIED;
import static at.gv.egiz.bku.accesscontroller.AuthenticationClass.PSEUDO_ANONYMOUS;
import static at.gv.egiz.bku.accesscontroller.AuthenticationClass.CERTIFIED_GOV_AGENCY;

import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.cert.X509Certificate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AuthenticationClassifier {
	private static AuthenticationClassifier instance = new AuthenticationClassifier();
	private static Log log = LogFactory.getLog(AuthenticationClassifier.class);
	private final static String GOV_DOMAIN = ".gv.at";

	private AuthenticationClassifier() {
	}

	/**
	 * Client Certificates are currently not supported
	 * 
	 */
	protected AuthenticationClass getMyAuthenticationClass(boolean isDataUrl,
			URL url, X509Certificate cert) {
		if (isDataUrl) {
			if (url.getProtocol().equalsIgnoreCase("https")) {
				try {
					if (InetAddress.getByName(url.getHost()).getCanonicalHostName()
							.endsWith(GOV_DOMAIN)) {
						return CERTIFIED_GOV_AGENCY;
					}
				} catch (UnknownHostException e) {
					log.error("Cannot determine host name", e);
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
	 * @param url if the url's protocol is https a cert parameter must be provided.
	 * @param cert
	 * @return
	 */
	public static AuthenticationClass getAuthenticationClass(boolean isDataUrl,
			URL url, X509Certificate cert) {
		return instance.getMyAuthenticationClass(isDataUrl, url, cert);
	}
}
