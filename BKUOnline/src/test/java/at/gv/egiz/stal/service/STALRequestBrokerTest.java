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


/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package at.gv.egiz.stal.service;

import at.gv.egiz.stal.service.impl.STALRequestBrokerImpl;
import at.gv.egiz.stal.service.impl.RequestBrokerSTALFactory;
import at.gv.egiz.stal.service.impl.STALRequestBroker;
import at.gv.egiz.stal.ErrorResponse;
import at.gv.egiz.stal.HashDataInput;
import at.gv.egiz.stal.InfoboxReadRequest;
import at.gv.egiz.stal.InfoboxReadResponse;
import at.gv.egiz.stal.QuitRequest;
import at.gv.egiz.stal.STALRequest;
import at.gv.egiz.stal.STALResponse;
import at.gv.egiz.stal.SignResponse;
import at.gv.egiz.stal.SignRequest;
import at.gv.egiz.stal.service.types.InfoboxReadRequestType;
import at.gv.egiz.stal.service.types.InfoboxReadResponseType;
import at.gv.egiz.stal.service.types.ObjectFactory;
import at.gv.egiz.stal.service.types.QuitRequestType;
import at.gv.egiz.stal.service.types.RequestType;
import at.gv.egiz.stal.service.types.ResponseType;
import at.gv.egiz.stal.service.types.SignRequestType;
import at.gv.egiz.stal.service.types.SignResponseType;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.JAXBElement;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

/**
 *
 * @author clemens
 */
@Ignore
public class STALRequestBrokerTest {

    private final Logger log = LoggerFactory.getLogger(STALRequestBrokerTest.class);
    protected static STALRequestBroker stal;

    @BeforeClass
    public static void setUp() {
        Logger log = LoggerFactory.getLogger(STALRequestBrokerTest.class);
        RequestBrokerSTALFactory fac = new RequestBrokerSTALFactory();
        stal = (STALRequestBrokerImpl) fac.createSTAL();
        log.debug("Created STAL " + stal.getClass().getName());
    }

    @Ignore
    public void testInfoboxRead() {
//        try {

        log.debug("*************** test ReadInfoboxRequest for two infoboxes");
        List<STALRequest> requests = new ArrayList<STALRequest>();
        InfoboxReadRequest r1 = new InfoboxReadRequest();
        r1.setInfoboxIdentifier("infobox1");
        requests.add(r1);
        InfoboxReadRequest r2 = new InfoboxReadRequest();
        r2.setInfoboxIdentifier("infobox2");
        requests.add(r2);

        BindingProcessorSimulator bp = new BindingProcessorSimulator();
        bp.setRequests(Collections.singletonList(requests));

        new Thread(new ServiceSimulator(), "STALService1").start();
        new Thread(bp, "BindingProcessor").start();
        new Thread(new ServiceSimulator(), "STALService2").start();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            log.error("interrupted: " + ex.getMessage());
        }
    }

    @Test
    public void testSign() {
        log.debug("**************** test SignRequest");
        List<STALRequest> requests = new ArrayList<STALRequest>();
        SignRequest r1 = new SignRequest();
        r1.setKeyIdentifier("keybox1");
        SignRequest.SignedInfo si1 = new SignRequest.SignedInfo();
        si1.setValue("1234".getBytes());
        r1.setSignedInfo(si1);
        HashDataInput hdi = new HashDataInput() {

            @Override
            public String getReferenceId() {
                return "refId1234";
            }

            @Override
            public String getMimeType() {
                return "text/plain";
            }

            @Override
            public InputStream getHashDataInput() {
                return new ByteArrayInputStream("hashdatainput1234".getBytes());
            }
            @Override
            public String getEncoding() {
              return "UTF-8";
            }


            @Override
            public String getFilename() {
                return "file.txt";
            }
        };
        r1.setHashDataInput(Collections.singletonList(hdi));
        requests.add(r1);

        BindingProcessorSimulator bp = new BindingProcessorSimulator();
        bp.setRequests(Collections.singletonList(requests));

        new Thread(bp, "BindingProcessor").start();
//        new Thread(bp2, "BindingProcessor2").start();
        new Thread(new ServiceSimulator(), "STALService").start();
        new Thread(new ZombieServiceSimulator(), "STALServiceZombie").start();
        
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            log.error("interrupted: " + ex.getMessage());
        }
    }

    @Ignore
    public void testResponseTimeout() {
        log.debug("**************** test SignRequest with responses timeout");
        List<STALRequest> requests = new ArrayList<STALRequest>();
        SignRequest r1 = new SignRequest();
        r1.setKeyIdentifier("keybox1");
        SignRequest.SignedInfo si1 = new SignRequest.SignedInfo();
        si1.setValue("1234".getBytes());
        r1.setSignedInfo(si1);
        HashDataInput hdi = new HashDataInput() {

            @Override
            public String getReferenceId() {
                return "refId1234";
            }

            @Override
            public String getMimeType() {
                return "text/plain";
            }

            @Override
            public InputStream getHashDataInput() {
                return new ByteArrayInputStream("hashdatainput1234".getBytes());
            }
            @Override
            public String getEncoding() {
              return "UTF-8";
            }

            @Override
            public String getFilename() {
                return "file.txt";
            }
        };
        r1.setHashDataInput(Collections.singletonList(hdi));
        requests.add(r1);

        BindingProcessorSimulator bp = new BindingProcessorSimulator();
        bp.setRequests(Collections.singletonList(requests));

        new Thread(bp, "BindingProcessor").start();
        new Thread(new TimeoutServiceSimulator(), "STALService").start();

        try {
            Thread.sleep(STALRequestBroker.DEFAULT_TIMEOUT_MS + 1);
        } catch (InterruptedException ex) {
            log.error("interrupted: " + ex.getMessage());
        }
    }

    @Ignore
    public void testRequestTimeout() {
        log.debug("**************** test requests timeout");
        TimeoutBindingProcessorSimulator bp = new TimeoutBindingProcessorSimulator();

        new Thread(bp, "BindingProcessor").start();
        new Thread(new ServiceSimulator(), "STALService").start();

        try {
            Thread.sleep(STALRequestBroker.DEFAULT_TIMEOUT_MS + 1);
        } catch (InterruptedException ex) {
            log.error("interrupted: " + ex.getMessage());
        }
    }

    @Ignore
    public void testMultipleServices() {
        log.debug("**************** test multiple SignRequests");
        List<STALRequest> requests = new ArrayList<STALRequest>();
        SignRequest r1 = new SignRequest();
        r1.setKeyIdentifier("keybox1");
        SignRequest.SignedInfo si1 = new SignRequest.SignedInfo();
        si1.setValue("1234".getBytes());
        r1.setSignedInfo(si1);
        HashDataInput hdi = new HashDataInput() {

            @Override
            public String getReferenceId() {
                return "refId1234";
            }

            @Override
            public String getMimeType() {
                return "text/plain";
            }

            @Override
            public InputStream getHashDataInput() {
                return new ByteArrayInputStream("hashdatainput1234".getBytes());
            }
            @Override
            public String getEncoding() {
              return "UTF-8";
            }

            @Override
            public String getFilename() {
                return "file.txt";
            }
        };
        r1.setHashDataInput(Collections.singletonList(hdi));
        requests.add(r1);

        List<STALRequest> requests2 = new ArrayList<STALRequest>();
        SignRequest r2 = new SignRequest();
        r2.setKeyIdentifier("keybox2");
        SignRequest.SignedInfo si2 = new SignRequest.SignedInfo();
        si2.setValue("6789".getBytes());
        r2.setSignedInfo(si2);
        HashDataInput hdi2 = new HashDataInput() {

            @Override
            public String getReferenceId() {
                return "refId6789";
            }

            @Override
            public String getMimeType() {
                return "text/xml";
            }

            @Override
            public InputStream getHashDataInput() {
                return new ByteArrayInputStream("<xml>hashdatainput6789</xml>".getBytes());
            }
            @Override
            public String getEncoding() {
              return "UTF-8";
            }

            @Override
            public String getFilename() {
                return "file.xml";
            }
        };
        r2.setHashDataInput(Collections.singletonList(hdi2));
        requests2.add(r2);

        BindingProcessorSimulator bp = new BindingProcessorSimulator();
        List<List<STALRequest>> requestList = new ArrayList<List<STALRequest>>();
        requestList.add(requests);
        requestList.add(requests2);
        bp.setRequests(requestList);

        new Thread(bp, "BindingProcessor").start();
//        new Thread(bp2, "BindingProcessor2").start();
        new Thread(new ServiceSimulator(), "STALService1").start();
        new Thread(new ServiceSimulator(), "STALService2").start();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            log.error("interrupted: " + ex.getMessage());
        }
    }

    class ZombieServiceSimulator implements Runnable {
    
        @Override
        public void run() {
            try {
              ObjectFactory of= new ObjectFactory();

                log.debug("calling stal.nextRequest(oldResponse)");
                InfoboxReadResponseType oldResp = of.createInfoboxReadResponseType();
                ArrayList<JAXBElement<? extends ResponseType>> oldResponses = new ArrayList<JAXBElement<? extends ResponseType>>();
                oldResponses.add(of.createGetNextRequestTypeInfoboxReadResponse(oldResp));

                List<JAXBElement<? extends RequestType>> requests = stal.nextRequest(oldResponses);
                log.debug("got " + requests.size() + " requests. processing...");
                Thread.sleep(1);
                List<JAXBElement<? extends ResponseType>> responses = new ArrayList<JAXBElement<? extends ResponseType>>();
                for (JAXBElement<? extends RequestType> requestElt : requests) {
                  RequestType request = requestElt.getValue();
                    if (request instanceof InfoboxReadRequestType) {
                      log.debug("received UNEXPECTED READINFOBOX request");
                      
                        InfoboxReadResponseType r = of.createInfoboxReadResponseType();
                        r.setInfoboxValue("dummyInfobox".getBytes());
                        responses.add(of.createGetNextRequestTypeInfoboxReadResponse(r));
                    } else if (request instanceof SignRequestType) {

                      log.debug("received UNEXPECTED SIGN request");
                      
                        log.debug("calling stal.getCurrentHashDataInputCallback");
                        List<HashDataInput> hdis = stal.getHashDataInput();
                        assertNotNull(hdis);
                        assertEquals(hdis.size(), 1);
                        HashDataInput hdi = hdis.get(0);// cb.getHashDataInput("1234");
                        InputStream hd = hdi.getHashDataInput();
                        byte[] data = new byte[hd.available()];
                        hd.read(data);
                        log.debug("got HashDataInput " + new String(data));


                        SignResponseType r = of.createSignResponseType();
                        r.setSignatureValue("dummySignature".getBytes());
                        responses.add(of.createGetNextRequestTypeSignResponse(r));
                    } else if (request instanceof QuitRequestType) {
                        log.debug("received EXPECTED QUIT request");
                        return;
                    }
                }

//                if (requests.size() > 0) {
//                    log.debug("calling stal.setResponse with " + requests.size() + " responses");
//                    stal.setResponse(responses);
//                }
                log.debug("calling stal.nextRequest with " + responses.size() + " responses");
                requests = stal.nextRequest(responses);
                for (JAXBElement<? extends RequestType> requestElt : requests) {
                  RequestType request = requestElt.getValue();
                    if (request instanceof QuitRequestType) {
                        log.debug("got QUIT request");
                    } else {
                        log.debug("expected QUIT request, got " + request.getClass().getName());
                    }
                }
            } catch (IOException ex) {
                log.error(ex.getMessage());
            } catch (InterruptedException ex) {
                log.error(ex.getMessage());
            }
        }
      
    }
    
    class ServiceSimulator implements Runnable {

        @Override
        public void run() {
            try {
              ObjectFactory of = new ObjectFactory();
                // first call w/ empty response list
                log.debug("calling stal.nextRequest");
                List<JAXBElement<? extends RequestType>> requests = stal.nextRequest(null); //new ArrayList<ResponseType>());
                log.debug("got " + requests.size() + " requests. processing...");
                Thread.sleep(1);
                List<JAXBElement<? extends ResponseType>> responses = new ArrayList<JAXBElement<? extends ResponseType>>();
                for (JAXBElement<? extends RequestType> requestElt : requests) {
                  RequestType request = requestElt.getValue();
                    if (request instanceof InfoboxReadRequestType) {
                        InfoboxReadResponseType r = of.createInfoboxReadResponseType();
                        r.setInfoboxValue("dummyInfobox".getBytes());
                        responses.add(of.createGetNextRequestTypeInfoboxReadResponse(r));
                    } else if (request instanceof SignRequestType) {

                        log.debug("calling stal.getCurrentHashDataInputCallback");
                        List<HashDataInput> hdis = stal.getHashDataInput();
                        assertNotNull(hdis);
                        assertEquals(hdis.size(), 1);
                        HashDataInput hdi = hdis.get(0);// cb.getHashDataInput("1234");
                        InputStream hd = hdi.getHashDataInput();
                        byte[] data = new byte[hd.available()];
                        hd.read(data);
                        log.debug("got HashDataInput " + new String(data));


                        SignResponseType r = of.createSignResponseType();
                        r.setSignatureValue("dummySignature".getBytes());
                        responses.add(of.createGetNextRequestTypeSignResponse(r));
                    } else if (request instanceof QuitRequestType) {
                        log.debug("received UNEXPECTED QUIT request");
                        return;
                    }
                }

//                if (requests.size() > 0) {
//                    log.debug("calling stal.setResponse with " + requests.size() + " responses");
//                    stal.setResponse(responses);
//                }
                log.debug("calling stal.nextRequest with " + responses.size() + " responses");
                requests = stal.nextRequest(responses);
                for (JAXBElement<? extends RequestType> requestElt : requests) {
                  RequestType request = requestElt.getValue();
                    if (request instanceof QuitRequestType) {
                        log.debug("got QUIT request");
                    } else {
                        log.debug("expected QUIT request, got " + request.getClass().getName());
                    }
                }
            } catch (IOException ex) {
                log.error(ex.getMessage());
            } catch (InterruptedException ex) {
                log.error(ex.getMessage());
            }
        }
    }

    class TimeoutServiceSimulator implements Runnable {

        @Override
        public void run() {
            try {
                // first call w/ empty response list
                log.debug("calling stal.nextRequest");
                List<JAXBElement<? extends RequestType>> requests = stal.nextRequest(null); //new ArrayList<ResponseType>());
                log.debug("got " + requests.size() + " requests. processing...");
                Thread.sleep(1);
                for (JAXBElement<? extends RequestType> requestElt : requests) {
                  RequestType request = requestElt.getValue();
//                    if (request instanceof InfoboxReadRequest) {
                    if (request instanceof SignRequestType) {
                        log.debug("calling stal.getCurrentHashDataInputCallback");
                        List<HashDataInput> hdis = stal.getHashDataInput();
                        assertNotNull(hdis);
                        assertEquals(hdis.size(), 1);
                        HashDataInput hdi = hdis.get(0);// cb.getHashDataInput("1234");
                        InputStream hd = hdi.getHashDataInput();
                        byte[] data = new byte[hd.available()];
                        hd.read(data);
                        log.debug("got HashDataInput " + new String(data));
                    } else if (request instanceof QuitRequestType) {
                        log.debug("received UNEXPECTED QUIT requests");
                        return;
                    }
                }
                log.debug("simulating timeout ...");
            } catch (IOException ex) {
                log.error(ex.getMessage());
            } catch (InterruptedException ex) {
                log.error(ex.getMessage());
            }
        }
    }

    class BindingProcessorSimulator implements Runnable {

        List<List<STALRequest>> requestsLists;

        public void setRequests(List<List<STALRequest>> requests) {
            this.requestsLists = requests;
        }

        @Override
        public void run() {

            //simulate SLCommand execution
            for (List<STALRequest> requests : requestsLists) {
                execSLCommand(requests);

                log.debug("SLCommand finished, calling stal.handleReqeusts(QUIT)");
                stal.handleRequest(Collections.singletonList((STALRequest) new QuitRequest()));
                log.debug("QUIT returned (waiting for applet reload)");
            }

        }

        public void execSLCommand(List<STALRequest> requests) {
            int numReq = requests.size();
            log.debug("SLCommand calling stal.handleRequests " + numReq + " requests");
            List<STALResponse> responses = stal.handleRequest(requests);
            assertEquals(numReq, responses.size());
            for (int i = 0; i < numReq; i++) {
                STALRequest request = requests.get(i);
                STALResponse response = responses.get(i);

                if (response instanceof ErrorResponse) {
                    log.warn("SLCommand received unexpected error response from STAL: " + ((ErrorResponse) response).getErrorCode());
                } else if (request instanceof InfoboxReadRequest) {
                    assertTrue(response instanceof InfoboxReadResponse);
                    String infobox = new String(((InfoboxReadResponse) response).getInfoboxValue());
                    log.debug("SLCommand received expected InfoboxReadResponse from STAL: " + infobox);
                } else if (request instanceof SignRequest) {
                    assertTrue(response instanceof SignResponse);
                    String signVal = new String(((SignResponse) response).getSignatureValue());
                    log.debug("SLCommand received expected SignResponse from STAL: " + signVal);
                } else {
                    log.error("***** RequestType: " + request.getClass() + " TODO");
                }
            //TODO
            }

        }
    }

    class TimeoutBindingProcessorSimulator implements Runnable {

        @Override
        public void run() {

            //simulate SLCommand execution
            log.debug("simulating timeout ...");
        }
    }
}
