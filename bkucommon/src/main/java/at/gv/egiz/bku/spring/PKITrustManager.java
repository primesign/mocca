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



package at.gv.egiz.bku.spring;

import iaik.logging.TransactionId;
import iaik.pki.PKIException;
import iaik.pki.PKIFactory;
import iaik.pki.PKIModule;
import iaik.pki.PKIProfile;
import iaik.pki.store.truststore.TrustStore;
import iaik.pki.store.truststore.TrustStoreException;
import iaik.pki.store.truststore.TrustStoreFactory;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Set;

import javax.net.ssl.X509TrustManager;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import at.gv.egiz.bku.conf.MoccaConfigurationFacade;

public class PKITrustManager implements X509TrustManager {
  
  Logger log = LoggerFactory.getLogger(PKITrustManager.class);

  protected PKIProfile pkiProfile;

  /**
   * The configuration facade.
   */
  protected final ConfigurationFacade configurationFacade = new ConfigurationFacade();
  
  public class ConfigurationFacade implements MoccaConfigurationFacade {
    
    private Configuration configuration;
    
    public static final String SSL_DISSABLE_ALL_CHECKS = "SSL.disableAllChecks";
    
    public boolean disableAllSslChecks() {
      return configuration.getBoolean(SSL_DISSABLE_ALL_CHECKS, false);
    }
    
  }

  /**
   * @return the configuration
   */
  public Configuration getConfiguration() {
    return configurationFacade.configuration;
  }

  /**
   * @param configuration the configuration to set
   */
  public void setConfiguration(Configuration configuration) {
    configurationFacade.configuration = configuration;
  }
  
  /**
   * @return the pkiProfile
   */
  public PKIProfile getPkiProfile() {
    return pkiProfile;
  }

  /**
   * @param pkiProfile the pkiProfile to set
   */
  public void setPkiProfile(PKIProfile pkiProfile) {
    this.pkiProfile = pkiProfile;
  }

  @Override
  public void checkClientTrusted(X509Certificate[] chain, String authType)
      throws CertificateException {
    checkServerTrusted(chain, authType);
  }

  @Override
  public void checkServerTrusted(X509Certificate[] chain, String authType)
      throws CertificateException {

    if (pkiProfile == null) {
      throw new CertificateException("No PKI profile set. Configuration error.");
    }
    
    if (configurationFacade.disableAllSslChecks()) {
      log.warn("SSL certificate validation disabled. " +
      		"Accepted certificate {}.", chain[0].getSubjectDN());
    } else {

      iaik.x509.X509Certificate[] certs = convertCerts(chain);
      
      TransactionId tid = new MDCTransactionId();    
      try {
        PKIModule pkiModule = PKIFactory.getInstance().getPKIModule(pkiProfile);
        if (!pkiModule.validateCertificate(new Date(), certs[0], certs, null,
            tid).isCertificateValid()) {
          throw new CertificateException("Certificate not valid.");
        }
      } catch (PKIException e) {
        log.warn("Failed to validate certificate.", e);
        throw new CertificateException("Failed to validate certificate. " + e.getMessage());
      }
      
    }

  }

  @Override
  public X509Certificate[] getAcceptedIssuers() {
    
    if (pkiProfile == null) {
      log.warn("No PKI profile set. Configuration error.");
      return new X509Certificate[] {};
    }
    
    TransactionId tid = new MDCTransactionId();
    
    try {
      
      TrustStore trustStore = TrustStoreFactory.getInstance(pkiProfile.getTrustStoreProfile(), tid);
      @SuppressWarnings("unchecked")
      Set<X509Certificate> certs = trustStore.getTrustedCertificates(tid);
      return certs.toArray(new X509Certificate[certs.size()]);
    } catch (TrustStoreException e) {
      log.warn("Failed to get list of accepted issuers.", e);
      return new X509Certificate[] {};
    } catch (ClassCastException e) {
      log.error("Failed to cast list of accepted issuers.", e);
      return new X509Certificate[] {};
    }
  }

  private static iaik.x509.X509Certificate[] convertCerts(
      X509Certificate[] certs) throws CertificateException {
    iaik.x509.X509Certificate[] retVal = new iaik.x509.X509Certificate[certs.length];
    int i = 0;
    for (X509Certificate cert : certs) {
      if (cert instanceof iaik.x509.X509Certificate) {
        retVal[i++] = (iaik.x509.X509Certificate) cert;
      } else {
        retVal[i++] = new iaik.x509.X509Certificate(cert.getEncoded());
      }
    }
    return retVal;
  }

  private static class MDCTransactionId implements TransactionId {
    @Override
    public String getLogID() {
      String sessionId = MDC.get("SessionId");
      return (sessionId != null) ? sessionId : "PKITrustManager"; 
    }
  }
}
