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
import at.gv.egiz.stal.service.translator.STALTranslator;
import at.gv.egiz.stal.service.translator.TranslationException;
import at.gv.egiz.stal.service.types.ErrorResponseType;
import at.gv.egiz.stal.service.types.GetNextRequestResponseType;
import at.gv.egiz.stal.service.types.GetNextRequestType;
import at.gv.egiz.stal.service.types.ObjectFactory;
import at.gv.egiz.stal.service.types.RequestType;
import at.gv.egiz.stal.service.types.ResponseType;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.ws.WebServiceException;

/**
 * 
 * @author Clemens Orthacker <clemens.orthacker@iaik.tugraz.at>
 */
public class AppletBKUWorker extends AbstractBKUWorker implements Runnable {

  protected BKUApplet applet;
  protected String sessionId;

  private ObjectFactory stalObjFactory = new ObjectFactory();

  public AppletBKUWorker(BKUApplet applet, BKUGUIFacade gui) {
    super(gui);
    this.applet = applet;
    
    sessionId = applet.getParameter(BKUApplet.SESSION_ID);
    if (sessionId == null) {
      sessionId = "TestSession";
      log.warn("using dummy sessionId " + sessionId);
    }
  }

  @Override
  public void run() {
    gui.showWelcomeDialog();

    try {
      STALPortType stalPort = applet.getSTALPort();
      STALTranslator stalTranslator = applet.getSTALTranslator();

      addRequestHandler(SignRequest.class,
              new AppletHashDataDisplay(stalPort, sessionId));

      GetNextRequestResponseType nextRequestResp = stalPort.connect(sessionId);

      do {
        List<JAXBElement<? extends RequestType>> requests;
        List<JAXBElement<? extends ResponseType>> responses = new ArrayList<JAXBElement<? extends ResponseType>>();

        try {
          requests = nextRequestResp.getInfoboxReadRequestOrSignRequestOrQuitRequest();
          responses.clear();

          // (rather use validator)
          if (requests.size() == 0) {
            log.error("Received empty NextRequestResponse: no STAL requests to handle. (STAL-X requests might not have gotten unmarshalled)");
            throw new RuntimeException("No STAL requests to handle.");
          }

          if (log.isInfoEnabled()) {
            StringBuilder sb = new StringBuilder("Received ");
            sb.append(requests.size());
            sb.append(" requests: ");
            for (JAXBElement<? extends RequestType> r : requests) {
              sb.append(r.getValue().getClass());
              sb.append(' ');
            }
            log.info(sb.toString());
          }

          List<STALRequest> stalRequests = new ArrayList<STALRequest>();
          for (JAXBElement<? extends RequestType> req : requests) {
            try {
              stalRequests.add(stalTranslator.translate(req));
            } catch (TranslationException ex) {
              log.error("Received unknown request from server STAL: " + ex.getMessage());
              throw new RuntimeException(ex);
            }
          }

          checkPermission(stalRequests);

          List<STALResponse> stalResponses = handleRequest(stalRequests);
          for (STALResponse stalResponse : stalResponses) {
            try {
              responses.add(stalTranslator.translate(stalResponse));
            } catch (TranslationException ex) {
              log.error("Received unknown response from STAL: " + ex.getMessage());
              throw new RuntimeException(ex);
            }
          }

        } catch (RuntimeException ex) {
          // return ErrorResponse to server, which displays error page
          log.error(ex.getMessage());
          ErrorResponseType err = stalObjFactory.createErrorResponseType();
          if (ex instanceof SecurityException) {
            err.setErrorCode(6002);
          } else {
            Throwable cause = ex.getCause();
            if (cause != null && cause instanceof InterruptedException) {
              log.info("do not return error response, client might want to resume session");
              finished = true;
            }
            err.setErrorCode(4000);
          }
          responses.clear();
          responses.add(stalObjFactory.createGetNextRequestTypeErrorResponse(err));

        } finally {
          if (!finished) {
            if (log.isInfoEnabled()) {
              StringBuilder sb = new StringBuilder("Sending ");
              sb.append(responses.size());
              sb.append(" responses: ");
              for (JAXBElement<? extends ResponseType> r : responses) {
                sb.append(r.getValue().getClass());
                sb.append(' ');
              }
              log.info(sb.toString());
            }
            GetNextRequestType nextRequest = stalObjFactory.createGetNextRequestType();
            nextRequest.setSessionId(sessionId);
            nextRequest.getInfoboxReadResponseOrSignResponseOrErrorResponse().addAll(responses);
            nextRequestResp = stalPort.getNextRequest(nextRequest);
          }
        }


      } while (!finished);
      log.info("Finished " + Thread.currentThread().getName());

    } catch (WebServiceException ex) {
      log.fatal("communication error with server STAL: " + ex.getMessage(), ex);
      showErrorDialog(BKUGUIFacade.ERR_SERVICE_UNREACHABLE, ex);
    } catch (MalformedURLException ex) {
      log.fatal(ex.getMessage(), ex);
      showErrorDialog(BKUGUIFacade.ERR_CONFIG, ex);
    } catch (Exception ex) {
      log.error(ex.getMessage(), ex);
      showErrorDialog(BKUGUIFacade.ERR_UNKNOWN_WITH_PARAM, ex);
    } finally {
      if (signatureCard != null) {
        signatureCard.disconnect(false);
      }
    }

    applet.sendRedirect(sessionId);
  }

  /**
   * throws RuntimeException if requests contain InfoboxReadRequest for IdentityLink
   * and STAL Service Endpoint is no e-Gov agency
   * @param stalRequests
   */
  private void checkPermission(List<STALRequest> stalRequests) {
    for (STALRequest request : stalRequests) {
      if (request instanceof at.gv.egiz.stal.InfoboxReadRequest) {
        at.gv.egiz.stal.InfoboxReadRequest r = (at.gv.egiz.stal.InfoboxReadRequest) request;
        String infoboxId = r.getInfoboxIdentifier();
        String domainId = r.getDomainIdentifier();
        if ("IdentityLink".equals(infoboxId) && domainId == null) {
          if (!InternalSSLSocketFactory.getInstance().isEgovAgency()) {
            throw new SecurityException("Insufficient rights to execute command InfoboxReadRequest for Infobox IdentityLink");
          }
        }
      }
    }
  }

  /**
   *
   * @param err_code
   * @param ex if not null, the message will be appended as parameter to the error message
   */
  protected void showErrorDialog(String err_code, Exception ex) {
    actionCommandList.clear();
    actionCommandList.add("ok");
    Object[] params = (ex != null) ? new Object[] { ex.getMessage() } : null;
    gui.showErrorDialog(err_code, params, this, "ok");
    try {
      waitForAction();
    } catch (InterruptedException e) {
      log.error(e);
    }
  }
}
