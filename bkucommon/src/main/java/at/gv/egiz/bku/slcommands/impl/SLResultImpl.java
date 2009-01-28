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
package at.gv.egiz.bku.slcommands.impl;

import java.io.UnsupportedEncodingException;
import java.util.Locale;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Node;

import at.buergerkarte.namespaces.securitylayer._1.ErrorResponseType;
import at.buergerkarte.namespaces.securitylayer._1.ObjectFactory;
import at.gv.egiz.marshal.NamespacePrefixMapperImpl;
import at.gv.egiz.bku.slcommands.SLCommandFactory;
import at.gv.egiz.bku.slcommands.SLResult;
import at.gv.egiz.bku.slexceptions.SLBindingException;
import at.gv.egiz.bku.slexceptions.SLCommandException;
import at.gv.egiz.bku.slexceptions.SLException;
import at.gv.egiz.bku.slexceptions.SLRuntimeException;
import at.gv.egiz.bku.utils.DebugOutputStream;
import at.gv.egiz.bku.utils.DebugWriter;
import at.gv.egiz.marshal.MarshallerFactory;
import javax.xml.bind.PropertyException;

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
  private static Log log = LogFactory.getLog(SLResult.class);

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

  private Marshaller getMarshaller() {
    try {
      JAXBContext context  = SLCommandFactory.getInstance().getJaxbContext();
      Marshaller marshaller = MarshallerFactory.createMarshaller(context, true);
      return marshaller;
    } catch (JAXBException e) {
      log.fatal("Failed to marshall error response.", e);
      throw new SLRuntimeException("Failed to marshall error response.", e);
    }
  }

  private TransformerHandler getTransformerHandler(Templates templates, Result result) throws SLException {
    try {
      SAXTransformerFactory transformerFactory = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
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

  @Override
  public void writeTo(Result result) {
    writeTo(result, null);
  }


  /**
   * Writes the given <code>response</code> to the SAX <code>result</code> using
   * the given transform <code>templates</code>.
   * 
   * @param response
   * @param result
   * @param templates
   */
  protected void writeTo(JAXBElement<?> response, Result result, Templates templates) {
    
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
        writeErrorTo(e, result, templates);
      }
    }
    
    Marshaller marshaller = getMarshaller();
    try {
      if (transformerHandler != null) {
        marshaller.marshal(response, transformerHandler);
      } else {
        marshaller.marshal(response, result);
      }
    } catch (JAXBException e) {
      log.info("Failed to marshall " + response.getName() + " result." , e);
      SLCommandException commandException = new SLCommandException(4000);
      writeErrorTo(commandException, result, templates);
    }
    
    if (ds != null) {
      try {
        log.trace("Marshalled result:\n" + new String(ds.getBufferedBytes(), "UTF-8"));
      } catch (UnsupportedEncodingException e) {
        log.trace(e.getMessage());
      }
    }
    
    if (dw != null) {
      log.trace("Marshalled result:\n" + dw.getBufferedString());
    }
    
  }
  
  protected void writeTo(Node node, Result result, Templates templates) {

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
        Transformer transformer = transformerFactory.newTransformer();
        transformer.transform(new DOMSource(node), result);
      } catch (TransformerConfigurationException e) {
        log.error("Failed to create Transformer.", e);
        writeErrorTo(new SLException(4000), result, null);
      } catch (TransformerException e) {
        log.error("Failed to transform result.", e);
        writeErrorTo(new SLException(4000), result, null);
      }
    } else {
      try {
        Transformer transformer = templates.newTransformer();
        transformer.transform(new DOMSource(node), result);
      } catch (TransformerConfigurationException e) {
        log.info("Failed to create transformer.", e);
        writeErrorTo(new SLException(2008), result, templates);
      } catch (TransformerException e) {
        log.error("Failed to transform result.", e);
        writeErrorTo(new SLException(2008), result, templates);
      }
    }

    if (ds != null) {
      try {
        log.trace("Marshalled result:\n" + new String(ds.getBufferedBytes(), "UTF-8"));
      } catch (UnsupportedEncodingException e) {
        log.trace(e.getMessage());
      }
    }
    
    if (dw != null) {
      log.trace("Marshalled result:\n" + dw.getBufferedString());
    }

  }
  
  protected void writeErrorTo(SLException slException, Result result, Templates templates) {
    writeErrorTo(slException, result, templates, Locale.getDefault());
  }
  
  protected void writeErrorTo(SLException slException, Result result, Templates templates, Locale locale) {
    
    TransformerHandler transformerHandler = null;
    if (templates != null) {
      try {
        transformerHandler = getTransformerHandler(templates, result);
      } catch (SLException e) {
        // write the exception thrown instead of the given one
        slException = e;
      }
    }

    ObjectFactory factory = new ObjectFactory();
    ErrorResponseType responseType = factory.createErrorResponseType();
    responseType.setErrorCode(slException.getErrorCode());
    responseType.setInfo(slException.getLocalizedMessage(locale));
    JAXBElement<ErrorResponseType> response = factory.createErrorResponse(responseType);
    
    Marshaller marshaller = getMarshaller();
    try {
      if (transformerHandler != null) {
        marshaller.marshal(response, transformerHandler);
      } else {
        marshaller.marshal(response, result);
      }
    } catch (JAXBException e) {
      log.fatal("Failed to marshall error result." , e);
      throw new SLRuntimeException("Failed to marshall error result.");
    }
    
  }

}
