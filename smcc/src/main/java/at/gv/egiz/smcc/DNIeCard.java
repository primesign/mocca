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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.gv.egiz.smcc.pin.gui.PINGUI;

public class DNIeCard extends AbstractSignatureCard implements SignatureCard {

	private final Logger log = LoggerFactory.getLogger(DNIeCard.class);

	private final byte[] MASTER_FILE_ID = new byte[] {

	(byte) 0x4D, (byte) 0x61, (byte) 0x73, (byte) 0x74, (byte) 0x65,
			(byte) 0x72, (byte) 0x2E, (byte) 0x46, (byte) 0x69, (byte) 0x6C,
			(byte) 0x65 };

	private final String SIG_KEY_NAME = "KprivFirmaDigital";
	private final String SIG_CERT_NAME = "CertFirmaDigital";
	
	protected PinInfo pinInfo = new PinInfo(8, 16,
			"[0-9A-Za-z_<>!()?%\\-=&+\\.]", "at/gv/egiz/smcc/DNIeCard",
			"sig.pin", (byte) 0x00, new byte[] {}, PinInfo.UNKNOWN_RETRIES);

	DNIeCardSecureChannel secureChannel;

	public DNIeCard() {
		this.secureChannel = new DNIeCardSecureChannel();

	}

	@Override
	public byte[] createSignature(InputStream input, KeyboxName keyboxName,
			PINGUI pinGUI, String alg) throws SignatureCardException,
			InterruptedException, IOException {

		CardChannel channel = getCardChannel();

		if (!secureChannel.isEstablished())
			try {
				secureChannel.establish(channel);
			} catch (CardException e) {

				log.debug("Error establishing secure channel to card.", e);
			}

		try {
			
			byte[] prKdf = executeReadPrKDF(channel);
			byte[] keyId = getKeyIdFromASN1File(prKdf);
			
			verifyPINLoop(channel, pinInfo, pinGUI);

			secureChannel.executeSecureManageSecurityEnvironment(channel, keyId);

			MessageDigest md;
			try {
				md = MessageDigest.getInstance("SHA-1");
			} catch (NoSuchAlgorithmException e) {
				log.error("Failed to get MessageDigest.", e);
				throw new SignatureCardException(e);
			}
			// calculate message digest
			byte[] digest = new byte[md.getDigestLength()];
			for (int l; (l = input.read(digest)) != -1;) {
				md.update(digest, 0, l);
			}
			digest = md.digest();

			return secureChannel.executeSecureCreateSignature(channel, digest);

		} catch (CardException e) {

			throw new SignatureCardException(
					"Error creating signature with DNIe card.", e);
		}

	}

	@Override
	public byte[] getCertificate(KeyboxName keyboxName, PINGUI provider)
			throws SignatureCardException, InterruptedException {

		byte[] result = null;

		CardChannel channel = getCardChannel();

		if (!secureChannel.isEstablished()) {
			try {
				secureChannel.establish(channel);
			} catch (CardException e) {

				log.debug("Error establishing secure channel to card.", e);
			}
		}

		byte[] certId = null;
		
		try {
			// read CDF file
			byte[] cdf = executeReadCDF(channel);
			
			// extract certificate id from ASN1 data structure
			certId = getCertIdFromASN1File(cdf);
			
		} catch (CardException e1) {

			log.error("Error reading ASN.1 data!");
			e1.printStackTrace();
		}
						
		log.debug("Try to read certificate..");
		try {

			verifyPINLoop(channel, pinInfo, provider);

			// select master file
			executeSecureSelectMasterFile(channel);

			// select 6081
			byte[] apdu = new byte[] {

			(byte) 0x00, (byte) 0xA4, (byte) 0x00, (byte) 0x00, (byte) 0x02,
					(byte) 0x60, (byte) 0x81 };

			secureChannel.executeSecureSelect(channel, apdu);

			// select cert id
			byte[] apdu2 = new byte[] {

			(byte) 0x00, (byte) 0xA4, (byte) 0x00, (byte) 0x00, (byte) 0x02,
			certId[certId.length-2], certId[certId.length-1] };

			byte[] fci = secureChannel.executeSecureSelect(channel, apdu2);

			byte sizeHi = fci[7];
			byte sizeLo = fci[8];

			byte[] data = secureChannel.executeSecureReadBinary(channel,
					sizeHi, sizeLo);

			byte[] compressedWithoutHeader = new byte[data.length - 8];
			System.arraycopy(data, 8, compressedWithoutHeader, 0,
					compressedWithoutHeader.length);

			result = decompressData(compressedWithoutHeader);

		} catch (CardException e) {

			throw new SignatureCardException("Error getting certificate.", e);
		}

		return result;
	}

	@Override
	public byte[] getInfobox(String infobox, PINGUI pinGUI, String domainId)
			throws SignatureCardException, InterruptedException {

		log.debug("Attempting to read infobox from DNIe..");

		throw new IllegalArgumentException("Infobox '" + infobox
				+ "' not supported.");

	}

	protected void verifyPINLoop(CardChannel channel, PinInfo spec,
			PINGUI provider) throws LockedException, NotActivatedException,
			SignatureCardException, InterruptedException, CardException {

		int retries = -1;
		do {
			retries = verifyPIN(channel, spec, provider, retries);
		} while (retries > 0);
	}

	protected int verifyPIN(CardChannel channel, PinInfo pinSpec,
			PINGUI provider, int retries) throws SignatureCardException,
			LockedException, NotActivatedException, InterruptedException,
			CardException {

		char[] pin = provider.providePIN(pinSpec, retries);

		byte[] apdu = new byte[5 + pin.length];
		apdu[0] = (byte) 0x00;
		apdu[1] = (byte) 0x20;
		apdu[2] = (byte) 0x00;
		apdu[3] = (byte) 0x00;
		apdu[4] = (byte) pin.length;

		for (int i = 0; i < pin.length; i++) {

			apdu[i + 5] = (byte) pin[i];
		}

		int result = secureChannel.executeSecurePINVerify(channel, apdu);

		if (result == 0x9000) {
			return -1;
		}
		if (result >> 4 == 0x63c) {
			return 0x0f & result;
		}

		switch (result) {
		case 0x6983:
			// authentication method blocked
			throw new LockedException();

		default:
			String msg = "VERIFY failed. SW=" + Integer.toHexString(result);
			log.info(msg);
			throw new SignatureCardException(msg);
		}
	}

	private void executeSecureSelectMasterFile(CardChannel channel)
			throws CardException {

		byte[] apdu = new byte[MASTER_FILE_ID.length + 5];
		apdu[0] = (byte) 0x00;
		apdu[1] = (byte) 0xA4;
		apdu[2] = (byte) 0x04;
		apdu[3] = (byte) 0x00;
		apdu[4] = (byte) MASTER_FILE_ID.length;
		System.arraycopy(MASTER_FILE_ID, 0, apdu, 5, MASTER_FILE_ID.length);

		secureChannel.executeSecureSelect(channel, apdu);
	}

	private byte[] executeReadCDF(CardChannel channel) throws CardException {
		
		return executeReadFile(channel, new byte[]{(byte)0x50,(byte)0x15,(byte)0x60,(byte)0x04});		
	}	
	
	private byte[] executeReadPrKDF(CardChannel channel) throws CardException {
		
		return executeReadFile(channel, new byte[]{(byte)0x50,(byte)0x15,(byte)0x60,(byte)0x01});
	}
	
	private byte[] getKeyIdFromASN1File(byte[] file) throws CardException {

		// split file in two records
		int record1Length = getRecordLength(file, 1);
		
		byte[] record1 = new byte[record1Length];
		byte[] record2 = new byte[file.length - record1.length];
		
		System.arraycopy(file, 0, record1, 0, record1.length);
		System.arraycopy(file, record1.length, record2, 0, record2.length);
		
		byte[] keyId = new byte[2];
		
		try {
			ASN1 asn1_1 = new ASN1(record1);
			ASN1 asn1_2 = new ASN1(record2);

			if(asn1_1.getElementAt(0).getElementAt(0).gvString().equalsIgnoreCase(SIG_KEY_NAME)) {
				
				byte[] data = asn1_1.getElementAt(2).gvByteArray();
				
				keyId[0] = data[9];
				keyId[1] = data[10];
			}

			else if(asn1_2.getElementAt(0).getElementAt(0).gvString().equalsIgnoreCase(SIG_KEY_NAME)) {
				
				byte[] data = asn1_2.getElementAt(2).gvByteArray();
				
				keyId[0] = data[9];
				keyId[1] = data[10];
			}
			
		} catch (Exception e) {

			throw new CardException("Error getting ASN1 data.", e);
		}		
		
		return keyId;
	}
	
	private byte[] getCertIdFromASN1File(byte[] file) throws CardException {

		int record1Length = getRecordLength(file, 1);
		
		// split file in two records
		byte[] record1 = new byte[record1Length];
		byte[] record2 = new byte[file.length - record1.length];
		
		System.arraycopy(file, 0, record1, 0, record1.length);
		System.arraycopy(file, record1.length, record2, 0, record2.length);
		
		byte[] certId = null;
		
		try {
			ASN1 asn1_1 = new ASN1(record1);
			ASN1 asn1_2 = new ASN1(record2);
	
			if(asn1_1.getElementAt(0).getElementAt(0).gvString().equalsIgnoreCase(SIG_CERT_NAME)) {
			
				certId = retrieveCertId(asn1_1.getElementAt(2).gvByteArray());				
			}
			
			if(asn1_2.getElementAt(0).getElementAt(0).gvString().equalsIgnoreCase(SIG_CERT_NAME)) {
				
				certId = retrieveCertId(asn1_2.getElementAt(2).gvByteArray());			
			}
			
		} catch (Exception e) {

			throw new CardException("Error getting ASN1 data.", e);
		}	
	
		return certId;
	}	
	
	private byte[] retrieveCertId(byte[] data) throws CardException {
		
		ASN1 contextSpecific = getASN1WithinContextSpecific(data);
		try {
			return contextSpecific.getElementAt(0).getElementAt(0).gvByteArray();
		} catch (IOException e) {
			throw new CardException("Error retrieving certificate ID from ASN1 data.", e);
		}
	}
	
	private ASN1 getASN1WithinContextSpecific(byte[] data) throws CardException {
		
		byte first = data[0];
		byte lengthOfLength = 0;
		
		if(first < 0) {
			
			lengthOfLength = (byte)(first & (byte)0x7F);
			lengthOfLength = (byte)(lengthOfLength +1);
		} else {
			
			lengthOfLength = 1;
		}
		
		byte[] asn1data = new byte[data.length - lengthOfLength];
		System.arraycopy(data, lengthOfLength, asn1data, 0, asn1data.length);
				
		try {
			return new ASN1(asn1data);
		} catch (IOException e) {
			throw new CardException("Error getting ASN1 structure.", e);
		}
	}	
	
	private int getRecordLength(byte[] data, int startOfLength) {
		
		byte lengthStartByte = data[startOfLength];
		
		if(lengthStartByte < 0) {
			// we have more than one length byte
			byte lengthOfLength = (byte)(lengthStartByte & (byte)0x7F);
			
			byte[] lengthValues = new byte[lengthOfLength];			
			System.arraycopy(data, startOfLength+1, lengthValues, 0, lengthOfLength);
			
			int result = 0;
			
			for(int i=0; i<lengthValues.length; i++) {
				
				result = (result + byteToInt(lengthValues[lengthValues.length-1-i]) * (int)Math.pow(256, i));
			}
			
			return result + startOfLength + lengthOfLength + 1; // defined length + tag byte + length bytes
			
		} else {
			
			return (int)lengthStartByte + startOfLength + 1; // defined length + tag byte + length byte
		}
		
	}
	
	private int byteToInt(byte b) {
		
		return b < 0 ? b + 256 : b;
		
	}
	
	private byte[] executeReadFile(CardChannel channel, byte[] path) throws CardException {
		
		log.debug("Executing secure read File command..");
		
		executeSecureSelectMasterFile(channel);
		
		// Select DF 
		byte[] apdu_1 = new byte[] {
				
				(byte)0x00,   	// CLA 
				(byte)0xA4, 	// INS
				(byte)0x00, 	// P1
				(byte)0x00, 	// P2
				(byte)0x02, 	// Lc
				path[0], 
				path[1] 
		};
		
		secureChannel.executeSecureSelect(channel, apdu_1);
		
		// Select EF 
		byte[] apdu_2 = new byte[] {
				
				(byte)0x00,   	// CLA 
				(byte)0xA4, 	// INS
				(byte)0x00, 	// P1
				(byte)0x00, 	// P2
				(byte)0x02, 	// Lc
				path[2], 
				path[3] 
		};
		
		byte[] fci = secureChannel.executeSecureSelect(channel, apdu_2);		
		byte[] file = secureChannel.executeSecureReadBinary(channel, fci[7], fci[8]);		
		
		return file;		
	}	

	private byte[] decompressData(byte[] input) throws CardException {

		Inflater decompresser = new Inflater();
		decompresser.setInput(input, 0, input.length);
		byte[] buffer = new byte[256];
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		
		try {
			while(!decompresser.finished()) {
			
				int numBytes = decompresser.inflate(buffer);
				bos.write(buffer, 0, numBytes);
			}

			decompresser.end();
			
		} catch (DataFormatException e) {

			throw new CardException("Error decompressing file.", e);
		}

		return bos.toByteArray();
	}
}
