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


package at.gv.egiz.bku.slcommands.impl;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.Locale;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import at.buergerkarte.namespaces.securitylayer._1_2_3.ErrorResponseType;
import at.buergerkarte.namespaces.securitylayer._1_2_3.ObjectFactory;
import at.gv.egiz.bku.slcommands.SLMarshallerFactory;
import at.gv.egiz.bku.slcommands.SLResult;
import at.gv.egiz.bku.slexceptions.SLBindingException;
import at.gv.egiz.bku.slexceptions.SLCommandException;
import at.gv.egiz.bku.slexceptions.SLException;
import at.gv.egiz.bku.slexceptions.SLRuntimeException;
import at.gv.egiz.bku.slexceptions.SLVersionException;
import at.gv.egiz.bku.utils.DebugOutputStream;
import at.gv.egiz.bku.utils.DebugWriter;

/**
 * This class serves as an abstract base class for the implementation of a
 * security layer result.
 * 
 * @author mcentner
 */
public abstract class SLResultImpl implements SLResult {

  /**
   * Logging facility.
   */
  private final Logger log = LoggerFactory.getLogger(SLResult.class);

  /**
   * The security layer result type (default = XML).
   */
  protected SLResultType resultType = SLResultType.XML;
  
  /**
   * The security layer result MIME-type (default = <code>text/xml</code>).
   */
  protected String resultingMimeType = "text/xml";

  /* (non-Javadoc)
   * @see at.gv.egiz.bku.slcommands.SLResult#getResultType()
   */
  public SLResultType getResultType() {
    return resultType;
  }

  /* (non-Javadoc)
   * @see at.gv.egiz.bku.slcommands.SLResult#getMimeType()
   */
  public String getMimeType() {
    return resultingMimeType;
  }

  @Override
  public void writeTo(Result result, boolean fragment) {
    writeTo(result, null, false);
  }

  @Override
  public abstract void writeTo(Result result, Templates templates, boolean fragment);

  private TransformerHandler getTransformerHandler(Templates templates, Result result) throws SLException {
    try {
      SAXTransformerFactory transformerFactory = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
      transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
      TransformerHandler transformerHandler = transformerFactory.newTransformerHandler(templates);
      transformerHandler.setResult(result);
      return transformerHandler;
    } catch (TransformerFactoryConfigurationError e) {
      log.error("Failed to create an instance of SAXTransformerFactory.", e);
      throw new SLBindingException(2000);
    } catch (IllegalArgumentException e) {
      log.error("Failed to set result for transformation.", e);
      throw new SLBindingException(2000);
    } catch (TransformerConfigurationException e) {
      log.info("Failed to create an instance of SAXTransformerFactory.", e);
      throw new SLBindingException(2008);
    }
  }

  /**
   * Writes the given <code>response</code> to the SAX <code>result</code> using
   * the given transform <code>templates</code>.
   * 
   * @param response
   * @param result
   * @param templates
   */
  protected void writeTo(JAXBElement<?> response, Result result, Templates templates, boolean fragment) {
    
    DebugWriter dw = null;
    DebugOutputStream ds = null;
    if (log.isTraceEnabled() && result instanceof StreamResult) {
      StreamResult streamResult = (StreamResult) result;
      if (streamResult.getOutputStream() != null) {
        ds = new DebugOutputStream(streamResult.getOutputStream());
        streamResult.setOutputStream(ds);
      }
      if (streamResult.getWriter() != null) {
        dw = new DebugWriter(streamResult.getWriter());
        streamResult.setWriter(dw);
      }
    }

    TransformerHandler transformerHandler = null;
    if (templates != null) {
      try {
        transformerHandler = getTransformerHandler(templates, result);
      } catch (SLException e) {
        writeErrorTo(e, result, templates, fragment);
      }
    }
    
    Marshaller marshaller = SLMarshallerFactory.getInstance().createMarshaller(true);
    try {
      if (transformerHandler != null) {
        marshaller.marshal(response, transformerHandler);
      } else {
        marshaller.marshal(response, result);
      }
    } catch (JAXBException e) {
      log.info("Failed to marshall {} result.", response.getName(), e);
      SLCommandException commandException = new SLCommandException(4000);
      writeErrorTo(commandException, result, templates, fragment);
    }
    
    if (ds != null) {
      try {
        log.trace("Marshalled result:\n{}", new String(ds.getBufferedBytes(), "UTF-8"));
      } catch (UnsupportedEncodingException e) {
        log.trace(e.getMessage());
      }
    }
    
    if (dw != null) {
      log.trace("Marshalled result:\n{}", dw.getBufferedString());
    }
    
  }
  
  protected void writeTo(Node node, Result result, Templates templates, boolean fragment) {

    DebugWriter dw = null;
    DebugOutputStream ds = null;
    if (log.isTraceEnabled() && result instanceof StreamResult) {
      StreamResult streamResult = (StreamResult) result;
      if (streamResult.getOutputStream() != null) {
        ds = new DebugOutputStream(streamResult.getOutputStream());
        streamResult.setOutputStream(ds);
      }
      if (streamResult.getWriter() != null) {
        dw = new DebugWriter(streamResult.getWriter());
        streamResult.setWriter(dw);
      }
    }

    if (templates == null) {
      try {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        Transformer transformer = transformerFactory.newTransformer();
        if (fragment) {
          transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        }
        transformer.transform(new DOMSource(node), result);
      } catch (TransformerConfigurationException e) {
        log.error("Failed to create Transformer.", e);
        writeErrorTo(new SLException(4000), result, null, fragment);
      } catch (TransformerException e) {
        log.error("Failed to transform result.", e);
        writeErrorTo(new SLException(4000), result, null, fragment);
      }
    } else {
      try {
        Transformer transformer = templates.newTransformer();
        if (fragment) {
          transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        }
        transformer.transform(new DOMSource(node), result);
      } catch (TransformerConfigurationException e) {
        log.info("Failed to create transformer.", e);
        writeErrorTo(new SLException(2008), result, templates, fragment);
      } catch (TransformerException e) {
        log.error("Failed to transform result.", e);
        writeErrorTo(new SLException(2008), result, templates, fragment);
      }
    }

    if (ds != null) {
      try {
        log.trace("Marshalled result:\n{}", new String(ds.getBufferedBytes(), "UTF-8"));
      } catch (UnsupportedEncodingException e) {
        log.trace(e.getMessage());
      }
    }
    
    if (dw != null) {
      log.trace("Marshalled result:\n{}", dw.getBufferedString());
    }

  }
  
  protected void writeErrorTo(SLException slException, Result result, Templates templates, boolean fragment) {
    writeErrorTo(slException, result, templates, Locale.getDefault(), fragment);
  }
  
  protected void writeErrorTo(SLException slException, Result result, Templates templates, Locale locale, boolean fragment) {
    
    TransformerHandler transformerHandler = null;
    if (templates != null) {
      try {
        transformerHandler = getTransformerHandler(templates, result);
      } catch (SLException e) {
        // write the exception thrown instead of the given one
        slException = e;
      }
    }

    Object response;
    
    Marshaller marshaller;
    if (slException instanceof SLVersionException
        && ("http://www.buergerkarte.at/namespaces/securitylayer/20020225#"
            .equals(((SLVersionException) slException).getNamespaceURI()) || 
            "http://www.buergerkarte.at/namespaces/securitylayer/20020831#"
            .equals(((SLVersionException) slException).getNamespaceURI()))) {
      // issue ErrorResponse in the legacy namespace
      at.buergerkarte.namespaces.securitylayer._20020225_.ObjectFactory factory 
          = new at.buergerkarte.namespaces.securitylayer._20020225_.ObjectFactory();
      at.buergerkarte.namespaces.securitylayer._20020225_.ErrorResponseType errorResponseType = factory
          .createErrorResponseType();
      errorResponseType.setErrorCode(BigInteger.valueOf(slException
          .getErrorCode()));
      errorResponseType.setInfo(slException.getLocalizedMessage(locale));
      response = factory.createErrorResponse(errorResponseType);
      marshaller = SLMarshallerFactory.getInstance().createLegacyMarshaller(true, fragment);
    } else {
      ObjectFactory factory = new ObjectFactory();
      ErrorResponseType responseType = factory.createErrorResponseType();
      responseType.setErrorCode(slException.getErrorCode());
      responseType.setInfo(slException.getLocalizedMessage(locale));
      response = factory.createErrorResponse(responseType);
      marshaller = SLMarshallerFactory.getInstance().createMarshaller(true, fragment);
    }
    
    try {
      if (transformerHandler != null) {
        marshaller.marshal(response, transformerHandler);
      } else {
        marshaller.marshal(response, result);
      }
    } catch (JAXBException e) {
      log.error("Failed to marshall error result." , e);
      throw new SLRuntimeException("Failed to marshall error result.");
    }
    
  }

}
