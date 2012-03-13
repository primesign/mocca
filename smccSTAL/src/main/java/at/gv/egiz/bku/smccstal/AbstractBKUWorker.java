/*
 * Copyright 2011 by Graz University of Technology, Austria
 * MOCCA has been developed by the E-Government Innovation Center EGIZ, a joint
 * initiative of the Federal Chancellery Austria and Graz University of Technology.
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * http://www.osor.eu/eupl/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 *
 * This product combines work with different licenses. See the "NOTICE" text
 * file for details on the various modules and licenses.
 * The "NOTICE" text file is part of the distribution. Any derivative works
 * that you distribute must include a readable copy of the "NOTICE" text file.
 */


package at.gv.egiz.bku.smccstal;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.gv.egiz.bku.gui.BKUGUIFacade;
import at.gv.egiz.smcc.SignatureCard;
import at.gv.egiz.smcc.util.SMCCHelper;
import at.gv.egiz.stal.QuitRequest;
import at.gv.egiz.stal.STALRequest;
import at.gv.egiz.stal.STALResponse;

/**
 * Abstract base class for AppletBKUWorker and LocalBKUWorker, 
 * providing card specific functionality not implemented by AbstractSMCCSTAL
 * as well as common GUI functionality (action event handling).
 * <br/>
 * This class implements SMCCSTALRequestHandler and registers itself as QUIT handler.
 * 
 * @author Clemens Orthacker <clemens.orthacker@iaik.tugraz.at>
 */
public abstract class AbstractBKUWorker extends AbstractSMCCSTAL implements ActionListener, SMCCSTALRequestHandler {

  private final Logger log = LoggerFactory.getLogger(AbstractBKUWorker.class);
  
  protected BKUGUIFacade gui;
  protected List<String> actionCommandList = new ArrayList<String>();
  protected Boolean actionPerformed = false;
  protected boolean finished = false;

  public AbstractBKUWorker(BKUGUIFacade gui) {
    if (gui == null) {
      throw new NullPointerException("No BKU GUI provided");
    }
    this.gui = gui;
//    this.locale = gui.getLocale();
    addRequestHandler(QuitRequest.class, this);
  }

  ///////////////////////////////////////////////////////////////////
  // Common action event handling                                  //
  ///////////////////////////////////////////////////////////////////

  /**
   * notifies all registered handlers that an event occured
   * @param e
   */
  @Override
  public void actionPerformed(ActionEvent e) {
    log.info("Action: {}.", e);
    if (actionCommandList != null) {
      if (actionCommandList.contains(e.getActionCommand())) {
        actionOccured();
      }
    } else {
      actionOccured();
    }
  }
  
  /**
   * register for notification on action event
   * @throws java.lang.InterruptedException
   */
  protected synchronized void waitForAction() throws InterruptedException {
    log.info("Waiting for Action.");
    while (!actionPerformed) {
      wait();
    }
    actionPerformed = false;
  }

  protected synchronized void actionOccured() {
    log.info("Received Action.");
    actionPerformed = true;
    notifyAll();
  }
  
  ///////////////////////////////////////////////////////////////////
  // card specific implementations of AbstractSMCCSTAL             //
  ///////////////////////////////////////////////////////////////////

  @Override
  protected boolean waitForCard() {
    if (signatureCard != null) {
      return false;
    }
    SMCCHelper smccHelper = new SMCCHelper();
    actionCommandList.clear();
    actionCommandList.add("cancel");
    // while no sigcard found or cancel button pressed
    int oldValue = SMCCHelper.PC_SC_NOT_SUPPORTED; // this is a safe default
    actionPerformed = false;
    while ((signatureCard == null) && (!actionPerformed)) {
      switch (smccHelper.getResultCode()) {
      case SMCCHelper.PC_SC_NOT_SUPPORTED:
        actionCommandList.clear();
        actionCommandList.add("ok");
        gui.showErrorDialog(BKUGUIFacade.ERR_NO_PCSC, null, this, "ok");
        try {
          waitForAction();
        } catch (InterruptedException e) {
        }
        return true;
      case SMCCHelper.TERMINAL_NOT_PRESENT:
        actionCommandList.clear();
        actionCommandList.add("ok");
        gui.showErrorDialog(BKUGUIFacade.ERR_NO_CARDTERMINAL, null, this, "ok");
        try {
          waitForAction();
        } catch (InterruptedException e) {
        }
        return true;
      case SMCCHelper.CARD_NOT_SUPPORTED:
        if (oldValue != SMCCHelper.CARD_NOT_SUPPORTED) {
          actionCommandList.clear();
          actionCommandList.add("cancel");
          gui.showMessageDialog(BKUGUIFacade.TITLE_CARD_NOT_SUPPORTED,
                  BKUGUIFacade.MESSAGE_CARD_NOT_SUPPORTED, null,
                  BKUGUIFacade.BUTTON_CANCEL, this, "cancel");
          oldValue = SMCCHelper.CARD_NOT_SUPPORTED;
        }
        break;
      case SMCCHelper.NO_CARD:
        if (oldValue != SMCCHelper.NO_CARD) {
          actionCommandList.clear();
          actionCommandList.add("cancel");
          gui.showMessageDialog(BKUGUIFacade.TITLE_INSERTCARD,
            BKUGUIFacade.MESSAGE_INSERTCARD, null,
            BKUGUIFacade.BUTTON_CANCEL, this, "cancel");
          oldValue = SMCCHelper.NO_CARD;
        }
        break;
      case SMCCHelper.CARD_FOUND:
        signatureCard = smccHelper.getSignatureCard(gui.getLocale());
        return false;
      }
      smccHelper.update(3000);
    }
    return signatureCard == null;
  }

  @Override
  protected BKUGUIFacade getGUI() {
    return gui;
  }

  ///////////////////////////////////////////////////////////////////
  // SMCCSTALRequestHandler for QUIT requests                      //
  ///////////////////////////////////////////////////////////////////
  
  /**
   * Handle QUIT requests: set finished true.
   * @param request a QUIT request
   * @return null (no response on QUIT)
   */
  @Override
  public STALResponse handleRequest(STALRequest request) {
    if (request instanceof QuitRequest) {
      log.info("Setting state to: finished for BKUWorker {}.", this);
      finished = true;
    } else {
      log.error("Unexpected request to handle: {}.", request);
    }
    return null;
  }

  /**
   * No initialization required for QUIT request handlers.
   * @param sc
   * @param gui
   */
  @Override
  public void init(SignatureCard sc, BKUGUIFacade gui) {
  }

  /**
   * QUIT request handlers do not require a card.
   * @return false
   */
  @Override
  public boolean requireCard() {
    return false;
  }
}
