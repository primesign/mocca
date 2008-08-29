/*
* Copyright 2008 Federal Chancellery Austria and
* Graz University of Technology
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package at.gv.egiz.bku.local.conf;

import iaik.security.ecc.provider.ECCProvider;
import iaik.xml.crypto.XSecProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.security.KeyStore;
import java.security.Provider;
import java.security.Security;
import java.security.cert.CertStore;
import java.security.cert.CertificateFactory;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import javax.net.ssl.CertPathTrustManagerParameters;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.ManagerFactoryParameters;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import at.gv.egiz.bku.slcommands.impl.xsect.STALProvider;
import at.gv.egiz.smcc.SWCard;
import at.gv.egiz.smcc.util.SMCCHelper;

public class Configurator {
  private Log log = LogFactory.getLog(Configurator.class);
  private XMLConfiguration baseConfig;
  private XMLConfiguration specialConfig;
  private boolean autoSave = false;

  public Configurator() {
    super();
    init();
    configure();
  }

  private void init() {
    log.debug("Initializing configuration");

    baseConfig = new XMLConfiguration();
    try {
      baseConfig.load(getClass().getClassLoader().getResourceAsStream(
          "./at/gv/egiz/bku/local/baseconfig.xml"));
      log.debug("Successfully loaded base configuration");
    } catch (ConfigurationException e) {
      log.error("Cannot load base configuration", e);
    }
    autoSave = baseConfig.getBoolean("OverrideConfigurationFile[@autosave]");
    try {
      specialConfig = new XMLConfiguration();
      specialConfig.setFileName(baseConfig
          .getString("OverrideConfigurationFile"));
      specialConfig.load();
    } catch (Exception e) {
      log.debug("Cannot get special configuration at: "
          + baseConfig.getString("OverrideConfigurationFile") + ": " + e);
      log.debug("Creating new special configuration");
      try {
        specialConfig = new XMLConfiguration(baseConfig);
        specialConfig.setFileName(baseConfig
            .getString("OverrideConfigurationFile"));
        specialConfig.save();
      } catch (ConfigurationException e1) {
        log.error("Cannot load defaults " + e1);
      }
    }
    specialConfig.setReloadingStrategy(new FileChangedReloadingStrategy());
    specialConfig.setAutoSave(autoSave);
  }

  protected void configUrlConnections() {
    HttpsURLConnection.setFollowRedirects(false);
    HttpURLConnection.setFollowRedirects(false);
  }

  protected KeyStore loadKeyStore(String fileName, String type, String password) {
    KeyStore ks = null;
    try {
      ks = KeyStore.getInstance(type);
      InputStream is = new FileInputStream(fileName);
      if (is == null) {
        log.warn("Cannot load keystore from: " + fileName);
      }
      ks.load(is, password.toCharArray());
      for (Enumeration<String> alias = ks.aliases(); alias.hasMoreElements();) {
        log.debug("Found keystore alias: " + alias.nextElement());
      }
    } catch (Exception e) {
      log.error("Cannot config keystore", e);
      return null;
    }
    return ks;
  }

  protected void configSSL() {
    String trustStoreName = specialConfig.getString("SSL.trustStoreFile");
    String trustStoreType = specialConfig.getString("SSL.trustStoreType");
    String trustStorePass = specialConfig.getString("SSL.trustStorePass");
    String certStoreDirectory = specialConfig
        .getString("SSL.certStoreDirectory");
    String keyStoreName = specialConfig.getString("SSL.keyStoreFile");
    String keyStoreType = specialConfig.getString("SSL.keyStoreType");
    String keyStorePass = specialConfig.getString("SSL.keyStorePass");

    String caIncludeDir = specialConfig.getString("SSL.caIncludeDirectory");

    KeyStore trustStore = loadKeyStore(trustStoreName, trustStoreType,
        trustStorePass);
    KeyStore keyStore = null;
    if (keyStoreName != null) {
      keyStore = loadKeyStore(keyStoreName, keyStoreType, keyStorePass);
    }

    try {
      PKIXBuilderParameters pkixParams = new PKIXBuilderParameters(trustStore,
          new X509CertSelector());

      if (certStoreDirectory != null) {
        File dir = new File(certStoreDirectory);
        if (dir.isDirectory()) {
          List<X509Certificate> certCollection = new LinkedList<X509Certificate>();
          CertificateFactory cf = CertificateFactory.getInstance("X.509");
          for (File f : dir.listFiles()) {
            log.debug("adding " + f.getName());
            certCollection.add((X509Certificate) cf
                .generateCertificate(new FileInputStream(f)));
          }
          CollectionCertStoreParameters csp = new CollectionCertStoreParameters(
              certCollection);
          CertStore cs = CertStore.getInstance("Collection", csp);
          pkixParams.addCertStore(cs);
          log.debug("Added collection certstore");
        } else {
          log.error("CertstoreDirectory " + certStoreDirectory
              + " is not a directory");
        }
      }

      if (caIncludeDir != null) {
        File dir = new File(caIncludeDir);
        if (dir.exists() && dir.isDirectory()) {
          CertificateFactory cf = CertificateFactory.getInstance("X.509");
          try {
            for (File f : dir.listFiles()) {
              FileInputStream fis = new FileInputStream(f);
              X509Certificate cert = (X509Certificate) cf
                  .generateCertificate(fis);
              fis.close();
              log.debug("Adding trusted cert " + cert.getSubjectDN());
              trustStore.setCertificateEntry(cert.getSubjectDN().getName(),
                  cert);
              f.delete();
            }
          } finally {
            trustStore.store(new FileOutputStream(trustStoreName),
                trustStorePass.toCharArray());
          }
        }
      }

      pkixParams.setRevocationEnabled(specialConfig
          .getBoolean("SSL.revocation"));
      if (specialConfig.getBoolean("SSL.revocation")) {
        System.setProperty("com.sun.security.enableCRLDP ", "true");
        Security.setProperty("ocsp.enable", "true");
      }
      System.setProperty("com.sun.security.enableAIAcaIssuers", "true");
      log.debug("Setting revocation check to: "
          + pkixParams.isRevocationEnabled());
      ManagerFactoryParameters trustParams = new CertPathTrustManagerParameters(
          pkixParams);
      TrustManagerFactory trustFab = TrustManagerFactory.getInstance("PKIX");
      trustFab.init(trustParams);

      KeyManager[] km = null;
      SSLContext sslCtx = SSLContext.getInstance(specialConfig
          .getString("SSL.sslProtocol"));
      if (keyStore != null) {
        KeyManagerFactory keyFab = KeyManagerFactory.getInstance("SunX509");
        keyFab.init(keyStore, keyStorePass.toCharArray());
        km = keyFab.getKeyManagers();
      }
      sslCtx.init(km, trustFab.getTrustManagers(), null);
      HttpsURLConnection.setDefaultSSLSocketFactory(sslCtx.getSocketFactory());
      log.info("Successfully configured ssl");
    } catch (Exception e) {
      log.debug("Cannot init ssl", e);
    }
  }

  protected void configureProviders() {
    log.debug("Registering security providers");
    ECCProvider.addAsProvider(false);
    Security.addProvider(new STALProvider());
    XSecProvider.addAsProvider(false);
    StringBuffer sb = new StringBuffer();
    sb.append("Following providers are now registered: ");
    int i = 1;
    for (Provider prov : Security.getProviders()) {
      sb.append((i++) + ". : " + prov);
    }
    log.debug("Configured provider" + sb.toString());
  }

  protected void configureBKU() {
    if (specialConfig.containsKey("BKU.useSWCard")) {
      boolean useSWCard = specialConfig.getBoolean("BKU.useSWCard");
      log.info("Setting SW Card to: "+useSWCard);
      SMCCHelper.setUseSWCard(useSWCard);
    }
    if (specialConfig.containsKey("BKU.SWCardDirectory")) {
     //SWCard.
    }
  }

  public void configure() {
    configureProviders();
    configSSL();
    configUrlConnections();
    configureBKU();

  }

  public void checkUpdate() {
    if (specialConfig.getReloadingStrategy().reloadingRequired()) {
      log.info("Reloading configuration: " + specialConfig.getFileName());
      specialConfig.setAutoSave(false);
      specialConfig.clear();
      try {
        specialConfig.load();
      } catch (ConfigurationException e) {
        log.fatal(e);
      }
      specialConfig.setAutoSave(specialConfig
          .getBoolean("OverrideConfigurationFile[@autosave]"));
      configure();
      specialConfig.getReloadingStrategy().reloadingPerformed();
    }
  }

}
