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

import at.gv.egiz.bku.smccstal.AbstractBKUWorker;
import at.gv.egiz.bku.gui.BKUGUIFacade;
import at.gv.egiz.stal.STALRequest;
import at.gv.egiz.stal.STALResponse;
import at.gv.egiz.stal.SignRequest;
import at.gv.egiz.stal.service.STALPortType;
import at.gv.egiz.stal.service.STALService;
import at.gv.egiz.stal.service.types.ErrorResponseType;
import at.gv.egiz.stal.service.types.GetNextRequestResponseType;
import at.gv.egiz.stal.service.types.GetNextRequestType;
import at.gv.egiz.stal.service.types.ObjectFactory;
import at.gv.egiz.stal.service.types.RequestType;
import at.gv.egiz.stal.service.types.ResponseType;
import at.gv.egiz.stal.util.STALTranslator;
import java.applet.AppletContext;
import java.awt.Dimension;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;

/**
 *
 * @author Clemens Orthacker <clemens.orthacker@iaik.tugraz.at>
 */
public class AppletBKUWorker extends AbstractBKUWorker implements Runnable {

  protected AppletContext ctx;
  protected AppletParameterProvider params;
  protected String sessionId;
  protected STALPortType stalPort;

  public AppletBKUWorker(BKUGUIFacade gui, AppletContext ctx, AppletParameterProvider paramProvider) {
    super(gui);
    if (ctx == null) {
      throw new NullPointerException("Applet context not provided");
    }
    if (paramProvider == null) {
      throw new NullPointerException("No applet parameters provided");
    }
    this.ctx = ctx;
    this.params = paramProvider;

    sessionId = params.getAppletParameter(BKUApplet.SESSION_ID);
    if (sessionId == null) {
      sessionId = "TestSession";
      log.info("using dummy sessionId " + sessionId);
    }
  }

  @Override
  public void run() {
    gui.showWelcomeDialog();
    try {
      stalPort = getSTALPort();
    } catch (Exception e) {
      log.fatal("Failed to get STAL web-service port: " + e.getMessage(), e);
      actionCommandList.clear();
      actionCommandList.add("ok");
      gui.showErrorDialog(BKUGUIFacade.ERR_SERVICE_UNREACHABLE,
              new Object[]{e.getMessage()});
      try {
        waitForAction();
      } catch (InterruptedException e1) {
        log.error(e1);
      }
      return;
    }

    try {
      registerSignRequestHandler();

      ObjectFactory of = new ObjectFactory();

      GetNextRequestResponseType nextRequestResp = stalPort.connect(sessionId);
      do {
        List<RequestType> requests = nextRequestResp.getInfoboxReadRequestOrSignRequestOrQuitRequest();
        List<STALRequest> stalRequests = STALTranslator.translateRequests(requests);

        if (log.isInfoEnabled()) {
          StringBuilder sb = new StringBuilder("Received ");
          sb.append(stalRequests.size());
          sb.append(" STAL requests: ");
          for (STALRequest r : stalRequests) {
            sb.append(r.getClass());
            sb.append(' ');
          }
          log.info(sb.toString());
        }

        boolean handle = true;
        for (STALRequest request : stalRequests) {
          if (request instanceof at.gv.egiz.stal.InfoboxReadRequest) {
            at.gv.egiz.stal.InfoboxReadRequest r = (at.gv.egiz.stal.InfoboxReadRequest) request;
            String infoboxId = r.getInfoboxIdentifier();
            String domainId = r.getDomainIdentifier();
            if ("IdentityLink".equals(infoboxId) && domainId == null) {
              if (!InternalSSLSocketFactory.getInstance().isEgovAgency()) {
                handle = false;
              }
            }
          }
        }

        List<ResponseType> responses;
        if (handle) {
          List<STALResponse> stalResponses = handleRequest(stalRequests);
          if (log.isInfoEnabled()) {
            StringBuilder sb = new StringBuilder(stalResponses.size());
            sb.append(" STAL responses: ");
            for (STALResponse r : stalResponses) {
              sb.append(r.getClass());
              sb.append(' ');
            }
            log.info(sb.toString());
          }
          responses = STALTranslator.fromSTAL(stalResponses);
        } else {
          responses = new ArrayList<ResponseType>(1);
          ErrorResponseType err = of.createErrorResponseType();
          err.setErrorCode(6002);
          // err.setErrorMessage();
          responses.add(err);
        }

        if (!finished) {
          log.info("Not finished yet (BKUWorker: " + this + "), sending responses");
          GetNextRequestType nextRequest = of.createGetNextRequestType();
          nextRequest.setSessionId(sessionId);
          nextRequest.getInfoboxReadResponseOrSignResponseOrErrorResponse().addAll(responses);
          nextRequestResp = stalPort.getNextRequest(nextRequest);
        }
      } while (!finished);
      log.info("Done " + Thread.currentThread().getName());
    } catch (Exception ex) {
      log.error(ex.getMessage(), ex);
      gui.showErrorDialog(BKUGUIFacade.ERR_UNKNOWN, new Object[]{ex.getMessage()});
      try {
        waitForAction();
      } catch (InterruptedException e) {
        log.error(e);
      }
    }
    if (signatureCard != null) {
      signatureCard.disconnect(false);
    }
    sendRedirect();
  }

  protected void sendRedirect() {
    try {
      URL redirectURL = params.getURLParameter(BKUApplet.REDIRECT_URL, sessionId);
      String redirectTarget = params.getAppletParameter(BKUApplet.REDIRECT_TARGET);
      if (redirectTarget == null) {
        log.info("Done. Redirecting to " + redirectURL + " ...");
        ctx.showDocument(redirectURL);
      } else {
        log.info("Done. Redirecting to " + redirectURL + " (target=" + redirectTarget + ") ...");
        ctx.showDocument(redirectURL, redirectTarget);
      }
    } catch (MalformedURLException ex) {
      log.warn("Failed to redirect: " + ex.getMessage(), ex);
    // gui.showErrorDialog(errorMsg, okListener, actionCommand)
    }
  }

  private STALPortType getSTALPort() throws MalformedURLException {
    URL wsdlURL = params.getURLParameter(BKUApplet.WSDL_URL);
    log.debug("STAL WSDL at " + wsdlURL);
    QName endpointName = new QName(BKUApplet.STAL_WSDL_NS, BKUApplet.STAL_SERVICE);
    STALService stal = new STALService(wsdlURL, endpointName);
    return stal.getSTALPort();
  }

  private void registerSignRequestHandler() throws MalformedURLException {
    String hashDataDisplayStyle = params.getAppletParameter(BKUApplet.HASHDATA_DISPLAY);
    if (BKUApplet.HASHDATA_DISPLAY_INTERNAL.equals(hashDataDisplayStyle)) {
      log.debug("register SignRequestHandler for STAL port " + BKUApplet.WSDL_URL);
      addRequestHandler(SignRequest.class, new AppletHashDataDisplay(stalPort, sessionId));
    } else if (BKUApplet.HASHDATA_DISPLAY_BROWSER.equals(hashDataDisplayStyle)) {
      URL hashDataURL = params.getURLParameter(BKUApplet.HASHDATA_URL, sessionId);
      log.debug("register SignRequestHandler for HashDataURL " + hashDataURL);
      addRequestHandler(SignRequest.class, new BrowserHashDataDisplay(ctx, hashDataURL));
    } else {
      //BKUApplet.HASHDATA_DISPLAY_FRAME
      log.debug("register SignRequestHandler for STAL port " + BKUApplet.WSDL_URL);
      addRequestHandler(SignRequest.class, new JDialogHashDataDisplay(stalPort, sessionId, new Dimension(400, 300), locale));
    }
  }
}
