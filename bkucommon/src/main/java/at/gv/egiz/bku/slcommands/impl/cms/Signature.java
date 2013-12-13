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
import iaik.security.ecc.interfaces.ECDSAParams;
import iaik.smime.ess.ESSCertID;
import iaik.smime.ess.ESSCertIDv2;
import iaik.x509.X509ExtensionException;

import java.security.InvalidParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.ECParameterSpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.buergerkarte.namespaces.securitylayer._1_2_3.CMSDataObjectRequiredMetaType;
import at.buergerkarte.namespaces.securitylayer._1_2_3.ExcludedByteRangeType;
import at.gv.egiz.bku.slcommands.impl.xsect.AlgorithmMethodFactory;
import at.gv.egiz.bku.slcommands.impl.xsect.AlgorithmMethodFactoryImpl;
import at.gv.egiz.bku.slcommands.impl.xsect.STALSignatureException;
import at.gv.egiz.bku.slexceptions.SLCommandException;
import at.gv.egiz.stal.HashDataInput;
import at.gv.egiz.stal.STAL;

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

  private SignedData signedData;
  private SignerInfo signerInfo;
  private byte[] signedDocument;
  private String mimeType;
  private AlgorithmID signatureAlgorithm;
  private AlgorithmID digestAlgorithm;
  private String signatureAlgorithmURI;
  private String digestAlgorithmURI;
  private ExcludedByteRangeType excludedByteRange;

  public Signature(CMSDataObjectRequiredMetaType dataObject, String structure,
      X509Certificate signingCertificate, Date signingTime, boolean useStrongHash)
          throws NoSuchAlgorithmException, CertificateEncodingException, CertificateException, X509ExtensionException, InvalidParameterException, CodingException {
    byte[] dataToBeSigned = getContent(dataObject);
    int mode = structure.equalsIgnoreCase("enveloping") ? SignedData.IMPLICIT : SignedData.EXPLICIT;
    this.signedData = new SignedData(dataToBeSigned, mode);
    setAlgorithmIDs(signingCertificate, useStrongHash);
    createSignerInfo(signingCertificate);
    setSignerCertificate(signingCertificate);
    this.mimeType = dataObject.getMetaInfo().getMimeType();
    setAttributes(this.mimeType, signingCertificate, signingTime);
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
    setSigningTimeAttrib(attributes, signingTime);
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

  private byte[] getContent(CMSDataObjectRequiredMetaType dataObject) throws InvalidParameterException {
    byte[] data = dataObject.getContent().getBase64Content();
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

  private void setSignerInfo() throws SLCommandException, CMSException, CMSSignatureException {
    try {
      signedData.addSignerInfo(signerInfo);
    } catch (NoSuchAlgorithmException e) {
      if (e.getCause() instanceof CMSException) {
        CMSException e2 = (CMSException) e.getCause();
        if (e2.getCause() instanceof SignatureException)
        {
          SignatureException e3 = (SignatureException) e2.getCause();
          if (e3.getCause() instanceof STALSignatureException) {
            STALSignatureException e4 = (STALSignatureException) e3.getCause();
            throw new SLCommandException(e4.getErrorCode());
          }
        }
        throw e2;
      }
      throw new CMSSignatureException(e);
    }
  }

  private void setAlgorithmIDs(X509Certificate signingCertificate, boolean useStrongHash) throws NoSuchAlgorithmException {
    PublicKey publicKey = signingCertificate.getPublicKey();
    String algorithm = publicKey.getAlgorithm();
    AlgorithmMethodFactory amf = new AlgorithmMethodFactoryImpl(signingCertificate, useStrongHash);
    signatureAlgorithmURI = amf.getSignatureAlgorithmURI();
    digestAlgorithmURI = amf.getDigestAlgorithmURI();

    if ("DSA".equals(algorithm)) {
      signatureAlgorithm = AlgorithmID.dsaWithSHA1;
    } else if ("RSA".equals(algorithm)) {

      int keyLength = 0;
      if (publicKey instanceof RSAPublicKey) {
        keyLength = ((RSAPublicKey) publicKey).getModulus().bitLength();
      }

      if (useStrongHash && keyLength >= 2048) {
        signatureAlgorithm = AlgorithmID.sha256WithRSAEncryption;
        digestAlgorithm = AlgorithmID.sha256;
//      } else if (useStrongHash) { // Cannot be used if not enabled in AlgorithmMethodFactoryImpl
//        signatureAlgorithm = AlgorithmID.rsaSignatureWithRipemd160;
//        digestAlgorithm = AlgorithmID.ripeMd160;
      } else {
        signatureAlgorithm = AlgorithmID.sha1WithRSAEncryption;
        digestAlgorithm = AlgorithmID.sha1;
      }
      
    } else if (("EC".equals(algorithm)) || ("ECDSA".equals(algorithm))) {

      int fieldSize = 0;
      if (publicKey instanceof iaik.security.ecc.ecdsa.ECPublicKey) {
        ECDSAParams params = ((iaik.security.ecc.ecdsa.ECPublicKey) publicKey).getParameter();
        fieldSize = params.getG().getCurve().getField().getSize().bitLength();
      } else if (publicKey instanceof ECPublicKey) {
        ECParameterSpec params = ((ECPublicKey) publicKey).getParams();
        fieldSize = params.getCurve().getField().getFieldSize();
      }
      
      if (useStrongHash && fieldSize >= 512) {
        signatureAlgorithm = AlgorithmID.ecdsa_With_SHA512;
        digestAlgorithm = AlgorithmID.sha512;
      } else if (useStrongHash && fieldSize >= 256) {
        signatureAlgorithm = AlgorithmID.ecdsa_With_SHA256;
        digestAlgorithm = AlgorithmID.sha256;
      } else if (useStrongHash) {
          signatureAlgorithm = AlgorithmID.ecdsa_plain_With_RIPEMD160;
          digestAlgorithm = AlgorithmID.ripeMd160;
      } else {
        signatureAlgorithm = AlgorithmID.ecdsa_With_SHA1;
        digestAlgorithm = AlgorithmID.sha1;
      }
    } else {
      throw new NoSuchAlgorithmException("Public key algorithm '" + algorithm
          + "' not supported.");
    }
  }

  private HashDataInput getHashDataInput() {
    return new CMSHashDataInput(signedDocument, mimeType);
  }

  public byte[] sign(STAL stal, String keyboxIdentifier) throws CMSException, CMSSignatureException, SLCommandException {
    signedData.setSecurityProvider(new STALSecurityProvider(
        stal, keyboxIdentifier, getHashDataInput(), this.excludedByteRange));
    setSignerInfo();
    ContentInfo contentInfo = new ContentInfo(signedData);
    return contentInfo.getEncoded();
  }
}
