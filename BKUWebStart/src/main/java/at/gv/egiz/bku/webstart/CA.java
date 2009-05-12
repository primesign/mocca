package at.gv.egiz.bku.webstart;

import iaik.asn1.ObjectID;
import iaik.asn1.structures.AlgorithmID;
import iaik.asn1.structures.Name;
import iaik.x509.X509Certificate;
import iaik.x509.extensions.BasicConstraints;
import iaik.x509.extensions.KeyUsage;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CA {
  private final static Log log = LogFactory.getLog(CA.class);

  private KeyPair caKeyPair;
  private X509Certificate caCert;

  private KeyPair serverKeyPair;
  private X509Certificate serverCert;

  public CA() {
  }

  private KeyPair generateKeyPair() throws NoSuchAlgorithmException {
    KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
    gen.initialize(2048);
    return gen.generateKeyPair();
  }

  private void generateCA() throws GeneralSecurityException {
    log.debug("Generating CA certificate");
    Name subject = new Name();
    subject.addRDN(ObjectID.country, "AT");
    subject.addRDN(ObjectID.organization, "MOCCA");
    subject.addRDN(ObjectID.organizationalUnit, "MOCCA-CA");

    caKeyPair = generateKeyPair();
    caCert = new X509Certificate();
    caCert.setSerialNumber(new BigInteger(20, new Random()));
    caCert.setSubjectDN(subject);
    caCert.setPublicKey(caKeyPair.getPublic());
    caCert.setIssuerDN(subject);

    caCert.addExtension(new BasicConstraints(true));
    caCert.addExtension(new KeyUsage(KeyUsage.keyCertSign | KeyUsage.cRLSign
        | KeyUsage.digitalSignature));

    GregorianCalendar date = new GregorianCalendar();
    date.add(Calendar.HOUR_OF_DAY, -1);
    caCert.setValidNotBefore(date.getTime());
    date.add(Calendar.YEAR, 7);
    caCert.setValidNotAfter(date.getTime());
    caCert.sign(AlgorithmID.sha1WithRSAEncryption, caKeyPair.getPrivate());
    log.debug("Successfully signed CA certificate");
  }

  private void generateServerCert() throws GeneralSecurityException {
    log.debug("Generating SSL certificate");
    Name subject = new Name();
    subject.addRDN(ObjectID.country, "AT");
    subject.addRDN(ObjectID.organization, "MOCCA");
    try {
      subject.addRDN(ObjectID.commonName, InetAddress.getLocalHost()
          .getHostName());
    } catch (UnknownHostException e) {
      subject.addRDN(ObjectID.commonName, "localhost");
    }
    serverKeyPair = generateKeyPair();
    serverCert = new X509Certificate();
    serverCert.setSerialNumber(new BigInteger(20, new Random()));
    serverCert.setSubjectDN(subject);
    serverCert.setPublicKey(serverKeyPair.getPublic());
    serverCert.setIssuerDN(caCert.getSubjectDN());

    serverCert.addExtension(new BasicConstraints(false));
    serverCert.addExtension(new KeyUsage(KeyUsage.keyEncipherment
        | KeyUsage.digitalSignature));

    GregorianCalendar date = new GregorianCalendar();
    date.add(Calendar.HOUR_OF_DAY, -1);
    serverCert.setValidNotBefore(date.getTime());
    date.add(Calendar.YEAR, 7);
    date.add(Calendar.HOUR_OF_DAY, -1);
    serverCert.setValidNotAfter(date.getTime());
    serverCert.sign(AlgorithmID.sha1WithRSAEncryption, caKeyPair.getPrivate());
    log.debug("Successfully signed server certificate");
    caKeyPair = null;
  }

  public KeyStore generateKeyStore(char[] password) {
    try {
      generateCA();
      generateServerCert();
      KeyStore ks = KeyStore.getInstance("JKS");
      ks.load(null, null);
      ks.setKeyEntry("server", serverKeyPair.getPrivate(), password, new X509Certificate[]{serverCert, caCert});
      return ks;
    } catch (Exception e) {
      log.error("Cannot generate certificate", e);
    }
    return null;
  }

}
