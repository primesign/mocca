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

import java.io.ByteArrayOutputStream;
import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.SignatureSpi;
import java.util.Collections;
import java.util.List;

import at.gv.egiz.stal.ErrorResponse;
import at.gv.egiz.stal.STAL;
import at.gv.egiz.stal.STALRequest;
import at.gv.egiz.stal.STALResponse;
import at.gv.egiz.stal.SignRequest;
import at.gv.egiz.stal.SignResponse;
import at.gv.egiz.stal.HashDataInputCallback;

/**
 * A signature service provider implementation that uses STAL to sign.
 * 
 * @author mcentner
 */
public class STALSignature extends SignatureSpi {

  /**
   * The private key.
   */
  protected STALPrivateKey privateKey;
  
  /**
   * The to-be signed data.
   */
  protected ByteArrayOutputStream data = new ByteArrayOutputStream();
  
  /* (non-Javadoc)
   * @see java.security.SignatureSpi#engineGetParameter(java.lang.String)
   */
  @Override
  protected Object engineGetParameter(String param)
      throws InvalidParameterException {
    throw new InvalidParameterException();
  }

  /* (non-Javadoc)
   * @see java.security.SignatureSpi#engineInitSign(java.security.PrivateKey)
   */
  @Override
  protected void engineInitSign(PrivateKey privateKey)
      throws InvalidKeyException {

    if (!(privateKey instanceof STALPrivateKey)) {
      throw new InvalidKeyException("STALSignature supports STALKeys only.");
    }
    
    this.privateKey = (STALPrivateKey) privateKey;

  }

  /* (non-Javadoc)
   * @see java.security.SignatureSpi#engineInitVerify(java.security.PublicKey)
   */
  @Override
  protected void engineInitVerify(PublicKey publicKey)
      throws InvalidKeyException {
    
    throw new UnsupportedOperationException("STALSignature does not support signature verification.");
  }

  /* (non-Javadoc)
   * @see java.security.SignatureSpi#engineSetParameter(java.lang.String, java.lang.Object)
   */
  @Override
  protected void engineSetParameter(String param, Object value)
      throws InvalidParameterException {
  }

  /* (non-Javadoc)
   * @see java.security.SignatureSpi#engineSign()
   */
  @Override
  protected byte[] engineSign() throws SignatureException {
   
    STAL stal = privateKey.getStal();
    
    if (stal == null) {
      throw new SignatureException("STALSignature requires the STALPrivateKey " +
      		"to provide a STAL implementation reference.");
    }
    
    HashDataInputCallback signRefDataSupplier = privateKey.getHashDataInputCallback();
    
    String keyboxIdentifier = privateKey.getKeyboxIdentifier();
    
    if (keyboxIdentifier == null) {
      throw new SignatureException("STALSignature requires the STALPrivateKey " + 
          "to provide a KeyboxIdentifier.");
    }
    
    SignRequest signRequest = new SignRequest();
    signRequest.setKeyIdentifier(keyboxIdentifier);
    signRequest.setSignedInfo(data.toByteArray());
    signRequest.setHashDataInput(signRefDataSupplier);
    
    List<STALResponse> responses = stal.handleRequest(Collections.singletonList((STALRequest) signRequest));
    
    if (responses == null || responses.size() != 1) {
      throw new SignatureException("Failed to access STAL.");
    }

    STALResponse response = responses.get(0);
    if (response instanceof SignResponse) {
      return ((SignResponse) response).getSignatureValue();
    } else if (response instanceof ErrorResponse) {
      throw new STALSignatureException(((ErrorResponse) response).getErrorCode());
    } else {
      throw new SignatureException("Failed to access STAL.");
    }

  }

  /* (non-Javadoc)
   * @see java.security.SignatureSpi#engineUpdate(byte)
   */
  @Override
  protected void engineUpdate(byte b) throws SignatureException {
    data.write(b);
  }

  /* (non-Javadoc)
   * @see java.security.SignatureSpi#engineUpdate(byte[], int, int)
   */
  @Override
  protected void engineUpdate(byte[] b, int off, int len)
      throws SignatureException {
    data.write(b, off, len);
  }

  /* (non-Javadoc)
   * @see java.security.SignatureSpi#engineVerify(byte[])
   */
  @Override
  protected boolean engineVerify(byte[] sigBytes) throws SignatureException {
    throw new UnsupportedOperationException("STALSignature des not support signature verification.");
  }

}
