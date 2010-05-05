/*
* Copyright 2009 Federal Chancellery Austria and
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

package at.gv.egiz.mocca.id;

import java.io.IOException;
import java.net.URL;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import oasis.names.tc.saml._1_0.assertion.AssertionType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class IdLinkFactory {
  
  protected static Logger log = LoggerFactory.getLogger(IdLinkFactory.class);
  
  public static final String[] SCHEMA_FILES = new String[] {
    "at/gv/egiz/mocca/id/idlschema/xmldsig-more.xsd",
    "at/gv/egiz/mocca/id/idlschema/xmldsig-core-schema.xsd",
    "at/gv/egiz/mocca/id/idlschema/PersonData.xsd",
    "at/gv/egiz/mocca/id/idlschema/oasis-sstc-saml-schema-assertion-1.0.xsd"};

  private static class InstanceHolder {
    private static final IdLinkFactory INSTANCE = new IdLinkFactory();
  }

  public static IdLinkFactory getInstance() {
    return InstanceHolder.INSTANCE;
  }
  
  static {
//    InitDOMStructure.init();
  }

  private final Schema idlSchema;

  private final JAXBContext jaxbContext;

  
  private IdLinkFactory() {
    
    try {
      SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      Source[] sources = new Source[SCHEMA_FILES.length];
      for (int i = 0; i < SCHEMA_FILES.length; i++) {
          String schemaFile = SCHEMA_FILES[i];
          URL schemaURL = cl.getResource(schemaFile);
          if (schemaURL == null) {
              throw new RuntimeException("Failed to load schema file " + schemaFile + ".");
          }
          log.debug("Schema location: " + schemaURL);
          sources[i] = new StreamSource(schemaURL.openStream());
      }
      idlSchema = schemaFactory.newSchema(sources);
    } catch (IOException e) {
      log.error("Failed to load identity link schema.", e);
      throw new RuntimeException(e);
    } catch (SAXException e) {
      log.error("Failed to load identity link schema.", e);
      throw new RuntimeException(e);
    }

    StringBuffer packageNames = new StringBuffer();
    packageNames.append(at.gv.e_government.reference.namespace.persondata._20020228_.ObjectFactory.class.getPackage().getName());
    packageNames.append(":");
    packageNames.append(oasis.names.tc.saml._1_0.assertion.ObjectFactory.class.getPackage().getName());

    try {
      jaxbContext = JAXBContext.newInstance(packageNames.toString());
    } catch (JAXBException e) {
      // we should not get an JAXBException initializing the JAXBContext
      throw new RuntimeException(e);
    }
    
  }

  public IdLink unmarshallIdLink(InputSource source) throws IdLinkException,
      ParserConfigurationException, SAXException, IOException, JAXBException {
    
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    dbf.setNamespaceAware(true);
    dbf.setSchema(idlSchema);
    dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
    
    // http://www.w3.org/TR/xmldsig-bestpractices/#be-aware-schema-normalization
    try {
      dbf.setAttribute("http://apache.org/xml/features/validation/schema/normalized-value", Boolean.FALSE);
    } catch (IllegalArgumentException e) {
      log.warn("Failed to disable schema normalization " +
      		"(see http://www.w3.org/TR/xmldsig-bestpractices/#be-aware-schema-normalization)", e);
    }
    
    DocumentBuilder documentBuilder = dbf.newDocumentBuilder();
    Document doc = documentBuilder.parse(source);
    
    return unmarshallIdLink(doc.getDocumentElement());
    
  }
  
  public IdLink unmarshallIdLink(Element element) throws IdLinkException, JAXBException {

    Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
    unmarshaller.setSchema(idlSchema);
    
    Object object = unmarshaller.unmarshal(element);
    
    IdLink idLink;
    if (object instanceof JAXBElement<?>
        && ((JAXBElement<?>) object).getDeclaredType() == AssertionType.class) {
       idLink = new IdLink(element, (AssertionType) ((JAXBElement<?>) object).getValue());
    } else {
      throw new IllegalArgumentException("Parameter node is not a "
          + new QName("urn:oasis:names:tc:SAML:1.0:assertion", "Assertion"));
    }

    return idLink;
    
  }
    
}
