package at.gv.egiz.bku.online.conf;

import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SSLConfigTest {
	
	private SpringConfigurator cfg;
	private ApplicationContext ctx;
		
	@Before
	public void setUp() throws Exception {
		ctx = new ClassPathXmlApplicationContext("applicationContext.xml");
		
	}
	
	@Test
	public void testConnect() throws Exception {
		String url = "https://apps.egiz.gv.at/exchange-moa-id-auth/VerifyIdentityLink?MOASessionID=8151862969943601574";
		URL u = new URL(url);
		HttpsURLConnection uc = (HttpsURLConnection) u.openConnection();
		uc.connect();
		System.out.println(uc.getCipherSuite());
	}
	
	@After
	public void shutDown() {
		
	}
}
