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

import java.io.IOException;
import java.util.Arrays;

import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

import at.gv.egiz.smcc.util.TLV;

public abstract class AbstractISCard extends AbstractSignatureCard implements
		SignatureCard {

//	private final Logger log = LoggerFactory.getLogger(AbstractISCard.class);

	protected static final byte[] OID = new byte[] { (byte) 0x30, (byte) 0x21, (byte) 0x30,
			(byte) 0x09, (byte) 0x06, (byte) 0x05, (byte) 0x2b,
			(byte) 0x0e, (byte) 0x03, (byte) 0x02, (byte) 0x1a,
			(byte) 0x05, (byte) 0x00, (byte) 0x04, (byte) 0x14 };
	
	protected abstract byte[] getAppletID();

	protected void selectApplet(CardChannel channel) throws CardException,
			SignatureCardException {

		CommandAPDU apdu = new CommandAPDU((byte) 0x00, (byte) 0xA4,
				(byte) 0x04, (byte) 0x00, getAppletID());

		ResponseAPDU resp = channel.transmit(apdu);

		if (resp.getSW() != 0x9000) {

			throw new SignatureCardException(
					"Error selecting card applet. Unexpected response from card: "
							+ Integer.toHexString(resp.getSW()));
		}
	}

	protected int toInt(byte[] array) {

		int len = array.length;
		int result = 0;

		for (int i = len - 1; i >= 0; i--) {

			int currentByte = (int)array[i];
			currentByte = currentByte < 0 ? currentByte+256 : currentByte;
			
			result = result + (int)(currentByte * Math.pow(256, len - i - 1));		
		}

		return result;
	}

	protected byte[] retrieveSigningCertificate(byte[] certData, byte[] certsMetaInfo, String identifier) throws SignatureCardException, IOException {
			
		byte[] cert = null;
		
		ASN1 meta1 = new ASN1(certsMetaInfo);
		int meta1Length = meta1.getEncoded().length;

		byte[] meta2Data = new byte[certsMetaInfo.length - meta1Length];
		System.arraycopy(certsMetaInfo, meta1Length, meta2Data, 0,
				meta2Data.length);
		ASN1 meta2 = new ASN1(meta2Data);

		if (meta1.getElementAt(0).getElementAt(0).gvString()
				.contains(identifier)) {

			cert = retrieveCertFromFile(certData, meta1);
		} else if (meta2.getElementAt(0).getElementAt(0).gvString()
				.contains(identifier)) {

			cert = retrieveCertFromFile(certData, meta2);
		} else {

			throw new SignatureCardException(
					"Cannot find certificate meta information.");
		}
		
		return cert;
	}
	
	protected byte[] retrieveCertFromFile(byte[] certsData, ASN1 metaInfo)
			throws SignatureCardException {

		byte[] cert = null;

		byte[] contextSpecificData;
		try {
			contextSpecificData = metaInfo.getElementAt(metaInfo.getSize() - 1)
					.getEncoded();

			if ((contextSpecificData[0] & 0xff) == 0xa1) {
				int ll = ((contextSpecificData[1] & 0xf0) == 0x80) ? (contextSpecificData[1] & 0x0f) + 2
						: 2;
				ASN1 info = new ASN1(Arrays.copyOfRange(contextSpecificData,
						ll, contextSpecificData.length));

				int offset = info.getElementAt(0).getElementAt(1).gvInt();
				byte[] contextSpecific = info.getElementAt(0).getElementAt(2)
						.getEncoded();
				int length = toInt(new TLV(contextSpecific, 0).getValue());

				cert = new byte[length];

				System.arraycopy(certsData, offset, cert, 0, length);
			} else {

				throw new SignatureCardException(
						"Cannot retrieve enduser certificate.");
			}

		} catch (IOException e) {

			throw new SignatureCardException(
					"Cannot retrieve enduser certificate.", e);
		}

//		if (cert == null ) {
//			log.error("Retrieved certificate is null.");
//			throw new SignatureCardException(
//					"Cannot retrieve enduser certificate.");
//		}

		return cert;
	}
}
