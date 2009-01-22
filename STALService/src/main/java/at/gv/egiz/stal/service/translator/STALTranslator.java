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
package at.gv.egiz.stal.service.translator;

import at.gv.egiz.stal.ErrorResponse;
import at.gv.egiz.stal.InfoboxReadRequest;
import at.gv.egiz.stal.InfoboxReadResponse;
import at.gv.egiz.stal.QuitRequest;
import at.gv.egiz.stal.STALRequest;
import at.gv.egiz.stal.STALResponse;
import at.gv.egiz.stal.SignRequest;
import at.gv.egiz.stal.SignResponse;
import at.gv.egiz.stal.service.types.ErrorResponseType;
import at.gv.egiz.stal.service.types.InfoboxReadRequestType;
import at.gv.egiz.stal.service.types.InfoboxReadResponseType;
import at.gv.egiz.stal.service.types.ObjectFactory;
import at.gv.egiz.stal.service.types.QuitRequestType;
import at.gv.egiz.stal.service.types.RequestType;
import at.gv.egiz.stal.service.types.ResponseType;
import at.gv.egiz.stal.service.types.SignRequestType;
import at.gv.egiz.stal.service.types.SignResponseType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.JAXBElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author Clemens Orthacker <clemens.orthacker@iaik.tugraz.at>
 */
public class STALTranslator {

  private static final Log log = LogFactory.getLog(STALTranslator.class);
  protected Map<Class, TranslationHandler> handlerMap = new HashMap<Class, TranslationHandler>();

  public STALTranslator() {
    registerTranslationHandler(new DefaultTranslationHandler());
  }

  /**
   * overwrites previously registered handlers for the same type
   * @param handler
   */
  public void registerTranslationHandler(TranslationHandler handler) {
    for (Class t : handler.getSupportedTypes()) {
      if (log.isDebugEnabled()) {
        log.debug("register " + t + " with translation handler " + handler.getClass());
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
    log.error("unknown STAL request type " + request.getClass());
    throw new TranslationException(request.getClass());
  }

  /**
   * Translate a STAL Webservice request to a STAL request.
   * @param request
   * @return
   * @throws at.gv.egiz.stal.service.translator.TranslationException
   */
  public STALRequest translate(JAXBElement<? extends RequestType> request) throws TranslationException {
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
    log.error("unknown request type " + req.getClass());
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
    log.error("unknown STAL response type " + response.getClass());
    throw new TranslationException(response.getClass());
  }

  /**
   * Translate a STAL Webservice response to a STAL response.
   * @param request
   * @return
   * @throws at.gv.egiz.stal.service.translator.TranslationException
   */
  public STALResponse translate(JAXBElement<? extends ResponseType> response) throws TranslationException {
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
    log.error("unknown response type " + resp.getClass());
    throw new TranslationException(resp.getClass());
  }

  /**
   * public (static) interface implemented by STAL extensions
   */
  public static interface TranslationHandler {

    List<Class> getSupportedTypes();

    JAXBElement<? extends RequestType> translate(STALRequest request) throws TranslationException;

    STALRequest translate(RequestType request) throws TranslationException;

    JAXBElement<? extends ResponseType> translate(STALResponse response) throws TranslationException;

    STALResponse translate(ResponseType response) throws TranslationException;
  }


  /**
   * Default Handler 
   */
  protected static class DefaultTranslationHandler implements TranslationHandler {

    private static final Log log = LogFactory.getLog(DefaultTranslationHandler.class);
    private ObjectFactory of;

    public DefaultTranslationHandler() {
      of = new ObjectFactory();
    }

    @Override
    public List<Class> getSupportedTypes() {
//      log.warn(" *** Fake STALTranslator ***");
      return Arrays.asList(new Class[]{InfoboxReadRequest.class,
                SignRequest.class,
                QuitRequest.class,
                InfoboxReadRequestType.class,
                SignRequestType.class,
                QuitRequestType.class,
                InfoboxReadResponse.class,
                SignResponse.class,
                ErrorResponse.class,
                InfoboxReadResponseType.class,
                SignResponseType.class,
                ErrorResponseType.class
      });
    }

    @Override
    public JAXBElement<? extends RequestType> translate(STALRequest request) throws TranslationException {
      log.trace("translate " + request.getClass());
      if (request instanceof SignRequest) {
        SignRequestType req = of.createSignRequestType();
        req.setKeyIdentifier(((SignRequest) request).getKeyIdentifier());
        req.setSignedInfo(((SignRequest) request).getSignedInfo());
        //TODO add hashdatainput (refactor signRequestType)
        return of.createGetNextRequestResponseTypeSignRequest(req);
      } else if (request instanceof InfoboxReadRequest) {
        InfoboxReadRequestType req = of.createInfoboxReadRequestType();
        req.setInfoboxIdentifier(((InfoboxReadRequest) request).getInfoboxIdentifier());
        req.setDomainIdentifier(((InfoboxReadRequest) request).getDomainIdentifier());
        return of.createGetNextRequestResponseTypeInfoboxReadRequest(req);
      } else if (request instanceof QuitRequest) {
        return of.createGetNextRequestResponseTypeQuitRequest(of.createQuitRequestType());
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
        stalReq.setSignedInfo(((SignRequestType) request).getSignedInfo());
        return stalReq;
      } else if (request instanceof QuitRequestType) {
        return new QuitRequest();
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
      }
      throw new TranslationException(response.getClass());
    }
  }
}

