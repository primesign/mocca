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

import java.util.ArrayList;
import java.util.Collections;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Templates;
import javax.xml.transform.dom.DOMResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import at.buergerkarte.namespaces.securitylayer._1_2_3.Base64XMLContentType;
import at.buergerkarte.namespaces.securitylayer._1_2_3.InfoboxReadResponseType;
import at.buergerkarte.namespaces.securitylayer._1_2_3.ObjectFactory;
import at.buergerkarte.namespaces.securitylayer._1_2_3.XMLContentType;
import at.gv.egiz.bku.slcommands.InfoboxReadResult;
import at.gv.egiz.bku.slcommands.SLCommand;
import at.gv.egiz.bku.slcommands.SLMarshallerFactory;
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
  protected final Logger log = LoggerFactory.getLogger(InfoboxReadResultFileImpl.class);

  /**
   * The XML document containing the infobox content.
   */
  protected Document xmlDocument;
  
  /**
   * Binary content of the infobox (may be <code>null</code>). 
   */
  protected byte[] binaryContent;

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

    Marshaller marshaller = SLMarshallerFactory.getInstance().createMarshaller(false);
    try {
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
  public Result getXmlResult(boolean preserveSpace) {
    
    xmlDocument = createResponseDocument(null, preserveSpace);
    
    NodeList nodeList = xmlDocument.getElementsByTagNameNS(SLCommand.NAMESPACE_URI, "XMLContent");
    return new DOMResult(nodeList.item(0));
    
  }

  /**
   * Creates a new <code>InfoboxReadResponse</code> document and appends
   * the given <code>node</code> as child node of the <code>XMLContent</code> element.
   * 
   * @param node the node to be appended as child node of the <code>XMLContnet</code> element
   * @param preserveSpace if <code>true</code> the value of the <code>XMLContent</code>'s <code>space</code> 
   * attribute is set to <code>preserve</code>.  
   */
  public void setResultXMLContent(Node node, boolean preserveSpace) {
    
    xmlDocument = createResponseDocument(null, preserveSpace);
    
    NodeList nodeList = xmlDocument.getElementsByTagNameNS(SLCommand.NAMESPACE_URI, "XMLContent");
    if (node.getOwnerDocument() != xmlDocument) {
      node = xmlDocument.importNode(node, true);
    }
    nodeList.item(0).appendChild(node);
    
  }
  
  /**
   * Creates a new result document for this <code>InfoboxReadResult</code>
   * and sets the given <code>resultBytes</code> as content.
   * 
   * @param resultBytes
   */
  public void setResultBytes(byte[] resultBytes) {
    this.binaryContent = resultBytes;
  }
  
  @Override
  public void writeTo(Result result, Templates templates, boolean fragment) {
    if (xmlDocument == null) {
      xmlDocument = createResponseDocument(binaryContent, false);
    }
    writeTo(xmlDocument, result, templates, fragment);
  }

  @Override
  public Object getContent() {
    if (xmlDocument != null) {
      NodeList nodes = xmlDocument.getElementsByTagNameNS(SLCommand.NAMESPACE_URI, "XMLContent");
      if (nodes.getLength() > 0) {
        NodeList children = nodes.item(0).getChildNodes();
        ArrayList<Node> content = new ArrayList<Node>();
        for (int i = 0; i < children.getLength(); i++) {
          content.add(children.item(i));
        }
        return Collections.unmodifiableList(content);
      } else {
        return null;
      }
    } else {
      return binaryContent;
    }
  }

}
