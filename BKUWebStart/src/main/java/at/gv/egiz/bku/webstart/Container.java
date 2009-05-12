package at.gv.egiz.bku.webstart;

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

  public static final String HTTP_PORT = "mocca.http.port";
  public static final String HTTPS_PORT = "mocca.http.port";

  private static Log log = LogFactory.getLog(Container.class);

  private Server server;

  public Container() {
  }

  public void init() {
    server = new Server();
    QueuedThreadPool qtp = new QueuedThreadPool();
    qtp.setMaxThreads(5);
    qtp.setMinThreads(2);
    qtp.setLowThreads(0);
    server.setThreadPool(qtp);
    server.setStopAtShutdown(true);
    server.setGracefulShutdown(3000);

    SelectChannelConnector connector = new SelectChannelConnector();
    connector.setPort(Integer.getInteger(HTTP_PORT, 3495).intValue());
    connector.setAcceptors(1);
    connector.setConfidentialPort(Integer.getInteger(HTTPS_PORT, 3496).intValue());

    SslSocketConnector sslConnector = new SslSocketConnector();
    sslConnector.setPort(Integer.getInteger(HTTPS_PORT, 3496).intValue());
    sslConnector.setAcceptors(1);
    sslConnector.setKeystore(System.getProperty("user.home")
        + "/.mocca/conf/keystore/keystore.ks");
    sslConnector.setPassword("changeMe");
    sslConnector.setKeyPassword("changeMe");

    server.setConnectors(new Connector[] { connector, sslConnector });
    
//    HandlerCollection handlers = new HandlerCollection();
    WebAppContext webapp = new WebAppContext();
    webapp.setContextPath("/");
    webapp.setExtractWAR(true); //false
    webapp.setParentLoaderPriority(false); 

//    webappcontext.setWar("BKULocal-1.0.4-SNAPSHOT.war");
    webapp.setWar(getClass().getClassLoader().getResource("BKULocalWar/").toString());

//    handlers.setHandlers(new Handler[] { webappcontext, new DefaultHandler() });

    server.setHandler(webapp);
    server.setGracefulShutdown(1000*3);
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