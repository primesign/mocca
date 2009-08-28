package at.gv.egiz.bku.webstart;

import iaik.asn1.CodingException;
import iaik.asn1.ObjectID;
import iaik.asn1.structures.AlgorithmID;
import iaik.asn1.structures.GeneralName;
import iaik.asn1.structures.GeneralNames;
import iaik.asn1.structures.Name;
import iaik.x509.X509Certificate;
import iaik.x509.extensions.AuthorityKeyIdentifier;
import iaik.x509.extensions.BasicConstraints;
import iaik.x509.extensions.ExtendedKeyUsage;
import iaik.x509.extensions.KeyUsage;

import iaik.x509.extensions.SubjectAltName;
import iaik.x509.extensions.SubjectKeyIdentifier;
import java.io.IOException;
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

public class TLSServerCA {
  public static final int CA_VALIDITY_Y = 3;
  public static final String MOCCA_TLS_SERVER_ALIAS = "server";
  public static final int SERVER_VALIDITY_Y = 3;
  private final static Log log = LogFactory.getLog(TLSServerCA.class);

  private KeyPair caKeyPair;
  private X509Certificate caCert;

  private KeyPair serverKeyPair;
  private X509Certificate serverCert;

  private KeyPair generateKeyPair() throws NoSuchAlgorithmException {
    KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
    gen.initialize(2048);
    return gen.generateKeyPair();
  }

  private void generateCACert() throws GeneralSecurityException, CodingException {
    log.debug("generating MOCCA CA certificate");
    Name subject = new Name();
    subject.addRDN(ObjectID.country, "AT");
    subject.addRDN(ObjectID.organization, "MOCCA");
    subject.addRDN(ObjectID.organizationalUnit, "MOCCA TLS Server CA");

    caKeyPair = generateKeyPair();
    caCert = new X509Certificate();
    caCert.setSerialNumber(new BigInteger(20, new Random()));
    caCert.setSubjectDN(subject);
    caCert.setPublicKey(caKeyPair.getPublic());
    caCert.setIssuerDN(subject);

    caCert.addExtension(new SubjectKeyIdentifier(caKeyPair.getPublic()));

    BasicConstraints bc = new BasicConstraints(true);
    bc.setCritical(true);
    caCert.addExtension(bc);
    KeyUsage ku = new KeyUsage(KeyUsage.keyCertSign | KeyUsage.cRLSign
        | KeyUsage.digitalSignature);
    ku.setCritical(true);
    caCert.addExtension(ku);

    GregorianCalendar date = new GregorianCalendar();
    date.add(Calendar.HOUR_OF_DAY, -1);
    caCert.setValidNotBefore(date.getTime());
    date.add(Calendar.YEAR, CA_VALIDITY_Y);
    caCert.setValidNotAfter(date.getTime());
    caCert.sign(AlgorithmID.sha1WithRSAEncryption, caKeyPair.getPrivate());
    
    log.debug("successfully generated MOCCA TLS Server CA certificate " + caCert.getSubjectDN());
  }

  private void generateServerCert() throws GeneralSecurityException, CodingException {
    log.debug("generating MOCCA server certificate");
    Name subject = new Name();
    subject.addRDN(ObjectID.country, "AT");
    subject.addRDN(ObjectID.organization, "MOCCA");
    subject.addRDN(ObjectID.organizationalUnit, "MOCCA TLS Server");
    subject.addRDN(ObjectID.commonName, "localhost");
    subject.addRDN(ObjectID.commonName, "127.0.0.1");

    serverKeyPair = generateKeyPair();
    serverCert = new X509Certificate();
    serverCert.setSerialNumber(new BigInteger(20, new Random()));
    serverCert.setSubjectDN(subject);
    serverCert.setPublicKey(serverKeyPair.getPublic());
    serverCert.setIssuerDN(caCert.getSubjectDN());

    serverCert.addExtension(new SubjectKeyIdentifier(serverKeyPair.getPublic()));
    byte[] aki = new SubjectKeyIdentifier(caCert.getPublicKey()).get();
    serverCert.addExtension(new AuthorityKeyIdentifier(aki));

    serverCert.addExtension(new ExtendedKeyUsage(ExtendedKeyUsage.serverAuth));

    GeneralNames altNames = new GeneralNames();
    altNames.addName(new GeneralName(GeneralName.dNSName, "localhost"));
    altNames.addName(new GeneralName(GeneralName.dNSName, "127.0.0.1"));
    altNames.addName(new GeneralName(GeneralName.iPAddress, "127.0.0.1"));
    serverCert.addExtension(new SubjectAltName(altNames));

    serverCert.addExtension(new BasicConstraints(false));
    serverCert.addExtension(new KeyUsage(KeyUsage.keyEncipherment
        | KeyUsage.digitalSignature));

    GregorianCalendar date = new GregorianCalendar();
    date.add(Calendar.HOUR_OF_DAY, -1);
    serverCert.setValidNotBefore(date.getTime());
    date.add(Calendar.YEAR,SERVER_VALIDITY_Y);
    date.add(Calendar.HOUR_OF_DAY, -1);
    serverCert.setValidNotAfter(date.getTime());
    serverCert.sign(AlgorithmID.sha1WithRSAEncryption, caKeyPair.getPrivate());

    log.debug("successfully generated MOCCA TLS Server certificate " + serverCert.getSubjectDN());
    caKeyPair = null;
  }

  public KeyStore generateKeyStore(char[] password) throws GeneralSecurityException, IOException, CodingException {
//    try {
      generateCACert();
      generateServerCert();
      KeyStore ks = KeyStore.getInstance("JKS");
      ks.load(null, null);
      ks.setKeyEntry(MOCCA_TLS_SERVER_ALIAS, serverKeyPair.getPrivate(), password, new X509Certificate[]{serverCert, caCert});
      return ks;
//    } catch (Exception e) {
//      log.error("Cannot generate certificate", e);
//    }
//    return null;
  }

}
