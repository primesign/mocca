/*
* Copyright 2008 Federal Chancellery Austria and
* Graz University of Technology
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package at.gv.egiz.bku.online.applet;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import at.gv.egiz.bku.gui.BKUGUIFacade;
import at.gv.egiz.bku.smccstal.AbstractSMCCSTAL;
import at.gv.egiz.bku.smccstal.SMCCSTALRequestHandler;
import at.gv.egiz.smcc.SignatureCard;
import at.gv.egiz.smcc.util.SMCCHelper;
import at.gv.egiz.stal.QuitRequest;
import at.gv.egiz.stal.STALRequest;
import at.gv.egiz.stal.STALResponse;
import at.gv.egiz.stal.service.GetNextRequestResponseType;
import at.gv.egiz.stal.service.GetNextRequestType;
import at.gv.egiz.stal.service.ObjectFactory;
import at.gv.egiz.stal.service.STALPortType;
import at.gv.egiz.stal.service.STALService;

public class BKUWorker extends AbstractSMCCSTAL implements Runnable,
    ActionListener, SMCCSTALRequestHandler {

  private static Log log = LogFactory.getLog(BKUWorker.class);

  protected BKUGUIFacade gui;
  protected BKUApplet parent;
  private STALPortType stalPort;
  protected List<String> actionCommandList = new ArrayList<String>();
  protected Boolean actionPerformed = false;
  protected boolean finished = false;
  protected ResourceBundle errorMessages;

  /**
   * 
   * @param gui
   *          must not be null
   */
  public BKUWorker(BKUGUIFacade gui, BKUApplet parent,
      ResourceBundle errorMessageBundle) {
    if ((gui == null) || (parent == null) || (errorMessageBundle == null)) {
      throw new NullPointerException("Parameter must not be set to null");
    }
    this.gui = gui;
    this.parent = parent;
    this.errorMessages = errorMessageBundle;
    addRequestHandler(QuitRequest.class, this);
  }

  private STALPortType getSTALPort() throws MalformedURLException {
    URL wsdlURL = null;
    String wsdlLocation = parent.getMyAppletParameter(BKUApplet.WSDL_URL);
    URL codebase = parent.getCodeBase();
    log.debug("Connecting to webservice: " + wsdlLocation);
    if (wsdlLocation != null) {
      try {
        if (codebase.getProtocol().equalsIgnoreCase("file")) {
          // for debugging in appletrunner
        wsdlURL = new URL(wsdlLocation);
        } else {
          wsdlURL = new URL(codebase, wsdlLocation);
        }
      } catch (MalformedURLException ex) {
        log.fatal("Paremeter 'wsdlLocation' is not a vailid URL.", ex);
        throw new MalformedURLException(ex.getMessage());
      }
    } else {
      log.fatal("Paremeter 'wsdlLocation' is not set.");
      throw new MalformedURLException("Null WSDL url");
    }
    log.debug("Found WSDL url: " + wsdlURL);
    QName endpointName = new QName("http://www.egiz.gv.at/wsdl/stal",
        "STALService");
    STALService stal = new STALService(wsdlURL, endpointName);
    return stal.getSTALPort();
  }

  @Override
  public void run() {
    gui.showWelcomeDialog();
    try {
      stalPort = getSTALPort();
    } catch (Exception e) {
      log.fatal("Failed to call STAL service.", e);
      actionCommandList.clear();
      actionCommandList.add("ok");
      gui.showErrorDialog(errorMessages.getString("failed.WS"));
      try {
        waitForAction();
      } catch (InterruptedException e1) {
        log.error(e1);
      }
      return;
    }

    ObjectFactory factory = new ObjectFactory();
    GetNextRequestType nextRequest = factory.createGetNextRequestType();

    String sessionId = parent.getMyAppletParameter(BKUApplet.SESSION_ID);
    if (sessionId == null) {
      // use the testsession for testing
      sessionId = "TestSession";
    }
    nextRequest.setSessionId(sessionId);
    do {
      GetNextRequestResponseType resp = stalPort.getNextRequest(nextRequest);
      log.info("Got " + resp.getRequest().size() + " requests from server.");
      List<STALRequest> stalRequests = resp.getRequest();
      List<STALResponse> responses = handleRequest(stalRequests);
      log.info("Got " + responses.size() + " responses.");
      nextRequest = factory.createGetNextRequestType();
      nextRequest.setSessionId(sessionId);
      nextRequest.getResponse().addAll(responses);
    } while (!finished);
    log.info("Done " + Thread.currentThread().getName());
    gui.showWelcomeDialog();
    sendRedirect();
  }

  protected void sendRedirect() {
    log.info("Done, sending redirect to get BKU response");
    String redirectURL = parent.getMyAppletParameter("redirectURL");
    String redirectTarget = parent.getMyAppletParameter("redirectTarget");
    log.info("Redirecting to: " + redirectURL + " target: " + redirectTarget);
    URL url = null;
    if (redirectURL != null) {
      try {
        url = new URL(parent.getCodeBase(),redirectURL + ";jsessionid="
            + parent.getMyAppletParameter(BKUApplet.SESSION_ID));
      } catch (MalformedURLException ex) {
        log.warn("Parameter 'redirectURL': " + redirectURL
            + " not a valid URL.", ex);
        // gui.showErrorDialog(errorMsg, okListener, actionCommand)
      }
      if (url != null) {
        if (redirectTarget == null) {
          log.info("Done. Trying to redirect to " + url + " ...");
          parent.getAppletContext().showDocument(url);
        } else {
          log.info("Done. Trying to redirect to " + url + " (target="
              + redirectTarget + ") ...");
          parent.getAppletContext().showDocument(url, redirectTarget);
        }
      }
    } else {
      log.error("No redirect URL set");
    }
  }

  protected synchronized void waitForAction() throws InterruptedException {
    log.info("Waiting for Action");
    while (!actionPerformed) {
      wait();
    }
    actionPerformed = false;
  }

  protected synchronized void actionOccured() {
    log.info("Received Action");
    actionPerformed = true;
    notifyAll();
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    log.info("Action: " + e);
    if (actionCommandList != null) {
      if (actionCommandList.contains(e.getActionCommand())) {
        actionOccured();
      }
    } else {
      actionOccured();
    }
  }

  @Override
  protected boolean waitForCard() {
    SMCCHelper smccHelper = new SMCCHelper();
    actionCommandList.clear();
    actionCommandList.add("cancel");
    // while no sigcard found or cancel button pressed
    int oldValue = SMCCHelper.PC_SC_NOT_SUPPORTED; // this is a save default
    while ((signatureCard == null) && (!actionPerformed)) {
      switch (smccHelper.getResultCode()) {
      case SMCCHelper.PC_SC_NOT_SUPPORTED:
        actionCommandList.clear();
        actionCommandList.add("ok");
        gui.showErrorDialog(errorMessages.getString("nopcscsupport"), this,
            "ok");
        try {
          waitForAction();
        } catch (InterruptedException e) {
          log.error(e);
        }
        return true;
      case SMCCHelper.TERMINAL_NOT_PRESENT:
        actionCommandList.clear();
        actionCommandList.add("ok");
        gui.showErrorDialog(errorMessages.getString("nocardterminal"), this,
            "ok");
        try {
          waitForAction();
        } catch (InterruptedException e) {
          log.error(e);
        }
        return true;
      case SMCCHelper.CARD_NOT_SUPPORTED:
        if (oldValue != SMCCHelper.CARD_NOT_SUPPORTED) {
          actionCommandList.clear();
          actionCommandList.add("cancel");
          gui.showCardNotSupportedDialog(this, "cancel");
          oldValue = SMCCHelper.CARD_NOT_SUPPORTED;
        }
        break;
      case SMCCHelper.NO_CARD:
        if (oldValue != SMCCHelper.NO_CARD) {
          actionCommandList.clear();
          actionCommandList.add("cancel");
          gui.showInsertCardDialog(this, "cancel");
          oldValue = SMCCHelper.NO_CARD;
        }
        break;
      case SMCCHelper.CARD_FOUND:
        gui.showWelcomeDialog();
        signatureCard = smccHelper.getSignatureCard(errorMessages.getLocale());
        return false;
      }
      smccHelper.update(3000);
    }
    return signatureCard == null;
  }

  @Override
  public STALResponse handleRequest(STALRequest request) {
    if (request instanceof QuitRequest) {
      finished = true;
    } else {
      log.error("Unexpected request to handle: " + request);
    }
    return null;
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

  @Override
  protected BKUGUIFacade getGUI() {
    return gui;
  }
}
