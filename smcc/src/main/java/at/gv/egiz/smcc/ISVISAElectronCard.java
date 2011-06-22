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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.gv.egiz.smcc.pin.gui.PINGUI;
import at.gv.egiz.smcc.util.SMCCHelper;

public class ISVISAElectronCard extends AbstractISCard implements SignatureCard {

	private static final byte[] APPLET_ID = new byte[] { (byte) 0xD2,
			(byte) 0x76, (byte) 0x00, (byte) 0x00, (byte) 0x98, (byte) 0x00,
			(byte) 0x00, (byte) 0x00 };

	private static final byte[] PKI_PROFILE = new byte[] { (byte) 0xA0,
			(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x63, (byte) 0x50,
			(byte) 0x4B, (byte) 0x43, (byte) 0x53, (byte) 0x2D, (byte) 0x31,
			(byte) 0x35 };

	private static final String CERTIFICATE_IDENTIFIER = "Undirritun";

	private static final int TRANSFER_BLOCK_SIZE = 0x80;

	private static final PinInfo PIN_SPEC = new PinInfo(6, 6, "[0-9]",
			"at/gv/egiz/smcc/ISVISAElectronCard", "sig.pin", (byte) 0x04,
			new byte[] {}, PinInfo.UNKNOWN_RETRIES);

	private final Logger log = LoggerFactory
			.getLogger(ISVISAElectronCard.class);

	@Override
	@Exclusive
	public byte[] getInfobox(String infobox, PINGUI pinGUI, String domainId)
			throws SignatureCardException, InterruptedException {

		throw new IllegalArgumentException("Infobox '" + infobox
				+ "' not supported.");
	}

	@Override
	@Exclusive
	public byte[] createSignature(InputStream input, KeyboxName keyboxName,
			PINGUI pinGUI, String alg) throws SignatureCardException,
			InterruptedException, IOException {

		CardChannel channel = getCardChannel();

		try {

			selectApplet(channel);
			selectPKIProfile(channel);

			// VERIFY PIN
			verifyPINLoop(channel, PIN_SPEC, pinGUI);

			setMSE(channel);

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

			return executeSecurityOperation(channel, digest);

		} catch (CardException e) {
			e.printStackTrace();
			throw new SignatureCardException("Error creating signature.", e);
		}
	}

	protected void verifyPINLoop(CardChannel channel, PinInfo spec,
			PINGUI provider) throws LockedException, NotActivatedException,
			SignatureCardException, InterruptedException, CardException {

		int retries = -1;
		do {
			retries = verifyPIN(channel, spec, provider, retries);
		} while (retries >= -1);
	}

	protected int verifyPIN(CardChannel channel, PinInfo pinSpec,
			PINGUI provider, int retries) throws SignatureCardException,
			LockedException, NotActivatedException, InterruptedException,
			CardException {

		VerifyAPDUSpec apduSpec = new VerifyAPDUSpec(new byte[] { (byte) 0x00,
				(byte) 0x20, (byte) 0x00, pinSpec.getKID(), (byte) 0x06,
				(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
				(byte) 0x00, (byte) 0x00 }, 0, VerifyAPDUSpec.PIN_FORMAT_ASCII,
				6);

		ResponseAPDU resp = reader.verify(channel, apduSpec, provider, pinSpec,
				retries);

		if (resp.getSW() == 0x9000) {
			return -2;
		}
		if (resp.getSW() >> 4 == 0x63c) {
			return 0x0f & resp.getSW();
		}

		switch (resp.getSW()) {
		case 0x6300:
			// incorrect PIN, number of retries not provided
			return -1;
		case 0x6400:
			// ?
			throw new TimeoutException();
		case 0x6983:
			// authentication method blocked
			throw new LockedException();
		case 0x6984:
			// reference data not usable
			throw new NotActivatedException();
		case 0x6985:
			// conditions of use not satisfied
			throw new NotActivatedException();

		default:
			String msg = "VERIFY failed. SW="
					+ Integer.toHexString(resp.getSW());
			log.info(msg);
			throw new SignatureCardException(msg);
		}

	}

	private byte[] executeSecurityOperation(CardChannel channel, byte[] digest)
			throws CardException, SignatureCardException {

		byte[] apduData = new byte[256];

		// pre-fill with padding
		for (int i = 0; i < apduData.length; i++) {
			apduData[i] = (byte) 0xFF;
		}
		apduData[0] = (byte) 0x00;
		apduData[1] = (byte) 0x01;
		apduData[apduData.length - 1 - OID.length - digest.length] = (byte) 0x00;

		System.arraycopy(digest, 0, apduData, apduData.length - digest.length,
				digest.length);
		System.arraycopy(OID, 0, apduData, apduData.length - OID.length
				- digest.length, OID.length);

		byte[] apduHeader = new byte[] { (byte) 0x00, (byte) 0x2A, (byte) 0x9E,
				(byte) 0x9A, (byte) 0x00, (byte) 0x01, (byte) 0x00 };

		byte[] data = new byte[apduData.length + apduHeader.length];
		System.arraycopy(apduHeader, 0, data, 0, apduHeader.length);
		System.arraycopy(apduData, 0, data, apduHeader.length, apduData.length);

		// We need to send 263 bytes, thus we need to split the data into
		// several blocks
		boolean finished = false;
		int bytesSent = 0;

		while (!finished) {

			boolean lastBlock = data.length - bytesSent <= TRANSFER_BLOCK_SIZE;

			int blockSize = lastBlock ? data.length - bytesSent
					: TRANSFER_BLOCK_SIZE;

			byte[] block = new byte[blockSize];
			System.arraycopy(data, bytesSent, block, 0, block.length);

			byte p1 = lastBlock ? (byte) 0x80 : (byte) 0x00;

			CommandAPDU apdu = new CommandAPDU((byte) 0x80, (byte) 0xAA, p1,
					(byte) 0x00, block);

			ResponseAPDU resp = channel.transmit(apdu);

			if (resp.getSW() != 0x9000) {

				throw new SignatureCardException(
						"Unexpected response from card: "
								+ Integer.toHexString(resp.getSW()));
			}

			bytesSent = bytesSent + block.length;

			if (bytesSent >= data.length) {

				finished = true;
			}
		}

		// try to get response
		finished = false;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();

		while (!finished) {
			CommandAPDU apdu = new CommandAPDU((byte) 0x80, (byte) 0xAB,
					(byte) 0x00, (byte) 0x00);

			ResponseAPDU resp = channel.transmit(apdu);

			if (resp.getSW1() == 0x6C) {

				apdu = new CommandAPDU((byte) 0x80, (byte) 0xAB, (byte) 0x00,
						(byte) 0x00, resp.getSW2());
				resp = channel.transmit(apdu);
			}

			try {
				bos.write(resp.getData());
			} catch (IOException e) {

				throw new SignatureCardException("Error saving card response.",
						e);
			}

			if (resp.getSW() == 0x6282) {

				// card indicates that all data has been read
				finished = true;
			}

		}

		return bos.toByteArray();
	}

	private void setMSE(CardChannel channel) throws CardException,
			SignatureCardException {

		CommandAPDU apdu = new CommandAPDU((byte) 0x00, (byte) 0x22,
				(byte) 0xF1, (byte) 0xB6, new byte[] { (byte) 0x84,
						(byte) 0x01, (byte) 0x02 });

		ResponseAPDU resp = channel.transmit(apdu);

		if (resp.getSW() != 0x9000) {

			throw new SignatureCardException("Unexpected response to MSE SET: "
					+ Integer.toHexString(resp.getSW()));
		}

	}

	@Override
	@Exclusive
	public byte[] getCertificate(KeyboxName keyboxName, PINGUI pinGUI)
			throws SignatureCardException, InterruptedException {

		log.debug("Trying to read certificate..");
		CardChannel channel = getCardChannel();

		byte[] cert = null;

		try {

			selectApplet(channel);
			selectPKIProfile(channel);

			selectDF(channel, new byte[] { (byte) 0x43, (byte) 0x04 });
			selectDF(channel, new byte[] { (byte) 0x45, (byte) 0x41 });

			byte[] certs = executeReadBinary(channel);

			selectPKIProfile(channel);
			selectDF(channel, new byte[] { (byte) 0x44, (byte) 0x04 });

			byte[] certsMetaInfo = executeReadBinary(channel);

			cert = retrieveSigningCertificate(certs, certsMetaInfo,
					CERTIFICATE_IDENTIFIER);

		} catch (CardException e) {

			throw new SignatureCardException("Error reading certificate.", e);
		} catch (IOException e) {

			throw new SignatureCardException("Error reading certificate.", e);
		}

		return cert;
	}

	private void selectPKIProfile(CardChannel channel) throws CardException,
			SignatureCardException {

		CommandAPDU apdu = new CommandAPDU((byte) 0x00, (byte) 0xA4,
				(byte) 0x04, (byte) 0x00, PKI_PROFILE);

		ResponseAPDU resp = channel.transmit(apdu);

		if (resp.getSW() != 0x9000) {

			throw new SignatureCardException(
					"Error selecting pki profile. Unexpected response from card: "
							+ Integer.toHexString(resp.getSW()));
		}

	}

	private void selectDF(CardChannel channel, byte[] id) throws CardException,
			SignatureCardException {

		CommandAPDU apdu = new CommandAPDU((byte) 0x00, (byte) 0xA4,
				(byte) 0x00, (byte) 0x00, id);

		ResponseAPDU resp = channel.transmit(apdu);

		if (resp.getSW() != 0x9000) {

			throw new SignatureCardException(
					"Error selecting DF. Unexpected response from card: "
							+ Integer.toHexString(resp.getSW()));
		}

	}

	private byte[] executeReadBinary(CardChannel channel) throws CardException {

		ByteArrayOutputStream bof = new ByteArrayOutputStream();

		int bytesRead = 0;

		boolean done = false;

		int offset = 0;
		int len = 0;

		while (!done) {

			byte[] offsetBytes = SMCCHelper.toByteArray(offset);
			ResponseAPDU resp = readFromCard(channel, offsetBytes[0],
					offsetBytes[1], (byte) len);

			if (resp.getSW1() == (byte) 0x6C) {

				// handle case: card returns 6CXX (wrong number of bytes
				// requested)

				resp = readFromCard(channel, offsetBytes[0], offsetBytes[1],
						(byte) resp.getSW2());
			}

			if (resp.getSW() == 0x6282) {

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

		}

		return bof.toByteArray();
	}

	private ResponseAPDU readFromCard(CardChannel channel, byte offsetHi,
			byte offsetLo, byte numBytes) throws CardException {

		byte[] apdu = new byte[] {

		(byte) 0x00, (byte) 0xB0, offsetHi, offsetLo, numBytes };

		CommandAPDU command = new CommandAPDU(apdu);
		ResponseAPDU resp = channel.transmit(command);

		return resp;

	}

	@Override
	protected byte[] getAppletID() {

		return APPLET_ID;
	}

}
