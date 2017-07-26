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


package at.gv.egiz.dom;

import iaik.utils.Base64OutputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.xml.sax.SAXException;

public final class DOMUtils {

  /**
   * DOM Implementation.
   */
  private static String DOM_LS_3_0 = "LS 3.0";

  private static DOMImplementationLS domImplLS;

  private final static Logger log = LoggerFactory.getLogger(DOMUtils.class);

  private DOMUtils() {
  }

  private static synchronized void ensureDOMImplementation() {
    
    if (domImplLS == null) {

      DOMImplementationRegistry registry;
      try {
        registry = DOMImplementationRegistry.newInstance();
      } catch (Exception e) {
        throw new RuntimeException("Failed to get DOMImplementationRegistry.");
      }

      domImplLS = (DOMImplementationLS) registry.getDOMImplementation(DOM_LS_3_0);
      if (domImplLS == null) {
        throw new RuntimeException("Failed to get DOMImplementation " + DOM_LS_3_0);
      }
      
    }
    
  }

  public static DOMImplementationLS getDOMImplementationLS() {
    
    if (domImplLS == null) {
      ensureDOMImplementation();
    }
    
    return domImplLS;
  }

  public static Document createDocument() {
    
    // This does not work with the Xerces-J version (2.6.2) included in Java 6
    //document = ((DOMImplementation) domImplLS).createDocument(null, null, null);
    // Therefore we have to employ the good old DocumentBuilderFactory
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    DocumentBuilder db;
    try {
      db = dbf.newDocumentBuilder();
    } catch (ParserConfigurationException e) {
      throw new RuntimeException(e);
    }
    return db.newDocument();
       
  }

  public static String documentToString(Document doc) {
    StringWriter sw = new StringWriter();
    try {
      TransformerFactory tf = TransformerFactory.newInstance();
      Transformer transformer = tf.newTransformer();
      transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
      transformer.setOutputProperty(OutputKeys.METHOD, "xml");
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

      transformer.transform(new DOMSource(doc), new StreamResult(sw));
    } catch (TransformerException ex) {
      log.error("documentToString Transformer Exception");
    }
    return sw.toString();
  }

  public static String nodeToString(Node node) {
    StringWriter sw = new StringWriter();
    try {
      Transformer t = TransformerFactory.newInstance().newTransformer();
      t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
      t.setOutputProperty(OutputKeys.INDENT, "yes");
      t.transform(new DOMSource(node), new StreamResult(sw));
    } catch (TransformerException te) {
      log.error("nodeToString Transformer Exception");
    }
    return sw.toString();
  }

  public static Text createBase64Text(byte[] bytes, Document doc) throws IOException {
    
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    Base64OutputStream base64OutputStream = new Base64OutputStream(outputStream);
    base64OutputStream.write(bytes);
    base64OutputStream.close();
    return doc.createTextNode(outputStream.toString("ASCII"));
    
  }

  public static Text createBase64Text(InputStream bytes, Document doc) throws IOException {
    
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    Base64OutputStream base64OutputStream = new Base64OutputStream(outputStream, new byte[] {0xa});

    byte[] b = new byte[2^8];
    for(int l; (l = bytes.read(b)) != -1;) {
      base64OutputStream.write(b, 0, l);
    }
    
    base64OutputStream.close();
    return doc.createTextNode(outputStream.toString("ASCII"));
  }
  
  /**
   * Set XML parser features to {@link DocumentBuilderFactory} to prevent XXE, XEE and SSRF attacks
   * <br>
   * <br>
   * These features are set by this method:
   * <ul>
   * 	<li>http://xml.org/sax/features/external-general-entities --> false</li>
   * 	<li>http://xml.org/sax/features/external-parameter-entities --> false</li>
   * 	<li>http://apache.org/xml/features/nonvalidating/load-external-dtd --> false</li>
   * 	<li>http://apache.org/xml/features/disallow-doctype-decl --> true</li>
   * </ul>
   *  
   * 
   * @param dbf {@link DocumentBuilderFactory} on which the features should be registered
   */
  public static void setXMLParserFlagsAgainstXXEAndSSRFAttacks(DocumentBuilderFactory dbf) {
	  try {
			dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
			dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
			dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
			dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
						
		} catch (ParserConfigurationException e) {
			log.error("Can NOT set Xerces parser security features. -> XML parsing is possible insecure!!!! ", e);
			
		}
	  
  }
  
  /**
   * Parse an {@link InputStream} that contains a XML document into a {@link Document}
   * This method set all features to prevent XXE, XEE and SSRF attacks
   * 
   * These features are set by this method:
   * <ul>
   * 	<li>http://xml.org/sax/features/external-general-entities --> false</li>
   * 	<li>http://xml.org/sax/features/external-parameter-entities --> false</li>
   * 	<li>http://apache.org/xml/features/nonvalidating/load-external-dtd --> false</li>
   * 	<li>http://apache.org/xml/features/disallow-doctype-decl --> true</li>
   * </ul>
   * 
   * @param is {@link InputStream} that contains a serialized XML document 
   * @return Deserialized {@link Document} from input XML 
   * @throws XMLStreamException XML parser has an error 
   * @throws IOException 
   */
	public static Document validateXMLAgainstXXEAndSSRFAttacks(InputStream is) throws XMLStreamException, IOException {		
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();		
		setXMLParserFlagsAgainstXXEAndSSRFAttacks(dbf);
		    	    	        
		try {        	
			//validate input stream        	
			return dbf.newDocumentBuilder().parse(is);
            		    		    		
		} catch (SAXException e) {
			log.error("XML data validation FAILED with msg: " + e.getMessage(), e);
			throw new XMLStreamException("XML data validation FAILED with msg: " + e.getMessage(), e);

		} catch (ParserConfigurationException e) {
			log.error("XML data validation FAILED with msg: " + e.getMessage(), e);
			throw new XMLStreamException("XML data validation FAILED with msg: " + e.getMessage(), e);
    		
		} catch (IOException e) {
			log.error("XML data validation FAILED with msg: " + e.getMessage(), e);
			throw new XMLStreamException("XML data validation FAILED with msg: " + e.getMessage(), e);
    		
		}
		
	}

}
