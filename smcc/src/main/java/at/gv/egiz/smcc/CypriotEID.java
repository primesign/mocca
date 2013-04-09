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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
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

import at.gv.egiz.smcc.cio.ObjectDirectory;
import at.gv.egiz.smcc.pin.gui.ModifyPINGUI;
import at.gv.egiz.smcc.pin.gui.PINGUI;
import at.gv.egiz.smcc.util.SMCCHelper;


public class CypriotEID extends AbstractSignatureCard implements
		PINMgmtSignatureCard {

	private final Logger log = LoggerFactory.getLogger(ACOSCard.class);

	public static final byte KID_PUK_SIG = (byte) 0x02;

	public static final byte KID_PIN_SIG = (byte) 0x01;

	public static final byte[] CD_ID = new byte[] { (byte) 0x70, (byte) 0x05 };

	public static final byte[] MF_ID = new byte[] { (byte) 0x3f, (byte) 0x00 };

	public static final byte[] ADF_AWP_ID = new byte[] { (byte) 0xad,
			(byte) 0xf1 };

	public static final byte[] AID_SIG = new byte[] { (byte) 0xA0, (byte) 0x00,
			(byte) 0x00, (byte) 0x00, (byte) 0x77, (byte) 0x01, (byte) 0x08,
			(byte) 0x00, (byte) 0x07, (byte) 0x00, (byte) 0x00, (byte) 0xFE,
			(byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00 };

	PinInfo pinPinInfo, pukPinInfo;

	ObjectDirectory od;

	protected byte[] cert_id;

	@Override
	public void init(Card card, CardTerminal cardTerminal) {
		super.init(card, cardTerminal);

		log.info("Cypriot EID found");

		pinPinInfo = new PinInfo(4, 64, "[0-9]", "at/gv/egiz/smcc/CypriotEID",
				"sig.pin", KID_PIN_SIG, AID_SIG, 3);

		pukPinInfo = new PinInfo(4, 64, "[0-9]", "at/gv/egiz/smcc/CypriotEID",
				"sig.puk", KID_PUK_SIG, AID_SIG, 3);

		try {
			this.exec_readcd(getCardChannel());
		} catch (CardException e) {
			log.warn("Failed to read the certificate ID", e);
			cert_id = null;
		} catch (SignatureCardException e) {
			log.warn("Failed to read the certificate ID", e);
			cert_id = null;
		} catch (IOException e) {
			log.warn("Failed to read the certificate ID", e);
			cert_id = null;
		}
	}

	@Override
	public byte[] getCertificate(KeyboxName keyboxName, PINGUI pinGUI)
			throws SignatureCardException, InterruptedException {
		CardChannel channel = getCardChannel();

		try {
			return exec_readcert(channel);
		} catch (CardException e) {
			log.info("Failed to get the certificate.", e);
			throw new SignatureCardException("Failed to get the certificate.",
					e);
		} catch (IOException e) {
			log.info("Failed to get the certificate.", e);
			throw new SignatureCardException("Failed to get the certificate.",
					e);
		}
	}

	@Override
	public byte[] getInfobox(String infobox, PINGUI pinGUI, String domainId)
			throws SignatureCardException, InterruptedException {
		throw new IllegalArgumentException("Infobox '" + infobox
				+ "' not supported.");
	}

	@Override
	public byte[] createSignature(InputStream input, KeyboxName keyboxName,
			PINGUI pinGUI, String alg) throws SignatureCardException,
			InterruptedException, IOException {

		byte AlgID = 0;

		MessageDigest md;
		try {
			if (KeyboxName.SECURE_SIGNATURE_KEYPAIR.equals(keyboxName)
					&& (alg == null || "http://www.w3.org/2000/09/xmldsig#rsa-sha1"
							.equals(alg))) {
				AlgID = (byte) 0x12; // SHA-1 with padding according to PKCS#1
										// block type 01
				md = MessageDigest.getInstance("SHA-1");
			} else if (KeyboxName.SECURE_SIGNATURE_KEYPAIR.equals(keyboxName)
					&& "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256"
							.equals(alg)) {
				AlgID = (byte) 0x41; // SHA-256 with padding according to PKCS#1
				md = MessageDigest.getInstance("SHA-256");
			} else {
				throw new SignatureCardException(
						"Card does not support signature algorithm " + alg
								+ ".");
			}
		} catch (NoSuchAlgorithmException e) {
			log.error("Failed to get MessageDigest.", e);
			throw new SignatureCardException(e);
		}

		byte[] digest = new byte[md.getDigestLength()];
		for (int l; (l = input.read(digest)) != -1;) {
			md.update(digest, 0, l);
		}
		digest = md.digest();

		CardChannel channel = getCardChannel();

		try {
			try {
				// SELECT application
				exec_selectADF(channel);

				// MANAGE SECURITY ENVIRONMENT : SET DST
				exec_MSE(channel, AlgID);

				// PERFORM SECURITY OPERATION : COMPUTE DIGITAL SIGNATURE
				return exec_sign(channel, digest);
			} catch (SecurityStatusNotSatisfiedException e) {
				// NEED to provide PIN code ...

				// SELECT application
				exec_selectADF(channel);

				// MANAGE SECURITY ENVIRONMENT : SET DST
				exec_MSE(channel, AlgID);

				// VERIFY
				verifyPINLoop(channel, pinPinInfo, pinGUI);

				// PERFORM SECURITY OPERATION : COMPUTE DIGITAL SIGNATURE
				return exec_sign(channel, digest);
			}
		} catch (CardException e) {
			log.info("Failed to create digital signature", e);
			throw new SignatureCardException("Failed to create digital signature", e);
		}

	}

	@Override
	public PinInfo[] getPinInfos() throws SignatureCardException {
		PinInfo[] pinInfos = new PinInfo[] { pinPinInfo, pukPinInfo };
		
		CardChannel channel = getCardChannel();
		for (PinInfo pinInfo : pinInfos) {
		      if (pinInfo.getState() == PinInfo.STATE.UNKNOWN ) {
		        try {
		          log.debug("Query pin status for {}.", pinInfo.getLocalizedName());
		          testPIN(channel, pinInfo);
		        } catch (Exception e) {
		          log.trace("Failed to execute command.", e);
		          // status already set by verifyPIN
		        }
		      } else if (log.isTraceEnabled()) {
		        log.trace("assume pin status {} to be up to date", pinInfo.getState());
		      }
		    }
		
		return pinInfos;
	}

	@Override
	public void verifyPIN(PinInfo pinInfo, PINGUI pinGUI)
			throws LockedException, NotActivatedException, CancelledException,
			SignatureCardException, InterruptedException {
		CardChannel channel = getCardChannel();

		try {
			// SELECT application
			exec_selectADF(channel);
			// VERIFY
			verifyPINLoop(channel, pinInfo, pinGUI);
		} catch (CardException e) {
			log.info("Failed to verify PIN.", e);
			throw new SignatureCardException("Failed to verify PIN.", e);
		}
	}

	@Override
	  public String toString() {
	    return ("Oberthur Thechnologies ID-ONE Token SLIM");
	  }
	
	@Override
	public void changePIN(PinInfo pinInfo, ModifyPINGUI changePINGUI)
			throws LockedException, NotActivatedException, CancelledException,
			PINFormatException, SignatureCardException, InterruptedException {
		
		CardChannel channel = getCardChannel();

		try {
			unblockPINLoop(channel, changePINGUI, pinInfo);
		} catch (CardException e) {
			log.info("Failed to change PIN.", e);
			throw new SignatureCardException("Failed to change PIN.", e);
		}
	}

	@Override
	public void activatePIN(PinInfo pinInfo, ModifyPINGUI activatePINGUI)
			throws CancelledException, SignatureCardException,
			InterruptedException {
		log.error("ACTIVATE PIN not supported by Cypriotic EID");
		throw new SignatureCardException(
				"PIN activation not supported by this card.");
	}

	@Override
	public void unblockPIN(PinInfo pinInfo, ModifyPINGUI pukGUI)
			throws CancelledException, SignatureCardException,
			InterruptedException {
		CardChannel channel = getCardChannel();

		try {
			unblockPINLoop(channel, pukGUI, pinInfo);
		} catch (CardException e) {
			log.info("Failed to unblock PIN.", e);
			throw new SignatureCardException("Failed to unblock PIN.", e);
		}
	}

	// //////////////////////////////////////////////////////////////////////
	// PROTECTED METHODS (assume exclusive card access)
	// //////////////////////////////////////////////////////////////////////

	protected void verifyPINLoop(CardChannel channel, PinInfo spec,
			PINGUI provider) throws InterruptedException, CardException,
			SignatureCardException {

		int retries = -1;
		do {
			retries = verifyPIN(channel, spec, provider, retries);
		} while (retries > 0);
	}

	protected void unblockPINLoop(CardChannel channel,
			ModifyPINGUI provider, PinInfo pin) throws InterruptedException, CardException, SignatureCardException{

		int retries = -1;
		do {
			retries = exec_unblockPIN(channel, provider, pin);
		} while (retries > 0);
	}
	
	/*
	 * Verify PIN/PUK entry
	 */
	protected int verifyPIN(CardChannel channel, PinInfo pinInfo,
			PINGUI provider, int retries) throws InterruptedException,
			CardException, SignatureCardException {

		char[] pin = provider.providePIN(pinInfo, pinInfo.retries);

		byte[] ascii_pin = encodePIN(pin);
		exec_selectADF(channel);

		ResponseAPDU resp = channel.transmit(new CommandAPDU(0x00, 0x20, 0x00,
				pinInfo.getKID(), ascii_pin));

		if (resp.getSW() == 0x9000) {
			pinInfo.setActive(pinInfo.maxRetries);
			return -1;
		}
		if (resp.getSW() >> 4 == 0x63c) {
			pinInfo.setActive(0x0f & resp.getSW());
			return 0x0f & resp.getSW();
		}

		switch (resp.getSW()) {
		case 0x6983:
			// authentication method blocked
			pinInfo.setBlocked();
			throw new LockedException();

		default:
			String msg = "VERIFY failed. SW="
					+ Integer.toHexString(resp.getSW());
			log.info(msg);
			pinInfo.setUnknown();
			throw new SignatureCardException(msg);
		}

	}
	
	protected int testPIN(CardChannel channel, PinInfo pinInfo) throws InterruptedException,
			CardException, SignatureCardException {

		exec_selectADF(channel);

		ResponseAPDU resp = channel.transmit(new CommandAPDU(0x00, 0x20, 0x00,
				pinInfo.getKID()));

		if (resp.getSW() == 0x9000) {
			pinInfo.setActive(pinInfo.maxRetries);
			return -1;
		}
		if (resp.getSW() >> 4 == 0x63c) {
			pinInfo.setActive(0x0f & resp.getSW());
			return 0x0f & resp.getSW();
		}

		switch (resp.getSW()) {
		case 0x6983:
			// authentication method blocked
			pinInfo.setBlocked();
			throw new LockedException();

		default:
			String msg = "VERIFY failed. SW="
					+ Integer.toHexString(resp.getSW());
			log.info(msg);
			pinInfo.setUnknown();
			throw new SignatureCardException(msg);
		}

	}

	private byte[] encodePIN(char[] pin) {
		return Charset.forName("ASCII").encode(CharBuffer.wrap(pin)).array();
	}

	/*
	 * Unblock PIN with PUK code
	 */
	protected int exec_unblockPIN(CardChannel channel, ModifyPINGUI changePINGUI, PinInfo pin)
			throws InterruptedException, CardException, SignatureCardException {
		
		
		char[] PUK = changePINGUI.providePUK(pin, pukPinInfo,
				pukPinInfo.retries);
		
		char[] newPIN = changePINGUI.provideNewPIN(pin);
		
		byte[] ascii_puk = encodePIN(PUK);

		byte[] ascii_pin = encodePIN(newPIN);

		exec_selectADF(channel);

		ResponseAPDU resp = channel.transmit(new CommandAPDU(0x00, 0x20, 0x00,
				pukPinInfo.getKID(), ascii_puk));

		if (resp.getSW() == 0x9000) {
			pukPinInfo.setActive(pukPinInfo.maxRetries);
		} else if (resp.getSW() >> 4 == 0x63c) {
			pukPinInfo.setActive(0x0f & resp.getSW());
			return 0x0f & resp.getSW();
		} else if (resp.getSW() == 0x6983) {
			// authentication method blocked
			pukPinInfo.setBlocked();
			throw new LockedException();
		} else {
			String msg = "VERIFY failed. SW="
					+ Integer.toHexString(resp.getSW());
			log.info(msg);
			pukPinInfo.setUnknown();
			throw new SignatureCardException(msg);
		}

		resp = channel.transmit(new CommandAPDU(0x00, 0x2C, 0x02, pin
				.getKID(), ascii_pin));

		if (resp.getSW() == 0x9000) {
			pin.setActive(pin.maxRetries);
			return -1;
		} else {
			String msg = "SET PIN failed. SW="
					+ Integer.toHexString(resp.getSW());
			log.info(msg);
			pin.setUnknown();
			throw new SignatureCardException(msg);
		}

	}

	/*
	 * Read certificate based on certificate ID
	 */
	protected byte[] exec_readcert(CardChannel channel) throws CardException,
			SignatureCardException, IOException {
		if (cert_id == null) {
			exec_readcd(channel);
		}

		if (cert_id == null) {
			throw new CardException("Failed to read the certificate id");
		}

		exec_selectADF(channel);
		exec_selectFILE(channel, cert_id);

		return exec_readBinary(channel);
	}

	/*
	 * Read and parse CD file to determine certificate ID
	 */
	protected void exec_readcd(CardChannel channel) throws CardException,
			SignatureCardException, IOException {
		exec_selectADF(channel);
		exec_selectFILE(channel, CD_ID);

		byte[] cd_buffer = exec_readBinary(channel);

		ASN1 asn = new ASN1(cd_buffer);

		for (int i = 0; i < asn.getSize(); i++) {

			ASN1 element = asn.getElementAt(i);

			if (element.getTagClass() == ASN1.TAG_CONTEXT_SPECIFIC
					&& element.getTypeOnly() == ASN1.TYPE_BOOLEAN) {
				ASN1 ele = element.gvASN1().getElementAt(0).getElementAt(0);
				if (ele.getTypeOnly() == ASN1.TYPE_OCTET_STRING) {
					cert_id = ele.gvByteArray();
					return;
				}
			}
		}
		cert_id = null;
		throw new CardException("Failed to read the certificate ID.");
	}

	/*
	 * Select the ADF application
	 */
	protected void exec_selectADF(CardChannel channel) throws CardException,
			SignatureCardException {

		exec_selectFILE(channel, MF_ID);
		exec_selectFILE(channel, ADF_AWP_ID);

	}

	/*
	 * Select a file in the current context by its id
	 */
	protected byte[] exec_selectFILE(CardChannel channel, byte[] file_id)
			throws CardException, SignatureCardException {
		ResponseAPDU resp = channel.transmit(new CommandAPDU(0x00, 0xA4, 0x01,
				0x0C, file_id));

		if (resp.getSW() != 0x9000) {
			String msg = "Failed to select File="
					+ SMCCHelper.toString(file_id) + " SW="
					+ Integer.toHexString(resp.getSW()) + ".";
			log.info(msg);
			throw new SignatureCardException(msg);
		}

		return resp.getBytes();
	}

	/*
	 * Setup Manage Security Environment (MSE) for cryptographic signatur !fixed
	 * key id
	 */
	protected void exec_MSE(CardChannel channel, byte algoID)
			throws CardException {
		byte[] secure_setup = new byte[] { (byte) 0x80, (byte) 0x01, algoID, // Algorithm
																				// setup
				(byte) 0x84, (byte) 0x01, (byte) 0x81 }; // Key setup

		ResponseAPDU resp = channel.transmit(new CommandAPDU(0x00, 0x22, 0x41,
				0xB6, secure_setup));
	}

	/*
	 * Execute signature command
	 */
	protected byte[] exec_sign(CardChannel channel, byte[] hash)
			throws CardException, SignatureCardException {
		ResponseAPDU resp = channel.transmit(new CommandAPDU(0x00, 0x2A, 0x9E,
				0x9A, hash));

		if (resp.getSW() == 0x6982) {
			throw new SecurityStatusNotSatisfiedException();
		}
		if (resp.getSW() != 0x9000) {
			throw new SignatureCardException(
					"PSO - COMPUTE DIGITAL SIGNATURE failed: SW="
							+ Integer.toHexString(resp.getSW()));
		} else {
			return resp.getData();
		}
	}

	/*
	 * Read current binary information
	 */
	protected byte[] exec_readBinary(CardChannel channel) throws CardException,
			IOException, SignatureCardException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		boolean repeat = true;

		int offset = 0;

		do {
			int offset_lo = (byte) offset;
			int offset_hi = (byte) (offset >> 8);

			ResponseAPDU resp = channel.transmit(new CommandAPDU(0x00, 0xB0,
					offset_hi, offset_lo, 0x00));

			if (resp.getSW() != 0x9000) {
				String msg = "Failed to read binary SW="
						+ Integer.toHexString(resp.getSW()) + ".";
				log.info(msg);
				throw new SignatureCardException(msg);
			}

			byte[] data = resp.getData();

			buffer.write(data);

			repeat = data.length == 231;

			offset += data.length;
		} while (repeat);

		byte[] buf = buffer.toByteArray();

		return buf;
	}

}
