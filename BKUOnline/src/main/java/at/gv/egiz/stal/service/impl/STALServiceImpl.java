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

import at.gv.egiz.bku.binding.BindingProcessor;
import at.gv.egiz.bku.binding.BindingProcessorManager;
import at.gv.egiz.stal.service.*;
import at.gv.egiz.bku.binding.Id;
import at.gv.egiz.bku.binding.IdFactory;
import at.gv.egiz.stal.ErrorResponse;
import at.gv.egiz.stal.STALRequest;
import at.gv.egiz.stal.STALResponse;
import at.gv.egiz.stal.HashDataInputCallback;
import at.gv.egiz.stal.InfoboxReadRequest;
import at.gv.egiz.stal.QuitRequest;
import at.gv.egiz.stal.SignRequest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import javax.annotation.Resource;
import javax.jws.WebService;
import javax.servlet.ServletContext;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author clemens
 */
@WebService(endpointInterface = "at.gv.egiz.stal.service.STALPortType")
public class STALServiceImpl implements STALPortType {

  public static final String BINDING_PROCESSOR_MANAGER = "bindingProcessorManager";
  public static final String TEST_SESSION_ID = "TestSession";
  protected static final Log log = LogFactory.getLog(STALServiceImpl.class);
  @Resource
  WebServiceContext wsContext;
  protected IdFactory idF = IdFactory.getInstance();

  @Override
  public GetNextRequestResponseType getNextRequest(GetNextRequestType request) {

    // HttpSession session = ((HttpServletRequest)
    // mCtx.get(MessageContext.SERVLET_REQUEST)).getSession();
    String sessId = request.getSessionId();
    List<STALResponse> responses = request.getResponse();
    if (log.isDebugEnabled()) {
      log.debug("Received GetNextRequest for session " + sessId
          + " containing " + responses.size() + " responses");
    }

    GetNextRequestResponseType response = new GetNextRequestResponseType();
    response.setSessionId(sessId);

    if (TEST_SESSION_ID.equals(sessId)) {
      if (responses.size() > 0 && responses.get(0) instanceof ErrorResponse) {
        log
            .info("Received TestSession GetNextRequest(ErrorResponse), returning QuitRequest");
        response.getRequest().add(new QuitRequest());
      } else {
        log
            .info("Received TestSession GetNextRequest, returning InfoboxReadRequest ");
        SignRequest sig = new SignRequest();
        sig.setKeyIdentifier("SecureSignatureKeypair");
        sig.setSignedInfo("<dsig:SignedInfo  xmlns:dsig=\"http://www.w3.org/2000/09/xmldsig#\" xmlns:xpf=\"http://www.w3.org/2002/06/xmldsig-filter2\"><dsig:CanonicalizationMethod Algorithm=\"http://www.w3.org/TR/2001/REC-xml-c14n-20010315\" /> <dsig:SignatureMethod Algorithm=\"http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha1\" /> <dsig:Reference Id=\"signed-data-reference-0-1214921968-27971781-24309\" URI=\"#signed-data-object-0-1214921968-27971781-13578\"><dsig:Transforms> <dsig:Transform Algorithm=\"http://www.w3.org/2002/06/xmldsig-filter2\"> <xpf:XPath xmlns:xpf=\"http://www.w3.org/2002/06/xmldsig-filter2\" Filter=\"intersect\">id('signed-data-object-0-1214921968-27971781-13578')/node()</xpf:XPath></dsig:Transform></dsig:Transforms><dsig:DigestMethod Algorithm=\"http://www.w3.org/2000/09/xmldsig#sha1\" /> <dsig:DigestValue>H1IePEEfGQ2SG03H6LTzw1TpCuM=</dsig:DigestValue></dsig:Reference><dsig:Reference Id=\"etsi-data-reference-0-1214921968-27971781-25439\" Type=\"http://uri.etsi.org/01903/v1.1.1#SignedProperties\" URI=\"#xmlns(etsi=http://uri.etsi.org/01903/v1.1.1%23)%20xpointer(id('etsi-data-object-0-1214921968-27971781-3095')/child::etsi:QualifyingProperties/child::etsi:SignedProperties)\"><dsig:DigestMethod Algorithm=\"http://www.w3.org/2000/09/xmldsig#sha1\" /><dsig:DigestValue>yV6Q+I60buqR4mMaxA7fi+CV35A=</dsig:DigestValue></dsig:Reference></dsig:SignedInfo>".getBytes());
        response.getRequest().add(sig);
        InfoboxReadRequest req = new InfoboxReadRequest();
        req.setInfoboxIdentifier("IdentityLink");
        req.setDomainIdentifier("hansiwurzel");
        response.getRequest().add(req);
        req = new InfoboxReadRequest();
        req.setInfoboxIdentifier("CertifiedKeypair");
        response.getRequest().add(req);
        req = new InfoboxReadRequest();
        req.setInfoboxIdentifier("SecureSignatureKeypair");
        response.getRequest().add(req);
      }
      return response;
    }

    // get Session Id
    Id sessionId = idF.createId(sessId);
    STALRequestBroker stal = getStal(sessionId);

    if (stal == null) {
      log.error("Failed to get STAL for session " + sessId
          + ", returning QuitRequest");
      response.getRequest().add(new QuitRequest());
    } else {
      List<STALResponse> responsesIn = request.getResponse();
      for (STALResponse resp : responsesIn) {
        log.debug(resp);
      }
      List<STALRequest> requestsOut = ((STALRequestBroker) stal)
          .nextRequest(responsesIn);
      response.getRequest().addAll(requestsOut);
      if (log.isDebugEnabled()) {
        log.debug("Returning GetNextRequestResponse for session " + sessId
            + " containing " + requestsOut.size() + " requests");
      }
    }
    return response;
  }

  @Override
  public GetHashDataInputResponseType getHashDataInput(
      GetHashDataInputType request) throws GetHashDataInputFault {

    String sessId = request.getSessionId();
    if (log.isDebugEnabled()) {
      log.debug("Received GetHashDataInputRequest for session " + sessId
          + " containing " + request.getReference().size() + " referencese");
    }

    // get Session Id
    Id sessionId = idF.createId(sessId);
    STALRequestBroker stal = getStal(sessionId);

    if (stal == null) {
      String msg = "Failed to get STAL for session " + sessId;
      log.error(msg);
      GetHashDataInputFaultType faultInfo = new GetHashDataInputFaultType();
      faultInfo.setErrorCode(1);
      faultInfo.setErrorMessage(msg);
      throw new GetHashDataInputFault(msg, faultInfo);
    } else {
      GetHashDataInputResponseType response = new GetHashDataInputResponseType();
      response.setSessionId(sessId);

      HashDataInputCallback hashDataInput = stal.getHashDataInput();
      if (TEST_SESSION_ID.equals(sessId)) {
        log
            .debug("Received TestSession GetHashDataInput, setting dummy HashDataInputCallback");
        hashDataInput = new HashDataInputCallback() {

          @Override
          public InputStream getHashDataInput(String referenceId) {
            byte[] hd = ("dummyhashdatainput_" + referenceId).getBytes();
            return new ByteArrayInputStream(hd);
          }
        };
      }
      if (hashDataInput != null) {
        List<GetHashDataInputType.Reference> references = request
            .getReference();
        for (GetHashDataInputType.Reference reference : references) {
          String refId = reference.getID();
          if (log.isDebugEnabled()) {
            log.debug("Resolving HashDataInput for reference " + refId);
          }
          ByteArrayOutputStream baos = null;
          try {
            InputStream hdi = hashDataInput.getHashDataInput(refId);
            baos = new ByteArrayOutputStream(hdi.available());
            int c;
            while ((c = hdi.read()) != -1) {
              baos.write(c);
            }
            GetHashDataInputResponseType.Reference ref = new GetHashDataInputResponseType.Reference();
            ref.setID(refId);
            ref.setValue(baos.toByteArray());
            response.getReference().add(ref);
          } catch (IOException ex) {
            String msg = "Failed to get HashDataInput for reference " + refId;
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
      } else {
        log.warn("Could not resolve any HashDataInputs for session " + sessId
            + ", no callback provided.");
      }
      return response;
    }
  }

  private STALRequestBroker getStal(Id sessionId) {
    // log.warn("RETURNING DUMMY STAL REQUEST BROKER");
    // return new STALRequestBrokerImpl();

    MessageContext mCtx = wsContext.getMessageContext();
    ServletContext sCtx = (ServletContext) mCtx
        .get(MessageContext.SERVLET_CONTEXT);
    BindingProcessorManager bpMgr = (BindingProcessorManager) sCtx
        .getAttribute(BINDING_PROCESSOR_MANAGER);
    BindingProcessor bp = bpMgr.getBindingProcessor(sessionId);
    return (bp == null) ? null : (STALRequestBroker) bp.getSTAL();
  }
}
