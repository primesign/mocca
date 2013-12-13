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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.jws.WebService;
import javax.servlet.ServletContext;
import javax.xml.bind.JAXBElement;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

import org.slf4j.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.buergerkarte.namespaces.cardchannel.service.CommandAPDUType;
import at.buergerkarte.namespaces.cardchannel.service.ScriptType;
import at.gv.egiz.bku.binding.BindingProcessor;
import at.gv.egiz.bku.binding.BindingProcessorManager;
import at.gv.egiz.bku.binding.Id;
import at.gv.egiz.bku.binding.IdFactory;
import at.gv.egiz.stal.HashDataInput;
import at.gv.egiz.stal.service.GetHashDataInputFault;
import at.gv.egiz.stal.service.STALPortType;
import at.gv.egiz.stal.service.types.ErrorResponseType;
import at.gv.egiz.stal.service.types.GetHashDataInputFaultType;
import at.gv.egiz.stal.service.types.GetHashDataInputResponseType;
import at.gv.egiz.stal.service.types.GetHashDataInputType;
import at.gv.egiz.stal.service.types.GetNextRequestResponseType;
import at.gv.egiz.stal.service.types.GetNextRequestType;
import at.gv.egiz.stal.service.types.InfoboxReadRequestType;
import at.gv.egiz.stal.service.types.QuitRequestType;
import at.gv.egiz.stal.service.types.RequestType;
import at.gv.egiz.stal.service.types.ResponseType;
import at.gv.egiz.stal.service.types.SignRequestType;
import at.gv.egiz.stal.service.types.GetHashDataInputType.Reference;

import com.sun.xml.ws.developer.UsesJAXBContext;

/**
 * 
 * @author clemens
 */
@WebService(endpointInterface = "at.gv.egiz.stal.service.STALPortType", portName="STALPort", serviceName="STALService", targetNamespace="http://www.egiz.gv.at/wsdl/stal", wsdlLocation="WEB-INF/wsdl/stal.wsdl")
@UsesJAXBContext(STALXJAXBContextFactory.class)
public class STALServiceImpl implements STALPortType {

  public static final String BINDING_PROCESSOR_MANAGER = "bindingProcessorManager";
  public static final Id TEST_SESSION_ID = IdFactory.getInstance().createId("TestSession");
  private final Logger log = LoggerFactory.getLogger(STALServiceImpl.class);

  static {
    Logger log = LoggerFactory.getLogger(STALServiceImpl.class);
    if (log.isTraceEnabled()) {
      log.trace("enabling webservice communication dump");
      System.setProperty("com.sun.xml.ws.transport.http.HttpAdapter.dump", "true");
    } else {
      System.setProperty("com.sun.xml.ws.fault.SOAPFaultBuilder.disableCaptureStackTrace", "false");
    }
  }
  @Resource
  protected WebServiceContext wsContext;
  protected IdFactory idF = IdFactory.getInstance();
  /** JAXB ObjectFactories */
  private at.gv.egiz.stal.service.types.ObjectFactory stalObjFactory = new at.gv.egiz.stal.service.types.ObjectFactory();
  /** don't confuse with at.buergerkarte.namespaces.cardchannel */
  private at.buergerkarte.namespaces.cardchannel.service.ObjectFactory ccObjFactory = new at.buergerkarte.namespaces.cardchannel.service.ObjectFactory();

  @Override
  public GetNextRequestResponseType connect(String sessId) {

    if (sessId == null) {
      throw new NullPointerException("No session id provided.");
    }

    Id sessionId = idF.createId(sessId);
    MDC.put("id", sessionId.toString());

    try {
      log.debug("Received Connect.");
  
      if (TEST_SESSION_ID.equals(sessionId)) {
        return getTestSessionNextRequestResponse(null);
      }
  
      GetNextRequestResponseType response = new GetNextRequestResponseType();
      response.setSessionId(sessionId.toString());
  
      STALRequestBroker stal = getStal(sessionId);
  
      if (stal != null) {
  
        List<JAXBElement<? extends RequestType>> requestsOut = ((STALRequestBroker) stal).connect();
        response.getInfoboxReadRequestOrSignRequestOrQuitRequest().addAll(requestsOut);
  
        if (log.isDebugEnabled()) {
          StringBuilder sb = new StringBuilder("Returning initial GetNextRequestResponse containing ");
          sb.append(requestsOut.size());
          sb.append(" requests: ");
          for (JAXBElement<? extends RequestType> reqOut : requestsOut) {
            sb.append(reqOut.getValue().getClass());
            sb.append(' ');
          }
          log.debug(sb.toString());
        }
      } else {
        log.error("Failed to get STAL, returning QuitRequest.");
        QuitRequestType quitT = stalObjFactory.createQuitRequestType();
        JAXBElement<QuitRequestType> quit = stalObjFactory.createGetNextRequestResponseTypeQuitRequest(quitT);
        response.getInfoboxReadRequestOrSignRequestOrQuitRequest().add(quit);
      }
      return response;
      
    } finally {
      MDC.remove("id");
    }
  }

  @Override
  public GetNextRequestResponseType getNextRequest(GetNextRequestType request) {

    if (request.getSessionId() == null) {
      throw new NullPointerException("No session id provided.");
    }

    Id sessionId = idF.createId(request.getSessionId());
    MDC.put("id", sessionId.toString());

    try {

      List<JAXBElement<? extends ResponseType>> responsesIn = request.getInfoboxReadResponseOrSignResponseOrErrorResponse();

      if (log.isDebugEnabled()) {
        StringBuilder sb = new StringBuilder("Received GetNextRequest containing ");
        sb.append(responsesIn.size());
        sb.append(" responses:");
        for (JAXBElement<? extends ResponseType> respIn : responsesIn) {
          sb.append(' ');
          sb.append(respIn.getValue().getClass());
          if (respIn.getValue() instanceof ErrorResponseType) {
            ErrorResponseType err = (ErrorResponseType)respIn.getValue();
            sb.append(" (" + err.getErrorCode() + " - " + err.getErrorMessage() + ")");
          }
        }
        log.debug(sb.toString());
      }
  
      if (TEST_SESSION_ID.equals(sessionId)) {
        return getTestSessionNextRequestResponse(responsesIn);
      }
  
      GetNextRequestResponseType response = new GetNextRequestResponseType();
      response.setSessionId(sessionId.toString());
  
      STALRequestBroker stal = getStal(sessionId);
  
      if (stal != null) {
  
        List<JAXBElement<? extends RequestType>> requestsOut = ((STALRequestBroker) stal).nextRequest(responsesIn);
        response.getInfoboxReadRequestOrSignRequestOrQuitRequest().addAll(requestsOut);
  
        if (log.isDebugEnabled()) {
          StringBuilder sb = new StringBuilder("Returning GetNextRequestResponse containing ");
          sb.append(requestsOut.size());
          sb.append(" requests: ");
          for (JAXBElement<? extends RequestType> reqOut : requestsOut) {
            sb.append(reqOut.getValue().getClass());
            sb.append(' ');
          }
          log.debug(sb.toString());
        }
      } else {
        log.error("Failed to get STAL, returning QuitRequest.");
        QuitRequestType quitT = stalObjFactory.createQuitRequestType();
        JAXBElement<QuitRequestType> quit = stalObjFactory.createGetNextRequestResponseTypeQuitRequest(quitT);
        response.getInfoboxReadRequestOrSignRequestOrQuitRequest().add(quit);
      }
      return response;
      
    } finally {
      MDC.remove("id");
    }
  }

  @Override
  public GetHashDataInputResponseType getHashDataInput(GetHashDataInputType request) throws GetHashDataInputFault {

    if (request.getSessionId() == null) {
      throw new NullPointerException("No session id provided.");
    }

    Id sessionId = idF.createId(request.getSessionId());
    MDC.put("id", sessionId.toString());

    try {
      
      if (log.isDebugEnabled()) {
        log.debug("Received GetHashDataInputRequest containing {} reference(s).", request.getReference().size());
      }
  
      if (TEST_SESSION_ID.equals(sessionId)) {
        return getTestSessionHashDataInputResponse(request.getReference());
      }
      
      GetHashDataInputResponseType response = new GetHashDataInputResponseType();
      response.setSessionId(sessionId.toString());
  
      STALRequestBroker stal = getStal(sessionId);
  
      if (stal != null) {
        List<HashDataInput> hashDataInputs = stal.getHashDataInput();
  
        if (hashDataInputs != null) {
  
          Map<String, HashDataInput> hashDataIdMap = new HashMap<String, HashDataInput>();
          for (HashDataInput hdi : hashDataInputs) {
            if (log.isTraceEnabled()) {
              log.trace("Provided HashDataInput for reference {}.", hdi.getReferenceId());
            }
            hashDataIdMap.put(hdi.getReferenceId(), hdi);
          }
  
          List<GetHashDataInputType.Reference> reqRefs = request.getReference();
          for (GetHashDataInputType.Reference reqRef : reqRefs) {
            String reqRefId = reqRef.getID();
            HashDataInput reqHdi = hashDataIdMap.get(reqRefId);
            if (reqHdi == null) {
              String msg = "Failed to resolve HashDataInput for reference " + reqRefId;
              log.error(msg);
              GetHashDataInputFaultType faultInfo = new GetHashDataInputFaultType();
              faultInfo.setErrorCode(1);
              faultInfo.setErrorMessage(msg);
              throw new GetHashDataInputFault(msg, faultInfo);
            }
  
            InputStream hashDataIS = reqHdi.getHashDataInput();
            if (hashDataIS == null) {
              //HashDataInput not cached?
              String msg = "Failed to obtain HashDataInput for reference " + reqRefId + ", reference not cached";
              log.error(msg);
              GetHashDataInputFaultType faultInfo = new GetHashDataInputFaultType();
              faultInfo.setErrorCode(1);
              faultInfo.setErrorMessage(msg);
              throw new GetHashDataInputFault(msg, faultInfo);
            }
            ByteArrayOutputStream baos = null;
            try {
              if (log.isDebugEnabled()) {
                Object[] args = {reqRefId, reqHdi.getMimeType(), reqHdi.getEncoding()};
                log.debug("Resolved HashDataInput {} ({};charset={}).", args);
              }
              baos = new ByteArrayOutputStream(hashDataIS.available());
              int c;
              while ((c = hashDataIS.read()) != -1) {
                baos.write(c);
              }
              GetHashDataInputResponseType.Reference ref = new GetHashDataInputResponseType.Reference();
              ref.setID(reqRefId);
              ref.setMimeType(reqHdi.getMimeType());
              ref.setEncoding(reqHdi.getEncoding());
              ref.setFilename(reqHdi.getFilename());
              ref.setValue(baos.toByteArray());
              response.getReference().add(ref);
            } catch (IOException ex) {
              String msg = "Failed to get HashDataInput for reference " + reqRefId;
              log.error(msg, ex);
              GetHashDataInputFaultType faultInfo = new GetHashDataInputFaultType();
              faultInfo.setErrorCode(1);
              faultInfo.setErrorMessage(msg);
              throw new GetHashDataInputFault(msg, faultInfo, ex);
            } finally {
              try {
                baos.close();
              } catch (IOException ex) {
              }
            }
          }
          return response;
        } else {
          String msg = "Failed to resolve any HashDataInputs.";
          log.error(msg);
          GetHashDataInputFaultType faultInfo = new GetHashDataInputFaultType();
          faultInfo.setErrorCode(1);
          faultInfo.setErrorMessage(msg);
          throw new GetHashDataInputFault(msg, faultInfo);
        }
      } else {
        String msg = "Session timeout."; //Failed to get STAL for session " + sessionId;
        log.error(msg);
        GetHashDataInputFaultType faultInfo = new GetHashDataInputFaultType();
        faultInfo.setErrorCode(1);
        faultInfo.setErrorMessage(msg);
        throw new GetHashDataInputFault(msg, faultInfo);
      }
      
    } finally {
      MDC.remove("id");
    }
  }

  private STALRequestBroker getStal(Id sessionId) {
    log.trace("Resolve STAL for session [{}].", sessionId);
    MessageContext mCtx = wsContext.getMessageContext();
    ServletContext sCtx = (ServletContext) mCtx.get(MessageContext.SERVLET_CONTEXT);
    BindingProcessorManager bpMgr = (BindingProcessorManager) sCtx.getAttribute(BINDING_PROCESSOR_MANAGER);
    BindingProcessor bindingProcessor = bpMgr.getBindingProcessor(sessionId);
    if (bindingProcessor != null) {
      if (bindingProcessor.getSTAL() instanceof STALRequestBroker) {
        return (STALRequestBroker) bindingProcessor.getSTAL();
      }
    }
    return null;
  }

  private GetNextRequestResponseType getTestSessionNextRequestResponse(List<JAXBElement<? extends ResponseType>> responsesIn) {
    GetNextRequestResponseType response = new GetNextRequestResponseType();
    response.setSessionId(TEST_SESSION_ID.toString());

    List<JAXBElement<? extends RequestType>> reqs = response.getInfoboxReadRequestOrSignRequestOrQuitRequest();

    if (responsesIn == null) {
      log.info("[TestSession] CONNECT");
//      addTestCardChannelRequest(reqs);
//      addTestInfoboxReadRequest("IdentityLink", reqs);
//      addTestInfoboxReadRequest("SecureSignatureKeypair", reqs);
//      addTestInfoboxReadRequest("CertifiedKeypair", reqs);
      addTestSignatureRequests("SecureSignatureKeypair", reqs);
    } else if (responsesIn != null && responsesIn.size() > 0 && responsesIn.get(0).getValue() instanceof ErrorResponseType) {
      log.info("[TestSession] received ErrorResponse, return QUIT request");
      QuitRequestType quitT = stalObjFactory.createQuitRequestType();
      reqs.add(stalObjFactory.createGetNextRequestResponseTypeQuitRequest(quitT));
    } else {
      log.info("[TestSession] received " + responsesIn.size() + " response(s), return QUIT" );
      QuitRequestType quitT = stalObjFactory.createQuitRequestType();
      reqs.add(stalObjFactory.createGetNextRequestResponseTypeQuitRequest(quitT));
    }
    return response;
  }
  
  
  private GetHashDataInputResponseType getTestSessionHashDataInputResponse(List<Reference> references) {
    log.debug("[TestSession] received GET_HASHDATAINPUT");
    
    GetHashDataInputResponseType response = new GetHashDataInputResponseType();
    response.setSessionId(TEST_SESSION_ID.toString());
    
    for (Reference reference : references) {
      String refId = reference.getID();
      log.debug("[TestSession] adding hashdata input for " + refId);
      GetHashDataInputResponseType.Reference ref = new GetHashDataInputResponseType.Reference();
      ref.setID(refId);
      ref.setMimeType(TestSignatureData.HASHDATA_MIMETYPES.get(refId)); //todo resolve from TestSignatureData
      ref.setValue(TestSignatureData.HASHDATA_INPUT.get(refId));
      ref.setEncoding(TestSignatureData.ENCODING);
      response.getReference().add(ref);
    }
    return response;
  }
  
  @SuppressWarnings("unused")
  private void addTestCardChannelRequest(List<JAXBElement<? extends RequestType>> requestList) {
    log.info("[TestSession] add CARDCHANNEL request");
    ScriptType scriptT = ccObjFactory.createScriptType();
    CommandAPDUType cmd = ccObjFactory.createCommandAPDUType();
    cmd.setValue("TestSession CardChannelCMD 1234".getBytes());
    scriptT.getResetOrCommandAPDUOrVerifyAPDU().add(cmd);
    requestList.add(ccObjFactory.createScript(scriptT));
  }

  @SuppressWarnings("unused")
  private void addTestInfoboxReadRequest(String infoboxIdentifier, List<JAXBElement<? extends RequestType>> requestList) {
    log.info("[TestSession] add READ "+ infoboxIdentifier + " request");
    InfoboxReadRequestType ibrT = stalObjFactory.createInfoboxReadRequestType();
    ibrT.setInfoboxIdentifier(infoboxIdentifier);
    requestList.add(stalObjFactory.createGetNextRequestResponseTypeInfoboxReadRequest(ibrT));
  }
  
  private void addTestSignatureRequests(String keyIdentifier, List<JAXBElement<? extends RequestType>> reqs) {
    log.info("[TestSession] add SIGN " + keyIdentifier + " request");
    SignRequestType sigT = stalObjFactory.createSignRequestType();
    sigT.setKeyIdentifier(keyIdentifier);
    SignRequestType.SignedInfo sigI = stalObjFactory.createSignRequestTypeSignedInfo();
    sigI.setValue(TestSignatureData.SIGNED_INFO.get(1));
    sigT.setSignedInfo(sigI); //select!
    reqs.add(stalObjFactory.createGetNextRequestResponseTypeSignRequest(sigT));
  }

}
