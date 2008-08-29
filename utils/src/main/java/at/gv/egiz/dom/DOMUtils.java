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
package at.gv.egiz.dom;

import iaik.utils.Base64OutputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Text;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;

public final class DOMUtils {

  /**
   * DOM Implementation.
   */
  private static String DOM_LS_3_0 = "LS 3.0";
  
  private static DOMImplementationLS domImplLS;
  
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
  
  public static Text createBase64Text(byte[] bytes, Document doc) throws IOException {
    
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    Base64OutputStream base64OutputStream = new Base64OutputStream(outputStream);
    base64OutputStream.write(bytes);
    base64OutputStream.flush();
    return doc.createTextNode(outputStream.toString("ASCII"));
    
  }
  
  public static Text createBase64Text(InputStream bytes, Document doc) throws IOException {
    
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    Base64OutputStream base64OutputStream = new Base64OutputStream(outputStream, new byte[] {0xa});

    byte[] b = new byte[2^8];
    for(int l; (l = bytes.read(b)) != -1;) {
      base64OutputStream.write(b, 0, l);
    }
    
    base64OutputStream.flush();
    return doc.createTextNode(outputStream.toString("ASCII"));
  }

}
