/*
 * Copyright 2013 by Graz University of Technology, Austria
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


package at.gv.egiz.bku.slcommands.impl.cms;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.xml.crypto.dsig.DigestMethod;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3._2000._09.xmldsig_.DigestMethodType;

import at.buergerkarte.namespaces.securitylayer._1_2_3.CMSDataObjectOptionalMetaType;
import at.buergerkarte.namespaces.securitylayer._1_2_3.CMSDataObjectRequiredMetaType;
import at.buergerkarte.namespaces.securitylayer._1_2_3.DigestAndRefType;
import at.buergerkarte.namespaces.securitylayer._1_2_3.ExcludedByteRangeType;
import at.buergerkarte.namespaces.securitylayer._1_2_3.MetaInfoType;
import at.gv.egiz.bku.slcommands.impl.xsect.AlgorithmMethodFactory;
import at.gv.egiz.bku.slcommands.impl.xsect.AlgorithmMethodFactoryImpl;
import at.gv.egiz.bku.slcommands.impl.xsect.STALSignatureException;
import at.gv.egiz.bku.slexceptions.SLCommandException;
import at.gv.egiz.bku.utils.urldereferencer.URLDereferencer;
import at.gv.egiz.stal.HashDataInput;
import at.gv.egiz.stal.STAL;
import iaik.asn1.ASN1Object;
import iaik.asn1.CodingException;
import iaik.asn1.ObjectID;
import iaik.asn1.SEQUENCE;
import iaik.asn1.UTF8String;
import iaik.asn1.structures.AlgorithmID;
import iaik.asn1.structures.Attribute;
import iaik.asn1.structures.ChoiceOfTime;
import iaik.cms.CMSException;
import iaik.cms.CMSSignatureException;
import iaik.cms.CertificateIdentifier;
import iaik.cms.ContentInfo;
import iaik.cms.IssuerAndSerialNumber;
import iaik.cms.SignedData;
import iaik.cms.SignerInfo;
import iaik.smime.ess.ESSCertID;
import iaik.smime.ess.ESSCertIDv2;
import iaik.x509.X509ExtensionException;

/**
 * This class represents a CMS-Signature as to be created by the
 * security layer command <code>CreateCMSSignatureRequest</code>.
 * 
 * @author tkellner
 */
public class Signature {

  public final static String ID_AA_ETS_MIMETYPE = "0.4.0.1733.2.1";

  /**
   * Logging facility.
   */
  private final Logger log = LoggerFactory.getLogger(Signature.class);

  protected SignedData signedData;
  protected SignerInfo signerInfo;
  protected byte[] signedDocument;
  protected String mimeType;
  protected AlgorithmID signatureAlgorithm;
  protected AlgorithmID digestAlgorithm;
  protected byte[] digestValue;
  protected String signatureAlgorithmURI;
  protected String digestAlgorithmURI;
  protected ExcludedByteRangeType excludedByteRange;
  private CMSHashDataInput hashDataInput;
  

public Signature(CMSDataObjectOptionalMetaType dataObject, String structure,
      X509Certificate signingCertificate, Date signingTime, URLDereferencer urlDereferencer,
      boolean useStrongHash)
          throws NoSuchAlgorithmException, CertificateEncodingException,
          CertificateException, X509ExtensionException, InvalidParameterException,
          CodingException, SLCommandException, IOException, CMSException {
    int mode = structure.equalsIgnoreCase("enveloping") ? SignedData.IMPLICIT : SignedData.EXPLICIT;
    if (dataObject.getContent() != null) {
      String filename = null;
      byte[] dataToBeSigned = getContent(dataObject, urlDereferencer);
      this.signedData = new SignedData(dataToBeSigned, mode);
      MetaInfoType metaInfo = dataObject.getMetaInfo();
      if (metaInfo != null) {
        this.mimeType = metaInfo.getMimeType();
        filename = metaInfo.getDescription(); // security layer doesn't specify explicit filename property for
                                              // single signature requests
      }
      hashDataInput = new CMSHashDataInput(signedDocument, mimeType);
      hashDataInput.setFilename(filename);
    } else {
      DigestAndRefType digestAndRef = dataObject.getDigestAndRef();
      DigestMethodType digestMethod = digestAndRef.getDigestMethod();     
      
      hashDataInput = new ReferencedHashDataInput(dataObject.getMetaInfo().getMimeType(), urlDereferencer,
					digestAndRef.getReference(), dataObject.getExcludedByteRange());
	
      try {
        digestAlgorithm = getAlgorithmID(digestMethod.getAlgorithm());
      } catch (URISyntaxException e) {
        //TODO: choose proper execption
        throw new NoSuchAlgorithmException(e);
      }
      digestValue = digestAndRef.getDigestValue();
      this.signedData = new SignedData(ObjectID.pkcs7_data);
    }
    setAlgorithmIDs(signingCertificate, useStrongHash);
    createSignerInfo(signingCertificate);
    setSignerCertificate(signingCertificate);
    this.mimeType = dataObject.getMetaInfo().getMimeType();
    
    setAttributes(this.mimeType, signingCertificate, signingTime);
  }
  
  public Signature(CMSDataObjectRequiredMetaType dataObject, String structure,
	      X509Certificate signingCertificate, URLDereferencer urlDereferencer,
	      boolean useStrongHash)
	          throws NoSuchAlgorithmException, CertificateEncodingException,
	          CertificateException, X509ExtensionException, InvalidParameterException,
	          CodingException, SLCommandException, IOException {
	    byte[] dataToBeSigned = getContent(dataObject, urlDereferencer);
	    int mode = structure.equalsIgnoreCase("enveloping") ? SignedData.IMPLICIT : SignedData.EXPLICIT;
	    this.signedData = new SignedData(dataToBeSigned, mode);
	    setAlgorithmIDs(signingCertificate, useStrongHash);
	    createSignerInfo(signingCertificate);
	    setSignerCertificate(signingCertificate);
	 
	    hashDataInput = new CMSHashDataInput(signedDocument, mimeType);
	    MetaInfoType metaInfo = dataObject.getMetaInfo();
	    if (metaInfo != null) {
	      // security layer doesn't specify explicit filename property for
	      // single signature requests
	      hashDataInput.setFilename(metaInfo.getDescription());
	    }
	    setAttributes(signingCertificate);
	  }
  
  

  private void createSignerInfo(X509Certificate signingCertificate) throws CertificateEncodingException, CertificateException {
    iaik.x509.X509Certificate sigcert =
        new iaik.x509.X509Certificate(signingCertificate.getEncoded());
    CertificateIdentifier signerIdentifier =
        new IssuerAndSerialNumber(sigcert);
    PrivateKey privateKey = new STALPrivateKey(signatureAlgorithmURI, digestAlgorithmURI);
    signerInfo = new SignerInfo(signerIdentifier, digestAlgorithm,
        signatureAlgorithm, privateKey);
  }

  private void setSignerCertificate(X509Certificate signingCertificate) {
    X509Certificate[] sigcerts = new X509Certificate[] { signingCertificate };
    signedData.addCertificates(sigcerts);
  }

  private void setAttributes(String mimeType, X509Certificate signingCertificate, Date signingTime) throws CertificateException, NoSuchAlgorithmException, CodingException {
    List<Attribute> attributes = new ArrayList<Attribute>();
    setMimeTypeAttrib(attributes, mimeType);
    setContentTypeAttrib(attributes);
    setSigningCertificateAttrib(attributes, signingCertificate);
    if (signingTime != null)
      setSigningTimeAttrib(attributes, signingTime);
    Attribute[] attributeArray = attributes.toArray(new Attribute[attributes.size()]);
    signerInfo.setSignedAttributes(attributeArray);
  }
  
  private void setAttributes(X509Certificate signingCertificate) throws CertificateException, NoSuchAlgorithmException, CodingException {
	    List<Attribute> attributes = new ArrayList<Attribute>();
	    setContentTypeAttrib(attributes);
	    setSigningCertificateAttrib(attributes, signingCertificate);
	    Attribute[] attributeArray = attributes.toArray(new Attribute[attributes.size()]);
	    signerInfo.setSignedAttributes(attributeArray);
	  }
  


  private void setMimeTypeAttrib(List<Attribute> attributes, String mimeType) {
    String oidStr = ID_AA_ETS_MIMETYPE;
    String name = "mime-type";
    ObjectID mimeTypeOID = new ObjectID(oidStr, name);

    Attribute mimeTypeAtt = new Attribute(mimeTypeOID, new ASN1Object[] {new UTF8String(mimeType)});
    attributes.add(mimeTypeAtt);
  }

  private void setContentTypeAttrib(List<Attribute> attributes) {
    Attribute contentType = new Attribute(ObjectID.contentType, new ASN1Object[] {ObjectID.cms_data});
    attributes.add(contentType);
  }

  private void setSigningCertificateAttrib(List<Attribute> attributes, X509Certificate signingCertificate) throws CertificateException, NoSuchAlgorithmException, CodingException {
    ObjectID id;
    ASN1Object value = new SEQUENCE();
    if (digestAlgorithm.equals(AlgorithmID.sha1)) {
      id = ObjectID.signingCertificate;
      value.addComponent(new ESSCertID(signingCertificate, true).toASN1Object());
    }
    else {
      id = ObjectID.signingCertificateV2;
      value.addComponent(new ESSCertIDv2(digestAlgorithm, signingCertificate, true).toASN1Object());
    }
    ASN1Object signingCert = new SEQUENCE();
    signingCert.addComponent(value);
    Attribute signingCertificateAttrib = new Attribute(id, new ASN1Object[] {signingCert});
    attributes.add(signingCertificateAttrib);
  }

  private void setSigningTimeAttrib(List<Attribute> attributes, Date date) {
    Attribute signingTime = new Attribute(ObjectID.signingTime, new ASN1Object[] {new ChoiceOfTime(date).toASN1Object()});
    attributes.add(signingTime);
  }

  private byte[] getContent(CMSDataObjectOptionalMetaType dataObject, URLDereferencer urlDereferencer)
      throws InvalidParameterException, SLCommandException, IOException {
    byte[] data = dataObject.getContent().getBase64Content();
    if (data == null) {
      String reference = dataObject.getContent().getReference();
      if (reference == null)
        throw new SLCommandException(4003);
      InputStream is = urlDereferencer.dereference(reference).getStream();
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      byte[] buffer = new byte[1024];
      for (int i = is.read(buffer); i > -1; i = is.read(buffer)) {
        baos.write(buffer, 0, i);
      }
      data = baos.toByteArray();
      is.close();
    }
    this.signedDocument = data.clone();

    this.excludedByteRange = dataObject.getExcludedByteRange();
    if (this.excludedByteRange == null)
      return data;

    int from = this.excludedByteRange.getFrom().intValue();
    int to = this.excludedByteRange.getTo().intValue();
    if (from > data.length || to > data.length || from > to)
      throw new InvalidParameterException("ExcludedByteRange contains invalid data: [" +
      from + "-" + to + "], Content length: " + data.length);

    // Fill ExcludedByteRange with 0s for document to display in viewer
    Arrays.fill(this.signedDocument, from, to+1, (byte)0);

    // Remove ExcludedByteRange from data to be signed
    byte[] first = null;
    byte[] second = null;
    if (from > 0)
      first = Arrays.copyOfRange(data, 0, from);
    if ((to + 1) < data.length)
      second = Arrays.copyOfRange(data, to + 1, data.length);
    data = ArrayUtils.addAll(first, second);
    log.debug("ExcludedByteRange [" + from + "-" + to + "], Content length: " + data.length);
    return data;
  }

  private void setAlgorithmIDs(X509Certificate signingCertificate, boolean useStrongHash) throws NoSuchAlgorithmException {
    AlgorithmMethodFactory amf = new AlgorithmMethodFactoryImpl(signingCertificate, useStrongHash);
    signatureAlgorithmURI = amf.getSignatureAlgorithmURI();
    signatureAlgorithm = amf.getSignatureAlgorithmID();
    if (digestAlgorithm != null) {
      if (AlgorithmID.sha1.equals(digestAlgorithm)) {
        digestAlgorithmURI = DigestMethod.SHA1;
      } else if (AlgorithmID.sha256.equals(digestAlgorithm)) {
        digestAlgorithmURI = DigestMethod.SHA256;
      } else if (AlgorithmID.sha512.equals(digestAlgorithm)) {
        digestAlgorithmURI = DigestMethod.SHA512;
      } else if (AlgorithmID.ripeMd160.equals(digestAlgorithm)) {
        digestAlgorithmURI = DigestMethod.RIPEMD160;
      } else {
        throw new NoSuchAlgorithmException("Algorithm '" + digestAlgorithm + "' not supported.");
      }
    } else {
    digestAlgorithmURI = amf.getDigestAlgorithmURI();
      digestAlgorithm = amf.getDigestAlgorithmID();
      }
      }
      
	public HashDataInput getHashDataInput() {

		if (hashDataInput != null) {
			return hashDataInput;
      } else {
			return new CMSHashDataInput(signedDocument, mimeType);
    }
  }


  public byte[] sign(STAL stal, String keyboxIdentifier) throws CMSException, CMSSignatureException, SLCommandException {
    STALSecurityProvider securityProvider = new STALSecurityProvider(stal, keyboxIdentifier, getHashDataInput(), this.excludedByteRange);
    signedData.setSecurityProvider(securityProvider);
    try {
      signedData.addSignerInfo(signerInfo);
    } catch (NoSuchAlgorithmException e) {
      STALSignatureException stalSignatureException = securityProvider.getStalSignatureException();
      if (stalSignatureException != null) {
        throw new SLCommandException(stalSignatureException.getErrorCode());
      }
      throw new CMSSignatureException(e);
    }
    if (digestValue != null) {
      try {
        signedData.setMessageDigest(digestAlgorithm, digestValue);
      } catch (NoSuchAlgorithmException e) {
        throw new CMSSignatureException(e);
      }
    }
    ContentInfo contentInfo = new ContentInfo(signedData);
    return contentInfo.getEncoded();
  }
  
  protected AlgorithmID getAlgorithmID(String uri) throws URISyntaxException {
    String oid = null;
    URI urn = new URI(uri);
    String scheme = urn.getScheme();
    if ("URN".equalsIgnoreCase(scheme)) {
      String schemeSpecificPart = urn.getSchemeSpecificPart().toLowerCase();
      if (schemeSpecificPart.startsWith("oid:")) {
        oid = schemeSpecificPart.substring(4, schemeSpecificPart.length());
}
    }
    return new AlgorithmID(new ObjectID(oid));
  }
}


