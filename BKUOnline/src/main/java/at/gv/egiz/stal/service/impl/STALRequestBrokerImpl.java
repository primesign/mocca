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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package at.gv.egiz.stal.service.impl;

import at.gv.egiz.stal.ErrorResponse;
import at.gv.egiz.stal.HashDataInput;
import at.gv.egiz.stal.QuitRequest;
import at.gv.egiz.stal.STALRequest;
import at.gv.egiz.stal.STALResponse;
import at.gv.egiz.stal.SignRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * An instance of STALRequestBroker is shared between a producer threads (SLCommand)
 * and multiple consumer threads (STALService).
 * This implementation assures that handleRequest is executed only once the previous invocation returned.
 * The BindingProcessor assures that a new SLCommand calls handleRequest() only once
 * the bindingProcessor called handleRequest(QUIT) after the previous SLCommand's handleRequest() returned.
 * 
 * Multiple STALService threads might call nextRequest()/getSignedReferences() in any order.
 * 
 * @author clemens
 */
public class STALRequestBrokerImpl implements STALRequestBroker {

    private static final Log log = LogFactory.getLog(STALRequestBrokerImpl.class);
    protected List<STALRequest> requests = null;
    protected List<STALResponse> responses = null;
    protected List<HashDataInput> currentHashDataInput;
//    private boolean isHandlingRequest = false;
    private boolean expectingResponse = false;
    private boolean interrupted = false;
    
    /**
     * Produce requests (and HashDataInputCallback) and wait for responses.
     * The next thread may enter once we consumed the responses.
     * 
     * @param requests
     * @return
     * 
     * @pre requests either single SignRequest, QuitRequest or multiple ReadInfoboxRequests
     */
    @Override
    public synchronized List<STALResponse> handleRequest(List<STALRequest> requests) {
      if (interrupted) {
        return null;
      }
        try {
//            long beforeWait = System.currentTimeMillis();
//            while (isHandlingRequest) {
//                log.trace("waiting to produce request");
//                wait(TIMEOUT_MS);
//                if (System.currentTimeMillis() - beforeWait >= TIMEOUT_MS) {
//                    log.warn("timeout while waiting to produce request");
//                    return Collections.singletonList((STALResponse) new ErrorResponse(ERR_6000));
//                }
//            }
            log.trace("produce request");
//            isHandlingRequest = true;

            this.requests = requests;
            currentHashDataInput = null;
            for (STALRequest request : requests) {
                if (request instanceof SignRequest) {
                    log.trace("Received SignRequest, keep HashDataInput.");
                    currentHashDataInput = ((SignRequest) request).getHashDataInput();
                    break;
                } else if (request instanceof QuitRequest) {
                    log.trace("Received QuitRequest, do not wait for responses.");
                    log.trace("notifying request consumers");
                    notify();
                    return new ArrayList<STALResponse>();
                } else if (log.isTraceEnabled()) {
                    log.trace("Received STAL request: " + request.getClass().getName());
                }
            }
            log.trace("notifying request consumers");
            notify();

            long beforeWait = System.currentTimeMillis();
            while (this.responses == null) {
                log.trace("waiting to consume response");
                wait(TIMEOUT_MS);
                if (System.currentTimeMillis() - beforeWait >= TIMEOUT_MS) {
                    log.warn("timeout while waiting to consume response");
                    this.requests = null;
                    currentHashDataInput = null;
//                    isHandlingRequest = false;
                    return Collections.singletonList((STALResponse) new ErrorResponse(ERR_6000));
                }
            }
            log.trace("consuming responses");
            List<STALResponse> resps = responses;
            responses = null;
            log.trace("notifying response producers");
            notify();

//            isHandlingRequest = false;
//            log.trace("notifying request producers");
//            notify();

            return resps;
        } catch (InterruptedException ex) {
            log.warn("interrupt in handleRequest(): " + ex.getMessage());
            interrupted = true;
            return null;
        }
    }

    /**
     * 
     * @param responses
     * @return QUIT if expected responses are not provided
     */
    @Override
    public synchronized List<STALRequest> nextRequest(List<STALResponse> responses) {
      if (interrupted) {
        return null;
      }
        try {
            if (responses != null && responses.size() > 0) {
                if (!expectingResponse) {
                    log.warn("Received unexpected response in nextRequest()");
                    return Collections.singletonList((STALRequest) new QuitRequest());
                }
                long beforeWait = System.currentTimeMillis();
                while (this.responses != null) {
                    log.trace("waiting to produce response");
                    wait(TIMEOUT_MS);
                    if (System.currentTimeMillis() - beforeWait >= TIMEOUT_MS) {
                        log.warn("timeout while waiting to produce response");
                        return Collections.singletonList((STALRequest) new QuitRequest());
                    }
                }
                log.trace("produce response");
                this.responses = responses;
                //reset HashDataInputCallback
                if (log.isTraceEnabled()) {
                    for (STALResponse response : responses) {
                        log.trace("Received STAL response: " + response.getClass().getName());
                    }
                }
                log.trace("notifying response consumers");
                notify();
            } else {
                if (expectingResponse) {
                    log.warn("No expected response received in nextRequest()");
                    return Collections.singletonList((STALRequest) new QuitRequest());
                }
                log.trace("expecting non-null response in next nextRequest(response)");
                expectingResponse = true;
            }
            long beforeWait = System.currentTimeMillis();
            while (this.requests == null) {
                log.trace("waiting to consume request");
                wait(TIMEOUT_MS);
                if (System.currentTimeMillis() - beforeWait >= TIMEOUT_MS) {
                    log.warn("timeout while waiting to consume request");
                    return Collections.singletonList((STALRequest) new QuitRequest());
                }
            }
            log.trace("consume request");
            List<STALRequest> reqs = requests;
            requests = null;
            if (reqs.size() > 0 && reqs.get(0) instanceof QuitRequest) {
//                isHandlingRequest = false;
//                log.trace("consumed QUIT, notifying request producers");
//                notify();
                log.trace("expecting no response in next nextRequest()");
                expectingResponse = false;
            }
            return reqs;
        } catch (InterruptedException ex) {
            log.warn("interrupt in nextRequest(): " + ex.getMessage());
            interrupted = true;
            return null;
        }
    }

    @Override
    public synchronized List<HashDataInput> getHashDataInput() {
        log.trace("return " + currentHashDataInput.size() + " current HashDataInput(s) ");
        return currentHashDataInput;
    }
    
    @Override
    public void setLocale(Locale locale) {
        // TODO Auto-generated method stub
    }
}
