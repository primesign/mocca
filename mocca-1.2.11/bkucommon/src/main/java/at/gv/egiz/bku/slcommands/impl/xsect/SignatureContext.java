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
package at.gv.egiz.bku.slcommands.impl.xsect;

import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.XMLSignatureFactory;

import org.w3c.dom.Document;

import at.gv.egiz.bku.utils.urldereferencer.URLDereferencerContext;

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
   * The URLDereferencerContext for dereferencing URLs.
   */
  private URLDereferencerContext dereferencerContext;
  
  /**
   * The DigestMethodFactory to create {@link DigestMethod} objects.
   */
  private AlgorithmMethodFactory digestMethodFactory;

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
   * @return the dereferencerContext
   */
  public URLDereferencerContext getDereferencerContext() {
    return dereferencerContext;
  }

  /**
   * @param dereferencerContext the dereferencerContext to set
   */
  public void setDereferencerContext(URLDereferencerContext dereferencerContext) {
    this.dereferencerContext = dereferencerContext;
  }

  /**
   * @return the digestMethodFactory
   */
  public AlgorithmMethodFactory getAlgorithmMethodFactory() {
    return digestMethodFactory;
  }

  /**
   * @param digestMethodFactory the digestMethodFactory to set
   */
  public void setAlgorithmMethodFactory(AlgorithmMethodFactory digestMethodFactory) {
    this.digestMethodFactory = digestMethodFactory;
  }

}
