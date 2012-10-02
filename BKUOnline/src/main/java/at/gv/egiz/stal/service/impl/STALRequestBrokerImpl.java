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



package at.gv.egiz.stal.service.impl;

import at.gv.egiz.stal.ErrorResponse;
import at.gv.egiz.stal.HashDataInput;
import at.gv.egiz.stal.QuitRequest;
import at.gv.egiz.stal.STALRequest;
import at.gv.egiz.stal.STALResponse;
import at.gv.egiz.stal.SignRequest;
import at.gv.egiz.stal.service.translator.STALTranslator;
import at.gv.egiz.stal.service.translator.TranslationException;
import at.gv.egiz.stal.service.types.ObjectFactory;
import at.gv.egiz.stal.service.types.QuitRequestType;
import at.gv.egiz.stal.service.types.RequestType;
import at.gv.egiz.stal.service.types.ResponseType;
import at.gv.egiz.stalx.service.translator.STALXTranslationHandler;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.JAXBElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private final Logger log = LoggerFactory.getLogger(STALRequestBrokerImpl.class);

    private ObjectFactory of = new ObjectFactory();
    private STALTranslator translator = new STALTranslator();
    
    private boolean interrupted = false;
    
    protected final ArrayList<JAXBElement<? extends RequestType>> requests;
    protected final ArrayList<JAXBElement<? extends ResponseType>> responses;

    protected ArrayList<HashDataInput> hashDataInputs;
    
    private long timeout;

    public STALRequestBrokerImpl(long timeoutMillisec) {
      if (timeoutMillisec <= 0) 
        timeoutMillisec = DEFAULT_TIMEOUT_MS;
      timeout = timeoutMillisec;
      requests = new ArrayList<JAXBElement<? extends RequestType>>();
      responses = new ArrayList<JAXBElement<? extends ResponseType>>();
      hashDataInputs = new ArrayList<HashDataInput>();

      // register handler for STAL-X
      translator.registerTranslationHandler(new STALXTranslationHandler());
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
            
            for (STALRequest stalRequest : stalRequests) {
              try {
                JAXBElement<? extends RequestType> request = translator.translate(stalRequest);
                requests.add(request);
                if (stalRequest instanceof SignRequest) {
                  //TODO refactor SignRequestType to keep HDI
                  // and getHashDataInput() accesses request obj
                  // (requests are cleared only when we receive the response)
                  // DataObjectHashDataInput with reference caching enabled DataObject
                  hashDataInputs.addAll(((SignRequest) stalRequest).getHashDataInput());
                } else if (stalRequest instanceof QuitRequest) {
                  log.trace("Received QuitRequest, do not wait for responses.");
                  log.trace("notifying request consumers");
                  requests.notify();
                  return new ArrayList<STALResponse>();
                }
              } catch (TranslationException ex) {
                log.error(ex.getMessage() + ", send QUIT");
                requests.clear();
                QuitRequestType reqT = of.createQuitRequestType();
                JAXBElement<QuitRequestType> req = of.createGetNextRequestResponseTypeQuitRequest(reqT);
                requests.add(req);
                log.trace("notifying request consumers");
                requests.notify();
                return new ArrayList<STALResponse>();
              }
            }
              

//                if (stalRequest instanceof SignRequest) {
//                  log.trace("Received SignRequest, keep HashDataInput.");
//                  SignRequestType reqT = of.createSignRequestType();
//                  reqT.setKeyIdentifier(((SignRequest) stalRequest).getKeyIdentifier());
//                  reqT.setSignedInfo(((SignRequest) stalRequest).getSignedInfo());
//                  JAXBElement<SignRequestType> req = of.createGetNextRequestResponseTypeSignRequest(reqT);
//                  requests.add(req);
//                  //DataObjectHashDataInput with reference caching enabled DataObject
//                  hashDataInputs.addAll(((SignRequest) stalRequest).getHashDataInput());
//                  break;
//                } else if (stalRequest instanceof InfoboxReadRequest) {
//                  log.trace("Received InfoboxReadRequest");
//                  InfoboxReadRequestType reqT = new InfoboxReadRequestType();
//                  reqT.setInfoboxIdentifier(((InfoboxReadRequest) stalRequest).getInfoboxIdentifier());
//                  reqT.setDomainIdentifier(((InfoboxReadRequest) stalRequest).getDomainIdentifier());
//                  JAXBElement<InfoboxReadRequestType> req = of.createGetNextRequestResponseTypeInfoboxReadRequest(reqT);
//                  requests.add(req);
//                } else if (stalRequest instanceof QuitRequest) {
//                  log.trace("Received QuitRequest, do not wait for responses.");
//                  QuitRequestType reqT = of.createQuitRequestType();
//                  JAXBElement<QuitRequestType> req = of.createGetNextRequestResponseTypeQuitRequest(reqT);
//                  requests.add(req);
//                  log.trace("notifying request consumers");
//                  requests.notify();
//                  return new ArrayList<STALResponse>();
//                } else {
//                  log.error("Received unsupported STAL request: " + stalRequest.getClass().getName() + ", send QUIT");
//                  requests.clear();
//                  QuitRequestType reqT = of.createQuitRequestType();
//                  JAXBElement<QuitRequestType> req = of.createGetNextRequestResponseTypeQuitRequest(reqT);
//                  requests.add(req);
//                  log.trace("notifying request consumers");
//                  requests.notify();
//                  return new ArrayList<STALResponse>();
//                }
//            }
            log.trace("notifying request consumers");
            requests.notify();
          }
          
          synchronized (responses) { 
            long beforeWait = System.currentTimeMillis();
            while (responses.isEmpty()) {
                log.trace("waiting to consume response");
                responses.wait(timeout);
                if (System.currentTimeMillis() - beforeWait >= timeout) {
                    log.warn("Timeout while waiting to consume response, cleanup requests.");
                    requests.clear(); 
                    hashDataInputs.clear();
                    ErrorResponse err = new ErrorResponse(ERR_4500);
                    err.setErrorMessage("Timeout while waiting to consume response");
                    return Collections.singletonList((STALResponse) err);
                }
            }
            log.trace("consuming responses");
            List<STALResponse> stalResponses = new ArrayList<STALResponse>();
            try {
              for (JAXBElement<? extends ResponseType> resp : responses) {
                  STALResponse stalResp = translator.translateWSResponse(resp);
                  stalResponses.add(stalResp);
              }
            } catch (TranslationException ex) {
              log.error(ex.getMessage() + ", return ErrorResponse (4000)");
              ErrorResponse stalResp = new ErrorResponse(4000);
              stalResp.setErrorMessage(ex.getMessage());
              stalResponses = Collections.singletonList((STALResponse) stalResp);
            }

            responses.clear();
            log.trace("notifying response producers");
            responses.notify();

            return stalResponses;
          }
        } catch (InterruptedException ex) {
            log.warn("interrupt in handleRequest(): " + ex.getMessage());
            interrupted = true;
            return null;
        }
    }

    @Override
    public List<JAXBElement<? extends RequestType>> connect() {
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
                    log.warn("Timeout while waiting to consume request.");
                    return createSingleQuitRequest();
                }
            }
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
    public List<JAXBElement<? extends RequestType>> nextRequest(List<JAXBElement<? extends ResponseType>> resps) {
      if (interrupted) {
        return null;
      }
        try {
          synchronized (requests) {
            log.trace("Received responses, now consume request.");
            if (requests.size() != 0) {
              requests.clear();
            } else {
              log.warn("Requests queue is empty, response might have already been produced previously.");
              // return QUIT?
            }
          }
          
          synchronized (responses) { 
            if (resps != null && resps.size() > 0) {
                long beforeWait = System.currentTimeMillis();
                while (!responses.isEmpty()) {
                    log.trace("waiting to produce response");
                    responses.wait(timeout);
                    if (System.currentTimeMillis() - beforeWait >= timeout) {
                        log.warn("Timeout while waiting to produce response.");
                        return createSingleQuitRequest();
                    }
                }
                log.trace("produce response");
                responses.addAll(resps);
                //reset HashDataInputCallback iff SignResponse
                if (log.isTraceEnabled()) {
                    for (JAXBElement<? extends ResponseType> response : resps) {
                        log.trace("Received STAL response: " + response.getValue().getClass().getName());
                    }
                }
                log.trace("notifying response consumers");
                responses.notify();
            } else {
              log.error("Received NextRequest without responses, return QUIT.");
              return createSingleQuitRequest();
            }
          }
          
          synchronized (requests) { 
            long beforeWait = System.currentTimeMillis();
            while (requests.isEmpty()) {
                log.trace("waiting to consume request");
                requests.wait(timeout);
                if (System.currentTimeMillis() - beforeWait >= timeout) {
                    log.warn("Timeout while waiting to consume request.");
                    return createSingleQuitRequest();
                }
            }
            log.trace("Don't consume request now, but on next response delivery.");
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
        log.trace("Return {} current HashDataInput(s).", hashDataInputs.size());
        return hashDataInputs;
      }
    }
    
    private List<JAXBElement<? extends RequestType>> createSingleQuitRequest() {
      QuitRequestType quitT = of.createQuitRequestType();
      JAXBElement<QuitRequestType> quit = of.createGetNextRequestResponseTypeQuitRequest(quitT);
      ArrayList<JAXBElement<? extends RequestType>> l = new ArrayList<JAXBElement<? extends RequestType>>();
      l.add(quit);
      return l;
    }
}
