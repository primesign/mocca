package at.gv.egiz.bku.webstart;

import at.gv.egiz.bku.utils.StreamUtil;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.DefaultHandler;
import org.mortbay.jetty.handler.HandlerCollection;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.security.SslSocketConnector;
import org.mortbay.jetty.webapp.WebAppContext;
import org.mortbay.thread.QueuedThreadPool;

public class Container {

  public static final String HTTP_PORT_PROPERTY = "mocca.http.port";
  public static final String HTTPS_PORT_PROPERTY = "mocca.http.port";

  private static Log log = LogFactory.getLog(Container.class);

  private Server server;

  public Container() {
  }

  public void init() throws IOException {
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
    File configDir = new File(System.getProperty("user.home") + "/" + BKULauncher.CONFIG_DIR);
    sslConnector.setKeystore(configDir.getPath() + "/" + BKULauncher.KEYSTORE_FILE);
    File passwdFile = new File(configDir, BKULauncher.PASSWD_FILE);
    BufferedReader reader = new BufferedReader(new FileReader(passwdFile));
    String pwd;
    while ((pwd = reader.readLine()) != null) {
      sslConnector.setPassword(pwd);
      sslConnector.setKeyPassword(pwd);
    }
    reader.close();

    server.setConnectors(new Connector[] { connector, sslConnector });
    
    WebAppContext webapp = new WebAppContext();
    webapp.setLogUrlOnStart(true);
    webapp.setContextPath("/");
    webapp.setExtractWAR(true); 
    webapp.setParentLoaderPriority(false); //true);

    webapp.setWar(copyWebapp(webapp.getTempDirectory())); //getClass().getClassLoader().getResource("BKULocalWar/").toString());

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