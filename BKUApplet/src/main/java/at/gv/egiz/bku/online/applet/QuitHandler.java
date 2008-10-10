package at.gv.egiz.bku.online.applet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import at.gv.egiz.bku.smccstal.SMCCSTALRequestHandler;
import at.gv.egiz.stal.QuitRequest;
import at.gv.egiz.stal.STALRequest;
import at.gv.egiz.stal.STALResponse;

public class QuitHandler extends MultiInstanceHandler {
  
  private static Log log = LogFactory.getLog(QuitHandler.class);
  
  private static QuitHandler instance = new QuitHandler();
  
  private QuitHandler() {
  }
  
  public static QuitHandler getInstance() {
    return instance;
  }

  @Override
  public STALResponse handleRequest(STALRequest request) {
    if (request instanceof QuitRequest) {
      log.info("Received QuitCommand");
      for (SMCCSTALRequestHandler handler : handlerList) {
        handler.handleRequest(request);
      }
    } else {
      log.error("Unexpected request to handle: " + request);
    }
    return null;
  }

}
