package at.gv.egiz.smcc;

import iaik.me.security.CryptoBag;
import iaik.me.security.CryptoException;
import iaik.me.security.MessageDigest;
import iaik.me.security.cipher.TripleDES;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sun.security.rsa.RSAPadding;
import at.gv.egiz.smcc.pin.gui.ModifyPINGUI;
import at.gv.egiz.smcc.pin.gui.PINGUI;
import at.gv.egiz.smcc.util.MSCMException;
import at.gv.egiz.smcc.util.MSCMService;

public class GemaltoNetV2_0Card extends AbstractSignatureCard implements
		PINMgmtSignatureCard {

	private static final String KSC_PATTERN = "ksc(\\p{XDigit}\\p{XDigit})";
	private static final String CERT_FILE = "mscp\\ksc";

	private static final String CONTAINER_00 = "00";
	private static final String CONTAINER_01 = "01";
	private static final String CONTAINER_02 = "02";
	private static final String CONTAINER_03 = "03";
	private static final String CONTAINER_04 = "04";
	private static final String CONTAINER_05 = "05";
	private static final String CONTAINER_06 = "06";
	private static final String CONTAINER_07 = "07";
	private static final String CONTAINER_08 = "08";
	private static final String CONTAINER_09 = "09";
	private static final String CONTAINER_10 = "10";
	private static final String CONTAINER_11 = "11";
	private static final String CONTAINER_12 = "12";
	private static final String CONTAINER_13 = "13";
	private static final String CONTAINER_14 = "14";

	private static final KeyboxName KEYBOX_CONTAINER_00 = KeyboxName
			.getKeyboxName(CONTAINER_00);
	private static final KeyboxName KEYBOX_CONTAINER_01 = KeyboxName
			.getKeyboxName(CONTAINER_01);
	private static final KeyboxName KEYBOX_CONTAINER_02 = KeyboxName
			.getKeyboxName(CONTAINER_02);
	private static final KeyboxName KEYBOX_CONTAINER_03 = KeyboxName
			.getKeyboxName(CONTAINER_03);
	private static final KeyboxName KEYBOX_CONTAINER_04 = KeyboxName
			.getKeyboxName(CONTAINER_04);
	private static final KeyboxName KEYBOX_CONTAINER_05 = KeyboxName
			.getKeyboxName(CONTAINER_05);
	private static final KeyboxName KEYBOX_CONTAINER_06 = KeyboxName
			.getKeyboxName(CONTAINER_06);
	private static final KeyboxName KEYBOX_CONTAINER_07 = KeyboxName
			.getKeyboxName(CONTAINER_07);
	private static final KeyboxName KEYBOX_CONTAINER_08 = KeyboxName
			.getKeyboxName(CONTAINER_08);
	private static final KeyboxName KEYBOX_CONTAINER_09 = KeyboxName
			.getKeyboxName(CONTAINER_09);
	private static final KeyboxName KEYBOX_CONTAINER_10 = KeyboxName
			.getKeyboxName(CONTAINER_10);
	private static final KeyboxName KEYBOX_CONTAINER_11 = KeyboxName
			.getKeyboxName(CONTAINER_11);
	private static final KeyboxName KEYBOX_CONTAINER_12 = KeyboxName
			.getKeyboxName(CONTAINER_12);
	private static final KeyboxName KEYBOX_CONTAINER_13 = KeyboxName
			.getKeyboxName(CONTAINER_13);
	private static final KeyboxName KEYBOX_CONTAINER_14 = KeyboxName
			.getKeyboxName(CONTAINER_14);

	private final Logger log = LoggerFactory
			.getLogger(GemaltoNetV2_0Card.class);

	PinInfo pinPinInfo;
	PinInfo pukPinInfo;

	Pattern pattern;

	private final byte[] SHA1_PADDING = new byte[] {

	(byte) 0x30, (byte) 0x21, (byte) 0x30, (byte) 0x09, (byte) 0x06,
			(byte) 0x05, (byte) 0x2B, (byte) 0x0E, (byte) 0x03, (byte) 0x02,
			(byte) 0x1A, (byte) 0x05, (byte) 0x00, (byte) 0x04, (byte) 0x14 };

	private final byte[] SHA256_PADDING = new byte[] {

	(byte) 0x30, (byte) 0x31, (byte) 0x30, (byte) 0x0d, (byte) 0x06,
			(byte) 0x09, (byte) 0x60, (byte) 0x86, (byte) 0x48, (byte) 0x01,
			(byte) 0x65, (byte) 0x03, (byte) 0x04, (byte) 0x02, (byte) 0x01,
			(byte) 0x05, (byte) 0x00, (byte) 0x04, (byte) 0x20 };

	public void init(Card card, CardTerminal cardTerminal) {
		super.init(card, cardTerminal);
		pattern = Pattern.compile(KSC_PATTERN);
		log.info("GemaltoNetV2 card found");

		pinPinInfo = new PinInfo(4, 64, "[0-9]",
				"at/gv/egiz/smcc/GemaltoNetV2_0Card", "sig.pin", (byte) 1,
				new byte[] {}, 5);

		pukPinInfo = new PinInfo(48, 48, "[0-9A-F]",
				"at/gv/egiz/smcc/GemaltoNetV2_0Card", "sig.puk", (byte) 2,
				new byte[] {}, 3);
	}

	public static byte[] hexStringToByteArray(String s) {
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character
					.digit(s.charAt(i + 1), 16));
		}
		return data;
	}

	private int findKeyIndex() throws IOException, CardException, MSCMException {
		MSCMService service = new MSCMService(this.getCardChannel());
		String[] files = service.getFiles("mscp");
		int currentMax = -1;
		for (int i = 0; i < files.length; i++) {
			String fileName = files[i];
			log.trace("Checking file: " + files[i]);
			Matcher matcher = pattern.matcher(fileName);
			if (matcher.find()) {
				if (matcher.groupCount() == 1) {
					String fidx = matcher.group(1);
					log.trace("Found idx: " + fidx);
					try {
						int curr = Integer.parseInt(fidx, 16);
						if (currentMax < curr) {
							currentMax = curr;
						}
					} catch (NumberFormatException e) {
						log.warn("Matched Regex but is no integer!", e);
					}
				}
			}
		}
		return currentMax;
	}

	private String getCertificateFileFormIdx(int idx) {
		return String.format("%s%02x", CERT_FILE, idx);
	}

	private int resolveKeyboxIdx(KeyboxName keyboxName) {
		if (keyboxName.equals(KEYBOX_CONTAINER_00)) {
			return 0;
		} else if (keyboxName.equals(KEYBOX_CONTAINER_01)) {
			return 1;
		} else if (keyboxName.equals(KEYBOX_CONTAINER_02)) {
			return 2;
		} else if (keyboxName.equals(KEYBOX_CONTAINER_03)) {
			return 3;
		} else if (keyboxName.equals(KEYBOX_CONTAINER_04)) {
			return 4;
		} else if (keyboxName.equals(KEYBOX_CONTAINER_05)) {
			return 5;
		} else if (keyboxName.equals(KEYBOX_CONTAINER_06)) {
			return 6;
		} else if (keyboxName.equals(KEYBOX_CONTAINER_07)) {
			return 7;
		} else if (keyboxName.equals(KEYBOX_CONTAINER_08)) {
			return 8;
		} else if (keyboxName.equals(KEYBOX_CONTAINER_09)) {
			return 9;
		} else if (keyboxName.equals(KEYBOX_CONTAINER_10)) {
			return 10;
		} else if (keyboxName.equals(KEYBOX_CONTAINER_11)) {
			return 11;
		} else if (keyboxName.equals(KEYBOX_CONTAINER_12)) {
			return 12;
		} else if (keyboxName.equals(KEYBOX_CONTAINER_13)) {
			return 13;
		} else if (keyboxName.equals(KEYBOX_CONTAINER_14)) {
			return 14;
		}
		return -1;
	}
	
	private String resolveKeybox(KeyboxName keyboxName) {
		int idx = resolveKeyboxIdx(keyboxName);
		if(idx < 0) {
			return null;
		}
		
		return getCertificateFileFormIdx(idx);
	}

	@Override
	public byte[] getCertificate(KeyboxName keyboxName, PINGUI pinGUI)
			throws SignatureCardException, InterruptedException {
		try {
			CardChannel channel = this.getCardChannel();// this.getCard().getBasicChannel();
			MSCMService service = new MSCMService(channel);
			String certFile = resolveKeybox(keyboxName);
			
			if(certFile == null) {
				int idx = findKeyIndex();
				if(idx < 0) {
					throw new SignatureCardException("There is no certificate available on this token!");
				}
				certFile = getCertificateFileFormIdx(idx);
			}
			
			log.info("Reading certificate: " + certFile);
			
			byte[] filecnt = service.readFile(certFile);

			Inflater inflater = new Inflater();
			inflater.setInput(filecnt, 4, filecnt.length - 4);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			while (!inflater.finished()) {
				byte[] uncompressedData = new byte[1024];
				int compressLength = inflater.inflate(uncompressedData, 0,
						uncompressedData.length);
				// System.out.println(compressedData);
				out.write(uncompressedData, 0, compressLength);
			}
			byte[] uncompressed = out.toByteArray();
			return uncompressed;
		} catch (CardException e) {
			log.info("Failed to get certificate.", e);
			throw new SignatureCardException(e);
		} catch (DataFormatException e) {
			log.info("Failed to get certificate.", e);
			throw new SignatureCardException(e);
		} catch (IOException e) {
			log.info("Failed to get certificate.", e);
			throw new SignatureCardException(e);
		} catch (MSCMException e) {
			log.info("Failed to get certificate.", e);
			throw new SignatureCardException(e);
		}
	}

	@Override
	public byte[] getInfobox(String infobox, PINGUI pinGUI, String domainId)
			throws SignatureCardException, InterruptedException {
		return getCertificate(KeyboxName.getKeyboxName(infobox), pinGUI);
	}

	@Override
	public byte[] createSignature(InputStream input, KeyboxName keyboxName,
			PINGUI pinGUI, String alg) throws SignatureCardException,
			InterruptedException, IOException {

		boolean sha1 = false;
		MessageDigest md;
		int ctrIdx = -1;
		try {
			
			ctrIdx = resolveKeyboxIdx(keyboxName);
			
			if(ctrIdx < 0) {
				ctrIdx = findKeyIndex();
				if(ctrIdx < 0) {
					throw new SignatureCardException("There is no private key available on this token!");
				}
			}
			
			if(ctrIdx > 14) {
				log.error("Key Index is bigger the maximum container available");
				throw new SignatureCardException("There is no private key available on this token!");
			}
			
			log.info("Signing with key: " + ctrIdx);
			
			if (alg == null || "http://www.w3.org/2000/09/xmldsig#rsa-sha1"
							.equals(alg)) {
				md = MessageDigest.getInstance("SHA-1");
				sha1 = true;
			} else if ("http://www.w3.org/2001/04/xmldsig-more#rsa-sha256"
							.equals(alg)) {
				md = MessageDigest.getInstance("SHA-256");
			} else {
				throw new SignatureCardException(
						"Card does not support signature algorithm " + alg
								+ ".");
			}
		} catch (CryptoException e) {
			log.error("Failed to get MessageDigest.", e);
			throw new SignatureCardException(e);
		} catch (CardException e) {
			log.error("Failed to get MessageDigest.", e);
			throw new SignatureCardException(e);
		} catch (MSCMException e) {
			log.error("Failed to get MessageDigest.", e);
			throw new SignatureCardException(e);
		}

		byte[] digest = new byte[md.getDigestLength()];
		for (int l; (l = input.read(digest)) != -1;) {
			md.update(digest, 0, l);
		}
		digest = md.digest();

		try {
			RSAPadding padding = RSAPadding.getInstance(
					RSAPadding.PAD_BLOCKTYPE_1, 256);

			CardChannel channel = this.getCardChannel();
			MSCMService service = new MSCMService(channel);

			verifyPINLoop(channel, pinPinInfo, pinGUI);

			ByteArrayOutputStream fdata = new ByteArrayOutputStream();

			if (sha1) {
				fdata.write(SHA1_PADDING);
			} else {
				fdata.write(SHA256_PADDING);
			}
			fdata.write(digest);
			fdata.close();
			byte[] msg = fdata.toByteArray();
			byte[] paded = padding.pad(msg);
			byte[] sign = service.privateKeyDecrypt((byte) ctrIdx, (byte) 2, paded);
			return sign;
		} catch (Throwable e) {
			log.warn("Failed to execute command.", e);
			throw new SignatureCardException("Failed to access card.", e);
		}
	}

	protected void unblockPINLoop(CardChannel channel, ModifyPINGUI provider,
			PinInfo pin) throws InterruptedException, CardException,
			SignatureCardException {

		int retries = -1;
		do {
			retries = exec_unblockPIN(channel, provider, pin);
		} while (retries > 0);
	}

	/*
	 * Unblock PIN with PUK code
	 */
	protected int exec_unblockPIN(CardChannel channel,
			ModifyPINGUI changePINGUI, PinInfo pin)
			throws InterruptedException, CardException, SignatureCardException {

		char[] oldPIN = changePINGUI.providePUK(pin, pukPinInfo,
				pukPinInfo.retries);

		char[] newPIN = changePINGUI.provideNewPIN(pin);

		byte[] ascii_pin = encodePIN(newPIN);

		MSCMService service = new MSCMService(channel);

		try {
			byte[] key = hexStringToByteArray(new String(oldPIN));
			if (key.length != 24) {
				throw new SignatureCardException(
						"Invalid ADMIN PIN (not 24 bytes long)!");
			}
			byte[] challenge = service.getChallenge();
			byte[] response = service.cryptoResponse(challenge, key);
			service.unblockPIN((byte) 1, response, ascii_pin, pin.maxRetries);
			pin.setActive(pin.maxRetries);
			return -1;
		} catch (IOException e) {
			String msg = "SET PIN failed.";
			log.info(msg);
			pin.setUnknown();
			throw new SignatureCardException(msg, e);
		} catch (MSCMException e) {
			log.info(e.getMessage());
			try {
				return service.getTriesRemaining((byte) 2);
			} catch (IOException ec) {
				String msg = "getTriesRemaining failed.";
				log.info(msg);
				pin.setUnknown();
				throw new SignatureCardException(msg, ec);
			} catch (MSCMException e1) {
				String msg = "getTriesRemaining failed.";
				log.info(msg);
				pin.setUnknown();
				throw new SignatureCardException(msg, e1);
			}
		}

	}

	protected void verifyPINLoop(CardChannel channel, PinInfo spec,
			PINGUI provider) throws InterruptedException, CardException,
			SignatureCardException {

		int retries = -1;
		do {
			retries = verifyPIN(channel, spec, provider, retries);
		} while (retries > 0);
	}

	/*
	 * Verify PIN/PUK entry
	 */
	protected int verifyPUK(CardChannel channel, PinInfo pinInfo,
			PINGUI provider, int retries) throws InterruptedException,
			CardException, SignatureCardException {

		char[] pin = provider.providePIN(pinInfo, pinInfo.retries);

		byte[] ascii_pin = hexStringToByteArray(new String(pin));

		if (ascii_pin.length != 24) {
			throw new SignatureCardException(
					"Invalid ADMIN PIN (not 24 bytes long)!");
		}

		MSCMService service = new MSCMService(channel);

		try {
			byte[] challenge = service.getChallenge();
			byte[] response = service.cryptoResponse(challenge, ascii_pin);
			service.doExternalAuthentication(response);
			pinInfo.setActive(pinInfo.maxRetries);
			return -1;
		} catch (MSCMException e) {
			try {
				int tries = service.getTriesRemaining(pinInfo.getKID());
				pinInfo.setActive(tries);
				return tries;
			} catch (Exception ex) {
				log.error("Failed to get remaining tries");
				throw new SignatureCardException(ex);
			}
		} catch (IOException e) {
			log.error("Failed to verify PIN");
			throw new SignatureCardException(e);
		}
	}

	/*
	 * Verify PIN/PUK entry
	 */
	protected int verifyPIN(CardChannel channel, PinInfo pinInfo,
			PINGUI provider, int retries) throws InterruptedException,
			CardException, SignatureCardException {

		char[] pin = provider.providePIN(pinInfo, pinInfo.retries);

		byte[] ascii_pin = encodePIN(pin);
		MSCMService service = new MSCMService(channel);

		try {
			service.verifyPin(pinInfo.getKID(), ascii_pin);
			pinInfo.setActive(pinInfo.maxRetries);
			return -1;
		} catch (MSCMException e) {
			try {
				int tries = service.getTriesRemaining(pinInfo.getKID());
				if (tries == 0) {
					pinInfo.setBlocked();
					throw new LockedException();
				}
				pinInfo.setActive(tries);
				return tries;
			} catch (IOException ex) {
				log.error("Failed to get remaining tries");
				throw new SignatureCardException(ex);
			} catch (MSCMException e1) {
				log.error("Failed to get remaining tries");
				throw new SignatureCardException(e1);
			}
		} catch (IOException e) {
			log.error("Failed to verify PIN");
			throw new SignatureCardException(e);
		}
	}

	protected void changePINLoop(CardChannel channel, ModifyPINGUI provider,
			PinInfo pin) throws InterruptedException, CardException,
			SignatureCardException {

		int retries = -1;
		do {
			if (pin.getKID() == 2) {
				retries = exec_changePUK(channel, provider, pin);
			} else {
				retries = exec_changePIN(channel, provider, pin);
			}
		} while (retries > 0);
	}

	protected byte[] cryptoChallenge(byte[] challenge, byte[] pass) {
		try {
			byte[] iv = new byte[] { (byte) 0x00, (byte) 0x00, (byte) 0x00,
					(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
					(byte) 0x00 };
			TripleDES cip = new TripleDES();
			cip.init(TripleDES.ENCRYPT_MODE, CryptoBag.makeSecretKey(pass),
					CryptoBag.makeIV(iv), null);
			log.info("Crypto IV: "
					+ MSCMService.bytArrayToHex(cip.getIV().getEncoded()));
			byte[] result = cip.doFinal(challenge);
			log.info("Crypto result: " + MSCMService.bytArrayToHex(result));
			return result;
		} catch (CryptoException e) {
			log.error("Failed to get crypto stuff", e);
			return null;
		}
	}

	/*
	 * Unblock PIN with PUK code
	 */
	protected int exec_changePUK(CardChannel channel,
			ModifyPINGUI changePINGUI, PinInfo pin)
			throws InterruptedException, CardException, SignatureCardException {

		char[] oldPIN = changePINGUI.providePUK(pin, pukPinInfo,
				pukPinInfo.retries);

		char[] newPIN = changePINGUI.provideNewPIN(pin);

		MSCMService service = new MSCMService(channel);

		try {
			byte[] key = hexStringToByteArray(new String(oldPIN));
			byte[] keynew = hexStringToByteArray(new String(newPIN));
			if (key.length != 24) {
				throw new SignatureCardException(
						"Invalid ADMIN PIN (not 24 bytes long)!");
			}
			if (keynew.length != 24) {
				throw new SignatureCardException(
						"Invalid ADMIN PIN (not 24 bytes long)!");
			}
			byte[] challenge = service.getChallenge();
			byte[] response = service.cryptoResponse(challenge, key);
			service.changePIN((byte) 2, response, keynew, pin.maxRetries);
			pin.setActive(pin.maxRetries);
			return -1;
		} catch (IOException e) {
			String msg = "SET PIN failed.";
			log.info(msg);
			pin.setUnknown();
			throw new SignatureCardException(msg, e);
		} catch (MSCMException e) {
			log.info(e.getMessage());
			try {
				int tries = service.getTriesRemaining(pin.getKID());
				if (tries == 0) {
					pin.setBlocked();
					throw new LockedException();
				}
				pin.setActive(tries);
				return tries;
			} catch (IOException ec) {
				String msg = "getTriesRemaining failed.";
				log.info(msg);
				pin.setUnknown();
				throw new SignatureCardException(msg, ec);
			} catch (MSCMException e1) {
				String msg = "getTriesRemaining failed.";
				log.info(msg);
				pin.setUnknown();
				throw new SignatureCardException(msg, e1);
			}
		}

	}

	/*
	 * Unblock PIN with PUK code
	 */
	protected int exec_changePIN(CardChannel channel,
			ModifyPINGUI changePINGUI, PinInfo pin)
			throws InterruptedException, CardException, SignatureCardException {

		char[] oldPIN = changePINGUI.providePUK(pin, pinPinInfo,
				pinPinInfo.retries);

		char[] newPIN = changePINGUI.provideNewPIN(pin);

		byte[] ascii_puk = encodePIN(oldPIN);

		byte[] ascii_pin = encodePIN(newPIN);

		MSCMService service = new MSCMService(channel);

		try {
			// service -> change pin
			service.changePIN((byte) 1, ascii_puk, ascii_pin, pin.maxRetries);
			pin.setActive(pin.maxRetries);
			return -1;
		} catch (IOException e) {
			String msg = "SET PIN failed.";
			log.info(msg);
			pin.setUnknown();
			throw new SignatureCardException(msg, e);
		} catch (MSCMException e) {
			log.info(e.getMessage());
			try {
				int tries = service.getTriesRemaining(pin.getKID());
				if (tries == 0) {
					pin.setBlocked();
					throw new LockedException();
				}
				pin.setActive(tries);
				return tries;
			} catch (IOException ec) {
				String msg = "getTriesRemaining failed.";
				log.info(msg);
				pin.setUnknown();
				throw new SignatureCardException(msg, ec);
			} catch (MSCMException e1) {
				String msg = "getTriesRemaining failed.";
				log.info(msg);
				pin.setUnknown();
				throw new SignatureCardException(msg, e1);
			}
		}

	}

	private byte[] encodePIN(char[] pin) {
		return Charset.forName("ASCII").encode(CharBuffer.wrap(pin)).array();
	}

	// @Override
	public PinInfo[] getPinInfos() throws SignatureCardException {
		return new PinInfo[] { pinPinInfo, pukPinInfo };
	}

	// @Override
	public void verifyPIN(PinInfo pinInfo, PINGUI pinGUI)
			throws LockedException, NotActivatedException, CancelledException,
			SignatureCardException, InterruptedException {

		try {
			CardChannel channel = this.getCardChannel();
			if (pinInfo.getKID() == 2) {
				verifyPUK(channel, pinInfo, pinGUI, pinInfo.retries);
			} else {
				verifyPIN(channel, pinInfo, pinGUI, pinInfo.retries);
			}
		} catch (CardException e) {
			log.error("Failed to verify PIN");
			throw new SignatureCardException(e);
		}
	}

	// @Override
	public void changePIN(PinInfo pinInfo, ModifyPINGUI changePINGUI)
			throws LockedException, NotActivatedException, CancelledException,
			PINFormatException, SignatureCardException, InterruptedException {
		try {
			changePINLoop(getCardChannel(), changePINGUI, pinInfo);
		} catch (CardException e) {
			log.error("Failed to change PIN");
			throw new SignatureCardException(e);
		}
	}

	// @Override
	public void activatePIN(PinInfo pinInfo, ModifyPINGUI activatePINGUI)
			throws CancelledException, SignatureCardException,
			InterruptedException {
		log.error("ACTIVATE PIN not supported by Cypriotic EID");
		throw new SignatureCardException(
				"PIN activation not supported by this card.");
	}

	// @Override
	public void unblockPIN(PinInfo pinInfo, ModifyPINGUI pukGUI)
			throws CancelledException, SignatureCardException,
			InterruptedException {
		if (pinInfo.getKID() == 2) {
			throw new SignatureCardException("Unable to unblock PUK");
		} else {
			CardChannel channel = getCardChannel();

			try {
				unblockPINLoop(channel, pukGUI, pinInfo);
			} catch (CardException e) {
				log.info("Failed to unblock PIN.", e);
				throw new SignatureCardException("Failed to unblock PIN.", e);
			}
		}
	}
}
