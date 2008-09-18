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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.cert.CertPathBuilder;
import java.security.cert.CertStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.LDAPCertStoreParameters;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.PKIXCertPathBuilderResult;
import java.security.cert.TrustAnchor;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.net.ssl.CertPathTrustManagerParameters;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.ManagerFactoryParameters;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import at.gv.egiz.bku.binding.DataUrl;
import at.gv.egiz.bku.binding.DataUrlConnection;
import at.gv.egiz.bku.slexceptions.SLRuntimeException;

public class SpringConfigurator extends Configurator implements
    ResourceLoaderAware {

  private final static Log log = LogFactory.getLog(SpringConfigurator.class);

  private ResourceLoader resourceLoader;

  public SpringConfigurator() {
    File configDir = new File(System.getProperty("user.home") + "/.bku/conf");
    if (configDir.exists()) {
      log.debug("Found existing config directory: " + configDir);
    } else {
      log.info("Config dir not existing, creating new");
      if (!configDir.mkdirs()) {
        log.error("Cannot create directory: " + configDir);
      }
    }
  }

  public void setResource(Resource resource) {
    log.debug("Loading config from: " + resource);
    if (resource != null) {
      Properties props = new Properties();
      try {
        props.load(resource.getInputStream());
        super.setConfiguration(props);
      } catch (IOException e) {
        log.error("Cannot load config", e);
      }
    } else {
      log.warn("Cannot load properties, resource: " + resource);
    }
  }

  public void configureVersion() {
    Properties p = new Properties();
    try {
      p.load(resourceLoader.getResource("META-INF/MANIFEST.MF")
          .getInputStream());
      String version = p.getProperty("Implementation-Build");
      properties.setProperty(DataUrlConnection.USER_AGENT_PROPERTY_KEY,
          "citizen-card-environment/1.2 MOCCA " + version);
      DataUrl.setConfiguration(properties);
      log.debug("Setting user agent to: "
          + properties.getProperty(DataUrlConnection.USER_AGENT_PROPERTY_KEY));
    } catch (IOException e) {
      log.error(e);
    }
  }

  public void configure() {
    super.configure();
    configureSSL();
    configureVersion();
    configureNetwork();
  }

  public void configureNetwork() {
    
  }

  private Set<TrustAnchor> getCACerts() throws IOException,
      CertificateException {
    Set<TrustAnchor> caCerts = new HashSet<TrustAnchor>();
    String caDirectory = getProperty("SSL.caDirectory");
    if (caDirectory != null) {
      Resource caDirRes = resourceLoader.getResource(caDirectory);
      File caDir = caDirRes.getFile();
      if (!caDir.isDirectory()) {
        log.error("Expecting directory as SSL.caDirectory parameter");
        throw new SLRuntimeException(
            "Expecting directory as SSL.caDirectory parameter");
      }
      CertificateFactory cf = CertificateFactory.getInstance("X.509");
      for (File f : caDir.listFiles()) {
        try {
          FileInputStream fis = new FileInputStream(f);
          X509Certificate cert = (X509Certificate) cf.generateCertificate(fis);
          fis.close();
          log.debug("Adding trusted cert " + cert.getSubjectDN());
          caCerts.add(new TrustAnchor(cert, null));
        } catch (Exception e) {
          log.error("Cannot add trusted ca", e);
        }
      }
      return caCerts;

    } else {
      log.warn("No CA certificates configured");
    }
    return null;
  }

  private List<CertStore> getCertstore() throws IOException,
      CertificateException, InvalidAlgorithmParameterException,
      NoSuchAlgorithmException {
    List<CertStore> resultList = new ArrayList<CertStore>();
    String certDirectory = getProperty("SSL.certDirectory");
    if (certDirectory != null) {
      Resource certDirRes = resourceLoader.getResource(certDirectory);

      File certDir = certDirRes.getFile();
      if (!certDir.isDirectory()) {
        log.error("Expecting directory as SSL.certDirectory parameter");
        throw new SLRuntimeException(
            "Expecting directory as SSL.certDirectory parameter");
      }
      List<X509Certificate> certCollection = new LinkedList<X509Certificate>();
      CertificateFactory cf = CertificateFactory.getInstance("X.509");
      for (File f : certDir.listFiles()) {
        try {
          FileInputStream fis = new FileInputStream(f);
          X509Certificate cert = (X509Certificate) cf.generateCertificate(fis);
          certCollection.add(cert);
          fis.close();
          log
              .trace("Added following cert to certstore: "
                  + cert.getSubjectDN());
        } catch (Exception ex) {
          log.error("Cannot add certificate", ex);
        }
      }
      CollectionCertStoreParameters csp = new CollectionCertStoreParameters(
          certCollection);
      resultList.add(CertStore.getInstance("Collection", csp));
      log.info("Added collection certstore");
    } else {
      log.warn("No certstore directory configured");
    }
    String ldapHost = getProperty("SSL.ldapServer");
    if ((ldapHost != null) && (!"".equals(ldapHost))) {
      String ldapPortString = getProperty("SSL.ldapPort");
      int ldapPort = 389;
      if (ldapPortString != null) {
        try {
          ldapPort = Integer.parseInt(ldapPortString);
        } catch (NumberFormatException nfe) {
          log.error("Invalid ldap port, using default 389");
        }
      } else {
        log.warn("ldap port not specified, using default 389");
      }
      LDAPCertStoreParameters ldapParams = new LDAPCertStoreParameters(
          ldapHost, ldapPort);
      resultList.add(CertStore.getInstance("LDAP", ldapParams));
      log.info("Added LDAP certstore");
    }
    return resultList;
  }

  public void configureSSL() {
    Set<TrustAnchor> caCerts = null;
    try {
      caCerts = getCACerts();
    } catch (Exception e1) {
      log.error("Cannot load CA certificates", e1);
    }
    List<CertStore> certStoreList = null;
    try {
      certStoreList = getCertstore();
    } catch (Exception e1) {
      log.error("Cannot load certstore certificates", e1);
    }
    String aia = getProperty("SSL.useAIA");
    if ((aia == null) || (aia.equals(""))) {
      System.setProperty("com.sun.security.enableAIAcaIssuers", "true");
    } else {
      System.setProperty("com.sun.security.enableAIAcaIssuers", aia);
    }
    String lifetime = getProperty("SSL.cache.lifetime");
    if ((lifetime == null) || (lifetime.equals(""))) {
      System.setProperty("sun.security.certpath.ldap.cache.lifetime", "0");
    } else {
      System.setProperty("sun.security.certpath.ldap.cache.lifetime", lifetime);
    }
    X509CertSelector selector = new X509CertSelector();
    PKIXBuilderParameters pkixParams;
    try {
      pkixParams = new PKIXBuilderParameters(caCerts, selector);
      if ((getProperty("SSL.doRevocationChecking") != null)
          && (Boolean.valueOf(getProperty("SSL.doRevocationChecking")))) {
        log.info("Enable revocation checking");
        System.setProperty("com.sun.security.enableCRLDP", "true");
        Security.setProperty("ocsp.enable", "true");
      } else {
        log.warn("Revocation checking disabled");
      }
      for (CertStore cs : certStoreList) {
        pkixParams.addCertStore(cs);
      }
      ManagerFactoryParameters trustParams = new CertPathTrustManagerParameters(
          pkixParams);
      TrustManagerFactory trustFab;
      trustFab = TrustManagerFactory.getInstance("PKIX");
      trustFab.init(trustParams);
      KeyManager[] km = null;
      SSLContext sslCtx = SSLContext
          .getInstance(getProperty("SSL.sslProtocol"));
      sslCtx.init(km, trustFab.getTrustManagers(), null);
      // sslCtx.init(km, new TrustManager[] { new MyTrustManager(caCerts,
      // certStoreList) }, null);
      HttpsURLConnection.setDefaultSSLSocketFactory(sslCtx.getSocketFactory());
    } catch (Exception e) {
      log.error("Cannot configure SSL", e);
    }
  }

  @Override
  public void setResourceLoader(ResourceLoader loader) {
    this.resourceLoader = loader;
  }
}

class MyTrustManager implements X509TrustManager {
  private static Log log = LogFactory.getLog(MyTrustManager.class);
  private Set<TrustAnchor> caCerts;
  private List<CertStore> certStoreList;
  private X509Certificate[] trustedCerts;

  public MyTrustManager(Set<TrustAnchor> caCerts, List<CertStore> cs) {
    this.caCerts = caCerts;
    this.certStoreList = cs;
    trustedCerts = new X509Certificate[caCerts.size()];
    int i = 0;
    for (Iterator<TrustAnchor> it = caCerts.iterator(); it.hasNext();) {
      TrustAnchor ta = it.next();
      trustedCerts[i++] = ta.getTrustedCert();
    }

  }

  @Override
  public void checkClientTrusted(X509Certificate[] arg0, String arg1)
      throws CertificateException {
    log.error("Did not expect this method to get called");
    throw new CertificateException("Method not implemented");
  }

  @Override
  public void checkServerTrusted(X509Certificate[] certs, String arg1)
      throws CertificateException {
    try {
      log.debug("Checking server certificate: " + certs[0].getSubjectDN());
      CertPathBuilder pathBuilder = CertPathBuilder.getInstance("PKIX");
      X509CertSelector selector = new X509CertSelector();
      selector.setCertificate(certs[0]);
      PKIXBuilderParameters pkixParams;
      pkixParams = new PKIXBuilderParameters(caCerts, selector);
      pkixParams.setRevocationEnabled(true); // FIXME
      for (CertStore cs : certStoreList) {
        pkixParams.addCertStore(cs);
      }
      PKIXCertPathBuilderResult result = (PKIXCertPathBuilderResult) pathBuilder
          .build(pkixParams);
      if (log.isTraceEnabled()) {
        StringBuffer sb = new StringBuffer();
        for (Certificate cert : result.getCertPath().getCertificates()) {
          sb.append(((X509Certificate) cert).getSubjectDN());
          sb.append("->");
        }
        sb.append("End");
        log.trace(sb);
      }
    } catch (Exception e) {
      throw new CertificateException(e);
    }
  }

  @Override
  public X509Certificate[] getAcceptedIssuers() {
    return trustedCerts;
  }

}