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

import iaik.asn1.structures.AlgorithmID;
import iaik.xml.crypto.XmldsigMore;

import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.ECParameterSpec;

import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.DigestMethodParameterSpec;
import javax.xml.crypto.dsig.spec.SignatureMethodParameterSpec;

/**
 * An implementation of the AlgorithmMethod factory that uses the signing
 * certificate to choose appropriate algorithms.
 * 
 * @author mcentner
 */
public class AlgorithmMethodFactoryImpl implements AlgorithmMethodFactory {

  /**
   * The signature algorithm URI.
   */
  private String signatureAlgorithmURI;
  
  /**
   * The signature algorithm ID.
   */
  private AlgorithmID signatureAlgorithmID;
  
  /**
   * the digest algorithm URI.
   */
  private String digestAlgorithmURI = DigestMethod.SHA1;

  /**
   * The digest algorithm ID.
   */
  private AlgorithmID digestAlgorithmID = AlgorithmID.sha1;

  /**
   * The algorithm parameters for the signature algorithm.
   */
  private SignatureMethodParameterSpec signatureMethodParameterSpec;

  /**
   * Creates a new AlgorithmMethodFactory with the given
   * <code>signingCertificate</code>.
   * 
   * @param signingCertificate
   * 
   * @throws NoSuchAlgorithmException
   *           if the public key algorithm of the given
   *           <code>signingCertificate</code> is not supported
   */
  public AlgorithmMethodFactoryImpl(X509Certificate signingCertificate, boolean useStrongHash)
      throws NoSuchAlgorithmException {

    PublicKey publicKey = signingCertificate.getPublicKey();
    String algorithm = publicKey.getAlgorithm();

    if ("DSA".equals(algorithm)) {
      signatureAlgorithmURI = SignatureMethod.DSA_SHA1;
      signatureAlgorithmID = AlgorithmID.dsaWithSHA1;
    } else if ("RSA".equals(algorithm)) {
      
      int keyLength = 0;
      if (publicKey instanceof RSAPublicKey) {
        keyLength = ((RSAPublicKey) publicKey).getModulus().bitLength();
      }
      
      if (useStrongHash && keyLength >= 2048) {
        signatureAlgorithmURI = XmldsigMore.SIGNATURE_RSA_SHA256;
        signatureAlgorithmID = AlgorithmID.sha256WithRSAEncryption;
        digestAlgorithmURI = DigestMethod.SHA256;
        digestAlgorithmID = AlgorithmID.sha256;
      } else {
        signatureAlgorithmURI = SignatureMethod.RSA_SHA1;
        signatureAlgorithmID = AlgorithmID.sha1WithRSAEncryption;
      }
      
    } else if (("EC".equals(algorithm)) || ("ECDSA".equals(algorithm))) {
      
      int fieldSize = 0;
      if (publicKey instanceof ECPublicKey) {
        ECParameterSpec params = ((ECPublicKey) publicKey).getParams();
        fieldSize = params.getCurve().getField().getFieldSize();
      } else {
        throw new NoSuchAlgorithmException("Public key type not supported.");
      }

      if (useStrongHash && fieldSize >= 512) {
        signatureAlgorithmURI = XmldsigMore.SIGNATURE_ECDSA_SHA512;
        signatureAlgorithmID = AlgorithmID.ecdsa_With_SHA512;
        digestAlgorithmURI = DigestMethod.SHA512;
        digestAlgorithmID = AlgorithmID.sha512;
      } else if (useStrongHash && fieldSize >= 256) {
        signatureAlgorithmURI = XmldsigMore.SIGNATURE_ECDSA_SHA256;
        signatureAlgorithmID = AlgorithmID.ecdsa_With_SHA256;
        digestAlgorithmURI = DigestMethod.SHA256;
        digestAlgorithmID = AlgorithmID.sha256;
      } else if (useStrongHash) {
          signatureAlgorithmURI = XmldsigMore.SIGNATURE_ECDSA_RIPEMD160;
          signatureAlgorithmID = AlgorithmID.ecdsa_plain_With_RIPEMD160;
          digestAlgorithmURI = DigestMethod.RIPEMD160;
          digestAlgorithmID = AlgorithmID.ripeMd160;
      } else {
        signatureAlgorithmURI = XmldsigMore.SIGNATURE_ECDSA_SHA1;
        signatureAlgorithmID = AlgorithmID.ecdsa_With_SHA1;
      }
      
    } else {
      throw new NoSuchAlgorithmException("Public key algorithm '" + algorithm
          + "' not supported.");
    }

  }

  /*
   * (non-Javadoc)
   * 
   * @seeat.gv.egiz.bku.slcommands.impl.xsect.AlgorithmMethodFactory#
   * createCanonicalizationMethod
   * (at.gv.egiz.bku.slcommands.impl.xsect.SignatureContext)
   */
  @Override
  public CanonicalizationMethod createCanonicalizationMethod(
      SignatureContext signatureContext) throws NoSuchAlgorithmException,
      InvalidAlgorithmParameterException {

    return signatureContext.getSignatureFactory().newCanonicalizationMethod(
        CanonicalizationMethod.EXCLUSIVE, (C14NMethodParameterSpec) null);

  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * at.gv.egiz.bku.slcommands.impl.xsect.AlgorithmMethodFactory#createDigestMethod
   * (at.gv.egiz.bku.slcommands.impl.xsect.SignatureContext)
   */
  @Override
  public DigestMethod createDigestMethod(SignatureContext signatureContext)
      throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {

    return signatureContext.getSignatureFactory().newDigestMethod(
        digestAlgorithmURI, (DigestMethodParameterSpec) null);
  }

  /*
   * (non-Javadoc)
   * 
   * @seeat.gv.egiz.bku.slcommands.impl.xsect.AlgorithmMethodFactory#
   * createSignatureMethod
   * (at.gv.egiz.bku.slcommands.impl.xsect.SignatureContext)
   */
  @Override
  public SignatureMethod createSignatureMethod(SignatureContext signatureContext)
      throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {

    return new STALSignatureMethod(signatureAlgorithmURI, signatureMethodParameterSpec);
  }

  @Override
  public String getSignatureAlgorithmURI() {
    return signatureAlgorithmURI;
  }

  @Override
  public String getDigestAlgorithmURI() {
    return digestAlgorithmURI;
  }

  @Override
  public AlgorithmID getSignatureAlgorithmID() {
    return signatureAlgorithmID;
  }

  @Override
  public AlgorithmID getDigestAlgorithmID() {
    return digestAlgorithmID;
  }

}
