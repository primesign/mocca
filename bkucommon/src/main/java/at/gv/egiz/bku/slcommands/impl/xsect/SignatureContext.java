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


package at.gv.egiz.bku.slcommands.impl.xsect;

import javax.xml.crypto.AlgorithmMethod;
import javax.xml.crypto.dsig.XMLSignatureFactory;

import org.w3c.dom.Document;

import at.gv.egiz.bku.utils.urldereferencer.URLDereferencer;

/**
 * An instance of this class carries context information for a XML-Signature
 * created by the security layer command <code>CreateXMLSignature</code>.
 * 
 * @author mcentner
 */
public class SignatureContext {

  /**
   * The document going to contain the XML signature.
   */
  private Document document;
  
  /**
   * The IdValueFactory used to create <code>xsd:ID</code>-attribute values.
   */
  private IdValueFactory idValueFactory;
  
  /**
   * The XMLSignatureFactory to create XML signature objects. 
   */
  private XMLSignatureFactory signatureFactory;
  
  /**
   * The URLDereferencer to dereference URLs with.
   */
  private URLDereferencer urlDereferencer;
  
  /**
   * The AlgorithmMethodFactory to create {@link AlgorithmMethod} objects.
   */
  private AlgorithmMethodFactory algorithmMethodFactory;

  /**
   * @return the document
   */
  public Document getDocument() {
    return document;
  }

  /**
   * @param document the document to set
   */
  public void setDocument(Document document) {
    this.document = document;
  }

  /**
   * @return the idValueFactory
   */
  public IdValueFactory getIdValueFactory() {
    return idValueFactory;
  }

  /**
   * @param idValueFactory the idValueFactory to set
   */
  public void setIdValueFactory(IdValueFactory idValueFactory) {
    this.idValueFactory = idValueFactory;
  }

  /**
   * @return the signatureFactory
   */
  public XMLSignatureFactory getSignatureFactory() {
    return signatureFactory;
  }

  /**
   * @param signatureFactory the signatureFactory to set
   */
  public void setSignatureFactory(XMLSignatureFactory signatureFactory) {
    this.signatureFactory = signatureFactory;
  }

  /**
   * @return the digestMethodFactory
   */
  public AlgorithmMethodFactory getAlgorithmMethodFactory() {
    return algorithmMethodFactory;
  }

  /**
   * @param digestMethodFactory the digestMethodFactory to set
   */
  public void setAlgorithmMethodFactory(AlgorithmMethodFactory digestMethodFactory) {
    this.algorithmMethodFactory = digestMethodFactory;
  }

  /**
   * @return the urlDereferencer
   */
  public URLDereferencer getUrlDereferencer() {
    return urlDereferencer;
  }

  /**
   * @param urlDereferencer the urlDereferencer to set
   */
  public void setUrlDereferencer(URLDereferencer urlDereferencer) {
    this.urlDereferencer = urlDereferencer;
  }

}
