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
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.gv.egiz.smcc.pin.gui.PINGUI;
import at.gv.egiz.smcc.util.ISO7816Utils;
import at.gv.egiz.smcc.util.SMCCHelper;
import at.gv.egiz.smcc.util.TLVSequence;

public class FINEIDCard extends AbstractSignatureCard implements SignatureCard {

	private static final int EF_OD_PADDING = 0xFF;
	private static final String SIG_CERT_LABEL = "allekirjoitusvarmenne";
	private static final String SIG_KEY_LABEL = "allekirjoitusavain";

	private final Logger log = LoggerFactory.getLogger(FINEIDCard.class);

	protected PinInfo pinInfo = new PinInfo(6, 8, "[0-9]",
			"at/gv/egiz/smcc/FINEIDCard", "sig.pin", (byte) 0x00,
			new byte[] {}, PinInfo.UNKNOWN_RETRIES);

	@Override
	public byte[] createSignature(InputStream input, KeyboxName keyboxName,
			PINGUI pinGUI, String alg) throws SignatureCardException,
			InterruptedException, IOException {

		CardChannel channel = getCardChannel();

		try {

			FINEIDEFObjectDirectory ef_od = new FINEIDEFObjectDirectory(
					EF_OD_PADDING);
			ef_od.selectAndRead(channel);

			// read PRKD to find correct key
			FINEIDCIOKeyDirectory ef_prkd = new FINEIDCIOKeyDirectory(ef_od
					.getPrKDReferences().get(0));
			ef_prkd.selectAndRead(channel);

			byte[] efKey = null;
			byte[] authID = null;

			for (CIOCertificate cioCertificate : ef_prkd.getCIOs()) {
				String label = cioCertificate.getLabel();
				if (label != null
						&& label.toLowerCase().contains(
								SIG_KEY_LABEL.toLowerCase())) {

					efKey = cioCertificate.getEfidOrPath();
					authID = cioCertificate.getAuthId();
				}
			}

			if (efKey == null) {

				throw new SignatureCardException(
						"Could not determine path to private key from PrKD.");
			}

			if (authID == null) {

				throw new SignatureCardException(
						"Could not determine authID of private key from PrKD.");
			}

			// read AOD to find the associated PIN (authId must match)
			FINEIDAODirectory ef_aod = new FINEIDAODirectory(ef_od.getAODReferences().get(0));
			ef_aod.selectAndRead(channel);

			byte[] pinPath = null;
			byte[] pwdRef = null;
			for (FINEIDAuthenticationObject ao : ef_aod.getAOs()) {

				byte[] id = ao.getAuthId();
				if (id != null && Arrays.equals(id, authID)) {
					pinPath = ao.getPath();
					pwdRef = ao.getPwdReference();
				}
			}

			if (pinPath == null) {

				throw new SignatureCardException(
						"Could not determine path to PIN from AOD.");
			}

			if (pwdRef == null) {

				throw new SignatureCardException(
						"Could not determine PIN reference from AOD.");
			}

			// verify PIN
			verifyPINLoop(channel, pinInfo, pinGUI, pinPath,
					pwdRef[pwdRef.length - 1]);

			// Set MSE
			CommandAPDU selectKeyPath = new CommandAPDU((byte) 0x00,
					(byte) 0xA4, (byte) 0x08, (byte) 0x00, FINEIDUtil
							.removeMFPath(efKey));
			ResponseAPDU resp = channel.transmit(selectKeyPath);

			if (resp.getSW() != 0x9000) {

				throw new SignatureCardException(
						"Could not select private key file DF.");
			}

			executeRestoreMSE(channel);

			byte[] dst = new byte[] { (byte) 0x80, (byte) 0x01, (byte) 0x12,
					(byte) 0x81, (byte) 0x02, efKey[efKey.length - 2],
					efKey[efKey.length - 1] };

			executeSetMSE(channel, dst);

			// SIGN
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

			byte[] sigVal = executeSign(channel, digest);
			return sigVal;

		} catch (CardException e) {

			throw new SignatureCardException("Error creating signature.", e);
		}

	}

	@Override
	public byte[] getCertificate(KeyboxName keyboxName, PINGUI pinGUI)
			throws SignatureCardException, InterruptedException {

		CardChannel channel = getCardChannel();

		try {
			FINEIDEFObjectDirectory ef_od = new FINEIDEFObjectDirectory(
					EF_OD_PADDING);
			ef_od.selectAndRead(channel);

			byte[] certPath = null;

			for (int i = 0; i < ef_od.getCDReferences().size(); i++) {

				FINEIDCIOCertificateDirectory ef_cd = new FINEIDCIOCertificateDirectory(
						ef_od.getCDReferences().get(i));

				try {
					ef_cd.selectAndRead(channel);
				} catch (IOException e) {
					log.debug("Cannot read EF.CD - try next one in list..");
					continue;
				}

				for (CIOCertificate cioCertificate : ef_cd.getCIOs()) {
					String label = cioCertificate.getLabel();
					if (label != null
							&& label.toLowerCase().contains(
									SIG_CERT_LABEL.toLowerCase())) {
						certPath = cioCertificate.getEfidOrPath();
					}
				}
			}

			if (certPath == null) {

				throw new SignatureCardException(
						"Could not determine path to certificate.");
			}

			log
					.debug("Read certificate path: "
							+ SMCCHelper.toString(certPath));

			certPath = FINEIDUtil.removeMFPath(certPath);

			CommandAPDU apdu = new CommandAPDU((byte) 0x00, (byte) 0xA4,
					(byte) 0x08, (byte) 0x00, certPath);
			ResponseAPDU resp = channel.transmit(apdu);

			byte[] fcx = new TLVSequence(resp.getBytes())
					.getValue(ISO7816Utils.TAG_FCI);
			byte[] fileDataLength = new TLVSequence(fcx).getValue(0x81);

			return ISO7816Utils.readTransparentFile(channel,
					computeLengthFromByteArray(fileDataLength));

		} catch (CardException e) {

			throw new SignatureCardException(
					"Error reading certificate from card.", e);
		}
	}

	@Override
	public byte[] getInfobox(String infobox, PINGUI pinGUI, String domainId)
			throws SignatureCardException, InterruptedException {

		throw new IllegalArgumentException("Infobox '" + infobox
				+ "' not supported.");
	}

	protected void verifyPINLoop(CardChannel channel, PinInfo spec,
			PINGUI provider, byte[] pinPath, byte keyID)
			throws LockedException, NotActivatedException,
			SignatureCardException, InterruptedException, CardException {

		CommandAPDU verifySelect = new CommandAPDU((byte) 0x00, (byte) 0xA4,
				(byte) 0x08, (byte) 0x00, FINEIDUtil.removeMFPath(pinPath));
		ResponseAPDU response = channel.transmit(verifySelect);

		if (response.getSW() != 0x9000) {

			throw new SignatureCardException("Cannot select PIN path "
					+ SMCCHelper.toString(pinPath) + ": "
					+ Integer.toHexString(response.getSW()));
		}

		int retries = -1;

		do {
			retries = verifyPIN(channel, spec, provider, retries, keyID);
		} while (retries > 0);
	}

	protected int verifyPIN(CardChannel channel, PinInfo pinSpec,
			PINGUI provider, int retries, byte keyID)
			throws SignatureCardException, LockedException,
			NotActivatedException, InterruptedException, CardException {

		VerifyAPDUSpec apduSpec = new VerifyAPDUSpec(new byte[] { (byte) 0x00,
				(byte) 0x20, (byte) 0x00, keyID, (byte) 0x08, (byte) 0x00,
				(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
				(byte) 0x00, (byte) 0x00, (byte) 0x00 }, 0,
				VerifyAPDUSpec.PIN_FORMAT_ASCII, 8);

		ResponseAPDU resp = reader.verify(channel, apduSpec, provider, pinSpec,
				retries);

		if (resp.getSW() == 0x9000) {
			return -1;
		}
		if (resp.getSW() >> 4 == 0x63c) {
			return 0x0f & resp.getSW();
		}

		switch (resp.getSW()) {
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

	private void executeRestoreMSE(CardChannel channel) throws CardException {

		CommandAPDU mseRestore = new CommandAPDU((byte) 0x00, (byte) 0x22,
				(byte) 0xF3, (byte) 0x00);
		ResponseAPDU resp = channel.transmit(mseRestore);

		if (resp.getSW() != 0x9000) {

			throw new CardException("Error restoring MSE: "
					+ Integer.toHexString(resp.getSW()));
		}

	}

	private void executeSetMSE(CardChannel channel, byte[] dst)
			throws CardException {

		CommandAPDU mseSet = new CommandAPDU((byte) 0x00, (byte) 0x22,
				(byte) 0x41, (byte) 0xB6, dst);
		ResponseAPDU resp = channel.transmit(mseSet);

		if (resp.getSW() != 0x9000) {

			throw new CardException("Error setting MSE: "
					+ Integer.toHexString(resp.getSW()));
		}

	}

	private byte[] executeSign(CardChannel channel, byte[] hash)
			throws CardException {

		CommandAPDU sign = new CommandAPDU((byte) 0x00, (byte) 0x2A,
				(byte) 0x9E, (byte) 0x9A, hash);
		ResponseAPDU resp = channel.transmit(sign);

		if (resp.getSW() != 0x9000) {

			throw new CardException("Error signing hash: "
					+ Integer.toHexString(resp.getSW()));
		}

		return resp.getData();
	}

	private int computeLengthFromByteArray(byte[] input) {

		int result = 0;

		for (int i = 0; i < input.length; i++) {
			int current = input[input.length - 1 - i];
			result = result + (int) (current * Math.pow(256, i));
		}
		return result;
	}

}
