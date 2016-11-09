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

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.gv.egiz.smcc.pin.gui.ModifyPINGUI;
import at.gv.egiz.smcc.pin.gui.PINGUI;
import at.gv.egiz.smcc.util.ISO7816Utils;
import at.gv.egiz.smcc.util.SMCCHelper;

@SuppressWarnings("restriction")
/**
 * @author Christof Rath <christof.rath@prime-sign.com>
 */
public class PrimSignATOSCard extends AbstractSignatureCard implements PINMgmtSignatureCard {

	/**
	 * Logging facility.
	 */
	private final Logger log = LoggerFactory.getLogger(PrimSignATOSCard.class);

	public static final byte[] MF = new byte[] { (byte) 0x3F, (byte) 0x00 };

	public static final byte[] DF_QES_ID = new byte[] { (byte) 0x3F, (byte) 0x04 };

	public static final byte[] EF_C_X509_CH_DS_FID = new byte[] { (byte) 0xC0, (byte) 0x00 };
	public static final byte[] EF_VERSION = new byte[] { (byte) 0x00, (byte) 0x32 };

	// public static final byte MSE_SET_ALGO_REF = (byte) 0x02;

	// public static final byte MSE_SET_PRIV_KEY_REF = (byte) 0x83;

	// public static final int SIGNATURE_LENGTH = (int) 0x80;

	public static final byte KID = (byte) 0x81;

	// public static final int READ_BUFFER_LENGTH = 256;

	public static final int PINSPEC_SS = 0;

	protected PinInfo ssPinInfo = new PinInfo(4, 12, "[0-9]", "at/gv/egiz/smcc/PrimSignATOSCard", "sig.pin", KID,
			DF_QES_ID, 10);

	@Override
	public void init(Card card, CardTerminal cardTerminal) {
		super.init(card, cardTerminal);

	    log.info("ATOS CardOS 5.3 card found");

	    // determine application version
	    CardChannel channel = getCardChannel();
	    try {
	      // SELECT MF
	      execSELECT_MF(channel);
	      // SELECT EF_VERSION
	      execSELECT_FID(channel, EF_VERSION);
	      // READ BINARY
	      byte[] ver = ISO7816Utils.readTransparentFile(channel, -1);
	      log.info(new String(ver) + " card found");
	    } catch (CardException e) {
	        log.warn("Failed to execute command.", e);
	      } catch (SignatureCardException e) {
	        log.warn("Failed to execute command.", e);
	    }
	}
	    
	@Override
	@Exclusive
	public byte[] getCertificate(KeyboxName keyboxName, PINGUI provider) throws SignatureCardException {

		if (keyboxName != KeyboxName.SECURE_SIGNATURE_KEYPAIR) {
			throw new IllegalArgumentException("Keybox " + keyboxName + " not supported");
		}

		try {
			CardChannel channel = getCardChannel();
			// SELECT application
			execSELECT_AID(channel, DF_QES_ID);
			// SELECT file
			execSELECT_FID(channel, EF_C_X509_CH_DS_FID);
			// READ BINARY
			byte[] certificate = ISO7816Utils.readTransparentFileTLV(channel, -1, (byte) 0x30);
			if (certificate == null) {
				throw new NotActivatedException();
			}
			return certificate;
		} catch (FileNotFoundException e) {
			throw new NotActivatedException();
		} catch (CardException e) {
			log.info("Failed to get certificate.", e);
			throw new SignatureCardException(e);
		}

	}

	@Override
	@Exclusive
	public byte[] getInfobox(String infobox, PINGUI provider, String domainId)
			throws SignatureCardException, InterruptedException {

		throw new IllegalArgumentException("Infobox '" + infobox + "' not supported.");
	}

	@Override
	@Exclusive
	public byte[] createSignature(InputStream input, KeyboxName keyboxName, PINGUI provider, String alg)
			throws SignatureCardException, InterruptedException, IOException {

		if (KeyboxName.SECURE_SIGNATURE_KEYPAIR != keyboxName) {
			throw new SignatureCardException("Card does not support key " + keyboxName + ".");
		}

		MessageDigest md;
		try {
			if ("http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha1".equals(alg)) {
				md = MessageDigest.getInstance("SHA-1");
			} else if ("http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha256".equals(alg)) {
				md = MessageDigest.getInstance("SHA-256");
			} else if ("http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha384".equals(alg)) {
				md = MessageDigest.getInstance("SHA-384");
			} else if ("http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha512".equals(alg)) {
				md = MessageDigest.getInstance("SHA-512");
			} else if ("http://www.w3.org/2007/05/xmldsig-more#ecdsa-ripemd160".equals(alg)) {
				md = MessageDigest.getInstance("RIPEMD160");
			} else {
				throw new SignatureCardException("Card does not support algorithm " + alg + ".");
			}
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

		try {

			CardChannel channel = getCardChannel();

			// SELECT MF
			execSELECT_MF(channel);
			// VERIFY
			verifyPIN(ssPinInfo, provider);
//			verifyPINLoop(channel, ssPinInfo, provider);
			// // MANAGE SECURITY ENVIRONMENT : SET DST
			// execMSE(channel, 0x41, 0xb6, dst);
			// PERFORM SECURITY OPERATION : COMPUTE DIGITAL SIGNATURE
			return execPSO_COMPUTE_DIGITAL_SIGNATURE(channel, digest);

		} catch (CardException e) {
			log.warn("Failed to execute command.", e);
			throw new SignatureCardException("Failed to access card.", e);
		}

	}

	public String toString() {
		return "PrimeSign ATOS Card";
	}

	@Override
	public PinInfo[] getPinInfos() throws SignatureCardException {
		// check if card is activated
		getCertificate(KeyboxName.SECURE_SIGNATURE_KEYPAIR, null);

		return new PinInfo[] { ssPinInfo };
	}

	@Override
	public void verifyPIN(PinInfo pinInfo, PINGUI pinGUI) throws LockedException, NotActivatedException,
			CancelledException, SignatureCardException, InterruptedException {

		CardChannel channel = getCardChannel();

		try {
			if (pinInfo.getContextAID() != null) {
				// SELECT application
				execSELECT_AID(channel, pinInfo.getContextAID());
			}
			// VERIFY
			verifyPINLoop(channel, pinInfo, pinGUI);
		} catch (CardException e) {
			log.info("Failed to verify PIN.", e);
			throw new SignatureCardException("Failed to verify PIN.", e);
		}

	}

	@Override
	public void changePIN(PinInfo pinInfo, ModifyPINGUI changePINGUI) throws LockedException, NotActivatedException,
			CancelledException, PINFormatException, SignatureCardException, InterruptedException {

		CardChannel channel = getCardChannel();

		try {
			if (pinInfo.getContextAID() != null) {
				// SELECT application
				execSELECT_AID(channel, pinInfo.getContextAID());
			}
			changePINLoop(channel, pinInfo, changePINGUI);
		} catch (CardException e) {
			log.info("Failed to change PIN.", e);
			throw new SignatureCardException("Failed to change PIN.", e);
		}

	}

	@Override
	public void activatePIN(PinInfo pinInfo, ModifyPINGUI activatePINGUI)
			throws CancelledException, SignatureCardException, InterruptedException {

		CardChannel channel = getCardChannel();

		try {
			if (pinInfo.getContextAID() != null) {
				// SELECT application
				execSELECT_AID(channel, pinInfo.getContextAID());
			}
			activatePIN(channel, pinInfo, activatePINGUI);
		} catch (CardException e) {
			log.info("Failed to activate PIN.", e);
			throw new SignatureCardException("Failed to activate PIN.", e);
		}

	}

	@Override
	public void unblockPIN(PinInfo pinInfo, ModifyPINGUI pukGUI)
			throws CancelledException, SignatureCardException, InterruptedException {
		throw new SignatureCardException("Unblock PIN not supported.");
	}

	////////////////////////////////////////////////////////////////////////
	// PROTECTED METHODS (assume exclusive card access)
	////////////////////////////////////////////////////////////////////////

	protected void execSELECT_MF(CardChannel channel) throws CardException, SignatureCardException {
		ResponseAPDU resp = channel.transmit(new CommandAPDU(0x00, 0xA4, 0x00, 0x0C));
		if (resp.getSW() != 0x9000) {
			throw new SignatureCardException("Failed to select MF: SW=" + Integer.toHexString(resp.getSW()) + ".");
		}
	}

	protected byte[] execSELECT_AID(CardChannel channel, byte[] aid) throws SignatureCardException, CardException {

		ResponseAPDU resp = channel.transmit(new CommandAPDU(0x00, 0xA4, 0x08, 0x0C, aid));

		if (resp.getSW() == 0x6A82) {
			String msg = "File or application not found AID=" + SMCCHelper.toString(aid) + " SW="
					+ Integer.toHexString(resp.getSW()) + ".";
			log.info(msg);
			throw new FileNotFoundException(msg);
		} else if (resp.getSW() != 0x9000) {
			String msg = "Failed to select application AID=" + SMCCHelper.toString(aid) + " SW="
					+ Integer.toHexString(resp.getSW()) + ".";
			log.info(msg);
			throw new SignatureCardException(msg);
		} else {
			return resp.getBytes();
		}

	}

	protected byte[] execSELECT_FID(CardChannel channel, byte[] fid) throws SignatureCardException, CardException {

		ResponseAPDU resp = channel.transmit(new CommandAPDU(0x00, 0xA4, 0x02, 0x04, fid, 256));

		if (resp.getSW() == 0x6A82) {
			String msg = "File or application not found FID=" + SMCCHelper.toString(fid) + " SW="
					+ Integer.toHexString(resp.getSW()) + ".";
			log.info(msg);
			throw new FileNotFoundException(msg);
		} else if (resp.getSW() != 0x9000) {
			String msg = "Failed to select application FID=" + SMCCHelper.toString(fid) + " SW="
					+ Integer.toHexString(resp.getSW()) + ".";
			log.error(msg);
			throw new SignatureCardException(msg);
		} else {
			return resp.getBytes();
		}

	}

	protected void execMSE(CardChannel channel, int p1, int p2, byte[] data)
			throws CardException, SignatureCardException {
		ResponseAPDU resp = channel.transmit(new CommandAPDU(0x00, 0x22, p1, p2, data, 256));
		if (resp.getSW() != 0x9000) {
			throw new SignatureCardException("MSE:SET failed: SW=" + Integer.toHexString(resp.getSW()));
		}
	}

	protected byte[] execPSO_COMPUTE_DIGITAL_SIGNATURE(CardChannel channel, byte[] hash)
			throws CardException, SignatureCardException {
		ResponseAPDU resp;
		resp = channel.transmit(new CommandAPDU(0x00, 0x2A, 0x9E, 0x9A, hash, 256));
		if (resp.getSW() == 0x6982) {
			throw new SecurityStatusNotSatisfiedException();
		} else if (resp.getSW() == 0x6983) {
			throw new LockedException();
		} else if (resp.getSW() != 0x9000) {
			throw new SignatureCardException(
					"PSO: COMPUTE DIGITAL SIGNATURE failed: SW=" + Integer.toHexString(resp.getSW()));
		} else {
			return resp.getData();
		}
	}

	protected void verifyPINLoop(CardChannel channel, PinInfo pinInfo, PINGUI provider)
			throws LockedException, NotActivatedException, SignatureCardException, InterruptedException, CardException {

		int retries = verifyPIN(channel, pinInfo, null, -1);
		do {
			retries = verifyPIN(channel, pinInfo, provider, retries);
		} while (retries > 0);
	}

	protected int verifyPIN(CardChannel channel, PinInfo pinInfo, PINGUI provider, int retries)
			throws SignatureCardException, LockedException, NotActivatedException, InterruptedException, CardException {

		VerifyAPDUSpec apduSpec = new VerifyAPDUSpec(
				new byte[] { (byte) 0x00, (byte) 0x20, (byte) 0x00, pinInfo.getKID(), (byte) 0x08, (byte) 0x20,
						(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff },
				1, VerifyAPDUSpec.PIN_FORMAT_BCD, 7, 4, 4);

		ResponseAPDU resp;
		if (provider != null) {
			resp = reader.verify(channel, apduSpec, provider, pinInfo, retries);
		} else {
			resp = channel.transmit(new CommandAPDU(0x00, 0x20, 0x00, pinInfo.getKID()));
		}

		if (resp.getSW() == 0x9000) {
			pinInfo.setActive(pinInfo.maxRetries);
			return -1;
		} else if (resp.getSW() == 0x6983 || resp.getSW() == 0x63c0) {
			// authentication method blocked (0x63c0 returned by 'short' VERIFY)
			pinInfo.setBlocked();
			throw new LockedException();
		} else if (resp.getSW() == 0x6984 || resp.getSW() == 0x6985) {
			// reference data not usable; conditions of use not satisfied
			pinInfo.setNotActive();
			throw new NotActivatedException();
		} else if (resp.getSW() >> 4 == 0x63c) {
			pinInfo.setActive(0x0f & resp.getSW());
			return 0x0f & resp.getSW();
		} else if (resp.getSW() == 0x6400) {
			String msg = "VERIFY failed, card not activated. SW=0x6400";
			log.error(msg);
			pinInfo.setNotActive();
			throw new SignatureCardException(msg);
		} else {
			String msg = "VERIFY failed. SW=" + Integer.toHexString(resp.getSW());
			log.error(msg);
			pinInfo.setUnknown();
			throw new SignatureCardException(msg);
		}
	}

	protected void changePINLoop(CardChannel channel, PinInfo pinInfo, ModifyPINGUI provider)
			throws LockedException, NotActivatedException, SignatureCardException, InterruptedException, CardException {

		int retries = verifyPIN(channel, pinInfo, null, -1);
		do {
			retries = changePIN(channel, pinInfo, provider, retries);
		} while (retries > 0);
	}

	protected int changePIN(CardChannel channel, PinInfo pinInfo, ModifyPINGUI pinProvider, int retries)
			throws CancelledException, InterruptedException, CardException, SignatureCardException {

		ChangeReferenceDataAPDUSpec apduSpec = new ChangeReferenceDataAPDUSpec(new byte[] { (byte) 0x00, (byte) 0x24,
				(byte) 0x00, pinInfo.getKID(), (byte) 0x10, (byte) 0x20, (byte) 0xff, (byte) 0xff, (byte) 0xff,
				(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0x20, (byte) 0xff, (byte) 0xff, (byte) 0xff,
				(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff }, 1, VerifyAPDUSpec.PIN_FORMAT_BCD, 7, 4, 4, 8);

		ResponseAPDU resp = reader.modify(channel, apduSpec, pinProvider, pinInfo, retries);

		if (resp.getSW() == 0x9000) {
			pinInfo.setActive(pinInfo.maxRetries);
			return -1;
		} else if (resp.getSW() == 0x6983) {
			// authentication method blocked
			pinInfo.setBlocked();
			throw new LockedException();
		} else if (resp.getSW() == 0x6984) {
			pinInfo.setNotActive();
			throw new NotActivatedException();
		} else if (resp.getSW() >> 4 == 0x63c) {
			pinInfo.setActive(0x0f & resp.getSW());
			return 0x0f & resp.getSW();
		} else {
			String msg = "CHANGE REFERENCE DATA failed. SW=" + Integer.toHexString(resp.getSW());
			log.error(msg);
			pinInfo.setUnknown();
			throw new SignatureCardException(msg);
		}
	}

	protected int activatePIN(CardChannel channel, PinInfo pinInfo, ModifyPINGUI provider)
			throws SignatureCardException, InterruptedException, CardException {

		ResponseAPDU resp;
		NewReferenceDataAPDUSpec apduSpec = new NewReferenceDataAPDUSpec(new byte[] { (byte) 0x00, (byte) 0x24,
				(byte) 0x00, pinInfo.getKID(), (byte) 0x10, (byte) 0x25, (byte) 0x01, (byte) 0x23, (byte) 0x4f,
				(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0x20, (byte) 0xff, (byte) 0xff, (byte) 0xff,
				(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff }, 1, VerifyAPDUSpec.PIN_FORMAT_BCD, 7, 4, 4);

		apduSpec.setPinInsertionOffsetNew(8);
		resp = reader.modify(channel, apduSpec, provider, pinInfo);

		if (resp.getSW() == 0x9000) {
			pinInfo.setActive(pinInfo.maxRetries);
			return -1;
		} else {
			String msg = "CHANGE REFERENCE DATA failed. SW=" + Integer.toHexString(resp.getSW());
			log.error(msg);
			pinInfo.setUnknown();
			throw new SignatureCardException(msg);
		}
	}

}
