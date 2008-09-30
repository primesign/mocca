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
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import at.gv.egiz.bku.gui.BKUGUIFacade;
import at.gv.egiz.smcc.SignatureCard;
import at.gv.egiz.smcc.util.SMCCHelper;
import at.gv.egiz.stal.ErrorResponse;
import at.gv.egiz.stal.InfoboxReadRequest;
import at.gv.egiz.stal.STAL;
import at.gv.egiz.stal.STALRequest;
import at.gv.egiz.stal.STALResponse;
import at.gv.egiz.stal.SignRequest;

public abstract class AbstractSMCCSTAL implements STAL {
  private static Log log = LogFactory.getLog(AbstractSMCCSTAL.class);

  protected Locale locale = Locale.getDefault();
//  protected SMCCHelper smccHelper = new SMCCHelper();
  protected SignatureCard signatureCard = null;
  protected static Map<String, SMCCSTALRequestHandler> handlerMap = new HashMap<String, SMCCSTALRequestHandler>();

  static {
    addRequestHandler(InfoboxReadRequest.class, new InfoBoxReadRequestHandler());
//    addRequestHandler(SignRequest.class, new SignRequestHandler());
  }

  /**
   * Implementations must assign the signature card within this method.
   * 
   * @return if the user canceled
   */
  protected abstract boolean waitForCard();

  protected abstract BKUGUIFacade getGUI();

  @Override
  public List<STALResponse> handleRequest(
      List<STALRequest> requestList) {
    log.debug("Got request list containing " + requestList.size()
        + " STAL requests");
    List<STALResponse> responseList = new ArrayList<STALResponse>(requestList
        .size());
    for (STALRequest request : requestList) {
      log.info("Processing: " + request.getClass());
      SMCCSTALRequestHandler handler = null;
      handler = handlerMap.get(request.getClass().getSimpleName());
      if (handler != null) {
        if (handler.requireCard()) {
          if (waitForCard()) {
            responseList.add(new ErrorResponse(6001));
            break;
          }
        }
        try {
          handler = handler.newInstance();
          handler.init(signatureCard, getGUI());
          STALResponse response = handler.handleRequest(request);
          if (response != null) {
            responseList.add(response);
          }
        } catch (Exception e) {
          log.info("Error while handling STAL request:" + e);
          responseList.add(new ErrorResponse(6000));
        }
      } else {
        log.error("Cannot find a handler for STAL request: " + request);
        responseList.add(new ErrorResponse());
      }
    }
    return responseList;
  }

  public static void addRequestHandler(Class<? extends STALRequest> id,
      SMCCSTALRequestHandler handler) {
    log.debug("Registering STAL request handler: " + id.getSimpleName());
    handlerMap.put(id.getSimpleName(), handler);
  }

  public static SMCCSTALRequestHandler getRequestHandler(
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
}
