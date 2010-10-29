package at.gv.egiz.smcc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.KeySpec;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Arrays;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DNIeCardSecureChannel {

	private final Logger log = LoggerFactory
			.getLogger(DNIeCardSecureChannel.class);

	private final byte[] APDU_DATA_MSE_LOAD_TERMINAL_CERTS = new byte[] {
			(byte) 0x83, (byte) 0x02, (byte) 0x02, (byte) 0x0F };

	private final String TERMINAL_MODULO = "DB2CB41E112BACFA2BD7C3D3D7967E84FB9434FC261F9D090A8983947DAF8488D3DF8FBDCC1F92493585E134A1B42DE519F463244D7ED384E26D516CC7A4FF7895B1992140043AACADFC12E856B202346AF8226B1A882137DC3C5A57F0D2815C1FCD4BB46FA9157FDFFD79EC3A10A824CCC1EB3CE0B6B4396AE236590016BA69";
	private final String TERMINAL_PRIVEXP = "18B44A3D155C61EBF4E3261C8BB157E36F63FE30E9AF28892B59E2ADEB18CC8C8BAD284B9165819CA4DEC94AA06B69BCE81706D1C1B668EB128695E5F7FEDE18A908A3011A646A481D3EA71D8A387D474609BD57A882B182E047DE80E04B4221416BD39DFA1FAC0300641962ADB109E28CAF50061B68C9CABD9B00313C0F46ED";

	private static final String ROOT_CA_MODULO = "EADEDA455332945039DAA404C8EBC4D3B7F5DC869283CDEA2F101E2AB54FB0D0B03D8F030DAF2458028288F54CE552F8FA57AB2FB103B112427E11131D1D27E10A5B500EAAE5D940301E30EB26C3E9066B257156ED639D70CCC090B863AFBB3BFED8C17BE7673034B9823E977ED657252927F9575B9FFF6691DB64F80B5E92CD";
	private static final String ROOT_CA_PUBEXP = "010001";

	private final byte[] C_CV_CA = new byte[] {

	(byte) 0x7F, (byte) 0x21, (byte) 0x81, (byte) 0xCE, (byte) 0x5F,
			(byte) 0x37, (byte) 0x81, (byte) 0x80, (byte) 0x3C, (byte) 0xBA,
			(byte) 0xDC, (byte) 0x36, (byte) 0x84, (byte) 0xBE, (byte) 0xF3,
			(byte) 0x20, (byte) 0x41, (byte) 0xAD, (byte) 0x15, (byte) 0x50,
			(byte) 0x89, (byte) 0x25, (byte) 0x8D, (byte) 0xFD, (byte) 0x20,
			(byte) 0xC6, (byte) 0x91, (byte) 0x15, (byte) 0xD7, (byte) 0x2F,
			(byte) 0x9C, (byte) 0x38, (byte) 0xAA, (byte) 0x99, (byte) 0xAD,
			(byte) 0x6C, (byte) 0x1A, (byte) 0xED, (byte) 0xFA, (byte) 0xB2,
			(byte) 0xBF, (byte) 0xAC, (byte) 0x90, (byte) 0x92, (byte) 0xFC,
			(byte) 0x70, (byte) 0xCC, (byte) 0xC0, (byte) 0x0C, (byte) 0xAF,
			(byte) 0x48, (byte) 0x2A, (byte) 0x4B, (byte) 0xE3, (byte) 0x1A,
			(byte) 0xFD, (byte) 0xBD, (byte) 0x3C, (byte) 0xBC, (byte) 0x8C,
			(byte) 0x83, (byte) 0x82, (byte) 0xCF, (byte) 0x06, (byte) 0xBC,
			(byte) 0x07, (byte) 0x19, (byte) 0xBA, (byte) 0xAB, (byte) 0xB5,
			(byte) 0x6B, (byte) 0x6E, (byte) 0xC8, (byte) 0x07, (byte) 0x60,
			(byte) 0xA4, (byte) 0xA9, (byte) 0x3F, (byte) 0xA2, (byte) 0xD7,
			(byte) 0xC3, (byte) 0x47, (byte) 0xF3, (byte) 0x44, (byte) 0x27,
			(byte) 0xF9, (byte) 0xFF, (byte) 0x5C, (byte) 0x8D, (byte) 0xE6,
			(byte) 0xD6, (byte) 0x5D, (byte) 0xAC, (byte) 0x95, (byte) 0xF2,
			(byte) 0xF1, (byte) 0x9D, (byte) 0xAC, (byte) 0x00, (byte) 0x53,
			(byte) 0xDF, (byte) 0x11, (byte) 0xA5, (byte) 0x07, (byte) 0xFB,
			(byte) 0x62, (byte) 0x5E, (byte) 0xEB, (byte) 0x8D, (byte) 0xA4,
			(byte) 0xC0, (byte) 0x29, (byte) 0x9E, (byte) 0x4A, (byte) 0x21,
			(byte) 0x12, (byte) 0xAB, (byte) 0x70, (byte) 0x47, (byte) 0x58,
			(byte) 0x8B, (byte) 0x8D, (byte) 0x6D, (byte) 0xA7, (byte) 0x59,
			(byte) 0x22, (byte) 0x14, (byte) 0xF2, (byte) 0xDB, (byte) 0xA1,
			(byte) 0x40, (byte) 0xC7, (byte) 0xD1, (byte) 0x22, (byte) 0x57,
			(byte) 0x9B, (byte) 0x5F, (byte) 0x38, (byte) 0x3D, (byte) 0x22,
			(byte) 0x53, (byte) 0xC8, (byte) 0xB9, (byte) 0xCB, (byte) 0x5B,
			(byte) 0xC3, (byte) 0x54, (byte) 0x3A, (byte) 0x55, (byte) 0x66,
			(byte) 0x0B, (byte) 0xDA, (byte) 0x80, (byte) 0x94, (byte) 0x6A,
			(byte) 0xFB, (byte) 0x05, (byte) 0x25, (byte) 0xE8, (byte) 0xE5,
			(byte) 0x58, (byte) 0x6B, (byte) 0x4E, (byte) 0x63, (byte) 0xE8,
			(byte) 0x92, (byte) 0x41, (byte) 0x49, (byte) 0x78, (byte) 0x36,
			(byte) 0xD8, (byte) 0xD3, (byte) 0xAB, (byte) 0x08, (byte) 0x8C,
			(byte) 0xD4, (byte) 0x4C, (byte) 0x21, (byte) 0x4D, (byte) 0x6A,
			(byte) 0xC8, (byte) 0x56, (byte) 0xE2, (byte) 0xA0, (byte) 0x07,
			(byte) 0xF4, (byte) 0x4F, (byte) 0x83, (byte) 0x74, (byte) 0x33,
			(byte) 0x37, (byte) 0x37, (byte) 0x1A, (byte) 0xDD, (byte) 0x8E,
			(byte) 0x03, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x01,
			(byte) 0x42, (byte) 0x08, (byte) 0x65, (byte) 0x73, (byte) 0x52,
			(byte) 0x44, (byte) 0x49, (byte) 0x60, (byte) 0x00, (byte) 0x06 };

	private final byte[] CHR = new byte[] {

	(byte) 0x83, (byte) 0x08, (byte) 0x65, (byte) 0x73, (byte) 0x53,
			(byte) 0x44, (byte) 0x49, (byte) 0x60, (byte) 0x00, (byte) 0x06 };

	private final byte[] KEY_SELECTOR = new byte[] {

	(byte) 0x83, (byte) 0x0C, (byte) 0x00, (byte) 0x00, (byte) 0x00,
			(byte) 0x00, (byte) 0x20, (byte) 0x00, (byte) 0x00, (byte) 0x00,
			(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x84,
			(byte) 0x02, (byte) 0x02, (byte) 0x1F };

	private final byte[] C_CV_IFD = new byte[] {

	(byte) 0x7f, (byte) 0x21, (byte) 0x81, (byte) 0xcd, (byte) 0x5f,
			(byte) 0x37, (byte) 0x81, (byte) 0x80, (byte) 0x82, (byte) 0x5b,
			(byte) 0x69, (byte) 0xc6, (byte) 0x45, (byte) 0x1e, (byte) 0x5f,
			(byte) 0x51, (byte) 0x70, (byte) 0x74, (byte) 0x38, (byte) 0x5f,
			(byte) 0x2f, (byte) 0x17, (byte) 0xd6, (byte) 0x4d, (byte) 0xfe,
			(byte) 0x2e, (byte) 0x68, (byte) 0x56, (byte) 0x75, (byte) 0x67,
			(byte) 0x09, (byte) 0x4b, (byte) 0x57, (byte) 0xf3, (byte) 0xc5,
			(byte) 0x78, (byte) 0xe8, (byte) 0x30, (byte) 0xe4, (byte) 0x25,
			(byte) 0x57, (byte) 0x2d, (byte) 0xe8, (byte) 0x28, (byte) 0xfa,
			(byte) 0xf4, (byte) 0xde, (byte) 0x1b, (byte) 0x01, (byte) 0xc3,
			(byte) 0x94, (byte) 0xe3, (byte) 0x45, (byte) 0xc2, (byte) 0xfb,
			(byte) 0x06, (byte) 0x29, (byte) 0xa3, (byte) 0x93, (byte) 0x49,
			(byte) 0x2f, (byte) 0x94, (byte) 0xf5, (byte) 0x70, (byte) 0xb0,
			(byte) 0x0b, (byte) 0x1d, (byte) 0x67, (byte) 0x77, (byte) 0x29,
			(byte) 0xf7, (byte) 0x55, (byte) 0xd1, (byte) 0x07, (byte) 0x02,
			(byte) 0x2b, (byte) 0xb0, (byte) 0xa1, (byte) 0x16, (byte) 0xe1,
			(byte) 0xd7, (byte) 0xd7, (byte) 0x65, (byte) 0x9d, (byte) 0xb5,
			(byte) 0xc4, (byte) 0xac, (byte) 0x0d, (byte) 0xde, (byte) 0xab,
			(byte) 0x07, (byte) 0xff, (byte) 0x04, (byte) 0x5f, (byte) 0x37,
			(byte) 0xb5, (byte) 0xda, (byte) 0xf1, (byte) 0x73, (byte) 0x2b,
			(byte) 0x54, (byte) 0xea, (byte) 0xb2, (byte) 0x38, (byte) 0xa2,
			(byte) 0xce, (byte) 0x17, (byte) 0xc9, (byte) 0x79, (byte) 0x41,
			(byte) 0x87, (byte) 0x75, (byte) 0x9c, (byte) 0xea, (byte) 0x9f,
			(byte) 0x92, (byte) 0xa1, (byte) 0x78, (byte) 0x05, (byte) 0xa2,
			(byte) 0x7c, (byte) 0x10, (byte) 0x15, (byte) 0xec, (byte) 0x56,
			(byte) 0xcc, (byte) 0x7e, (byte) 0x47, (byte) 0x1a, (byte) 0x48,
			(byte) 0x8e, (byte) 0x6f, (byte) 0x1b, (byte) 0x91, (byte) 0xf7,
			(byte) 0xaa, (byte) 0x5f, (byte) 0x38, (byte) 0x3c, (byte) 0xad,
			(byte) 0xfc, (byte) 0x12, (byte) 0xe8, (byte) 0x56, (byte) 0xb2,
			(byte) 0x02, (byte) 0x34, (byte) 0x6a, (byte) 0xf8, (byte) 0x22,
			(byte) 0x6b, (byte) 0x1a, (byte) 0x88, (byte) 0x21, (byte) 0x37,
			(byte) 0xdc, (byte) 0x3c, (byte) 0x5a, (byte) 0x57, (byte) 0xf0,
			(byte) 0xd2, (byte) 0x81, (byte) 0x5c, (byte) 0x1f, (byte) 0xcd,
			(byte) 0x4b, (byte) 0xb4, (byte) 0x6f, (byte) 0xa9, (byte) 0x15,
			(byte) 0x7f, (byte) 0xdf, (byte) 0xfd, (byte) 0x79, (byte) 0xec,
			(byte) 0x3a, (byte) 0x10, (byte) 0xa8, (byte) 0x24, (byte) 0xcc,
			(byte) 0xc1, (byte) 0xeb, (byte) 0x3c, (byte) 0xe0, (byte) 0xb6,
			(byte) 0xb4, (byte) 0x39, (byte) 0x6a, (byte) 0xe2, (byte) 0x36,
			(byte) 0x59, (byte) 0x00, (byte) 0x16, (byte) 0xba, (byte) 0x69,
			(byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x01, (byte) 0x42,
			(byte) 0x08, (byte) 0x65, (byte) 0x73, (byte) 0x53, (byte) 0x44,
			(byte) 0x49, (byte) 0x60, (byte) 0x00, (byte) 0x06

	};

	private final byte[] APDU_GET_CHIP_INFO = new byte[] { (byte) 0x90,
			(byte) 0xB8, (byte) 0x00, (byte) 0x00, (byte) 0x07 };

	private final byte[] APDU_SELECT_EF_DF_HEADER = new byte[] { (byte) 0x00,
			(byte) 0xA4, (byte) 0x00, (byte) 0x00 };

	private final byte[] APDU_READ_BINARY = new byte[] { (byte) 0x00,
			(byte) 0xB0, (byte) 0x00, (byte) 0x00, (byte) 0xFF };

	private final byte[] SECURE_CHANNEL_COMP_CERT_ID = new byte[] {
			(byte) 0x60, (byte) 0x1F };
	private final byte[] SECURE_CHANNEL_INTERMEDIAT_CERT_ID = new byte[] {
			(byte) 0x60, (byte) 0x20 };

	private final byte[] TERMINAL_CHALLENGE_TAIL = new byte[] {

	(byte) 0x20, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
			(byte) 0x00, (byte) 0x00, (byte) 0x01 };

	private final byte[] KENC_COMPUTATION_TAIL = new byte[] {

	(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01 };

	private final byte[] KMAC_COMPUTATION_TAIL = new byte[] {

	(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x02 };

	private final int BLOCK_LENGTH = 8;

	private final byte[] IV = new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x00, 0x00 };

	private final byte[] KEY_ID = new byte[] { (byte) 0x01, (byte) 0x02 };

	private final byte[] HASH_PADDING = new byte[] {

	(byte) 0x30, (byte) 0x21, (byte) 0x30, (byte) 0x09, (byte) 0x06,
			(byte) 0x05, (byte) 0x2B, (byte) 0x0E, (byte) 0x03, (byte) 0x02,
			(byte) 0x1A, (byte) 0x05, (byte) 0x00, (byte) 0x04, (byte) 0x14 };

	private byte[] snIcc;
	private byte[] componentCert;
	private byte[] intermediateCert;

	private byte[] rndIfd;
	private byte[] rndIcc;
	private int prndLength;

	private byte[] kicc;
	private byte[] kifd;

	private byte[] kEnc;
	private byte[] kMac;
	private byte[] ssc;

	private boolean established;

	public DNIeCardSecureChannel() {

		this.established = false;
	}

	public void establish(CardChannel channel) throws CardException {

		// get chip info
		this.snIcc = executeGetChipInfo(channel);

		// get certificates to establish secure channel
		this.intermediateCert = executeReadSecureChannelCertificate(channel,
				SECURE_CHANNEL_INTERMEDIAT_CERT_ID);
		this.componentCert = executeReadSecureChannelCertificate(channel,
				SECURE_CHANNEL_COMP_CERT_ID);

		// verify card's secure channel certificates
		verifyCertificates();

		// load terminal secure channel certificates and select appropriate keys
		loadTerminalCertsAndSelectKeys(channel);

		// perform internal authentication
		performInternalAuthentication(channel);

		// perform external authentication
		performExternalAuthentication(channel);

		// derive channel keys
		calculateChannelKeys();

		// secure channel successfully established
		this.established = true;
		log.debug("Secure channel successfully established.");

	}

	public byte[] executeSecureSelect(CardChannel channel, byte[] apdu)
			throws CardException {

		log.debug("Executing secure select command..");

		byte[] securedApdu = getSecuredAPDU(apdu);

		CommandAPDU command = new CommandAPDU(securedApdu);
		ResponseAPDU resp = channel.transmit(command);

		if (resp.getSW() != 0x9000) {

			throw new CardException("Unexpected response from card: "
					+ Integer.toHexString(resp.getSW()));
		}

		byte[] data = resp.getData();

		byte[] response = verifyAndDecryptSecuredResponseAPDU(data);

		if (response.length >= 2
				&& response[response.length - 2] == (byte) 0x90
				&& response[response.length - 1] == (byte) 0x00) {

			log.debug("OK");
		} else {

			log.error("FAILED");
			throw new CardException("Unable to select file on card.");
		}

		byte[] fci = new byte[response.length - 2];
		System.arraycopy(response, 0, fci, 0, response.length - 2);

		return fci;

	}

	public void executeSecureManageSecurityEnvironment(CardChannel channel)
			throws CardException {

		log.debug("Manage Security Environment..");

		byte[] apdu = new byte[7 + KEY_ID.length];
		apdu[0] = (byte) 0x00;
		apdu[1] = (byte) 0x22;
		apdu[2] = (byte) 0x41;
		apdu[3] = (byte) 0xB6;
		apdu[4] = (byte) (KEY_ID.length + 2);
		apdu[5] = (byte) 0x84; // Tag
		apdu[6] = (byte) KEY_ID.length; // Length
		System.arraycopy(KEY_ID, 0, apdu, 7, KEY_ID.length);

		byte[] securedAPDU = getSecuredAPDU(apdu);

		CommandAPDU command = new CommandAPDU(securedAPDU);
		ResponseAPDU resp = channel.transmit(command);

		if (resp.getSW() == 0x9000) {

			byte[] response = resp.getData();

			byte[] decryptedResponse = verifyAndDecryptSecuredResponseAPDU(response);

			if (decryptedResponse.length == 2
					&& decryptedResponse[0] == (byte) 0x90
					&& decryptedResponse[1] == (byte) 0x00) {

				log.debug("OK");
			} else {

				log.debug("FAILED");
				throw new CardException(
						"Execution of command Manage Security Environment failed: "
								+ formatByteArray(decryptedResponse));
			}

		}
	}

	public byte[] executeSecureCreateSignature(CardChannel channel, byte[] data)
			throws CardException {

		log.debug("Compute electronic signature on card..");

		byte[] apdu = new byte[5 + HASH_PADDING.length + data.length];
		apdu[0] = (byte) 0x00;
		apdu[1] = (byte) 0x2A;
		apdu[2] = (byte) 0x9E;
		apdu[3] = (byte) 0x9A;
		apdu[4] = (byte) (HASH_PADDING.length + data.length);

		System.arraycopy(HASH_PADDING, 0, apdu, 5, HASH_PADDING.length);
		System.arraycopy(data, 0, apdu, 5 + HASH_PADDING.length, data.length);

		byte[] securedAPDU = getSecuredAPDU(apdu);

		CommandAPDU command = new CommandAPDU(securedAPDU);
		ResponseAPDU resp = channel.transmit(command);

		if (resp.getSW() != 0x9000) {

			throw new CardException("Unexpected response from card: "
					+ Integer.toHexString(resp.getSW()));
		}

		byte[] signatureValue = resp.getData();
		byte[] decryptedSignatureValueWithSW = verifyAndDecryptSecuredResponseAPDU(signatureValue);

		int len = decryptedSignatureValueWithSW.length;
		if (decryptedSignatureValueWithSW[len - 2] == (byte) 0x90
				&& decryptedSignatureValueWithSW[len - 1] == (byte) 0x00) {

			log.debug("OK");

			byte[] sigVal = new byte[decryptedSignatureValueWithSW.length - 2];
			System.arraycopy(decryptedSignatureValueWithSW, 0, sigVal, 0,
					decryptedSignatureValueWithSW.length - 2);

			log.debug("Computed signature value: " + formatByteArray(sigVal));
			return sigVal;

		} else {

			log.debug("FAILED");
			throw new CardException(
					"Error creating signature on card: "
							+ Integer
									.toHexString(decryptedSignatureValueWithSW[len - 2])
							+ " "
							+ Integer
									.toHexString(decryptedSignatureValueWithSW[len - 1]));
		}

	}

	private byte[] getSecuredAPDU(byte[] apdu) throws CardException {

		// TODO: Handle APDU format: CLA INS P1 P2 Lc Data Le (not required by this implementation)

		if (apdu == null || apdu.length < 4) {

			throw new CardException("Invalid APDU to secure.");
		}

		if (apdu.length < 6) {

			// TODO: Handle APDU format: CLA INS P1 P2 (not required by this implementation)

			if (apdu.length == 5) {

				// handle case CLA INS P1 P2 LE

				byte encCLA = (byte) (apdu[0] | (byte) 0x0C);
				byte[] encHeader = new byte[] { encCLA, apdu[1], apdu[2],
						apdu[3] };
				byte[] paddedHeader = applyPadding(BLOCK_LENGTH, encHeader);

				byte[] leField = new byte[3];
				leField[0] = (byte) 0x97;
				leField[1] = (byte) 0x01;
				leField[2] = apdu[4];

				byte[] macData = new byte[paddedHeader.length + leField.length];
				System.arraycopy(paddedHeader, 0, macData, 0,
						paddedHeader.length);
				System.arraycopy(leField, 0, macData, paddedHeader.length,
						leField.length);

				byte[] paddedMacData = applyPadding(BLOCK_LENGTH, macData);

				incrementSSC();

				byte[] mac = calculateAPDUMAC(paddedMacData, kMac, this.ssc);

				byte[] encapsulatedMac = new byte[mac.length + 2];
				encapsulatedMac[0] = (byte) 0x8E;
				encapsulatedMac[1] = (byte) mac.length;
				System.arraycopy(mac, 0, encapsulatedMac, 2, mac.length);

				byte[] completeMessage = new byte[5 + leField.length
						+ encapsulatedMac.length];
				completeMessage[0] = encCLA;
				completeMessage[1] = apdu[1];
				completeMessage[2] = apdu[2];
				completeMessage[3] = apdu[3];
				completeMessage[4] = (byte) (encapsulatedMac.length + leField.length);
				System
						.arraycopy(leField, 0, completeMessage, 5,
								leField.length);
				System.arraycopy(encapsulatedMac, 0, completeMessage,
						5 + leField.length, encapsulatedMac.length);

				return completeMessage;
			}
		}

		// case data field available (assuming that Le field is missing)

		byte cla = apdu[0];
		byte ins = apdu[1];
		byte p1 = apdu[2];
		byte p2 = apdu[3];
		byte lc = apdu[4];

		byte[] data = new byte[lc];
		System.arraycopy(apdu, 5, data, 0, lc);

		byte[] paddedData = applyPadding(BLOCK_LENGTH, data);

		byte[] encrypted = null;

		try {

			encrypted = perform3DESCipherOperation(paddedData, kEnc,
					Cipher.ENCRYPT_MODE);

		} catch (Exception e) {

			throw new CardException("Error encrypting APDU.", e);
		}

		byte[] encapsulated = new byte[encrypted.length + 3];
		encapsulated[0] = (byte) 0x87;
		encapsulated[1] = (byte) (encrypted.length + 1);
		encapsulated[2] = (byte) 0x01;
		System.arraycopy(encrypted, 0, encapsulated, 3, encrypted.length);

		// calculate MAC

		// prepare CLA byte

		byte encCLA = (byte) (cla | (byte) 0x0C);
		byte[] encHeader = new byte[] { encCLA, ins, p1, p2 };
		byte[] paddedHeader = applyPadding(BLOCK_LENGTH, encHeader);

		byte[] headerAndData = new byte[paddedHeader.length
				+ encapsulated.length];
		System
				.arraycopy(paddedHeader, 0, headerAndData, 0,
						paddedHeader.length);
		System.arraycopy(encapsulated, 0, headerAndData, paddedHeader.length,
				encapsulated.length);

		byte[] paddedHeaderAndData = applyPadding(BLOCK_LENGTH, headerAndData);

		incrementSSC();

		byte[] mac = calculateAPDUMAC(paddedHeaderAndData, kMac, this.ssc);

		byte[] encapsulatedMac = new byte[mac.length + 2];
		encapsulatedMac[0] = (byte) 0x8E;
		encapsulatedMac[1] = (byte) mac.length;
		System.arraycopy(mac, 0, encapsulatedMac, 2, mac.length);

		byte[] completeMessage = new byte[5 + encapsulated.length
				+ encapsulatedMac.length];
		completeMessage[0] = encCLA;
		completeMessage[1] = ins;
		completeMessage[2] = p1;
		completeMessage[3] = p2;
		completeMessage[4] = (byte) (encapsulated.length + encapsulatedMac.length);
		System.arraycopy(encapsulated, 0, completeMessage, 5,
				encapsulated.length);
		System.arraycopy(encapsulatedMac, 0, completeMessage,
				5 + encapsulated.length, encapsulatedMac.length);

		return completeMessage;
	}

	private byte[] verifyAndDecryptSecuredResponseAPDU(byte[] securedAPDU)
			throws CardException {

		byte[] data = new byte[securedAPDU.length - 10];
		byte[] commandResponse = new byte[4];
		byte[] obtainedMac = new byte[4];

		System.arraycopy(securedAPDU, 0, data, 0, data.length);
		System.arraycopy(securedAPDU, data.length, commandResponse, 0,
				commandResponse.length);
		System.arraycopy(securedAPDU, data.length + commandResponse.length + 2,
				obtainedMac, 0, obtainedMac.length);

		byte[] macData = new byte[data.length + commandResponse.length];
		System.arraycopy(data, 0, macData, 0, data.length);
		System.arraycopy(commandResponse, 0, macData, data.length,
				commandResponse.length);

		byte[] paddedMacData = applyPadding(BLOCK_LENGTH, macData);

		incrementSSC();

		byte[] mac = calculateAPDUMAC(paddedMacData, this.kMac, this.ssc);

		if (!Arrays.equals(mac, obtainedMac)) {

			throw new CardException("Unable to verify MAC of Response APDU.");
		}

		if (data.length > 0) {

			byte[] data2decrypt = new byte[data.length
					- getCutOffLength(data, BLOCK_LENGTH)];
			System.arraycopy(data, getCutOffLength(data, BLOCK_LENGTH),
					data2decrypt, 0, data2decrypt.length);

			byte[] plainData = null;

			try {
				plainData = perform3DESCipherOperation(data2decrypt, this.kEnc,
						Cipher.DECRYPT_MODE);
			} catch (Exception e) {
				throw new CardException("Unable to decrypt data.", e);
			}

			byte[] unpaddedData = removePadding(plainData);

			byte[] result = new byte[unpaddedData.length + 2];
			System.arraycopy(unpaddedData, 0, result, 0, unpaddedData.length);
			result[result.length - 2] = commandResponse[2];
			result[result.length - 1] = commandResponse[3];

			return result;
		} else {

			// no data in response
			byte[] result = new byte[2];
			result[result.length - 2] = commandResponse[2];
			result[result.length - 1] = commandResponse[3];
			return result;
		}
	}

	private byte[] perform3DESCipherOperation(byte[] data, byte[] keyData,
			int mode) throws NoSuchAlgorithmException, NoSuchProviderException,
			NoSuchPaddingException, InvalidKeyException,
			InvalidAlgorithmParameterException, ShortBufferException,
			IllegalBlockSizeException, BadPaddingException {

		byte[] full3DESKey = new byte[24];
		System.arraycopy(keyData, 0, full3DESKey, 0, 16);
		System.arraycopy(keyData, 0, full3DESKey, 16, 8);

		SecretKeySpec key = new SecretKeySpec(full3DESKey, "DESede");
		IvParameterSpec ivSpec = new IvParameterSpec(IV);

		Cipher cipher = Cipher.getInstance("DESede/CBC/NoPadding");

		cipher.init(mode, key, ivSpec);
		byte[] cipherText = new byte[cipher.getOutputSize(data.length)];
		int ctLength = cipher.update(data, 0, data.length, cipherText, 0);
		ctLength += cipher.doFinal(cipherText, ctLength);
		return cipherText;

	}

	private byte[] calculateAPDUMAC(byte[] data, byte[] key, byte[] ssc)
			throws CardException {

		SecretKeySpec desSingleKey = new SecretKeySpec(key, 0, BLOCK_LENGTH,
				"DES");
		Cipher singleDesCipher;
		try {
			singleDesCipher = Cipher.getInstance("DES/CBC/NoPadding");
		} catch (Exception e) {

			throw new CardException("Error creating DES cipher instance.", e);
		}

		// Calculate the first n - 1 block.
		IvParameterSpec ivSpec;
		ivSpec = new IvParameterSpec(IV);
		int dataLen = data.length;

		try {
			singleDesCipher.init(Cipher.ENCRYPT_MODE, desSingleKey, ivSpec);
		} catch (Exception e) {
			throw new CardException("Error initializing DES cipher.", e);
		}
		byte[] result;
		try {
			result = singleDesCipher.doFinal(ssc);
		} catch (Exception e) {
			throw new CardException("Error applying DES cipher.", e);
		}

		byte[] dataBlock = new byte[BLOCK_LENGTH];

		for (int i = 0; i < dataLen - BLOCK_LENGTH; i = i + BLOCK_LENGTH) {

			System.arraycopy(data, i, dataBlock, 0, BLOCK_LENGTH);
			byte[] input = xorByteArrays(result, dataBlock);

			try {
				result = singleDesCipher.doFinal(input);
			} catch (Exception e) {
				throw new CardException("Error applying DES cipher.", e);
			}
		}

		// calculate the last block with 3DES
		byte[] fullKey = new byte[24];
		System.arraycopy(key, 0, fullKey, 0, 16);
		System.arraycopy(key, 0, fullKey, 16, 8);

		SecretKeySpec desKey = new SecretKeySpec(fullKey, "DESede");
		Cipher cipher;
		try {
			cipher = Cipher.getInstance("DESede/CBC/NoPadding");
		} catch (Exception e) {
			throw new CardException("Error getting 3DES cipher instance.", e);
		}

		ivSpec = new IvParameterSpec(IV);
		try {
			cipher.init(Cipher.ENCRYPT_MODE, desKey, ivSpec);
		} catch (Exception e) {
			throw new CardException("Error initializing 3DES cipher.", e);
		}

		System.arraycopy(data, data.length - BLOCK_LENGTH, dataBlock, 0,
				BLOCK_LENGTH);
		byte[] input = xorByteArrays(result, dataBlock);

		byte[] mac = new byte[4];

		try {

			result = cipher.doFinal(input);

		} catch (Exception e) {
			throw new CardException("Error applying 3DES cipher.", e);
		}

		System.arraycopy(result, 0, mac, 0, 4);
		return mac;
	}

	private byte[] executeSendTerminalChallenge(CardChannel channel,
			byte[] challenge) throws CardException {

		// send challenge to card
		CommandAPDU command = new CommandAPDU((byte) 0x00, (byte) 0x88,
				(byte) 0x00, (byte) 0x00, challenge);
		ResponseAPDU resp = channel.transmit(command);

		byte[] data = null;

		if (resp.getSW() == 0x9000) {

			data = resp.getData();

		} else {

			throw new CardException("Invalid response to terminal challenge: "
					+ Integer.toHexString(resp.getSW()));
		}

		return data;
	}

	private byte[] readFromCard(CardChannel channel, byte offsetHi,
			byte offsetLo, byte numBytes) throws CardException {

		byte[] apdu = new byte[] {

		(byte) 0x00, (byte) 0xB0, offsetHi, offsetLo, numBytes };

		byte[] securedAPDU = getSecuredAPDU(apdu);

		CommandAPDU command = new CommandAPDU(securedAPDU);
		ResponseAPDU resp = channel.transmit(command);

		if (resp.getSW() != 0x9000) {

			throw new CardException("Unexpected reponse from card: "
					+ Integer.toHexString(resp.getSW()));
		}

		byte[] data = resp.getData();

		byte[] decryptedResponse = verifyAndDecryptSecuredResponseAPDU(data);

		log.debug("Read plain data: " + formatByteArray(decryptedResponse));

		return decryptedResponse;

	}

	public int executeSecurePINVerify(CardChannel channel, byte[] apdu)
			throws CardException {

		byte[] securedAPDU = getSecuredAPDU(apdu);

		log.debug("Verifiying PIN..");

		CommandAPDU command = new CommandAPDU(securedAPDU);

		ResponseAPDU resp = channel.transmit(command);

		if (resp.getSW() == 0x9000) {

			byte[] securedResponseData = resp.getData();

			byte[] plainData = verifyAndDecryptSecuredResponseAPDU(securedResponseData);

			if (plainData.length == 2) {

				return getSWAsInt(plainData);
			} else {

				throw new CardException(
						"Unexpected response to verify PIN APDU: "
								+ formatByteArray(plainData));
			}
		} else {

			throw new CardException("Unexpected response to verify PIN APDU: "
					+ Integer.toHexString(resp.getSW()));
		}
	}

	public byte[] executeSecureReadBinary(CardChannel channel, byte lengthHi,
			byte lengthLo) throws CardException {

		log.debug("Executing secure read binary..");

		ByteArrayOutputStream bof = new ByteArrayOutputStream();

		int bytes2read = (lengthHi * 256) + lengthLo;
		int bytesRead = 0;

		boolean done = false;
		boolean forceExit = false;

		int offset = 0;
		int len = 0;

		while (!done) {

			if (bytes2read - bytesRead > 0xef) {
				len = 0xef;
			} else {
				len = bytes2read - bytesRead;
			}

			byte[] offsetBytes = intToHex(offset);
			byte[] decryptedResponse = readFromCard(channel, offsetBytes[0],
					offsetBytes[1], (byte) len);

			if (decryptedResponse.length == 2
					&& decryptedResponse[0] == (byte) 0x6C) {

				// handle case: card returns 6CXX (wrong number of bytes
				// requested)
				// This happens sometimes with the DNIe in the final iteration

				decryptedResponse = readFromCard(channel, offsetBytes[0],
						offsetBytes[1], decryptedResponse[1]);

				forceExit = true;
			}

			byte[] decryptedData = new byte[decryptedResponse.length - 2];
			System.arraycopy(decryptedResponse, 0, decryptedData, 0,
					decryptedResponse.length - 2);

			try {
				bof.write(decryptedData);
			} catch (IOException e) {
				throw new CardException("Error reading data from card", e);
			}

			bytesRead = bytesRead + decryptedData.length;
			offset = bytesRead;

			if (bytesRead == bytes2read) {

				done = true;
			}

			if (forceExit) {

				break;
			}
		}

		return bof.toByteArray();
	}

	private byte[] executeRequestCardChallenge(CardChannel channel)
			throws CardException {

		CommandAPDU command = new CommandAPDU((byte) 0x00, (byte) 0x84,
				(byte) 0x00, (byte) 0x00, (byte) BLOCK_LENGTH);
		ResponseAPDU resp = channel.transmit(command);

		if (resp.getSW() != 0x9000) {

			throw new CardException(
					"Invalid response from card upon challenge request: "
							+ Integer.toHexString(resp.getSW()));
		}

		return resp.getData();

	}

	private void performExternalAuthentication(CardChannel channel)
			throws CardException {

		log.debug("Starting external authentication..");

		byte[] cardChallenge = executeRequestCardChallenge(channel);

		this.rndIcc = cardChallenge;

		byte[] prnd2 = getRandomBytes(this.prndLength);
		byte[] kIfd = getRandomBytes(32);

		// compute hash
		byte[] hashData = new byte[prnd2.length + kIfd.length
				+ cardChallenge.length + BLOCK_LENGTH];

		System.arraycopy(prnd2, 0, hashData, 0, prnd2.length);
		System.arraycopy(kIfd, 0, hashData, prnd2.length, kIfd.length);
		System.arraycopy(cardChallenge, 0, hashData,
				prnd2.length + kIfd.length, cardChallenge.length);

		int snPadding = BLOCK_LENGTH - snIcc.length;

		for (int i = 0; i < snPadding; i++) {

			hashData[prnd2.length + kIfd.length + cardChallenge.length + i] = (byte) 0x00;
		}

		System.arraycopy(snIcc, 0, hashData, prnd2.length + kIfd.length
				+ cardChallenge.length + snPadding, snIcc.length);

		byte[] digest = computeSHA1Hash(hashData);

		// prepare data to be encrypted
		byte[] plain = new byte[2 + prnd2.length + kIfd.length + digest.length];

		plain[0] = (byte) 0x6A;

		System.arraycopy(prnd2, 0, plain, 1, prnd2.length);
		System.arraycopy(kIfd, 0, plain, 1 + prnd2.length, kIfd.length);
		System.arraycopy(digest, 0, plain, 1 + prnd2.length + kIfd.length,
				digest.length);

		plain[plain.length - 1] = (byte) 0xBC;

		// encrypt plain data
		RSAPrivateKey terminalPrivateKey = createRSAPrivateKey(TERMINAL_MODULO,
				TERMINAL_PRIVEXP);

		byte[] encResult = null;
		try {
			encResult = rsaEncrypt(terminalPrivateKey, plain);
		} catch (Exception e) {

			throw new CardException("Error encrypting authentication data.", e);
		}

		// apply MIN function
		BigInteger sig = new BigInteger(encResult);
		BigInteger mod = new BigInteger(TERMINAL_MODULO, 16);

		BigInteger diff = mod.subtract(sig);
		BigInteger sigMin = diff.min(sig);

		// encrypt with card public key
		PublicKey cardPubKey = null;

		X509Certificate cert = createCertificate(componentCert);
		cardPubKey = cert.getPublicKey();

		byte[] authData = null;
		try {
			authData = rsaEncrypt(cardPubKey, sigMin.toByteArray());
		} catch (Exception e) {

			throw new CardException("Error encrypting authentication data.");
		}

		// send auth data to card
		// BE CAREFUL WITH THAT! EXT-AUTH METHOD MAY GET BLOCKED!
		if (executeExternalAuthenticate(channel, authData)) {

			this.kifd = kIfd;
			log.debug("External authentication succeeded.");
		} else {
			log.error("External authentication failed.");
			throw new CardException("External Authentication failed.");
		}

	}

	private boolean executeExternalAuthenticate(CardChannel channel,
			byte[] authData) throws CardException {

		CommandAPDU command = new CommandAPDU((byte) 0x00, (byte) 0x82,
				(byte) 0x00, (byte) 0x00, authData);
		ResponseAPDU resp = channel.transmit(command);

		return resp.getSW() == 0x9000;
	}

	private void performInternalAuthentication(CardChannel channel)
			throws CardException {

		byte[] randomBytes = getRandomBytes(BLOCK_LENGTH);
		byte[] challengeData = new byte[randomBytes.length
				+ TERMINAL_CHALLENGE_TAIL.length];

		this.rndIfd = randomBytes;

		System.arraycopy(randomBytes, 0, challengeData, 0, randomBytes.length);
		System.arraycopy(TERMINAL_CHALLENGE_TAIL, 0, challengeData,
				randomBytes.length, TERMINAL_CHALLENGE_TAIL.length);

		byte[] data = executeSendTerminalChallenge(channel, challengeData);

		// verify response
		boolean ok = verifyCardResponse(data);

		log.debug("Internal Authentiction succeeded: " + ok);

		if (!ok) {

			log.debug("Internal Authentiction failed - cancel.");
			throw new CardException("Internal authentication failed");
		}

	}

	private void verifyCertificates() throws CardException {

		// This method verifies the card's component and intermediate
		// certificates cryptographically.
		// TODO: Revocation checking (cannot be done now since CRL URLs in certificates seem to be incorrect)

		RSAPublicKey rootPubKey = createRSAPublicKey(ROOT_CA_MODULO,
				ROOT_CA_PUBEXP);

		X509Certificate intermediate = createCertificate(intermediateCert);
		X509Certificate component = createCertificate(componentCert);

		try {
			component.verify(intermediate.getPublicKey());
			intermediate.verify(rootPubKey);
		} catch (Exception e) {

			log.error("Certificate verification failed.");
			e.printStackTrace();
		}
	}

	private boolean verifyCardResponse(byte[] resp) throws CardException {

		log.debug("Verifying card response..");

		byte[] challenge = this.rndIfd;
		byte[] response = resp;

		// decrypt response with terminal private key
		byte[] plain = null;
		RSAPrivateKey terminalPrivateKey = createRSAPrivateKey(TERMINAL_MODULO,
				TERMINAL_PRIVEXP);
		try {
			plain = rsaDecrypt(terminalPrivateKey, response);
		} catch (Exception e) {
			throw new CardException("Error decrypting card response.", e);
		}

		PublicKey pubKey = null;

		X509Certificate cert = createCertificate(componentCert);
		pubKey = cert.getPublicKey();

		byte[] sig = null;

		try {
			sig = rsaDecrypt(pubKey, plain);

		} catch (Exception e) {

			throw new CardException(
					"Error decrypting card response with card's public key", e);
		}

		if (sig == null) {

			throw new CardException("Invalid decryption result - null.");
		} else {

			if (sig[0] == (byte) 0x6A && sig[sig.length - 1] == (byte) 0xBC) {

				// Obtained response from card was obviously SIG - nothing else
				// to do here

			} else {

				// Obtained response from card was obviously N.ICC-SIG -
				// compute N.ICC-SIG and decrypt result again

				RSAPublicKey rsaPubKey = (RSAPublicKey) pubKey;
				BigInteger mod = rsaPubKey.getModulus();
				BigInteger sigVal = new BigInteger(plain);

				BigInteger substractionResult = mod.subtract(sigVal);
				byte[] encrypted = substractionResult.toByteArray();

				// necessary as substraction result seems to contain one leading
				// zero byte
				byte[] trimmed = new byte[128];
				System.arraycopy(encrypted, encrypted.length - 128, trimmed, 0,
						128);

				try {
					sig = rsaDecrypt(pubKey, trimmed);

				} catch (Exception e) {

					throw new CardException("Error decrypting card response.",
							e);
				}
			}
		}

		// extract data from decrypted response
		byte[] hash = new byte[20];
		byte[] kIcc = new byte[32];
		byte[] prnd1 = new byte[sig.length - 2 - 20 - 32];

		this.prndLength = prnd1.length;

		System.arraycopy(sig, 1, prnd1, 0, prnd1.length); // 1 byte offset due
		// to 6A padding
		System.arraycopy(sig, prnd1.length + 1, kIcc, 0, kIcc.length);
		System.arraycopy(sig, prnd1.length + kIcc.length + 1, hash, 0,
				hash.length);

		// verify hash
		byte[] hashData = new byte[prnd1.length + kIcc.length
				+ challenge.length + TERMINAL_CHALLENGE_TAIL.length];

		System.arraycopy(prnd1, 0, hashData, 0, prnd1.length);
		System.arraycopy(kIcc, 0, hashData, prnd1.length, kIcc.length);
		System.arraycopy(challenge, 0, hashData, prnd1.length + kIcc.length,
				challenge.length);
		System.arraycopy(TERMINAL_CHALLENGE_TAIL, 0, hashData, prnd1.length
				+ kIcc.length + challenge.length,
				TERMINAL_CHALLENGE_TAIL.length);

		byte[] digest = computeSHA1Hash(hashData);

		boolean internalAuthResult = Arrays.equals(hash, digest);

		if (internalAuthResult) {

			// if verification succeeded, remember kicc for subsequent channel
			// key derivation
			this.kicc = kIcc;
		}

		return internalAuthResult;

	}

	private byte[] computeSHA1Hash(byte[] data) throws CardException {

		try {
			MessageDigest sha = MessageDigest.getInstance("SHA");

			sha.update(data);
			return sha.digest();

		} catch (NoSuchAlgorithmException e) {
			throw new CardException("Error computing SHA1 hash.", e);
		}

	}

	private byte[] executeGetChipInfo(CardChannel channel) throws CardException {

		// get chip info - read out card serial number
		log.debug("Getting chip info..");
		CommandAPDU command = new CommandAPDU(APDU_GET_CHIP_INFO);
		ResponseAPDU resp = channel.transmit(command);

		if (resp.getSW() == 0x9000) {
			log.debug("OK.");
		} else {
			log.debug("FAILED: " + Integer.toHexString(resp.getSW()));
		}

		log.debug("Read chip info: " + formatByteArray(resp.getData()));

		return resp.getData();
	}

	private byte[] executeSelect(CardChannel channel, byte[] id)
			throws CardException {

		log.debug("Selecting DF or EF..");

		byte[] apdu = new byte[APDU_SELECT_EF_DF_HEADER.length + 1 + id.length];
		System.arraycopy(APDU_SELECT_EF_DF_HEADER, 0, apdu, 0,
				APDU_SELECT_EF_DF_HEADER.length);
		apdu[APDU_SELECT_EF_DF_HEADER.length] = (byte) id.length;
		System.arraycopy(id, 0, apdu, APDU_SELECT_EF_DF_HEADER.length + 1,
				id.length);

		CommandAPDU command = new CommandAPDU(apdu);
		ResponseAPDU resp = channel.transmit(command);

		if (resp.getSW() != 0x9000) {

			throw new CardException("Unexpected response to Select Command: "
					+ Integer.toHexString(resp.getSW()));
		}

		return resp.getData();

	}

	private byte[] executeReadSecureChannelCertificate(CardChannel channel,
			byte[] certId) throws CardException {

		log.debug("Reading certificate..");

		byte[] fci = executeSelect(channel, certId);

		byte certLenHigh;
		byte certLenLow;

		if (fci != null && fci.length >= 7) {

			certLenHigh = fci[7];
			certLenLow = fci[8];
		} else {

			throw new CardException("Invalid FCI obtained from card.");
		}

		ByteArrayOutputStream bof = new ByteArrayOutputStream();

		int bytes2read = (certLenHigh * 256) + certLenLow;
		int bytesRead = 0;

		boolean done = false;
		int offset = 0;
		int len = 0;

		while (!done) {

			if (bytes2read - bytesRead > 255) {
				len = 255;
			} else {
				len = bytes2read - bytesRead;
			}

			byte[] offsetBytes = intToHex(offset);

			byte[] apdu = new byte[5];
			System.arraycopy(APDU_READ_BINARY, 0, apdu, 0,
					APDU_READ_BINARY.length);
			apdu[2] = offsetBytes[0];
			apdu[3] = offsetBytes[1];
			apdu[4] = (byte) len;

			CommandAPDU command = new CommandAPDU(apdu);
			ResponseAPDU resp = channel.transmit(command);

			byte[] certData = resp.getData();

			try {
				bof.write(certData);
			} catch (IOException e) {
				throw new CardException("Error reading certificate from card",
						e);
			}

			bytesRead = bytesRead + certData.length;
			offset = bytesRead;

			if (bytesRead == bytes2read) {

				done = true;
			}
		}

		log.debug("OK.");

		return bof.toByteArray();
	}

	private void executeManageSecurityEnvironment(CardChannel channel, byte p1,
			byte p2, byte[] data) throws CardException {

		// MSE
		CommandAPDU command = new CommandAPDU((byte) 0x00, (byte) 0x22, p1, p2,
				data);
		ResponseAPDU resp = channel.transmit(command);

		if (resp.getSW() != 0x9000) {

			throw new CardException(
					"Unexpected response from card during preparation of secure channel credentials: "
							+ Integer.toHexString(resp.getSW()));
		}
	}

	private void executePerformSecurityOperation(CardChannel channel,
			byte[] data) throws CardException {

		// PSO - load intermediate certificate
		CommandAPDU command = new CommandAPDU((byte) 0x00, (byte) 0x2A,
				(byte) 0x00, (byte) 0xAE, data);
		ResponseAPDU resp = channel.transmit(command);

		if (resp.getSW() != 0x9000) {

			throw new CardException(
					"Unexpected response from card during preparation of secure channel credentials: "
							+ Integer.toHexString(resp.getSW()));
		}

	}

	private void loadTerminalCertsAndSelectKeys(CardChannel channel)
			throws CardException {

		log
				.debug("Loading terminal certificates and selecting appropriate keys to establish secure channel..");

		// MSE
		executeManageSecurityEnvironment(channel, (byte) 0x81, (byte) 0xB6,
				APDU_DATA_MSE_LOAD_TERMINAL_CERTS);

		// PSO - load intermediate certificate
		executePerformSecurityOperation(channel, C_CV_CA);

		// MSE
		executeManageSecurityEnvironment(channel, (byte) 0x81, (byte) 0xB6, CHR);

		// PSO - load terminal certificate
		executePerformSecurityOperation(channel, C_CV_IFD);

		// MSE - select keys
		executeManageSecurityEnvironment(channel, (byte) 0xC1, (byte) 0xA4,
				KEY_SELECTOR);

		log.debug("OK.");

	}

	private void calculateChannelKeys() throws CardException {

		log.debug("Generating channel keys..");

		if (this.kicc == null || this.kifd == null) {

			throw new CardException(
					"Required data for deriving keys not available.");
		}

		if (this.kicc.length != this.kifd.length) {

			throw new CardException(
					"Required data for deriving keys is invalid.");
		}

		byte[] kifdicc = new byte[this.kicc.length];

		for (int i = 0; i < kifdicc.length; i++) {

			kifdicc[i] = (byte) (this.kicc[i] ^ this.kifd[i]);
		}

		byte[] kEncHashData = new byte[kifdicc.length
				+ KENC_COMPUTATION_TAIL.length];
		byte[] kMacHashData = new byte[kifdicc.length
				+ KMAC_COMPUTATION_TAIL.length];

		System.arraycopy(kifdicc, 0, kEncHashData, 0, kifdicc.length);
		System.arraycopy(kifdicc, 0, kMacHashData, 0, kifdicc.length);

		System.arraycopy(KENC_COMPUTATION_TAIL, 0, kEncHashData,
				kifdicc.length, KENC_COMPUTATION_TAIL.length);
		System.arraycopy(KMAC_COMPUTATION_TAIL, 0, kMacHashData,
				kifdicc.length, KMAC_COMPUTATION_TAIL.length);

		byte[] hashEnc = computeSHA1Hash(kEncHashData);
		byte[] hashMac = computeSHA1Hash(kMacHashData);

		this.kEnc = Arrays.copyOfRange(hashEnc, 0, 16);
		this.kMac = Arrays.copyOfRange(hashMac, 0, 16);

		// compute sequence counter SSC
		if (this.rndIcc == null || this.rndIfd == null
				|| this.rndIcc.length < 4 || this.rndIfd.length < 4) {

			throw new CardException("Data required to compute SSC not valid.");
		}

		this.ssc = new byte[BLOCK_LENGTH];

		System.arraycopy(this.rndIcc, this.rndIcc.length - 4, this.ssc, 0, 4);
		System.arraycopy(this.rndIfd, this.rndIfd.length - 4, this.ssc, 4, 4);

		log.debug("OK.");

	}

	private String formatByteArray(byte[] data) {

		StringBuffer buf = new StringBuffer();

		for (int i = 0; i < data.length; i++) {

			String s = Integer.toHexString(data[i]);

			if (s.length() == 1) {
				s = "0" + s;
			}

			if (s.length() > 2) {
				s = s.substring(s.length() - 2);
			}

			buf.append(s + " ");
		}

		return buf.toString();
	}

	private byte[] intToHex(int val) throws CardException {

		String hexString = Integer.toHexString(val);

		if (hexString.length() > 4) {
			throw new CardException(
					"Unexpected input length to inToHex() utility method: "
							+ hexString.length());
		}

		byte high = 0x00;
		byte low = 0x00;

		if (hexString.length() <= 2) {

			low = (byte) Integer.parseInt(hexString, 16);
		} else {

			low = (byte) Integer.parseInt(hexString.substring(hexString
					.length() - 2), 16);
			high = (byte) Integer.parseInt(hexString.substring(0, hexString
					.length() - 2), 16);
		}

		return new byte[] { high, low };
	}

	private byte[] getRandomBytes(int length) {

		byte[] result = new byte[length];

		for (int i = 0; i < length; i++) {

			Random rand = new Random();
			byte current = (byte) rand.nextInt(255);
			result[i] = current;
		}

		return result;
	}

	private RSAPrivateKey createRSAPrivateKey(String mod, String privExponent)
			throws CardException {

		BigInteger modulus = new BigInteger(mod, 16);
		BigInteger privExp = new BigInteger(privExponent, 16);

		KeyFactory fac;
		RSAPrivateKey key;
		try {
			fac = KeyFactory.getInstance("RSA");
			KeySpec spec = new RSAPrivateKeySpec(modulus, privExp);
			key = (RSAPrivateKey) fac.generatePrivate(spec);
		} catch (Exception e) {

			throw new CardException("Unable to create private key.", e);
		}

		return key;
	}

	private RSAPublicKey createRSAPublicKey(String mod, String pubExponent)
			throws CardException {

		BigInteger modulus = new BigInteger(mod, 16);
		BigInteger pubExp = new BigInteger(pubExponent, 16);

		KeyFactory fac;
		RSAPublicKey key;
		try {
			fac = KeyFactory.getInstance("RSA");
			KeySpec spec = new RSAPublicKeySpec(modulus, pubExp);
			key = (RSAPublicKey) fac.generatePublic(spec);
		} catch (Exception e) {

			throw new CardException("Unable to create public key.", e);
		}

		return key;
	}

	private byte[] rsaEncrypt(Key key, byte[] data)
			throws NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidKeyException, IllegalBlockSizeException, BadPaddingException {

		Cipher rsa = Cipher.getInstance("RSA/ECB/NoPadding");
		rsa.init(Cipher.ENCRYPT_MODE, key);
		byte[] encrypted = rsa.doFinal(data);

		return encrypted;

	}

	private byte[] rsaDecrypt(Key key, byte[] cipher)
			throws NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidKeyException, IllegalBlockSizeException, BadPaddingException {

		Cipher rsa = Cipher.getInstance("RSA/ECB/NoPadding");
		rsa.init(Cipher.DECRYPT_MODE, key);
		byte[] decrypted = rsa.doFinal(cipher);

		return decrypted;
	}

	private X509Certificate createCertificate(byte[] certData)
			throws CardException {

		try {
			InputStream inStream = new ByteArrayInputStream(certData);
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			X509Certificate certificate = (X509Certificate) cf
					.generateCertificate(inStream);
			inStream.close();

			return certificate;

		} catch (Exception e) {

			throw new CardException("Unable to create certificate.", e);
		}
	}

	private byte[] applyPadding(int blockSize, byte[] data) {

		// add mandatory 0x80
		byte[] extended = new byte[data.length + 1];
		System.arraycopy(data, 0, extended, 0, data.length);
		extended[extended.length - 1] = (byte) 0x80;

		if (extended.length % blockSize == 0) {

			return extended;
		}

		int requiredBlocks = ((int) (extended.length / blockSize) + 1);

		byte[] result = new byte[requiredBlocks * blockSize];
		Arrays.fill(result, (byte) 0x00);
		System.arraycopy(extended, 0, result, 0, extended.length);

		return result;

	}

	private void incrementSSC() {

		BigInteger ssc = new BigInteger(this.ssc);
		ssc = ssc.add(new BigInteger("1", 10));
		this.ssc = ssc.toByteArray();
	}

	private byte[] xorByteArrays(byte[] array1, byte[] array2)
			throws CardException {

		if (array1 == null || array2 == null || array1.length != array2.length) {

			throw new CardException("Cannot xor byte arrays - invalid input.");
		}

		byte[] result = new byte[array1.length];

		for (int i = 0; i < array1.length; i++) {

			result[i] = (byte) (array1[i] ^ array2[i]);
		}

		return result;
	}

	private int getCutOffLength(byte[] data, int blockSize)
			throws CardException {

		int len = data.length % blockSize;

		// verify
		if (data[len - 1] == (byte) 0x01) {

			return len;
		} else {
			throw new CardException(
					"Unable to reconstruct encrypted datablock.");
		}

	}

	private byte[] removePadding(byte[] paddedData) throws CardException {

		for (int i = paddedData.length - 1; i >= 0; i--) {

			byte current = paddedData[i];

			if (current == (byte) 0x00) {

				continue;
			}

			if (current == (byte) 0x80) {

				// end of padding reached
				byte[] data = new byte[i];
				System.arraycopy(paddedData, 0, data, 0, i);
				return data;

			} else {

				throw new CardException("Wrong padding.");
			}

		}

		throw new CardException(
				"Error removing padding from data. Unexpected data format.");

	}

	public boolean isEstablished() {
		return established;
	}

	private int getSWAsInt(byte[] sw) throws CardException {

		if (sw.length != 2) {

			throw new CardException(
					"Cannot transform SW to innteger - invalid input length.");
		}

		int sw1 = (int) sw[0] < 0 ? (int) sw[0] + 256 : (int) sw[0];
		int sw2 = (int) sw[1] < 0 ? (int) sw[1] + 256 : (int) sw[1];

		return (sw1 * 256) + sw2;

	}

}
