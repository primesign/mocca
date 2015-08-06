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

import iaik.asn1.structures.AlgorithmID;
import iaik.cms.IaikProvider;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.buergerkarte.namespaces.securitylayer._1_2_3.ExcludedByteRangeType;
import at.gv.egiz.stal.HashDataInput;


/**
 * This security Provider is used to collect multiple sign Requests to create one Stal BulkRequest.
 * The related signature parameters are stored as a List of <code>BulkSignatureInfo</code>.
 * @author szoescher
 *
 */
public class BulkCollectionSecurityProvider extends IaikProvider {

  private final static Logger log = LoggerFactory.getLogger(BulkCollectionSecurityProvider.class);

  private String keyboxIdentifier;
  private List<HashDataInput> hashDataInput;
  private ExcludedByteRangeType excludedByteRange;
  
  private List<BulkSignatureInfo> bulkSignatureInfo;
  

	public BulkCollectionSecurityProvider() {
		bulkSignatureInfo = new LinkedList<BulkSignatureInfo>();
	}
	
	public BulkCollectionSecurityProvider(String keyboxIdentifier, HashDataInput hashDataInput,
			ExcludedByteRangeType excludedByteRange) {
 
		bulkSignatureInfo = new LinkedList<BulkSignatureInfo>();
		updateBulkCollectionSecurityProvider(keyboxIdentifier, hashDataInput, excludedByteRange);

	}

	public void updateBulkCollectionSecurityProvider(String keyboxIdentifier, HashDataInput hashDataInput,
			ExcludedByteRangeType excludedByteRange) {

		this.keyboxIdentifier = keyboxIdentifier;
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
    
    //Store signature information that is required to create a StalBulkSignatureRequest.
    bulkSignatureInfo.add(new BulkSignatureInfo(privateKey, signatureAlgorithm, keyboxIdentifier, signedAttributes,
        spk.getAlgorithm(), spk.getDigestAlgorithm(), hashDataInput, excludedByteRange));

      //TODO(SZ): How is size calculated/estimated?
      byte[] signaturePlaceholder = new byte[1024];
      return signaturePlaceholder;   
  }

public List<BulkSignatureInfo> getBulkSignatureInfo() {
	return bulkSignatureInfo;
}

  
  
  
 

}
