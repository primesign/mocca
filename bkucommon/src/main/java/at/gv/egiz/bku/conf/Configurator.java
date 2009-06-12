package at.gv.egiz.bku.conf;

import iaik.security.ecc.provider.ECCProvider;
import iaik.security.provider.IAIK;
import iaik.xml.crypto.XSecProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Security;
import java.security.Provider.Service;
import java.security.cert.CertStore;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.LDAPCertStoreParameters;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import at.gv.egiz.bku.binding.DataUrl;
import at.gv.egiz.bku.slcommands.impl.xsect.DataObject;
import at.gv.egiz.bku.slcommands.impl.xsect.STALProvider;
import at.gv.egiz.bku.slexceptions.SLRuntimeException;
import at.gv.egiz.bku.utils.urldereferencer.URLDereferencer;
import javax.net.ssl.SSLSocketFactory;

public abstract class Configurator {

  private Log log = LogFactory.getLog(Configurator.class);

  public final static String USERAGENT_CONFIG_P = "UserAgent";
  public static final String DATAURLCONNECTION_CONFIG_P = "DataURLConnectionImplClass";

  public static final String USERAGENT_DEFAULT = "citizen-card-environment/1.2 MOCCA/UNKNOWN";
  public static final String USERAGENT_BASE = "citizen-card-environment/1.2 MOCCA/";

  public static final String SIGNATURE_LAYOUT = "SignatureLayout";

  protected Properties properties;

  protected CertValidator certValidator;
  protected String signaturLayoutVersion;

  protected Configurator() {
  }

  protected abstract File getCertDir();

  protected abstract File getCADir();

  protected abstract InputStream getManifest();

  private X509Certificate[] getCACerts() throws IOException,
      CertificateException {
    List<X509Certificate> caCerts = new ArrayList<X509Certificate>();
    File caDir = getCADir();
    if (caDir != null) {
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
          caCerts.add(cert);
        } catch (Exception e) {
          log.error("Cannot add trusted ca", e);
        }
      }
      return caCerts.toArray(new X509Certificate[caCerts.size()]);
    } else {
      log.warn("No CA certificates configured");
    }
    return null;
  }

  protected List<CertStore> getCertstore() throws IOException,
      CertificateException, InvalidAlgorithmParameterException,
      NoSuchAlgorithmException {
    List<CertStore> resultList = new ArrayList<CertStore>();
    File certDir = getCertDir();
    if (certDir != null) {
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

  protected void configUrlConnections() {
    HttpsURLConnection.setFollowRedirects(false);
    HttpURLConnection.setFollowRedirects(false);
  }

  protected void configureProviders() {
    log.debug("Registering security providers");
    Security.insertProviderAt(new IAIK(), 1);
    Security.insertProviderAt(new ECCProvider(false), 2);

    // registering STALProvider as delegation provider for XSECT
    STALProvider stalProvider = new STALProvider();
    Set<Service> services = stalProvider.getServices();
    StringBuilder sb = new StringBuilder();
    for (Service service : services) {
      String algorithm = service.getType() + "." + service.getAlgorithm();
      XSecProvider.setDelegationProvider(algorithm, stalProvider.getName());
      sb.append("\n" + algorithm);
    }
    log
        .debug("Registered STALProvider as XSecProvider delegation provider for the following services : "
            + sb.toString());

    Security.addProvider(stalProvider);
    XSecProvider.addAsProvider(false);
    sb = new StringBuilder();
    sb.append("Registered providers: ");
    int i = 1;
    for (Provider prov : Security.getProviders()) {
      sb.append((i++) + ". : " + prov);
    }
    log.debug(sb.toString());
  }

  protected void configViewer() {
    String bv = properties.getProperty("ValidateHashDataInputs");
    if (bv != null) {
      DataObject.enableHashDataInputValidation(Boolean.parseBoolean(bv));
    } else {
      log.warn("ValidateHashDataInputs not set, falling back to default");
    }
  }

  public void configureSingatureLayoutVersion() {
    if (properties.get(SIGNATURE_LAYOUT) == null) {
      try {
        String classContainer = Configurator.class.getProtectionDomain()
            .getCodeSource().getLocation().toString();
        URL manifestUrl = new URL("jar:" + classContainer
            + "!/META-INF/MANIFEST.MF");
        Manifest manifest = new Manifest(manifestUrl.openStream());
        Attributes att = manifest.getMainAttributes();
        String layout = null;
        if (att != null) {
          layout = att.getValue(SIGNATURE_LAYOUT);
        }
        if (layout != null) {
          log.info("setting SignatureLayout header to " + layout);
          properties.put(SIGNATURE_LAYOUT, layout);
        } else {
          log.warn("no SignatureLayout version defined");
        }
      } catch (Exception ex) {
        log.warn("Cannot read manifest", ex);
      }
    }
  }

  public void configureNetwork() {
    String proxy = getProperty("HTTPProxyHost");
    String portString = getProperty("HTTPProxyPort");
    if ((proxy == null) || (proxy.equals(""))) {
      log.info("No proxy configured");
    } else {
      log.info("Setting proxy to: " + proxy + ":" + portString);
      System.setProperty("proxyHost", proxy);
      System.setProperty("proxyPort", portString);
    }
    String timeout = getProperty("DefaultSocketTimeout");
    if ((timeout != null) && (!timeout.equals(""))) {
      System.setProperty("sun.net.client.defaultConnectTimeout", timeout);
    }
  }

  public void configureVersion() {
    if (properties.getProperty(USERAGENT_CONFIG_P) == null) {
      Properties p = new Properties();
      try {
        InputStream is = getManifest();
        if (is != null) {
          p.load(getManifest());
          String version = p.getProperty("Implementation-Build");
          if (version == null) {
            version="UNKNOWN";
          }
          properties.setProperty(USERAGENT_CONFIG_P, USERAGENT_BASE + version);
          log.debug("Setting user agent to: "
              + properties.getProperty(USERAGENT_CONFIG_P));
        } else {
          log.warn("Cannot read manifest");
          properties.setProperty(USERAGENT_CONFIG_P, USERAGENT_DEFAULT);
        }
      } catch (IOException e) {
        log.error(e);
      }
    } else {
      log.info("using configured user agent " + properties.getProperty(USERAGENT_CONFIG_P));
    }
  }

  /**
   * TODO cleanup configuration (read MANIFEST, DataURLconfig,...)
   */
  public void configure() {
    configureProviders();
    configUrlConnections();
    configViewer();
    configureSSL();
    configureVersion();
    configureSingatureLayoutVersion();
    configureNetwork();
    //after configureVersion() and configureSignatureLayoutVersion()
    DataUrl.setConfiguration(properties);
  }

  public void setConfiguration(Properties props) {
    this.properties = props;
  }

  public String getProperty(String key) {
    if (properties != null) {
      return properties.getProperty(key);
    }
    return null;
  }

  public void configureSSL() {
    X509Certificate[] caCerts = null;
    try {
      caCerts = getCACerts();
    } catch (Exception e1) {
      log.error("Cannot load CA certificates", e1);
    }
    String disableAll = getProperty("SSL.disableAllChecks");
    String disableHostnameVerification = getProperty("SSL.disableHostnameVerification");
    try {
      KeyManager[] km = null;
      SSLContext sslCtx = SSLContext
          .getInstance(getProperty("SSL.sslProtocol"));
      if ((disableAll != null) && (Boolean.parseBoolean(disableAll))) {
        log.warn("--------------------------------------");
        log.warn(" Disabling SSL Certificate Validation ");
        log.warn("--------------------------------------");

        sslCtx.init(km,
            new TrustManager[] { new MyAlwaysTrustManager(caCerts) }, null);
      } else {
        MyPKITrustManager pkixTM = new MyPKITrustManager(certValidator,
            getCertDir(), getCADir(), caCerts);
        sslCtx.init(km, new TrustManager[] { pkixTM }, null);
      }
      DataUrl.setSSLSocketFactory(sslCtx.getSocketFactory());
      URLDereferencer.getInstance().setSSLSocketFactory(
          sslCtx.getSocketFactory());
    } catch (Exception e) {
      log.error("Cannot configure SSL", e);
    }
    if ((disableAll != null && Boolean.parseBoolean(disableAll))
        || (disableHostnameVerification != null && Boolean
            .parseBoolean(disableHostnameVerification))) {
      log.warn("---------------------------------");
      log.warn(" Disabling Hostname Verification ");
      log.warn("---------------------------------");
      DataUrl.setHostNameVerifier(new HostnameVerifier() {
        @Override
        public boolean verify(String hostname, SSLSession session) {
          return true;
        }
      });
      URLDereferencer.getInstance().setHostnameVerifier(new HostnameVerifier() {
        @Override
        public boolean verify(String hostname, SSLSession session) {
          return true;
        }
      });
    }
  }

  public void setCertValidator(CertValidator certValidator) {
    this.certValidator = certValidator;
  }

  private static class MyPKITrustManager implements X509TrustManager {
    private static Log log = LogFactory.getLog(MyPKITrustManager.class);

    private CertValidator certValidator;
    private X509Certificate[] trustedCerts;

    public MyPKITrustManager(CertValidator cv, File certStore, File trustStore,
        X509Certificate[] trustedCerts) {
      certValidator = cv;
      certValidator.init(certStore, trustStore);
      this.trustedCerts = trustedCerts;
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType)
        throws CertificateException {
      log.error("Did not expect this method to get called");
      throw new CertificateException("Method not implemented");
    }

    private static iaik.x509.X509Certificate[] convertCerts(
        X509Certificate[] certs) throws GeneralSecurityException {
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

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType)
        throws CertificateException {
      try {
        boolean valid = certValidator.isCertificateValid(Thread.currentThread()
            .getName(), convertCerts(chain));
        if (!valid) {
          throw new CertificateException("Certificate not valid");
        }
      } catch (GeneralSecurityException e) {
        throw new CertificateException(e);
      }
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
      return trustedCerts;
    }
  }

  private static class MyAlwaysTrustManager implements X509TrustManager {
    private static Log log = LogFactory.getLog(MyAlwaysTrustManager.class);
    private X509Certificate[] trustedCerts;

    public MyAlwaysTrustManager(X509Certificate[] trustedCerts) {
      this.trustedCerts = trustedCerts;
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
      log.warn("-------------------------------------");
      log.warn("SSL Certificate Validation Disabled !");
      log.warn("-------------------------------------");
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
      return trustedCerts;
    }
  }
}
