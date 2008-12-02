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

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Templates;
import javax.xml.transform.dom.DOMResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import at.buergerkarte.namespaces.securitylayer._1.Base64XMLContentType;
import at.buergerkarte.namespaces.securitylayer._1.InfoboxReadResponseType;
import at.buergerkarte.namespaces.securitylayer._1.ObjectFactory;
import at.buergerkarte.namespaces.securitylayer._1.XMLContentType;
import at.gv.egiz.bku.slcommands.InfoboxReadResult;
import at.gv.egiz.bku.slcommands.SLCommand;
import at.gv.egiz.bku.slcommands.SLCommandFactory;
import at.gv.egiz.bku.slexceptions.SLRuntimeException;

/**
 * This class implements the result of the security layer command <code>InfoboxReadRequest</code>.
 * 
 * @author mcentner
 */
public class InfoboxReadResultFileImpl extends SLResultImpl implements
    InfoboxReadResult {

  /**
   * Logging facility.
   */
  protected static Log log = LogFactory.getLog(InfoboxReadResultFileImpl.class);

  /**
   * The XML document containing the infobox content.
   */
  Document xmlDocument;

  /**
   * Creates the response document from the given <code>binaryContent</code>.
   * 
   * @param binaryContent the infobox content
   * @param preserveSpace the value of the <code>preserveSpace</code> parameter
   * 
   * @return the created response document
   */
  private Document createResponseDocument(byte[] binaryContent, boolean preserveSpace) {
    
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    Document doc;
    try {
      doc = dbf.newDocumentBuilder().newDocument();
    } catch (ParserConfigurationException e) {
      // it should always be possible to create a new Document
      log.error("Failed to create XML document.", e);
      throw new SLRuntimeException(e);
    }

    ObjectFactory factory = new ObjectFactory();
    
    Base64XMLContentType base64XMLContentType = factory.createBase64XMLContentType();
    if (binaryContent == null) {
      XMLContentType xmlContentType = factory.createXMLContentType();
      if (preserveSpace) {
        xmlContentType.setSpace("preserve");
      }
      base64XMLContentType.setXMLContent(xmlContentType);
    } else {
      base64XMLContentType.setBase64Content(binaryContent);
    }
    InfoboxReadResponseType infoboxReadResponseType = factory.createInfoboxReadResponseType();
    infoboxReadResponseType.setBinaryFileData(base64XMLContentType);
    
    JAXBElement<InfoboxReadResponseType> infoboxReadResponse = factory.createInfoboxReadResponse(infoboxReadResponseType);
    
    JAXBContext context = SLCommandFactory.getInstance().getJaxbContext();
    try {
      Marshaller marshaller = context.createMarshaller();
      marshaller.marshal(infoboxReadResponse, doc);
    } catch (JAXBException e) {
      log.error("Failed to marshal 'InfoboxReadResponse' document.", e);
      throw new SLRuntimeException(e);
    }

    return doc;
    
  }
  
  
  /**
   * @return an XMLResult for marshalling the infobox to
   */
  Result getXmlResult(boolean preserveSpace) {
    
    xmlDocument = createResponseDocument(null, preserveSpace);
    
    NodeList nodeList = xmlDocument.getElementsByTagNameNS(SLCommand.NAMESPACE_URI, "XMLContent");
    return new DOMResult(nodeList.item(0));
    
  }
  
  /**
   * Creates a new result document for this <code>InfoboxReadResult</code>
   * and sets the given <code>resultBytes</code> as content.
   * 
   * @param resultBytes
   */
  void setResultBytes(byte[] resultBytes) {
    
    xmlDocument = createResponseDocument(resultBytes, false);
    
  }
  
  @Override
  public void writeTo(Result result, Templates templates) {
    writeTo(xmlDocument, result, templates);
  }

}
