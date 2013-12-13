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

import iaik.xml.crypto.dsig.AbstractSignatureMethodImpl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.crypto.XMLCryptoContext;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.crypto.dsig.spec.SignatureMethodParameterSpec;

import at.gv.egiz.bku.slcommands.impl.DataObjectHashDataInput;
import at.gv.egiz.bku.slexceptions.SLViewerException;
import at.gv.egiz.bku.utils.StreamUtil;
import at.gv.egiz.stal.ErrorResponse;
import at.gv.egiz.stal.HashDataInput;
import at.gv.egiz.stal.STAL;
import at.gv.egiz.stal.STALRequest;
import at.gv.egiz.stal.STALResponse;
import at.gv.egiz.stal.SignRequest;
import at.gv.egiz.stal.SignRequest.SignedInfo;
import at.gv.egiz.stal.SignResponse;

public class STALSignatureMethod extends AbstractSignatureMethodImpl {
  
  /**
   * Creates a new instance of this <code>STALSignatureMethod</code>
   * with the given <code>algorithm</code> and <code>params</code>.
   * 
   * @param algorithm the algorithm URI
   * @param params optional algorithm parameters
   * @throws InvalidAlgorithmParameterException if the specified parameters 
   * are inappropriate for the requested algorithm
   * @throws NoSuchAlgorithmException if an implementation of the specified 
   * algorithm cannot be found
   * @throws NullPointerException if <code>algorithm</code> is <code>null</code>  
   */
  public STALSignatureMethod(String algorithm,
      SignatureMethodParameterSpec params)
      throws InvalidAlgorithmParameterException, NoSuchAlgorithmException {
    super(algorithm, params);
  }

  @Override
  public byte[] calculateSignatureValue(XMLCryptoContext context, Key key, InputStream message)
      throws XMLSignatureException, IOException {
    
    if (!(key instanceof STALPrivateKey)) {
      throw new XMLSignatureException("STALSignatureMethod expects STALPrivateKey.");
    }

    STAL stal = ((STALPrivateKey) key).getStal();
    String keyboxIdentifier = ((STALPrivateKey) key).getKeyboxIdentifier();
    List<DataObject> dataObjects = ((STALPrivateKey) key).getDataObjects();
    
    List<HashDataInput> hashDataInputs = new ArrayList<HashDataInput>();
    for (DataObject dataObject : dataObjects) {
      try {
        dataObject.validateHashDataInput();
      } catch (SLViewerException e) {
        throw new XMLSignatureException(e);
      }
      hashDataInputs.add(new DataObjectHashDataInput(dataObject));
    }
    
    ByteArrayOutputStream m = new ByteArrayOutputStream();
    StreamUtil.copyStream(message, m);
    
    SignRequest signRequest = new SignRequest();
    signRequest.setKeyIdentifier(keyboxIdentifier);
    SignedInfo signedInfo = new SignedInfo();
    signedInfo.setValue(m.toByteArray());
    signRequest.setSignedInfo(signedInfo);
    signRequest.setHashDataInput(hashDataInputs);

    List<STALResponse> responses = 
      stal.handleRequest(Collections.singletonList((STALRequest) signRequest));
    
    if (responses == null || responses.size() != 1) {
      throw new XMLSignatureException("Failed to access STAL.");
    }

    STALResponse response = responses.get(0);
    if (response instanceof SignResponse) {
      return ((SignResponse) response).getSignatureValue();
    } else if (response instanceof ErrorResponse) {
      ErrorResponse err = (ErrorResponse) response;
      STALSignatureException se = new STALSignatureException(err.getErrorCode(), err.getErrorMessage());
      throw new XMLSignatureException(se);
    } else {
      throw new XMLSignatureException("Failed to access STAL.");
    }
    
  }

  @Override
  public boolean validateSignatureValue(XMLCryptoContext context, Key key, byte[] value,
      InputStream message) throws XMLSignatureException, IOException {
    throw new XMLSignatureException("The STALSignatureMethod does not support validation.");
  }

  @Override
  protected Class<?> getParameterSpecClass() {
    return null;
  }
  
}
