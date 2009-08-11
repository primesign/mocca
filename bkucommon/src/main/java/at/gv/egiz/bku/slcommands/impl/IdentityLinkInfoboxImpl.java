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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import at.buergerkarte.namespaces.personenbindung._20020506_.CompressedIdentityLinkType;
import at.buergerkarte.namespaces.securitylayer._1.AnyChildrenType;
import at.buergerkarte.namespaces.securitylayer._1.InfoboxReadRequestType;
import at.gv.egiz.bku.slcommands.InfoboxReadResult;
import at.gv.egiz.bku.slcommands.SLCommand;
import at.gv.egiz.bku.slcommands.SLCommandContext;
import at.gv.egiz.bku.slexceptions.SLCommandException;
import at.gv.egiz.bku.slexceptions.SLExceptionMessages;
import at.gv.egiz.bku.slexceptions.SLRuntimeException;
import at.gv.egiz.idlink.CompressedIdentityLinkFactory;
import at.gv.egiz.idlink.IdentityLinkTransformer;
import at.gv.egiz.idlink.ans1.IdentityLink;
import at.gv.egiz.stal.InfoboxReadRequest;
import at.gv.egiz.stal.STALRequest;

/**
 * An implementation of the {@link Infobox} <em>IdentityLink</em> as
 * specified in Security Layer 1.2
 * 
 * @author mcentner
 */
public class IdentityLinkInfoboxImpl extends AbstractBinaryFileInfobox {

  /**
   * Logging facility.
   */
  private static Log log = LogFactory.getLog(IdentityLinkInfoboxImpl.class);

  /**
   * The box specific parameter <code>IdentityLinkDomainIdentifier</code>.
   */
  public static final String BOX_SPECIFIC_PARAMETER_IDENTITY_LINK_DOMAIN_IDENTIFIER = "IdentityLinkDomainIdentifier";
  
  /**
   * The value of the box specific parameter <code>IdentityLinkDomainIdentifier</code>.
   */
  private String domainIdentifier;
  
  @Override
  public String getIdentifier() {
    return "IdentityLink";
  }

  /**
   * @return the value of the box specific parameter <code>IdentityLinkDomainIdentifier</code>
   */
  public String getDomainIdentifier() {
    return domainIdentifier;
  }

  @Override
  public InfoboxReadResult read(InfoboxReadRequestType req, SLCommandContext cmdCtx) throws SLCommandException {
    
    AnyChildrenType boxSpecificParameters = req.getBoxSpecificParameters();

    if (boxSpecificParameters != null) {
      // check BoxSpecificParameters
      List<Object> parameter = boxSpecificParameters.getAny();
      JAXBElement<?> element;
      if (parameter != null 
          && parameter.size() == 1 
          && parameter.get(0) instanceof JAXBElement<?>
          && SLCommand.NAMESPACE_URI.equals((element = (JAXBElement<?>) parameter.get(0)).getName().getNamespaceURI())
          && BOX_SPECIFIC_PARAMETER_IDENTITY_LINK_DOMAIN_IDENTIFIER.equals(element.getName().getLocalPart())
          && element.getValue() instanceof String) {
        domainIdentifier = (String) element.getValue();
        log.debug("Got sl:IdentityLinkDomainIdentifier: " + domainIdentifier);
      } else {
        log.info("Got invalid BoxSpecificParameters.");
        throw new SLCommandException(4010);
      }
    }
    
    setIsXMLEntity(req);
    
    STALHelper stalHelper = new STALHelper(cmdCtx.getSTAL());
    
    List<STALRequest> stalRequests = new ArrayList<STALRequest>();

    InfoboxReadRequest infoboxReadRequest;
    // get raw identity link
    infoboxReadRequest = new InfoboxReadRequest();
    infoboxReadRequest.setInfoboxIdentifier(getIdentifier());
    infoboxReadRequest.setDomainIdentifier(domainIdentifier);
    stalRequests.add(infoboxReadRequest);
    
    // get certificates
    infoboxReadRequest = new InfoboxReadRequest();
    infoboxReadRequest.setInfoboxIdentifier("SecureSignatureKeypair");
    stalRequests.add(infoboxReadRequest);
    infoboxReadRequest = new InfoboxReadRequest();
    infoboxReadRequest.setInfoboxIdentifier("CertifiedKeypair");
    stalRequests.add(infoboxReadRequest);

    stalHelper.transmitSTALRequest(stalRequests);
    log.trace("Got STAL response");

    IdentityLink identityLink = stalHelper.getIdentityLinkFromResponses();
    List<X509Certificate> certificates = stalHelper.getCertificatesFromResponses();
    
    
    CompressedIdentityLinkFactory idLinkFactory = CompressedIdentityLinkFactory.getInstance();
    JAXBElement<CompressedIdentityLinkType> compressedIdentityLink = idLinkFactory
        .createCompressedIdentityLink(identityLink, certificates, getDomainIdentifier());

    IdentityLinkTransformer identityLinkTransformer = IdentityLinkTransformer.getInstance();
    String issuerTemplate = identityLink.getIssuerTemplate();
    
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    DocumentBuilder db;
    try {
      db = dbf.newDocumentBuilder();
    } catch (ParserConfigurationException e) {
      log.error("Failed to create XML document.", e);
      throw new SLRuntimeException(e);
    }
    
    Document document = db.newDocument();
    try {
      idLinkFactory.marshallCompressedIdentityLink(compressedIdentityLink, document, null, true);
    } catch (JAXBException e) {
      log.info("Failed to marshall CompressedIdentityLink.", e);
      throw new SLCommandException(4000,
          SLExceptionMessages.EC4000_UNCLASSIFIED_INFOBOX_INVALID,
          new Object[] { getIdentifier() });
    }
    
    InfoboxReadResultFileImpl result = new InfoboxReadResultFileImpl();
    ByteArrayOutputStream resultBytes = null;
    Result xmlResult = (isXMLEntity() || getDomainIdentifier() != null) 
          ? result.getXmlResult(true) 
          : new StreamResult((resultBytes = new ByteArrayOutputStream()));
    try {
      log.trace("Trying to transform identitylink");
      identityLinkTransformer.transformIdLink(issuerTemplate, new DOMSource(document), xmlResult);
    } catch (MalformedURLException e) {
      log.warn("Malformed issuer template URL '" + issuerTemplate + "'.");
      throw new SLCommandException(4000,
          SLExceptionMessages.EC4000_UNCLASSIFIED_IDLINK_TRANSFORMATION_FAILED,
          new Object[] { issuerTemplate });
    } catch (IOException e) {
      log.warn("Failed to dereferene issuer template URL '" + issuerTemplate + "'." ,e);
      throw new SLCommandException(4000,
          SLExceptionMessages.EC4000_UNCLASSIFIED_IDLINK_TRANSFORMATION_FAILED,
          new Object[] { issuerTemplate });
    } catch (TransformerConfigurationException e) {
      log.warn("Failed to create transformation template from issuer template URL '" + issuerTemplate + "'", e);
      throw new SLCommandException(4000,
          SLExceptionMessages.EC4000_UNCLASSIFIED_IDLINK_TRANSFORMATION_FAILED,
          new Object[] { issuerTemplate });
    } catch (TransformerException e) {
      log.info("Faild to transform CompressedIdentityLink.", e);
      throw new SLCommandException(4000,
          SLExceptionMessages.EC4000_UNCLASSIFIED_IDLINK_TRANSFORMATION_FAILED,
          new Object[] { issuerTemplate });
    }
    
    // TODO: Report BUG in IssuerTemplates
    // Some IssuerTemplate stylesheets do not consider the pr:Type-Element of the CompressedIdentityLink ...
    if (getDomainIdentifier() != null) {
      if (xmlResult instanceof DOMResult) {
        Node node = ((DOMResult) xmlResult).getNode();
        Node nextSibling = ((DOMResult) xmlResult).getNextSibling();
        Node idLinkNode;
        if (nextSibling != null) {
          idLinkNode = nextSibling.getPreviousSibling();
        } else if (node != null) {
          idLinkNode = node.getFirstChild();
        } else {
          log
              .error("An IdentityLinkDomainIdentifier of '"
                  + getDomainIdentifier()
                  + "' has been given. However, it cannot be set, as the transformation result does not contain a node.");
          throw new SLCommandException(4000,
              SLExceptionMessages.EC4000_UNCLASSIFIED_IDLINK_TRANSFORMATION_FAILED,
              new Object[] { issuerTemplate });
        }
        IdentityLinkTransformer.setDomainIdentifier(idLinkNode, getDomainIdentifier());
      } else {
        log
            .error("An IdentityLinkDomainIdentifier of '"
                + getDomainIdentifier()
                + "' has been given. However, it cannot be set, as the transformation result is not of type DOM.");
        throw new SLCommandException(4000,
            SLExceptionMessages.EC4000_UNCLASSIFIED_IDLINK_TRANSFORMATION_FAILED,
            new Object[] { issuerTemplate });
      }
    }
    
    if (!isXMLEntity()) {
      if (resultBytes == null) {
        resultBytes = new ByteArrayOutputStream();

        if (xmlResult instanceof DOMResult) {
          Node node = ((DOMResult) xmlResult).getNode();
          Node nextSibling = ((DOMResult) xmlResult).getNextSibling();
          
          DOMSource xmlSource;
          if (nextSibling != null) {
            xmlSource = new DOMSource(nextSibling.getPreviousSibling());
          } else if (node != null) {
            xmlSource = new DOMSource(node.getFirstChild());
          } else {
            log
                .error("IssuerTemplate transformation returned no node.");
            throw new SLCommandException(4000,
                SLExceptionMessages.EC4000_UNCLASSIFIED_IDLINK_TRANSFORMATION_FAILED,
                new Object[] { issuerTemplate });
          }
          TransformerFactory transformerFactory = TransformerFactory.newInstance();
          try {
            Transformer transformer = transformerFactory.newTransformer();
            transformer.transform(xmlSource, new StreamResult(resultBytes));
          } catch (TransformerConfigurationException e) {
            log.error(e);
            throw new SLCommandException(4000,
                SLExceptionMessages.EC4000_UNCLASSIFIED_IDLINK_TRANSFORMATION_FAILED,
                new Object[] { issuerTemplate });
          } catch (TransformerException e) {
            log.error(e);
            throw new SLCommandException(4000,
                SLExceptionMessages.EC4000_UNCLASSIFIED_IDLINK_TRANSFORMATION_FAILED,
                new Object[] { issuerTemplate });
          }
        } else if (xmlResult instanceof StreamResult) {
          OutputStream outputStream = ((StreamResult) xmlResult).getOutputStream();
          if (outputStream instanceof ByteArrayOutputStream) {
            result.setResultBytes(((ByteArrayOutputStream) outputStream).toByteArray());
          } else {
            log.error("ContentIsXMLEntity is set to 'false'. However, an XMLResult has already been set.");
            throw new SLCommandException(4000,
                SLExceptionMessages.EC4000_UNCLASSIFIED_IDLINK_TRANSFORMATION_FAILED,
                new Object[] { issuerTemplate });
          }
        }
      } 
      result.setResultBytes(resultBytes.toByteArray());
    }
    
    return result;

  }
  

}
