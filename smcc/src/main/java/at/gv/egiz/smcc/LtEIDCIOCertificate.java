/*
* Copyright 2009 Federal Chancellery Austria and
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

package at.gv.egiz.smcc;

import iaik.me.asn1.ASN1;
import iaik.me.security.BigInteger;

import java.io.IOException;
import java.util.Arrays;

import at.gv.egiz.smcc.cio.CIOCertificate;

public class LtEIDCIOCertificate extends CIOCertificate {

	// The Lithuanian eID card stores both certificates in one file.
	// For each certificate, EF.CD contains an offset and a length that may be used
	// to extract the certificates from the file.
	private BigInteger offset;
	private byte[] length;
	
	public LtEIDCIOCertificate(byte[] cio) throws IOException {
		
		super(cio);
		
		ASN1 x509Certificate = new ASN1(cio);
		
        //read CONTEXTSPECIFIC manually
        byte[] ctxSpecific = x509Certificate.getElementAt(x509Certificate.getSize()-1).getEncoded();
        if ((ctxSpecific[0] & 0xff) == 0xa1) {
            int ll = ((ctxSpecific[1] & 0xf0) == 0x80)
                    ? (ctxSpecific[1] & 0x0f) + 2 : 2;
            ASN1 x509CertificateAttributes = new ASN1(Arrays.copyOfRange(ctxSpecific, ll, ctxSpecific.length));

            offset = x509CertificateAttributes.getElementAt(0).getElementAt(1).gvBigInteger();
            
            // first byte indicates number of relevant bytes in array 
            byte[] lengthValue = x509CertificateAttributes.getElementAt(0).getElementAt(2).gvByteArray();
            if(lengthValue == null || lengthValue[0] != lengthValue.length-1) {
            	
            	throw new IOException("Cannot extract certificate length information. Unexpected format.");
            }
            
            length = new byte[lengthValue[0]];
            System.arraycopy(lengthValue, 1, length, 0, lengthValue[0]);
            
        }		
	}

	public BigInteger getOffset() {
		return offset;
	}

	public void setOffset(BigInteger offset) {
		this.offset = offset;
	}

	public byte[] getLength() {
		return length;
	}

	public void setLength(byte[] length) {
		this.length = length;
	}	
}
