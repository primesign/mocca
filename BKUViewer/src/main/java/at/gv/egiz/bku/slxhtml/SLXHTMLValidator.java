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


package at.gv.egiz.bku.slxhtml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Locale;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.ValidatorHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import at.gv.egiz.bku.slxhtml.css.CSSValidatorSLXHTML;
import at.gv.egiz.bku.viewer.ValidationException;

public class SLXHTMLValidator implements at.gv.egiz.bku.viewer.Validator {

  /**
   * The schema file for the SLXHTML schema.
   */
  private static final String SLXHTML_SCHEMA_FILE = "at/gv/egiz/bku/slxhtml/slxhtml.xsd";
//    public static final String[] SLXHTML_SCHEMA_FILES = new String[]{
//      "at/gv/egiz/bku/slxhtml/slxhtml.xsd",
//      "at/gv/egiz/bku/slxhtml/slxhtml-model-1.xsd",
//      "at/gv/egiz/bku/slxhtml/slxhtml-modules-1.xsd",
//      "at/gv/egiz/bku/slxhtml/xhtml-attribs-1.xsd",
//      "at/gv/egiz/bku/slxhtml/xhtml-blkphras-1.xsd",
//      "at/gv/egiz/bku/slxhtml/xhtml-blkpres-1.xsd",
//      "at/gv/egiz/bku/slxhtml/xhtml-blkstruct-1.xsd",
//      "at/gv/egiz/bku/slxhtml/xhtml-datatypes-1.xsd",
//      "at/gv/egiz/bku/slxhtml/xhtml-framework-1.xsd",
//      "at/gv/egiz/bku/slxhtml/xhtml-image-1.xsd",
//      "at/gv/egiz/bku/slxhtml/xhtml-inlphras-1.xsd",
//      "at/gv/egiz/bku/slxhtml/xhtml-inlpres-1.xsd",
//      "at/gv/egiz/bku/slxhtml/xhtml-inlstruct-1.xsd",
//      "at/gv/egiz/bku/slxhtml/xhtml-list-1.xsd",
//      "at/gv/egiz/bku/slxhtml/xhtml-pres-1.xsd",
//      "at/gv/egiz/bku/slxhtml/xhtml-struct-1.xsd",
//      "at/gv/egiz/bku/slxhtml/xhtml-style-1.xsd",
//      "at/gv/egiz/bku/slxhtml/xhtml-table-1.xsd",
//      "at/gv/egiz/bku/slxhtml/xhtml-text-1.xsd",
//      "at/gv/egiz/bku/slxhtml/xml.xsd"
//    };

  /**
   * Logging facility.
   */
  private static Logger log = LoggerFactory.getLogger(SLXHTMLValidator.class);

  private static Schema slSchema;
  
  /**
   * Initialize the security layer schema.
   */
  private synchronized static void ensureSchema() {
      if (slSchema == null) {
          try {
              SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
              ClassLoader cl = SLXHTMLValidator.class.getClassLoader();
              URL schemaURL = cl.getResource(SLXHTML_SCHEMA_FILE);
              log.debug("Trying to create SLXHTML schema from URL '{}'.", schemaURL);
              long t0 = System.currentTimeMillis();
              slSchema = schemaFactory.newSchema(schemaURL);
              long t1 = System.currentTimeMillis();
              log.debug("SLXHTML schema successfully created in {}ms.", (t1 - t0));
          } catch (SAXException e) {
              log.error("Failed to load security layer XHTML schema.", e);
              throw new RuntimeException("Failed to load security layer XHTML schema.", e);
          }

      }
  }
  
  public SLXHTMLValidator() {
    ensureSchema();
  }
  
  public void validate(InputStream is, String charset)
      throws ValidationException {
    if (charset == null) {
      validate(is, (Charset) null);
    } else {
      try {
        validate(is, Charset.forName(charset));
      } catch (IllegalCharsetNameException e) {
        throw new ValidationException(e);
      } catch (UnsupportedCharsetException e) {
        throw new ValidationException(e);
      }
    }
  }
  
  public void validate(InputStream is, Charset charset) throws ValidationException {
    
    SAXParserFactory spf = SAXParserFactory.newInstance();
    spf.setNamespaceAware(true);
    spf.setSchema(slSchema);
    spf.setValidating(true);
    spf.setXIncludeAware(false);
    
    SAXParser parser;
    try {
      parser = spf.newSAXParser();
    } catch (ParserConfigurationException e) {
      log.error("Failed to create SLXHTML parser.", e);
      throw new RuntimeException("Failed to create SLXHTML parser.", e);
    } catch (SAXException e) {
      log.error("Failed to create SLXHTML parser.", e);
      throw new RuntimeException("Failed to create SLXHTML parser.", e);
    }
    
    InputSource source;
    if (charset != null) {
      source = new InputSource(new InputStreamReader(is, charset));
    } else {
      source = new InputSource(is);
    }
    

    ValidatorHandler validatorHandler = slSchema.newValidatorHandler();

    DefaultHandler defaultHandler = new ValidationHandler(validatorHandler);
    try {
      parser.parse(source, defaultHandler);
    } catch (SAXException e) {
      if (e.getException() instanceof ValidationException) {
        throw (ValidationException) e.getException();
      } else {
        throw new ValidationException(e);
      }
    } catch (IOException e) {
      throw new ValidationException(e);
    }
       
  }
  
  private void validateCss(InputStream is) throws ValidationException {
    CSSValidatorSLXHTML cssValidator = new CSSValidatorSLXHTML();
    // TODO: use the right locale
    cssValidator.validate(is, Locale.getDefault(), "SLXHTML", 0);
  }
  
  private class ValidationHandler extends DefaultHandler implements ContentHandler {
    
    private ValidatorHandler validatorHandler;
    
    private boolean insideStyle = false;
    
    private StringBuffer style = new StringBuffer();
    
    private ValidationHandler(ValidatorHandler contentHandler) {
      this.validatorHandler = contentHandler;
    }
    
    @Override
    public void endDocument() throws SAXException {
      validatorHandler.endDocument();
    }

    @Override
    public void endPrefixMapping(String prefix) throws SAXException {
      validatorHandler.endPrefixMapping(prefix);
    }

    @Override
    public void ignorableWhitespace(char[] ch, int start, int length)
        throws SAXException {
      validatorHandler.ignorableWhitespace(ch, start, length);
    }

    @Override
    public void processingInstruction(String target, String data)
        throws SAXException {
      validatorHandler.processingInstruction(target, data);
    }

    @Override
    public void setDocumentLocator(Locator locator) {
      validatorHandler.setDocumentLocator(locator);
    }

    @Override
    public void skippedEntity(String name) throws SAXException {
      validatorHandler.skippedEntity(name);
    }

    @Override
    public void startDocument() throws SAXException {
      validatorHandler.startDocument();
    }

    @Override
    public void startPrefixMapping(String prefix, String uri)
        throws SAXException {
      validatorHandler.startPrefixMapping(prefix, uri);
    }

    @Override
    public void startElement(String uri, String localName, String name,
        Attributes attributes) throws SAXException {
      validatorHandler.startElement(uri, localName, name, attributes);
      
      System.out.println(uri + ":" + localName);
      
      if ("http://www.w3.org/1999/xhtml".equals(uri) &&
          "style".equals(localName)) {
        insideStyle = true;
      }
    }

    @Override
    public void characters(char[] ch, int start, int length)
        throws SAXException {
      validatorHandler.characters(ch, start, length);
      
      if (insideStyle) {
        style.append(ch, start, length);
      }
      
    }

    @Override
    public void endElement(String uri, String localName, String name)
        throws SAXException {
      validatorHandler.endElement(uri, localName, name);
      
      if (insideStyle) {
        insideStyle = false;
        try {
          validateCss(new ByteArrayInputStream(style.toString().getBytes(Charset.forName("UTF-8"))));
        } catch (ValidationException e) {
          throw new SAXException(e);
        }
      }
    }

  }
  
  
}
