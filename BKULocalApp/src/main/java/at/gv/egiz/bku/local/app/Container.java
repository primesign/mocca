package at.gv.egiz.bku.local.app;

import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.DefaultHandler;
import org.mortbay.jetty.handler.HandlerCollection;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.security.SslSocketConnector;
import org.mortbay.jetty.webapp.WebAppClassLoader;
import org.mortbay.jetty.webapp.WebAppContext;
import org.mortbay.thread.QueuedThreadPool;

public class Container {

  private static Log log = LogFactory.getLog(Container.class);

  private Server server;

  public Container() {
  }

  public void init() {
    Thread.currentThread().setContextClassLoader(ClassLoader.getSystemClassLoader());
   log.debug("-----------------> "+ClassLoader.getSystemClassLoader());
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
    
    File tmpDir = new File(System.getProperty("user.home") + "/.mocca/tmp");
    // tmpDir.mkdirs();
    // webappcontext.setTempDirectory(tmpDir);
    try {
      File f = new File(System.getProperty("user.home")
          + "/.mocca/war/mocca.war");
      log.debug("Deploying war: " + f.getCanonicalPath());
      if (!f.exists()) {
        log.error("WAR file does not exist, cannot run MOCCA");
      }
      webappcontext.setWar(f.getParent());
    } catch (IOException e) {
      log.error(e);
    }
    handlers.setHandlers(new Handler[] { webappcontext, new DefaultHandler() });

    server.setHandler(handlers);
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