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
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.gv.egiz.smcc.util.SMCCHelper;

public class DNIeSecuredChannel extends T0CardChannel {

	// Public key data of Root CA - required to validate card certificates
	private static final String ROOT_CA_MODULO = "EADEDA455332945039DAA404C8EBC4D3B7F5DC869283CDEA2F101E2AB54FB0D0B03D8F030DAF2458028288F54CE552F8FA57AB2FB103B112427E11131D1D27E10A5B500EAAE5D940301E30EB26C3E9066B257156ED639D70CCC090B863AFBB3BFED8C17BE7673034B9823E977ED657252927F9575B9FFF6691DB64F80B5E92CD";
	private static final String ROOT_CA_PUBEXP = "010001";

	// Terminal private RSA key for secure channel establishment
	private final String TERMINAL_MODULO = "DB2CB41E112BACFA2BD7C3D3D7967E84FB9434FC261F9D090A8983947DAF8488D3DF8FBDCC1F92493585E134A1B42DE519F463244D7ED384E26D516CC7A4FF7895B1992140043AACADFC12E856B202346AF8226B1A882137DC3C5A57F0D2815C1FCD4BB46FA9157FDFFD79EC3A10A824CCC1EB3CE0B6B4396AE236590016BA69";
	private final String TERMINAL_PRIVEXP = "18B44A3D155C61EBF4E3261C8BB157E36F63FE30E9AF28892B59E2ADEB18CC8C8BAD284B9165819CA4DEC94AA06B69BCE81706D1C1B668EB128695E5F7FEDE18A908A3011A646A481D3EA71D8A387D474609BD57A882B182E047DE80E04B4221416BD39DFA1FAC0300641962ADB109E28CAF50061B68C9CABD9B00313C0F46ED";

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

	// PDU to retrieve card info
	private final byte[] APDU_GET_CHIP_INFO = new byte[] { (byte) 0x90,
			(byte) 0xB8, (byte) 0x00, (byte) 0x00, (byte) 0x07 };

	// Path to card's component certificate
	private final byte[] SECURE_CHANNEL_COMP_CERT_ID = new byte[] {
			(byte) 0x60, (byte) 0x1F };

	// Path to card's intermediate certificate
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

	private final Logger log = LoggerFactory
			.getLogger(DNIeSecuredChannel.class);

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

	public DNIeSecuredChannel(CardChannel channel) {

		super(channel);
		this.established = false;

		try {

			this.establish();

		} catch (CardException e) {

			log.error("Error establishing secure channel with card.", e);
		}
	}

	public void establish() throws CardException {

		log.trace("Try to set up secure channel to card..");

		// select master file
		executeSelectMasterFile();

		// get chip info
		this.snIcc = executeGetChipInfo();

		// get card certificates to establish secure channel
		this.intermediateCert = executeReadCardCertificate(SECURE_CHANNEL_INTERMEDIAT_CERT_ID);
		this.componentCert = executeReadCardCertificate(SECURE_CHANNEL_COMP_CERT_ID);

		// verify card's secure channel certificates
		verifyCertificates();

		// load terminal secure channel certificates and select appropriate keys
		loadTerminalCertsAndSelectKeys();

		// perform internal authentication
		performInternalAuthentication();

		// perform external authentication
		performExternalAuthentication();

		// derive channel keys
		calculateChannelKeys();

		// secure channel successfully established
		this.established = true;
		log.trace("Secure channel successfully established.");

	}

	@Override
	public int transmit(ByteBuffer command, ByteBuffer response)
			throws CardException {

		byte[] commandAPDU = new byte[command.remaining()];
		for (int i = 0; i < commandAPDU.length; i++) {

			commandAPDU[i] = command.get();
		}

		CommandAPDU apdu = new CommandAPDU(commandAPDU);
		ResponseAPDU resp = transmit(apdu);

		byte[] responseData = resp.getBytes();
		for (int i = 0; i < responseData.length; i++) {

			response.put(responseData[i]);
		}

		return responseData.length;
	}

	@Override
	public ResponseAPDU transmit(CommandAPDU apdu) throws CardException {

		if (!this.established) {

			this.establish();
		}

		byte[] plainAPDUData = apdu.getBytes();
		byte[] securedAPDUData = secureAPDU(plainAPDUData);

		CommandAPDU securedAPDU = new CommandAPDU(securedAPDUData);
		ResponseAPDU securedResp = super.transmit(securedAPDU);

		byte[] respData = verifyAndDecryptSecuredResponseAPDU(securedResp
				.getData());
		ResponseAPDU resp = new ResponseAPDU(respData);

		return resp;
	}

	private byte[] executeGetChipInfo() throws CardException {

		// get chip info - read out card serial number
		CommandAPDU command = new CommandAPDU(APDU_GET_CHIP_INFO);
		ResponseAPDU resp = super.transmit(command);

		if (resp.getSW() != 0x9000) {

			log.error("Error getting chip info: "
					+ Integer.toHexString(resp.getSW()));
			throw new CardException("Error getting chip info: "
					+ Integer.toHexString(resp.getSW()));
		}

		return resp.getData();
	}

	private byte[] executeReadCardCertificate(byte[] certId)
			throws CardException {

		byte[] fci = executeSelect(certId);

		byte certLenHigh;
		byte certLenLow;

		if (fci != null && fci.length >= 7) {

			certLenHigh = fci[7];
			certLenLow = fci[8];
		} else {
			log.error("Error reading card certificate: Invalid FCI");
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

			byte[] offsetBytes = SMCCHelper.toByteArray(offset);

			byte[] apdu = new byte[5];
			apdu[0] = (byte) 0x00;
			apdu[1] = (byte) 0xB0;
			apdu[2] = offsetBytes[0];
			apdu[3] = offsetBytes[1];
			apdu[4] = (byte) len;

			CommandAPDU command = new CommandAPDU(apdu);
			ResponseAPDU resp = super.transmit(command);

			byte[] certData = resp.getData();

			try {
				bof.write(certData);
			} catch (IOException e) {
				log.error("Error reading card certificate.", e);
				throw new CardException("Error reading certificate from card",
						e);
			}

			bytesRead = bytesRead + certData.length;
			offset = bytesRead;

			if (bytesRead == bytes2read) {

				done = true;
			}
		}

		return bof.toByteArray();
	}

	private byte[] executeSelect(byte[] id) throws CardException {

		byte[] apduHeader = new byte[] { (byte) 0x00, (byte) 0xA4, (byte) 0x00,
				(byte) 0x00 };

		byte[] apdu = new byte[apduHeader.length + 1 + id.length];
		System.arraycopy(apduHeader, 0, apdu, 0, apduHeader.length);
		apdu[apduHeader.length] = (byte) id.length;
		System.arraycopy(id, 0, apdu, apduHeader.length + 1, id.length);

		CommandAPDU command = new CommandAPDU(apdu);
		ResponseAPDU resp = super.transmit(command);

		if (resp.getSW() != 0x9000) {

			log.error("Error selecting DF or EF: "
					+ Integer.toHexString(resp.getSW()));
			throw new CardException("Unexpected response to Select Command: "
					+ Integer.toHexString(resp.getSW()));
		}

		return resp.getData();
	}

	private void executeSelectMasterFile() throws CardException {

		byte[] apdu = new byte[ESDNIeCard.MASTER_FILE_ID.length + 5];
		apdu[0] = (byte) 0x00;
		apdu[1] = (byte) 0xA4;
		apdu[2] = (byte) 0x04;
		apdu[3] = (byte) 0x00;
		apdu[4] = (byte) ESDNIeCard.MASTER_FILE_ID.length;
		System.arraycopy(ESDNIeCard.MASTER_FILE_ID, 0, apdu, 5,
				ESDNIeCard.MASTER_FILE_ID.length);

		CommandAPDU command = new CommandAPDU(apdu);
		ResponseAPDU resp = super.transmit(command);

		if (resp.getSW() != 0x9000) {

			log.error("Error selecting master file: "
					+ Integer.toHexString(resp.getSW()));
			throw new CardException("Error selecting master file: "
					+ Integer.toHexString(resp.getSW()));
		}
	}

	private void verifyCertificates() throws CardException {

		// This method verifies the card's component and intermediate
		// certificates cryptographically only (no revocation checking).

		RSAPublicKey rootPubKey = DNIeCryptoUtil.createRSAPublicKey(
				ROOT_CA_MODULO, ROOT_CA_PUBEXP);

		X509Certificate intermediate = DNIeCryptoUtil
				.createCertificate(intermediateCert);
		X509Certificate component = DNIeCryptoUtil
				.createCertificate(componentCert);

		try {
			component.verify(intermediate.getPublicKey());
			intermediate.verify(rootPubKey);
		} catch (Exception e) {

			log.error("Error verifying SM card certificate.", e);
			throw new CardException("Certificate verification failed.", e);
		}
	}

	private void loadTerminalCertsAndSelectKeys() throws CardException {

		// MSE
		executeManageSecurityEnvironment((byte) 0x81, (byte) 0xB6, new byte[] {
				(byte) 0x83, (byte) 0x02, (byte) 0x02, (byte) 0x0F });

		// PSO - load intermediate certificate
		executePerformSecurityOperation(C_CV_CA);

		// MSE
		executeManageSecurityEnvironment((byte) 0x81, (byte) 0xB6, CHR);

		// PSO - load terminal certificate
		executePerformSecurityOperation(C_CV_IFD);

		// MSE - select keys
		executeManageSecurityEnvironment((byte) 0xC1, (byte) 0xA4, KEY_SELECTOR);

	}

	private void executeManageSecurityEnvironment(byte p1, byte p2, byte[] data)
			throws CardException {

		// MSE
		CommandAPDU command = new CommandAPDU((byte) 0x00, (byte) 0x22, p1, p2,
				data);
		ResponseAPDU resp = super.transmit(command);

		if (resp.getSW() != 0x9000) {

			log.error("Error executing Manage Security Environment: "
					+ Integer.toHexString(resp.getSW()));
			throw new CardException(
					"Unexpected response from card during preparation of secure channel credentials: "
							+ Integer.toHexString(resp.getSW()));
		}
	}

	private void executePerformSecurityOperation(byte[] data)
			throws CardException {

		// PSO - load intermediate certificate
		CommandAPDU command = new CommandAPDU((byte) 0x00, (byte) 0x2A,
				(byte) 0x00, (byte) 0xAE, data);
		ResponseAPDU resp = super.transmit(command);

		if (resp.getSW() != 0x9000) {

			log.error("Error executing Perform Security Operation: "
					+ Integer.toHexString(resp.getSW()));
			throw new CardException(
					"Unexpected response from card during preparation of secure channel credentials: "
							+ Integer.toHexString(resp.getSW()));
		}
	}

	private void performInternalAuthentication() throws CardException {

		log.trace("Starting internal authentication..");

		byte[] randomBytes = DNIeCryptoUtil.getRandomBytes(BLOCK_LENGTH);
		byte[] challengeData = new byte[randomBytes.length
				+ TERMINAL_CHALLENGE_TAIL.length];

		this.rndIfd = randomBytes;

		System.arraycopy(randomBytes, 0, challengeData, 0, randomBytes.length);
		System.arraycopy(TERMINAL_CHALLENGE_TAIL, 0, challengeData,
				randomBytes.length, TERMINAL_CHALLENGE_TAIL.length);

		byte[] responseData = executeSendTerminalChallenge(challengeData);

		// verify response
		boolean ok = verifyCardResponse(responseData);

		log.trace("Internal Authentiction succeeded: " + ok);

		if (!ok) {

			log
					.error("Internal authentication failed - unable to sucessfully verify card response.");
			throw new CardException("Internal authentication failed");
		}

	}

	private byte[] executeSendTerminalChallenge(byte[] challenge)
			throws CardException {

		// send challenge to card
		CommandAPDU command = new CommandAPDU((byte) 0x00, (byte) 0x88,
				(byte) 0x00, (byte) 0x00, challenge);
		ResponseAPDU resp = super.transmit(command);

		byte[] data = null;

		if (resp.getSW() == 0x9000) {

			data = resp.getData();

		} else {

			log.error("Error sending terminal challenge to card: "
					+ Integer.toHexString(resp.getSW()));
			throw new CardException("Invalid response to terminal challenge: "
					+ Integer.toHexString(resp.getSW()));
		}

		return data;
	}

	private boolean verifyCardResponse(byte[] resp) throws CardException {

		byte[] challenge = this.rndIfd;
		byte[] response = resp;

		// decrypt response with terminal private key
		byte[] plain = null;
		RSAPrivateKey terminalPrivateKey = DNIeCryptoUtil.createRSAPrivateKey(
				TERMINAL_MODULO, TERMINAL_PRIVEXP);
		try {
			plain = DNIeCryptoUtil.rsaDecrypt(terminalPrivateKey, response);
		} catch (Exception e) {
			log.error("Error verifying card response.");
			throw new CardException("Error decrypting card response.", e);
		}

		X509Certificate cert = DNIeCryptoUtil.createCertificate(componentCert);
		PublicKey pubKey = cert.getPublicKey();

		byte[] sig = null;

		try {
			sig = DNIeCryptoUtil.rsaDecrypt(pubKey, plain);

		} catch (Exception e) {

			log.error("Error verifying card response.", e);
			throw new CardException(
					"Error decrypting card response with card's public key", e);
		}

		if (sig == null) {

			log
					.error("Error verifying card response - decryption result is null");
			throw new CardException("Invalid decryption result: null.");
		} else {

			if (sig[0] == (byte) 0x6A && sig[sig.length - 1] == (byte) 0xBC) {

				// Obtained response from card was obviously SIG - nothing else
				// to do here so far

			} else {

				// Obtained response from card was probably N.ICC-SIG -
				// compute N.ICC-SIG and decrypt result again

				RSAPublicKey rsaPubKey = (RSAPublicKey) pubKey;
				BigInteger mod = rsaPubKey.getModulus();
				BigInteger sigVal = SMCCHelper.createUnsignedBigInteger(plain);

				BigInteger substractionResult = mod.subtract(sigVal);
				byte[] encrypted = substractionResult.toByteArray();

				// necessary if substraction result contains leading
				// zero byte
				byte[] trimmed = new byte[128];
				System.arraycopy(encrypted, encrypted.length - 128, trimmed, 0,
						128);

				try {
					sig = DNIeCryptoUtil.rsaDecrypt(pubKey, trimmed);

				} catch (Exception e) {

					log.error("Error verifying card response.", e);
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

		byte[] digest = DNIeCryptoUtil.computeSHA1Hash(hashData);

		boolean internalAuthResult = Arrays.equals(hash, digest);

		if (internalAuthResult) {

			// if verification succeeded, remember kicc for subsequent channel
			// key derivation
			this.kicc = kIcc;
		}

		return internalAuthResult;
	}

	private void performExternalAuthentication() throws CardException {

		log.trace("Performing external authentication.");

		byte[] cardChallenge = executeRequestCardChallenge();

		this.rndIcc = cardChallenge;

		byte[] prnd2 = DNIeCryptoUtil.getRandomBytes(this.prndLength);

		byte[] kIfd = DNIeCryptoUtil.getRandomBytes(32);

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

		byte[] digest = DNIeCryptoUtil.computeSHA1Hash(hashData);

		// prepare data to be encrypted
		byte[] plain = new byte[2 + prnd2.length + kIfd.length + digest.length];

		plain[0] = (byte) 0x6A;

		System.arraycopy(prnd2, 0, plain, 1, prnd2.length);
		System.arraycopy(kIfd, 0, plain, 1 + prnd2.length, kIfd.length);
		System.arraycopy(digest, 0, plain, 1 + prnd2.length + kIfd.length,
				digest.length);

		plain[plain.length - 1] = (byte) 0xBC;

		// encrypt plain data
		RSAPrivateKey terminalPrivateKey = DNIeCryptoUtil.createRSAPrivateKey(
				TERMINAL_MODULO, TERMINAL_PRIVEXP);

		byte[] encResult = null;
		try {
			encResult = DNIeCryptoUtil.rsaEncrypt(terminalPrivateKey, plain);
		} catch (Exception e) {
			log.error("Error performing external authentication.", e);
			throw new CardException("Error encrypting authentication data.", e);
		}

		// apply MIN function
		BigInteger sig = SMCCHelper.createUnsignedBigInteger(encResult);
		BigInteger mod = new BigInteger(TERMINAL_MODULO, 16);

		BigInteger diff = mod.subtract(sig);
		BigInteger sigMin = diff.min(sig);

		// encrypt with card public key
		PublicKey cardPubKey = null;

		X509Certificate cert = DNIeCryptoUtil.createCertificate(componentCert);
		cardPubKey = cert.getPublicKey();

		byte[] authData = null;
		try {
			authData = DNIeCryptoUtil.rsaEncrypt(cardPubKey, sigMin
					.toByteArray());
		} catch (Exception e) {
			log.error("Error performing external authentication.", e);
			throw new CardException("Error encrypting authentication data.", e);
		}

		// send auth data to card
		// BE CAREFUL WITH THAT! EXT-AUTH METHOD MAY GET BLOCKED!
		if (executeExternalAuthenticate(authData)) {

			log.trace("External authentication succeeded.");
			this.kifd = kIfd;
		} else {
			log.error("Error performing external authentication");
			throw new CardException("External Authentication failed.");
		}

	}

	private byte[] executeRequestCardChallenge() throws CardException {

		CommandAPDU command = new CommandAPDU((byte) 0x00, (byte) 0x84,
				(byte) 0x00, (byte) 0x00, (byte) BLOCK_LENGTH);
		ResponseAPDU resp = super.transmit(command);

		if (resp.getSW() != 0x9000) {

			log.error("Error requesting challenge from card: "
					+ Integer.toHexString(resp.getSW()));
			throw new CardException(
					"Invalid response from card upon challenge request: "
							+ Integer.toHexString(resp.getSW()));
		}

		return resp.getData();
	}

	private boolean executeExternalAuthenticate(byte[] authData)
			throws CardException {

		CommandAPDU command = new CommandAPDU((byte) 0x00, (byte) 0x82,
				(byte) 0x00, (byte) 0x00, authData);
		ResponseAPDU resp = super.transmit(command);

		log.trace("Card answer to EXTERNL AUTHENTICATE: "
				+ Integer.toHexString(resp.getSW()));

		return resp.getSW() == 0x9000;
	}

	private void calculateChannelKeys() throws CardException {

		if (this.kicc == null || this.kifd == null) {

			log
					.error("Error generating channel keys - required key data is null.");
			throw new CardException(
					"Required data for deriving keys not available.");
		}

		if (this.kicc.length != this.kifd.length) {

			log.error("Error generating channel keys - invalid key data");
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

		byte[] hashEnc = DNIeCryptoUtil.computeSHA1Hash(kEncHashData);
		byte[] hashMac = DNIeCryptoUtil.computeSHA1Hash(kMacHashData);

		this.kEnc = Arrays.copyOfRange(hashEnc, 0, 16);
		this.kMac = Arrays.copyOfRange(hashMac, 0, 16);

		// compute sequence counter SSC
		if (this.rndIcc == null || this.rndIfd == null
				|| this.rndIcc.length < 4 || this.rndIfd.length < 4) {

			log.error("Error generating channel keys - invlaid ssc data");
			throw new CardException("Data required to compute SSC not valid.");
		}

		this.ssc = new byte[BLOCK_LENGTH];

		System.arraycopy(this.rndIcc, this.rndIcc.length - 4, this.ssc, 0, 4);
		System.arraycopy(this.rndIfd, this.rndIfd.length - 4, this.ssc, 4, 4);
	}

	private byte[] secureAPDUWithoutData(byte[] apdu) throws CardException {

		if (apdu.length < 4 || apdu.length > 5) {

			log.error("Error securing APDU - invalid APDU length: "
					+ apdu.length);
			throw new CardException("Invalid APDU length.");
		}

		boolean leAvailable = apdu.length == 5;

		byte encCLA = (byte) (apdu[0] | (byte) 0x0C);
		byte[] encHeader = new byte[] { encCLA, apdu[1], apdu[2], apdu[3] };
		byte[] paddedHeader = DNIeCryptoUtil.applyPadding(BLOCK_LENGTH,
				encHeader);

		int leFieldLen;
		byte[] leField = null;
		if (leAvailable) {
			leField = new byte[3];
			leField[0] = (byte) 0x97;
			leField[1] = (byte) 0x01;
			leField[2] = apdu[4];
			leFieldLen = leField.length;
		} else {

			leFieldLen = 0;
		}

		byte[] macData = new byte[paddedHeader.length + leFieldLen];
		System.arraycopy(paddedHeader, 0, macData, 0, paddedHeader.length);

		if (leAvailable) {
			System.arraycopy(leField, 0, macData, paddedHeader.length,
					leField.length);

			macData = DNIeCryptoUtil.applyPadding(BLOCK_LENGTH, macData);
		}

		incrementSSC();

		byte[] mac = DNIeCryptoUtil.calculateAPDUMAC(macData, kMac, this.ssc,
				BLOCK_LENGTH);

		byte[] encapsulatedMac = new byte[mac.length + 2];
		encapsulatedMac[0] = (byte) 0x8E;
		encapsulatedMac[1] = (byte) mac.length;
		System.arraycopy(mac, 0, encapsulatedMac, 2, mac.length);

		byte[] completeMessage = new byte[5 + leFieldLen
				+ encapsulatedMac.length];
		completeMessage[0] = encCLA;
		completeMessage[1] = apdu[1];
		completeMessage[2] = apdu[2];
		completeMessage[3] = apdu[3];
		completeMessage[4] = (byte) (encapsulatedMac.length + leFieldLen);

		if (leAvailable) {
			System.arraycopy(leField, 0, completeMessage, 5, leField.length);
		}

		System.arraycopy(encapsulatedMac, 0, completeMessage, 5 + leFieldLen,
				encapsulatedMac.length);

		return completeMessage;

	}

	private byte[] secureAPDUWithData(byte[] apdu) throws CardException {

		if (apdu.length < 6) {

			log.error("Error securing APDU - invalid APDU length: "
					+ apdu.length);
			throw new CardException(
					"Error securing APDU - invalid APDU length: " + apdu.length);
		}

		byte cla = apdu[0];
		byte ins = apdu[1];
		byte p1 = apdu[2];
		byte p2 = apdu[3];
		byte lc = apdu[4];

		boolean leAvailable;
		if (apdu.length == lc + 5 + 1) {

			leAvailable = true;
		} else if (apdu.length != lc + 5) {

			log.error("Error securing APDU - invalid APDU length: "
					+ apdu.length);
			throw new CardException("Invalid APDU length or format.");
		} else {

			leAvailable = false;
		}

		byte[] leField = null;
		if (leAvailable) {

			byte le = apdu[apdu.length - 1];

			leField = new byte[3];
			leField[0] = (byte) 0x97;
			leField[1] = (byte) 0x01;
			leField[2] = le;
		}

		byte[] data = new byte[lc];
		System.arraycopy(apdu, 5, data, 0, lc);

		byte[] paddedData = DNIeCryptoUtil.applyPadding(BLOCK_LENGTH, data);

		byte[] encrypted = null;

		try {

			encrypted = DNIeCryptoUtil.perform3DESCipherOperation(paddedData,
					kEnc, Cipher.ENCRYPT_MODE);

		} catch (Exception e) {

			log.error("Error encrypting APDU.", e);
			throw new CardException("Error encrypting APDU.", e);
		}

		byte[] encapsulated = new byte[encrypted.length + 3];
		encapsulated[0] = (byte) 0x87;
		encapsulated[1] = (byte) (encrypted.length + 1);
		encapsulated[2] = (byte) 0x01;
		System.arraycopy(encrypted, 0, encapsulated, 3, encrypted.length);

		// calculate MAC
		byte encCLA = (byte) (cla | (byte) 0x0C);
		byte[] encHeader = new byte[] { encCLA, ins, p1, p2 };
		byte[] paddedHeader = DNIeCryptoUtil.applyPadding(BLOCK_LENGTH,
				encHeader);

		byte[] headerAndData = new byte[paddedHeader.length
				+ encapsulated.length];
		System
				.arraycopy(paddedHeader, 0, headerAndData, 0,
						paddedHeader.length);
		System.arraycopy(encapsulated, 0, headerAndData, paddedHeader.length,
				encapsulated.length);

		if (leAvailable) {
			byte[] macData = new byte[headerAndData.length + leField.length];
			System
					.arraycopy(headerAndData, 0, macData, 0,
							headerAndData.length);
			System.arraycopy(leField, 0, macData, headerAndData.length,
					leField.length);

			headerAndData = macData;
		}

		byte[] paddedHeaderAndData = DNIeCryptoUtil.applyPadding(BLOCK_LENGTH,
				headerAndData);

		incrementSSC();

		byte[] mac = DNIeCryptoUtil.calculateAPDUMAC(paddedHeaderAndData, kMac,
				this.ssc, BLOCK_LENGTH);

		byte[] encapsulatedMac = new byte[mac.length + 2];
		encapsulatedMac[0] = (byte) 0x8E;
		encapsulatedMac[1] = (byte) mac.length;
		System.arraycopy(mac, 0, encapsulatedMac, 2, mac.length);

		int leFieldLen;
		if (leAvailable) {
			leFieldLen = leField.length;
		} else {
			leFieldLen = 0;
		}

		byte[] completeMessage = new byte[5 + encapsulated.length
				+ encapsulatedMac.length + leFieldLen];
		completeMessage[0] = encCLA;
		completeMessage[1] = ins;
		completeMessage[2] = p1;
		completeMessage[3] = p2;

		completeMessage[4] = (byte) (encapsulated.length + leFieldLen + encapsulatedMac.length);
		System.arraycopy(encapsulated, 0, completeMessage, 5,
				encapsulated.length);

		if (leAvailable) {
			System.arraycopy(leField, 0, completeMessage,
					5 + encapsulated.length, leFieldLen);
		}

		System.arraycopy(encapsulatedMac, 0, completeMessage, 5
				+ encapsulated.length + leFieldLen, encapsulatedMac.length);

		return completeMessage;

	}

	private byte[] secureAPDU(byte[] apdu) throws CardException {

		if (apdu == null || apdu.length < 4) {

			log.error("Invalid APDU to secure.");
			throw new CardException("Invalid APDU to secure.");
		}

		if (apdu.length == 4 || apdu.length == 5) {

			return secureAPDUWithoutData(apdu);
		}

		if (apdu.length > 5) {

			return secureAPDUWithData(apdu);
		}

		throw new CardException("Error securing APDU - unexpected APDU length.");
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

		byte[] paddedMacData = DNIeCryptoUtil.applyPadding(BLOCK_LENGTH,
				macData);

		incrementSSC();

		byte[] mac = DNIeCryptoUtil.calculateAPDUMAC(paddedMacData, this.kMac,
				this.ssc, BLOCK_LENGTH);

		if (!Arrays.equals(mac, obtainedMac)) {

			log
					.error("Error verifiying MAC of secured response. MAC values do not match.");
			throw new CardException("Unable to verify MAC of Response APDU.");
		}

		if (data.length > 0) {

			byte[] data2decrypt = new byte[data.length
					- DNIeCryptoUtil.getCutOffLength(data, BLOCK_LENGTH)];
			System.arraycopy(data, DNIeCryptoUtil.getCutOffLength(data,
					BLOCK_LENGTH), data2decrypt, 0, data2decrypt.length);

			byte[] plainData = null;

			try {
				plainData = DNIeCryptoUtil.perform3DESCipherOperation(
						data2decrypt, this.kEnc, Cipher.DECRYPT_MODE);
			} catch (Exception e) {
				log.error("Error decrypting data.", e);
				throw new CardException("Unable to decrypt data.", e);
			}

			byte[] unpaddedData = DNIeCryptoUtil.removePadding(plainData);

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

	private void incrementSSC() {

		BigInteger ssc = new BigInteger(this.ssc);
		ssc = ssc.add(new BigInteger("1", 10));
		this.ssc = ssc.toByteArray();
	}
}
