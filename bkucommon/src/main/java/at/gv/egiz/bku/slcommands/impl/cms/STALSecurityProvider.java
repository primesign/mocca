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


package at.gv.egiz.bku.slcommands.impl.cms;

import iaik.asn1.DerCoder;
import iaik.asn1.INTEGER;
import iaik.asn1.SEQUENCE;
import iaik.asn1.structures.AlgorithmID;
import iaik.cms.IaikProvider;
import iaik.utils.Util;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.buergerkarte.namespaces.securitylayer._1_2_3.ExcludedByteRangeType;
import at.gv.egiz.bku.slcommands.impl.xsect.STALSignatureException;
import at.gv.egiz.stal.ErrorResponse;
import at.gv.egiz.stal.HashDataInput;
import at.gv.egiz.stal.STAL;
import at.gv.egiz.stal.STALRequest;
import at.gv.egiz.stal.STALResponse;
import at.gv.egiz.stal.SignRequest;
import at.gv.egiz.stal.SignRequest.SignedInfo;
import at.gv.egiz.stal.SignResponse;

public class STALSecurityProvider extends IaikProvider {

  private final static Logger log = LoggerFactory.getLogger(STALSecurityProvider.class);

  private final static String ID_ECSIGTYPE = "1.2.840.10045.4";
  @SuppressWarnings("unused")
  private final static String ECDSA_PLAIN_SIGNATURES = "0.4.0.127.0.7.1.1.4.1";

  private String keyboxIdentifier;
  private STAL stal;
  private List<HashDataInput> hashDataInput;
  private ExcludedByteRangeType excludedByteRange;

  public STALSecurityProvider(STAL stal, String keyboxIdentifier,
      HashDataInput hashDataInput, ExcludedByteRangeType excludedByteRange) {
    this.keyboxIdentifier = keyboxIdentifier;
    this.stal = stal;
    this.hashDataInput = new ArrayList<HashDataInput>();
    this.hashDataInput.add(hashDataInput);
    this.excludedByteRange = excludedByteRange;
  }

  /* (non-Javadoc)
   * @see iaik.cms.IaikProvider#calculateSignatureFromSignedAttributes(iaik.asn1.structures.AlgorithmID, iaik.asn1.structures.AlgorithmID, java.security.PrivateKey, byte[])
   */
  @Override
  public byte[] calculateSignatureFromSignedAttributes(AlgorithmID signatureAlgorithm,
      AlgorithmID digestAlgorithm, PrivateKey privateKey,
      byte[] signedAttributes)
      throws SignatureException, InvalidKeyException, NoSuchAlgorithmException {
    log.debug("calculateSignatureFromSignedAttributes: " + signatureAlgorithm + ", " + digestAlgorithm);

    STALPrivateKey spk = (STALPrivateKey) privateKey;
    SignRequest signRequest = getSTALSignRequest(keyboxIdentifier, signedAttributes,
        spk.getAlgorithm(), spk.getDigestAlgorithm(), hashDataInput, excludedByteRange);

    log.debug("Sending STAL request ({})", privateKey.getAlgorithm());
    List<STALResponse> responses =
      stal.handleRequest(Collections.singletonList((STALRequest) signRequest));

    if (responses == null || responses.size() != 1) {
      throw new SignatureException("Failed to access STAL.");
    }

    STALResponse response = responses.get(0);
    if (response instanceof SignResponse) {
      byte[] sig = ((SignResponse) response).getSignatureValue();
      log.debug("Got STAL response: " + Util.toBase64String(sig));
      return wrapSignatureValue(sig, signatureAlgorithm);
    } else if (response instanceof ErrorResponse) {
      ErrorResponse err = (ErrorResponse) response;
      STALSignatureException se = new STALSignatureException(err.getErrorCode(), err.getErrorMessage());
      throw new SignatureException(se);
    } else {
      throw new SignatureException("Failed to access STAL.");
    }
  }

  private static SignRequest getSTALSignRequest(String keyboxIdentifier,
      byte[] signedAttributes, String signatureMethod, String digestMethod,
      List<HashDataInput> hashDataInput, ExcludedByteRangeType excludedByteRange) {
    SignRequest signRequest = new SignRequest();
    signRequest.setKeyIdentifier(keyboxIdentifier);
    log.debug("SignedAttributes: " + Util.toBase64String(signedAttributes));
    SignedInfo signedInfo = new SignedInfo();
    signedInfo.setValue(signedAttributes);
    signedInfo.setIsCMSSignedAttributes(true);
    signRequest.setSignedInfo(signedInfo);
    signRequest.setSignatureMethod(signatureMethod);
    signRequest.setDigestMethod(digestMethod);
    signRequest.setHashDataInput(hashDataInput);
    if (excludedByteRange != null) {
      SignRequest.ExcludedByteRange ebr = new SignRequest.ExcludedByteRange();
      ebr.setFrom(excludedByteRange.getFrom());
      ebr.setTo(excludedByteRange.getTo());
      signRequest.setExcludedByteRange(ebr);
    }
    return signRequest;
  }

  private static byte[] wrapSignatureValue(byte[] sig, AlgorithmID sigAlgorithmID) {
    String id = sigAlgorithmID.getAlgorithm().getID();
    if (id.startsWith(ID_ECSIGTYPE)) //X9.62 Format ECDSA signatures
    {
      //Wrap r and s in ASN.1 SEQUENCE
      byte[] r = Arrays.copyOfRange(sig, 0, sig.length/2);
      byte[] s = Arrays.copyOfRange(sig, sig.length/2, sig.length);
      SEQUENCE sigS = new SEQUENCE();
      sigS.addComponent(new INTEGER(new BigInteger(1, r)));
      sigS.addComponent(new INTEGER(new BigInteger(1, s)));
      return DerCoder.encode(sigS);
    }
    else
      return sig;
  }

}
