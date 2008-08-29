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
import at.gv.egiz.stal.QuitRequest;
import at.gv.egiz.stal.STALRequest;
import at.gv.egiz.stal.STALResponse;
import at.gv.egiz.stal.HashDataInputCallback;
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
//    protected RequestResponseBroker broker;   
    protected List<STALRequest> requests = null;
    protected List<STALResponse> responses = null;
    protected HashDataInputCallback currentHashDataInputCallback;
    private boolean isHandlingRequest = false;
    private boolean expectingResponse = false;
//    private Object handleRequestCondition = new Object();
//    private Object gotResponsesCondition = new Object();
//    public STALRequestBrokerImpl() {
//        broker = new RequestResponseBroker();
//        new Thread(handler).start();
//    }

//    @Override
//    public HashDataInputCallback getCurrentHashDataInputCallback() {
//        return broker.getCurrentHashDataInputCallback();
//    }
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
        while (isHandlingRequest) {
            log.trace("waiting to produce request");
            try {
                long beforeWait = System.currentTimeMillis();
                wait(TIMEOUT_MS);
                if (System.currentTimeMillis() - beforeWait >= TIMEOUT_MS) {
                    log.warn("timeout while waiting to produce request");
                    return Collections.singletonList((STALResponse) new ErrorResponse(ERR_6000));
                }
            } catch (InterruptedException ex) {
                log.warn("interrupt while waiting to produce request: " + ex.getMessage());
            }
        }
        log.trace("produce request");
        isHandlingRequest = true;

        this.requests = requests;
        currentHashDataInputCallback = null;
        for (STALRequest request : requests) {
            if (request instanceof SignRequest) {
                log.trace("Received SignRequest, keep HashDataInput callback.");
                currentHashDataInputCallback = ((SignRequest) request).getHashDataInput();
                break;
            } else if (request instanceof QuitRequest) {
                //alternative1:
                //for QUIT requests, do not wait for responses, but for request consumation
                // (i.e. set isHandlingReq to false once QUIT is consumed)
                log.trace("Received QuitRequest, do not wait for responses.");
                log.trace("notifying request consumers");
                notify();
                //alternative2:
                //wait for QUIT to be consumed
                // (i.e. notify me noce QUIT is consumed)
//                while (this.requests != null) {
//                    try {
//                        long beforeWait = System.currentTimeMillis();
//                        wait(TIMEOUT_MS);
//                        if (System.currentTimeMillis() - beforeWait >= TIMEOUT_MS) {
//                            log.warn("timeout while waiting for QUIT to be consumed");
//                            this.requests = null;
//                            isHandlingRequest = false;
//                            return Collections.singletonList((STALResponse) new ErrorResponse(ERR_6000));
//                        }
//                    } catch (InterruptedException ex) {
//                        log.warn("interrupt while waiting for QUIT to be consumed: " + ex.getMessage());
//                    }
//                }
//                isHandlingRequest = false;
                return new ArrayList<STALResponse>();
            } else if (log.isTraceEnabled()) {
                log.trace("Received STAL request: " + request.getClass().getName());
            }
        }
        log.trace("notifying request consumers");
        notify();

        while (this.responses == null) {
            log.trace("waiting to consume response");
            try {
                long beforeWait = System.currentTimeMillis();
                wait(TIMEOUT_MS);
                if (System.currentTimeMillis() - beforeWait >= TIMEOUT_MS) {
                    log.warn("timeout while waiting to consume response");
                    this.requests = null;
                    currentHashDataInputCallback = null;
                    isHandlingRequest = false;
                    return Collections.singletonList((STALResponse) new ErrorResponse(ERR_6000));
                }
            } catch (InterruptedException ex) {
                log.warn("interrupt while waiting to consume response: " + ex.getMessage());
            }
        }
        log.trace("consuming responses");
        List<STALResponse> resps = responses;
        responses = null;
        log.trace("notifying response producers");
        notify();

        isHandlingRequest = false;
        log.trace("notifying request producers");
        notify();

        return resps;
    }

    /**
     * 
     * @param responses
     * @return QUIT if expected responses are not provided
     */
    @Override
    public synchronized List<STALRequest> nextRequest(List<STALResponse> responses) {
        if (responses != null && responses.size() > 0) {
            if (!expectingResponse) {
                log.warn("Received unexpected response in nextRequest()");
                return Collections.singletonList((STALRequest) new QuitRequest());
            }
            while (this.responses != null) {
                log.trace("waiting to produce response");
                try {
                    long beforeWait = System.currentTimeMillis();
                    wait(TIMEOUT_MS);
                    if (System.currentTimeMillis() - beforeWait >= TIMEOUT_MS) {
                        log.warn("timeout while waiting to produce response");
                        return Collections.singletonList((STALRequest) new QuitRequest());
                    }
                } catch (InterruptedException ex) {
                    log.warn("interrupt while waiting to produce response: " + ex.getMessage());
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
                // while (expectingResponse) wait();
                log.warn("No expected response received in nextRequest()");
                return Collections.singletonList((STALRequest) new QuitRequest());
            }
            log.trace("expecting non-null response in next nextRequest(response)");
            expectingResponse = true;
        }
        while (this.requests == null) {
            log.trace("waiting to consume request");
            try {
                long beforeWait = System.currentTimeMillis();
                wait(TIMEOUT_MS);
                if (System.currentTimeMillis() - beforeWait >= TIMEOUT_MS) {
                    log.warn("timeout while waiting to consume request");
                    return Collections.singletonList((STALRequest) new QuitRequest());
                }
            } catch (InterruptedException ex) {
                log.warn("interrupt while waiting to consume request: " + ex.getMessage());
            }
        }
        log.trace("consume request");
        List<STALRequest> reqs = requests;
        //TODO check if QUIT and set isHandlingReq to false here? 
        // (rename isHandlingReq -> produce)
        // handleReq(QUIT) doesn't wait() and returns immediately
        // cf. handleReq(QUIT)
        requests = null;
        //no need to notify; request producer is waiting for isHandlingRequest
        //(alt2: the QUIT producer returned immediately and didn't notify)
        //(alt1: the QUIT producer is waiting for notification on QUIT consumption)
        if (reqs.size() > 0 && reqs.get(0) instanceof QuitRequest) {
            isHandlingRequest = false;
            log.trace("consumed QUIT, notifying request producers");
            notify();
            log.trace("expecting no response in next nextRequest()");
            expectingResponse = false;
            //notify no-response request consumers
        }
        return reqs;
    }

    @Override
    public synchronized HashDataInputCallback getHashDataInput() {
        log.trace("return current HashDataInput callback");
        return currentHashDataInputCallback;
    }
//    /**
//     * Causes the calling thread to sleep until response is passed via nextRequest()
//     * (except for QUIT request, which returns immediately).
//     * The requestList may contain at most one signRequest.
//     * The signRequest's signedRefCallback is stored until a response to the signRequest is provided (2nd nextRequest() call),
//     * i.e. until handleRequest() returns.
//     * 
//     * @param aRequestList
//     * @return
//     * @pre requestList contains at most one signRequest
//     */
//    @Override
//    public List<STALResponse> handleRequest(List<STALRequest> requestList) {
//        try {
//            if (log.isTraceEnabled()) {
//                log.trace("HandleRequest (" + requestList.size() + " requests)");
//            }
//
//            broker.produceRequests(requestList);
//
//            // QUIT returns immediately
//            if (requestList.size() == 1 && requestList.get(0) instanceof QuitRequest) {
//                log.trace("Received QUIT request, do not wait for responses.");
//                return new ArrayList<STALResponse>();
//            }
//            return broker.consumeResponses();
//        } catch (InterruptedException ex) {
//            log.error("Interrupted while handling STAL request list: " + ex.getMessage());
//            return Collections.singletonList((STALResponse) new ErrorResponse());
//        } catch (TimeoutException ex) {
//            log.error("Timeout during handle request: " + ex.getMessage());
//            ErrorResponse err = new ErrorResponse();
//            err.setErrorCode(ERR_6000);
//            return Collections.singletonList((STALResponse) err);
//        }
//    }
//
//    @Override
//    public void setResponse(List<STALResponse> responses) {
//        try {
////        if (responses != null && responses.size() > 0) {
////            List<STALResponse> stalResponses = translateResponses(responses);
//            broker.produceResponses(responses);
////        } else {
////            log.trace("Received emtpy responses list, do not add.");
////        }
//        } catch (InterruptedException ex) {
//            log.error("Interrupted while setting STAL response: " + ex.getMessage());
////            broker.interrupt(new ErrorResponse());
//        } catch (TimeoutException ex) {
//            log.error("Timeout during setResponse: " + ex.getMessage());
//        }
//    }
//
//    /**
//     * TODO split in nextRequest(void) and setResponses(responses)
//     * <br/>
//     * Translate (possibly empty) STAL-WS response list to STAL responses and
//     * wait until request(s) are available and translate to STAL-WS requests.
//     * @param prevResponse if null or zero-length, they are not passed to the handler
//     * @return
//     */
//    @Override
//    public List<STALRequest> nextRequest() { //List<ResponseType> responses) {
//        try {
////            if (responses != null && responses.size() > 0) {
////                List<STALResponse> stalResponses = translateResponses(responses);
////                broker.produceResponses(stalResponses);
////            } else {
////                log.trace("Received emtpy responses list, do not add.");
////            }
//
////            List<? extends STALRequest> stalRequests = broker.consumeRequests();
////            List<RequestType> requests = translateRequests(stalRequests);
//            return broker.consumeRequests();
////        } catch (InterruptedException ex) {
////            log.error("Interrupted while requesting next STAL request: " + ex.getMessage());
////            return Collections.singletonList((STALResponse) new ErrorResponse());
//        } catch (InterruptedException ex) {
//            log.error("Interrupted while requesting next STAL request: " + ex.getMessage());
////            broker.interrupt(new ErrorResponse());
//            return new ArrayList<STALRequest>();
//        } catch (TimeoutException ex) {
//            log.error("Timeout during nextRequest: " + ex.getMessage());
//            return new ArrayList<STALRequest>();
//        }
//    }
//
////    @Override
////    public void interruptRequestHandling(ErrorResponseType error) {
////        if (log.isTraceEnabled()) {
////            log.trace("Received Error: " + error.getErrorMessage());
////        }
////        broker.interrupt(new ErrorResponse(error.getErrorCode()));
////    }
//
//    //TODO
////    private List<RequestType> translateRequests(List<? extends STALRequest> stalRequests) {
////        List<RequestType> requests = new ArrayList<RequestType>(stalRequests.size());
////        for (STALRequest stalRequest : stalRequests) {
////            if (stalRequest instanceof InfoboxReadRequest) {
////                InfoboxReadRequestType req = new InfoboxReadRequestType();
////                req.setInfoboxIdentifier(((InfoboxReadRequest) stalRequest).getInfoboxIdentifier());
////                log.warn("TODO consider domain identifier for infobox " + req.getInfoboxIdentifier());
////                req.setDomainIdentifier("TODO");
////                requests.add(req);
////            } else if (stalRequest instanceof SignRequest) {
////                //TODO
////                //remember current sign request for getSignedReferences()
////                throw new UnsupportedOperationException("SignRequest unsupported");
////            } else if (stalRequest instanceof QuitRequest) {
////                requests.add(new QuitRequestType());
////            } else {
////                log.error("Unknown STAL request: " + stalRequest.getClass().getName());
////            }
////        }
////        return requests;
////    }
//
////    private List<STALResponse> translateResponses(List<ResponseType> responses) {
////        List<STALResponse> stalResponses = new ArrayList<STALResponse>(responses.size());
////        for (ResponseType response : responses) {
////            if (response instanceof InfoboxReadResponseType) {
////                byte[] infoboxValue = ((InfoboxReadResponseType) response).getInfoboxValue();
////                stalResponses.add(new InfoboxReadResponse(infoboxValue));
////            } else if (response instanceof SignResponseType) {
////                byte[] signatureValue = ((SignResponseType) response).getSignatureValue();
////                stalResponses.add(new SignResponse(signatureValue));
////            } else if (response instanceof ErrorResponseType) {
////                int errorCode = ((ErrorResponseType) response).getErrorCode();
////                log.warn("TODO consider error msg: " + ((ErrorResponseType) response).getErrorMessage());
////                stalResponses.add(new ErrorResponse(errorCode));
////            } else {
////                log.error("Unknown STAL service response " + response.getId() + ": " + response.getClass().getName());
////            }
////        }
////        return stalResponses;
////    }
//    /**
//     * synchronize on this, not on request/response lists since they are nulled
//     */
//    // protected since outer handler field is protected 
//    protected class RequestResponseBroker { //implements Runnable {
//
//        protected List<STALRequest> requests = null;
//        protected List<STALResponse> responses = null;
//        protected HashDataInputCallback currentHashDataInputCallback;
//
////        @Override
////        public void run() {
////            while (true) {
////                ;
////            }
////            //TODO handler lifecycle in run()?
////        }
//        /**
//         * wait until requests are consumed,
//         * produce requests, remember sigRefCallback and notify consumer
//         * (no need for synchronized?)
//         * @param requests 
//         */
//        public synchronized void produceRequests(List<STALRequest> requests) throws InterruptedException, TimeoutException {
////            synchronized (requests) {
//
//            // requests is null, since there's only one producer thread calling handleRequests()
//            // and handleRequest() returns only if nextRequest() was called
//            while (this.requests != null) {
////                    requests.wait();
//                long before = System.currentTimeMillis();
//                log.trace("waiting to produce requests ...");
//                wait(); //TIMEOUT_MS);
//                if (System.currentTimeMillis() - before >= TIMEOUT_MS) {
//                    log.error("Timeout while waiting to produce requests.");
//                    throw new TimeoutException();
//                }
//            }
//            log.trace("producing requests");
//            this.requests = requests;
//            // getSignedReferences does not produce responses, 
//            // so the command thread will not continue (and no further signRequest can possibly be produced)
//            // once the ws-client sends nextRequest with responses to the signRequest, the callback is invalidated
//
//            // reset callback if for some reason produceResponse() wasn't called
//            currentHashDataInputCallback = null;
//            for (STALRequest request : requests) {
//                if (request instanceof SignRequest) {
//                    log.trace("keep hashdatainput callback");
//                    currentHashDataInputCallback = ((SignRequest) request).getHashDataInput();
//                    break;
//                }
//            }
//
////                requests.notify();
//            log.trace("notifying request consumers (TODO not only consumers)");
//            notify();
////            }
//        }
//
//        /**
//         * wait until requests are produced and consume them
//         * @return
//         */
//        public synchronized List<STALRequest> consumeRequests() throws InterruptedException, TimeoutException {
//            List<STALRequest> retVal = null;
////            synchronized (requests) {
//            while (requests == null) {
////                    requests.wait();
//                long before = System.currentTimeMillis();
//                log.trace("waiting to consumer requests ...");
//                wait(); //TIMEOUT_MS);
//                if (System.currentTimeMillis() - before >= TIMEOUT_MS) {
//                    log.error("Timeout while waiting to consume requests.");
//                    throw new TimeoutException();
//                }
//            }
//            log.trace("consuming requests");
//            retVal = requests;
//            requests = null;
////            }
//            log.trace("???notify request producers???");
//            return retVal;
//        }
//
//        /**
//         * wait until previous responses are consumed,
//         * produce responses and notify consumer
//         * @param responses
//         */
//        public synchronized void produceResponses(List<STALResponse> responses) throws InterruptedException, TimeoutException {
////            synchronized (responses) {
//            while (this.responses != null) {
////                    responses.wait();
//                long before = System.currentTimeMillis();
//                log.trace("waiting to produce responses ...");
//                wait(); //TIMEOUT_MS);
//                if (System.currentTimeMillis() - before >= TIMEOUT_MS) {
//                    log.error("Timeout while waiting to produce responses.");
//                    throw new TimeoutException();
//                }
//            }
//            log.trace("producing responses");
//            this.responses = responses;
//            //invalidate sigrefcallback (from now on handleRequest() may be called, producing new requests)
//            //make sure the provided responses are for the corresponding signrequest
//            if (this.requests == null) {//requests already consumed=>responses correspond to these
//                log.trace("resetting current hashdatainput");
//                currentHashDataInputCallback = null;
//            }
////                responses.notify();
//            log.trace("notify response consumers (TODO only consumers?)");
//            notify();
////            }
//        }
//
//        /**
//         * wait until responses are available, consume them
//         * @return
//         * @throws java.lang.Exception
//         */
//        public synchronized List<STALResponse> consumeResponses() throws InterruptedException, TimeoutException {
//            List<STALResponse> retVal = null;
////            synchronized (responses) {
//            while (responses == null) {
////                    responses.wait();
//                long before = System.currentTimeMillis();
//                log.trace("waiting to consume responses ...");
//                wait(); //TIMEOUT_MS);
//                if (System.currentTimeMillis() - before >= TIMEOUT_MS) {
//                    log.error("Timeout while waiting to consume responses.");
//                    throw new TimeoutException();
//                }
//            }
//            log.trace("consuming responses");
//            retVal = responses;
//            responses = null;
////            }
//            log.trace("???notify response producers???");
//            return retVal;
//        }
//
//        /**
//         * get the signrefcallback until handleRequest() is called the next time.
//         * @return null if last request was not a signRequest
//         */
//        public synchronized HashDataInputCallback getCurrentHashDataInputCallback() {
//            log.trace("obtain current hashdatainput");
//            return currentHashDataInputCallback;
//        }
//        /**
//         * add the error to responses and notify (response-) consumers
//         * @param error
//         */
////        public synchronized void interrupt(ErrorResponse error) {
//////            synchronized (responses) {
////            if (responses == null) {
////                responses = Collections.singletonList((STALResponse) error);
////            } else {
////                responses.add(error);
////            }
//////                responses.notify();
////            notify();
//////            }
////        }
//    }
    @Override
    public void setLocale(Locale locale) {
        // TODO Auto-generated method stub
    }
}
