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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author clemens
 */
public class STALTranslator {

  protected static final Log log = LogFactory.getLog(STALTranslator.class);

  public static List<STALRequest> translateRequests(List<RequestType> requests) {
    List<STALRequest> stalRequests = new ArrayList<STALRequest>(requests.size());
    for (RequestType request : requests) {
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

  public static List<ResponseType> fromSTAL(List<STALResponse> stalResponses) {
    List<ResponseType> responses = new ArrayList<ResponseType>(stalResponses.size());
    for (STALResponse stalResp : stalResponses) {
      if (stalResp instanceof InfoboxReadResponse) {
        InfoboxReadResponseType resp = new InfoboxReadResponseType();
        resp.setInfoboxValue(((InfoboxReadResponse) stalResp).getInfoboxValue());
        responses.add(resp);
      } else if (stalResp instanceof SignResponse) {
        SignResponseType resp = new SignResponseType();
        resp.setSignatureValue(((SignResponse) stalResp).getSignatureValue());
        responses.add(resp);
      } else if (stalResp instanceof ErrorResponse) {
        ErrorResponseType resp = new ErrorResponseType();
        resp.setErrorCode(((ErrorResponse) stalResp).getErrorCode());
        resp.setErrorMessage(((ErrorResponse) stalResp).getErrorMessage());
        responses.add(resp);
      } else {
        log.error("unknown STAL response type: " + stalResp.getClass());
        ErrorResponseType resp = new ErrorResponseType(); 
        resp.setErrorCode(4000);
        resp.setErrorMessage("unknown STAL response type: " + stalResp.getClass());
        responses = Collections.singletonList((ResponseType) resp);
        break;
      }
    }
    return responses;
  }
  
  public static List<STALResponse> toSTAL(List<ResponseType> responses) {
    List<STALResponse> stalResponses = new ArrayList<STALResponse>(responses.size());
    for (ResponseType resp : responses) {
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
