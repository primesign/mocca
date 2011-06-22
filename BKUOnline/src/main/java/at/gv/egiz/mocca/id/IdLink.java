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



package at.gv.egiz.mocca.id;

import iaik.xml.crypto.dom.DOMCryptoContext;
import iaik.xml.crypto.dsig.keyinfo.KeyValueType;

import java.io.IOException;
import java.io.InputStream;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dom.DOMStructure;
import javax.xml.crypto.dsig.Manifest;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.XMLObject;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.X509Data;

import oasis.names.tc.saml._1_0.assertion.AnyType;
import oasis.names.tc.saml._1_0.assertion.AssertionType;
import oasis.names.tc.saml._1_0.assertion.AttributeStatementType;
import oasis.names.tc.saml._1_0.assertion.AttributeType;
import oasis.names.tc.saml._1_0.assertion.StatementAbstractType;
import oasis.names.tc.saml._1_0.assertion.SubjectConfirmationType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import at.gv.e_government.reference.namespace.persondata._20020228_.PhysicalPersonType;
import at.gv.egiz.bku.utils.StreamUtil;

public class IdLink {
  
  protected Logger log = LoggerFactory.getLogger(IdLink.class);
  
  /**
   * The IdLink is backed by a DOM.
   */
  protected Node node;
  
  /**
   * The <code>Assertion</code> (root element) of the IdLink.
   */
  protected AssertionType assertion;
  
  /**
   * The citizen's asserted public keys.
   */
  protected List<PublicKey> citizenPublicKeys;
  
  /**
   * The XMLSignature.
   */
  protected XMLSignature signature;
  
  /**
   * The assertion's signer certificate.
   */
  protected X509Certificate signerCert;
  
  /**
   * Is the assertion's signature manifest valid?
   */
  protected Boolean manifestValid;
  
  /**
   * Is the assertion's signature valid?
   */
  protected Boolean signatureValid;
  
  /**
   * The personal identifier
   */
  protected IdLinkPersonData personData;
 
  public IdLink(Element node, AssertionType assertion) throws JAXBException {
    this.node = node;
    this.assertion = assertion;
  }
  
  public PhysicalPersonType getPhysicalPerson() {

    AttributeStatementType attributeStatement = getAttributeStatement();
    if (attributeStatement != null) {
      JAXBElement<?> subjectConfirmation = attributeStatement.getSubject().getContent().get(0);
      if (subjectConfirmation.getDeclaredType() == SubjectConfirmationType.class) {
        Object data = ((SubjectConfirmationType) subjectConfirmation.getValue())
            .getSubjectConfirmationData().getContent().get(0);
        if (data instanceof JAXBElement<?>
            && ((JAXBElement<?>) data).getValue() instanceof PhysicalPersonType) {
          return (PhysicalPersonType) ((JAXBElement<?>) data).getValue();
        }
      }
    }

    return null;
  }
  
  public AttributeStatementType getAttributeStatement() {
    
    StatementAbstractType statement = 
      assertion.getStatementOrSubjectStatementOrAuthenticationStatement().get(0);
    
    if (statement instanceof AttributeStatementType) {
      return (AttributeStatementType) statement;
    }

    return null;
    
  }

  public IdLinkPersonData getPersonData() throws MarshalException {
    if (personData == null) {
      try {
        personData = new IdLinkPersonData(getPhysicalPerson());
      } catch (ParseException e) {
        throw new MarshalException(e);
      }
    }
    return personData;
  }
  
  public List<PublicKey> getCitizenPublicKeys() throws MarshalException {
    if (citizenPublicKeys == null) {
      
      citizenPublicKeys = new ArrayList<PublicKey>();

      AttributeStatementType attributeStatement = getAttributeStatement();
      if (attributeStatement != null) {
        List<AttributeType> attributes = attributeStatement.getAttribute();
        for (AttributeType attribute : attributes) {
          if ("urn:publicid:gv.at:namespaces:identitylink:1.2".equals(attribute.getAttributeNamespace())
              && "CitizenPublicKey".equals(attribute.getAttributeName())) {
            List<AnyType> value = attribute.getAttributeValue();
            if (value.size() == 1 && value.get(0).getContent().size() == 1) {
              Object object = value.get(0).getContent().get(0);
              if (object instanceof Element) {
                Element element = (Element) object;
                DOMStructure structure = iaik.xml.crypto.dom.DOMStructure.getInstance(element, new DOMCryptoContext());
                if (structure instanceof KeyValueType) {
                  citizenPublicKeys.add(((KeyValueType) structure).getPublicKey());
                }
              }
            }
          }
        }
      }
      
    }
    return citizenPublicKeys;
  }
  
  public XMLSignature getXMLSignature() throws MarshalException {
    if (signature == null) {

      Node n = node.getLastChild();
      while (n != null && n.getNodeType() != Node.ELEMENT_NODE) {
        n = n.getPreviousSibling();
      }

      if (n != null 
          && XMLSignature.XMLNS.equals(n.getNamespaceURI())
          && "Signature".equals(n.getLocalName())) {
        
        XMLSignatureFactory signatureFactory = XMLSignatureFactory.getInstance();
        signature = signatureFactory.unmarshalXMLSignature(new DOMStructure(n));
      }
      
      
    }
    return signature;
  }
  
  public X509Certificate getSignerCert() throws MarshalException {
    if (signerCert == null) {

      if (getXMLSignature() != null) {

        KeyInfo keyInfo = signature.getKeyInfo();
        if (keyInfo != null) {
          List<?> content = keyInfo.getContent();
          for (Object data : content) {
            if (data instanceof X509Data) {
              List<?> x509Data = ((X509Data) data).getContent();
              for (Object object : x509Data) {
                if (object instanceof X509Certificate) {
                  signerCert = (X509Certificate) object;
                  return signerCert;
                }
              }
            }
          }
        }
      }
    }
    return signerCert;
  }
  
  
  @SuppressWarnings("unchecked")
  public boolean verifySignature() throws MarshalException, XMLSignatureException {
    if (signatureValid == null) {
      if (getXMLSignature() != null && getSignerCert() != null) {
        
        DOMValidateContext validateContext = new DOMValidateContext(signerCert.getPublicKey(), node);
        validateContext.setProperty("javax.xml.crypto.dsig.cacheReference", Boolean.TRUE);
        
        signatureValid = signature.validate(validateContext);
        
        // logging
        if (!signatureValid && log.isTraceEnabled()) {
          List<Reference> references = signature.getSignedInfo().getReferences();
          for (Reference reference : references) {
            if (!Manifest.TYPE.equals(reference.getType())) {
              if (!reference.validate(validateContext)) { 
                InputStream digestInputStream = reference.getDigestInputStream();
                if (digestInputStream != null) {
                  try {
                    log.trace("SignedInfo's reference digest input:\n{}",
                        StreamUtil.asString(digestInputStream, "UTF-8"));
                  } catch (IOException e) {
                    log.info("Failed to get SignedInfos's reference digest input", e.toString());
                  }
                }
              } else {
                try {
                  log.trace("Signature canonicalized data:\n{}", StreamUtil.asString(signature
                      .getSignedInfo().getCanonicalizedData(), "UTF-8"));
                } catch (IOException e) {
                  log.info("Failed to get canonicalized data.", e);
                }
              }
              break;
            }
          }
        }
        
      }
    } 
    return signatureValid;
  }
  
  @SuppressWarnings("unchecked")
  public boolean verifyManifest() throws MarshalException, XMLSignatureException {
    if (manifestValid == null) {
      if (getXMLSignature() != null && getSignerCert() != null) {

        DOMValidateContext validateContext = new DOMValidateContext(signerCert.getPublicKey(), node);
        if (log.isTraceEnabled()) {
          // enable reference caching in trace log-level
          validateContext.setProperty("javax.xml.crypto.dsig.cacheReference", Boolean.TRUE);
        }
        boolean valid = false;
        
        // validate manifest
        List<XMLObject> objects = signature.getObjects();
        for (XMLObject object : objects) {
          List<?> content = object.getContent();
          if (content.get(0) instanceof Manifest) {
            Manifest manifest = (Manifest) content.get(0);
            List<Reference> references = manifest.getReferences();
            for (Reference reference : references) {
              
              valid = reference.validate(validateContext);
              
              // logging
              if (!valid && log.isTraceEnabled()) {
                InputStream digestInputStream = reference.getDigestInputStream();
                if (digestInputStream != null) {
                  try {
                    log.trace("Manifest's reference digest input:\n{}",
                        StreamUtil.asString(digestInputStream, "UTF-8"));
                  } catch (IOException e) {
                    log.info("Failed to get Manifest's reference digest input", e.toString());
                  }
                }
              }
              break;
            }
          }
        }

        // validate reference to manifest
        if (valid) {
          List<Reference> references = signature.getSignedInfo().getReferences();
          for (Reference reference : references) {
            if (Manifest.TYPE.equals(reference.getType())) {
              
              boolean refValid = reference.validate(validateContext);

              // logging
              if (!refValid && log.isTraceEnabled()) {
                InputStream digestInputStream = reference.getDigestInputStream();
                if (digestInputStream != null) {
                  try {
                    log.trace("SignedInfo's manifest reference digest input:\n{}",
                        StreamUtil.asString(digestInputStream, "UTF-8"));
                  } catch (IOException e) {
                    log.info("Failed to get SignedInfos's manifest reference digest input", e.toString());
                  }
                }
              }

              valid &= refValid;
              
            }
          }
        }
        
        manifestValid = valid;
        
      }
      
    }
    return manifestValid;
  }
  
}
