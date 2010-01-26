package at.gv.egiz.bku.accesscontroller;

import static org.junit.Assert.assertTrue;

import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import org.junit.Before;
import org.junit.Test;

public class AuthenticationClassifierTest {

	private X509Certificate atrust;

	@Before
	public void setUp() throws Exception {
		atrust = (X509Certificate) CertificateFactory.getInstance("X509")
				.generateCertificate(
						getClass().getClassLoader().getResourceAsStream(
								"at/gv/egiz/bku/accesscontroller/www.a-trust.at.crt"));
	}

	@Test
	public void testATrust() {
		assertTrue(AuthenticationClassifier.isGovAgency(atrust));
	}

}
