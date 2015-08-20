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

import java.security.PrivateKey;
import java.util.List;

import at.buergerkarte.namespaces.securitylayer._1_2_3.ExcludedByteRangeType;
import at.gv.egiz.stal.HashDataInput;


/**
 * 
 * @author szoescher
 *
 */
public class BulkSignatureInfo {

	AlgorithmID signatureAlgorithm;

	String keyboxIdentifier;
	
    byte[] signedAttributes;
    
    String signatureMethod;
    
    String digestMethod;
    
    List<HashDataInput> hashDataInput;
    
    ExcludedByteRangeType excludedByteRange;
    
    PrivateKey privateKey;

	public BulkSignatureInfo(PrivateKey privateKey, AlgorithmID signatureAlgorithm, String keyboxIdentifier,
			byte[] signedAttributes, String signatureMethod, String digestMethod, List<HashDataInput> hashDataInput,
			ExcludedByteRangeType excludedByteRange) {
		this.privateKey = privateKey;
		this.signatureAlgorithm = signatureAlgorithm;
		this.keyboxIdentifier = keyboxIdentifier;
		this.signedAttributes = signedAttributes;
		this.signatureMethod = signatureMethod;
		this.digestMethod = digestMethod;
		this.hashDataInput = hashDataInput;
		this.excludedByteRange = excludedByteRange;
	}

	public String getKeyboxIdentifier() {
		return keyboxIdentifier;
	}

	public byte[] getSignedAttributes() {
		return signedAttributes;
	}

	public String getSignatureMethod() {
		return signatureMethod;
	}

	public String getDigestMethod() {
		return digestMethod;
	}

	public List<HashDataInput> getHashDataInput() {
		return hashDataInput;
	}

	public ExcludedByteRangeType getExcludedByteRange() {
		return excludedByteRange;
	}

	public AlgorithmID getSignatureAlgorithm() {
		return signatureAlgorithm;
	}

	public PrivateKey getPrivateKey() {
		return privateKey;
	}
	
	
	
		
}
