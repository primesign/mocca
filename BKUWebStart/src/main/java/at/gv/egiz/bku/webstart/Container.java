package at.gv.egiz.bku.webstart;

import at.gv.egiz.bku.utils.StreamUtil;
import java.awt.AWTPermission;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilePermission;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.ReflectPermission;
import java.net.NetPermission;
import java.net.SocketPermission;
import java.security.Permissions;
import java.security.SecurityPermission;
import java.util.PropertyPermission;
import javax.smartcardio.CardPermission;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.security.SslSocketConnector;
import org.mortbay.jetty.webapp.WebAppContext;
import org.mortbay.thread.QueuedThreadPool;

public class Container {

  public static final String HTTP_PORT_PROPERTY = "mocca.http.port";
  public static final String HTTPS_PORT_PROPERTY = "mocca.http.port";

  private static Log log = LogFactory.getLog(Container.class);
  static {
    if (log.isDebugEnabled()) {
      //Jetty log INFO and WARN, include ignored exceptions
      //jetty logging may be further restricted by setting level in log4j.properties
      System.setProperty("VERBOSE", "true");
      //do not set Jetty DEBUG logging, produces loads of output
      //System.setProperty("DEBUG", "true");
    }
  }

  private Server server;

  public void init() throws IOException {
//    System.setProperty("DEBUG", "true");
    server = new Server();
    QueuedThreadPool qtp = new QueuedThreadPool();
    qtp.setMaxThreads(5);
    qtp.setMinThreads(2);
    qtp.setLowThreads(0);
    server.setThreadPool(qtp);
    server.setStopAtShutdown(true);
    server.setGracefulShutdown(3000);

    SelectChannelConnector connector = new SelectChannelConnector();
    connector.setPort(Integer.getInteger(HTTP_PORT_PROPERTY, 3495).intValue());
    connector.setAcceptors(1);
    connector.setConfidentialPort(Integer.getInteger(HTTPS_PORT_PROPERTY, 3496).intValue());
    connector.setHost("127.0.0.1");

    SslSocketConnector sslConnector = new SslSocketConnector();
    sslConnector.setPort(Integer.getInteger(HTTPS_PORT_PROPERTY, 3496).intValue());
    sslConnector.setAcceptors(1);
    sslConnector.setHost("127.0.0.1");
    File configDir = new File(System.getProperty("user.home") + "/" + Configurator.CONFIG_DIR);
    File keystoreFile = new File(configDir, Configurator.KEYSTORE_FILE);
    if (!keystoreFile.canRead()) {
      log.error("MOCCA keystore file not readable: " + keystoreFile.getAbsolutePath());
      throw new FileNotFoundException("MOCCA keystore file not readable: " + keystoreFile.getAbsolutePath());
    }
    log.debug("loading MOCCA keystore from " + keystoreFile.getAbsolutePath());
    sslConnector.setKeystore(keystoreFile.getAbsolutePath());
    File passwdFile = new File(configDir, Configurator.PASSWD_FILE);
    BufferedReader reader = new BufferedReader(new FileReader(passwdFile));
    String pwd;
    while ((pwd = reader.readLine()) != null) {
      sslConnector.setPassword(pwd);
      sslConnector.setKeyPassword(pwd);
    }
    reader.close();
    
    //avoid jetty's ClassCastException: iaik.security.ecc.ecdsa.ECPublicKey cannot be cast to java.security.interfaces.ECPublicKey
    String[] RFC4492CipherSuites = new String[] {
      "TLS_ECDH_ECDSA_WITH_NULL_SHA",
     "TLS_ECDH_ECDSA_WITH_RC4_128_SHA",
     "TLS_ECDH_ECDSA_WITH_3DES_EDE_CBC_SHA",
     "TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA",
     "TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA",

     "TLS_ECDHE_ECDSA_WITH_NULL_SHA",
     "TLS_ECDHE_ECDSA_WITH_RC4_128_SHA",
     "TLS_ECDHE_ECDSA_WITH_3DES_EDE_CBC_SHA",
     "TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA",
     "TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA",

     "TLS_ECDH_RSA_WITH_NULL_SHA",
     "TLS_ECDH_RSA_WITH_RC4_128_SHA",
     "TLS_ECDH_RSA_WITH_3DES_EDE_CBC_SHA",
     "TLS_ECDH_RSA_WITH_AES_128_CBC_SHA",
     "TLS_ECDH_RSA_WITH_AES_256_CBC_SHA",

     "TLS_ECDHE_RSA_WITH_NULL_SHA",
     "TLS_ECDHE_RSA_WITH_RC4_128_SHA",
     "TLS_ECDHE_RSA_WITH_3DES_EDE_CBC_SHA",
     "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA",
     "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA",

     "TLS_ECDH_anon_WITH_NULL_SHA",
     "TLS_ECDH_anon_WITH_RC4_128_SHA",
     "TLS_ECDH_anon_WITH_3DES_EDE_CBC_SHA",
     "TLS_ECDH_anon_WITH_AES_128_CBC_SHA",
     "TLS_ECDH_anon_WITH_AES_256_CBC_SHA"
    };

    sslConnector.setExcludeCipherSuites(RFC4492CipherSuites);

    server.setConnectors(new Connector[] { connector, sslConnector });
    
    WebAppContext webapp = new WebAppContext();
    webapp.setLogUrlOnStart(true);
    webapp.setContextPath("/");
    webapp.setExtractWAR(true); 
    webapp.setParentLoaderPriority(false);

    webapp.setWar(copyWebapp(webapp.getTempDirectory()));
    webapp.setPermissions(getPermissions(webapp.getTempDirectory()));
    
    server.setHandler(webapp);
    server.setGracefulShutdown(1000*3);
  }

  private String copyWebapp(File webappDir) throws IOException {
    File webapp = new File(webappDir, "BKULocal.war");
    log.debug("copying BKULocal classpath resource to " + webapp);
    InputStream is = getClass().getClassLoader().getResourceAsStream("BKULocal.war");
    OutputStream os = new BufferedOutputStream(new FileOutputStream(webapp));
    StreamUtil.copyStream(is, os);
    os.close();
    return webapp.getPath();
  }

  private Permissions getPermissions(File webappDir) {
    Permissions perms = new Permissions();

    // jetty-webstart (spring?)
    perms.add(new RuntimePermission("getClassLoader"));

    // standard permissions
    perms.add(new PropertyPermission("*", "read"));
    perms.add(new RuntimePermission("accessDeclaredMembers"));
    perms.add(new RuntimePermission("accessClassInPackage.*"));
    perms.add(new RuntimePermission("defineClassInPackage.*"));
    perms.add(new RuntimePermission("setFactory"));
    perms.add(new RuntimePermission("getProtectionDomain"));
    perms.add(new RuntimePermission("modifyThread"));
    perms.add(new RuntimePermission("modifyThreadGroup"));
    perms.add(new RuntimePermission("setFactory"));
    perms.add(new ReflectPermission("suppressAccessChecks"));

    // MOCCA specific
    perms.add(new SocketPermission("*", "connect,resolve"));
    perms.add(new NetPermission("specifyStreamHandler"));
    perms.add(new SecurityPermission("insertProvider.*"));
    perms.add(new SecurityPermission("putProviderProperty.*"));
    perms.add(new SecurityPermission("removeProvider.*"));
    perms.add(new CardPermission("*", "*"));
    perms.add(new AWTPermission("*"));

    perms.add(new FilePermission(webappDir.getAbsolutePath() + "/-", "read"));
    perms.add(new FilePermission(new File(System.getProperty("java.home") + "/lib/xalan.properties").getAbsolutePath(), "read"));
    perms.add(new FilePermission(new File(System.getProperty("java.home") + "/lib/xerces.properties").getAbsolutePath(), "read"));
    perms.add(new FilePermission(new File(System.getProperty("user.home")).getAbsolutePath(), "read, write"));
    perms.add(new FilePermission(new File(System.getProperty("user.home") + "/-").getAbsolutePath(), "read, write"));
    perms.add(new FilePermission(new File(System.getProperty("user.home") + "/.mocca/logs/*").getAbsolutePath(), "read, write,delete"));
    perms.add(new FilePermission(new File(System.getProperty("user.home") + "/.mocca/certs/-").getAbsolutePath(), "read, write,delete"));

    //TODO
    log.trace("granting file read/write permission to MOCCA local");
    perms.add(new FilePermission("<<ALL_FILES>>", "read, write"));

    return perms;
  }

  public void start() throws Exception {
    server.start();
  }

  public boolean isRunning() {
    return server.isRunning();
  }

  public void stop() throws Exception {
    server.stop();
  }

  public void destroy() {
    server.destroy();
  }

  public void join() throws InterruptedException {
    server.join();
  }
}