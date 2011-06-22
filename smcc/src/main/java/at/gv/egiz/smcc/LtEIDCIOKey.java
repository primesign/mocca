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
