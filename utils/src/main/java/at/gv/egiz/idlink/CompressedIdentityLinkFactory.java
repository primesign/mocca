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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMResult;

import org.w3._2000._09.xmldsig_.KeyValueType;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import at.buergerkarte.namespaces.personenbindung._20020506_.CompressedIdentityLinkType;
import at.gv.e_government.reference.namespace.persondata._20020228_.AbstractPersonType;
import at.gv.e_government.reference.namespace.persondata._20020228_.IdentificationType;
import at.gv.e_government.reference.namespace.persondata._20020228_.PersonNameType;
import at.gv.e_government.reference.namespace.persondata._20020228_.PhysicalPersonType;
import at.gv.e_government.reference.namespace.persondata._20020228_.IdentificationType.Value;
import at.gv.e_government.reference.namespace.persondata._20020228_.PersonNameType.FamilyName;
import at.gv.egiz.idlink.ans1.CitizenPublicKey;
import at.gv.egiz.idlink.ans1.IdentityLink;
import at.gv.egiz.idlink.ans1.PersonData;
import at.gv.egiz.idlink.ans1.PhysicalPersonData;
import at.gv.egiz.marshal.MarshallerFactory;
import at.gv.egiz.xmldsig.KeyTypeNotSupportedException;
import at.gv.egiz.xmldsig.KeyValueFactory;

public class CompressedIdentityLinkFactory {
  
  /**
   * The instance returned by {@link #getInstance()}.
   */
  private static CompressedIdentityLinkFactory instance;
  
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
  public synchronized static CompressedIdentityLinkFactory getInstance() {
    if (instance == null) {
      instance = new CompressedIdentityLinkFactory();
    }
    return instance;
  }

  /**
   * Private constructor.
   */
  private CompressedIdentityLinkFactory() {
    
    keyValueFactory = new KeyValueFactory();

    StringBuffer packageNames = new StringBuffer();
    packageNames.append(at.gv.e_government.reference.namespace.persondata._20020228_.ObjectFactory.class.getPackage().getName());
    packageNames.append(":");
    packageNames.append(org.w3._2000._09.xmldsig_.ObjectFactory.class.getPackage().getName());
    packageNames.append(":");
    packageNames.append(org.w3._2001._04.xmldsig_more_.ObjectFactory.class.getPackage().getName());
    packageNames.append(":");
    packageNames.append(at.buergerkarte.namespaces.personenbindung._20020506_.ObjectFactory.class.getPackage().getName());

    try {
      jaxbContext = JAXBContext.newInstance(packageNames.toString());
    } catch (JAXBException e) {
      // we should not get an JAXBException initializing the JAXBContext
      throw new RuntimeException(e);
    }
  
  }
  
  public IdentityLink createIdLink(CompressedIdentityLinkType compressedIdentityLinkType) {
    
    // IssuerTemplate
    String issuerTemplate = compressedIdentityLinkType.getIssuerTemplate();
    
    // AssertionId
    String assertionID = compressedIdentityLinkType.getAssertionID();
    
    // IssueInstant
    String issueInstant = compressedIdentityLinkType.getIssueInstant();
    
    AbstractPersonType personDataType = compressedIdentityLinkType.getPersonData();

    String baseId = null;

    List<IdentificationType> identifications = personDataType.getIdentification();
    for (IdentificationType identificationType : identifications) {
      String type = identificationType.getType();
      if ("urn:publicid:gv.at:baseid".equals(type)) {
        baseId = identificationType.getValue().getValue();
      }
    }

    String givenName = null;
    String familyName = null;
    String dateOfBirth = null;
    
    if (personDataType instanceof PhysicalPersonType) {
      PhysicalPersonType physicalPersonType = (PhysicalPersonType) personDataType;
      PersonNameType name = physicalPersonType.getName();
      List<String> givenNames = name.getGivenName();
      if (!givenNames.isEmpty()) {
        givenName = givenNames.get(0);
      }
      List<FamilyName> familyNames = name.getFamilyName();
      if (!familyNames.isEmpty()) {
        familyName = familyNames.get(0).getValue();
      }
      dateOfBirth = physicalPersonType.getDateOfBirth();
    }
    
    PhysicalPersonData physicalPersonData = new PhysicalPersonData(baseId, givenName, familyName, dateOfBirth);
    PersonData personData = new PersonData(physicalPersonData);

    int numKeys = compressedIdentityLinkType.getCitizenPublicKey().size();
    CitizenPublicKey[] citizenPublicKeys = new CitizenPublicKey[numKeys];
    for (int i = 0; i < numKeys;) {
      citizenPublicKeys[i] = new CitizenPublicKey(++i); 
    }
    
    byte[] signatureValue = compressedIdentityLinkType.getSignatureValue();
    byte[] referenceDigest = compressedIdentityLinkType.getReferenceDigest();
    byte[] referenceManifestDigest = compressedIdentityLinkType.getReferenceManifestDigest();
    byte[] manifestReferenceDigest = compressedIdentityLinkType.getManifestReferenceDigest();
    
    IdentityLink idLink = new IdentityLink(issuerTemplate, assertionID, issueInstant, personData, citizenPublicKeys, signatureValue);
    idLink.setReferenceDigest(referenceDigest);
    idLink.setReferenceManifestDigest(referenceManifestDigest);
    idLink.setManifestReferenceDigest(manifestReferenceDigest);
    
    return idLink;
    
  }
  
  /**
   * Creates a new <code>CompressedIdentityLink</code> element from the given
   * ASN.1 representation of an <code>idLink</code>.
   * 
   * @param idLink
   *          the ASN.1 representation of an <code>IdentityLink</code>
   * @param certificates
   *          a list of {@link X509Certificate}s containing the corresponding
   *          public keys
   * @param domainId TODO
   * @return a new <code>CompressedIdentityLink</code> element
   * 
   * @throws NullPointerException
   *           if <code>idLink</code> or <code>certificates</code> is
   *           <code>null</code>
   * @throws IllegalArgumentException
   *           if <code>idLink</code> references certificates not in the range
   *           of the <code>certificates</code> list
   */
  public JAXBElement<CompressedIdentityLinkType> createCompressedIdentityLink(
      at.gv.egiz.idlink.ans1.IdentityLink idLink,
      List<X509Certificate> certificates, String domainId) {

    at.gv.e_government.reference.namespace.persondata._20020228_.ObjectFactory prFactory = 
      new at.gv.e_government.reference.namespace.persondata._20020228_.ObjectFactory();

    at.buergerkarte.namespaces.personenbindung._20020506_.ObjectFactory pbFactory = 
      new at.buergerkarte.namespaces.personenbindung._20020506_.ObjectFactory();

    org.w3._2000._09.xmldsig_.ObjectFactory dsFactory = new org.w3._2000._09.xmldsig_.ObjectFactory();

    // PersonData
    PhysicalPersonData __physicalPersonData = idLink.getPersonData()
        .getPhysicalPerson();

    Value identificationTypeValue = prFactory.createIdentificationTypeValue();
    identificationTypeValue.setValue(__physicalPersonData.getBaseId());
    IdentificationType identificationType = prFactory
        .createIdentificationType();
    identificationType.setValue(identificationTypeValue);
    if (domainId != null) {
      identificationType.setType(domainId);
    } else {
      identificationType.setType("urn:publicid:gv.at:baseid");
    }

    PersonNameType personNameType = prFactory.createPersonNameType();
    FamilyName personNameTypeFamilyName = prFactory
        .createPersonNameTypeFamilyName();
    personNameTypeFamilyName.setValue(__physicalPersonData.getFamilyName());
    personNameType.getFamilyName().add(personNameTypeFamilyName);
    personNameType.getGivenName().add(__physicalPersonData.getGivenName());

    PhysicalPersonType physicalPersonType = prFactory
        .createPhysicalPersonType();
    physicalPersonType.getIdentification().add(identificationType);
    physicalPersonType.setName(personNameType);
    physicalPersonType.setDateOfBirth(__physicalPersonData.getDateOfBirth());

    // CompressedIdentityLink
    CompressedIdentityLinkType compressedIdentityLinkType = pbFactory
        .createCompressedIdentityLinkType();
    compressedIdentityLinkType.setIssuerTemplate(idLink.getIssuerTemplate());
    compressedIdentityLinkType.setAssertionID(idLink.getAssertionID());
    compressedIdentityLinkType.setIssueInstant(idLink.getIssueInstant());
    compressedIdentityLinkType.setPersonData(physicalPersonType);

    // CitizenPublicKey
    CitizenPublicKey[] __citizenPublicKeys = idLink.getCitizenPublicKeys();
    for (CitizenPublicKey __citizenPublicKey : __citizenPublicKeys) {

      X509Certificate certificate = certificates.get(__citizenPublicKey.getOnToken());
      PublicKey publicKey = certificate.getPublicKey();
      
      JAXBElement<?> keyValue;
      try {
        keyValue = keyValueFactory.createKeyValue(publicKey);
      } catch (KeyTypeNotSupportedException e) {
        // TODO: handle exception properly
        throw new RuntimeException(e);
      }

      KeyValueType keyValueType = dsFactory.createKeyValueType();
      keyValueType.getContent().add(keyValue);
      
      compressedIdentityLinkType.getCitizenPublicKey().add(keyValueType);
    }

    compressedIdentityLinkType.setSignatureValue(idLink.getSignatureValue());
    compressedIdentityLinkType.setReferenceDigest(idLink.getReferenceDigest());
    compressedIdentityLinkType.setReferenceManifestDigest(idLink
        .getReferenceManifestDigest());
    compressedIdentityLinkType.setManifestReferenceDigest(idLink
        .getManifestReferenceDigest());
    JAXBElement<CompressedIdentityLinkType> compressedIdentityLink = pbFactory
        .createCompressedIdentityLink(compressedIdentityLinkType);

    return compressedIdentityLink;

  }

  /**
   * Marshall the given <code>compressedIdentityLink</code> into a DOM document
   * with the given Nodes as <code>parent</code> and <code>nextSibling</code>
   * nodes.
   * 
   * @param compressedIdentityLink
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
  public void marshallCompressedIdentityLink(
      JAXBElement<CompressedIdentityLinkType> compressedIdentityLink,
      Node parent, Node nextSibling, boolean applyWorkarounds) throws JAXBException {
    
    DOMResult result = new DOMResult(parent, nextSibling);
    

    try {
      Marshaller marshaller = MarshallerFactory.createMarshaller(jaxbContext);

      marshaller.marshal(compressedIdentityLink, result);
    } catch (PropertyException e) {
      throw new RuntimeException(e);
    } 
  
    if (applyWorkarounds) {
      Element element = (Element) ((nextSibling != null) 
          ? nextSibling.getPreviousSibling() 
          : parent.getFirstChild());
      applyWorkarounds(element, 76);
    }
    
  }
  
  @SuppressWarnings("unchecked")
  public CompressedIdentityLinkType unmarshallCompressedIdentityLink(Source source) throws JAXBException {
    
    Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
    
    return ((JAXBElement<CompressedIdentityLinkType>) unmarshaller.unmarshal(source)).getValue();
    
  }
  
  /**
   * Apply some workarounds to the given CompressedIdentityLink
   * <code>element</code> to achieve compatibility with IdentityLink
   * transformation stylesheets that have been designed for a (buggy) form of
   * the CompressedIdentityLink as produced by a well-known citizen card
   * environment implementation.
   * 
   * <ol>
   * <li>Replace the attribute node <code>URN</code> of the
   * <code>NamedCurve</code> element of an <code>ECDSAKeyValue</code> element by
   * a child text-node with the same content.</li>
   * <li>Replace the attribute nodes <code>Value</code> of the <code>X</code>
   * and <code>Y</code> elements of an <code>ECDSAKeyValue</code> element by a
   * child text-node with the same content.</li>
   * <li>Insert &quot;\n&quot; at <code>base64LineLength</code> into the Base64
   * content of the <code>Modulus</code> element of an <code>RSAKeyValue</code>
   * element.
   * </ol>
   * 
   * @param element
   *          the <code>CompressedIdentityLink</code> element
   * @param base64LineLength
   *          the line length of Base64 content
   */
  public void applyWorkarounds(Element element, int base64LineLength) {
    
    Document document = element.getOwnerDocument();
    
    NodeList nodeList = element.getElementsByTagNameNS(
        "http://www.w3.org/2001/04/xmldsig-more#", "NamedCurve");
    for (int i = 0; i < nodeList.getLength(); i++) {
      Node ecdsaNameCurve = nodeList.item(i);
      Attr attrNode = ((Element) ecdsaNameCurve).getAttributeNodeNS(null,
          "URN");
      ecdsaNameCurve
          .appendChild(document.createTextNode(attrNode.getValue()));
      ((Element) ecdsaNameCurve).removeAttributeNode(attrNode);
    }
    nodeList = document.getElementsByTagNameNS(
        "http://www.w3.org/2001/04/xmldsig-more#", "X");
    for (int i = 0; i < nodeList.getLength(); i++) {
      Node x = nodeList.item(i);
      Attr attrNode = ((Element) x).getAttributeNodeNS(null, "Value");
      x.appendChild(document.createTextNode(attrNode.getValue()));
      ((Element) x).removeAttributeNode(attrNode);
    }
    nodeList = document.getElementsByTagNameNS(
        "http://www.w3.org/2001/04/xmldsig-more#", "Y");
    for (int i = 0; i < nodeList.getLength(); i++) {
      Node y = nodeList.item(i);
      Attr attrNode = ((Element) y).getAttributeNodeNS(null, "Value");
      y.appendChild(document.createTextNode(attrNode.getValue()));
      ((Element) y).removeAttributeNode(attrNode);
    }

    if (base64LineLength > 0) {
      nodeList = document.getElementsByTagNameNS(
          "http://www.w3.org/2000/09/xmldsig#", "Modulus");
      for (int i = 0; i < nodeList.getLength(); i++) {
        Node modulus = nodeList.item(i);
        String value = ((Element) modulus).getTextContent();
        BufferedReader reader = new BufferedReader(new InputStreamReader(
            new ByteArrayInputStream(value.getBytes())));
        char[] buff = new char[base64LineLength];
        StringBuffer newValue = new StringBuffer();
        int found = 0;
        try {
          while ((found = reader.read(buff)) > 0) {
            newValue.append(buff, 0, found);
            if (found == base64LineLength)
              newValue.append('\n');
          }
        } catch (IOException e) {
          // this should never happen, as we are reading from a ByteArrayInputStream
          throw new RuntimeException(e);
        }
        ((Element) modulus).setTextContent(newValue.toString());
      }

    }

    
  }
  
}
