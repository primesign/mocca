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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
//import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.gv.egiz.bku.gui.BKUGUIFacade;
import at.gv.egiz.smcc.SignatureCard;
import at.gv.egiz.stal.ErrorResponse;
import at.gv.egiz.stal.InfoboxReadRequest;
import at.gv.egiz.stal.STAL;
import at.gv.egiz.stal.STALRequest;
import at.gv.egiz.stal.STALResponse;
import at.gv.egiz.stal.StatusRequest;

public abstract class AbstractSMCCSTAL implements STAL {
  private final Logger log = LoggerFactory.getLogger(AbstractSMCCSTAL.class);

  public final static int DEFAULT_MAX_RETRIES = 1;

//  protected Locale locale = Locale.getDefault();
  protected SignatureCard signatureCard = null;
  protected Map<String, SMCCSTALRequestHandler> handlerMap = new HashMap<String, SMCCSTALRequestHandler>();

  protected int maxRetries = DEFAULT_MAX_RETRIES;
  protected Set<Integer> unrecoverableErrors = new HashSet<Integer>();

  protected AbstractSMCCSTAL() {
    addRequestHandler(InfoboxReadRequest.class, new InfoBoxReadRequestHandler());
    addRequestHandler(StatusRequest.class, new StatusRequestHandler());
    unrecoverableErrors.add(6001);
  }

  /**
   * Implementations must assign the signature card within this method.
   * 
   * @return whether the user canceled
   */
  protected abstract boolean waitForCard();

  protected abstract BKUGUIFacade getGUI();

  private STALResponse getResponse(STALRequest request) throws InterruptedException {
    int retryCounter = 0;
    while (retryCounter < maxRetries) {
      log.info("Retry #{} of {}.", retryCounter+1, maxRetries);
      SMCCSTALRequestHandler handler = null;
      handler = handlerMap.get(request.getClass().getSimpleName());
      if (handler != null) {
        if (handler.requireCard()) {
          if (waitForCard()) {
            ErrorResponse err = new ErrorResponse(6001);
            err.setErrorMessage("Cancel while waiting for card");
            return err;
          }
        }
        try {
          handler.init(signatureCard, getGUI());
          STALResponse response = handler.handleRequest(request);
          if (response != null) {
            if (response instanceof ErrorResponse) {
              ErrorResponse err = (ErrorResponse) response;
              log.info("Got an error response: " + err.getErrorMessage());
              if (unrecoverableErrors.contains(err.getErrorCode())) {
                return response;
              }
              if ((++retryCounter < maxRetries) && (handler.requireCard())) {
                signatureCard.disconnect(true);
                signatureCard = null;
              } else {
                log.info("Exceeded max retries, returning error {}.", err
                    .getErrorMessage());
                return response;
              }
            } else {
              return response;
            }
          } else {
            log.info("Got null response from handler, assuming quit");
            return null;
          }
        } catch (InterruptedException e) {
          log.info("Interrupt during request handling, do not retry.");
          throw e;
        } catch (Exception e) {
          log.info("Error while handling STAL request.", e);
          if (++retryCounter < maxRetries) {
            signatureCard.disconnect(true);
            signatureCard = null;
          } else {
            log.info("Exceeded max retries, returning error.");
            ErrorResponse err = new ErrorResponse(6000);
            err.setErrorMessage("Exceeded max retries trying to read STAL response");
            return err;
          }
        }
      } else {
        log.error("Cannot find a handler for STAL request: {}.", request);
        ErrorResponse err = new ErrorResponse();
        err.setErrorMessage("Cannot find a handler for STAL request: " + request);
        return err;
      }
    }
    ErrorResponse err = new ErrorResponse(6000);
    err.setErrorMessage("Exceeded max retries trying to read STAL response");
    return err;
  }

  /**
   *
   * @param requestList
   * @return
   * @throws RuntimeException with cause InterruptedException if interrupted
   */
  @Override
  public List<STALResponse> handleRequest(List<? extends STALRequest> requestList) {
    log.debug("Got request list containing {} STAL requests.", requestList.size());
    List<STALResponse> responseList = new ArrayList<STALResponse>(requestList
        .size());
    for (STALRequest request : requestList) {
      log.info("Processing: {}.", request.getClass());
      STALResponse response;
      try {
        response = getResponse(request);
        if (response != null) {
          responseList.add(response);
          if (response instanceof ErrorResponse) {
            ErrorResponse err = (ErrorResponse)response;
            log.info("Got an error response, don't process remaining requests: " + err.getErrorMessage());
            break;
          }
        }
      } catch (InterruptedException ex) {
        log.error("Interrupted during request handling.");
        throw new RuntimeException("Interrupted during request handling", ex);
      }
      
    }
    return responseList;
  }

  public void addRequestHandler(Class<? extends STALRequest> id,
      SMCCSTALRequestHandler handler) {
    log.trace("Registering STAL request handler: {}.", id.getSimpleName());
    handlerMap.put(id.getSimpleName(), handler);
  }

  public void removeRequestHandler(Class<? extends STALRequest> id) {
    log.trace("De-registering STAL request handler: {}", id.getSimpleName());
    handlerMap.remove(id.getSimpleName());
  }

  public SMCCSTALRequestHandler getRequestHandler(
      Class<? extends STALRequest> request) {
    return handlerMap.get(request.getSimpleName());
  }

//  @Override
//  public void setLocale(Locale locale) {
//    if (locale == null) {
//      throw new NullPointerException("Locale must not be set to null");
//    }
//    this.locale = locale;
//  }

  public void setMaxRetries(int maxRetries) {
    this.maxRetries = maxRetries;
  }

  public Set<Integer> getUnrecoverableErrors() {
    return unrecoverableErrors;
  }

  public void setUnrecoverableErrors(Set<Integer> unrecoverableErrors) {
    this.unrecoverableErrors = unrecoverableErrors;
  }
}
