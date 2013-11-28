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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dom.DOMStructure;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.XMLObject;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.stream.XMLStreamException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSException;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSParser;
import org.w3c.dom.ls.LSResourceResolver;
import org.w3c.dom.ls.LSSerializer;

import at.buergerkarte.namespaces.securitylayer._1_2_3.Base64XMLLocRefReqRefContentType;
import at.buergerkarte.namespaces.securitylayer._1_2_3.Base64XMLOptRefContentType;
import at.buergerkarte.namespaces.securitylayer._1_2_3.DataObjectAssociationType;
import at.buergerkarte.namespaces.securitylayer._1_2_3.DataObjectInfoType;
import at.buergerkarte.namespaces.securitylayer._1_2_3.SignatureInfoCreationType;
import at.gv.egiz.bku.binding.HttpUtil;
import at.gv.egiz.bku.slexceptions.SLCommandException;
import at.gv.egiz.bku.slexceptions.SLRequestException;
import at.gv.egiz.bku.slexceptions.SLViewerException;
import at.gv.egiz.bku.utils.HexDump;
import at.gv.egiz.bku.utils.urldereferencer.StreamData;
import at.gv.egiz.bku.utils.urldereferencer.URLDereferencer;
import at.gv.egiz.dom.DOMUtils;
import at.gv.egiz.slbinding.impl.XMLContentType;
import at.gv.egiz.stal.STAL;
import at.gv.egiz.xades.QualifyingProperties1_4Factory;
import at.gv.egiz.xades.QualifyingPropertiesException;
import at.gv.egiz.xades.QualifyingPropertiesFactory;

/**
 * This class represents an XML-Signature as to be created by the
 * security layer command <code>CreateXMLSignatureRequest</code>. 
 * 
 * @author mcentner
 */
public class Signature {
  public static final String XMLDSIG_PREFIX = "dsig";
  
  /**
   * Logging facility.
   */
  private final Logger log = LoggerFactory.getLogger(Signature.class);

  /**
   * The DOM implementation used.
   */
  private DOMImplementationLS domImplLS;
  
  /**
   * The SignatureContext for the XMLSignature.
   */
  private SignatureContext ctx;
  
  /**
   * The list of {@link DataObject}s for this signature.
   */
  private List<DataObject> dataObjects = new ArrayList<DataObject>();
  
  /**
   * A mapping from the <code>Id</code>-attribute values of this signature's 
   * <code>ds:Reference</code>s to the corresponding {@link DataObject}s.
   */
//  private Map<String, DataObject> dataObjectReferencIds = new HashMap<String, DataObject>();
  
  /**
   * The SignatureEnvironment for this signature.
   */
  private SignatureLocation signatureLocation;
  
  /**
   * The XML signature.
   */
  private XMLSignature xmlSignature;
  
  /**
   * A list of attributes of type <code>xsd:ID</code> to be registered in the {@link DOMSignContext}.
   */
  private List<IdAttribute> idAttributes = new ArrayList<IdAttribute>();
  
  /**
   * The signer's X509 certificate.
   */
  private X509Certificate signerCertificate;
  
  /**
   * The signing time.
   */
  private Date signingTime;
  
  /**
   * Whether to use XAdES v1.4 (v1.1 else)
   */
  private boolean useXAdES14;
  
  /**
   * Creates a new SLXMLSignature instance.
   * @param urlDereferencer TODO
   */
  public Signature(URLDereferencer urlDereferencer,
      IdValueFactory idValueFactory,
      AlgorithmMethodFactory algorithmMethodFactory,
      boolean useXAdES14) {
    
    this.useXAdES14 = useXAdES14;
    
    domImplLS = DOMUtils.getDOMImplementationLS();
    
    ctx = new SignatureContext();
  
    ctx.setSignatureFactory(XMLSignatureFactory.getInstance());

    ctx.setUrlDereferencer(urlDereferencer);
    ctx.setIdValueFactory(idValueFactory);
    ctx.setAlgorithmMethodFactory(algorithmMethodFactory);
    
  }

  /**
   * @return the Document containing this Signature
   */
  public Document getDocument() {
    return ctx.getDocument();
  }
  
  /**
   * @return the parent Node for this Signature
   */
  public Node getParent() {
    return (signatureLocation != null) ? signatureLocation.getParent() : null;
  }

  /**
   * @return the next sibling Node for this Signature
   */
  public Node getNextSibling() {
    return (signatureLocation != null) ? signatureLocation.getNextSibling() : null;
  }
  
  /**
   * @return the XMLSignature
   */
  public XMLSignature getXMLSignature() {
    return xmlSignature;
  }
  
  /**
   * @return the list of {@link Reference}s of this Signature
   */
  @SuppressWarnings("unchecked")
  public List<Reference> getReferences() {
    return (xmlSignature != null) ? xmlSignature.getSignedInfo().getReferences() : null;
  }
  
  /**
   * @return the list of {@link XMLObject}s of this Signature
   */
  @SuppressWarnings("unchecked")
  public List<XMLObject> getXMLObjects() {
    return (xmlSignature != null) ? xmlSignature.getObjects() : null;
  }

  /**
   * Prepares the signature document with the information given by the
   * <code>signatureInfo</code> provided.
   * 
   * @param signatureInfo
   *          the <code>SignatureInfo</code>
   * 
   * @throws SLCommandException
   *           if processing fails for any reason
   * @throws IllegalStateException
   *           if the <code>parent</code> node has already been set
   * @throws NullPointerException
   *           if <code>signatureInfo</code> is <code>null</code>
   */
  public void setSignatureInfo(SignatureInfoCreationType signatureInfo) throws SLCommandException {
    
    if (signatureLocation != null) {
      throw new IllegalStateException("SignatureEnvironment already set.");
    }
    
    Base64XMLOptRefContentType signatureEnvironment = signatureInfo.getSignatureEnvironment();
    
    if (signatureEnvironment == null) {

      // no SignatureEnvironment, so we use an empty document and the document as parent
      ensureSignatureLocation();
      
    } else {
      
      // parse SignatureEnvrionment and use as document
      Document document = parseSignatureEnvironment(signatureEnvironment, signatureInfo.getSupplement());
      ctx.setDocument(document);

      signatureLocation = new SignatureLocation(ctx);
      signatureLocation.setSignatureInfo(signatureInfo);
      
    }
    
  }

  /**
   * Ensures a SignatureLocation for this Signature.
   */
  private void ensureSignatureLocation() {

    if (signatureLocation == null) {
      Document document = DOMUtils.createDocument();
      ctx.setDocument(document);
      
      signatureLocation = new SignatureLocation(ctx);
      signatureLocation.setParent(document);
    }
    
  }

  /**
   * Adds a DataObject with the information given by the
   * <code>dataObjectInfo</code> provided to this Signature.
   * 
   * @param dataObjectInfo
   *          the <code>DataObjectInfo</code> element
   * 
   * @throws SLCommandException
   *           if adding the DataObject fails
   * @throws SLRequestException
   *           if the information provided by the given
   *           <code>dataObjectInfo</code> does not conform to the security
   *           layer specification
   * @throws NullPointerException
   *           if <code>dataObjectInfo</code> is <code>null</code>
   */
  public void addDataObject(DataObjectInfoType dataObjectInfo) throws SLCommandException, SLRequestException {
    
    ensureSignatureLocation();
    
    DataObject dataObject = new DataObject(ctx);
    dataObject.setDataObjectInfo(dataObjectInfo);
    
    dataObjects.add(dataObject);
    
//    dataObjectReferencIds.put(dataObject.getReference().getId(), dataObject);
    
  }
  
  /**
   * Sets the <code>SigningTime</code> qualifying property of this Signature.
   * 
   * @param signingTime the signing time to set
   */
  public void setSigningTime(Date signingTime) {
    this.signingTime = signingTime;
  }

  /**
   * Sets the <code>SignerCertificate</code> qualifying property of this Signature.
   * 
   * @param certificate the signer's certificate
   */
  public void setSignerCertificate(X509Certificate certificate) {
    this.signerCertificate = certificate;
  }
  
  /**
   * Builds the XMLSignature data structure of this Signature as configured by
   * the various setter methods.
   * 
   * @throws SLCommandException if building this signature fails
   */
  public void buildXMLSignature() throws SLCommandException {
    
    String signatureId = ctx.getIdValueFactory().createIdValue("Signature");

    List<XMLObject> objects = new ArrayList<XMLObject>();
    List<Reference> references = new ArrayList<Reference>();
    
    // add all data objects
    for (DataObject dataObject : dataObjects) {
      if (dataObject.getXmlObject() != null) {
        objects.add(dataObject.getXmlObject());
      }
      if (dataObject.getReference() != null) {
        references.add(dataObject.getReference());
      }
    }

    if (useXAdES14)
      addXAdES1_4ObjectAndReference(objects, references, signatureId);
    else
      addXAdESObjectAndReference(objects, references, signatureId);
    
    XMLSignatureFactory signatureFactory = ctx.getSignatureFactory();
    AlgorithmMethodFactory algorithmMethodFactory = ctx.getAlgorithmMethodFactory();
    
    CanonicalizationMethod cm;
    SignatureMethod sm;
    try {
      cm = algorithmMethodFactory.createCanonicalizationMethod(ctx);
      sm = algorithmMethodFactory.createSignatureMethod(ctx);
    } catch (NoSuchAlgorithmException e) {
      log.error("Failed to get Canonicalization or Signature algorithm.", e);
      throw new SLCommandException(4006);
    } catch (InvalidAlgorithmParameterException e) {
      log.error("Failed to get Canonicalization or Signature algorithm.", e);
      throw new SLCommandException(4006);
    }
    
    String siId = ctx.getIdValueFactory().createIdValue("SignedInfo");
    
    SignedInfo si = signatureFactory.newSignedInfo(cm, sm, references, siId);
    
    KeyInfo ki = null;
    if (signerCertificate != null) {
      KeyInfoFactory kif = KeyInfoFactory.getInstance();
      X509Data x509Data = kif.newX509Data(Collections.singletonList(signerCertificate));
      ki = kif.newKeyInfo(Collections.singletonList(x509Data));
    }
    
    String signatureValueId = ctx.getIdValueFactory().createIdValue("SignatureValue");
    
    xmlSignature = signatureFactory.newXMLSignature(si, ki, objects, signatureId, signatureValueId);
    
  }

  /**
   * Sign this Signature using the given <code>signContext</code>.
   * <p>
   * Call's {@link #buildXMLSignature()} if it has not been called yet.
   * </p>
   * 
   * @param signContext
   *          the signing context
   * 
   * @throws MarshalException
   *           if marshalling the XMLSignature fails
   * @throws XMLSignatureException
   *           if signing the XMLSignature fails
   * @throws SLCommandException
   *           if building the XMLSignature fails
   * @throws SLViewerException 
   * @throws NullPointerException
   *           if <code>signContext</code> is <code>null</code>
   */
  public void sign(DOMSignContext signContext) throws MarshalException, XMLSignatureException, SLCommandException, SLViewerException {

    if (xmlSignature == null) {
      buildXMLSignature();
    }
    
    for (IdAttribute idAttribute : idAttributes) {
      signContext.setIdAttributeNS(idAttribute.element, idAttribute.namespaceURI, idAttribute.localName);
    }
    
    // DO NOT USE: 
    // signContext.setProperty("iaik.xml.crypto.dsig.sign-over", Boolean.TRUE);
    
    signContext.setProperty("javax.xml.crypto.dsig.cacheReference", Boolean.TRUE);
    
    signContext.putNamespacePrefix(XMLSignature.XMLNS,XMLDSIG_PREFIX); 
    
    signContext.setURIDereferencer(new URIDereferncerAdapter(ctx.getUrlDereferencer()));
    
    try {
      xmlSignature.sign(signContext);
    } catch (XMLSignatureException e) {
      Throwable cause = e.getCause();
      while (cause != null) {
        if (cause instanceof STALSignatureException) {
          STALSignatureException stalCause = (STALSignatureException) cause;
          if (stalCause.getCause() instanceof SLViewerException) {
            throw (SLViewerException) stalCause.getCause(); 
          }
          int errorCode = stalCause.getErrorCode();
          SLCommandException commandException = new SLCommandException(errorCode);
          log.info("Failed to sign signature: {}", stalCause.getMessage(), e);
          throw commandException;
        } else {
          cause = cause.getCause();
        }
      }
      throw e;
    }
    
    // debug
    if (log.isTraceEnabled()) {
      for (DataObject dataObject : dataObjects) {
        Reference reference = dataObject.getReference();
        InputStream digestInputStream = reference.getDigestInputStream();
        if (digestInputStream != null) {
          String mimeType = dataObject.getMimeType();
          StringBuilder sb = new StringBuilder();
          sb.append("DigestInput for Reference with id='");
          sb.append(reference.getId());
          sb.append("' (MIME-Type=");
          sb.append(dataObject.getMimeType());
          sb.append("):\n");
          try {
            if (mimeType != null && (
                mimeType.startsWith("text") ||
                "application/xhtml+xml".equals(mimeType))) {
              byte[] b = new byte[512];
              for (int l; (l = digestInputStream.read(b)) != -1;) {
                sb.append(new String(b, 0, l));
              }
            } else {
              sb.append(HexDump.hexDump(digestInputStream));
            }
          } catch (IOException e) {
            log.error("Failed to log DigestInput.", e);
          }
          log.trace(sb.toString());
        } else {
          log.trace("Reference caching is not enabled.");
        }
      }
      for (Reference reference : getReferences()) {
        if (reference.getType() != null) {
          InputStream digestInputStream = reference.getDigestInputStream();
          if (digestInputStream != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("DigestInput for Reference with id='");
            sb.append(reference.getId());
            sb.append("'; Type:");
            sb.append(reference.getType());
            sb.append("):\n");
            try {
              byte[] b = new byte[512];
              for (int l; (l = digestInputStream.read(b)) != -1;) {
                sb.append(new String(b, 0, l));
              }
            } catch (IOException e) {
              log.error("Failed to log DigestInput.", e);
            }
            log.trace(sb.toString());
          } else {
            log.trace("Reference caching is not enabled.");
          }
          
        }
      }
    }
    
  }

  /**
   * Sign this Signature using the given <code>stal</code> implementation and
   * <code>keyboxIdentifier</code>.
   * <p>
   * This method configures an appropriate {@link DOMSignContext} and calls
   * {@link #sign(DOMSignContext)}. If {@link #buildXMLSignature()} has not been
   * called yet, it is called by this method.
   * </p>
   * 
   * @param stal
   *          the STAL implementation to use
   * @param keyboxIdentifier
   *          the KeyboxIdentifier to use
   * 
   * @throws MarshalException
   *           if marshalling this Signature fails
   * @throws XMLSignatureException
   *           if signing this Signature fails
   * @throws SLCommandException
   *           if building this Signature fails 
   * @throws SLViewerException 
   * @throws NullPointerException
   *           if <code>stal</code> or <code>keyboxIdentifier</code> is
   *           <code>null</code>
   */
  public void sign(STAL stal, String keyboxIdentifier) throws MarshalException, XMLSignatureException, SLCommandException, SLViewerException {

    if (stal == null) {
      throw new NullPointerException("Argument 'stal' must not be null.");
    }
    
    if (keyboxIdentifier == null) {
      throw new NullPointerException("Argument 'keyboxIdentifier' must not be null.");
    }
    
    if (xmlSignature == null) {
      buildXMLSignature();
    }
    
    SignatureMethod signatureMethod = xmlSignature.getSignedInfo().getSignatureMethod();
    String algorithm = signatureMethod.getAlgorithm();
    
    //don't get hashDataInputs (digestInputStreams) now, only once Signature.sign() was called (cf STALSignature.engineSign)
    PrivateKey privateKey = new STALPrivateKey(stal, algorithm, keyboxIdentifier, dataObjects); // hashDataInputs);
    
    DOMSignContext signContext;
    if (getNextSibling() == null) {
      signContext = new DOMSignContext(privateKey, getParent());
    } else {
      signContext = new DOMSignContext(privateKey, getParent(), getNextSibling());
    }
    
    sign(signContext);
  }
  
//  @Override
//  public HashDataInput getHashDataInput(final String referenceId) {
//      final DataObject dataObject = dataObjectReferencIds.get(referenceId);
//      if (dataObject != null) {
//          return new HashDataInput() {
//
//              InputStream hashDataInput = dataObject.getReference().getDigestInputStream();
//              
//                @Override
//                public String getReferenceId() {
//                    return referenceId;
//                }
//
//                @Override
//                public String getMimeType() {
//                    return dataObject.getMimeType();
//                }
//
//                @Override
//                public InputStream getHashDataInput() {
//                    return hashDataInput;
//                }
//          };
//      } 
//      return null;
//  }

  /**
   * Adds the XAdES <code>QualifyingProperties</code> as an
   * <code>ds:Object</code> and a corresponding <code>ds:Reference</code> to
   * its <code>SignedProperties</code> element to this Signature.
   * 
   * @param objects
   *          the list of <code>ds:Objects</code> to add the created
   *          <code>ds:Object</code> to
   * @param references
   *          the list of <code>ds:References</code> to add the created
   *          <code>ds:Reference</code> to
   * @param signatureId TODO
   * @throws SLCommandException
   *           if creating and adding the XAdES
   *           <code>QualifyingProperties</code> fails
   * @throws NullPointerException
   *           if <code>objects</code> or <code>references</code> is
   *           <code>null</code>
   */
  private void addXAdESObjectAndReference(List<XMLObject> objects, List<Reference> references, String signatureId) throws SLCommandException {
    
    QualifyingPropertiesFactory factory = QualifyingPropertiesFactory.getInstance();
    
    String idValue = ctx.getIdValueFactory().createIdValue("SignedProperties");
    
    Date date = (signingTime != null) ? signingTime : new Date();
    
    List<X509Certificate> signingCertificates;
    if (signerCertificate != null) {
      signingCertificates = Collections.singletonList(signerCertificate);
    } else {
      signingCertificates = Collections.emptyList();
    }
    
    // TODO: report MOA-SP bug
    //
    // The security layer specification mandates the use of version 1.2.2. of the
    // XAdES QualifyingProperties. However MOA-SP supports only version 1.1.1. Therefore,
    // the version 1.1.1 is used in order to be compatible with current MOA-SP versions.
    
    List<org.etsi.uri._01903.v1_1.DataObjectFormatType> dataObjectFormats = new ArrayList<org.etsi.uri._01903.v1_1.DataObjectFormatType>();
    for (DataObject dataObject : dataObjects) {
      if (dataObject.getMimeType() != null && dataObject.getReference() != null) {
        Reference reference = dataObject.getReference();
        if (reference.getId() != null) {
          String objectReference = "#" + reference.getId();
          dataObjectFormats.add(factory.createDataObjectFormatType(
              objectReference, dataObject.getMimeType(), dataObject
                  .getDescription()));
        }
      }
    }
    
    String target = "#" + signatureId;
    
    DigestMethod dm;
    try {
      dm = ctx.getAlgorithmMethodFactory().createDigestMethod(ctx);
    } catch (NoSuchAlgorithmException e) {
      log.error("Failed to get DigestMethod algorithm.", e);
      throw new SLCommandException(4006);
    } catch (InvalidAlgorithmParameterException e) {
      log.error("Failed to get DigestMethod algorithm.", e);
      throw new SLCommandException(4006);
    }
    
    JAXBElement<org.etsi.uri._01903.v1_1.QualifyingPropertiesType> qualifyingProperties;
    try {
      qualifyingProperties = factory.createQualifyingProperties111(target, date, signingCertificates, idValue, dataObjectFormats, dm);
    } catch (QualifyingPropertiesException e) {
      log.error("Failed to create QualifyingProperties.", e);
      throw new SLCommandException(4000);
    }
    
    DocumentFragment fragment = ctx.getDocument().createDocumentFragment();
    
    try {
      factory.marshallQualifyingProperties(qualifyingProperties, fragment);
    } catch (JAXBException e) {
      log.error("Failed to marshal QualifyingProperties.", e);
      throw new SLCommandException(4000);
    }
    
    List<DOMStructure> content = Collections.singletonList(new DOMStructure(fragment.getFirstChild()));
    
    String objectIdValue = ctx.getIdValueFactory().createIdValue("Object");
    
    XMLObject object = ctx.getSignatureFactory().newXMLObject(content, objectIdValue, null, null);
    
    objects.add(object);

    // TODO: Report MOA-SP Bug
    //
    // Direct referencing of the SignedPorperties Id-attribute is not supported by MOA-SP
    // because the QualifyingProperties are parsed without the XAdES schema. Therefore,
    // the shorthand XPointer could not be resolved.
    //
    // The following workaround uses an XPointer to select the SignedProperties in order
    // to allow the signature to be verified with MOA-SP.
    
    String referenceURI = "#xmlns(xades=http://uri.etsi.org/01903/v1.1.1%23)%20xpointer(id('"
        + objectIdValue
        + "')/child::xades:QualifyingProperties/child::xades:SignedProperties)";
    
    String referenceIdValue = ctx.getIdValueFactory().createIdValue("Reference");
    String referenceType = QualifyingPropertiesFactory.SIGNED_PROPERTIES_REFERENCE_TYPE_V1_1_1;
    
    try {
      dm = ctx.getAlgorithmMethodFactory().createDigestMethod(ctx);
    } catch (NoSuchAlgorithmException e) {
      log.error("Failed to get DigestMethod algorithm.", e);
      throw new SLCommandException(4006);
    } catch (InvalidAlgorithmParameterException e) {
      log.error("Failed to get DigestMethod algorithm.", e);
      throw new SLCommandException(4006);
    }

    Reference reference = ctx.getSignatureFactory().newReference(referenceURI, dm, null, referenceType, referenceIdValue);
    
    references.add(reference);
    
    Node child = fragment.getFirstChild();
    if (child instanceof Element) {
      NodeList nodes = ((Element) child).getElementsByTagNameNS(QualifyingPropertiesFactory.NS_URI_V1_1_1, "SignedProperties");
      if (nodes.getLength() > 0) {
        IdAttribute idAttribute = new IdAttribute();
        idAttribute.element = (Element) nodes.item(0);
        idAttribute.namespaceURI = null;
        idAttribute.localName = "Id";
        idAttributes.add(idAttribute);
      }
    }
    
  }

  /**
   * Adds the XAdES 1.4 <code>QualifyingProperties</code> as an
   * <code>ds:Object</code> and a corresponding <code>ds:Reference</code> to
   * its <code>SignedProperties</code> element to this Signature.
   * 
   * @param objects
   *          the list of <code>ds:Objects</code> to add the created
   *          <code>ds:Object</code> to
   * @param references
   *          the list of <code>ds:References</code> to add the created
   *          <code>ds:Reference</code> to
   * @param signatureId TODO
   * @throws SLCommandException
   *           if creating and adding the XAdES
   *           <code>QualifyingProperties</code> fails
   * @throws NullPointerException
   *           if <code>objects</code> or <code>references</code> is
   *           <code>null</code>
   */
  private void addXAdES1_4ObjectAndReference(List<XMLObject> objects, List<Reference> references, String signatureId) throws SLCommandException {
    
    QualifyingProperties1_4Factory factory = QualifyingProperties1_4Factory.getInstance();
    
    String signedPropertiesIdValue = ctx.getIdValueFactory().createIdValue("SignedProperties");
    
    Date date = (signingTime != null) ? signingTime : new Date();
    
    List<X509Certificate> signingCertificates;
    if (signerCertificate != null) {
      signingCertificates = Collections.singletonList(signerCertificate);
    } else {
      signingCertificates = Collections.emptyList();
    }
    
    List<org.etsi.uri._01903.v1_3.DataObjectFormatType> dataObjectFormats = new ArrayList<org.etsi.uri._01903.v1_3.DataObjectFormatType>();
    for (DataObject dataObject : dataObjects) {
      if (dataObject.getMimeType() != null && dataObject.getReference() != null) {
        Reference reference = dataObject.getReference();
        if (reference.getId() != null) {
          String objectReference = "#" + reference.getId();
          dataObjectFormats.add(factory.createDataObjectFormatType(
              objectReference, dataObject.getMimeType(), dataObject
                  .getDescription()));
        }
      }
    }
    
    String target = "#" + signatureId;
    
    DigestMethod dm;
    try {
      dm = ctx.getAlgorithmMethodFactory().createDigestMethod(ctx);
    } catch (NoSuchAlgorithmException e) {
      log.error("Failed to get DigestMethod algorithm.", e);
      throw new SLCommandException(4006);
    } catch (InvalidAlgorithmParameterException e) {
      log.error("Failed to get DigestMethod algorithm.", e);
      throw new SLCommandException(4006);
    }
    
    JAXBElement<org.etsi.uri._01903.v1_3.QualifyingPropertiesType> qualifyingProperties;
    try {
      qualifyingProperties = factory.createQualifyingProperties141(target, date, signingCertificates, signedPropertiesIdValue, dataObjectFormats, dm);
    } catch (QualifyingPropertiesException e) {
      log.error("Failed to create QualifyingProperties.", e);
      throw new SLCommandException(4000);
    }
    
    DocumentFragment fragment = ctx.getDocument().createDocumentFragment();
    
    try {
      factory.marshallQualifyingProperties(qualifyingProperties, fragment);
    } catch (JAXBException e) {
      log.error("Failed to marshal QualifyingProperties.", e);
      throw new SLCommandException(4000);
    }
    
    List<DOMStructure> content = Collections.singletonList(new DOMStructure(fragment.getFirstChild()));
    
    String objectIdValue = ctx.getIdValueFactory().createIdValue("Object");
    
    XMLObject object = ctx.getSignatureFactory().newXMLObject(content, objectIdValue, null, null);
    
    objects.add(object);

    String referenceURI = "#" + signedPropertiesIdValue;
    
    String referenceIdValue = ctx.getIdValueFactory().createIdValue("Reference");
    String referenceType = QualifyingProperties1_4Factory.SIGNED_PROPERTIES_REFERENCE_TYPE;
    
    try {
      dm = ctx.getAlgorithmMethodFactory().createDigestMethod(ctx);
    } catch (NoSuchAlgorithmException e) {
      log.error("Failed to get DigestMethod algorithm.", e);
      throw new SLCommandException(4006);
    } catch (InvalidAlgorithmParameterException e) {
      log.error("Failed to get DigestMethod algorithm.", e);
      throw new SLCommandException(4006);
    }

    Reference reference = ctx.getSignatureFactory().newReference(referenceURI, dm, null, referenceType, referenceIdValue);
    
    references.add(reference);
    
    Node child = fragment.getFirstChild();
    if (child instanceof Element) {
      NodeList nodes = ((Element) child).getElementsByTagNameNS(QualifyingProperties1_4Factory.NS_URI_V1_3_2, "SignedProperties");
      if (nodes.getLength() > 0) {
        IdAttribute idAttribute = new IdAttribute();
        idAttribute.element = (Element) nodes.item(0);
        idAttribute.namespaceURI = null;
        idAttribute.localName = "Id";
        idAttributes.add(idAttribute);
      }
    }
    
  }

  /**
   * Parse the SignatureEnvironment.
   * 
   * @param signatureEnvironment
   *          the <code>SignatureEnvironment</code> element
   * @param supplements
   *          an optional list of <code>Supplements</code> (may be
   *          <code>null</code>)
   * 
   * @return the parsed SignatureEnvironment document
   * 
   * @throws SLCommandException
   *           if parsing the SignatureEnvironment fails
   * @throws NullPointerException
   *           if <code>signatureEnvironment</code> is <code>null</code>
   */
  private Document parseSignatureEnvironment(
      Base64XMLOptRefContentType signatureEnvironment,
      List<DataObjectAssociationType> supplements) throws SLCommandException {

    if (signatureEnvironment == null) {
      throw new NullPointerException("Argument 'signatureEnvironment' must not be null.");
    }
    
    LSInput input;
    try {
      if (signatureEnvironment.getReference() != null) {
        log.debug("SignatureEnvironment contains Reference '{}'.", signatureEnvironment.getReference());
        input = createLSInput(signatureEnvironment.getReference());
      } else if (signatureEnvironment.getBase64Content() != null) {
        log.debug("SignatureEnvironment contains Base64Content.");
        input = createLSInput(signatureEnvironment.getBase64Content());
      } else if (signatureEnvironment.getXMLContent() != null) {
        log.debug("SignatureEnvironment contains XMLContent.");
        input = createLSInput((XMLContentType) signatureEnvironment.getXMLContent());
      } else {
        // the schema does not allow us to reach this point
        throw new SLCommandException(4000);
      }
    } catch (IOException e) {
      log.info("XML document in which the signature is to be integrated cannot be resolved.", e);
      throw new SLCommandException(4100);
    } catch (XMLStreamException e) {
      log.info("XML document in which the signature is to be integrated cannot be resolved.", e);
      throw new SLCommandException(4100);
    }
    
    LSParser parser = domImplLS.createLSParser(DOMImplementationLS.MODE_SYNCHRONOUS, null);
    DOMConfiguration domConfig = parser.getDomConfig();
    SimpleDOMErrorHandler errorHandler = new SimpleDOMErrorHandler();
    domConfig.setParameter("error-handler", errorHandler);
    LSResourceResolverAdapter resourceResolver = new LSResourceResolverAdapter(supplements);
    domConfig.setParameter("resource-resolver", resourceResolver);
    domConfig.setParameter("validate", Boolean.TRUE);

    Document doc;
    try {
      doc = parser.parse(input);
    } catch (DOMException e) {
      log.info("XML document in which the signature is to be integrated cannot be parsed.", e);
      throw new SLCommandException(4101);
    } catch (LSException e) {
      log.info("XML document in which the signature is to be integrated cannot be parsed.", e);
      throw new SLCommandException(4101);
    }
    
    if (resourceResolver.getError() != null) {
      log.info("Failed to resolve resource while parsing SignatureEnvironment document.", resourceResolver.getError());
      // we don't stop here, as we only _try_ to parse validating
    }
    
    if (errorHandler.hasFatalErrors()) {
      // log fatal errors
      if (log.isInfoEnabled()) {
        List<String> errorMessages = errorHandler.getErrorMessages();
        StringBuffer sb = new StringBuffer();
        sb.append("XML document in which the signature is to be integrated cannot be parsed.");
        for (String errorMessage : errorMessages) {
          sb.append(" ");
          sb.append(errorMessage);
        }
        log.info(sb.toString());
      }
      throw new SLCommandException(4101);
    }

    // log parsed document
    if (log.isTraceEnabled()) {
      
      StringWriter writer = new StringWriter();
      
      writer.write("SignatureEnvironment:\n");
      
      LSOutput output = domImplLS.createLSOutput();
      output.setCharacterStream(writer);
      output.setEncoding("UTF-8");
      LSSerializer serializer = domImplLS.createLSSerializer();
      serializer.write(doc, output);
      
      log.trace(writer.toString());
    }

    return doc;
    
  }

  /**
   * Creates an LSInput from the given <code>reference</code> URI.
   * 
   * @param reference
   *          the reference URL
   * 
   * @return an LSInput from the given <code>reference</code> URI
   * 
   * @throws IOException
   *           if dereferencing the given <code>reference</code> fails
   */
  private LSInput createLSInput(String reference) throws IOException {
    
    URLDereferencer urlDereferencer = ctx.getUrlDereferencer();
    StreamData streamData = urlDereferencer.dereference(reference);

    String contentType = streamData.getContentType();
    String charset = HttpUtil.getCharset(contentType, true);
    InputStreamReader streamReader;
    try {
      streamReader = new InputStreamReader(streamData.getStream(), charset);
    } catch (UnsupportedEncodingException e) {
      log.info("Charset {} not supported. Using default.", charset);
      streamReader = new InputStreamReader(streamData.getStream());
    }

    LSInput input = domImplLS.createLSInput();
    input = domImplLS.createLSInput();
    input.setCharacterStream(streamReader);
    
    return input;
    
  }

  /**
   * Creates an LSInput from the given <code>content</code> bytes.
   * 
   * @param content
   *          the content bytes
   * 
   * @return an LSInput from the givne <code>content</code> bytes
   */
  private LSInput createLSInput(byte[] content) {
    
    ByteArrayInputStream inputStream = new ByteArrayInputStream(content);
    LSInput input = domImplLS.createLSInput();
    input.setByteStream(inputStream);

    return input;
    
  }

  /**
   * Creates an LSInput from the given XML <code>content</code>.
   * 
   * @param content
   *          the XML content
   * @return an LSInput from the given XML <code>content</code>
   * 
   * @throws XMLStreamException
   *           if reading the XMLStream from the given XML content fails
   */
  private LSInput createLSInput(XMLContentType content) throws XMLStreamException {

    ByteArrayOutputStream redirectedStream = content.getRedirectedStream();
    if (redirectedStream != null) {
      LSInput input = domImplLS.createLSInput();
      input.setByteStream(new ByteArrayInputStream(redirectedStream.toByteArray()));
      return input;
    } else {
      return null;
    }
    
  }
  
  /**
   * Represents an <code>xsd:Id</code>-attribute value.
   * 
   * @author mcentner
   */
  private class IdAttribute {
    
    private Element element;
    
    private String namespaceURI;
    
    private String localName;
    
  }
  
  /**
   * An implementation of the LSResourceResolver that uses a list of supplements
   * to resolve resources.
   * 
   * @author mcentner
   */
  private class LSResourceResolverAdapter implements LSResourceResolver {
    
    List<DataObjectAssociationType> supplements;
    
    private LSResourceResolverAdapter(
        List<DataObjectAssociationType> supplements) {
      this.supplements = supplements;
    }
  
    private Exception error;
    
    /**
     * @return the error
     */
    public Exception getError() {
      return error;
    }
  
    @Override
    public LSInput resolveResource(String type, String namespaceURI,
        String publicId, String systemId, String baseURI) {
  
      if (log.isTraceEnabled()) {
        log.trace("Resolve resource :" +
            "\n  type=" + type +
            "\n  namespaceURI=" + namespaceURI +
            "\n  publicId=" + publicId +
            "\n  systemId=" + systemId +
            "\n  baseURI=" + baseURI);
      }
  
      if (systemId != null) {

        log.debug("Resolve resource '{}'.", systemId);
        
        for (DataObjectAssociationType supplement : supplements) {
          
          Base64XMLLocRefReqRefContentType content = supplement.getContent();
          if (content != null) {
  
            String reference = content.getReference();
            if (systemId.equals(reference)) {
              
              try {
                if (content.getLocRefContent() != null) {
                  log.trace("Resolved resource '{}' to supplement with LocRefContent.", reference);
                  return createLSInput(content.getLocRefContent());
                } else if (content.getBase64Content() != null) {
                  log.trace("Resolved resource '{}' to supplement with Base64Content.", reference);
                  return createLSInput(content.getBase64Content());
                } else if (content.getXMLContent() != null) {
                  log.trace("Resolved resource '{}' to supplement with XMLContent.", reference);
                  return createLSInput((XMLContentType) content.getXMLContent());
                } else {
                  return null;
                }
              } catch (IOException e) {
                log.info("Failed to resolve resource '{}' to supplement.", systemId, e);
                error = e;
                return null;
              } catch (XMLStreamException e) {
                log.info("Failed to resolve resource '{}' to supplement.", systemId, e);
                error = e;
                return null;
              }
              
            }
  
          }
          
        }

        log.info("Failed to resolve resource '{}' to supplement. No such supplement.", systemId);
        
      }
  
      return null;
      
    }
    
    
  }

}
