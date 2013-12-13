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


package at.gv.egiz.stal.service.translator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.gv.egiz.stal.ErrorResponse;
import at.gv.egiz.stal.InfoboxReadRequest;
import at.gv.egiz.stal.InfoboxReadResponse;
import at.gv.egiz.stal.QuitRequest;
import at.gv.egiz.stal.STALRequest;
import at.gv.egiz.stal.STALResponse;
import at.gv.egiz.stal.SignRequest;
import at.gv.egiz.stal.SignRequest.ExcludedByteRange;
import at.gv.egiz.stal.SignRequest.SignedInfo;
import at.gv.egiz.stal.SignResponse;
import at.gv.egiz.stal.StatusRequest;
import at.gv.egiz.stal.StatusResponse;
import at.gv.egiz.stal.service.types.ErrorResponseType;
import at.gv.egiz.stal.service.types.InfoboxReadRequestType;
import at.gv.egiz.stal.service.types.InfoboxReadResponseType;
import at.gv.egiz.stal.service.types.ObjectFactory;
import at.gv.egiz.stal.service.types.QuitRequestType;
import at.gv.egiz.stal.service.types.RequestType;
import at.gv.egiz.stal.service.types.ResponseType;
import at.gv.egiz.stal.service.types.SignRequestType;
import at.gv.egiz.stal.service.types.SignResponseType;
import at.gv.egiz.stal.service.types.StatusRequestType;
import at.gv.egiz.stal.service.types.StatusResponseType;

/**
 *
 * @author Clemens Orthacker <clemens.orthacker@iaik.tugraz.at>
 */
public class STALTranslator {

  private final Logger log = LoggerFactory.getLogger(STALTranslator.class);
  protected Map<Class<?>, TranslationHandler> handlerMap = new HashMap<Class<?>, TranslationHandler>();

  public STALTranslator() {
    registerTranslationHandler(new DefaultTranslationHandler());
  }

  /**
   * overwrites previously registered handlers for the same type
   * @param handler
   */
  public void registerTranslationHandler(TranslationHandler handler) {
    for (Class<?> t : handler.getSupportedTypes()) {
      if (log.isTraceEnabled()) {
        log.trace("Register {} with translation handler {}.", t, handler.getClass());
      }
      handlerMap.put(t, handler);
    }
  }

  /**
   * Translate a STAL request to a STAL Webservice request.
   * @param request
   * @return
   * @throws at.gv.egiz.stal.service.translator.TranslationException
   */
  public JAXBElement<? extends RequestType> translate(STALRequest request) throws TranslationException {
    if (handlerMap.containsKey(request.getClass())) {
      TranslationHandler handler = handlerMap.get(request.getClass());
      JAXBElement<? extends RequestType> r = handler.translate(request);
      if (r != null) {
        return r;
      }
    }
    log.error("Unknown STAL request type {}.", request.getClass());
    throw new TranslationException(request.getClass());
  }

  /**
   * Translate a STAL Webservice request to a STAL request.
   * @param request
   * @return
   * @throws at.gv.egiz.stal.service.translator.TranslationException
   */
  public STALRequest translateWSRequest(JAXBElement<? extends RequestType> request) throws TranslationException {
    RequestType req = request.getValue();
    if (req == null) {
      throw new RuntimeException("RequestType must not be null");
    }
    if (handlerMap.containsKey(req.getClass())) {
      TranslationHandler handler = handlerMap.get(req.getClass());
      STALRequest stalRequest = handler.translate(req);
      if (stalRequest != null) {
        return stalRequest;
      }
    }
    log.error("Unknown request type {}.", req.getClass());
    throw new TranslationException(req.getClass());
  }

  /**
   * Translate a STAL response to a STAL Webservice response.
   * @param request
   * @return
   * @throws at.gv.egiz.stal.service.translator.TranslationException
   */
  public JAXBElement<? extends ResponseType> translate(STALResponse response) throws TranslationException {
    if (handlerMap.containsKey(response.getClass())) {
      TranslationHandler handler = handlerMap.get(response.getClass());
      JAXBElement<? extends ResponseType> r = handler.translate(response);
      if (r != null) {
        return r;
      }
    }
    log.error("Unknown STAL response type {}.", response.getClass());
    throw new TranslationException(response.getClass());
  }

  /**
   * Translate a STAL Webservice response to a STAL response.
   * @param request
   * @return
   * @throws at.gv.egiz.stal.service.translator.TranslationException
   */
  public STALResponse translateWSResponse(JAXBElement<? extends ResponseType> response) throws TranslationException {
    ResponseType resp = response.getValue();
    if (resp == null) {
      throw new RuntimeException("ResponseType must not be null");
    }
    if (handlerMap.containsKey(resp.getClass())) {
      TranslationHandler handler = handlerMap.get(resp.getClass());
      STALResponse stalResponse = handler.translate(resp);
      if (stalResponse != null) {
        return stalResponse;
      }
    }
    log.error("Unknown response type {}.", resp.getClass());
    throw new TranslationException(resp.getClass());
  }

  /**
   * public (static) interface implemented by STAL extensions
   */
  public static interface TranslationHandler {

    List<Class<?>> getSupportedTypes();

    JAXBElement<? extends RequestType> translate(STALRequest request) throws TranslationException;

    JAXBElement<? extends ResponseType> translate(STALResponse response) throws TranslationException;

    STALRequest translate(RequestType request) throws TranslationException;

    STALResponse translate(ResponseType response) throws TranslationException;
  }


  /**
   * Default Handler 
   */
  protected static class DefaultTranslationHandler implements TranslationHandler {

    private final Logger log = LoggerFactory.getLogger(DefaultTranslationHandler.class);
    private ObjectFactory of;

    public DefaultTranslationHandler() {
      of = new ObjectFactory();
    }

    @Override
    public List<Class<?>> getSupportedTypes() {
      return Arrays.asList(new Class<?>[]{InfoboxReadRequest.class,
                SignRequest.class,
                QuitRequest.class,
                StatusRequest.class,
                InfoboxReadRequestType.class,
                SignRequestType.class,
                QuitRequestType.class,
                StatusRequestType.class,
                InfoboxReadResponse.class,
                SignResponse.class,
                ErrorResponse.class,
                StatusResponse.class,
                InfoboxReadResponseType.class,
                SignResponseType.class,
                ErrorResponseType.class,
                StatusResponseType.class
      });
    }

    @Override
    public JAXBElement<? extends RequestType> translate(STALRequest request) throws TranslationException {
      log.trace("translate " + request.getClass());
      if (request instanceof SignRequest) {
        SignRequestType req = of.createSignRequestType();
        req.setKeyIdentifier(((SignRequest) request).getKeyIdentifier());
        SignRequestType.SignedInfo signedInfo = of.createSignRequestTypeSignedInfo();
        signedInfo.setValue(((SignRequest) request).getSignedInfo().getValue());
        signedInfo.setIsCMSSignedAttributes(((SignRequest) request).getSignedInfo().isIsCMSSignedAttributes());
        req.setSignedInfo(signedInfo);
        req.setSignatureMethod(((SignRequest) request).getSignatureMethod());
        req.setDigestMethod(((SignRequest) request).getDigestMethod());
        if (((SignRequest) request).getExcludedByteRange() != null) {
          SignRequestType.ExcludedByteRange excludedByteRange = of.createSignRequestTypeExcludedByteRange();
          excludedByteRange.setFrom(((SignRequest) request).getExcludedByteRange().getFrom());
          excludedByteRange.setTo(((SignRequest) request).getExcludedByteRange().getTo());
          req.setExcludedByteRange(excludedByteRange);
        }
        //TODO add hashdatainput (refactor signRequestType)
        return of.createGetNextRequestResponseTypeSignRequest(req);
      } else if (request instanceof InfoboxReadRequest) {
        InfoboxReadRequestType req = of.createInfoboxReadRequestType();
        req.setInfoboxIdentifier(((InfoboxReadRequest) request).getInfoboxIdentifier());
        req.setDomainIdentifier(((InfoboxReadRequest) request).getDomainIdentifier());
        return of.createGetNextRequestResponseTypeInfoboxReadRequest(req);
      } else if (request instanceof QuitRequest) {
        return of.createGetNextRequestResponseTypeQuitRequest(of.createQuitRequestType());
      } else if (request instanceof StatusRequest) {
        StatusRequestType req = of.createStatusRequestType();
        return of.createGetNextRequestResponseTypeStatusRequest(req);
      }
      throw new TranslationException(request.getClass());
    }

    @Override
    public STALRequest translate(RequestType request) throws TranslationException {
      if (request instanceof InfoboxReadRequestType) {
        InfoboxReadRequest stalReq = new InfoboxReadRequest();
        stalReq.setDomainIdentifier(((InfoboxReadRequestType) request).getDomainIdentifier());
        stalReq.setInfoboxIdentifier(((InfoboxReadRequestType) request).getInfoboxIdentifier());
        return stalReq;
      } else if (request instanceof SignRequestType) {
        SignRequest stalReq = new SignRequest();
        stalReq.setKeyIdentifier(((SignRequestType) request).getKeyIdentifier());
        SignedInfo signedInfo = new SignedInfo();
        signedInfo.setValue(((SignRequestType) request).getSignedInfo().getValue());
        signedInfo.setIsCMSSignedAttributes(((SignRequestType) request).getSignedInfo().isIsCMSSignedAttributes());
        stalReq.setSignedInfo(signedInfo);
        stalReq.setSignatureMethod(((SignRequestType) request).getSignatureMethod());
        stalReq.setDigestMethod(((SignRequestType) request).getDigestMethod());
        if (((SignRequestType) request).getExcludedByteRange() != null) {
          ExcludedByteRange excludedByteRange = new ExcludedByteRange();
          excludedByteRange.setFrom(((SignRequestType) request).getExcludedByteRange().getFrom());
          excludedByteRange.setTo(((SignRequestType) request).getExcludedByteRange().getTo());
          stalReq.setExcludedByteRange(excludedByteRange);
        }
        return stalReq;
      } else if (request instanceof QuitRequestType) {
        return new QuitRequest();
      } else if (request instanceof StatusRequestType) {
        return new StatusRequest();
      }
      throw new TranslationException(request.getClass());
    }

    @Override
    public JAXBElement<? extends ResponseType> translate(STALResponse response) throws TranslationException {
      if (response instanceof InfoboxReadResponse) {
        InfoboxReadResponseType resp = of.createInfoboxReadResponseType();
        resp.setInfoboxValue(((InfoboxReadResponse) response).getInfoboxValue());
        return of.createGetNextRequestTypeInfoboxReadResponse(resp);
      } else if (response instanceof SignResponse) {
        SignResponseType resp = of.createSignResponseType();
        resp.setSignatureValue(((SignResponse) response).getSignatureValue());
        return of.createGetNextRequestTypeSignResponse(resp);
      } else if (response instanceof ErrorResponse) {
        ErrorResponseType resp = of.createErrorResponseType();
        resp.setErrorCode(((ErrorResponse) response).getErrorCode());
        resp.setErrorMessage(((ErrorResponse) response).getErrorMessage());
        return of.createGetNextRequestTypeErrorResponse(resp);
      } else if (response instanceof StatusResponse) {
        StatusResponseType resp = of.createStatusResponseType();
        resp.setCardReady(((StatusResponse) response).isCardReady());
        return of.createGetNextRequestTypeStatusResponse(resp);
      }
      throw new TranslationException(response.getClass());
    }

    @Override
    public STALResponse translate(ResponseType response) throws TranslationException {
      if (response instanceof InfoboxReadResponseType) {
        InfoboxReadResponse stalResp = new InfoboxReadResponse();
        stalResp.setInfoboxValue(((InfoboxReadResponseType) response).getInfoboxValue());
        return stalResp;
      } else if (response instanceof SignResponseType) {
        SignResponse stalResp = new SignResponse();
        stalResp.setSignatureValue(((SignResponseType) response).getSignatureValue());
        return stalResp;
      } else if (response instanceof ErrorResponseType) {
        ErrorResponse stalResp = new ErrorResponse();
        stalResp.setErrorCode(((ErrorResponseType) response).getErrorCode());
        stalResp.setErrorMessage(((ErrorResponseType) response).getErrorMessage());
        return stalResp;
      } else if (response instanceof StatusResponseType) {
        StatusResponse stalResp = new StatusResponse();
        stalResp.setCardReady(((StatusResponseType) response).isCardReady());
        return stalResp;
      }
      throw new TranslationException(response.getClass());
    }
  }
}

