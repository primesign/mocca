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

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Locale;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import at.gv.egiz.bku.binding.BindingProcessorManager;
import at.gv.egiz.bku.binding.FormParameter;
import at.gv.egiz.bku.binding.Id;
import at.gv.egiz.bku.binding.InputDecoder;
import at.gv.egiz.bku.binding.InputDecoderFactory;
import at.gv.egiz.bku.online.webapp.MoccaParameterBean;
import at.gv.egiz.bku.slcommands.SLCommand;
import at.gv.egiz.bku.slcommands.SLMarshallerFactory;
import at.gv.egiz.bku.slcommands.SLResult;
import at.gv.egiz.bku.slcommands.impl.DomCreateXMLSignatureResultImpl;
import at.gv.egiz.bku.slcommands.impl.DomErrorResultImpl;
import at.gv.egiz.bku.slcommands.impl.DomInfoboxReadResultImpl;
import at.gv.egiz.bku.slcommands.impl.ErrorResultImpl;
import at.gv.egiz.bku.slcommands.impl.SLCommandImpl;
import at.gv.egiz.bku.slexceptions.SLCommandException;
import at.gv.egiz.bku.utils.DebugInputStream;
import at.gv.egiz.bku.utils.StreamUtil;
import at.gv.egiz.org.apache.tomcat.util.http.AcceptLanguage;
import at.gv.egiz.slbinding.SLUnmarshaller;

public class DataURLServerServlet extends HttpServlet {
  
  private static Logger log = LoggerFactory.getLogger(DataURLServerServlet.class);
  
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  /* (non-Javadoc)
   * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
   */
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    
    BindingProcessorManager bindingProcessorManager = (BindingProcessorManager) getServletContext()
        .getAttribute("bindingProcessorManager");
    if (bindingProcessorManager == null) {
      String msg = "Configuration error: BindingProcessorManager missing!";
      log.error(msg);
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, msg);
      return;
    }

    Id id = (Id) req.getAttribute("id");
    if (id == null) {
      String msg = "No request id! Configuration error: ServletFilter missing?"; 
      log.error(msg);
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, msg);
      return;
    }
    
    // if binding processor with same id is present: remove
    bindingProcessorManager.removeBindingProcessor(id);

    String userAgent = req.getHeader("User-Agent");
    String contentType = req.getContentType();
    log.debug("Content-Type: " + contentType + " User-Agent: " + userAgent);

    InputDecoder dec = InputDecoderFactory.getDecoder(contentType, req.getInputStream());
    
    String sessionId = null;
    Element respElement = null;
    
    Iterator<FormParameter> formParams = dec.getFormParameterIterator();
    while(formParams.hasNext()) {
      FormParameter parameter = formParams.next();
      String name = parameter.getFormParameterName();
      if ("SessionID_".equals(name)) {
        sessionId = StreamUtil.asString(parameter.getFormParameterValue(), "UTF-8");
        log.debug("SessionID: {}", sessionId);
      } else if ("ResponseType".equals(name)) {
        String parameterContentType = parameter.getFormParameterContentType();
        if (log.isDebugEnabled()) {
          log.debug("ResponseType: ({}) {}.", parameterContentType, StreamUtil.asString(parameter.getFormParameterValue(), "UTF-8"));
        }
      } else if ("XMLResponse".equals(name)) {
        InputStream inputStream = parameter.getFormParameterValue();
        
        DebugInputStream di = null;
        if (log.isDebugEnabled()) {
          di = new DebugInputStream(inputStream);
          inputStream = di;
        }
        
        SLUnmarshaller slUnmarshaller = new SLUnmarshaller();
        
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        dbf.setSchema(slUnmarshaller.getSlSchema());
        try {
          dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        } catch (ParserConfigurationException e) {
          log.warn("Failed to enable secure processing.", e);
        }
        
        // http://www.w3.org/TR/xmldsig-bestpractices/#be-aware-schema-normalization
        try {
          dbf.setAttribute("http://apache.org/xml/features/validation/schema/normalized-value", Boolean.FALSE);
        } catch (IllegalArgumentException e) {
          log.warn("Failed to disable schema normalization " +
                "(see http://www.w3.org/TR/xmldsig-bestpractices/#be-aware-schema-normalization)", e);
        }
        
        DocumentBuilder documentBuilder;
        try {
          documentBuilder = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
          log.error("Failed to create parser for Security Layer response." , e);
          resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
          return;
        }
        
        try {
          Document doc = documentBuilder.parse(inputStream);
          respElement = doc.getDocumentElement();
        } catch (SAXException e) {
          log.info("Failed to parse Security Layer response.", e);
          // TODO set error and redirect 
          resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
          return;
        }

        if (di != null) {
          log.debug("XMLResponse:\n{}", new String(di.getBufferedBytes(), "UTF-8"));
        }
        
      }
      
    }
    
    Locale locale = AcceptLanguage.getLocale(req.getHeader("Accept-Language"));
    if (log.isInfoEnabled()) {
      log.info("Recieved request (Accept-Language locale: {}).", locale);
    }
    
    // create new binding processor
    String protocol = MoccaParameterBean.getInitParameter("protocol", getServletConfig(), getServletContext());
    if (protocol == null || protocol.isEmpty()) {
      protocol = req.getScheme();
    }
    SAMLBindingProcessorImpl bindingProcessor = (SAMLBindingProcessorImpl) bindingProcessorManager
        .createBindingProcessor(protocol, locale);

    if (bindingProcessor != null && respElement != null) {
      
      SLResult slResult = null;
      if ("http://www.buergerkarte.at/namespaces/securitylayer/1.2#".equals(respElement.getNamespaceURI())) {
        if ("NullOperationResponse".equals(respElement.getLocalName())) {
          slResult = null;
        } else if ("InfoboxReadResponse".equals(respElement.getLocalName())) {
          slResult = new DomInfoboxReadResultImpl(respElement);
        } else if ("CreateXMLSignatureResponse".equals(respElement.getLocalName())) {
          slResult = new DomCreateXMLSignatureResultImpl(respElement);
        } else if ("ErrorResponse".equals(respElement.getLocalName())) {
          slResult = new DomErrorResultImpl(respElement);
        } else {
          // TODO: report proper error
          at.gv.egiz.bku.slexceptions.SLException slException = new at.gv.egiz.bku.slexceptions.SLException(0);
          slResult = new ErrorResultImpl(slException, null);
        }
        
      }
      
      SLCommand slCommand = null;
      try {
        slCommand = bindingProcessor.setExternalResult(slResult);
      } catch (SLCommandException e) {
        log.debug(e.getMessage());
      } catch (InterruptedException e) {
        // interrupted 
      }
       
      if (slCommand instanceof SLCommandImpl<?>) {
        JAXBElement<?> request = ((SLCommandImpl<?>) slCommand).getRequest();
        Marshaller marshaller = SLMarshallerFactory.getInstance().createMarshaller(false, false);
        try {

          resp.setCharacterEncoding("UTF-8");
          resp.setContentType("text/xml");
          
          marshaller.marshal(request, resp.getOutputStream());
          
          return;
          
        } catch (JAXBException e) {
          log.error("Failed to marshall Security Layer request.", e);
        }
        
      }

    }

    resp.sendRedirect("ui;jsessionid=" + id.toString());
    
  }    

}
