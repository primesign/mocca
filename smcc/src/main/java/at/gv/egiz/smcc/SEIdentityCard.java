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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;

import at.gv.egiz.smcc.pin.gui.PINGUI;
import at.gv.egiz.smcc.util.ISO7816Utils;
import at.gv.egiz.smcc.util.SMCCHelper;

// TODO: This class uses predefined IDs and path to communicate with the Swedish e-ID card.
// Key and certificate IDs / paths should instead be read out from files defined by ISO 7816-15

public class SEIdentityCard extends AbstractSignatureCard implements
		SignatureCard {

	private static final byte[] SIGDATA_PREFIX = new byte[] { (byte) 0x30, (byte) 0x21, (byte) 0x30,
			(byte) 0x09, (byte) 0x06, (byte) 0x05, (byte) 0x2B,
			(byte) 0x0E, (byte) 0x03, (byte) 0x02, (byte) 0x1A,
			(byte) 0x05, (byte) 0x00, (byte) 0x04, (byte) 0x14 };
	
	private static final PinInfo PIN_SPEC = new PinInfo(6, 8, ".",
			"at/gv/egiz/smcc/SEIdentityCard", "pin", (byte) 0x82, null,
			PinInfo.UNKNOWN_RETRIES);
	
	private final Logger log = LoggerFactory.getLogger(SEIdentityCard.class);

	@Override
	public byte[] createSignature(InputStream input, KeyboxName keyboxName,
			PINGUI pinGUI, String alg) throws SignatureCardException,
			InterruptedException, IOException {

		log.debug("Trying to create signature..");

		CardChannel channel = getCardChannel();

		// SELECT FILE
		try {
			selectFile(channel, new byte[] { (byte) 0x50, (byte) 0x15,
					(byte) 0x50, (byte) 0x16, (byte) 0x4B, (byte) 0x02 });
		} catch (CardException e) {
			
			throw new SignatureCardException("Error selecting file.", e);
		}

		// VERIFY PIN
		try {
			verifyPINLoop(channel, PIN_SPEC, pinGUI);
		} catch (CardException e1) {

			throw new SignatureCardException("Error verifying PIN.", e1);
		}

		// SET MSE
		setMSE(channel);

		// CREATE SIGNATURE

		MessageDigest md;
		try {
			md = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException e) {
			log.error("Failed to get MessageDigest.", e);
			throw new SignatureCardException(e);
		}
		// calculate message digest
		try {
			byte[] digest = new byte[md.getDigestLength()];
			for (int l; (l = input.read(digest)) != -1;) {
				md.update(digest, 0, l);
			}
			digest = md.digest();

			byte[] sigData = new byte[SIGDATA_PREFIX.length + digest.length];
			System.arraycopy(SIGDATA_PREFIX, 0, sigData, 0, SIGDATA_PREFIX.length);
			System.arraycopy(digest, 0, sigData, SIGDATA_PREFIX.length, digest.length);
			
			CommandAPDU apdu = new CommandAPDU((byte) 0x00, (byte) 0x2A,
					(byte) 0x9E, (byte) 0x9A, sigData);			
			
			ResponseAPDU resp = channel.transmit(apdu);

			if (resp.getSW() != 0x9000) {

				throw new SignatureCardException("Error creating signature: "
						+ Integer.toHexString(resp.getSW()));
			}

			return resp.getData();
			
			
		} catch (IOException e) {
			throw new SignatureCardException("Error creating signature.", e);
		} catch (CardException e) {
			throw new SignatureCardException("Error creating signature.", e);
		}
	}

	@Override
	public byte[] getCertificate(KeyboxName keyboxName, PINGUI pinGUI)
			throws SignatureCardException, InterruptedException {

		log.debug("Trying to fetch certificate..");

		CardChannel channel = getCardChannel();

		byte[] fci = null;

		try {
			fci = selectFile(channel, new byte[] { (byte) 0x50, (byte) 0x15,
					(byte) 0x50, (byte) 0x16, (byte) 0x43, (byte) 0x32 });
		} catch (CardException e) {

			throw new SignatureCardException("Error selecting card file.", e);
		}

		if (fci == null) {
			throw new SignatureCardException(
					"Could not retireve FCI for certificate file.");
		}

		byte[] cert = null;

		try {
			cert = executeReadBinary(channel, ISO7816Utils
					.getLengthFromFCx(fci));
		} catch (CardException e) {
			throw new SignatureCardException(
					"Error reading certificate from card.", e);
		}

		return cert;
	}

	@Override
	public byte[] getInfobox(String infobox, PINGUI pinGUI, String domainId)
			throws SignatureCardException, InterruptedException {

		throw new IllegalArgumentException("Infobox '" + infobox
				+ "' not supported.");
	}

	private void setMSE(CardChannel channel) throws SignatureCardException {

		byte[] dst = new byte[] { (byte) 0x80, (byte) 0x01, (byte) 0x02,
				(byte) 0x81, (byte) 0x02, (byte) 0x4B, (byte) 0x02 };

		CommandAPDU apdu = new CommandAPDU((byte) 0x00, (byte) 0x22,
				(byte) 0x41, (byte) 0xB6, dst);

		try {
			ResponseAPDU resp = channel.transmit(apdu);

			if (resp.getSW() != 0x9000) {
				throw new SignatureCardException("Error setting DST: "
						+ Integer.toHexString(resp.getSW()));
			}

		} catch (CardException e) {

			throw new SignatureCardException("Error setting DST.", e);
		}

	}

	private byte[] selectFile(CardChannel channel, byte[] fid)
			throws CardException, SignatureCardException {

		CommandAPDU apdu = new CommandAPDU((byte) 0x00, (byte) 0xA4,
				(byte) 0x08, (byte) 0x00, fid);

		ResponseAPDU resp = channel.transmit(apdu);

		if (resp.getSW() != 0x9000) {

			throw new SignatureCardException("Unexpected result from card: "
					+ Integer.toHexString(resp.getSW()));
		}

		return resp.getData();
	}

	private byte[] executeReadBinary(CardChannel channel, int bytes2read)
			throws CardException {

		ByteArrayOutputStream bof = new ByteArrayOutputStream();

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

	private ResponseAPDU readFromCard(CardChannel channel, byte offsetHi,
			byte offsetLo, byte numBytes) throws CardException {

		byte[] apdu = new byte[] {

		(byte) 0x00, (byte) 0xB0, offsetHi, offsetLo, numBytes };

		CommandAPDU command = new CommandAPDU(apdu);
		ResponseAPDU resp = channel.transmit(command);

		return resp;

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
				(byte) 0x20, (byte) 0x00, pinSpec.getKID(), (byte) 0x08,
				(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
				(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00}, 0,
				VerifyAPDUSpec.PIN_FORMAT_ASCII, 8);

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

}
