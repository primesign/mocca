/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package at.gv.egiz.stal.util;

import at.gv.egiz.stal.ErrorResponse;
import at.gv.egiz.stal.InfoboxReadRequest;
import at.gv.egiz.stal.InfoboxReadResponse;
import at.gv.egiz.stal.QuitRequest;
import at.gv.egiz.stal.STALRequest;
import at.gv.egiz.stal.STALResponse;
import at.gv.egiz.stal.SignRequest;
import at.gv.egiz.stal.SignResponse;
import at.gv.egiz.stal.service.types.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.JAXBElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author clemens
 */
public class STALTranslator {

  protected static final Log log = LogFactory.getLog(STALTranslator.class);

  public static List<STALRequest> translateRequests(List<JAXBElement<? extends RequestType>> requests) {
    List<STALRequest> stalRequests = new ArrayList<STALRequest>(requests.size());
    for (JAXBElement<? extends RequestType> requestElt : requests) {
      RequestType request = requestElt.getValue();
      if (request instanceof InfoboxReadRequestType) {
        InfoboxReadRequest stalReq = new InfoboxReadRequest();
        stalReq.setDomainIdentifier(((InfoboxReadRequestType) request).getDomainIdentifier());
        stalReq.setInfoboxIdentifier(((InfoboxReadRequestType) request).getInfoboxIdentifier());
        stalRequests.add(stalReq);
      } else if (request instanceof SignRequestType) {
        SignRequest stalReq = new SignRequest();
        stalReq.setKeyIdentifier(((SignRequestType) request).getKeyIdentifier());
        stalReq.setSignedInfo(((SignRequestType) request).getSignedInfo());
        stalRequests.add(stalReq);
      } else if (request instanceof QuitRequestType) {
        stalRequests.add(new QuitRequest());
      } else {
        log.error("unknown STALService request type: " + request.getClass());
        stalRequests = Collections.singletonList((STALRequest) new QuitRequest());
        break;
      }
    }
    return stalRequests;
  }

  public static List<JAXBElement<? extends ResponseType>> fromSTAL(List<STALResponse> stalResponses) {
    ObjectFactory stalObjFactory = new ObjectFactory();
    List<JAXBElement<? extends ResponseType>> responses = new ArrayList<JAXBElement<? extends ResponseType>>(stalResponses.size());
    for (STALResponse stalResp : stalResponses) {
      if (stalResp instanceof InfoboxReadResponse) {
        InfoboxReadResponseType resp = stalObjFactory.createInfoboxReadResponseType();
        resp.setInfoboxValue(((InfoboxReadResponse) stalResp).getInfoboxValue());
        responses.add(stalObjFactory.createGetNextRequestTypeInfoboxReadResponse(resp));
      } else if (stalResp instanceof SignResponse) {
        SignResponseType resp = stalObjFactory.createSignResponseType();
        resp.setSignatureValue(((SignResponse) stalResp).getSignatureValue());
        responses.add(stalObjFactory.createGetNextRequestTypeSignResponse(resp));
      } else if (stalResp instanceof ErrorResponse) {
        ErrorResponseType resp = stalObjFactory.createErrorResponseType();
        resp.setErrorCode(((ErrorResponse) stalResp).getErrorCode());
        resp.setErrorMessage(((ErrorResponse) stalResp).getErrorMessage());
        responses.add(stalObjFactory.createGetNextRequestTypeErrorResponse(resp));
      } else {
        log.error("unknown STAL response type: " + stalResp.getClass());
        ErrorResponseType resp = stalObjFactory.createErrorResponseType();
        resp.setErrorCode(4000);
        resp.setErrorMessage("unknown STAL response type: " + stalResp.getClass());
        responses.clear();
        responses.add(stalObjFactory.createGetNextRequestTypeErrorResponse(resp));
        break;
      }
    }
    return responses;
  }
  
  public static List<STALResponse> toSTAL(List<JAXBElement<? extends ResponseType>> responses) {
    List<STALResponse> stalResponses = new ArrayList<STALResponse>(responses.size());
    for (JAXBElement<? extends ResponseType> respElt : responses) {
      ResponseType resp = respElt.getValue();
      if (resp instanceof InfoboxReadResponseType) {
        InfoboxReadResponse stalResp = new InfoboxReadResponse();
        stalResp.setInfoboxValue(((InfoboxReadResponseType) resp).getInfoboxValue());
        stalResponses.add(stalResp);
      } else if (resp instanceof SignResponseType) {
        SignResponse stalResp = new SignResponse();
        stalResp.setSignatureValue(((SignResponseType) resp).getSignatureValue());
        stalResponses.add(stalResp);
      } else if (resp instanceof ErrorResponseType) {
        ErrorResponse stalResp = new ErrorResponse();
        stalResp.setErrorCode(((ErrorResponseType) resp).getErrorCode());
        stalResp.setErrorMessage(((ErrorResponseType) resp).getErrorMessage());
        stalResponses.add(stalResp);
      } else {
        log.error("unknown STALService response type: " + resp.getClass());
        ErrorResponse stalResp = new ErrorResponse(); 
        stalResp.setErrorCode(4000);
        stalResp.setErrorMessage("unknown STALService response type: " + resp.getClass());
        stalResponses = Collections.singletonList((STALResponse) stalResp);
        break;
      }
    }
    return stalResponses;
  }
}
