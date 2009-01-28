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
package at.gv.egiz.bku.smccstal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import at.gv.egiz.bku.gui.BKUGUIFacade;
import at.gv.egiz.smcc.SignatureCard;
import at.gv.egiz.stal.ErrorResponse;
import at.gv.egiz.stal.InfoboxReadRequest;
import at.gv.egiz.stal.STAL;
import at.gv.egiz.stal.STALRequest;
import at.gv.egiz.stal.STALResponse;

public abstract class AbstractSMCCSTAL implements STAL {
  private static Log log = LogFactory.getLog(AbstractSMCCSTAL.class);

  public final static int DEFAULT_MAX_RETRIES = 3;

  protected Locale locale = Locale.getDefault();
  protected SignatureCard signatureCard = null;
  protected Map<String, SMCCSTALRequestHandler> handlerMap = new HashMap<String, SMCCSTALRequestHandler>();

  protected int maxRetries = DEFAULT_MAX_RETRIES;
  protected Set<Integer> unrecoverableErrors = new HashSet<Integer>();

  protected AbstractSMCCSTAL() {
    addRequestHandler(InfoboxReadRequest.class, new InfoBoxReadRequestHandler());
    unrecoverableErrors.add(6001);
  }

  /**
   * Implementations must assign the signature card within this method.
   * 
   * @return if the user canceled
   */
  protected abstract boolean waitForCard();

  protected abstract BKUGUIFacade getGUI();

  private STALResponse getResponse(STALRequest request) throws InterruptedException {
    int retryCounter = 0;
    while (retryCounter < maxRetries) {
      log.info("Retry #" + retryCounter + " of " + maxRetries);
      SMCCSTALRequestHandler handler = null;
      handler = handlerMap.get(request.getClass().getSimpleName());
      if (handler != null) {
        if (handler.requireCard()) {
          if (waitForCard()) {
            return new ErrorResponse(6001);
          }
        }
        try {
          handler.init(signatureCard, getGUI());
          STALResponse response = handler.handleRequest(request);
          if (response != null) {
            if (response instanceof ErrorResponse) {
              log.info("Got an error response");
              ErrorResponse err = (ErrorResponse) response;
              if (unrecoverableErrors.contains(err.getErrorCode())) {
                return response;
              }
              if ((++retryCounter < maxRetries) && (handler.requireCard())) {
                signatureCard.disconnect(true);
                signatureCard = null;
              } else {
                log.info("Exceeded max retries, returning error "
                    + err.getErrorMessage());
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
          log.info("Interrupt in handleRequest, do not retry");
          throw e;
        } catch (Exception e) {
          log.info("Error while handling STAL request:", e);
          if (++retryCounter < maxRetries) {
            signatureCard.disconnect(true);
            signatureCard = null;
          } else {
            log.info("Exceeded max retries, returning error.");
            return new ErrorResponse(6000);
          }
        }
      } else {
        log.error("Cannot find a handler for STAL request: " + request);
        return new ErrorResponse();
      }
    }
    return new ErrorResponse(6000);
  }

  @Override
  public List<STALResponse> handleRequest(List<? extends STALRequest> requestList) {
    log.debug("Got request list containing " + requestList.size()
        + " STAL requests");
    List<STALResponse> responseList = new ArrayList<STALResponse>(requestList
        .size());
    for (STALRequest request : requestList) {
      log.info("Processing: " + request.getClass());
      STALResponse response;
      try {
        response = getResponse(request);
        if (response != null) {
          responseList.add(response);
          if (response instanceof ErrorResponse) {
            log.info("Got an error response, don't process remaining requests");
            break;
          }
        }
      } catch (InterruptedException ex) {
        log.error("got interrupted, return ErrorResponse 6001");
        throw new RuntimeException(ex);
      }
      
    }
    return responseList;
  }

  public void addRequestHandler(Class<? extends STALRequest> id,
      SMCCSTALRequestHandler handler) {
    log.debug("Registering STAL request handler: " + id.getSimpleName());
    handlerMap.put(id.getSimpleName(), handler);
  }

  public SMCCSTALRequestHandler getRequestHandler(
      Class<? extends STALRequest> request) {
    return handlerMap.get(request.getSimpleName());
  }

  @Override
  public void setLocale(Locale locale) {
    if (locale == null) {
      throw new NullPointerException("Locale must not be set to null");
    }
    this.locale = locale;
  }

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
