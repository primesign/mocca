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
package at.gv.egiz.bku.slcommands.impl.xsect;

import iaik.security.ecc.interfaces.ECDSAParams;
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
   * Use SHA-2?
   */
  private static boolean SHA2 = false;
  
  /**
   * The signature algorithm URI.
   */
  private String signatureAlgorithmURI;
  
  /**
   * the digest algorithm URI.
   */
  private String digestAlgorithmURI = DigestMethod.SHA1;

  /**
   * The algorithm parameters for the signature algorithm.
   */
  private SignatureMethodParameterSpec signatureMethodParameterSpec;

  /**
   * Creates a new AlgrithmMethodFactory with the given
   * <code>signingCertificate</code>.
   * 
   * @param signingCertificate
   * 
   * @throws NoSuchAlgorithmException
   *           if the public key algorithm of the given
   *           <code>signingCertificate</code> is not supported
   */
  public AlgorithmMethodFactoryImpl(X509Certificate signingCertificate)
      throws NoSuchAlgorithmException {

    PublicKey publicKey = signingCertificate.getPublicKey();
    String algorithm = publicKey.getAlgorithm();

    if ("DSA".equals(algorithm)) {
      signatureAlgorithmURI = SignatureMethod.DSA_SHA1;
    } else if ("RSA".equals(algorithm)) {
      
      int keyLength = 0;
      if (publicKey instanceof RSAPublicKey) {
        keyLength = ((RSAPublicKey) publicKey).getModulus().bitLength();
      }
      
      if (SHA2 && keyLength >= 2048) {
        signatureAlgorithmURI = XmldsigMore.SIGNATURE_RSA_SHA256;
        digestAlgorithmURI = DigestMethod.SHA256;
      } else {
        signatureAlgorithmURI = SignatureMethod.RSA_SHA1;
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
      
      if (SHA2 && fieldSize >= 512) {
        signatureAlgorithmURI = XmldsigMore.SIGNATURE_ECDSA_SHA512;
        digestAlgorithmURI = DigestMethod.SHA512;
      } else if (SHA2 && fieldSize >= 256) {
        signatureAlgorithmURI = XmldsigMore.SIGNATURE_ECDSA_SHA256;
        digestAlgorithmURI = DigestMethod.SHA256;
      } else {
        signatureAlgorithmURI = XmldsigMore.SIGNATURE_ECDSA_SHA1;
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

}
