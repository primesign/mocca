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

    private boolean expectingResponse = false;
    private boolean interrupted = false;
    
    private final RequestsMonitor reqMon = new RequestsMonitor();
    private final ResponsesMonitor respMon = new ResponsesMonitor();
    
    private long timeout;

    public STALRequestBrokerImpl(long timeoutMillisec) {
      if (timeoutMillisec <= 0) 
        timeoutMillisec = DEFAULT_TIMEOUT_MS;
      this.timeout = timeoutMillisec;
    }
    
    /**
     * Produce requests (and HashDataInputCallback) and wait for responses.
     * This method is not thread safe, since every bindingprocessor thread possesses it's own instance.
     * It however assures cooperation with STAL webservice threads consuming the requests and producing responses.
     * 
     * @param requests
     * @return
     * 
     * @pre requests: either single SignRequest, QuitRequest or multiple ReadInfoboxRequests
     */
    @Override
    public List<STALResponse> handleRequest(List<STALRequest> requests) {
      if (interrupted) {
        return null;
      }
        try {
          synchronized (reqMon) {
            log.trace("produce request");

            reqMon.produce(requests);
            reqMon.setHashDataInput(null);
            for (STALRequest request : requests) {
                if (request instanceof SignRequest) {
                    log.trace("Received SignRequest, keep HashDataInput.");
                    reqMon.setHashDataInput(((SignRequest) request).getHashDataInput());
                    break;
                } else if (request instanceof QuitRequest) {
                    log.trace("Received QuitRequest, do not wait for responses.");
                    log.trace("notifying request consumers");
                    reqMon.notify();
                    return new ArrayList<STALResponse>();
                } else if (log.isTraceEnabled()) {
                    log.trace("Received STAL request: " + request.getClass().getName());
                }
            }
            log.trace("notifying request consumers");
            reqMon.notify();
          }
          
          synchronized (respMon) {
            long beforeWait = System.currentTimeMillis();
            while (respMon.responses == null) {
                log.trace("waiting to consume response");
                respMon.wait(timeout);
                if (System.currentTimeMillis() - beforeWait >= timeout) {
                    log.warn("timeout while waiting to consume response, cleanup requests");
                    reqMon.consume(); //TODO check deadlock?
                    reqMon.setHashDataInput(null);
                    return Collections.singletonList((STALResponse) new ErrorResponse(ERR_6000));
                }
            }
            log.trace("consuming responses");
            List<STALResponse> responses = respMon.consume();
            log.trace("notifying response producers");
            respMon.notify();

            return responses;
          }
        } catch (InterruptedException ex) {
            log.warn("interrupt in handleRequest(): " + ex.getMessage());
            interrupted = true;
            return null;
        }
    }

    /**
     * This method is thread-safe, except for 
     * an 'initial' call to nextRequest(null) followed by a
     * 'zombie' call to nextRequest(notNull). 
     * This case (per design) leads to a timeout of the original call.
     * (synchronizing the entire method does not 
     * hinder the zombie to interrupt two consecutive nextRequest() calls.)
     * 
     * @param responses
     * @return QUIT if expected responses are not provided
     */
    @Override
    public List<STALRequest> nextRequest(List<STALResponse> responses) {
      if (interrupted) {
        return null;
      }
        try {
          synchronized (respMon) {
            if (responses != null && responses.size() > 0) {
                if (!expectingResponse) {
                    log.warn("Received unexpected response in nextRequest(), return QUIT");
                    return Collections.singletonList((STALRequest) new QuitRequest());
                }
                long beforeWait = System.currentTimeMillis();
                while (respMon.responses != null) {
                    log.trace("waiting to produce response");
                    respMon.wait(timeout);
                    if (System.currentTimeMillis() - beforeWait >= timeout) {
                        log.warn("timeout while waiting to produce response");
                        return Collections.singletonList((STALRequest) new QuitRequest());
                    }
                }
                log.trace("produce response");
                respMon.produce(responses);
                //reset HashDataInputCallback iff SignResponse
                if (log.isTraceEnabled()) {
                    for (STALResponse response : responses) {
                        log.trace("Received STAL response: " + response.getClass().getName());
                    }
                }
                log.trace("notifying response consumers");
                respMon.notify();
            } else {
                if (expectingResponse) {
                    log.warn("Did not receive expected response(s) in nextRequest(), return QUIT");
                    return Collections.singletonList((STALRequest) new QuitRequest());
                }
                log.trace("expecting non-null response in next nextRequest(response)");
                expectingResponse = true;
            }
          }
          
          synchronized (reqMon) {
            long beforeWait = System.currentTimeMillis();
            while (reqMon.requests == null) {
                log.trace("waiting to consume request");
                reqMon.wait(timeout);
                if (System.currentTimeMillis() - beforeWait >= timeout) {
                    log.warn("timeout while waiting to consume request");
                    return Collections.singletonList((STALRequest) new QuitRequest());
                }
            }
            log.trace("consume request");
            List<STALRequest> requests = reqMon.consume();
            if (requests.size() > 0 && requests.get(0) instanceof QuitRequest) {
                log.trace("expecting no response in next nextRequest()");
                expectingResponse = false;
            }
            return requests;
          }
        } catch (InterruptedException ex) {
            log.warn("interrupt in nextRequest(): " + ex.getMessage());
            interrupted = true;
            return null;
        }
    }

    @Override
    public synchronized List<HashDataInput> getHashDataInput() {
        log.trace("return " + reqMon.hashDataInput.size() + " current HashDataInput(s) ");
        return reqMon.getHashDataInput();
    }
    
    @Override
    public void setLocale(Locale locale) {
    }
    
    class RequestsMonitor {
      List<STALRequest> requests;
      List<HashDataInput> hashDataInput;
      
      void produce(List<STALRequest> req) {
        requests = req;
      }
      
      synchronized List<STALRequest> consume() {
        List<STALRequest> reqs = requests;
        requests = null;
        return reqs;
      }
      
      void setHashDataInput(List<HashDataInput> hdi) {
        hashDataInput = hdi;
      }
      
      List<HashDataInput> getHashDataInput() {
        return hashDataInput;
      }
    }
    
    class ResponsesMonitor {
      List<STALResponse> responses;
      
      void produce(List<STALResponse> resp) {
        responses = resp;
      }
      
      synchronized List<STALResponse> consume() {
        List<STALResponse> resps = responses;
        responses = null;
        return resps;
      }
    }
}
