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

package at.gv.egiz.stal.service.impl;

import at.gv.egiz.stal.ErrorResponse;
import at.gv.egiz.stal.HashDataInput;
import at.gv.egiz.stal.InfoboxReadRequest;
import at.gv.egiz.stal.QuitRequest;
import at.gv.egiz.stal.STALRequest;
import at.gv.egiz.stal.STALResponse;
import at.gv.egiz.stal.SignRequest;
import at.gv.egiz.stal.service.types.InfoboxReadRequestType;
import at.gv.egiz.stal.service.types.QuitRequestType;
import at.gv.egiz.stal.service.types.RequestType;
import at.gv.egiz.stal.service.types.ResponseType;
import at.gv.egiz.stal.service.types.SignRequestType;
import at.gv.egiz.stal.util.STALTranslator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * An instance of STALRequestBroker is shared between a producer thread (SLCommand)
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

//    private boolean expectingResponse = false;
    private boolean interrupted = false;
    
//    private final RequestsMonitor reqMon = new RequestsMonitor();
//    private final ResponsesMonitor respMon = new ResponsesMonitor();

    protected ArrayList<RequestType> requests;
    protected ArrayList<ResponseType> responses;

    protected ArrayList<HashDataInput> hashDataInputs;
    
    private long timeout;

    public STALRequestBrokerImpl(long timeoutMillisec) {
      if (timeoutMillisec <= 0) 
        timeoutMillisec = DEFAULT_TIMEOUT_MS;
      timeout = timeoutMillisec;
      requests = new ArrayList<RequestType>();
      responses = new ArrayList<ResponseType>();
      hashDataInputs = new ArrayList<HashDataInput>();
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
    public List<STALResponse> handleRequest(List<? extends STALRequest> stalRequests) {
      if (interrupted) {
        return null;
      }
        try {
          synchronized (requests) {
            log.trace("produce request");

            requests.clear();
            hashDataInputs.clear();
//            reqMon.produce(requests);
//            reqMon.setHashDataInput(null);
            
            for (STALRequest stalRequest : stalRequests) {
                if (stalRequest instanceof SignRequest) {
                  log.trace("Received SignRequest, keep HashDataInput.");
                  SignRequestType req = new SignRequestType();
                  req.setKeyIdentifier(((SignRequest) stalRequest).getKeyIdentifier());
                  req.setSignedInfo(((SignRequest) stalRequest).getSignedInfo());
                  requests.add(req);
                  //DataObjectHashDataInput with reference caching enabled DataObject 
                  hashDataInputs.addAll(((SignRequest) stalRequest).getHashDataInput());
                  break;
                } else if (stalRequest instanceof InfoboxReadRequest) {
                  log.trace("Received InfoboxReadRequest");
                  InfoboxReadRequestType req = new InfoboxReadRequestType();
                  req.setInfoboxIdentifier(((InfoboxReadRequest) stalRequest).getInfoboxIdentifier());
                  req.setDomainIdentifier(((InfoboxReadRequest) stalRequest).getDomainIdentifier());
                  requests.add(req);
                } else if (stalRequest instanceof QuitRequest) {
                  log.trace("Received QuitRequest, do not wait for responses.");
                  requests.add(new QuitRequestType());
                  log.trace("notifying request consumers");
                  requests.notify();
//                    reqMon.notify();
                  return new ArrayList<STALResponse>();
                } else {
                  log.error("Received unsupported STAL request: " + stalRequest.getClass().getName() + ", send QUIT");
                  requests.clear();
                  requests.add(new QuitRequestType());
                  log.trace("notifying request consumers");
                  requests.notify();
                  return new ArrayList<STALResponse>();
                }
            }
            log.trace("notifying request consumers");
            requests.notify();
//            reqMon.notify();
          }
          
          synchronized (responses) { //respMon) {
            long beforeWait = System.currentTimeMillis();
//            while (respMon.responses == null) {
            while (responses.isEmpty()) {
                log.trace("waiting to consume response");
//                respMon.wait(timeout);
                responses.wait(timeout);
                if (System.currentTimeMillis() - beforeWait >= timeout) {
                    log.warn("timeout while waiting to consume response, cleanup requests");
//                    reqMon.consume(); //TODO check deadlock?
//                    reqMon.setHashDataInput(null);
                    requests.clear(); //TODO sync on requests?
                    hashDataInputs.clear();
                    return Collections.singletonList((STALResponse) new ErrorResponse(ERR_4500));
                }
            }
            log.trace("consuming responses");
//            List<STALResponse> responses = respMon.consume();
            List<STALResponse> resps = STALTranslator.toSTAL(responses);
            responses.clear();
            log.trace("notifying response producers");
            responses.notify();
//            respMon.notify();

            return resps;
          }
        } catch (InterruptedException ex) {
            log.warn("interrupt in handleRequest(): " + ex.getMessage());
            interrupted = true;
            return null;
        }
    }

    @Override
    public List<RequestType> connect() {
      if (interrupted) {
        return null;
      }
        try {
          synchronized (requests) {
            long beforeWait = System.currentTimeMillis();
            while (requests.isEmpty()) {
                log.trace("waiting to consume request");
                requests.wait(timeout);
                if (System.currentTimeMillis() - beforeWait >= timeout) {
                    log.warn("timeout while waiting to consume request");
                    return Collections.singletonList((RequestType) new QuitRequestType());
                }
            }
            
//            log.trace("consume request");
//            List<RequestType> reqs = new ArrayList<RequestType>(); 
//            reqs.addAll(requests);
//            requests.clear();
//            return reqs;
            log.trace("don't consume request now, leave for further connect calls");
            return requests;
          }
        } catch (InterruptedException ex) {
            log.warn("interrupt in nextRequest(): " + ex.getMessage());
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
    public List<RequestType> nextRequest(List<ResponseType> resps) {
      if (interrupted) {
        return null;
      }
        try {
          synchronized (requests) {
            log.trace("received responses, now consume request");
            if (requests.size() != 0) {
              requests.clear();
            } else {
              log.warn("requests queue is empty, response might have already been produced previously ");
              // return QUIT?
            }
          }
          
          synchronized (responses) { //respMon) {
            if (resps != null && resps.size() > 0) {
//                if (!expectingResponse) {
//                    log.warn("Received unexpected response in nextRequest(), return QUIT");
//                    return Collections.singletonList((RequestType) new QuitRequestType());
//                }
                long beforeWait = System.currentTimeMillis();
//                while (respMon.responses != null) {
                while (!responses.isEmpty()) {
                    log.trace("waiting to produce response");
//                    respMon.wait(timeout);
                    responses.wait(timeout);
                    if (System.currentTimeMillis() - beforeWait >= timeout) {
                        log.warn("timeout while waiting to produce response");
                        return Collections.singletonList((RequestType) new QuitRequestType());
                    }
                }
                log.trace("produce response");
//                respMon.produce(resps);
                responses.addAll(resps);
                //reset HashDataInputCallback iff SignResponse
                if (log.isTraceEnabled()) {
                    for (ResponseType response : resps) {
                        log.trace("Received STAL response: " + response.getClass().getName());
                    }
                }
                log.trace("notifying response consumers");
//                respMon.notify();
                responses.notify();
            } else {
//                if (expectingResponse) {
//                    log.warn("Did not receive expected response(s) in nextRequest(), return QUIT");
//                    return Collections.singletonList((RequestType) new QuitRequestType());
//                }
//                log.trace("expecting non-null response in next nextRequest(response)");
//                expectingResponse = true;
              log.error("Received NextRequest without responses, return QUIT");
              return Collections.singletonList((RequestType) new QuitRequestType());
            }
          }
          
          synchronized (requests) { //reqMon) {
            long beforeWait = System.currentTimeMillis();
//            while (reqMon.requests == null) {
            while (requests.isEmpty()) {
                log.trace("waiting to consume request");
//                reqMon.wait(timeout);
                requests.wait(timeout);
                if (System.currentTimeMillis() - beforeWait >= timeout) {
                    log.warn("timeout while waiting to consume request");
                    return Collections.singletonList((RequestType) new QuitRequestType());
                }
            }
//            log.trace("consume request");
//            List<RequestType> reqs = new ArrayList<RequestType>(); // reqMon.consume();
//            reqs.addAll(requests);
//            
////            if (requests.size() > 0 && requests.get(0) instanceof QuitRequestType) {
////                log.trace("expecting no response in next nextRequest()");
////                expectingResponse = false;
////            }
//            requests.clear();
//            return reqs;
            log.trace("don't consume request now, but on next response delivery");
            return requests;
          }
        } catch (InterruptedException ex) {
            log.warn("interrupt in nextRequest(): " + ex.getMessage());
            interrupted = true;
            return null;
        }
    }

    @Override
    public List<HashDataInput> getHashDataInput() {
      synchronized (requests) {
        log.trace("return " + hashDataInputs.size() + " current HashDataInput(s) ");
        return hashDataInputs; //reqMon.getHashDataInput();
      }
    }
    
    @Override
    public void setLocale(Locale locale) {
    }
    
//    class RequestsMonitor {
//      List<STALRequest> requests;
//      List<HashDataInput> hashDataInput;
//      
//      void produce(List<STALRequest> req) {
//        requests = req;
//      }
//      
//      synchronized List<at.gv.egiz.stal.service.types.STALRequest> consume() {
//        List<at.gv.egiz.stal.service.types.STALRequest> reqs = new ArrayList<at.gv.egiz.stal.service.types.STALRequest>();
//        for (STALRequest request : requests) {
//          if (request instanceof SignRequest) {
//            at.gv.egiz.stal.service.types.SignRequest r = new at.gv.egiz.stal.service.types.SignRequest();
//            r.setKeyIdentifier(((SignRequest) request).getKeyIdentifier());
//            r.setSignedInfo(((SignRequest) request).getSignedInfo());
//            reqs.add(r);
//          } else if (request instanceof InfoboxReadRequest) {
//            at.gv.egiz.stal.service.types.InfoboxReadRequest r = new at.gv.egiz.stal.service.types.InfoboxReadRequest();
//            r.setDomainIdentifier(((InfoboxReadRequest) request).getDomainIdentifier());
//            r.setInfoboxIdentifier(((InfoboxReadRequest) request).getInfoboxIdentifier());
//            reqs.add(r);
//          } else if (request instanceof QuitRequest) {
//            at.gv.egiz.stal.service.types.QuitRequest r = new at.gv.egiz.stal.service.types.QuitRequest();
//            reqs.add(r);
//          } else {
//            log.error("unknown STAL request type: " + request.getClass());
//            requests = null;
//            return Collections.singletonList((at.gv.egiz.stal.service.types.STALRequest) new at.gv.egiz.stal.service.types.QuitRequest());
//          }
//        }
//        requests = null;
//        return reqs;
//      }
//      
//      void setHashDataInput(List<HashDataInput> hdi) {
//        hashDataInput = hdi;
//      }
//      
//      List<HashDataInput> getHashDataInput() {
//        return hashDataInput;
//      }
//    }
//    
//    /** TODO: now, that responses are not nulled, synchronize directly on responses? */
//    class ResponsesMonitor {
//      List<at.gv.egiz.stal.service.types.STALResponse> responses;
//      
//      void produce(List<at.gv.egiz.stal.service.types.STALResponse> resp) {
//        responses = resp;
//      }
//      
//      synchronized List<STALResponse> consume() {
//        List<STALResponse> resps = new ArrayList<STALResponse>();
//        
//        for (at.gv.egiz.stal.service.types.STALResponse response : responses) {
//          if (response instanceof at.gv.egiz.stal.service.types.InfoboxReadResponse) {
//            InfoboxReadResponse r = new InfoboxReadResponse();
//            r.setInfoboxValue(((at.gv.egiz.stal.service.types.InfoboxReadResponse) response).getInfoboxValue());
//            resps.add(r);
//          } else if (response instanceof at.gv.egiz.stal.service.types.SignResponse) {
//            SignResponse r = new SignResponse();
//            r.setSignatureValue(((at.gv.egiz.stal.service.types.SignResponse) response).getSignatureValue());
//            resps.add(r);
//          } else if (response instanceof at.gv.egiz.stal.service.types.ErrorResponse) {
//            ErrorResponse r = new ErrorResponse();
//            r.setErrorCode(((at.gv.egiz.stal.service.types.ErrorResponse) response).getErrorCode());
//            r.setErrorMessage(((at.gv.egiz.stal.service.types.ErrorResponse) response).getErrorMessage());
//            resps.add(r);
//          } else {
//            log.error("unknown STAL response type: " + response.getClass());
//            ErrorResponse r = new ErrorResponse(4000);
//            r.setErrorMessage("unknown STAL response type: " + response.getClass());
//            responses = null;
//            return Collections.singletonList((STALResponse) r);
//          }
//        }
//        responses = null;
//        return resps;
//      }
//    }
}
