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

import java.util.Iterator;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import at.buergerkarte.namespaces.securitylayer._1_2_3.SignatureInfoCreationType;
import at.gv.egiz.bku.slexceptions.SLCommandException;
import at.gv.egiz.slbinding.impl.SignatureLocationType;

/**
 * This class implements the <code>SignatureLocation</code> of an XML-Signature
 * to be created by the security layer command <code>CreateXMLSignature</code>.
 * 
 * @author mcentner
 */
public class SignatureLocation {

  /**
   * Logging facility.
   */
  private final Logger log = LoggerFactory.getLogger(SignatureLocation.class);
  
  /**
   * The SignatureContext for the XML signature
   */
  private SignatureContext ctx;

  /**
   * The parent node for the XML signature.
   */
  private Node parent;
  
  /**
   * The next sibling node for the XML signature.
   */
  private Node nextSibling;

  /**
   * Creates a new SignatureLocation with the given <code>signatureContext</code>
   * 
   * @param signatureContext the context for the XML signature creation
   */
  public SignatureLocation(SignatureContext signatureContext) {
    this.ctx = signatureContext;
  }

  /**
   * @return the parent node for the XML signature
   */
  public Node getParent() {
    return parent;
  }

  /**
   * @param parent the parent for the XML signature
   */
  public void setParent(Node parent) {
    this.parent = parent;
  }

  /**
   * @return the next sibling node for the XML signature
   */
  public Node getNextSibling() {
    return nextSibling;
  }

  /**
   * @param nextSibling the next sibling node for the XML signature
   */
  public void setNextSibling(Node nextSibling) {
    this.nextSibling = nextSibling;
  }

  /**
   * Configures this SignatureLocation with the information provided by the
   * given <code>SignatureInfo</code> element.
   * 
   * @param signatureInfo
   *          the <code>SignatureInfo</code> element
   * 
   * @throws SLCommandException
   *           if configuring this SignatureLocation with given
   *           <code>signatureInfo</code>fails
   */
  public void setSignatureInfo(SignatureInfoCreationType signatureInfo)
      throws SLCommandException {

    // evaluate signature location XPath ...
    SignatureLocationType signatureLocation = (SignatureLocationType) signatureInfo
        .getSignatureLocation();

    NamespaceContext namespaceContext = new MOAIDWorkaroundNamespaceContext(
        signatureLocation.getNamespaceContext());

    parent = evaluateSignatureLocation(signatureInfo.getSignatureLocation()
        .getValue(), namespaceContext, ctx.getDocument().getDocumentElement());

    // ... and index
    nextSibling = findNextSibling(parent, signatureInfo.getSignatureLocation()
        .getIndex().intValue());

  }
  
  /**
   * Evaluates the given <code>xpath</code> with the document element as context node
   * and returns the resulting node.
   * 
   * @param xpath the XPath expression
   * @param nsContext the namespace context of the XPath expression
   * @param contextNode the context node for the XPath evaluation
   * 
   * @return the result of evaluating the XPath expression
   * 
   * @throws SLCommandException
   */
  private Node evaluateSignatureLocation(String xpath, NamespaceContext nsContext, Node contextNode) throws SLCommandException {

    Node node = null;
    try {
      XPathFactory xpathFactory = XPathFactory.newInstance();
      XPath xPath = xpathFactory.newXPath();
      xPath.setNamespaceContext(nsContext);
      XPathExpression xpathExpr = xPath.compile(xpath);
      node = (Node) xpathExpr.evaluate(contextNode, XPathConstants.NODE);
    } catch (XPathExpressionException e) {
      log.info("Failed to evaluate SignatureLocation XPath expression '{}' on context node.", xpath, e);
      throw new SLCommandException(4102);
    }

    if (node == null) {
      log.info("Failed to evaluate SignatureLocation XPath expression '{}'. Result is empty.", xpath);
      throw new SLCommandException(4102);
    }
    
    return node;
    
  }

  /**
   * Finds the next sibling node of the <code>parent</code>'s <code>n</code>-th child node
   * or <code>null</code> if there is no next sibling.
   * 
   * @param parent the parent node
   * @param n the index of the child node
   * 
   * @return the next sibling node of the node specified by <code>parent</code> and index <code>n</code>,
   * or <code>null</code> if there is no next sibling node.
   * 
   * @throws SLCommandException if the <code>n</code>-th child of <code>parent</code> does not exist
   */
  private Node findNextSibling(Node parent, int n) throws SLCommandException {
    return parent.getChildNodes().item(n);
  }

  /**
   * Workaround for a missing namespace prefix declaration in MOA-ID.
   * 
   * @author mcentner
   */
  private class MOAIDWorkaroundNamespaceContext implements NamespaceContext {

    private NamespaceContext namespaceContext;
    
    public MOAIDWorkaroundNamespaceContext(NamespaceContext namespaceContext) {
      super();
      this.namespaceContext = namespaceContext;
    }

    @Override
    public String getNamespaceURI(String prefix) {
      
      String namespaceURI = namespaceContext.getNamespaceURI(prefix);
      
      if ((namespaceURI == null || XMLConstants.NULL_NS_URI.equals(namespaceURI)) && "saml".equals(prefix)) {
        namespaceURI = "urn:oasis:names:tc:SAML:1.0:assertion";
        log.debug("Namespace prefix '{}' resolved to '{}' (MOA-ID Workaround).", prefix, namespaceURI);
      } else {
        log.trace("Namespace prefix '{}' resolved to '{}'.", prefix, namespaceURI);
      }
      
      return namespaceURI;
    }

    @Override
    public String getPrefix(String namespaceURI) {
      return namespaceContext.getPrefix(namespaceURI);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Iterator<String> getPrefixes(String namespaceURI) {
      return namespaceContext.getPrefixes(namespaceURI);
    }
    
  }
  
}
