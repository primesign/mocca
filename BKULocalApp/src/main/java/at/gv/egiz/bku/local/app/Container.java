package at.gv.egiz.bku.local.app;

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
    connector.setPort(3495);
    connector.setAcceptors(1);
    connector.setConfidentialPort(3496);

    SslSocketConnector sslConnector = new SslSocketConnector();
    sslConnector.setPort(3496);
    sslConnector.setAcceptors(1);
    sslConnector.setKeystore(System.getProperty("user.home")
        + "/.mocca/conf/keystore/keystore.ks");
    sslConnector.setPassword("changeMe");
    sslConnector.setKeyPassword("changeMe");

    server.setConnectors(new Connector[] { connector, sslConnector });
    HandlerCollection handlers = new HandlerCollection();

    WebAppContext webappcontext = new WebAppContext();
    webappcontext.setContextPath("/");
    webappcontext.setExtractWAR(false);
    System.out.println(getClass().getClassLoader().getResource("log4j.properties"));
    webappcontext.setWar("BKULocal-1.0.4-SNAPSHOT.war");

    handlers.setHandlers(new Handler[] { webappcontext, new DefaultHandler() });

    server.setHandler(handlers);
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