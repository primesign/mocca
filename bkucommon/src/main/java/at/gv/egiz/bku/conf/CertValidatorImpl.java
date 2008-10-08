package at.gv.egiz.bku.conf;

import iaik.logging.TransactionId;
import iaik.logging.impl.TransactionIdImpl;
import iaik.pki.DefaultPKIConfiguration;
import iaik.pki.DefaultPKIProfile;
import iaik.pki.PKIConfiguration;
import iaik.pki.PKIException;
import iaik.pki.PKIFactory;
import iaik.pki.PKIModule;
import iaik.pki.PKIProfile;
import iaik.pki.store.certstore.CertStoreParameters;
import iaik.pki.store.certstore.directory.DefaultDirectoryCertStoreParameters;
import iaik.pki.store.truststore.DefaultTrustStoreProfile;
import iaik.pki.store.truststore.TrustStoreProfile;
import iaik.pki.store.truststore.TrustStoreTypes;
import iaik.x509.X509Certificate;

import java.io.File;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CertValidatorImpl implements CertValidator {

  private static Log log = LogFactory.getLog(CertValidatorImpl.class);

  private PKIFactory pkiFactory;
  private PKIProfile profile;

  public CertValidatorImpl() {

  }

  /* (non-Javadoc)
   * @see at.gv.egiz.bku.conf.CertValidator#init(java.io.File, java.io.File)
   */
  public void init(File certDir, File caDir) {
    // the parameters specifying the directory certstore
    CertStoreParameters[] certStoreParameters = { new DefaultDirectoryCertStoreParameters(
        "CS-001", certDir.getAbsolutePath(), true, false) };

    // create a new PKI configuration using the certstore parameters
    PKIConfiguration pkiConfig = new DefaultPKIConfiguration(
        certStoreParameters);

    // Transaction ID for logging
    TransactionId tid = new TransactionIdImpl("Configure-PKI");
    // get PKI factory for creating PKI module(s)
    pkiFactory = PKIFactory.getInstance();
    // configure the factory
    try {
      pkiFactory.configure(pkiConfig, tid);
    } catch (PKIException e) {
      log.error("Cannot configure PKI module", e);
    }
    // the truststore to be used
    TrustStoreProfile trustProfile = new DefaultTrustStoreProfile("TS-001",
        TrustStoreTypes.DIRECTORY, caDir.getAbsolutePath());
    profile = new DefaultPKIProfile(trustProfile);
    ((DefaultPKIProfile)profile).setAutoAddCertificates(true);
  }

  /* (non-Javadoc)
   * @see at.gv.egiz.bku.conf.CertValidator#isCertificateValid(java.lang.String, iaik.x509.X509Certificate[])
   */
  public boolean isCertificateValid(String transactionId,
      X509Certificate[] certs) {
    // Transaction ID for logging
    TransactionId tid = new TransactionIdImpl(transactionId);
    // get a PKIModule
    PKIModule pkiModule;
    try {
      pkiModule = pkiFactory.getPKIModule(profile);
      return pkiModule.validateCertificate(new Date(), certs[0], certs, null,
          tid).isCertificateValid();
    } catch (PKIException e) {
      log.error("Cannot validate certificate", e);
    }
    return false;
  }
}
