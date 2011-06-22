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

import org.etsi.uri._01903.v1_1.CertIDListType;
import org.etsi.uri._01903.v1_1.CertIDType;
import org.etsi.uri._01903.v1_1.DataObjectFormatType;
import org.etsi.uri._01903.v1_1.DigestAlgAndValueType;
import org.etsi.uri._01903.v1_1.QualifyingPropertiesType;
import org.etsi.uri._01903.v1_1.SignaturePolicyIdentifierType;
import org.etsi.uri._01903.v1_1.SignedDataObjectPropertiesType;
import org.etsi.uri._01903.v1_1.SignedPropertiesType;
import org.etsi.uri._01903.v1_1.SignedSignaturePropertiesType;
import org.w3._2000._09.xmldsig_.DigestMethodType;
import org.w3._2000._09.xmldsig_.X509IssuerSerialType;
import org.w3c.dom.Node;

import at.gv.egiz.marshal.MarshallerFactory;

public class QualifyingPropertiesFactory {
  
  public static String NS_URI_V1_1_1 = "http://uri.etsi.org/01903/v1.1.1#";
  
  public static String SIGNED_PROPERTIES_REFERENCE_TYPE_V1_1_1 = NS_URI_V1_1_1 + "SignedProperties";

  private static QualifyingPropertiesFactory instance;
  
  /**
   * The <code>JAXBContext</code>.
   */
  private static JAXBContext jaxbContext;
  
  public static synchronized QualifyingPropertiesFactory getInstance() {
    if (instance == null) {
      instance = new QualifyingPropertiesFactory();
    }
    return instance;
  }

  private DatatypeFactory datatypeFactory;
  
  private org.etsi.uri._01903.v1_1.ObjectFactory qpFactory;
  
  private org.w3._2000._09.xmldsig_.ObjectFactory dsFactory;

  public QualifyingPropertiesFactory() {
    
    try {
      datatypeFactory = DatatypeFactory.newInstance();
    } catch (DatatypeConfigurationException e) {
      throw new RuntimeException(e);
    }
    
    qpFactory = new org.etsi.uri._01903.v1_1.ObjectFactory();
    
    dsFactory = new org.w3._2000._09.xmldsig_.ObjectFactory();
    
    StringBuffer packageNames = new StringBuffer();
    
    packageNames.append(org.etsi.uri._01903.v1_1.ObjectFactory.class.getPackage().getName());
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

    DigestAlgAndValueType digestAlgAndValueType = qpFactory.createDigestAlgAndValueType();
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
    
    DataObjectFormatType dataObjectFormatType = qpFactory.createDataObjectFormatType();
    dataObjectFormatType.setObjectReference(objectReference);
    
    if (mimeType != null) {
      dataObjectFormatType.setMimeType(mimeType);
    }
    if (description != null) {
      dataObjectFormatType.setDescription(description);
    }
    
    return dataObjectFormatType;
  }
  
  public JAXBElement<QualifyingPropertiesType> createQualifyingProperties111(
      String target, Date signingTime, List<X509Certificate> certificates,
      String idValue, List<DataObjectFormatType> dataObjectFormats,
      DigestMethod digestMethod) throws QualifyingPropertiesException {

    GregorianCalendar gregorianCalendar = new GregorianCalendar();
    gregorianCalendar.setTimeZone(TimeZone.getTimeZone("UTC"));
    gregorianCalendar.setTime(signingTime);
    
    SignedSignaturePropertiesType signedSignaturePropertiesType = qpFactory.createSignedSignaturePropertiesType();
    
    // SigningTime
    XMLGregorianCalendar xmlGregorianCalendar = datatypeFactory.newXMLGregorianCalendar(gregorianCalendar);
    xmlGregorianCalendar.setFractionalSecond(null);
    signedSignaturePropertiesType.setSigningTime(xmlGregorianCalendar);

    // SigningCertificate
    CertIDListType certIDListType = qpFactory.createCertIDListType();
    List<CertIDType> certIDs = certIDListType.getCert();

    for (X509Certificate certificate : certificates) {
      
      CertIDType certIDType = qpFactory.createCertIDType();
      certIDType.setCertDigest(createDigestAlgAndValueType(certificate, digestMethod));
      certIDType.setIssuerSerial(createX509IssuerSerialType(certificate));
      
      certIDs.add(certIDType);
      
    }
    signedSignaturePropertiesType.setSigningCertificate(certIDListType);
    
    // SignaturePolicy
    SignaturePolicyIdentifierType signaturePolicyIdentifierType = qpFactory.createSignaturePolicyIdentifierType();
    signaturePolicyIdentifierType.setSignaturePolicyImplied(new SignaturePolicyIdentifierType.SignaturePolicyImplied());
    signedSignaturePropertiesType.setSignaturePolicyIdentifier(signaturePolicyIdentifierType);

    // SignedProperties
    SignedPropertiesType signedPropertiesType = qpFactory.createSignedPropertiesType();
    signedPropertiesType.setSignedSignatureProperties(signedSignaturePropertiesType);

    // DataObjectFormat
    if (dataObjectFormats != null && !dataObjectFormats.isEmpty()) {
      SignedDataObjectPropertiesType signedDataObjectPropertiesType = qpFactory.createSignedDataObjectPropertiesType();
      List<DataObjectFormatType> dataObjectFormatTypes = signedDataObjectPropertiesType.getDataObjectFormat();
      dataObjectFormatTypes.addAll(dataObjectFormats);
      signedPropertiesType.setSignedDataObjectProperties(signedDataObjectPropertiesType);
    }
    
    signedPropertiesType.setId(idValue);
    
    // QualifyingProperties
    QualifyingPropertiesType qualifyingPropertiesType = qpFactory.createQualifyingPropertiesType();
    qualifyingPropertiesType.setSignedProperties(signedPropertiesType);
    
    qualifyingPropertiesType.setTarget(target);
    
    return qpFactory.createQualifyingProperties(qualifyingPropertiesType);
    
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
