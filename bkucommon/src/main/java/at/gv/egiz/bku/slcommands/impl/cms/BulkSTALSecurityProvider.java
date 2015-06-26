/*
 * Copyright 2015 Datentechnik Innovation GmbH and Prime Sign GmbH, Austria
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
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.buergerkarte.namespaces.securitylayer._1_2_3.ExcludedByteRangeType;
import at.gv.egiz.bku.slcommands.impl.xsect.STALSignatureException;
import at.gv.egiz.stal.BulkSignRequest;
import at.gv.egiz.stal.BulkSignResponse;
import at.gv.egiz.stal.ErrorResponse;
import at.gv.egiz.stal.HashDataInput;
import at.gv.egiz.stal.STAL;
import at.gv.egiz.stal.STALRequest;
import at.gv.egiz.stal.STALResponse;
import at.gv.egiz.stal.SignRequest;
import at.gv.egiz.stal.SignRequest.SignedInfo;


/**
 * This implementation of the <code>SecurityProvider<code> is used to send a STALBulkSignatureRequest.
 * @author szoescher
 *
 */
public class BulkSTALSecurityProvider extends IaikProvider {

	private final static Logger log = LoggerFactory.getLogger(BulkSTALSecurityProvider.class);

	private final static String ID_ECSIGTYPE = "1.2.840.10045.4";
	@SuppressWarnings("unused")
	private final static String ECDSA_PLAIN_SIGNATURES = "0.4.0.127.0.7.1.1.4.1";

	private STAL stal;

	private List<BulkSignatureInfo> bulkSignatureInfo;

	private List<byte[]> signatureValues;

	public BulkSTALSecurityProvider(List<BulkSignatureInfo> bulkSignatureInfo, STAL stal) {

		this.bulkSignatureInfo = bulkSignatureInfo;
		this.stal = stal;
	}

  /* (non-Javadoc)
   * @see iaik.cms.IaikProvider#calculateSignatureFromSignedAttributes(iaik.asn1.structures.AlgorithmID, iaik.asn1.structures.AlgorithmID, java.security.PrivateKey, byte[])
   */
	@Override
	public byte[] calculateSignatureFromSignedAttributes(AlgorithmID signatureAlgorithm, AlgorithmID digestAlgorithm,
			PrivateKey privateKey, byte[] signedAttributes) throws SignatureException, InvalidKeyException,
			NoSuchAlgorithmException {

		log.debug("calculateSignatureFromSignedAttributes: " + signatureAlgorithm + ", " + digestAlgorithm);

		BulkSignRequest signRequest = getSTALSignRequest(bulkSignatureInfo);
		List<STALResponse> responses = stal.handleRequest(Collections.singletonList((STALRequest) signRequest));

		if (responses == null || responses.size() != 1) {
			throw new SignatureException("Failed to access STAL.");
		}

		STALResponse response = responses.get(0);
		if (response instanceof BulkSignResponse) {
			BulkSignResponse bulkSignatureResponse = ((BulkSignResponse) response);

			signatureValues = new ArrayList<byte[]>(bulkSignatureResponse.getSignResponse().size());

			for (int i = 0; i < bulkSignatureResponse.getSignResponse().size(); i++) {
				byte[] sig = ((BulkSignResponse) response).getSignResponse().get(i).getSignatureValue();
				log.debug("Got signature response: " + Util.toBase64String(sig));
				signatureValues.add(wrapSignatureValue(sig, signatureAlgorithm));
			}
			
			byte[] sig = ((BulkSignResponse) response).getSignResponse().get(0).getSignatureValue();
			log.debug("Got STAL response: " + Util.toBase64String(sig));

			wrapSignatureValue(sig, signatureAlgorithm);

			byte[] signaturePlaceholder = new byte[1024];
			return signaturePlaceholder;
		} else if (response instanceof ErrorResponse) {

			ErrorResponse err = (ErrorResponse) response;
			STALSignatureException se = new STALSignatureException(err.getErrorCode(), err.getErrorMessage());
			throw new SignatureException(se);
		} else {
			throw new SignatureException("Failed to access STAL.");
		}
	}

	private static BulkSignRequest getSTALSignRequest(List<BulkSignatureInfo> bulkSignatureInfo) {
		BulkSignRequest bulkSignRequest = new BulkSignRequest();

		for (BulkSignatureInfo signatureInfo : bulkSignatureInfo) {
			SignRequest signRequest = new SignRequest();
			signRequest.setKeyIdentifier(signatureInfo.getKeyboxIdentifier());
			log.debug("SignedAttributes: " + Util.toBase64String(signatureInfo.getSignedAttributes()));
			SignedInfo signedInfo = new SignedInfo();
			signedInfo.setValue(signatureInfo.getSignedAttributes());
			signedInfo.setIsCMSSignedAttributes(true);
			signRequest.setSignedInfo(signedInfo);

			signRequest.setSignatureMethod(signatureInfo.getSignatureMethod());
			signRequest.setDigestMethod(signatureInfo.getDigestMethod());
			signRequest.setHashDataInput(signatureInfo.getHashDataInput());

			ExcludedByteRangeType excludedByteRange = signatureInfo.getExcludedByteRange();
			if (excludedByteRange != null) {
				SignRequest.ExcludedByteRange ebr = new SignRequest.ExcludedByteRange();
				ebr.setFrom(excludedByteRange.getFrom());
				ebr.setTo(excludedByteRange.getTo());
				signRequest.setExcludedByteRange(ebr);
			}
			
			bulkSignRequest.getSignRequests().add(signRequest);
		}
		return bulkSignRequest;
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

	public List<byte[]> getSignatureValues() {
		return signatureValues;
	}

}
