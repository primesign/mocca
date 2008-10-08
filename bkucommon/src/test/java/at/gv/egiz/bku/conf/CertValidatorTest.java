package at.gv.egiz.bku.conf;

import iaik.x509.X509Certificate;

import java.io.File;
import java.io.IOException;
import java.security.cert.CertificateException;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class CertValidatorTest {
  
  private CertValidator cv;
  
  @Before
  public void setUp() {
    cv = new CertValidatorImpl();
    String caDir = getClass().getClassLoader().getResource("at/gv/egiz/bku/conf/certs/CACerts").getPath();
    String certDir = getClass().getClassLoader().getResource("at/gv/egiz/bku/conf/certs/certStore").getPath();
    cv.init(new File(caDir), new File(certDir));
  }
  
  @Test
  public void testValid() throws CertificateException, IOException {
    X509Certificate cert = new X509Certificate(getClass().getClassLoader().getResourceAsStream("at/gv/egiz/bku/conf/certs/testCerts/www.a-trust.at.der"));
    assertTrue(cv.isCertificateValid("TID", new X509Certificate[]{cert}));
  }

}
