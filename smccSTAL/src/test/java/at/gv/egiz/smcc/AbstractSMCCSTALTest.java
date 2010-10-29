package at.gv.egiz.smcc;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.smartcardio.Card;
import javax.smartcardio.CardTerminal;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import at.gv.egiz.bku.gui.BKUGUIFacade;
import at.gv.egiz.bku.smccstal.AbstractSMCCSTAL;
import at.gv.egiz.bku.smccstal.SMCCSTALRequestHandler;
import at.gv.egiz.smcc.pin.gui.PINGUI;
import at.gv.egiz.stal.ErrorResponse;
import at.gv.egiz.stal.InfoboxReadRequest;
import at.gv.egiz.stal.InfoboxReadResponse;
import at.gv.egiz.stal.STALRequest;
import at.gv.egiz.stal.STALResponse;

public class AbstractSMCCSTALTest extends AbstractSMCCSTAL implements
    SMCCSTALRequestHandler {
  private int errorConter;

  @Override
  protected BKUGUIFacade getGUI() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected boolean waitForCard() {
   signatureCard = new SignatureCard() {

    @Override
    public byte[] createSignature(InputStream input, KeyboxName keyboxName,
        PINGUI provider, String alg) throws SignatureCardException {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public void disconnect(boolean reset) {
      // TODO Auto-generated method stub
      
    }

    @Override
    public byte[] getCertificate(KeyboxName keyboxName, PINGUI gui)
        throws SignatureCardException {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public byte[] getInfobox(String infobox, PINGUI provider,
        String domainId) throws SignatureCardException {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public void init(Card card, CardTerminal cardTerminal) {
      // TODO Auto-generated method stub
      
    }

    @Override
    public void setLocale(Locale locale) {
      // TODO Auto-generated method stub
      
    }

    @Override
    public Card getCard() {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public void reset() throws SignatureCardException {
      // TODO Auto-generated method stub
      
    }
   };
    return false;
  }

  @Before
  public void setUp() {
    addRequestHandler(InfoboxReadRequest.class, this);
  }

  @Test
  @Ignore
  public void testRetry() {
    InfoboxReadRequest irr = new InfoboxReadRequest();
    List<STALRequest> irrl = new ArrayList<STALRequest>();
    irrl.add(irr);
    List<STALResponse> list = handleRequest(irrl);
    Assert.assertFalse(list.get(0) instanceof ErrorResponse);
  }

  @Override
  public STALResponse handleRequest(STALRequest request) {
    if (++errorConter < 3) {
      return new ErrorResponse(400);
    }
    return new InfoboxReadResponse();
  }

  @Override
  public void init(SignatureCard sc, BKUGUIFacade gui) {
  }

  @Override
  public boolean requireCard() {
    return true;
  }

}
