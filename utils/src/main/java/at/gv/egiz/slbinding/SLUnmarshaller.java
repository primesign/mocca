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



package at.gv.egiz.slbinding;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import at.gv.egiz.bku.utils.ClasspathURLStreamHandler;
import at.gv.egiz.validation.ReportingValidationEventHandler;

public class SLUnmarshaller {

  /**
   * Logging facility.
   */
  private final Logger log = LoggerFactory.getLogger(SLUnmarshaller.class);
  
  private static class DefaultSchema {
    
    /**
     * Schema files required for Security Layer command validation.
     */
    public static final String[] SCHEMA_FILES = new String[] {
        "classpath:at/gv/egiz/bku/slschema/xml.xsd",
        "classpath:at/gv/egiz/bku/slschema/xmldsig-core-schema.xsd",
        "classpath:at/gv/egiz/bku/slschema/Core-1.2.xsd",
        "classpath:at/gv/egiz/bku/slschema/Core.20020225.xsd",
        "classpath:at/gv/egiz/bku/slschema/Core.20020831.xsd" };
    
    private static final Schema SCHEMA;
    
    static {
      try {
        SCHEMA = createSchema(Arrays.asList(SCHEMA_FILES));
      } catch (IOException e) {
        Logger log = LoggerFactory.getLogger(SLUnmarshaller.class);
        log.error("Failed to load security layer schema.", e);
        throw new RuntimeException(e);
      } catch (SAXException e) {
        Logger log = LoggerFactory.getLogger(SLUnmarshaller.class);
        log.error("Failed to load security layer schema.", e);
        throw new RuntimeException(e);
      }
      
    }
  }
  
  public static Collection<String> getDefaultSchemaUrls() {
    return Collections.unmodifiableList(Arrays.asList(DefaultSchema.SCHEMA_FILES));
  }
  
  private static Schema createSchema(Collection<String> schemaUrls) throws SAXException, IOException {
    Logger log = LoggerFactory.getLogger(SLUnmarshaller.class);
    Source[] sources = new Source[schemaUrls.size()];
    Iterator<String> urls = schemaUrls.iterator();
    StringBuilder sb = null;
    if (log.isDebugEnabled()) {
      sb = new StringBuilder();
      sb.append("Created schema using URLs: ");
    }
    for (int i = 0; i < sources.length && urls.hasNext(); i++) {
      String url = urls.next();
      if (url != null && url.startsWith("classpath:")) {
        URL schemaUrl = new URL(null, url, new ClasspathURLStreamHandler());
        sources[i] = new StreamSource(schemaUrl.openStream());
      } else {
        sources[i] = new StreamSource(url);
      }
      if (sb != null) {
        sb.append(url);
        if (urls.hasNext()) {
          sb.append(", ");
        }
      }
    }
    SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    Schema schema = schemaFactory.newSchema(sources);
    if (sb != null) {
      log.debug(sb.toString());
    }
    return schema;
  }
  
  private static class DefaultContext {
    
    private static final String[] packageNames = {
      at.buergerkarte.namespaces.securitylayer._1_2_3.ObjectFactory.class.getPackage().getName(),
      org.w3._2000._09.xmldsig_.ObjectFactory.class.getPackage().getName(),
      at.buergerkarte.namespaces.cardchannel.ObjectFactory.class.getPackage().getName(),
      at.buergerkarte.namespaces.securitylayer._20020225_.ObjectFactory.class.getPackage().getName(),
      at.buergerkarte.namespaces.securitylayer._20020831_.ObjectFactory.class.getPackage().getName()
    };

    private static final JAXBContext CONTEXT;
    
    static {
      try {
        CONTEXT = createJAXBContext(Arrays.asList(packageNames));
      } catch (JAXBException e) {
        Logger log = LoggerFactory.getLogger(SLUnmarshaller.class);
        log.error("Failed to setup JAXBContext security layer request/response.", e);
        throw new RuntimeException(e);
      }
    }
    
  }
  
  public static Collection<String> getDefaultJAXBContextPackageNames() {
    return Collections.unmodifiableList(Arrays.asList(DefaultContext.packageNames));
  }
  
  private static JAXBContext createJAXBContext(Collection<String> packageNames) throws JAXBException {
    StringBuilder contextPath = new StringBuilder();
    for (String pkg : packageNames) {
      if (contextPath.length() > 0) {
        contextPath.append(':');
      }
      contextPath.append(pkg);
    }
    return JAXBContext.newInstance(contextPath.toString());
  }
  
  /**
   * Schema for Security Layer command validation.
   */
  protected Schema slSchema = DefaultSchema.SCHEMA;

  /**
   * The JAXBContext.
   */
  protected JAXBContext jaxbContext = DefaultContext.CONTEXT;

  /**
   * Returns the schema used for validation.
   * 
   * @return the slSchema
   */
  public Schema getSlSchema() {
    return slSchema;
  }

  /**
   * Sets the schema for validation.
   * 
   * @param slSchema the slSchema to set
   */
  public void setSlSchema(Schema slSchema) {
    this.slSchema = slSchema;
  }

  /**
   * Sets the schema created from the given {@code schemaUrls}.
   * 
   * @param schemaUrls a collection of URLs of schema files (supports {@code classpath:} URLs)
   * @throws SAXException if schema creation fails
   * @throws IOException if an error occurs upon dereferencing the given {@code schemaUrls}
   */
  public void setSchemaUrls(Collection<String> schemaUrls) throws SAXException, IOException {
    slSchema = createSchema(schemaUrls);
  }
  
  /**
   * @return the jaxbContext
   */
  public JAXBContext getJaxbContext() {
    return jaxbContext;
  }

  /**
   * @param jaxbContext the jaxbContext to set
   */
  public void setJaxbContext(JAXBContext jaxbContext) {
      this.jaxbContext = jaxbContext;
  }
  
  /**
   * Sets the JAXBContext for unmarshalling using the given {@code packageNames}.
   * 
   * @param packageNames a collection of java package names
   * @throws JAXBException if creating the JAXBContext with the given {@code packageNames} fails
   */
  public void setJaxbContextPackageNames(Collection<String> packageNames) throws JAXBException {
    this.jaxbContext = createJAXBContext(packageNames);
  }

  /**
   * @param source a StreamSource wrapping a Reader (!) for the marshalled Object
   * @return the unmarshalled Object
   * @throws XMLStreamException
   * @throws JAXBException
   */
public Object unmarshal(StreamSource source) throws XMLStreamException, JAXBException {
    
    ReportingValidationEventHandler validationEventHandler = new ReportingValidationEventHandler();
    XMLInputFactory inputFactory = XMLInputFactory.newInstance();
    XMLEventReader eventReader = inputFactory.createXMLEventReader(source.getReader());
    RedirectEventFilter redirectEventFilter = new RedirectEventFilter();
    XMLEventReader filteredReader = inputFactory.createFilteredReader(eventReader, redirectEventFilter);

    Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
    unmarshaller.setEventHandler(validationEventHandler);

    unmarshaller.setListener(new RedirectUnmarshallerListener(redirectEventFilter));
    unmarshaller.setSchema(slSchema);

    Object object;
    try {
      log.trace("Before unmarshal().");
      object = unmarshaller.unmarshal(filteredReader);
      log.trace("After unmarshal().");
    } catch (UnmarshalException e) {
      if (log.isDebugEnabled()) {
          log.debug("Failed to unmarshal security layer message.", e);
      } else {
          log.info("Failed to unmarshal security layer message." + (e.getMessage() != null ? " " + e.getMessage() : ""));
      }
      
      if (validationEventHandler.getErrorEvent() != null) {
          ValidationEvent errorEvent = validationEventHandler.getErrorEvent();
          if (e.getLinkedException() == null) {
            e.setLinkedException(errorEvent.getLinkedException());
          }
      }
      throw e;
    }

    return object;

  }

}
