package at.gv.egiz.smcc;

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

public class DNIeCard extends AbstractT0SignatureCard implements SignatureCard {

	private final Logger log = LoggerFactory.getLogger(DNIeCard.class);

	private final byte[] MASTER_FILE_ID = new byte[] {

	(byte) 0x4D, (byte) 0x61, (byte) 0x73, (byte) 0x74, (byte) 0x65,
			(byte) 0x72, (byte) 0x2E, (byte) 0x46, (byte) 0x69, (byte) 0x6C,
			(byte) 0x65 };

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
			verifyPINLoop(channel, pinInfo, pinGUI);

			secureChannel.executeSecureManageSecurityEnvironment(channel);

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

		if (!secureChannel.isEstablished())
			try {
				secureChannel.establish(channel);
			} catch (CardException e) {

				log.debug("Error establishing secure channel to card.", e);
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

			// select 7005
			byte[] apdu2 = new byte[] {

			(byte) 0x00, (byte) 0xA4, (byte) 0x00, (byte) 0x00, (byte) 0x02,
					(byte) 0x70, (byte) 0x05 };

			byte[] fci = secureChannel.executeSecureSelect(channel, apdu2);

			byte sizeHi = fci[7];
			byte sizeLo = fci[8];

			byte[] data = secureChannel.executeSecureReadBinary(channel,
					sizeHi, sizeLo);

			int uncompressedDataLen = getUncompressedDataLength(data);

			byte[] compressedWithoutHeader = new byte[data.length - 8];
			System.arraycopy(data, 8, compressedWithoutHeader, 0,
					compressedWithoutHeader.length);

			result = decompressData(compressedWithoutHeader,
					uncompressedDataLen);

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

	private int getUncompressedDataLength(byte[] data) {

		byte len0 = data[0];
		byte len1 = data[1];
		byte len2 = data[2];
		byte len3 = data[3];

		int a = len0;
		int b = len1 * 256;
		int c = len2 * 256 * 256;
		int d = len3 * 256 * 256 * 256;

		return a + b + c + d;
	}

	private byte[] decompressData(byte[] input, int len) throws CardException {

		Inflater decompresser = new Inflater();
		decompresser.setInput(input, 0, input.length);
		byte[] result = new byte[len];

		try {
			decompresser.inflate(result);
			decompresser.end();

			return result;

		} catch (DataFormatException e) {

			throw new CardException("Error decompressing file.", e);
		}
	}

}
