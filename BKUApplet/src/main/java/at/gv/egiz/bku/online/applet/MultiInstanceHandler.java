package at.gv.egiz.bku.online.applet;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import at.gv.egiz.bku.gui.BKUGUIFacade;
import at.gv.egiz.bku.smccstal.SMCCSTALRequestHandler;
import at.gv.egiz.smcc.SignatureCard;
import at.gv.egiz.stal.QuitRequest;
import at.gv.egiz.stal.STALRequest;
import at.gv.egiz.stal.STALResponse;

public abstract class MultiInstanceHandler implements SMCCSTALRequestHandler {
  
  private static Log log = LogFactory.getLog(MultiInstanceHandler.class);
  
  protected List<SMCCSTALRequestHandler> handlerList = new ArrayList<SMCCSTALRequestHandler>();
  
  
  
  protected MultiInstanceHandler() {  
  }
  
  public void registerHandlerInstance(SMCCSTALRequestHandler handler) {
    handlerList.add(handler);
  }
  
  public void unregisterHandlerInstance(SMCCSTALRequestHandler handler) {
    handlerList.remove(handler);
  }
  
 
  @Override
  public void init(SignatureCard sc, BKUGUIFacade gui) {
  }

  @Override
  public SMCCSTALRequestHandler newInstance() {
    return this;
  }

  @Override
  public boolean requireCard() {
    return false;
  }
}
