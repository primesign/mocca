package at.gv.egiz.bku.slcommands.impl.cms;

import iaik.asn1.structures.AlgorithmID;
import iaik.cms.IaikProvider;
import iaik.utils.Util;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.gv.egiz.bku.slcommands.impl.xsect.STALSignatureException;
import at.gv.egiz.stal.ErrorResponse;
import at.gv.egiz.stal.STAL;
import at.gv.egiz.stal.STALRequest;
import at.gv.egiz.stal.STALResponse;
import at.gv.egiz.stal.SignRequest;
import at.gv.egiz.stal.SignResponse;

public class STALSecurityProvider extends IaikProvider {

  private final Logger log = LoggerFactory.getLogger(STALSecurityProvider.class);

  private String keyboxIdentifier;

  private STAL stal;

  public STALSecurityProvider(STAL stal, String keyboxIdentifier) {
    this.keyboxIdentifier = keyboxIdentifier;
    this.stal = stal;
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

    SignRequest signRequest = new SignRequest();
    signRequest.setKeyIdentifier(keyboxIdentifier);
    log.debug("SignedAttributes: " + Util.toBase64String(signedAttributes));
    signRequest.setSignedInfo(signedAttributes);
    signRequest.setSignedInfoIsRawData(true);
    signRequest.setSignatureMethod(privateKey.getAlgorithm());

    log.debug("Sending STAL request");
    List<STALResponse> responses =
      stal.handleRequest(Collections.singletonList((STALRequest) signRequest));

    if (responses == null || responses.size() != 1) {
      throw new SignatureException("Failed to access STAL.");
    }

    STALResponse response = responses.get(0);
    if (response instanceof SignResponse) {
      log.debug("Got STAL response: " + Util.toBase64String(((SignResponse) response).getSignatureValue()));
      return ((SignResponse) response).getSignatureValue();
    } else if (response instanceof ErrorResponse) {
      ErrorResponse err = (ErrorResponse) response;
      STALSignatureException se = new STALSignatureException(err.getErrorCode(), err.getErrorMessage());
      throw new SignatureException(se);
    } else {
      throw new SignatureException("Failed to access STAL.");
    }
  }

}
