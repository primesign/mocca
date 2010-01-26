package at.gv.egiz.bku.conf;

import iaik.x509.X509Certificate;

import java.io.File;

public interface CertValidator {

  public abstract void init(File certDir, File caDir);

  public abstract boolean isCertificateValid(String transactionId, X509Certificate[] certs);

}