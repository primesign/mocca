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
import com.sun.xml.ws.developer.UsesJAXBContext;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import javax.jws.WebService;
import javax.servlet.ServletContext;
import javax.xml.bind.JAXBElement;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author clemens
 */
@WebService(endpointInterface = "at.gv.egiz.stal.service.STALPortType", portName="STALPort", serviceName="STALService", targetNamespace="http://www.egiz.gv.at/wsdl/stal", wsdlLocation="WEB-INF/wsdl/stal.wsdl")
@UsesJAXBContext(STALXJAXBContextFactory.class)
public class STALServiceImpl implements STALPortType {

  public static final String BINDING_PROCESSOR_MANAGER = "bindingProcessorManager";
  public static final Id TEST_SESSION_ID = IdFactory.getInstance().createId("TestSession");
  protected static final Log log = LogFactory.getLog(STALServiceImpl.class);


  static {
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
      throw new NullPointerException("No session id provided");
    }

    Id sessionId = idF.createId(sessId);

    if (log.isDebugEnabled()) {
      log.debug("Received Connect [" + sessionId + "]");
    }

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
        StringBuilder sb = new StringBuilder("Returning initial GetNextRequestResponse [");
        sb.append(sessionId.toString());
        sb.append("] containing ");
        sb.append(requestsOut.size());
        sb.append(" requests: ");
        for (JAXBElement<? extends RequestType> reqOut : requestsOut) {
          sb.append(reqOut.getValue().getClass());
          sb.append(' ');
        }
        log.debug(sb.toString());
      }
    } else {
      log.error("Failed to get STAL for session " + sessionId + ", returning QuitRequest");
      QuitRequestType quitT = stalObjFactory.createQuitRequestType();
      JAXBElement<QuitRequestType> quit = stalObjFactory.createGetNextRequestResponseTypeQuitRequest(quitT);
      response.getInfoboxReadRequestOrSignRequestOrQuitRequest().add(quit);
    }
    return response;
  }

  @Override
  public GetNextRequestResponseType getNextRequest(GetNextRequestType request) {

    if (request.getSessionId() == null) {
      throw new NullPointerException("No session id provided");
    }

    Id sessionId = idF.createId(request.getSessionId());

    List<JAXBElement<? extends ResponseType>> responsesIn = request.getInfoboxReadResponseOrSignResponseOrErrorResponse();
//    List<ResponseType> responsesIn = request.getInfoboxReadResponseOrSignResponseOrErrorResponse();//getResponse();

    if (log.isDebugEnabled()) {
      StringBuilder sb = new StringBuilder("Received GetNextRequest [");
      sb.append(sessionId.toString());
      sb.append("] containing ");
      sb.append(responsesIn.size());
      sb.append(" responses: ");
      for (JAXBElement<? extends ResponseType> respIn : responsesIn) {
        sb.append(respIn.getValue().getClass());
        sb.append(' ');
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
        StringBuilder sb = new StringBuilder("Returning GetNextRequestResponse [");
        sb.append(sessionId.toString());
        sb.append("] containing ");
        sb.append(requestsOut.size());
        sb.append(" requests: ");
        for (JAXBElement<? extends RequestType> reqOut : requestsOut) {
          sb.append(reqOut.getValue().getClass());
          sb.append(' ');
        }
        log.debug(sb.toString());
      }
    } else {
      log.error("Failed to get STAL for session " + sessionId + ", returning QuitRequest");
      QuitRequestType quitT = stalObjFactory.createQuitRequestType();
      JAXBElement<QuitRequestType> quit = stalObjFactory.createGetNextRequestResponseTypeQuitRequest(quitT);
      response.getInfoboxReadRequestOrSignRequestOrQuitRequest().add(quit);
    }
    return response;
  }

  @Override
  public GetHashDataInputResponseType getHashDataInput(GetHashDataInputType request) throws GetHashDataInputFault {

    if (request.getSessionId() == null) {
      throw new NullPointerException("No session id provided");
    }

    Id sessionId = idF.createId(request.getSessionId());

    if (log.isDebugEnabled()) {
      log.debug("Received GetHashDataInputRequest for session " + sessionId + " containing " + request.getReference().size() + " reference(s)");
    }

    GetHashDataInputResponseType response = new GetHashDataInputResponseType();
    response.setSessionId(sessionId.toString());

    if (TEST_SESSION_ID.equals(sessionId)) {
      log.debug("Received GetHashDataInput for session " + TEST_SESSION_ID + ", return DummyHashDataInput");
      GetHashDataInputResponseType.Reference ref = new GetHashDataInputResponseType.Reference();
      ref.setID("signed-data-reference-0-1214921968-27971781-24309"); //Reference-" + TEST_SESSION_ID + "-001");
      ref.setMimeType("text/plain");

      Charset charset;
      try {
        charset = Charset.forName("iso-8859-15");
        ref.setEncoding("iso-8859-15");
      } catch (Exception ex) {
        log.warn(ex.getMessage());
        charset = Charset.defaultCharset();
        ref.setEncoding(charset.toString());
      }
      ref.setValue("hashdatainput-öäüß@€-00000000001".getBytes(charset));
      response.getReference().add(ref);
      return response;
    } else {
      STALRequestBroker stal = getStal(sessionId);

      if (stal != null) {
        List<HashDataInput> hashDataInputs = stal.getHashDataInput();

        if (hashDataInputs != null) {

          Map<String, HashDataInput> hashDataIdMap = new HashMap<String, HashDataInput>();
          for (HashDataInput hdi : hashDataInputs) {
            if (log.isTraceEnabled()) {
              log.trace("Provided HashDataInput for reference " + hdi.getReferenceId());
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
                log.debug("Resolved HashDataInput " + reqRefId + " (" + reqHdi.getMimeType() + ";charset=" + reqHdi.getEncoding() + ")");
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
          String msg = "Failed to resolve any HashDataInputs for session " + sessionId;
          log.error(msg);
          GetHashDataInputFaultType faultInfo = new GetHashDataInputFaultType();
          faultInfo.setErrorCode(1);
          faultInfo.setErrorMessage(msg);
          throw new GetHashDataInputFault(msg, faultInfo);
        }
      } else {
        String msg = "Session timeout"; //Failed to get STAL for session " + sessionId;
        log.error(msg + " " + sessionId);
        GetHashDataInputFaultType faultInfo = new GetHashDataInputFaultType();
        faultInfo.setErrorCode(1);
        faultInfo.setErrorMessage(msg);
        throw new GetHashDataInputFault(msg, faultInfo);
      }
    }
  }

  private STALRequestBroker getStal(Id sessionId) {
    if (log.isTraceEnabled()) {
      log.trace("resolve STAL for session " + sessionId);
    }
    MessageContext mCtx = wsContext.getMessageContext();
    ServletContext sCtx = (ServletContext) mCtx.get(MessageContext.SERVLET_CONTEXT);
    BindingProcessorManager bpMgr = (BindingProcessorManager) sCtx.getAttribute(BINDING_PROCESSOR_MANAGER);
    BindingProcessor bp = bpMgr.getBindingProcessor(sessionId);
    return (bp == null) ? null : (bp.isFinished() ? null : (STALRequestBroker) bp.getSTAL());
  }

  private GetNextRequestResponseType getTestSessionNextRequestResponse(List<JAXBElement<? extends ResponseType>> responsesIn) {
    GetNextRequestResponseType response = new GetNextRequestResponseType();
    response.setSessionId(TEST_SESSION_ID.toString());

    List<JAXBElement<? extends RequestType>> reqs = response.getInfoboxReadRequestOrSignRequestOrQuitRequest();

    if (responsesIn == null) {
      log.info("[TestSession] received CONNECT, return dummy requests ");
//      addDummyRequests(reqs);
      ScriptType scriptT = ccObjFactory.createScriptType();
      CommandAPDUType cmd = ccObjFactory.createCommandAPDUType();
      cmd.setValue("TestSession CardChannelCMD 1234".getBytes());
      scriptT.getResetOrCommandAPDUOrVerifyAPDU().add(cmd);
      reqs.add(ccObjFactory.createScript(scriptT));
    } else if (responsesIn != null && responsesIn.size() > 0 && responsesIn.get(0).getValue() instanceof ErrorResponseType) {
      log.info("[TestSession] received ErrorResponse, return QUIT request");
      QuitRequestType quitT = stalObjFactory.createQuitRequestType();
      reqs.add(stalObjFactory.createGetNextRequestResponseTypeQuitRequest(quitT));
    } else {
      log.info("[TestSession] received " + responsesIn.size() + " response(s), return dummy requests" );
      addDummyRequests(reqs);
    }
    return response;
  }

  private void addDummyRequests(List<JAXBElement<? extends RequestType>> reqs) {
//    log.info("[TestSession] add READ request for Infobox IdentityLink");
//    InfoboxReadRequestType ibrT1 = stalObjFactory.createInfoboxReadRequestType();
//    ibrT1.setInfoboxIdentifier("IdentityLink");
//    reqs.add(stalObjFactory.createGetNextRequestResponseTypeInfoboxReadRequest(ibrT1));

    log.info("[TestSession] add READ request for Infobox CertifiedKeypair");
    InfoboxReadRequestType ibrT2 = stalObjFactory.createInfoboxReadRequestType();
    ibrT2.setInfoboxIdentifier("CertifiedKeypair");
    reqs.add(stalObjFactory.createGetNextRequestResponseTypeInfoboxReadRequest(ibrT2));

    log.info("[TestSession] add READ request for Infobox SecureSignatureKeypair");
    InfoboxReadRequestType ibrT3 = stalObjFactory.createInfoboxReadRequestType();
    ibrT3.setInfoboxIdentifier("SecureSignatureKeypair");
    reqs.add(stalObjFactory.createGetNextRequestResponseTypeInfoboxReadRequest(ibrT3));

    log.info("[TestSession] add SIGN request");
    SignRequestType sigT1 = stalObjFactory.createSignRequestType();
    sigT1.setKeyIdentifier("SecureSignatureKeypair");
    sigT1.setSignedInfo("<dsig:SignedInfo  xmlns:dsig=\"http://www.w3.org/2000/09/xmldsig#\" xmlns:xpf=\"http://www.w3.org/2002/06/xmldsig-filter2\"><dsig:CanonicalizationMethod Algorithm=\"http://www.w3.org/TR/2001/REC-xml-c14n-20010315\" /> <dsig:SignatureMethod Algorithm=\"http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha1\" /> <dsig:Reference Id=\"signed-data-reference-0-1214921968-27971781-24309\" URI=\"#signed-data-object-0-1214921968-27971781-13578\"><dsig:Transforms> <dsig:Transform Algorithm=\"http://www.w3.org/2002/06/xmldsig-filter2\"> <xpf:XPath xmlns:xpf=\"http://www.w3.org/2002/06/xmldsig-filter2\" Filter=\"intersect\">id('signed-data-object-0-1214921968-27971781-13578')/node()</xpf:XPath></dsig:Transform></dsig:Transforms><dsig:DigestMethod Algorithm=\"http://www.w3.org/2000/09/xmldsig#sha1\" /> <dsig:DigestValue>H1IePEEfGQ2SG03H6LTzw1TpCuM=</dsig:DigestValue></dsig:Reference><dsig:Reference Id=\"etsi-data-reference-0-1214921968-27971781-25439\" Type=\"http://uri.etsi.org/01903/v1.1.1#SignedProperties\" URI=\"#xmlns(etsi=http://uri.etsi.org/01903/v1.1.1%23)%20xpointer(id('etsi-data-object-0-1214921968-27971781-3095')/child::etsi:QualifyingProperties/child::etsi:SignedProperties)\"><dsig:DigestMethod Algorithm=\"http://www.w3.org/2000/09/xmldsig#sha1\" /><dsig:DigestValue>yV6Q+I60buqR4mMaxA7fi+CV35A=</dsig:DigestValue></dsig:Reference></dsig:SignedInfo>".getBytes());
    reqs.add(stalObjFactory.createGetNextRequestResponseTypeSignRequest(sigT1));
  }

}
