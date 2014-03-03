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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.gv.egiz.smcc.cio.CIOCertificate;
import at.gv.egiz.smcc.pin.gui.PINGUI;
import at.gv.egiz.smcc.util.ISO7816Utils;
import at.gv.egiz.smcc.util.SMCCHelper;

public class LtEIDCard extends AbstractSignatureCard implements SignatureCard {

	private static final byte[] AID = new byte[] {

	(byte) 0xA0, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x18,
			(byte) 0x0C, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x63,
			(byte) 0x42, (byte) 0x00 };

	private static final String CERT_LABEL_IDENTIFIER = "DigitalSignature";

	private static final PinInfo QS_PIN_SPEC = new PinInfo(8, 8, ".",
			"at/gv/egiz/smcc/LtEIDCard", "qs.pin", (byte) 0x81, AID,
			PinInfo.UNKNOWN_RETRIES);

	private final Logger log = LoggerFactory.getLogger(LtEIDCard.class);

	@Override
	@Exclusive
	public byte[] createSignature(InputStream input, KeyboxName keyboxName,
			PINGUI pinGUI, String alg) throws SignatureCardException,
			InterruptedException, IOException {

		CardChannel channel = getCardChannel();

		// select AID
		try {
			selectApplication(channel);
		} catch (CardException e) {

			throw new SignatureCardException("Error selecting AID.", e);
		}

		try {
			// read certificate info to get key id
			byte[] signCertAndKeyID = null;

			LtEIDObjectDirectory efod = new LtEIDObjectDirectory();

			efod.selectAndRead(channel);

			if (efod.getCDReferences() == null
					|| efod.getCDReferences().size() < 1) {

				throw new SignatureCardException(
						"EF.CD not found - cannot get certificate information.");
			}

			LtEIDCIOCertificateDirectory efcd = new LtEIDCIOCertificateDirectory(
					efod.getCDReferences().get(0));
			efcd.selectAndRead(channel);

			List<CIOCertificate> cioList = efcd.getCIOs();

			LtEIDCIOCertificate sigCertInfo = null;
			for (CIOCertificate cert : cioList) {

				if (cert instanceof LtEIDCIOCertificate
						&& cert.getLabel().contains(CERT_LABEL_IDENTIFIER)) {

					sigCertInfo = (LtEIDCIOCertificate) cert;
					signCertAndKeyID = sigCertInfo.getiD();
				}
			}

			// verify PIN
			// Note: PIN verify is required prior to read PrKD
			// verifyPIN(channel);

			log.debug("Starting real PIN Verification..");
			// TODO: Test real PIN Verification
			verifyPINLoop(channel, QS_PIN_SPEC, pinGUI);

			if (efod.getPrKDReferences() == null
					|| efod.getPrKDReferences().size() < 1) {

				throw new SignatureCardException(
						"EF.PrKD not found - cannot get key information.");
			}

			@SuppressWarnings("unused")
			List<byte[]> prKDReferences = efod.getPrKDReferences();

			LtEIDCIOKeyDirectory efprkd = new LtEIDCIOKeyDirectory(efod
					.getPrKDReferences().get(0));
			efprkd.selectAndRead(channel);

			LtEIDCIOKey signKey = null;

			for (LtEIDCIOKey key : efprkd.getKeys()) {

				if (signCertAndKeyID != null
						&& Arrays.equals(key.getID(), signCertAndKeyID)) {

					signKey = key;
				}
			}

			if (signKey == null) {

				throw new SignatureCardException(
						"Unable to determine required key information.");
			}

			execMSESet(channel, signKey.getKeyReference().intValue());
			execPSOHash(channel, input);

			return execPSOComputeDigitalSignature(channel);

		} catch (CardException e) {

			throw new SignatureCardException("Error creating signature.", e);
		}
	}

	@Override
	@Exclusive
	public byte[] getCertificate(KeyboxName keyboxName, PINGUI pinGUI)
			throws SignatureCardException, InterruptedException {

		CardChannel channel = getCardChannel();

		// select AID
		try {
			selectApplication(channel);
		} catch (CardException e) {

			throw new SignatureCardException("Error selecting AID.", e);
		}

		LtEIDObjectDirectory efod = new LtEIDObjectDirectory();
		try {
			efod.selectAndRead(channel);

			if (efod.getCDReferences() == null
					|| efod.getCDReferences().size() < 1) {

				throw new SignatureCardException(
						"EF.CD not found - cannot get certificate information.");
			}

			LtEIDCIOCertificateDirectory efcd = new LtEIDCIOCertificateDirectory(
					efod.getCDReferences().get(0));
			efcd.selectAndRead(channel);

			List<CIOCertificate> cioList = efcd.getCIOs();

			LtEIDCIOCertificate sigCertInfo = null;
			for (CIOCertificate cert : cioList) {

				if (cert instanceof LtEIDCIOCertificate
						&& cert.getLabel().contains(CERT_LABEL_IDENTIFIER)) {

					sigCertInfo = (LtEIDCIOCertificate) cert;
				}
			}

			if (sigCertInfo == null) {

				throw new SignatureCardException(
						"Unable to determine signature certificate.");
			}

			if (sigCertInfo.getOffset() == null
					|| sigCertInfo.getLength() == null
					|| sigCertInfo.getEfidOrPath() == null) {

				throw new SignatureCardException(
						"Unable to retrieve required certificate information.");
			}

			// select file with cert
			byte[] fci = selectFile(channel, sigCertInfo.getEfidOrPath());

			byte[] certFile = executeReadBinary(channel, ISO7816Utils
					.getLengthFromFCx(fci));
			byte[] sigCert = new byte[toInt(sigCertInfo.getLength())];
			System.arraycopy(certFile, sigCertInfo.getOffset().intValue(),
					sigCert, 0, sigCert.length);

			return sigCert;

		} catch (CardException e) {
			throw new SignatureCardException(
					"Unable to retrieve certificate from card.", e);
		} catch (FileNotFoundException e) {
			throw new SignatureCardException(
					"Unable to retrieve certificate from card.", e);
		} catch (IOException e) {
			throw new SignatureCardException(
					"Unable to retrieve certificate from card.", e);
		}

	}

	private void execMSESet(CardChannel channel, int keyReference)
			throws CardException {

		// Note: AlgoID (tag 0x80) has to be 0x12
		CommandAPDU apdu = new CommandAPDU((byte) 0x00, (byte) 0x22,
				(byte) 0x41, (byte) 0xB6, new byte[] { (byte) 0x80,
						(byte) 0x01, (byte) 0x12, (byte) 0x84, (byte) 0x01,
						(byte) keyReference });

		ResponseAPDU resp = channel.transmit(apdu);

		if (resp.getSW() != 0x9000) {

			throw new CardException(
					"Error executing MSE-SET. Unexpected response: "
							+ Integer.toHexString(resp.getSW()));
		}
	}

	private void execPSOHash(CardChannel channel, InputStream input)
			throws SignatureCardException {

		MessageDigest md;
		try {
			md = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException e) {
			log.error("Failed to get MessageDigest.", e);
			throw new SignatureCardException(e);
		}
		// calculate message digest
		ByteArrayOutputStream data = new ByteArrayOutputStream();

		try {
			byte[] digest = new byte[md.getDigestLength()];
			for (int l; (l = input.read(digest)) != -1;) {
				md.update(digest, 0, l);
			}
			digest = md.digest();

			data.write(new byte[] { (byte) 0x90, (byte) 0x14 });
			data.write(digest);

		} catch (IOException e) {
			throw new SignatureCardException("Error computing hash.", e);
		}

		try {

			CommandAPDU apdu = new CommandAPDU((byte) 0x00, (byte) 0x2A,
					(byte) 0x90, (byte) 0xA0, data.toByteArray());

			ResponseAPDU resp = channel.transmit(apdu);

			log.debug("Answer to PSO-HASH: "
					+ Integer.toHexString(resp.getSW()));

			if (resp.getSW() != 0x9000) {

				throw new SignatureCardException(
						"Error setting hash. Unexpected answer from card: "
								+ Integer.toHexString(resp.getSW()));
			}

		} catch (CardException e) {
			throw new SignatureCardException("Error setting hash.", e);
		}
	}

	private byte[] execPSOComputeDigitalSignature(CardChannel channel)
			throws SignatureCardException {

		// Note: Le is mandatory to ensure correct functionality
		CommandAPDU apdu = new CommandAPDU(new byte[] { (byte) 0x00,
				(byte) 0x2A, (byte) 0x9E, (byte) 0x9A, (byte) 0x00 });

		try {
			ResponseAPDU resp = channel.transmit(apdu);

			log.debug("Answer to PSO-Compute Digital Signature: "
					+ Integer.toHexString(resp.getSW()));

			if (resp.getSW() != 0x9000) {

				throw new SignatureCardException(
						"Error computing signature. Unexpected answer from card: "
								+ Integer.toHexString(resp.getSW()));
			}

			return resp.getData();

		} catch (CardException e) {
			throw new SignatureCardException("Error computing signature.", e);
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
				(byte) 0x20, (byte) 0x00, pinSpec.getKID(), (byte) 0x10,
				(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
				(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
				(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
				(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00 }, 0,
				VerifyAPDUSpec.PIN_FORMAT_ASCII, 16);

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

	@Override
	@Exclusive
	public byte[] getInfobox(String infobox, PINGUI pinGUI, String domainId)
			throws SignatureCardException, InterruptedException {

		throw new IllegalArgumentException("Infobox '" + infobox
				+ "' not supported.");

	}

	private void selectApplication(CardChannel channel) throws CardException {

		CommandAPDU apdu = new CommandAPDU((byte) 0x00, (byte) 0xA4,
				(byte) 0x04, (byte) 0x00, AID);

		ResponseAPDU resp = channel.transmit(apdu);

		if (resp.getSW() != 0x9000) {

			throw new CardException(
					"Error selecting AID - unexpected response from card: "
							+ Integer.toHexString(resp.getSW()));
		}
	}

	private byte[] selectFile(CardChannel channel, byte[] path)
			throws CardException {

		byte[] finalPath = null;

		if (path != null && path.length > 2 && path[0] == (byte) 0x3F
				&& path[1] == (byte) 0x00) {

			// cut off MF identifier
			finalPath = new byte[path.length - 2];
			System.arraycopy(path, 2, finalPath, 0, path.length - 2);
		} else {
			finalPath = path;
		}

		CommandAPDU apdu = new CommandAPDU((byte) 0x00, (byte) 0xA4,
				(byte) 0x08, (byte) 0x00, finalPath);

		ResponseAPDU resp = channel.transmit(apdu);

		if (resp.getSW() != 0x9000) {

			throw new CardException(
					"Error selecting File - unexpected response from card: "
							+ Integer.toHexString(resp.getSW()));
		}

		return resp.getData();

	}

	private byte[] executeReadBinary(CardChannel channel, int bytes2read)
			throws CardException {

		ByteArrayOutputStream bof = new ByteArrayOutputStream();

		// int bytes2read = (lengthHi * 256) + lengthLo;
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

	private int toInt(byte[] array) {

		int len = array.length;
		int result = 0;

		for (int i = len - 1; i >= 0; i--) {

			result = (int) (result + array[i] * Math.pow(256, len - i - 1));
		}

		return result;
	}

}
