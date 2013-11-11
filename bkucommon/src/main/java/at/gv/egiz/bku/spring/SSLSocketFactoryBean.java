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

import iaik.pki.PKIProfile;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import org.apache.commons.configuration.Configuration;
import org.springframework.beans.factory.FactoryBean;

import at.gv.egiz.bku.conf.MoccaConfigurationFacade;

public class SSLSocketFactoryBean implements FactoryBean {

  protected PKIProfile pkiProfile;

  /**
   * The configuration facade.
   */
  protected final ConfigurationFacade configurationFacade = new ConfigurationFacade();

  public class ConfigurationFacade implements MoccaConfigurationFacade {

    private Configuration configuration;

    //avoid ClassCastException: iaik.security.ecc.ecdsa.ECPublicKey cannot be cast to java.security.interfaces.ECPublicKey
    private final String DEFAULT_DISABLED_CIPHER_SUITES =
      "TLS_ECDH_ECDSA_WITH_NULL_SHA," +
      "TLS_ECDH_ECDSA_WITH_RC4_128_SHA," +
      "TLS_ECDH_ECDSA_WITH_3DES_EDE_CBC_SHA," +
      "TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA," +
      "TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA," +
      "TLS_ECDHE_ECDSA_WITH_NULL_SHA," +
      "TLS_ECDHE_ECDSA_WITH_RC4_128_SHA," +
      "TLS_ECDHE_ECDSA_WITH_3DES_EDE_CBC_SHA," +
      "TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA," +
      "TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA," +
      "TLS_ECDH_RSA_WITH_NULL_SHA," +
      "TLS_ECDH_RSA_WITH_RC4_128_SHA," +
      "TLS_ECDH_RSA_WITH_3DES_EDE_CBC_SHA," +
      "TLS_ECDH_RSA_WITH_AES_128_CBC_SHA," +
      "TLS_ECDH_RSA_WITH_AES_256_CBC_SHA," +
      "TLS_ECDHE_RSA_WITH_NULL_SHA," +
      "TLS_ECDHE_RSA_WITH_RC4_128_SHA," +
      "TLS_ECDHE_RSA_WITH_3DES_EDE_CBC_SHA," +
      "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA," +
      "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA," +
      "TLS_ECDH_anon_WITH_NULL_SHA," +
      "TLS_ECDH_anon_WITH_RC4_128_SHA," +
      "TLS_ECDH_anon_WITH_3DES_EDE_CBC_SHA," +
      "TLS_ECDH_anon_WITH_AES_128_CBC_SHA," +
      "TLS_ECDH_anon_WITH_AES_256_CBC_SHA";

    public static final String SSL_PROTOCOL = "SSL.sslProtocol";

    public static final String SSL_DISABLE_ALL_CHECKS = "SSL.disableAllChecks";

    public static final String SSL_DISABLED_CIPHER_SUITES = "SSL.disabledCipherSuites";

    public String getSslProtocol() {
      return configuration.getString(SSL_PROTOCOL, "TLS");
    }

    public boolean disableAllSslChecks() {
      return configuration.getBoolean(SSL_DISABLE_ALL_CHECKS, false);
    }

    public String[] getDisabledCipherSuites() {
      String suites = configuration.getString(SSL_DISABLED_CIPHER_SUITES,
            DEFAULT_DISABLED_CIPHER_SUITES);
      return suites.split(",");
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
  public Object getObject() throws Exception {
    PKITrustManager pkiTrustManager = new PKITrustManager();
    pkiTrustManager.setConfiguration(configurationFacade.configuration);
    pkiTrustManager.setPkiProfile(pkiProfile);

    SSLContext sslContext = SSLContext.getInstance(configurationFacade.getSslProtocol());
    sslContext.init(null, new TrustManager[] {pkiTrustManager}, null);

    SSLSocketFactory ssf = sslContext.getSocketFactory();

    return new InternalSSLSocketFactory(ssf, configurationFacade.getDisabledCipherSuites());
  }

  @Override
  public Class<?> getObjectType() {
    return SSLSocketFactory.class;
  }

  @Override
  public boolean isSingleton() {
    return false;
  }

}
