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


package at.gv.egiz.idlink;

import iaik.xml.crypto.XmldsigMore;

import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Manifest;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLObject;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.crypto.dsig.spec.XPathFilterParameterSpec;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import oasis.names.tc.saml._1_0.assertion.AnyType;
import oasis.names.tc.saml._1_0.assertion.AssertionType;
import oasis.names.tc.saml._1_0.assertion.AttributeStatementType;
import oasis.names.tc.saml._1_0.assertion.AttributeType;
import oasis.names.tc.saml._1_0.assertion.SubjectConfirmationType;
import oasis.names.tc.saml._1_0.assertion.SubjectType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import at.gv.e_government.reference.namespace.persondata._20020228_.AbstractPersonType;
import at.gv.e_government.reference.namespace.persondata._20020228_.IdentificationType;
import at.gv.e_government.reference.namespace.persondata._20020228_.PersonNameType;
import at.gv.e_government.reference.namespace.persondata._20020228_.PhysicalPersonType;
import at.gv.e_government.reference.namespace.persondata._20020228_.IdentificationType.Value;
import at.gv.e_government.reference.namespace.persondata._20020228_.PersonNameType.FamilyName;
import at.gv.egiz.marshal.MarshallerFactory;
import at.gv.egiz.xmldsig.KeyTypeNotSupportedException;
import at.gv.egiz.xmldsig.KeyValueFactory;

public class IdentityLinkFactory {
  
  private final Logger log = LoggerFactory.getLogger(IdentityLinkFactory.class);
  
  /**
   * The instance returned by {@link #getInstance()}.
   */
  private static IdentityLinkFactory instance;
  
  /**
   * The <code>JAXBContext</code>.
   */
  private static JAXBContext jaxbContext;
  
  /**
   * The <code>KeyValueFactory</code>.
   */
  private static KeyValueFactory keyValueFactory;
  
  /**
   * Get an instance of this <code>CompressedIdentityLinkFactory</code>.
   * 
   * @return an instance of this <code>CompressedIdentityLinkFactory</code>
   */
  public synchronized static IdentityLinkFactory getInstance() {
    if (instance == null) {
      instance = new IdentityLinkFactory();
    }
    return instance;
  }

  /**
   * Private constructor.
   */
  private IdentityLinkFactory() {
    
    keyValueFactory = new KeyValueFactory();

    StringBuffer packageNames = new StringBuffer();
    packageNames.append(at.gv.e_government.reference.namespace.persondata._20020228_.ObjectFactory.class.getPackage().getName());
    packageNames.append(":");
    packageNames.append(org.w3._2000._09.xmldsig_.ObjectFactory.class.getPackage().getName());
    packageNames.append(":");
    packageNames.append(org.w3._2001._04.xmldsig_more_.ObjectFactory.class.getPackage().getName());
    packageNames.append(":");
    packageNames.append(at.buergerkarte.namespaces.personenbindung._20020506_.ObjectFactory.class.getPackage().getName());
    packageNames.append(":");
    packageNames.append(oasis.names.tc.saml._1_0.assertion.ObjectFactory.class.getPackage().getName());

    try {
      jaxbContext = JAXBContext.newInstance(packageNames.toString());
    } catch (JAXBException e) {
      // we should not get an JAXBException initializing the JAXBContext
      throw new RuntimeException(e);
    }
  
  }
  
  public JAXBElement<AssertionType> createAssertion(String assertionId,
      Date issueInstant, String issuer, long majorVersion, long minorVersion, AttributeStatementType attributeStatement) {

    oasis.names.tc.saml._1_0.assertion.ObjectFactory asFactory = 
      new oasis.names.tc.saml._1_0.assertion.ObjectFactory();

    AssertionType assertionType = asFactory.createAssertionType();
    
    assertionType.setAssertionID(assertionId);
    
    GregorianCalendar gregorianCalendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
    gregorianCalendar.setTime(issueInstant);
    try {
      DatatypeFactory datatypeFactory = DatatypeFactory.newInstance();
      assertionType.setIssueInstant(datatypeFactory.newXMLGregorianCalendar(gregorianCalendar));
    } catch (DatatypeConfigurationException e) {
      throw new RuntimeException(e);
    }

    assertionType.setIssuer(issuer);
    
    assertionType.setMajorVersion(BigInteger.valueOf(majorVersion));
    assertionType.setMinorVersion(BigInteger.valueOf(minorVersion));
    
    assertionType.getStatementOrSubjectStatementOrAuthenticationStatement().add(attributeStatement);
    
    return asFactory.createAssertion(assertionType);
    
  }
  
  public AttributeStatementType createAttributeStatement(String idValue, String idType,
      String givenName, String familyName, String dateOfBirth,
      PublicKey[] publicKeys) throws KeyTypeNotSupportedException {
    
    oasis.names.tc.saml._1_0.assertion.ObjectFactory asFactory = 
      new oasis.names.tc.saml._1_0.assertion.ObjectFactory();
    
    at.gv.e_government.reference.namespace.persondata._20020228_.ObjectFactory prFactory = 
      new at.gv.e_government.reference.namespace.persondata._20020228_.ObjectFactory();
    
    AttributeStatementType attributeStatementType = asFactory.createAttributeStatementType();

    // saml:Subject
    SubjectConfirmationType subjectConfirmationType = asFactory.createSubjectConfirmationType();
    
    subjectConfirmationType.getConfirmationMethod().add("urn:oasis:names:tc:SAML:1.0:cm:sender-vouches");

    // pr:Person
    Value identificationTypeValue = prFactory.createIdentificationTypeValue();
    identificationTypeValue.setValue(idValue);
    IdentificationType identificationType = prFactory
        .createIdentificationType();
    identificationType.setValue(identificationTypeValue);
    identificationType.setType(idType);

    PersonNameType personNameType = prFactory.createPersonNameType();
    FamilyName personNameTypeFamilyName = prFactory
        .createPersonNameTypeFamilyName();
    personNameTypeFamilyName.setValue(familyName);
    personNameTypeFamilyName.setPrimary("undefined");
    personNameType.getFamilyName().add(personNameTypeFamilyName);
    personNameType.getGivenName().add(givenName);

    PhysicalPersonType physicalPersonType = prFactory
        .createPhysicalPersonType();
    physicalPersonType.getIdentification().add(identificationType);
    physicalPersonType.setName(personNameType);
    physicalPersonType.setDateOfBirth(dateOfBirth);
    JAXBElement<AbstractPersonType> physicalPerson = prFactory.createPerson(physicalPersonType);

    AnyType personType = asFactory.createAnyType();
    personType.getContent().add(physicalPerson);
    subjectConfirmationType.setSubjectConfirmationData(personType);
    
    JAXBElement<SubjectConfirmationType> subjectConfirmation = asFactory.createSubjectConfirmation(subjectConfirmationType);
    
    SubjectType subjectType = asFactory.createSubjectType();
    subjectType.getContent().add(subjectConfirmation);
    
    attributeStatementType.setSubject(subjectType);
    
    // saml:Attribute CitizenPublicKey
    for (int i = 0; i < publicKeys.length; i++) {
      
      JAXBElement<?> createKeyValue = keyValueFactory.createKeyValue(publicKeys[i]);
      AttributeType attributeType = asFactory.createAttributeType();
      attributeType.setAttributeName("CitizenPublicKey");
      attributeType.setAttributeNamespace("urn:publicid:gv.at:namespaces:identitylink:1.2");
      AnyType attributeValueType = asFactory.createAnyType();
      attributeValueType.getContent().add(createKeyValue);
      attributeType.getAttributeValue().add(attributeValueType);
      
      attributeStatementType.getAttribute().add(attributeType);
      
    }
    
    return attributeStatementType;
  }
  
  /**
   * Marshall the given <code>compressedIdentityLink</code> into a DOM document
   * with the given Nodes as <code>parent</code> and <code>nextSibling</code>
   * nodes.
   * 
   * @param identityLink
   *          the <code>CompressedIdentityLink</code> element
   * @param parent
   *          the parent node
   * @param nextSibling
   *          the next sibling node (may be <code>null</code>)
   * @param applyWorkarounds
   *          apply workarounds as spefiyed by
   *          {@link #applyWorkarounds(Element, int)}
   * 
   * @throws JAXBException
   *           if an unexpected error occurs while marshalling
   * @throws NullPointerException
   *           if <code>compressdIdentityLink</code> or <code>parent</code> is
   *           <code>null</code>
   */
  public void marshallIdentityLink(
      JAXBElement<AssertionType> identityLink,
      Node parent, Node nextSibling) throws JAXBException {
    
    DOMResult result = new DOMResult(parent, nextSibling);

    try {
      Marshaller marshaller = MarshallerFactory.createMarshaller(jaxbContext, true);

      marshaller.marshal(identityLink, result);
    } catch (PropertyException e) {
      throw new RuntimeException(e);
    } 
  
  }
  
  public void signIdentityLink(Element assertion, X509Certificate certificate,
      PrivateKey key) throws NoSuchAlgorithmException,
      InvalidAlgorithmParameterException, XMLSignatureException,
      MarshalException {

    signIdentityLink(assertion, certificate, key,
        XMLSignatureFactory.getInstance(), KeyInfoFactory.getInstance());
  }

  public void signIdentityLink(Element assertion, X509Certificate certificate,
      PrivateKey key, XMLSignatureFactory signatureFactory,
      KeyInfoFactory keyInfoFactory) throws NoSuchAlgorithmException,
      InvalidAlgorithmParameterException, XMLSignatureException,
      MarshalException {

    List<Reference> references = new ArrayList<Reference>();
    
    // Reference #1

    Map<String, String> prefixMap = new HashMap<String, String>();
    prefixMap.put("pr", "http://reference.e-government.gv.at/namespace/persondata/20020228#");
    List<Transform> transforms1 = new ArrayList<Transform>();
    transforms1.add(signatureFactory.newTransform(Transform.XPATH,
        new XPathFilterParameterSpec(
            "not(ancestor-or-self::pr:Identification)", prefixMap)));
    transforms1.add(signatureFactory.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null));
    DigestMethod digestMethod1 = signatureFactory.newDigestMethod(DigestMethod.SHA1, null);
    references.add(signatureFactory.newReference("", digestMethod1, transforms1, null, null));
    
    // Reference (Manifest)
    
    DigestMethod digestMethod2 = signatureFactory.newDigestMethod(DigestMethod.SHA1, null);
    references.add(signatureFactory.newReference("#manifest", digestMethod2, null, Manifest.TYPE, null));
    
    CanonicalizationMethod canonicalizationMethod = signatureFactory
        .newCanonicalizationMethod(CanonicalizationMethod.EXCLUSIVE,
            (C14NMethodParameterSpec) null);

    SignatureMethod signatureMethod;
    String algorithm = key.getAlgorithm();
    if ("RSA".equalsIgnoreCase(algorithm)) {
      signatureMethod = signatureFactory.newSignatureMethod(SignatureMethod.RSA_SHA1, null);
    } else if ("ECDSA".equalsIgnoreCase(algorithm) || "EC".equalsIgnoreCase(algorithm)) {
      signatureMethod = signatureFactory.newSignatureMethod(XmldsigMore.SIGNATURE_ECDSA_SHA1, null);
    } else if ("DSA".equalsIgnoreCase(algorithm)) {
      signatureMethod = signatureFactory.newSignatureMethod(SignatureMethod.DSA_SHA1, null);
    } else {
      throw new NoSuchAlgorithmException("Algorithm '" + algorithm + "' not supported.");
    }
    
    SignedInfo signedInfo = signatureFactory.newSignedInfo(canonicalizationMethod, signatureMethod, references);
    
    
    X509Data x509Data = keyInfoFactory.newX509Data(Collections.singletonList(certificate));
    KeyInfo keyInfo = keyInfoFactory.newKeyInfo(Collections.singletonList(x509Data));
    
    // Manifest
    
    Map<String, String> manifestPrefixMap = new HashMap<String, String>();
    manifestPrefixMap.put("dsig", XMLSignature.XMLNS);
    List<Transform> manifestTransforms = Collections
        .singletonList(signatureFactory.newTransform(Transform.XPATH,
            new XPathFilterParameterSpec(
                "not(ancestor-or-self::dsig:Signature)", manifestPrefixMap)));
    Reference manifestReference = signatureFactory.newReference("",
        signatureFactory.newDigestMethod(DigestMethod.SHA1, null),
        manifestTransforms, null, null);

    Manifest manifest = signatureFactory.newManifest(Collections
        .singletonList(manifestReference), "manifest");

    XMLObject xmlObject = signatureFactory.newXMLObject(Collections
        .singletonList(manifest), null, null, null);

    XMLSignature xmlSignature = signatureFactory.newXMLSignature(signedInfo,
        keyInfo, Collections.singletonList(xmlObject), null, null);

    DOMSignContext signContext = new DOMSignContext(key, assertion);
    signContext.putNamespacePrefix(XMLSignature.XMLNS, "dsig");
    
    if (log.isTraceEnabled()) {
      signContext.setProperty("javax.xml.crypto.dsig.cacheReference", Boolean.TRUE);
    }

    xmlSignature.sign(signContext);
    
    if (log.isDebugEnabled()) {
      
      try {
        
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        
        StringWriter writer = new StringWriter();
        
        transformer.transform(new DOMSource(assertion), new StreamResult(writer));

        log.debug(writer.toString());
        
      } catch (Exception e) {
        log.debug("Logging assertion failed.", e);
      }
      
    }
    
    if (log.isTraceEnabled()) {

      StringBuilder sb = new StringBuilder();

      sb.append("Digest input data:\n\n");

      try {

        Iterator<Reference> refs = references.iterator();
        for (int i = 0; refs.hasNext(); i++) {
          Reference reference = (Reference) refs.next();
          sb.append("Reference " + i + "\n");

          Reader reader = new InputStreamReader(reference
              .getDigestInputStream(), Charset.forName("UTF-8"));
          char c[] = new char[512];
          for (int l; (l = reader.read(c)) != -1;) {
            sb.append(c, 0, l);
          }
          sb.append("\n");
        }

        sb.append("Manifest Reference\n");
        
        Reader reader = new InputStreamReader(manifestReference
            .getDigestInputStream(), Charset.forName("UTF-8"));
        char c[] = new char[512];
        for (int l; (l = reader.read(c)) != -1;) {
          sb.append(c, 0, l);
        }

      } catch (Exception e) {
        sb.append(e.getMessage());
      }

      log.trace(sb.toString());
    }
    
  }
  
}
