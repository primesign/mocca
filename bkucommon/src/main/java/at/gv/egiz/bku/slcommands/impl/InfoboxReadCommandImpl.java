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

import iaik.asn1.CodingException;
import iaik.asn1.DerCoder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
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
import at.buergerkarte.namespaces.securitylayer._1.InfoboxReadParamsBinaryFileType;
import at.buergerkarte.namespaces.securitylayer._1.InfoboxReadRequestType;
import at.gv.egiz.bku.slcommands.InfoboxReadCommand;
import at.gv.egiz.bku.slcommands.SLCommand;
import at.gv.egiz.bku.slcommands.SLCommandContext;
import at.gv.egiz.bku.slcommands.SLResult;
import at.gv.egiz.bku.slexceptions.SLCommandException;
import at.gv.egiz.bku.slexceptions.SLExceptionMessages;
import at.gv.egiz.bku.slexceptions.SLRuntimeException;
import at.gv.egiz.idlink.CompressedIdentityLinkFactory;
import at.gv.egiz.idlink.IdentityLinkTransformer;
import at.gv.egiz.idlink.ans1.IdentityLink;
import at.gv.egiz.stal.InfoboxReadRequest;
import at.gv.egiz.stal.InfoboxReadResponse;
import at.gv.egiz.stal.STALRequest;

/**
 * This class implements the security layer command
 * <code>InfoboxReadRequest</code>.
 * <p>
 * <b>NOTE:</b> Currently the only supported infobox identifier is '
 * <code>IdentityLink</code>'.
 * </p>
 * 
 * @author mcentner
 */
public class InfoboxReadCommandImpl extends SLCommandImpl<InfoboxReadRequestType> implements
    InfoboxReadCommand {
  
  /**
   * Logging facility.
   */
  protected static Log log = LogFactory.getLog(InfoboxReadCommandImpl.class);

  public static final String INFOBOX_IDENTIFIER_CERTIFICATES = "Certificates";
  
  public static final String BOX_SPECIFIC_PARAMETER_IDENTITY_LINK_DOMAIN_IDENTIFIER = "IdentityLinkDomainIdentifier";
  
  public static final String INFOBOX_IDENTIFIER_IDENTITY_LINK = "IdentityLink";

  /**
   * The <code>InfoboxIdentifier</code>
   */
  protected String infoboxIdentifier;
  
  /**
   * The <code>IdentityLinkDomainIdentifier</code> value of an IdentyLink infobox.
   */
  protected String identityLinkDomainIdentifier;

  /**
   * Is content XML entity?
   */
  protected boolean isXMLEntity;
  
  @Override
  public String getName() {
    return "InfoboxReadRequest";
  }

  /**
   * @return the infoboxIdentifier
   */
  public String getInfoboxIdentifier() {
    return infoboxIdentifier;
  }

  @Override
  public void init(SLCommandContext ctx, Object request) throws SLCommandException {
    super.init(ctx, request);
    
    InfoboxReadRequestType req = getRequestValue();
    
    infoboxIdentifier = req.getInfoboxIdentifier();
    
    InfoboxReadParamsBinaryFileType binaryFileParameters = req.getBinaryFileParameters();
    if (binaryFileParameters != null) {
      isXMLEntity = binaryFileParameters.isContentIsXMLEntity();
      log.debug("Got ContentIsXMLEntity=" + isXMLEntity + ".");
    }
    
    if (INFOBOX_IDENTIFIER_IDENTITY_LINK.equals(infoboxIdentifier)) {
      
      if (req.getAssocArrayParameters() != null) {
        log.info("Got AssocArrayParameters but Infobox type is BinaryFile.");
        throw new SLCommandException(4010);
      }
      
      
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
          identityLinkDomainIdentifier = (String) element.getValue();
          log.debug("Got sl:IdentityLinkDomainIdentifier: " + identityLinkDomainIdentifier);
        } else {
          log.info("Got invalid BoxSpecificParameters.");
          throw new SLCommandException(4010);
        }
      }
      
    } else {
      throw new SLCommandException(4002,
          SLExceptionMessages.EC4002_INFOBOX_UNKNOWN,
          new Object[] { infoboxIdentifier });
    }
    
  }

  @Override
  public SLResult execute() {
    try {
      return readIdentityLink();
    } catch (SLCommandException e) {
      return new ErrorResultImpl(e);
    }
  }
 
  /**
   * Gets the IdentitiyLink form the next STAL response.
   * 
   * @return the IdentityLink
   * 
   * @throws SLCommandException if getting the IdentitiyLink fails
   */
  private IdentityLink getIdentityLinkFromResponses() throws SLCommandException {

    // IdentityLink
    InfoboxReadResponse response;
    if (hasNextResponse()) {
      response = (InfoboxReadResponse) nextResponse(InfoboxReadResponse.class);
      byte[] idLink = response.getInfoboxValue();
      try {
        return new IdentityLink(DerCoder.decode(idLink));
      } catch (CodingException e) {
        log.info("Failed to decode infobox '" + INFOBOX_IDENTIFIER_IDENTITY_LINK + "'.", e);
        throw new SLCommandException(4000,
            SLExceptionMessages.EC4000_UNCLASSIFIED_INFOBOX_INVALID,
            new Object[] { INFOBOX_IDENTIFIER_IDENTITY_LINK });
      }
    } else {
      log.info("No infobox '" + INFOBOX_IDENTIFIER_IDENTITY_LINK + "' returned from STAL.");
      throw new SLCommandException(4000);
    }
    
  }
  
  /**
   * Gets the list of certificates from the next STAL responses.
   * 
   * @return the list of certificates
   * 
   * @throws SLCommandException if getting the list of certificates fails
   */
  private List<X509Certificate> getCertificatesFromResponses() throws SLCommandException {
    
    List<X509Certificate> certificates = new ArrayList<X509Certificate>();

    CertificateFactory certFactory;
    try {
      certFactory = CertificateFactory.getInstance("X509");
    } catch (CertificateException e) {
      // we should always be able to get an X509 certificate factory
      log.error("CertificateFactory.getInstance(\"X509\") failed.", e);
      throw new SLRuntimeException(e);
    }
    
    InfoboxReadResponse response;
    while(hasNextResponse()) {
      response = (InfoboxReadResponse) nextResponse(InfoboxReadResponse.class);
      byte[] cert = response.getInfoboxValue();
      try {
        certificates.add((X509Certificate) certFactory.generateCertificate(new ByteArrayInputStream(cert)));
      } catch (CertificateException e) {
        log.info("Failed to decode certificate.", e);
        throw new SLCommandException(4000,
            SLExceptionMessages.EC4000_UNCLASSIFIED_INFOBOX_INVALID,
            new Object[] { INFOBOX_IDENTIFIER_CERTIFICATES });
      }
    }
    
    return certificates;

  }

  /**
   * Uses STAL to read the IdentityLink.
   * 
   * @return the corresponding security layer result
   * 
   * @throws SLCommandException if reading the IdentityLink fails
   */
  private SLResult readIdentityLink() throws SLCommandException {
    
    List<STALRequest> stalRequests = new ArrayList<STALRequest>();

    InfoboxReadRequest infoboxReadRequest;
    // get raw identity link
    infoboxReadRequest = new InfoboxReadRequest();
    infoboxReadRequest.setInfoboxIdentifier(INFOBOX_IDENTIFIER_IDENTITY_LINK);
    infoboxReadRequest.setDomainIdentifier(identityLinkDomainIdentifier);
    stalRequests.add(infoboxReadRequest);
    
    // get certificates
    infoboxReadRequest = new InfoboxReadRequest();
    infoboxReadRequest.setInfoboxIdentifier("SecureSignatureKeypair");
    stalRequests.add(infoboxReadRequest);
    
    infoboxReadRequest = new InfoboxReadRequest();
    infoboxReadRequest.setInfoboxIdentifier("CertifiedKeypair");
    stalRequests.add(infoboxReadRequest);

    requestSTAL(stalRequests);

    IdentityLink identityLink = getIdentityLinkFromResponses();
    List<X509Certificate> certificates = getCertificatesFromResponses();
    
    
    CompressedIdentityLinkFactory idLinkFactory = CompressedIdentityLinkFactory.getInstance();
    JAXBElement<CompressedIdentityLinkType> compressedIdentityLink = idLinkFactory
        .createCompressedIdentityLink(identityLink, certificates, identityLinkDomainIdentifier);

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
          new Object[] { INFOBOX_IDENTIFIER_IDENTITY_LINK });
    }
    
    InfoboxReadResultImpl result = new InfoboxReadResultImpl();
    ByteArrayOutputStream resultBytes = null;
    Result xmlResult = (isXMLEntity || identityLinkDomainIdentifier != null) 
          ? result.getXmlResult(true) 
          : new StreamResult((resultBytes = new ByteArrayOutputStream()));
    try {
      identityLinkTransformer.transformIdLink(issuerTemplate, new DOMSource(document), xmlResult);
    } catch (IOException e) {
      // we should not get an IOException as we are writing into a DOMResult
      throw new SLRuntimeException(e);
    } catch (TransformerException e) {
      log.info("Faild to transform CompressedIdentityLink.", e);
      throw new SLCommandException(4000,
          SLExceptionMessages.EC4000_UNCLASSIFIED_IDLINK_TRANSFORMATION_FAILED,
          new Object[] { issuerTemplate });
    }
    
    // TODO: Report BUG in IssuerTemplates
    // Some IssuerTemplate stylesheets do not consider the pr:Type-Element of the CompressedIdentityLink ...
    if (identityLinkDomainIdentifier != null) {
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
                  + identityLinkDomainIdentifier
                  + "' has been given. However, it cannot be set, as the transformation result does not contain a node.");
          throw new SLCommandException(4000,
              SLExceptionMessages.EC4000_UNCLASSIFIED_IDLINK_TRANSFORMATION_FAILED,
              new Object[] { issuerTemplate });
        }
        IdentityLinkTransformer.setDomainIdentifier(idLinkNode, identityLinkDomainIdentifier);
      } else {
        log
            .error("An IdentityLinkDomainIdentifier of '"
                + identityLinkDomainIdentifier
                + "' has been given. However, it cannot be set, as the transformation result is not of type DOM.");
        throw new SLCommandException(4000,
            SLExceptionMessages.EC4000_UNCLASSIFIED_IDLINK_TRANSFORMATION_FAILED,
            new Object[] { issuerTemplate });
      }
    }
    
    if (!isXMLEntity) {
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
      } else {
        result.setResultBytes(resultBytes.toByteArray());
      }
    }
    
    
    return result;
    
  }
}
