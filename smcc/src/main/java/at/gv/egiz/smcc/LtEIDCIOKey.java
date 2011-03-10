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

public class LtEIDCIOKey {

	private byte[] iD;
	private BigInteger keyReference;

	public LtEIDCIOKey(byte[] cio) throws IOException {

		ASN1 asn1 = new ASN1(cio);

		iD = asn1.getElementAt(1).getElementAt(0).gvByteArray();
		keyReference = asn1.getElementAt(1).getElementAt(3).gvBigInteger();
	}

	public byte[] getID() {
		return iD;
	}

	public void setID(byte[] id) {
		iD = id;
	}

	public BigInteger getKeyReference() {
		return keyReference;
	}

	public void setKeyReference(BigInteger keyReference) {
		this.keyReference = keyReference;
	}
}
