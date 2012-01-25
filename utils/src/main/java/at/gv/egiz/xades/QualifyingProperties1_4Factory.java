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


package at.gv.egiz.xades;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.etsi.uri._01903.v1_3.CertIDListType;
import org.etsi.uri._01903.v1_3.CertIDType;
import org.etsi.uri._01903.v1_3.DataObjectFormatType;
import org.etsi.uri._01903.v1_3.DigestAlgAndValueType;
import org.etsi.uri._01903.v1_3.QualifyingPropertiesType;
import org.etsi.uri._01903.v1_3.SignaturePolicyIdentifierType;
import org.etsi.uri._01903.v1_3.SignedDataObjectPropertiesType;
import org.etsi.uri._01903.v1_3.SignedPropertiesType;
import org.etsi.uri._01903.v1_3.SignedSignaturePropertiesType;
import org.w3._2000._09.xmldsig_.DigestMethodType;
import org.w3._2000._09.xmldsig_.X509IssuerSerialType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import at.gv.egiz.marshal.MarshallerFactory;

public class QualifyingProperties1_4Factory {
  
  public static String NS_URI = "http://uri.etsi.org/01903#";
  public static String NS_URI_V1_3_2 = "http://uri.etsi.org/01903/v1.3.2#";
  
  public static String SIGNED_PROPERTIES_REFERENCE_TYPE = NS_URI + "SignedProperties";

  private static QualifyingProperties1_4Factory instance;
  
  /**
   * The <code>JAXBContext</code>.
   */
  private static JAXBContext jaxbContext;
  
  public static synchronized QualifyingProperties1_4Factory getInstance() {
    if (instance == null) {
      instance = new QualifyingProperties1_4Factory();
    }
    return instance;
  }

  private DatatypeFactory datatypeFactory;
  
  private org.etsi.uri._01903.v1_3.ObjectFactory qpFactory_v1_3;
  
  private org.w3._2000._09.xmldsig_.ObjectFactory dsFactory;

  public QualifyingProperties1_4Factory() {
    
    try {
      datatypeFactory = DatatypeFactory.newInstance();
    } catch (DatatypeConfigurationException e) {
      throw new RuntimeException(e);
    }
    
    qpFactory_v1_3 = new org.etsi.uri._01903.v1_3.ObjectFactory();
    
    dsFactory = new org.w3._2000._09.xmldsig_.ObjectFactory();
    
    StringBuffer packageNames = new StringBuffer();
    
    packageNames.append(org.etsi.uri._01903.v1_4.ObjectFactory.class.getPackage().getName());
    packageNames.append(":");
    packageNames.append(org.w3._2000._09.xmldsig_.ObjectFactory.class.getPackage().getName());

    try {
      jaxbContext = JAXBContext.newInstance(packageNames.toString());
    } catch (JAXBException e) {
      // we should not get an JAXBException initializing the JAXBContext
      throw new RuntimeException(e);
    }

  }
  
  public DigestAlgAndValueType createDigestAlgAndValueType(X509Certificate certificate, DigestMethod dm) throws QualifyingPropertiesException {
    
    DigestMethodType digestMethodType = dsFactory.createDigestMethodType();
    digestMethodType.setAlgorithm(dm.getAlgorithm());
    
    byte[] digest;
    try {
      MessageDigest messageDigest = MessageDigest.getInstance(dm.getAlgorithm());
      digest = messageDigest.digest(certificate.getEncoded());
    } catch (CertificateEncodingException e) {
      throw new QualifyingPropertiesException(e);
    } catch (NoSuchAlgorithmException e) {
      throw new QualifyingPropertiesException(e);
    }

    DigestAlgAndValueType digestAlgAndValueType = qpFactory_v1_3.createDigestAlgAndValueType();
    digestAlgAndValueType.setDigestMethod(digestMethodType);
    digestAlgAndValueType.setDigestValue(digest);

    return digestAlgAndValueType;
    
  }
  
  public X509IssuerSerialType createX509IssuerSerialType(X509Certificate certificate) {
    
    String name = certificate.getIssuerX500Principal().getName("RFC2253");
    BigInteger serialNumber = certificate.getSerialNumber();
    
    X509IssuerSerialType issuerSerialType = dsFactory.createX509IssuerSerialType();
    issuerSerialType.setX509IssuerName(name);
    issuerSerialType.setX509SerialNumber(serialNumber);
    
    return issuerSerialType;
    
  }
  
  public DataObjectFormatType createDataObjectFormatType(String objectReference, String mimeType, String description) {
    
    DataObjectFormatType dataObjectFormatType = qpFactory_v1_3.createDataObjectFormatType();
    dataObjectFormatType.setObjectReference(objectReference);
    
    if (mimeType != null) {
      dataObjectFormatType.setMimeType(mimeType);
    }
    if (description != null) {
      dataObjectFormatType.setDescription(description);
    }
    
    return dataObjectFormatType;
  }
  
  public JAXBElement<QualifyingPropertiesType> createQualifyingProperties141(
      String target, Date signingTime, List<X509Certificate> certificates,
      String idValue, List<DataObjectFormatType> dataObjectFormats,
      DigestMethod digestMethod) throws QualifyingPropertiesException {

    GregorianCalendar gregorianCalendar = new GregorianCalendar();
    gregorianCalendar.setTimeZone(TimeZone.getTimeZone("UTC"));
    gregorianCalendar.setTime(signingTime);
    
    SignedSignaturePropertiesType signedSignaturePropertiesType = qpFactory_v1_3.createSignedSignaturePropertiesType();
    
    // SigningTime
    XMLGregorianCalendar xmlGregorianCalendar = datatypeFactory.newXMLGregorianCalendar(gregorianCalendar);
    xmlGregorianCalendar.setFractionalSecond(null);
    signedSignaturePropertiesType.setSigningTime(xmlGregorianCalendar);

    // SigningCertificate
    CertIDListType certIDListType = qpFactory_v1_3.createCertIDListType();
    List<CertIDType> certIDs = certIDListType.getCert();

    for (X509Certificate certificate : certificates) {
      
      CertIDType certIDType = qpFactory_v1_3.createCertIDType();
      certIDType.setCertDigest(createDigestAlgAndValueType(certificate, digestMethod));
      certIDType.setIssuerSerial(createX509IssuerSerialType(certificate));
      
      certIDs.add(certIDType);
      
    }
    signedSignaturePropertiesType.setSigningCertificate(certIDListType);
    
    // SignaturePolicy
    SignaturePolicyIdentifierType signaturePolicyIdentifierType = qpFactory_v1_3.createSignaturePolicyIdentifierType();
    try {
        Element e = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument().createElement("SignaturePolicyImplied");
        signaturePolicyIdentifierType.setSignaturePolicyImplied(e);
    } catch (ParserConfigurationException e1) {
        //Should not fail
        throw new RuntimeException(e1);
    }
    signedSignaturePropertiesType.setSignaturePolicyIdentifier(signaturePolicyIdentifierType);

    // SignedProperties
    SignedPropertiesType signedPropertiesType = qpFactory_v1_3.createSignedPropertiesType();
    signedPropertiesType.setSignedSignatureProperties(signedSignaturePropertiesType);

    // DataObjectFormat
    if (dataObjectFormats != null && !dataObjectFormats.isEmpty()) {
      SignedDataObjectPropertiesType signedDataObjectPropertiesType = qpFactory_v1_3.createSignedDataObjectPropertiesType();
      List<DataObjectFormatType> dataObjectFormatTypes = signedDataObjectPropertiesType.getDataObjectFormat();
      dataObjectFormatTypes.addAll(dataObjectFormats);
      signedPropertiesType.setSignedDataObjectProperties(signedDataObjectPropertiesType);
    }
    
    signedPropertiesType.setId(idValue);
    
    // QualifyingProperties
    QualifyingPropertiesType qualifyingPropertiesType = qpFactory_v1_3.createQualifyingPropertiesType();
    qualifyingPropertiesType.setSignedProperties(signedPropertiesType);
    
    qualifyingPropertiesType.setTarget(target);
    
    return qpFactory_v1_3.createQualifyingProperties(qualifyingPropertiesType);
    
  }
  
  public void marshallQualifyingProperties(JAXBElement<QualifyingPropertiesType> qualifyingProperties, Node parent) throws JAXBException {
    
    try {
      Marshaller marshaller = MarshallerFactory.createMarshaller(jaxbContext, true);

      marshaller.marshal(qualifyingProperties, parent);
    } catch (PropertyException e) {
      throw new RuntimeException(e);
    } 
    
  }
  
}
