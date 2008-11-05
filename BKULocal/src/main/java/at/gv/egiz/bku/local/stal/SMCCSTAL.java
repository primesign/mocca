package at.gv.egiz.bku.local.stal;

import java.util.List;

import javax.swing.JDialog;

import at.gv.egiz.bku.gui.BKUGUIFacade;
import at.gv.egiz.bku.smccstal.AbstractBKUWorker;
import at.gv.egiz.stal.QuitRequest;
import at.gv.egiz.stal.STALRequest;
import at.gv.egiz.stal.STALResponse;
import at.gv.egiz.stal.SignRequest;

public class SMCCSTAL extends AbstractBKUWorker {

  private JDialog container;

  public SMCCSTAL(BKUGUIFacade gui, JDialog container) {
    super(gui);
    this.container = container;
    addRequestHandler(SignRequest.class, new LocalSignRequestHandler());
  }

  @Override
  public List<STALResponse> handleRequest(List<STALRequest> requestList) {
    signatureCard = null;
    List<STALResponse> responses = super.handleRequest(requestList);
    // container.setVisible(false);
    return responses;
  }

  @Override
  public STALResponse handleRequest(STALRequest request) {
    if (request instanceof QuitRequest) {
      container.setVisible(false);
    }
    return null;
  }

}
