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

import at.gv.egiz.smcc.cio.CIOCertificate;
import at.gv.egiz.smcc.cio.ObjectDirectory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.ResponseAPDU;
import javax.smartcardio.CommandAPDU;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.gv.egiz.smcc.pin.gui.PINGUI;
import at.gv.egiz.smcc.util.SMCCHelper;

public class ESDNIeCard extends AbstractSignatureCard implements SignatureCard {

	public static final byte[] MASTER_FILE_ID = new byte[] {

	(byte) 0x4D, (byte) 0x61, (byte) 0x73, (byte) 0x74, (byte) 0x65,
			(byte) 0x72, (byte) 0x2E, (byte) 0x46, (byte) 0x69, (byte) 0x6C,
			(byte) 0x65 };

	private final byte[] HASH_PADDING = new byte[] {

	(byte) 0x30, (byte) 0x21, (byte) 0x30, (byte) 0x09, (byte) 0x06,
			(byte) 0x05, (byte) 0x2B, (byte) 0x0E, (byte) 0x03, (byte) 0x02,
			(byte) 0x1A, (byte) 0x05, (byte) 0x00, (byte) 0x04, (byte) 0x14 };

	private final String SIG_KEY_NAME = "KprivFirmaDigital";
	private final String SIG_CERT_NAME = "CertFirmaDigital";

	private final Logger log = LoggerFactory.getLogger(ESDNIeCard.class);

	protected PinInfo pinInfo = new PinInfo(8, 16,
			"[0-9A-Za-z_<>!()?%\\-=&+\\.]", "at/gv/egiz/smcc/ESDNIeCard",
			"sig.pin", (byte) 0x00, new byte[] {}, PinInfo.UNKNOWN_RETRIES);


	@Override
	protected CardChannel getCardChannel() {
		
		// set up a new secure channel each time
		return new DNIeSecuredChannel(getCard().getBasicChannel());
	}
	
	@Override
	@Exclusive
	public byte[] createSignature(InputStream input, KeyboxName keyboxName,
			PINGUI pinGUI, String alg) throws SignatureCardException,
			InterruptedException, IOException {

		CardChannel channel = getCardChannel();

		try {
			
			// Select MF
			executeSelectMasterFile(channel);

			// Select DF.CIA
			executeSelectDFCIA(channel);

			ObjectDirectory efOd = new ObjectDirectory();
			efOd.selectAndRead(channel);

			DNIeCIOCertificateDirectory efPrkd = new DNIeCIOCertificateDirectory(
					efOd.getPrKDReferences().get(0));
			efPrkd.selectAndRead(channel);

			byte[] efKey = null;
			for (CIOCertificate cioCertificate : efPrkd.getCIOs()) {
				String label = cioCertificate.getLabel();
				if (label != null
						&& label.toLowerCase().contains(
								SIG_KEY_NAME.toLowerCase())) {
					efKey = cioCertificate.getEfidOrPath();
				}
			}

			// Check PIN
			// CommandAPDU c = new CommandAPDU((byte) 0x00, (byte) 0x20,
			// (byte) 0x00, (byte) 0x00);
			// ResponseAPDU r = channel.transmit(c);
			// log.debug("Answer to PIN Check: "
			// + SMCCHelper.toString(r.getBytes()));

			if (efKey == null) {
				throw new NotActivatedException();
			}
			
			verifyPINLoop(channel, pinInfo, pinGUI);

			if (efKey != null && efKey.length >= 2) {

				byte[] keyId = new byte[2];
				keyId[0] = efKey[efKey.length - 2];
				keyId[1] = efKey[efKey.length - 1];

				executeManageSecurityEnvironment(channel, keyId);
			} else {

				throw new CardException(
						"Unable to determine valid key path. Key path either null or unexpected length.");
			}

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

			return executeCreateSignature(channel, digest);

		} catch (CardException e) {

			log.error("Error during signature creation.", e);
			throw new SignatureCardException(
					"Error creating signature with DNIe card.", e);
		}
		
	}

	@Override
	@Exclusive
	public byte[] getCertificate(KeyboxName keyboxName, PINGUI pinGUI)
			throws SignatureCardException, InterruptedException {

		byte[] result = null;
		
		CardChannel channel = getCardChannel();

		byte[] certId = null;

		try {
			
			// Select MF
			executeSelectMasterFile(channel);

			// Select DF.CIA
			executeSelectDFCIA(channel);

			byte[] efQcert = null;

			ObjectDirectory efOd = new ObjectDirectory();
			efOd.selectAndRead(channel);

			DNIeCIOCertificateDirectory efCd = new DNIeCIOCertificateDirectory(
					efOd.getCDReferences().get(0));

			try {
				efCd.selectAndRead(channel);
			} catch (IOException e) {

				throw new CardException("Error retrieving certificate path. ",
						e);
			}

			for (CIOCertificate cioCertificate : efCd.getCIOs()) {
				String label = cioCertificate.getLabel();
				if (label != null
						&& label.toLowerCase().contains(
								SIG_CERT_NAME.toLowerCase())) {
					efQcert = cioCertificate.getEfidOrPath();
				}
			}

			if (efQcert == null) {
				throw new NotActivatedException();
			}

			if (efQcert.length == 4) {

				certId = efQcert;
			} else {

				throw new CardException(
						"Unable to determine valid certificate path. Cert path has unexpected length.");
			}

			// verify PIN to be able to read certificate
			verifyPINLoop(channel, pinInfo, pinGUI);

			// select master file
			executeSelectMasterFile(channel);

			// select certificate path
			executeSelect(channel, new byte[] { certId[0], certId[1] });
			byte[] fci = executeSelect(channel, new byte[] { certId[2],
					certId[3] });

			byte sizeHi = fci[7];
			byte sizeLo = fci[8];

			byte[] data = executeReadBinary(channel, sizeHi, sizeLo);

			byte[] compressedWithoutHeader = new byte[data.length - 8];
			System.arraycopy(data, 8, compressedWithoutHeader, 0,
					compressedWithoutHeader.length);

			result = decompressData(compressedWithoutHeader);
			
		} catch (CardException e) {

			log.error("Error reading certificate from card.", e);
			throw new SignatureCardException(
					"Error reading certificate from card.", e);
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

		ResponseAPDU resp = channel.transmit(new CommandAPDU(apdu));
		int result = resp.getSW();

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

	private void executeSelectMasterFile(CardChannel channel)
			throws CardException {

		byte[] apdu = new byte[MASTER_FILE_ID.length + 5];
		apdu[0] = (byte) 0x00;
		apdu[1] = (byte) 0xA4;
		apdu[2] = (byte) 0x04;
		apdu[3] = (byte) 0x00;
		apdu[4] = (byte) MASTER_FILE_ID.length;
		System.arraycopy(MASTER_FILE_ID, 0, apdu, 5, MASTER_FILE_ID.length);

		CommandAPDU command = new CommandAPDU(apdu);
		ResponseAPDU resp = channel.transmit(command);

		if (resp.getSW() != 0x9000) {

			log.error("Error selecting master file: "
					+ Integer.toHexString(resp.getSW()));
			throw new CardException("Error selecting master file: "
					+ Integer.toHexString(resp.getSW()));
		}
	}

	private void executeSelectDFCIA(CardChannel channel) throws CardException {

		executeSelect(channel, new byte[] { (byte) 0x50, (byte) 0x015 });
	}

	private byte[] executeSelect(CardChannel channel, byte[] id)
			throws CardException {

		byte[] apduHeader = new byte[] {

		(byte) 0x00, (byte) 0xA4, (byte) 0x00, (byte) 0x00, (byte) id.length };

		byte[] apdu = new byte[apduHeader.length + id.length];
		System.arraycopy(apduHeader, 0, apdu, 0, apduHeader.length);
		System.arraycopy(id, 0, apdu, apduHeader.length, id.length);

		CommandAPDU command = new CommandAPDU(apdu);
		ResponseAPDU resp = channel.transmit(command);

		if (resp.getSW() != 0x9000) {

			log.error("error selecting file " + SMCCHelper.toString(id) + ": "
					+ Integer.toHexString(resp.getSW()));
			throw new CardException("Error selecting file "
					+ SMCCHelper.toString(id) + ": "
					+ Integer.toHexString(resp.getSW()));
		}

		return resp.getData();
	}

	private byte[] executeReadBinary(CardChannel channel, byte lengthHi,
			byte lengthLo) throws CardException {

		ByteArrayOutputStream bof = new ByteArrayOutputStream();

		int bytes2read = (lengthHi * 256) + lengthLo;
		int bytesRead = 0;

		boolean done = false;

		int offset = 0;
		int len = 0;

		while (!done) {

			if (bytes2read - bytesRead > 0xef) {
				len = 0xef;
			} else {
				len = bytes2read - bytesRead;
			}

			byte[] offsetBytes = SMCCHelper.toByteArray(offset);
			ResponseAPDU resp = readFromCard(channel, offsetBytes[0],
					offsetBytes[1], (byte) len);

			if (resp.getSW1() == (byte) 0x6C) {

				// handle case: card returns 6CXX (wrong number of bytes
				// requested)

				resp = readFromCard(channel, offsetBytes[0], offsetBytes[1],
						(byte) resp.getSW2());

				// this has to be the final iteration
				done = true;
			}

			try {
				bof.write(resp.getData());
			} catch (IOException e) {
				log.error("Error executing secure read binary.", e);
				throw new CardException("Error reading data from card", e);
			}

			bytesRead = bytesRead + resp.getData().length;
			offset = bytesRead;

			if (bytesRead == bytes2read) {

				done = true;
			}
		}

		return bof.toByteArray();
	}

	private void executeManageSecurityEnvironment(CardChannel channel, byte[] id)
			throws CardException {

		byte[] apdu = new byte[7 + 2];
		apdu[0] = (byte) 0x00;
		apdu[1] = (byte) 0x22;
		apdu[2] = (byte) 0x41;
		apdu[3] = (byte) 0xB6;
		apdu[4] = (byte) (2 + 2);
		apdu[5] = (byte) 0x84;
		apdu[6] = (byte) 0x02;
		apdu[7] = id[0];
		apdu[8] = id[1];

		CommandAPDU command = new CommandAPDU(apdu);
		ResponseAPDU resp = channel.transmit(command);

		if (resp.getSW() != 0x9000) {

			log.error("Error executing Manage Security Environment: "
					+ Integer.toHexString(resp.getSW()));
			throw new CardException(
					"Execution of command Manage Security Environment failed: "
							+ Integer.toHexString(resp.getSW()));
		}

	}

	private byte[] executeCreateSignature(CardChannel channel, byte[] data)
			throws CardException {

		byte[] apdu = new byte[5 + HASH_PADDING.length + data.length + 1];
		apdu[0] = (byte) 0x00;
		apdu[1] = (byte) 0x2A;
		apdu[2] = (byte) 0x9E;
		apdu[3] = (byte) 0x9A;
		apdu[4] = (byte) (HASH_PADDING.length + data.length);

		System.arraycopy(HASH_PADDING, 0, apdu, 5, HASH_PADDING.length);
		System.arraycopy(data, 0, apdu, 5 + HASH_PADDING.length, data.length);

		apdu[apdu.length - 1] = (byte) 0x80;

		CommandAPDU command = new CommandAPDU(apdu);
		ResponseAPDU resp = channel.transmit(command);

		if (resp.getSW() != 0x9000) {

			log.error("Error computing electronic signature on card: "
					+ Integer.toHexString(resp.getSW()));
			throw new CardException("Unexpected response from card: "
					+ Integer.toHexString(resp.getSW()));
		}

		return resp.getData();

	}

	private ResponseAPDU readFromCard(CardChannel channel, byte offsetHi,
			byte offsetLo, byte numBytes) throws CardException {

		byte[] apdu = new byte[] {

		(byte) 0x00, (byte) 0xB0, offsetHi, offsetLo, numBytes };

		CommandAPDU command = new CommandAPDU(apdu);
		ResponseAPDU resp = channel.transmit(command);

		return resp;

	}

	private byte[] decompressData(byte[] input) throws CardException {

		Inflater decompresser = new Inflater();
		decompresser.setInput(input, 0, input.length);
		byte[] buffer = new byte[256];
		ByteArrayOutputStream bos = new ByteArrayOutputStream();

		try {
			while (!decompresser.finished()) {

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
