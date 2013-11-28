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



package at.gv.egiz.mocca.id;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMValidateContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import at.buergerkarte.namespaces.securitylayer._1_2_3.AnyChildrenType;
import at.buergerkarte.namespaces.securitylayer._1_2_3.Base64XMLLocRefOptRefContentType;
import at.buergerkarte.namespaces.securitylayer._1_2_3.CreateXMLSignatureRequestType;
import at.buergerkarte.namespaces.securitylayer._1_2_3.DataObjectInfoType;
import at.buergerkarte.namespaces.securitylayer._1_2_3.InfoboxReadParamsBinaryFileType;
import at.buergerkarte.namespaces.securitylayer._1_2_3.InfoboxReadRequestType;
import at.buergerkarte.namespaces.securitylayer._1_2_3.MetaInfoType;
import at.buergerkarte.namespaces.securitylayer._1_2_3.ObjectFactory;
import at.buergerkarte.namespaces.securitylayer._1_2_3.TransformsInfoType;
import at.gv.egiz.bku.binding.FormParameter;
import at.gv.egiz.bku.binding.HTTPBindingProcessor;
import at.gv.egiz.bku.binding.HttpUtil;
import at.gv.egiz.bku.binding.InputDecoder;
import at.gv.egiz.bku.binding.InputDecoderFactory;
import at.gv.egiz.bku.slcommands.CreateXMLSignatureResult;
import at.gv.egiz.bku.slcommands.ErrorResult;
import at.gv.egiz.bku.slcommands.InfoboxReadResult;
import at.gv.egiz.bku.slcommands.SLCommand;
import at.gv.egiz.bku.slcommands.SLCommandFactory;
import at.gv.egiz.bku.slcommands.SLResult;
import at.gv.egiz.bku.slexceptions.SLCommandException;
import at.gv.egiz.bku.slexceptions.SLVersionException;

public class SAMLBindingProcessorImpl extends
    AbstractCommandSequenceBindingProcessor implements HTTPBindingProcessor {

  private static final Logger log = LoggerFactory
      .getLogger(SAMLBindingProcessorImpl.class);

  private String requestContentType;

  private String domainIdentifier = "urn:publicid:gv.at:wbpk+FN+468924i";

  private String keyBoxIdentifier = "SecureSignatureKeypair";
  
  private QESTemplates templates = new QESTemplates();

  private IdLink idLink;

  private Element signature;

  private SLResult errorResponse;

  @Override
  protected void processResult(SLResult result) {
    if (result instanceof ErrorResult) {
      ErrorResult errorResult = (ErrorResult) result;
      log.info("Got ErrorResponse {}: {}", errorResult.getErrorCode(),
          errorResult.getInfo());
      errorResponse = result;
      return;
    } else if (result instanceof InfoboxReadResult) {
      try {
        processInfoboxReadResult((InfoboxReadResult) result);
        if (idLink != null) {
          try {
            IdLinkPersonData personData = idLink.getPersonData();
            log.info("Got idLink for {}.", personData);
          } catch (MarshalException e) {
            log.info("Failed to unmarshal idLink.");
          }
        }
      } catch (JAXBException e) {
        log.info("InfoboxReadResult contains unexpected data.", e);
        errorResponse = result;
      } catch (IdLinkException e) {
        log.info("InfoboxReadResult contains invalid identity link.", e);
        errorResponse = result;
      }
    } else if (result instanceof CreateXMLSignatureResult) {
      signature = ((CreateXMLSignatureResult) result).getContent();
      log.info("Got signature.");
      boolean valid = validate(signature) && validate(idLink);
      log.info("Signature is valid: " + valid);
    }
  }

  @Override
  protected SLCommand getNextCommand() {

    JAXBElement<?> request = null;
    if (errorResponse == null) {
      if (idLink == null) {
        request = createReadInfoboxRequest(domainIdentifier);
      } else if (signature == null) {
        request = createXMLSignatureRequest();
      }
    }

    if (request != null) {
      SLCommandFactory commandFactory = SLCommandFactory.getInstance();
      try {
        return commandFactory.createSLCommand(request);
      } catch (SLCommandException e) {
        log.error("Failed to create SLCommand.", e);
        setError(e);
      } catch (SLVersionException e) {
        log.error("Failed to create SLCommand.", e);
        setError(e);
      }
    }

    return null;
  }

  protected void processInfoboxReadResult(InfoboxReadResult result)
      throws JAXBException, IdLinkException {

    Object object = result.getContent();
    if (object instanceof byte[]) {
      log.info("InfoboxReadResult contains unexpected binary data.");
      errorResponse = result;
      return;
    } else if (object instanceof List<?>) {
      JAXBException exception = null;
      for (Object content : (List<?>) object) {
        if (content instanceof Element) {
          try {
            idLink = IdLinkFactory.getInstance().unmarshallIdLink(
                (Element) content);
            return;
          } catch (JAXBException e) {
            exception = e;
          }
        }
      }
      if (exception != null) {
        throw exception;
      }
    }

  }

  @Override
  public void setHTTPHeaders(Map<String, String> headerMap) {
    for (String header : headerMap.keySet()) {
      if (HttpUtil.HTTP_HEADER_CONTENT_TYPE.equalsIgnoreCase(header)) {
        requestContentType = headerMap.get(header);
      }
    }
  }

  @Override
  public void consumeRequestStream(String url, InputStream is) {
    InputDecoder inputDecoder = InputDecoderFactory.getDecoder(
        requestContentType, is);
    Iterator<FormParameter> fpi = inputDecoder.getFormParameterIterator();
    while (fpi.hasNext()) {
      FormParameter formParameter = fpi.next();
      if ("BKUUrl".equals(formParameter.getFormParameterName())) {
        setExternal(true);
      }
    }
  }

  @Override
  public String getResultContentType() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void writeResultTo(OutputStream os, String encoding)
      throws IOException {
    // TODO Auto-generated method stub

  }

  protected JAXBElement<InfoboxReadRequestType> createReadInfoboxRequest(
      String domainIdentifier) {

    ObjectFactory factory = new ObjectFactory();

    InfoboxReadRequestType infoboxReadRequestType = factory
        .createInfoboxReadRequestType();
    infoboxReadRequestType.setInfoboxIdentifier("IdentityLink");

    InfoboxReadParamsBinaryFileType infoboxReadParamsBinaryFileType = factory
        .createInfoboxReadParamsBinaryFileType();
    infoboxReadParamsBinaryFileType.setContentIsXMLEntity(true);
    infoboxReadRequestType
        .setBinaryFileParameters(infoboxReadParamsBinaryFileType);

    if (domainIdentifier != null) {
      JAXBElement<String> identityLinkDomainIdentifier = factory
          .createIdentityLinkDomainIdentifier(domainIdentifier);
      AnyChildrenType anyChildrenType = factory.createAnyChildrenType();
      anyChildrenType.getAny().add(identityLinkDomainIdentifier);

      infoboxReadRequestType.setBoxSpecificParameters(anyChildrenType);
    }

    return factory.createInfoboxReadRequest(infoboxReadRequestType);

  }

  protected JAXBElement<CreateXMLSignatureRequestType> createXMLSignatureRequest() {

    ObjectFactory factory = new ObjectFactory();

    CreateXMLSignatureRequestType createXMLSignatureRequest = factory
        .createCreateXMLSignatureRequestType();
    createXMLSignatureRequest.setKeyboxIdentifier(keyBoxIdentifier);

    DataObjectInfoType dataObjectInfoType = factory.createDataObjectInfoType();
    dataObjectInfoType.setStructure("enveloping");

    TransformsInfoType transformsInfoType = factory.createTransformsInfoType();
    MetaInfoType metaInfoType = factory.createMetaInfoType();
    metaInfoType.setMimeType("application/xhtml+xml");
    transformsInfoType.setFinalDataMetaInfo(metaInfoType);

    dataObjectInfoType.getTransformsInfo().add(transformsInfoType);

    Base64XMLLocRefOptRefContentType contentType = factory
        .createBase64XMLLocRefOptRefContentType();

    PersonalIdentifier identifier;
    try {
      identifier = idLink.getPersonData().getIdentifier();
    } catch (MarshalException e) {
      setError(e);
      return null;
    }
    if ("urn:publicid:gv.at:baseid".equals(identifier.getType())) {
      identifier = identifier.getDerivedValue(domainIdentifier);
    }
    String template = templates.createQESTemplate("test", locale, idLink, "",
        identifier, new Date());

    contentType.setBase64Content(template.getBytes(Charset.forName("UTF-8")));

    dataObjectInfoType.setDataObject(contentType);

    createXMLSignatureRequest.getDataObjectInfo().add(dataObjectInfoType);

    return factory.createCreateXMLSignatureRequest(createXMLSignatureRequest);

  }

  protected boolean validate(IdLink idLink) {
    try {
      if (domainIdentifier != null && domainIdentifier.startsWith("urn:publicid:gv.at:ccid")) {
        if (!idLink.verifyManifest()) {
          log.info("Identity link manifest verification failed.");
          return false;
        }
      }
      if (idLink.verifySignature()) {
        return true;
      }
    } catch (MarshalException e) {
      log.info("Identity link signature verification failed.", e);
    } catch (XMLSignatureException e) {
      log.info("Identity link signature verification failed.", e);
    } 
    log.info("Identity link signature verification failed.");
    return false;
  }
  
  
  protected boolean validate(Element signature) {

    Document doc = signature.getOwnerDocument();
    if (signature != signature.getOwnerDocument().getDocumentElement()) {
      doc.replaceChild(signature, doc.getDocumentElement());
    }

    XMLSignatureFactory xmlSignatureFactory = XMLSignatureFactory.getInstance();

    try {
      IdLinkKeySelector keySelector = new IdLinkKeySelector(idLink);
      DOMValidateContext validateContext = new DOMValidateContext(keySelector, signature);
      
      XMLSignature xmlSignature = xmlSignatureFactory
          .unmarshalXMLSignature(validateContext);

      return xmlSignature.validate(validateContext);
    } catch (MarshalException e) {
      log.info("Failed to unmarshall signature.", e);
    } catch (XMLSignatureException e) {
      log.info("Failed to validate signature.", e);
    }
    return false;
  }

  @Override
  public InputStream getFormData(String parameterName) {
    if ("appletPage".equals(parameterName)) {
      String appletPage = (isExternal()) ? "local.jsp" : "applet.jsp";
      return new ByteArrayInputStream(appletPage.getBytes());
    }
    return null;
  }

  @Override
  public String getRedirectURL() {
    return null;
  }

  @Override
  public int getResponseCode() {
    return HttpServletResponse.SC_OK;
  }

  @Override
  public Map<String, String> getResponseHeaders() {
    return Collections.emptyMap();
  }

}
