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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
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
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import javax.smartcardio.TerminalFactory;

import org.junit.Ignore;

import at.gv.egiz.smcc.pin.gui.PINGUI;
import at.gv.egiz.smcc.util.SMCCHelper;

@Ignore
@SuppressWarnings("unused")
public class ESCardTest extends AbstractSignatureCard {

	private static final String ROOT_CA_MODULO = "EADEDA455332945039DAA404C8EBC4D3B7F5DC869283CDEA2F101E2AB54FB0D0B03D8F030DAF2458028288F54CE552F8FA57AB2FB103B112427E11131D1D27E10A5B500EAAE5D940301E30EB26C3E9066B257156ED639D70CCC090B863AFBB3BFED8C17BE7673034B9823E977ED657252927F9575B9FFF6691DB64F80B5E92CD";
	private static final String ROOT_CA_PUBEXP = "010001";

	private static final String TERMINAL_MODULO = "DB2CB41E112BACFA2BD7C3D3D7967E84FB9434FC261F9D090A8983947DAF8488D3DF8FBDCC1F92493585E134A1B42DE519F463244D7ED384E26D516CC7A4FF7895B1992140043AACADFC12E856B202346AF8226B1A882137DC3C5A57F0D2815C1FCD4BB46FA9157FDFFD79EC3A10A824CCC1EB3CE0B6B4396AE236590016BA69";
	private static final String TERMINAL_PRIVEXP = "18B44A3D155C61EBF4E3261C8BB157E36F63FE30E9AF28892B59E2ADEB18CC8C8BAD284B9165819CA4DEC94AA06B69BCE81706D1C1B668EB128695E5F7FEDE18A908A3011A646A481D3EA71D8A387D474609BD57A882B182E047DE80E04B4221416BD39DFA1FAC0300641962ADB109E28CAF50061B68C9CABD9B00313C0F46ED";
	private static final String TERMINAL_PUBEXP = "010001";

	private static final byte[] C_CV_CA = new byte[] {

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

	private static final byte[] CHR = new byte[] {

	(byte) 0x83, (byte) 0x08, (byte) 0x65, (byte) 0x73, (byte) 0x53,
			(byte) 0x44, (byte) 0x49, (byte) 0x60, (byte) 0x00, (byte) 0x06 };

	// private static final byte[] C_CV_IFD = new byte[] {
	//		
	// (byte)0x7F, (byte)0x21, (byte)0x81, (byte)0xCD, (byte)0x5F, (byte)0x37,
	// (byte)0x81, (byte)0x80, (byte)0x25, (byte)0x63, (byte)0xFC, (byte)0xF6,
	// (byte)0x71, (byte)0x46, (byte)0x24, (byte)0xC0, (byte)0xC4, (byte)0xF5,
	// (byte)0xD7, (byte)0x5F, (byte)0x67, (byte)0x30, (byte)0x5E, (byte)0xBF,
	// (byte)0x8F, (byte)0x3A, (byte)0x6B, (byte)0xB7, (byte)0x72, (byte)0x99,
	// (byte)0x6A, (byte)0xFB, (byte)0x64, (byte)0x97, (byte)0xBB, (byte)0x46,
	// (byte)0x6A, (byte)0x23, (byte)0x8E, (byte)0x73, (byte)0x1D, (byte)0x08,
	// (byte)0xD3, (byte)0x78, (byte)0xE9, (byte)0xF4, (byte)0xEC, (byte)0xC0,
	// (byte)0x40, (byte)0x70, (byte)0xE3, (byte)0x71, (byte)0x7C, (byte)0x13,
	// (byte)0x8E, (byte)0xA8, (byte)0xD6, (byte)0xD3, (byte)0x5A, (byte)0x14,
	// (byte)0xED, (byte)0x18, (byte)0x62, (byte)0xB7, (byte)0xF8, (byte)0x4E,
	// (byte)0x35, (byte)0x1B, (byte)0x2D, (byte)0xCE, (byte)0x4C, (byte)0xFB,
	// (byte)0x30, (byte)0xF8, (byte)0xC7, (byte)0x6B, (byte)0x8A, (byte)0xD1,
	// (byte)0x73, (byte)0x1E, (byte)0x9A, (byte)0xA8, (byte)0x4A, (byte)0xB0,
	// (byte)0xB3, (byte)0xBD, (byte)0x30, (byte)0xC3, (byte)0xF0, (byte)0x0D,
	// (byte)0xA2, (byte)0x74, (byte)0xE2, (byte)0x00, (byte)0x5A, (byte)0x51,
	// (byte)0xEB, (byte)0x42, (byte)0x13, (byte)0xFD, (byte)0x55, (byte)0x23,
	// (byte)0xAB, (byte)0xC9, (byte)0x75, (byte)0x84, (byte)0xA9, (byte)0xFB,
	// (byte)0xD2, (byte)0x57, (byte)0x6C, (byte)0xB5, (byte)0xDD, (byte)0x9D,
	// (byte)0xD5, (byte)0x72, (byte)0xAE, (byte)0x49, (byte)0xA5, (byte)0x97,
	// (byte)0xE1, (byte)0x4E, (byte)0xCF, (byte)0xFA, (byte)0x91, (byte)0xF7,
	// (byte)0x6E, (byte)0x04, (byte)0xF6, (byte)0x08, (byte)0x12, (byte)0x92,
	// (byte)0xAE, (byte)0x07, (byte)0xCE, (byte)0xF7, (byte)0x5F, (byte)0x38,
	// (byte)0x3C, (byte)0x25, (byte)0x8B, (byte)0x73, (byte)0xCF, (byte)0xB9,
	// (byte)0x4A, (byte)0x73, (byte)0x9C, (byte)0xB5, (byte)0xE9, (byte)0x73,
	// (byte)0x92, (byte)0xE5, (byte)0x99, (byte)0xE8, (byte)0xFB, (byte)0x45,
	// (byte)0xA6, (byte)0x00, (byte)0x72, (byte)0xCA, (byte)0xA6, (byte)0xFC,
	// (byte)0xD5, (byte)0xF2, (byte)0x15, (byte)0xC3, (byte)0xC7, (byte)0xE0,
	// (byte)0x25, (byte)0xEA, (byte)0x3C, (byte)0x9E, (byte)0xB2, (byte)0xCB,
	// (byte)0xE4, (byte)0x7D, (byte)0xE9, (byte)0xFE, (byte)0xE8, (byte)0x00,
	// (byte)0x2B, (byte)0xF2, (byte)0xF6, (byte)0xD4, (byte)0xA2, (byte)0x43,
	// (byte)0x50, (byte)0xAB, (byte)0x3F, (byte)0x5F, (byte)0x15, (byte)0xDB,
	// (byte)0x05, (byte)0xA1, (byte)0x27, (byte)0x00, (byte)0x01, (byte)0x00,
	// (byte)0x01, (byte)0x42, (byte)0x08, (byte)0x65, (byte)0x73, (byte)0x53,
	// (byte)0x44, (byte)0x49, (byte)0x60, (byte)0x00, (byte)0x06
	// };

	private static final byte[] KEY_SELECTOR = new byte[] {

	(byte) 0x83, (byte) 0x0C, (byte) 0x00, (byte) 0x00, (byte) 0x00,
			(byte) 0x00, (byte) 0x20, (byte) 0x00, (byte) 0x00, (byte) 0x00,
			(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x84,
			(byte) 0x02, (byte) 0x02, (byte) 0x1F };

	private static final byte[] C_CV_IFD = new byte[] {

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

	private static final byte[] RANDOM_TAIL = new byte[] {

	(byte) 0x20, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
			(byte) 0x00, (byte) 0x00, (byte) 0x01 };

	private static final byte[] KENC_COMPUTATION_TAIL = new byte[] {

	(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01 };

	private static final byte[] KMAC_COMPUTATION_TAIL = new byte[] {

	(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x02 };

	// private static final byte[] TEST_CHALLENGE = new byte[] {
	//		
	// (byte)0xfe, (byte)0x65, (byte)0x72, (byte)0x83, (byte)0xd6, (byte)0x14,
	// (byte)0xc5, (byte)0x4e
	// };
	//	
	// private static final byte[] TEST_RESPONSE = new byte[] {
	//		
	// (byte)0xce, (byte)0x19, (byte)0x59, (byte)0x34, (byte)0x09, (byte)0xe0,
	// (byte)0xf4, (byte)0x06, (byte)0xa0, (byte)0x95, (byte)0x45, (byte)0x7c,
	// (byte)0x81, (byte)0x2a, (byte)0x07, (byte)0x7d, (byte)0x1c, (byte)0xc3,
	// (byte)0xcf, (byte)0x46, (byte)0x15, (byte)0xc9, (byte)0x39, (byte)0x8b,
	// (byte)0x16, (byte)0x1b, (byte)0x06, (byte)0x50, (byte)0xbb, (byte)0xc1,
	// (byte)0x60, (byte)0x65, (byte)0xc4, (byte)0xa3, (byte)0xad, (byte)0xcd,
	// (byte)0x10, (byte)0x26, (byte)0x51, (byte)0xd0, (byte)0x1e, (byte)0x23,
	// (byte)0x80, (byte)0x57, (byte)0xd6, (byte)0xae, (byte)0x8a, (byte)0xb4,
	// (byte)0xd8, (byte)0xb8, (byte)0xbb, (byte)0x68, (byte)0x0d, (byte)0x6f,
	// (byte)0xae, (byte)0xf1, (byte)0xef, (byte)0x6b, (byte)0x5b, (byte)0x20,
	// (byte)0xd8, (byte)0x45, (byte)0x26, (byte)0xb3, (byte)0x39, (byte)0x9f,
	// (byte)0xd5, (byte)0xad, (byte)0x0d, (byte)0x7d, (byte)0x3b, (byte)0xac,
	// (byte)0x7f, (byte)0x11, (byte)0x2f, (byte)0xee, (byte)0xc4, (byte)0xe8,
	// (byte)0x4a, (byte)0xd5, (byte)0xd0, (byte)0x55, (byte)0x3f, (byte)0xe3,
	// (byte)0x1b, (byte)0xaf, (byte)0xd9, (byte)0x93, (byte)0x7f, (byte)0xa5,
	// (byte)0xa0, (byte)0x4f, (byte)0x9f, (byte)0xb4, (byte)0xa0, (byte)0x65,
	// (byte)0xc6, (byte)0x4a, (byte)0xc9, (byte)0xf4, (byte)0xf7, (byte)0x09,
	// (byte)0x9a , (byte)0x56, (byte)0x3f, (byte)0xb3, (byte)0xdb, (byte)0xa3,
	// (byte)0x96, (byte)0x10, (byte)0xd5, (byte)0xae, (byte)0x70, (byte)0x56,
	// (byte)0xc5, (byte)0x8c, (byte)0x32, (byte)0x44, (byte)0x19, (byte)0x2a,
	// (byte)0x62, (byte)0xae, (byte)0xa9, (byte)0x12, (byte)0xf6, (byte)0x86,
	// (byte)0x3e, (byte)0x76
	//		
	// };
	//	
	// private static final byte[] TEST_RESPONSE_DOCU = new byte[] {
	//	
	// (byte)0x43,(byte)0x73,(byte)0x93,(byte)0xb3,(byte)0x0d,(byte)0x1f,(byte)0x01,(byte)0x61,(byte)0x45,(byte)0x5b,(byte)0xb1,(byte)0x32,(byte)0xe9,(byte)0x99,(byte)0xde,(byte)0x7b,(byte)0xb8,(byte)0xf7,(byte)0xd8,(byte)0x2f,(byte)0x91,(byte)0xd5,(byte)0x07,(byte)0xd6
	// ,(byte)0xd1,(byte)0x16,(byte)0x07,(byte)0x4d,(byte)0x33,(byte)0xe6,(byte)0x04,(byte)0x57,(byte)0xf1,(byte)0x89,(byte)0xb9,(byte)0x76,(byte)0x23,(byte)0x5c,(byte)0xab,(byte)0x57,(byte)0x62,(byte)0xb6,(byte)0x4f,(byte)0x89,(byte)0x6b,(byte)0xe8,(byte)0xa9,(byte)0x24
	// ,(byte)0x1a,(byte)0x24,(byte)0x5d,(byte)0xca,(byte)0xc9,(byte)0x76,(byte)0xfa,(byte)0x2d,(byte)0x0c,(byte)0xac,(byte)0x87,(byte)0x19,(byte)0x15,(byte)0x7e,(byte)0x29,(byte)0x27,(byte)0xc6,(byte)0x1e,(byte)0x0b,(byte)0xcb,(byte)0x48,(byte)0xb5,(byte)0x11,(byte)0x70
	// ,(byte)0xea,(byte)0x08,(byte)0x98,(byte)0x38,(byte)0x1e,(byte)0xf9,(byte)0x19,(byte)0x39,(byte)0x8e,(byte)0x46,(byte)0x41,(byte)0x78,(byte)0x99,(byte)0xab,(byte)0xe8,(byte)0x27,(byte)0x08,(byte)0xad,(byte)0xdd,(byte)0x1b,(byte)0x75,(byte)0x5c,(byte)0x05,(byte)0x6f
	// ,(byte)0x5f,(byte)0x7b,(byte)0x96,(byte)0xba,(byte)0x69,(byte)0xbd,(byte)0x56,(byte)0xfc,(byte)0x57,(byte)0x6c,(byte)0x80,(byte)0x9b,(byte)0x27,(byte)0xb8,(byte)0xf8,(byte)0x36,(byte)0x4f,(byte)0xc4,(byte)0xd5,(byte)0x59,(byte)0xd1,(byte)0xda,(byte)0x86,(byte)0x81
	// ,(byte)0xfa,(byte)0x04,(byte)0x14,(byte)0x5d,(byte)0xdd,(byte)0x63,(byte)0x6e,(byte)0xb7
	// };
	//
	// private static final byte[] TEST_CHALLENGE_2 = new byte[] {
	//		
	// (byte)0x15, (byte)0x5A, (byte)0xA9, (byte)0x3F, (byte)0xD5, (byte)0xE3,
	// (byte)0xBC, (byte)0xA3
	// };
	//	
	// private static final byte[] TEST_RESPONSE_2 = new byte[] {
	//		
	// (byte)0x0d,(byte)0x82,(byte)0xd5 ,(byte)0xbb ,(byte)0x84 ,(byte)0x00
	// ,(byte)0x2b ,(byte)0x2a ,(byte)0xed ,(byte)0x57 ,(byte)0x6e ,(byte)0x63
	// ,(byte)0x6f ,(byte)0xf3 ,(byte)0xc6 ,(byte)0x1a ,(byte)0xde ,(byte)0xb6
	// ,(byte)0x28 ,(byte)0x6b ,(byte)0x23 ,(byte)0x9c ,(byte)0xfb ,(byte)0x7c
	// ,(byte)0x65 ,(byte)0xce ,(byte)0x6e ,(byte)0x1a ,(byte)0x49 ,(byte)0x57
	// ,(byte)0x47 ,(byte)0x41 ,(byte)0xb1 ,(byte)0x7e ,(byte)0x85 ,(byte)0x10
	// ,(byte)0xaa ,(byte)0x0f ,(byte)0xef ,(byte)0x44 ,(byte)0x89 ,(byte)0x7a
	// ,(byte)0xcd ,(byte)0xfc ,(byte)0xbb ,(byte)0x33 ,(byte)0x9a ,(byte)0x7e
	// ,(byte)0xaf ,(byte)0x2c ,(byte)0xa2 ,(byte)0x46 ,(byte)0xb3 ,(byte)0xba
	// ,(byte)0x8f ,(byte)0x7c ,(byte)0x16 ,(byte)0x3a ,(byte)0x36 ,(byte)0xf1
	// ,(byte)0xbc ,(byte)0x89 ,(byte)0x53 ,(byte)0x9d ,(byte)0x74 ,(byte)0x72
	// ,(byte)0x21 ,(byte)0xb0 ,(byte)0xb2 ,(byte)0xd9 ,(byte)0xa3 ,(byte)0xc4
	// ,(byte)0xa0 ,(byte)0xa2 ,(byte)0xd8 ,(byte)0x2b ,(byte)0x30 ,(byte)0x4f
	// ,(byte)0xbd ,(byte)0x6f ,(byte)0xc3 ,(byte)0xfc ,(byte)0xd4 ,(byte)0x2d
	// ,(byte)0xea ,(byte)0x1d ,(byte)0x08 ,(byte)0xad ,(byte)0x1c ,(byte)0x98
	// ,(byte)0x51 ,(byte)0xb3 ,(byte)0x0f ,(byte)0xbf ,(byte)0x11 ,(byte)0x6e
	// ,(byte)0x78 ,(byte)0x0b ,(byte)0xb4 ,(byte)0x0f ,(byte)0x1f ,(byte)0x55
	// ,(byte)0xc7 ,(byte)0xc9 ,(byte)0x75 ,(byte)0xcb ,(byte)0xe3 ,(byte)0x36
	// ,(byte)0x17 ,(byte)0x63 ,(byte)0xe4 ,(byte)0xed ,(byte)0x8d ,(byte)0xe4
	// ,(byte)0xaa ,(byte)0x80 ,(byte)0xf6 ,(byte)0xd7 ,(byte)0x73 ,(byte)0x0a
	// ,(byte)0xc1 ,(byte)0xdf ,(byte)0xa9 ,(byte)0x89 ,(byte)0x02 ,(byte)0xc7
	// ,(byte)0x2b ,(byte)0xa8
	// };
	//	
	// private static final byte[] TEST_CARD_CHALLENGE = new byte[] {
	//	
	// (byte)0xea,(byte)0xef,(byte)0xa8,(byte)0xfb,(byte)0xd3,(byte)0x1a,(byte)0xc8,(byte)0xec
	// };
	//	
	// private static final byte[] TEST_PRND2 = new byte[] {
	//		
	// (byte)0x2a,(byte)0x5c,(byte)0x73,(byte)0x00,(byte)0xd9,(byte)0x6d,(byte)0xf9,(byte)0x19,(byte)0x0e,(byte)0xc2,(byte)0xae,(byte)0x1c,(byte)0xef,(byte)0x89,(byte)0xbd,(byte)0xe1,(byte)0x56,(byte)0x78,(byte)0xa7,(byte)0x8b,(byte)0x7f,(byte)0x7d,(byte)0x4d,(byte)0xf3
	// ,(byte)0xd7,(byte)0xd9,(byte)0xbc,(byte)0xfa,(byte)0x31,(byte)0x3a,(byte)0x09,(byte)0x86,(byte)0xef,(byte)0xcc,(byte)0x39,(byte)0x69,(byte)0x5b,(byte)0xd2,(byte)0x98,(byte)0x12,(byte)0xd2,(byte)0xce,(byte)0x59,(byte)0x31,(byte)0xe1,(byte)0xc2,(byte)0xfc,(byte)0x2d
	// ,(byte)0x06,(byte)0x8a,(byte)0x5c,(byte)0xaf,(byte)0x3d,(byte)0xe5,(byte)0xba,(byte)0xfe,(byte)0xf6,(byte)0x21,(byte)0xcd,(byte)0x8b,(byte)0xa7,(byte)0x08,(byte)0x26,(byte)0x59,(byte)0xa3,(byte)0x07,(byte)0xd9,(byte)0xa4,(byte)0x53,(byte)0x18,(byte)0xd4,(byte)0x79
	// ,(byte)0x80,(byte)0x84
	// };
	//	
	// private static final byte[] TEST_KIFD = new byte[] {
	//	
	// (byte)0xcd,(byte)0x97,(byte)0xdb,(byte)0xf2,(byte)0x23,(byte)0x57,(byte)0x60,(byte)0xd8,(byte)0xb3,(byte)0xe9,(byte)0xc4,(byte)0xd7,(byte)0xe9,(byte)0xa6,(byte)0x91,(byte)0xfb,(byte)0x12,(byte)0xcd,(byte)0x20,(byte)0x7c,(byte)0x42,(byte)0x66,(byte)0x0d,(byte)0xf9
	// ,(byte)0xc6,(byte)0x93,(byte)0x57,(byte)0xba,(byte)0x7b,(byte)0x25,(byte)0xd0,(byte)0xfc
	// };

	private static final byte[] IV = new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00,
			0x00, 0x00, 0x00 };

	private int prndLength;
	private byte[] snIcc;
	private byte[] componentCert;
	private byte[] intermediateCert;

	private byte[] kicc;
	private byte[] kifd;

	private byte[] kEnc;
	private byte[] kMac;

	private byte[] rndIcc;
	private byte[] rndIfd;
	private byte[] ssc;

	private byte[] sigVal;
	private byte[] sigCert;

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		System.setProperty("sun.security.smartcardio.t0GetResponse", "false");

		// IAIK.addAsProvider();

		ESCardTest tester = new ESCardTest();

		tester.cardTest();
//		tester.byteBufferTest();
//		tester.testEchtCert();
//		try {
//			CardChannel channel = tester.setupCardChannel();
//
//			for (int i = 0; i < 1; i++) {
//				tester.establishSecureChannel(channel);
//			}
//
//		} catch (Exception e) {
//			e.printStackTrace();
//		}

		
		
	}

	private void cardTest() {
		
	    SMCCHelper helper = new SMCCHelper();

	    SignatureCard signatureCard = helper.getSignatureCard(Locale.getDefault());
	    
	    try {
//			signatureCard.createSignature(null, null, null, null);
	    	signatureCard.getCertificate(null, null);
		} catch (SignatureCardException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
		}
		
	}
	
	private void byteBufferTest() {
		
		byte[] testarray = new byte[]{(byte)0x05,(byte)0x07,(byte)0x09,(byte)0x0B,(byte)0x0D};		
		ByteBuffer buf = ByteBuffer.wrap(testarray);
		
		System.out.println("Position:" + buf.position());
		System.out.println("Remaining:" + buf.remaining());
		System.out.println("Get: " + buf.get());
		System.out.println("Position:" + buf.position());
		System.out.println("Remaining:" + buf.remaining());
		
		buf.put((byte)0x11);
		System.out.println("Position:" + buf.position());
		System.out.println("Remaining:" + buf.remaining());
		
		printByteArray(buf.array());
	}
	
	private void testEchtCert() {

		try {
		java.io.File file = new java.io.File("f:/c.hex");
		InputStream is = new FileInputStream(file);

		long length = file.length();

		byte[] bytes = new byte[(int) length];
		// Read in the bytes
		int offset = 0;
		int numRead = 0;
		while (offset < bytes.length
				&& (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
			offset += numRead;
		}

		if (offset < bytes.length) {
			is.close();
			throw new IOException("Could not completely read file "
					+ file.getName());
		}

		is.close(); 
		
		
		int uncompressedDataLen = getUncompressedDataLength(bytes);

		byte[] compressedWithoutHeader = new byte[bytes.length - 8];
		System.arraycopy(bytes, 8, compressedWithoutHeader, 0,
				compressedWithoutHeader.length);

		byte[] decompressed = decompressData(compressedWithoutHeader,
				uncompressedDataLen);

		writeDataToFile(decompressed, "F:/cert2.cer");		
		
		} catch(Exception e) {
			
			e.printStackTrace();
		}
		
	}

	private byte[] secure4ByteAPDU(byte[] apdu) throws CardException {
		
		if(apdu.length != 4) {
			
			throw new CardException("Invalid APDU length.");
		}

		byte encCLA = (byte) (apdu[0] | (byte) 0x0C);
		byte[] encHeader = new byte[] { encCLA, apdu[1], apdu[2],
				apdu[3] };
		byte[] paddedHeader = DNIeCryptoUtil.applyPadding(8,
				encHeader);
		
		byte[] macData = new byte[paddedHeader.length];
		System.arraycopy(paddedHeader, 0, macData, 0,
				paddedHeader.length);
		

//		byte[] paddedMacData = DNIeCryptoUtil.applyPadding(
//				8, macData);

		incrementSSC();

		System.out.println("MAC data:");
		printByteArray(macData);
		
		byte[] mac = DNIeCryptoUtil.calculateAPDUMAC(macData,
				kMac, this.ssc, 8);

		System.out.println("MAC:");
		printByteArray(mac);
		
		byte[] encapsulatedMac = new byte[mac.length + 2];
		encapsulatedMac[0] = (byte) 0x8E;
		encapsulatedMac[1] = (byte) mac.length;
		System.arraycopy(mac, 0, encapsulatedMac, 2, mac.length);

		byte[] completeMessage = new byte[5+ encapsulatedMac.length];
		completeMessage[0] = encCLA;
		completeMessage[1] = apdu[1];
		completeMessage[2] = apdu[2];
		completeMessage[3] = apdu[3];
		completeMessage[4] = (byte) (encapsulatedMac.length);

		
		System.arraycopy(encapsulatedMac, 0, completeMessage,
				5, encapsulatedMac.length);

		System.out.println("Secured 4 Byte APDU to: ");
		printByteArray(completeMessage);
		
		return completeMessage;		
		
	}	
	
	
	private void testZLib() {

		try {
			// Encode a String into bytes
			String inputString = "blahblahblah??";
			byte[] input = inputString.getBytes("UTF-8");

			// Compress the bytes
			byte[] output = new byte[100];
			Deflater compresser = new Deflater();
			compresser.setInput(input);
			compresser.finish();
			int compressedDataLength = compresser.deflate(output);

			System.out.println("compressed data (len):" + compressedDataLength);
			printByteArray(output);

			// Decompress the bytes
			Inflater decompresser = new Inflater();
			decompresser.setInput(output, 0, compressedDataLength);
			byte[] result = new byte[100];
			int resultLength = decompresser.inflate(result);
			decompresser.end();

			System.out.println("Decompressed data (len):" + resultLength);
			printByteArray(result);

			// Decode the bytes into a String
			String outputString = new String(result, 0, resultLength, "UTF-8");
		} catch (java.io.UnsupportedEncodingException ex) {
			// handle
		} catch (java.util.zip.DataFormatException ex) {
			// handle
		}

	}


	private byte[] decompressData(byte[] input, int len) throws CardException {

		Inflater decompresser = new Inflater();
		decompresser.setInput(input, 0, input.length);
		byte[] buffer = new byte[256];
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		
		try {
			while(!decompresser.finished()) {
			
				int numBytes = decompresser.inflate(buffer);
				bos.write(buffer, 0, numBytes);
			}

		} catch (DataFormatException e) {

			throw new CardException("Error decompressing file.", e);
		}

		return bos.toByteArray();
	}	
	
	private void testSigVerify() throws CardException {

		this.kEnc = new byte[] {

		(byte) 0x59, (byte) 0x8f, (byte) 0x26, (byte) 0xe3, (byte) 0x6e,
				(byte) 0x11, (byte) 0xa8, (byte) 0xec, (byte) 0x14,
				(byte) 0xb8, (byte) 0x1e, (byte) 0x19, (byte) 0xbd,
				(byte) 0xa2, (byte) 0x23, (byte) 0xca };

		this.kMac = new byte[] {

		(byte) 0x5d, (byte) 0xe2, (byte) 0x93, (byte) 0x9a, (byte) 0x1e,
				(byte) 0xa0, (byte) 0x3a, (byte) 0x93, (byte) 0x0b,
				(byte) 0x88, (byte) 0x20, (byte) 0x6d, (byte) 0x8f,
				(byte) 0x73, (byte) 0xe8, (byte) 0xa7 };

		this.ssc = new byte[] {

		(byte) 0xd3, (byte) 0x1a, (byte) 0xc8, (byte) 0xec, (byte) 0x7b,
				(byte) 0xa0, (byte) 0xfe, (byte) 0x73 };

		byte[] sigData_1 = new byte[] {

		(byte) 0x87, (byte) 0x82, (byte) 0x01, (byte) 0x09, (byte) 0x01,
				(byte) 0xe4, (byte) 0xe9, (byte) 0xca, (byte) 0x20,
				(byte) 0xeb, (byte) 0x99, (byte) 0x6f, (byte) 0x14,
				(byte) 0xf2, (byte) 0x19, (byte) 0xd0, (byte) 0x86,
				(byte) 0xb1, (byte) 0x31, (byte) 0x87, (byte) 0xd6,
				(byte) 0x40, (byte) 0xa7, (byte) 0xf7, (byte) 0xd9,
				(byte) 0xa9, (byte) 0x3b, (byte) 0x74, (byte) 0x36,
				(byte) 0x88, (byte) 0x70, (byte) 0xe7, (byte) 0x30,
				(byte) 0x0e, (byte) 0xc5, (byte) 0xdb, (byte) 0x20,
				(byte) 0x2e, (byte) 0x68, (byte) 0x3e, (byte) 0x85,
				(byte) 0x07, (byte) 0xae, (byte) 0x87, (byte) 0x1f,
				(byte) 0xf9, (byte) 0xfe, (byte) 0x6c, (byte) 0x10,
				(byte) 0xcd, (byte) 0x6b, (byte) 0x66, (byte) 0xe9,
				(byte) 0x42, (byte) 0xaf, (byte) 0xfa, (byte) 0xc1,
				(byte) 0x99, (byte) 0x6a, (byte) 0x20, (byte) 0x48,
				(byte) 0xe0, (byte) 0x56, (byte) 0x4d, (byte) 0x8e,
				(byte) 0xdd, (byte) 0x8b, (byte) 0x00, (byte) 0x57,
				(byte) 0xea, (byte) 0xa0, (byte) 0x57, (byte) 0x6e,
				(byte) 0xc8, (byte) 0x3e, (byte) 0x0b, (byte) 0x62,
				(byte) 0xeb, (byte) 0x07, (byte) 0xbe, (byte) 0xa6,
				(byte) 0xf7, (byte) 0x4a, (byte) 0x43, (byte) 0xa8,
				(byte) 0xd4, (byte) 0xa5, (byte) 0x18, (byte) 0xf5,
				(byte) 0x5d, (byte) 0xae, (byte) 0xba, (byte) 0x0b,
				(byte) 0xc6, (byte) 0xa8, (byte) 0x7f, (byte) 0x0e,
				(byte) 0x30, (byte) 0xfb, (byte) 0x1a, (byte) 0x75,
				(byte) 0xd4, (byte) 0x47, (byte) 0x21, (byte) 0x08,
				(byte) 0xec, (byte) 0xb0, (byte) 0x43, (byte) 0xdc,
				(byte) 0xd2, (byte) 0xf2, (byte) 0x2f, (byte) 0x85,
				(byte) 0xdb, (byte) 0x56, (byte) 0x40, (byte) 0xc8,
				(byte) 0x6b, (byte) 0x50, (byte) 0x0b, (byte) 0xf5,
				(byte) 0xa8, (byte) 0x19, (byte) 0xac, (byte) 0x73,
				(byte) 0x63, (byte) 0xd8, (byte) 0x79, (byte) 0xf9,
				(byte) 0x8b, (byte) 0x42, (byte) 0x95, (byte) 0x35,
				(byte) 0x9d, (byte) 0x2f, (byte) 0x7c, (byte) 0x04,
				(byte) 0xf1, (byte) 0x34, (byte) 0xfd, (byte) 0x28,
				(byte) 0xaf, (byte) 0x07, (byte) 0xb7, (byte) 0xe5,
				(byte) 0xb3, (byte) 0x27, (byte) 0xf3, (byte) 0x21,
				(byte) 0x4a, (byte) 0x81, (byte) 0x6f, (byte) 0xcd,
				(byte) 0xa4, (byte) 0x89, (byte) 0xb1, (byte) 0x7e,
				(byte) 0x0a, (byte) 0x0f, (byte) 0x9b, (byte) 0x9c,
				(byte) 0x3a, (byte) 0xf5, (byte) 0xb7, (byte) 0xce,
				(byte) 0xaa, (byte) 0x43, (byte) 0x11, (byte) 0xc7,
				(byte) 0xa6, (byte) 0x6e, (byte) 0xec, (byte) 0xe2,
				(byte) 0x10, (byte) 0xde, (byte) 0x9b, (byte) 0x47,
				(byte) 0x1c, (byte) 0x78, (byte) 0xae, (byte) 0xf8,
				(byte) 0x58, (byte) 0xcf, (byte) 0xea, (byte) 0xeb,
				(byte) 0x11, (byte) 0xda, (byte) 0x40, (byte) 0xba,
				(byte) 0x93, (byte) 0x2d, (byte) 0x49, (byte) 0x90,
				(byte) 0x0b, (byte) 0x73, (byte) 0xf6, (byte) 0xa1,
				(byte) 0x44, (byte) 0x51, (byte) 0xc0, (byte) 0xca,
				(byte) 0xe0, (byte) 0x4b, (byte) 0x08, (byte) 0x5b,
				(byte) 0x62, (byte) 0xaa, (byte) 0x19, (byte) 0x03,
				(byte) 0x7b, (byte) 0x62, (byte) 0xea, (byte) 0xf7,
				(byte) 0xb9, (byte) 0x7b, (byte) 0xf7, (byte) 0xe6,
				(byte) 0xdf, (byte) 0x54, (byte) 0xcb, (byte) 0x8a,
				(byte) 0x4e, (byte) 0x3d, (byte) 0xb5, (byte) 0xbc,
				(byte) 0x56, (byte) 0x81, (byte) 0x84, (byte) 0x67,
				(byte) 0xac, (byte) 0x75, (byte) 0x6b, (byte) 0x53,
				(byte) 0xba, (byte) 0x04, (byte) 0xb5, (byte) 0xdb,
				(byte) 0x0f, (byte) 0x55, (byte) 0xc1, (byte) 0xdb,
				(byte) 0x23, (byte) 0xf2, (byte) 0x28, (byte) 0x9f,
				(byte) 0x9e, (byte) 0x2c, (byte) 0x35, (byte) 0x96,
				(byte) 0x71, (byte) 0xba, (byte) 0x17, (byte) 0x4a,
				(byte) 0x9e, (byte) 0xcd, (byte) 0xe2, (byte) 0x61, (byte) 0x17 };

		byte[] sigData_2 = new byte[] {

		(byte) 0x78, (byte) 0x4d, (byte) 0xd3, (byte) 0x60, (byte) 0x9f,
				(byte) 0x32, (byte) 0xf0, (byte) 0x40, (byte) 0xda,
				(byte) 0xe1, (byte) 0xc1, (byte) 0x44, (byte) 0x7e,
				(byte) 0x99, (byte) 0x02, (byte) 0x90, (byte) 0x00,
				(byte) 0x8e, (byte) 0x04, (byte) 0x99, (byte) 0x9b,
				(byte) 0x1e, (byte) 0x4e, (byte) 0x90, (byte) 0x00

		};

		// ------------------

		System.out.println("Data_1 lenght: " + sigData_1.length);
		System.out.println("Data_2 lenght: " + sigData_2.length);

		byte[] allData = new byte[sigData_1.length - 2 + sigData_2.length - 2];
		System.arraycopy(sigData_1, 0, allData, 0, sigData_1.length - 2);
		System.arraycopy(sigData_2, 0, allData, sigData_1.length - 2,
				sigData_2.length - 2);

		System.out.println("AllData lenght: " + allData.length);
		System.out.println("AllData:");
		printByteArray(allData);

		verifyAndDecryptSecuredResponseAPDU(allData);

	}

	private byte[] executeGetResponse(CardChannel channel, byte sw2)
			throws CardException {

		// System.out.print("Run GET RESPONSE..");

		boolean done = false;
		ByteArrayOutputStream bof = new ByteArrayOutputStream();

		while (!done) {

			CommandAPDU command = new CommandAPDU(new byte[] { (byte) 0x00,
					(byte) 0xC0, (byte) 0x00, (byte) 0x00, (byte) sw2 });
			ResponseAPDU resp = channel.transmit(command);

			// System.out.println("Answer from card: " +
			// Integer.toHexString(resp.getSW()));
			// System.out.println("Number of bytes read: " +
			// resp.getData().length);
			//			
			// if(resp.getSW1() == (byte)0x61 || resp.getSW() == 0x9000) {
			//					
			// return resp.getData();
			// } else {
			//					
			// throw new CardException("Command GET RESPONSE failed. SW=" +
			// Integer.toHexString(resp.getSW()));
			// }

			// System.out.println(Integer.toHexString(resp.getSW()));

			try {
				bof.write(resp.getData());
			} catch (IOException e) {

				throw new CardException("Unable to read card response.", e);
			}

			if (resp.getSW1() == (byte) 0x61) {

				// more data to be read
				sw2 = (byte) resp.getSW2();
				continue;
			}

			if (resp.getSW() == 0x9000) {

				// all data read
				done = true;
			} else {

				throw new CardException(
						"An error has occured while fetching response from card: "
								+ Integer.toHexString(resp.getSW()));
			}

		}

		return bof.toByteArray();
	}

	public void testT0() throws CardException {

		CardChannel channel = setupCardChannel();

		byte[] apdu = new byte[] { (byte) 0x00, (byte) 0xA4, (byte) 0x00,
				(byte) 0x00, (byte) 0x02, (byte) 0x60, (byte) 0x20 };
		CommandAPDU command = new CommandAPDU(apdu);
		ResponseAPDU resp = channel.transmit(command);

		System.out.println("Response: " + Integer.toHexString(resp.getSW()));

	}

	private void executeSecureSelectCertificate(CardChannel channel)
			throws CardException {

		// select master file
		executeSecureSelectMasterFile(channel);

		// select 6081
		byte[] apdu = new byte[] {

		(byte) 0x00, (byte) 0xA4, (byte) 0x00, (byte) 0x00, (byte) 0x02,
				(byte) 0x60, (byte) 0x81 };

		executeSecureSelect(channel, apdu);

		// select 7004
		byte[] apdu2 = new byte[] {

		(byte) 0x00, (byte) 0xA4, (byte) 0x00, (byte) 0x00, (byte) 0x02,
				(byte) 0x70, (byte) 0x04 };

		byte[] fci = executeSecureSelect(channel, apdu2);

		 System.out.println("Obtained FCI:");
		 printByteArray(fci);

		byte sizeHi = fci[7];
		byte sizeLo = fci[8];

		byte[] data = executeSecureReadBinary(channel, sizeHi, sizeLo);

		int uncompressedDataLen = getUncompressedDataLength(data);

		byte[] compressedWithoutHeader = new byte[data.length - 8];
		System.arraycopy(data, 8, compressedWithoutHeader, 0,
				compressedWithoutHeader.length);

		byte[] decompressed = decompressData(compressedWithoutHeader,
				uncompressedDataLen);

		writeDataToFile(decompressed, "F:/cert_7004.cer");

		this.sigCert = decompressed;

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

	private void verifySignature() throws CardException {

		PublicKey pubKey = null;

		// IAIK:
		// X509Certificate certificate = new X509Certificate(this.sigCert);

		// SUN:
		X509Certificate certificate = createCertificate(this.sigCert);

		pubKey = certificate.getPublicKey();

		try {
			byte[] plain = rsaDecrypt(pubKey, this.sigVal);

			System.out.println("Deciphered Data:");
			printByteArray(plain);

		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

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

	private void executeSecurePerformSecurityOperation(CardChannel channel)
			throws CardException {

		System.out.print("Compute electronic signature on card..");

		byte[] testHashData = new byte[] {

		(byte) 0x00, (byte) 0x11, (byte) 0x22, (byte) 0x33, (byte) 0x44,
				(byte) 0x55, (byte) 0x66, (byte) 0x77, (byte) 0x88,
				(byte) 0x99, (byte) 0xAA, (byte) 0xBB, (byte) 0xCC,
				(byte) 0xDD, (byte) 0xEE, (byte) 0xFF, (byte) 0x01,
				(byte) 0x02, (byte) 0x03, (byte) 0x04 };

		byte[] padding = new byte[] {

		(byte) 0x30, (byte) 0x21, (byte) 0x30, (byte) 0x09, (byte) 0x06,
				(byte) 0x05, (byte) 0x2B, (byte) 0x0E, (byte) 0x03,
				(byte) 0x02, (byte) 0x1A, (byte) 0x05, (byte) 0x00,
				(byte) 0x04, (byte) 0x14 };

		byte[] apduHeader = new byte[] {

		(byte) 0x00, (byte) 0x2A, (byte) 0x9E, (byte) 0x9A, (byte) 0x23 };

		byte[] apdu = new byte[apduHeader.length + padding.length
				+ testHashData.length];
		System.arraycopy(apduHeader, 0, apdu, 0, apduHeader.length);
		System.arraycopy(padding, 0, apdu, apduHeader.length, padding.length);
		System.arraycopy(testHashData, 0, apdu, apduHeader.length
				+ padding.length, testHashData.length);

		byte[] securedAPDU = getSecuredAPDU(apdu);

		CommandAPDU command = new CommandAPDU(securedAPDU);
		ResponseAPDU resp = channel.transmit(command);

		// System.out.println("Response to SignCommand: " +
		// Integer.toHexString(resp.getSW()));

		byte[] signatureValue = executeGetResponse(channel, (byte) resp
				.getSW2());

		// System.out.println("Encrypted Signature Value:");
		// printByteArray(signatureValue);
		// System.out.println("Length of encrypted signature value: " +
		// signatureValue.length);

		// boolean done = false;
		// ByteArrayOutputStream bof = new ByteArrayOutputStream();

		// while(!done) {
		//		
		//			
		// try {
		// bof.write(resp.getData());
		// } catch (IOException e) {
		// throw new CardException("Error reading response from card.", e);
		// }
		//			
		// }

		byte[] decryptedSignatureValueWithSW = verifyAndDecryptSecuredResponseAPDU(signatureValue);

		int len = decryptedSignatureValueWithSW.length;
		if (decryptedSignatureValueWithSW[len - 2] == (byte) 0x90
				&& decryptedSignatureValueWithSW[len - 1] == (byte) 0x00) {

			System.out.println("OK");

			byte[] sigVal = new byte[decryptedSignatureValueWithSW.length - 2];
			System.arraycopy(decryptedSignatureValueWithSW, 0, sigVal, 0,
					decryptedSignatureValueWithSW.length - 2);

			System.out.println("Signature Value:");
			printByteArray(sigVal);

			this.sigVal = sigVal;

		} else {

			throw new CardException(
					"Error creating signature on card: "
							+ Integer
									.toHexString(decryptedSignatureValueWithSW[len - 2])
							+ " "
							+ Integer
									.toHexString(decryptedSignatureValueWithSW[len - 1]));
		}

	}

	private void executeSecureManageSecurityEnvironment(CardChannel channel)
			throws CardException {

		System.out.print("Manage Security Environment..");

		byte[] apdu = new byte[] {

		(byte) 0x00, (byte) 0x22, (byte) 0x41, (byte) 0xB6, (byte) 0x04,
				(byte) 0x84, (byte) 0x02, (byte) 0x01, (byte) 0x01 };

		byte[] securedAPDU = getSecuredAPDU(apdu);

		CommandAPDU command = new CommandAPDU(securedAPDU);
		ResponseAPDU resp = channel.transmit(command);

		// System.out.println("Obtained result: " +
		// Integer.toHexString(resp.getSW()));

		if (resp.getSW1() == (byte) 0x61) {

			byte[] response = executeGetResponse(channel, (byte) resp.getSW2());
			byte[] decryptedResponse = verifyAndDecryptSecuredResponseAPDU(response);

			// System.out.println("Decrypted Response from card:");
			// printByteArray(decryptedResponse);

			if (decryptedResponse.length == 2
					&& decryptedResponse[0] == (byte) 0x90
					&& decryptedResponse[1] == (byte) 0x00) {

				System.out.println("OK");
			} else {

				System.out.println("FAILED");
			}

		}

	}

	private void executeSecureSelectMasterFile(CardChannel channel)
			throws CardException {

		byte[] apdu = new byte[] {

		(byte) 0x00, (byte) 0xA4, (byte) 0x04, (byte) 0x00, (byte) 0x0B,
				(byte) 0x4D, (byte) 0x61, (byte) 0x73, (byte) 0x74,
				(byte) 0x65, (byte) 0x72, (byte) 0x2E, (byte) 0x46,
				(byte) 0x69, (byte) 0x6C, (byte) 0x65

		};

		executeSecureSelect(channel, apdu);

	}

	private void executeSecurePINVerify(CardChannel channel)
			throws CardException {

		// this.kEnc = new byte[] {
		//		
		// (byte) 0x59, (byte) 0x8f, (byte) 0x26, (byte) 0xe3, (byte) 0x6e,
		// (byte) 0x11, (byte) 0xa8, (byte) 0xec, (byte) 0x14,
		// (byte) 0xb8, (byte) 0x1e, (byte) 0x19, (byte) 0xbd,
		// (byte) 0xa2, (byte) 0x23, (byte) 0xca };
		//		
		// this.kMac = new byte[] {
		//		
		// (byte) 0x5d, (byte) 0xe2, (byte) 0x93, (byte) 0x9a, (byte) 0x1e,
		// (byte) 0xa0, (byte) 0x3a, (byte) 0x93, (byte) 0x0b,
		// (byte) 0x88, (byte) 0x20, (byte) 0x6d, (byte) 0x8f,
		// (byte) 0x73, (byte) 0xe8, (byte) 0xa7 };
		//		
		// this.ssc = new byte[] {
		//		
		// (byte) 0xd3, (byte) 0x1a, (byte) 0xc8, (byte) 0xec, (byte) 0x7b,
		// (byte) 0xa0, (byte) 0xfe, (byte) 0x6e };
		//		

		
		byte[] apdu = new byte[] {

				// PIN VERIFY (0 0 0 0 0 0 0 0)
				(byte) 0x00, (byte) 0x20, (byte) 0x00, (byte) 0x00,
				(byte) 0x08, (byte) 0x30, (byte) 0x30, (byte) 0x30,
				(byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x30

		// PIN VERIFY (C R Y P T O K I)
		// (byte)0x00, (byte)0x20, (byte)0x00, (byte)0x00, (byte)0x08,
		// (byte)0x43, (byte)0x52, (byte)0x59, (byte)0x50, (byte)0x54,
		// (byte)0x4f, (byte)0x4b, (byte)0x49
		};

		byte[] securedAPDU = getSecuredAPDU(apdu);

		// System.out.println("Secured PINVerify APDU:");
		// printByteArray(securedAPDU);

		System.out.print("Verifiying PIN..");

		CommandAPDU command = new CommandAPDU(securedAPDU);
		// System.out.println("Sending APDU: " + command.toString());
		ResponseAPDU resp = channel.transmit(command);

		// System.out.println("Received status word: " +
		// Integer.toHexString(resp.getSW()));

		if (resp.getSW1() == (byte) 0x61) {

			// System.out.println("Reading available bytes..");
			byte[] securedResponseData = executeGetResponse(channel,
					(byte) resp.getSW2());
			// printByteArray(securedResponseData);

			byte[] plainData = verifyAndDecryptSecuredResponseAPDU(securedResponseData);

			// System.out.println("Decrypted data:");
			// printByteArray(plainData);

			if (plainData.length == 2 && plainData[0] == (byte) 0x90
					&& plainData[1] == (byte) 0x00) {

				System.out.println("OK");
			} else {

				System.out.println("FAILED");
			}

		}
	}

	private void checkPIN(CardChannel channel) throws CardException {
		
		byte[] apdu = new byte[]{
				(byte)0x00, (byte)0x20, (byte)0x00, (byte)0x00
		};
		
		byte[] securedAPDU = secure4ByteAPDU(apdu);
		
		
		CommandAPDU command = new CommandAPDU(securedAPDU);
		ResponseAPDU resp = channel.transmit(command);
		
		System.out.println("Response: " + Integer.toHexString(resp.getSW()));
		
	}
	
	private byte[] readFromCard(CardChannel channel, byte offsetHi,
			byte offsetLo, byte numBytes) throws CardException {

		byte[] apdu = new byte[] {

		(byte) 0x00, (byte) 0xB0, offsetHi, offsetLo, numBytes };

		// System.out.println("Sent APDU (plain):");
		// printByteArray(apdu);

		byte[] securedAPDU = getSecuredAPDU(apdu);

		CommandAPDU command = new CommandAPDU(securedAPDU);

		ResponseAPDU resp = channel.transmit(command);
		// System.out.println("Answer to read binary: " +
		// Integer.toHexString(resp.getSW()));

		byte[] data = executeGetResponse(channel, (byte) resp.getSW2());

		byte[] decryptedResponse = verifyAndDecryptSecuredResponseAPDU(data);

		return decryptedResponse;

	}

	private byte[] executeSecureReadBinary(CardChannel channel, byte lengthHi,
			byte lengthLo) throws CardException {

		System.out.print("Executing secure read binary..");

		ByteArrayOutputStream bof = new ByteArrayOutputStream();

		int bytes2read = (lengthHi * 256) + lengthLo;
		// System.out.println("Bytes to read in total: " + bytes2read);
		int bytesRead = 0;

		boolean done = false;
		boolean forceExit = false;

		int offset = 0;
		int len = 0;

		while (!done) {

			// System.out.println("\nEntering loop..");
			// System.out.println("Bytes read so far: " + bytesRead);

			if (bytes2read - bytesRead > 0xef) {
				len = 0xef;
			} else {
				len = bytes2read - bytesRead;
			}

			// System.out.println("Bytes to read in this iteration: " + len);

			byte[] offsetBytes = intToHex(offset);

			// byte[] apdu = new byte[] {
			//					
			// (byte)0x00, (byte)0xB0, offsetBytes[0], offsetBytes[1], (byte)len
			// };
			//			
			// System.out.println("Sent APDU (plain):");
			// printByteArray(apdu);
			//			
			// byte[] securedAPDU = getSecuredAPDU(apdu);
			//			
			//			
			// CommandAPDU command = new CommandAPDU(securedAPDU);
			//
			// ResponseAPDU resp = channel.transmit(command);
			// System.out.println("Answer to read binary: " +
			// Integer.toHexString(resp.getSW()));
			//			
			// byte[] data = executeGetResponse(channel, (byte)resp.getSW2());
			//
			// byte[] decryptedResponse =
			// verifyAndDecryptSecuredResponseAPDU(data);

			byte[] decryptedResponse = readFromCard(channel, offsetBytes[0],
					offsetBytes[1], (byte) len);

			// System.out.println("Decrypted response:");
			// printByteArray(decryptedResponse);

			if (decryptedResponse.length == 2
					&& decryptedResponse[0] == (byte) 0x6C) {

				// handle case: card returns 6CXX (wrong number of bytes
				// requested)
				// we assume that his only happens in the final iteration

				decryptedResponse = readFromCard(channel, offsetBytes[0],
						offsetBytes[1], decryptedResponse[1]);

				// System.out.println("Decrypted response:");
				// printByteArray(decryptedResponse);

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

		System.out.println("OK");

		return bof.toByteArray();

	}

	private void executeSecureReadPrKDF(CardChannel channel)
			throws CardException {

		System.out.println("Reading PrKDF..");

		byte[] file = executeSecureReadFile(channel, new byte[] { (byte) 0x50,
				(byte) 0x15, (byte) 0x60, (byte) 0x01 });
		writeDataToFile(file, "f:/PrKDF.bin");

		System.out.println("Reading PrKDF successful.");

		// try {
		// ASN1 asn1 = new ASN1(file);
		//			
		// ASN1Object obj = asn1.toASN1Object();
		//			
		// System.out.println("Here!");
		//			
		// } catch (CodingException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

		getKeyIdFromASN1File(file);

	}

	private void getKeyIdFromASN1File(byte[] file) throws CardException {

		// split file in two records
		int record1Length = getRecordLength(file, 1);

		byte[] record1 = new byte[record1Length];
		byte[] record2 = new byte[file.length - record1.length];

		System.arraycopy(file, 0, record1, 0, record1.length);
		System.arraycopy(file, record1.length, record2, 0, record2.length);

		try {
			ASN1 asn1_1 = new ASN1(record1);
			ASN1 asn1_2 = new ASN1(record2);

			byte keyId = (byte) 0x00;

			if (asn1_1.getElementAt(0).getElementAt(0).gvString()
					.equalsIgnoreCase("KprivFirmaDigital")) {

				byte[] data = asn1_1.getElementAt(2).gvByteArray();

				keyId = data[10];
			}

			else if (asn1_2.getElementAt(0).getElementAt(0).gvString()
					.equalsIgnoreCase("KprivFirmaDigital")) {

				byte[] data = asn1_2.getElementAt(2).gvByteArray();

				keyId = data[10];
			}

			System.out.println("Found keyId: " + keyId);

		} catch (Exception e) {

			throw new CardException("Error getting ASN1 data.", e);
		}
	}

	private byte[] getCertIdFromASN1File(byte[] file) throws CardException {

		int record1Length = getRecordLength(file, 1);

		// split file in two records
		byte[] record1 = new byte[record1Length];
		byte[] record2 = new byte[file.length - record1.length];

		System.arraycopy(file, 0, record1, 0, record1.length);
		System.arraycopy(file, record1.length, record2, 0, record2.length);

		byte[] certId = null;

		try {
			ASN1 asn1_1 = new ASN1(record1);
			ASN1 asn1_2 = new ASN1(record2);

			if (asn1_1.getElementAt(0).getElementAt(0).gvString()
					.equalsIgnoreCase("CertFirmaDigital")) {

				certId = retrieveCertId(asn1_1.getElementAt(2).gvByteArray());
			}

			if (asn1_2.getElementAt(0).getElementAt(0).gvString()
					.equalsIgnoreCase("CertFirmaDigital")) {

				certId = retrieveCertId(asn1_2.getElementAt(2).gvByteArray());
			}

			// byte[] data = asn1_1.getElementAt(2).gvByteArray();
			//			
			// System.out.println("asn1data:");
			// printByteArray(data);
			// ASN1 contextSpecific = getASN1WithinContextSpecific(data);
			//			
			// byte[] id =
			// contextSpecific.getElementAt(0).getElementAt(0).gvByteArray();
			// System.out.println("CertID:");
			// printByteArray(id);

			//
			// else
			// if(asn1_2.getElementAt(0).getElementAt(0).gvString().equalsIgnoreCase("KprivFirmaDigital"))
			// {
			//				
			// byte[] data = asn1_2.getElementAt(2).gvByteArray();
			//				
			// keyId = data[10];
			// }
			//			
			// System.out.println("Found keyId: " + keyId);

		} catch (Exception e) {

			throw new CardException("Error getting ASN1 data.", e);
		}

		System.out.println("Found certId:");
		printByteArray(certId);

		return certId;
	}

	private byte[] retrieveCertId(byte[] data) throws CardException {

		ASN1 contextSpecific = getASN1WithinContextSpecific(data);
		try {
			return contextSpecific.getElementAt(0).getElementAt(0)
					.gvByteArray();
		} catch (IOException e) {
			throw new CardException(
					"Error retrieving certificate ID from ASN1 data.", e);
		}
	}

	private void executeSecureReadCDF(CardChannel channel) throws CardException {

		System.out.println("Reading CDF file..");

		byte[] file = executeSecureReadFile(channel, new byte[] { (byte) 0x50,
				(byte) 0x15, (byte) 0x60, (byte) 0x04 });

		writeDataToFile(file, "f:/CDF.bin");

		getCertIdFromASN1File(file);

		// NEW
//		try {
//		
//	      EFObjectDirectory ef_od = new EFObjectDirectory(new byte[]{(byte)0x50, (byte)0x15});
//	      ef_od.selectAndRead(channel);
//
//	      CIOCertificateDirectory ef_cd = new CIOCertificateDirectory(ef_od.getEf_cd());
//	      ef_cd.selectAndRead(channel);
//
//	        byte[] ef_qcert = null;
//	        for (CIOCertificate cioCertificate : ef_cd.getCIOs()) {
//	            String label = cioCertificate.getLabel();
//	            //"TEST LLV APO 2s Liechtenstein Post Qualified CA ID"
//	            if (label != null && label.toLowerCase()
//	                    .contains("liechtenstein post qualified ca id")) {
//	                ef_qcert = cioCertificate.getEfidOrPath();
//	            }
//	        }		
//		
//		} catch(SignatureCardException e) {
//			
//			System.out.println("Error getting CDF.");
//			e.printStackTrace();
//		}
//				
//		 catch (IOException e) {
//				System.out.println("Error getting CDF.");
//				e.printStackTrace();
//		}
		// END NEW		
		 
		 
		System.out.println("Reading CDF file successful.");
	}

	private byte[] executeSecureReadFile(CardChannel channel, byte[] path)
			throws CardException {

		System.out.println("Executing secure read File command..");

		executeSecureSelectMasterFile(channel);

		// Select DF
		byte[] apdu_2 = new byte[] {

		(byte) 0x00, // CLA
				(byte) 0xA4, // INS
				(byte) 0x00, // P1
				(byte) 0x00, // P2
				(byte) 0x02, // Lc
				path[0], path[1] };

		executeSecureSelect(channel, apdu_2);

		// Select EF
		byte[] apdu_3 = new byte[] {

		(byte) 0x00, // CLA
				(byte) 0xA4, // INS
				(byte) 0x00, // P1
				(byte) 0x00, // P2
				(byte) 0x02, // Lc
				path[2], path[3] };

		byte[] fci = executeSecureSelect(channel, apdu_3);

		System.out.println("FCI of File:");
		printByteArray(fci);

		byte[] file = executeSecureReadBinary(channel, fci[7], fci[8]);
		writeDataToFile(file, "f:/asn1.bin");
		return file;

	}

	private byte[] executeSecureSelect(CardChannel channel, byte[] apdu)
			throws CardException {

		System.out.print("Executing secure select command..");

		// System.out.println("kEnc:");
		// printByteArray(this.kEnc);

		// this.kEnc[0] = (byte)(this.kEnc[0]+ (byte)0x01);

		// System.out.println("kEnc after modification:");
		// printByteArray(this.kEnc);

		// byte[] apdu = new byte[] {
		//
		// // Select by name "Master.File"
		// (byte) 0x00, (byte) 0xA4, (byte) 0x04, (byte) 0x00, (byte) 0x0B,
		// (byte) 0x4D, (byte) 0x61, (byte) 0x73, (byte) 0x74,
		// (byte) 0x65, (byte) 0x72, (byte) 0x2E, (byte) 0x46,
		// (byte) 0x69, (byte) 0x6C, (byte) 0x65

		// (byte) 0x00, (byte)0xA4, (byte)0x04, (byte)0x00, (byte)0x02,
		// (byte)0x3F, (byte)0x00

		// (byte)0x00, (byte)0xA4, (byte)0x04, (byte)0x00, (byte)0x0C,
		// (byte)0xA0, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x63,
		// (byte)0x50, (byte)0x4B, (byte)0x43, (byte)0x53, (byte)0x2D,
		// (byte)0x31, (byte)0x35

		// (byte)0x00, (byte)0xC0, (byte)0x00, (byte)0x00, (byte)0x05

		// PIN VERIFY
		// (byte)0x00, (byte)0x20, (byte)0x00, (byte)0x00, (byte)0x08,
		// (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
		// (byte)0x00, (byte)0x00, (byte)0x00

		// MSE
		// (byte)0x00, (byte)0x22, (byte)0x41, (byte)0xB6, (byte)0x04,
		// (byte)0x84, (byte)0x02, (byte)0x01, (byte)0x01

		// SIGNATURE
		// (byte)0x00, (byte)0x2A, (byte)0x9E, (byte)0x9A, (byte)0x23,
		// (byte)0x30, (byte)0x21, (byte)0x30, (byte)0x09,
		// (byte)0x06, (byte)0x05, (byte)0x2B, (byte)0x0E, (byte)0x03,
		// (byte)0x02, (byte)0x1A, (byte)0x05, (byte)0x00,
		// (byte)0x04, (byte)0x14, (byte)0x83, (byte)0x9B, (byte)0x54,
		// (byte)0x3F, (byte)0xB0, (byte)0x9D, (byte)0x20,
		// (byte)0xC8, (byte)0x03, (byte)0xB4, (byte)0x6E, (byte)0x6F,
		// (byte)0x8F, (byte)0x07, (byte)0x47, (byte)0x24,
		// (byte)0x49, (byte)0x51, (byte)0x82, (byte)0x2F
		// };

		byte[] securedApdu = getSecuredAPDU(apdu);

		// System.out.println("Secured APDU:");
		// printByteArray(securedApdu);

		CommandAPDU command = new CommandAPDU(securedApdu);
		// System.out.println("Sending APDU: " + command.toString());
		// printByteArray(securedApdu);
		ResponseAPDU resp = channel.transmit(command);

		// System.out.println("Response: " + Integer.toHexString(resp.getSW()));
		// printByteArray(resp.getData());

		byte[] data = executeGetResponse(channel, (byte) resp.getSW2());

		// System.out.println("Encrypted response from card:");
		// printByteArray(data);

		byte[] response = verifyAndDecryptSecuredResponseAPDU(data);

		// System.out.println("Decrypted response from card:");
		// printByteArray(response);

		if (response.length >= 2
				&& response[response.length - 2] == (byte) 0x90
				&& response[response.length - 1] == (byte) 0x00) {

			System.out.println("OK");
		} else {

			System.out.println("FAILED");
		}

		byte[] fci = new byte[response.length - 2];
		System.arraycopy(response, 0, fci, 0, response.length - 2);

		return fci;

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

	private void calculateChannelKey() throws CardException {

		// this.kifd = new byte[] {
		//				
		// (byte)0xcd, (byte)0x97, (byte)0xdb, (byte)0xf2, (byte)0x23,
		// (byte)0x57, (byte)0x60, (byte)0xd8, (byte)0xb3, (byte)0xe9,
		// (byte)0xc4, (byte)0xd7, (byte)0xe9, (byte)0xa6, (byte)0x91,
		// (byte)0xfb
		// , (byte)0x12, (byte)0xcd, (byte)0x20, (byte)0x7c, (byte)0x42,
		// (byte)0x66, (byte)0x0d, (byte)0xf9, (byte)0xc6, (byte)0x93,
		// (byte)0x57, (byte)0xba, (byte)0x7b, (byte)0x25, (byte)0xd0,
		// (byte)0xfc
		// };
		//		
		// this.kicc = new byte[] {
		//			
		// (byte)0x88, (byte)0x6b, (byte)0x2b, (byte)0x01, (byte)0x5c,
		// (byte)0xe1, (byte)0xc9, (byte)0x52, (byte)0x9d, (byte)0x94,
		// (byte)0x63, (byte)0x69, (byte)0x5f, (byte)0xab, (byte)0xd1,
		// (byte)0xa4
		// , (byte)0xa4, (byte)0xb8, (byte)0x5e, (byte)0x0a, (byte)0x1c,
		// (byte)0xcd, (byte)0x4f, (byte)0x55, (byte)0x29, (byte)0x2c,
		// (byte)0x20, (byte)0xe2, (byte)0x1e, (byte)0x95, (byte)0x4d,
		// (byte)0x31
		// };
		//		
		// this.rndIcc = new byte[] {
		//		
		// (byte)0xea, (byte)0xef, (byte)0xa8, (byte)0xfb, (byte)0xd3,
		// (byte)0x1a, (byte)0xc8, (byte)0xec
		// };
		//		
		// this.rndIfd = new byte[] {
		//				
		// (byte)0x23, (byte)0x1D, (byte)0xac, (byte)0x73, (byte)0x7B,
		// (byte)0xa0, (byte)0xfe, (byte)0x6e
		// };

		System.out.print("Generating channel keys..");

		if (this.kicc == null || this.kifd == null) {

			throw new CardException("Required key data not available.");
		}

		if (this.kicc.length != this.kifd.length) {

			throw new CardException("Required key data is invalid.");
		}

		byte[] kifdicc = new byte[this.kicc.length];

		for (int i = 0; i < kifdicc.length; i++) {

			kifdicc[i] = (byte) (this.kicc[i] ^ this.kifd[i]);
		}

		byte[] kEncHashData = new byte[kifdicc.length
				+ KENC_COMPUTATION_TAIL.length];
		byte[] kMacHashData = new byte[kifdicc.length
				+ KMAC_COMPUTATION_TAIL.length];

		for (int i = 0; i < kifdicc.length; i++) {

			kEncHashData[i] = kifdicc[i];
			kMacHashData[i] = kifdicc[i];
		}

		for (int i = 0; i < KENC_COMPUTATION_TAIL.length; i++) {

			kEncHashData[i + kifdicc.length] = KENC_COMPUTATION_TAIL[i];
		}

		for (int i = 0; i < KMAC_COMPUTATION_TAIL.length; i++) {

			kMacHashData[i + kifdicc.length] = KMAC_COMPUTATION_TAIL[i];
		}

		byte[] hashEnc = computeHash(kEncHashData);
		byte[] hashMac = computeHash(kMacHashData);

		this.kEnc = Arrays.copyOfRange(hashEnc, 0, 16);
		this.kMac = Arrays.copyOfRange(hashMac, 0, 16);

		// printByteArray(kEnc);
		// printByteArray(kMac);

		// compute sequence counter SSC
		if (this.rndIcc == null || this.rndIfd == null
				|| this.rndIcc.length < 4 || this.rndIfd.length < 4) {

			throw new CardException("Data required to compute SSC not valid.");
		}

		this.ssc = new byte[8];

		for (int i = 0; i < 4; i++) {

			this.ssc[i] = this.rndIcc[this.rndIcc.length - 4 + i];
		}

		for (int i = 0; i < 4; i++) {

			this.ssc[i + 4] = this.rndIfd[this.rndIfd.length - 4 + i];
		}

		// printByteArray(ssc);
		System.out.println("done.");

		// System.out.println("Kenc:");
		// printByteArray(this.kEnc);
		//		
		// System.out.println("Kmac:");
		// printByteArray(this.kMac);
		//		
		// System.out.println("SSC:");
		// printByteArray(this.ssc);

	}

	private byte[] verifyAndDecryptSecuredResponseAPDU(byte[] securedAPDU)
			throws CardException {

		// FOR TEST PURPOSES
		// this.kEnc = new byte[] {
		//
		// (byte) 0x59, (byte) 0x8f, (byte) 0x26, (byte) 0xe3, (byte) 0x6e,
		// (byte) 0x11, (byte) 0xa8, (byte) 0xec, (byte) 0x14,
		// (byte) 0xb8, (byte) 0x1e, (byte) 0x19, (byte) 0xbd,
		// (byte) 0xa2, (byte) 0x23, (byte) 0xca };
		//
		// this.kMac = new byte[] {
		//
		// (byte) 0x5d, (byte) 0xe2, (byte) 0x93, (byte) 0x9a, (byte) 0x1e,
		// (byte) 0xa0, (byte) 0x3a, (byte) 0x93, (byte) 0x0b,
		// (byte) 0x88, (byte) 0x20, (byte) 0x6d, (byte) 0x8f,
		// (byte) 0x73, (byte) 0xe8, (byte) 0xa7 };
		//
		// this.ssc = new byte[] {
		//
		// (byte) 0xd3, (byte) 0x1a, (byte) 0xc8, (byte) 0xec, (byte) 0x7b,
		// (byte) 0xa0, (byte) 0xfe, (byte) 0x75 };
		//		
		// securedAPDU = new byte[] {
		// (byte)0x87, (byte) 0x21, (byte) 0x01, (byte) 0xbb, (byte) 0x10,
		// (byte) 0xc8, (byte) 0x91, (byte) 0x5f, (byte) 0x0d, (byte) 0xdb,
		// (byte) 0x63, (byte) 0x0c, (byte) 0xf3, (byte) 0x8e, (byte) 0xd7,
		// (byte) 0x99, (byte) 0xa2, (byte) 0x02, (byte) 0x64, (byte) 0x7c,
		// (byte) 0xa4, (byte) 0x74, (byte) 0x49, (byte) 0xd2
		// , (byte) 0x79, (byte) 0x13, (byte) 0x9f, (byte) 0xc2, (byte) 0x26,
		// (byte) 0xd2, (byte) 0x5c, (byte) 0x20, (byte) 0x76, (byte) 0x56,
		// (byte) 0x45, (byte) 0x99, (byte) 0x02, (byte) 0x90, (byte) 0x00,
		// (byte) 0x8e, (byte) 0x04, (byte) 0x9a, (byte) 0xa7, (byte) 0x77,
		// (byte) 0x07
		// };

		// System.out.println("APDU to verify:");
		// printByteArray(securedAPDU);

		byte[] data = new byte[securedAPDU.length - 10];
		byte[] commandResponse = new byte[4];
		byte[] obtainedMac = new byte[4];

		System.arraycopy(securedAPDU, 0, data, 0, data.length);
		System.arraycopy(securedAPDU, data.length, commandResponse, 0,
				commandResponse.length);
		System.arraycopy(securedAPDU, data.length + commandResponse.length + 2,
				obtainedMac, 0, obtainedMac.length);

		// System.out.println("Extracted data:");
		// printByteArray(data);
		// System.out.println("Extracted command response:");
		// printByteArray(commandResponse);
		// System.out.println("Extracted MAC:");
		// printByteArray(obtainedMac);

		byte[] macData = new byte[data.length + commandResponse.length];
		System.arraycopy(data, 0, macData, 0, data.length);
		System.arraycopy(commandResponse, 0, macData, data.length,
				commandResponse.length);

		byte[] paddedMacData = applyPadding(8, macData);

		// System.out.println("padded macData: ");
		// printByteArray(paddedMacData);

		incrementSSC();

		byte[] mac = calculateMAC(paddedMacData, this.kMac, this.ssc);

		// System.out.println("Computed MAC:");
		// printByteArray(mac);
		//		
		// System.out.println("Obtained MAC:");
		// printByteArray(obtainedMac);

		if (!Arrays.equals(mac, obtainedMac)) {

			throw new CardException("Unable to verify MAC of response APDU.");
		} else {

			// System.out.println("MAC OK!");
		}

		if (data.length > 0) {

			byte[] data2decrypt = new byte[data.length
					- getCutOffLength(data, 8)];
			System.arraycopy(data, getCutOffLength(data, 8), data2decrypt, 0,
					data2decrypt.length);

			// System.out.println("Data to be decrypted:");
			// printByteArray(data2decrypt);

			byte[] plainData = null;

			try {
				plainData = perform3DESCipherOperation(data2decrypt, this.kEnc,
						Cipher.DECRYPT_MODE);
			} catch (Exception e) {
				throw new CardException("Unable to decrypt data.", e);
			}

			// System.out.println("Plain data:");
			// printByteArray(plainData);

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

	// private int getLengthValue(byte[] data, int blockLen) throws
	// CardException {
	//		
	//		
	// int len = data.length;
	//		
	// if( (len-3) % blockLen == 0) {
	//			
	// // length of length tag is probably 1 -> check
	// if(data[2] == (byte)0x01) {
	//				
	// return 1;
	// }
	// }
	//		
	// if( (len-4) % blockLen == 0) {
	//			
	// // length of length tag is probably 2 -> check
	// if(data[3] == (byte)0x01) {
	//				
	// return 2;
	// }
	//			
	// }
	//			
	// throw new CardException("Unable to determine length of length tag");
	//
	// }

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

	private byte[] getSecuredAPDU(byte[] apdu) throws CardException {

		// FOR TEST PURPOSES
		// this.kEnc = new byte[] {
		//
		// (byte) 0x59, (byte) 0x8f, (byte) 0x26, (byte) 0xe3, (byte) 0x6e,
		// (byte) 0x11, (byte) 0xa8, (byte) 0xec, (byte) 0x14,
		// (byte) 0xb8, (byte) 0x1e, (byte) 0x19, (byte) 0xbd,
		// (byte) 0xa2, (byte) 0x23, (byte) 0xca };
		//
		// this.kMac = new byte[] {
		//
		// (byte) 0x5d, (byte) 0xe2, (byte) 0x93, (byte) 0x9a, (byte) 0x1e,
		// (byte) 0xa0, (byte) 0x3a, (byte) 0x93, (byte) 0x0b,
		// (byte) 0x88, (byte) 0x20, (byte) 0x6d, (byte) 0x8f,
		// (byte) 0x73, (byte) 0xe8, (byte) 0xa7 };
		//
		// this.ssc = new byte[] {
		//
		// (byte) 0xd3, (byte) 0x1a, (byte) 0xc8, (byte) 0xec, (byte) 0x7b,
		// (byte) 0xa0, (byte) 0xfe, (byte) 0x6e };

		if (apdu == null || apdu.length < 4) {

			throw new CardException("Invalid APDU to secure.");
		}

		if (apdu.length < 6) {

			// TODO: Handle cases: (a) CLA INS P1 P2, (b) CLA INS P1 P2 LE

			if (apdu.length == 5) {

				// handle case CLA INS P1 P2 LE

				byte encCLA = (byte) (apdu[0] | (byte) 0x0C);
				byte[] encHeader = new byte[] { encCLA, apdu[1], apdu[2],
						apdu[3] };
				byte[] paddedHeader = applyPadding(8, encHeader);

				byte[] leField = new byte[3];
				leField[0] = (byte) 0x97;
				leField[1] = (byte) 0x01;
				leField[2] = apdu[4];

				byte[] macData = new byte[paddedHeader.length + leField.length];
				System.arraycopy(paddedHeader, 0, macData, 0,
						paddedHeader.length);
				System.arraycopy(leField, 0, macData, paddedHeader.length,
						leField.length);

				byte[] paddedMacData = applyPadding(8, macData);

				incrementSSC();

				// System.out.println("Calculating MAC based on following data:");
				// printByteArray(paddedMacData);

				byte[] mac = calculateMAC(paddedMacData, kMac, this.ssc);

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

			return null;
		}

		// case data field available

		byte cla = apdu[0];
		byte ins = apdu[1];
		byte p1 = apdu[2];
		byte p2 = apdu[3];
		byte lc = apdu[4];

		byte[] data = new byte[lc];
		System.arraycopy(apdu, 5, data, 0, lc);

		byte[] paddedData = applyPadding(8, data);

		byte[] encrypted = null;

		try {

			encrypted = perform3DESCipherOperation(paddedData, kEnc,
					Cipher.ENCRYPT_MODE);
			// printByteArray(encrypted);

		} catch (Exception e) {

			throw new CardException("Error encrypting APDU.", e);
		}

		byte[] encapsulated = new byte[encrypted.length + 3];
		encapsulated[0] = (byte) 0x87;
		encapsulated[1] = (byte) (encrypted.length + 1);
		encapsulated[2] = (byte) 0x01;
		System.arraycopy(encrypted, 0, encapsulated, 3, encrypted.length);

		// printByteArray(encapsulated);

		// calculate MAC

		// prepare CLA byte

		byte encCLA = (byte) (cla | (byte) 0x0C);
		byte[] encHeader = new byte[] { encCLA, ins, p1, p2 };
		byte[] paddedHeader = applyPadding(8, encHeader);

		byte[] headerAndData = new byte[paddedHeader.length
				+ encapsulated.length];
		System
				.arraycopy(paddedHeader, 0, headerAndData, 0,
						paddedHeader.length);
		System.arraycopy(encapsulated, 0, headerAndData, paddedHeader.length,
				encapsulated.length);

		byte[] paddedHeaderAndData = applyPadding(8, headerAndData);

		// printByteArray(paddedHeaderAndData);

		incrementSSC();
		// printByteArray(this.ssc);

		byte[] mac = calculateMAC(paddedHeaderAndData, kMac, this.ssc);
		// printByteArray(mac);

		byte[] encapsulatedMac = new byte[mac.length + 2];
		encapsulatedMac[0] = (byte) 0x8E;
		encapsulatedMac[1] = (byte) mac.length;
		System.arraycopy(mac, 0, encapsulatedMac, 2, mac.length);

		// printByteArray(encapsulatedMac);

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

		// printByteArray(completeMessage);

		return completeMessage;
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

	public byte[] calculateMAC(byte[] data, byte[] key, byte[] ssc)
			throws CardException {

		SecretKeySpec desSingleKey = new SecretKeySpec(key, 0, 8, "DES");
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

		// printByteArray(result);

		byte[] dataBlock = new byte[8];

		for (int i = 0; i < dataLen - 8; i = i + 8) {

			System.arraycopy(data, i, dataBlock, 0, 8);
			byte[] input = xorByteArrays(result, dataBlock);

			try {
				result = singleDesCipher.doFinal(input);
			} catch (Exception e) {
				throw new CardException("Error applying DES cipher.", e);
			}

			// printByteArray(result);

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

		System.arraycopy(data, data.length - 8, dataBlock, 0, 8);
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

		// should never happen
		return null;

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

	private byte[] executeGetChipInfo(CardChannel channel) throws CardException {

		// get chip info - read out card serial number
		System.out.print("Getting chip info..");
		CommandAPDU command = new CommandAPDU(0x90, 0xB8, 0x00, 0x00, 0x07);
		ResponseAPDU resp = channel.transmit(command);

		if (resp.getSW() == 0x9000) {
			System.out.println("done.");
		} else {
			System.out.println("error: " + Integer.toHexString(resp.getSW()));
		}

		System.out.println("Read chip info:");
		printByteArray(resp.getData());

		return resp.getData();
	}

	private byte[] executeReadCertificate(CardChannel channel, byte[] id)
			throws CardException {

		// certificate
		System.out.print("Reading certificate..");

		byte[] apdu = new byte[] { (byte) 0x00, (byte) 0xA4, (byte) 0x00,
				(byte) 0x00, (byte) 0x02, id[0], id[1] };
		CommandAPDU command = new CommandAPDU(apdu);
		ResponseAPDU resp = channel.transmit(command);

		byte certLenHigh = 0x00;
		byte certLenLow = 0x00;

		if (resp.getSW() == 0x9000) {
			// Selection successful - FCI is already in response
			byte[] fci = resp.getData();
			certLenHigh = fci[7];
			certLenLow = fci[8];

		} else if (resp.getSW1() == 0x61) {
			// Selection successful - FCI ready to be read
			// int fciLen = resp.getSW2();
			// command = new CommandAPDU(new byte[] { (byte) 0x00, (byte) 0xC0,
			// (byte) 0x00, (byte) 0x00, (byte) fciLen });
			// resp = channel.transmit(command);
			//
			// byte[] fci = resp.getData();
			byte[] fci = executeGetResponse(channel, (byte) resp.getSW2());

			certLenHigh = fci[7];
			certLenLow = fci[8];

		} else {

			throw new CardException("Unexpected response from card: "
					+ Integer.toHexString(resp.getSW()));
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

			command = new CommandAPDU(0x00, 0xB0, offsetBytes[0],
					offsetBytes[1], len);
			resp = channel.transmit(command);

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

		System.out.println("done.");

		return bof.toByteArray();
	}

	private void verifyCertificates() throws CardException {

		RSAPublicKey rootPubKey = createRSAPublicKey(ROOT_CA_MODULO,
				ROOT_CA_PUBEXP);

		X509Certificate intermediate = createCertificate(intermediateCert);
		X509Certificate component = createCertificate(componentCert);

		try {
			component.verify(intermediate.getPublicKey());
			intermediate.verify(rootPubKey);
		} catch (Exception e) {

			System.out.println("Certificate verification failed.");
			e.printStackTrace();
		}
	}

	public void establishSecureChannel(CardChannel channel)
			throws CardException {

		// get card serial number
		this.snIcc = executeGetChipInfo(channel);
		System.out.println("Card Serial Number");
		printByteArray(snIcc);

		// get card certificates
		this.intermediateCert = executeReadCertificate(channel, new byte[] {
				(byte) 0x60, (byte) 0x20 });
		printByteArray(this.intermediateCert);
		writeDataToFile(intermediateCert,
				"f:/secure_channel_card_intermediate.cer");
		this.componentCert = executeReadCertificate(channel, new byte[] {
				(byte) 0x60, (byte) 0x1F });
		printByteArray(this.componentCert);
		writeDataToFile(componentCert, "f:/secure_channel_card_component.cer");

		// TODO: Verify certificate chain
		verifyCertificates();

		// load certs and select keys
		loadCertsAndSelectKeys(channel);

		// internal authentication
		internalAuthentication(channel);

		// external authentication
		externalAuthentication(channel);

		// derive channel key
		calculateChannelKey();

		// // test secure channel
		// executeSecureSelect(channel);
		// executeSecureSelect(channel);
		// executeSecureSelect(channel);
		// executeSecureSelect(channel);
		// executeSecureSelect(channel);

		// VERIFY PIN
		executeSecurePINVerify(channel);
		checkPIN(channel);

//		// GET PrKDF
//		executeSecureReadPrKDF(channel);
//
//		// Manage Security Environment
//		executeSecureManageSecurityEnvironment(channel);
//
//		// Create signature
//		executeSecurePerformSecurityOperation(channel);
//
//		// GET CDF
//		executeSecureReadCDF(channel);
//
//		// Select certificate
//		executeSecureSelectCertificate(channel);
//
//		// Verify signature
//		verifySignature();

	}

	private void internalAuthentication(CardChannel channel)
			throws CardException {

		byte[] randomBytes = getRandomBytes(8);
		byte[] challengeData = new byte[randomBytes.length + RANDOM_TAIL.length];

		this.rndIfd = randomBytes;

		for (int i = 0; i < randomBytes.length; i++) {

			challengeData[i] = randomBytes[i];
		}
		for (int i = 0; i < RANDOM_TAIL.length; i++) {

			challengeData[i + randomBytes.length] = RANDOM_TAIL[i];
		}

		// System.out.println("Generated terminal challenge:");
		// printByteArray(challengeData);

		// send challenge to card
		CommandAPDU command_6 = new CommandAPDU((byte) 0x00, (byte) 0x88,
				(byte) 0x00, (byte) 0x00, challengeData);
		ResponseAPDU resp_6 = channel.transmit(command_6);

		// System.out.println("Challenge:");
		// printByteArray(challengeData);

		System.out.println("Response to terminal challenge: "
				+ Integer.toHexString(resp_6.getSW()));

		byte[] data = null;

		if (resp_6.getSW() == 0x9000) {

			data = resp_6.getData();

		} else if (resp_6.getSW1() == 0x61) {

			data = executeGetResponse(channel, (byte) resp_6.getSW2());

		} else {

			System.out.println("An error occured - cancel.");
			throw new CardException("Internal authentication failed - "
					+ Integer.toHexString(resp_6.getSW()));
		}

		// verify response
		boolean ok = verifyInternalAuthenticationResponse(randomBytes, data);

		System.out.println("Internal Authentiction succeeded: " + ok);

		if (!ok) {

			System.out.println("Internal Authentiction failed - cancel.");
			throw new CardException("Internal authentication failed");
		}

	}

	private void loadCertsAndSelectKeys(CardChannel channel)
			throws CardException {

		CommandAPDU command_1 = new CommandAPDU((byte) 0x00, (byte) 0x22,
				(byte) 0x81, (byte) 0xB6, new byte[] { (byte) 0x83,
						(byte) 0x02, (byte) 0x02, (byte) 0x0F });
		ResponseAPDU resp_1 = channel.transmit(command_1);

		System.out.println("Response: " + Integer.toHexString(resp_1.getSW()));

		CommandAPDU command_2 = new CommandAPDU((byte) 0x00, (byte) 0x2A,
				(byte) 0x00, (byte) 0xAE, C_CV_CA);
		ResponseAPDU resp_2 = channel.transmit(command_2);

		System.out.println("Response: " + Integer.toHexString(resp_2.getSW()));

		CommandAPDU command_3 = new CommandAPDU((byte) 0x00, (byte) 0x22,
				(byte) 0x81, (byte) 0xB6, CHR);
		ResponseAPDU resp_3 = channel.transmit(command_3);

		System.out.println("Response: " + Integer.toHexString(resp_3.getSW()));

		CommandAPDU command_4 = new CommandAPDU((byte) 0x00, (byte) 0x2A,
				(byte) 0x00, (byte) 0xAE, C_CV_IFD);
		ResponseAPDU resp_4 = channel.transmit(command_4);

		System.out.println("Response: " + Integer.toHexString(resp_4.getSW()));

		CommandAPDU command_5 = new CommandAPDU((byte) 0x00, (byte) 0x22,
				(byte) 0xC1, (byte) 0xA4, KEY_SELECTOR);
		ResponseAPDU resp_5 = channel.transmit(command_5);

		System.out.println("Response: " + Integer.toHexString(resp_5.getSW()));

	}

	private byte[] computeHash(byte[] input) throws CardException {

		byte[] digest = null;

		try {
			MessageDigest sha = MessageDigest.getInstance("SHA");

			sha.update(input);
			digest = sha.digest();

		} catch (NoSuchAlgorithmException e) {
			throw new CardException("Error computing hash.", e);
		}

		return digest;
	}

	private static BigInteger createUnsignedBigInteger(byte[] data) {

		byte[] unsigned = new byte[data.length + 1];
		unsigned[0] = (byte) 0x00;
		System.arraycopy(data, 0, unsigned, 1, data.length);

		return new BigInteger(unsigned);

	}

	private void externalAuthentication(CardChannel channel)
			throws CardException {

		System.out.println("Starting external authentication..");

		// request card challenge
		// System.out.print("Requesting card challenge..");
		CommandAPDU command_7 = new CommandAPDU((byte) 0x00, (byte) 0x84,
				(byte) 0x00, (byte) 0x00, (byte) 0x08);
		ResponseAPDU resp_7 = channel.transmit(command_7);
		byte[] cardChallenge = resp_7.getData();

		// System.out.println("Obtained card challenge:");
		// printByteArray(cardChallenge);

		// System.out.println(Integer.toHexString(resp_7.getSW()));

		this.rndIcc = cardChallenge;

		// System.out.println("Card Challenge:");
		// printByteArray(cardChallenge);

		byte[] prnd2 = getRandomBytes(this.prndLength);
		byte[] kIfd = getRandomBytes(32);

		// System.out.println("Created KIFD:");
		// printByteArray(kIfd);

		// byte[] prnd2 = TEST_PRND2;
		// byte[] kIfd = TEST_KIFD;
		// byte[] cardChallenge = TEST_CARD_CHALLENGE;
		// snIcc = new
		// byte[]{(byte)0x06,(byte)0x5A,(byte)0x85,(byte)0xCA,(byte)0x58,(byte)0x6F,(byte)0x32};

		// System.out.println("Card Serial Number:");
		// printByteArray(snIcc);

		// compute hash
		byte[] hashData = new byte[prnd2.length + kIfd.length
				+ cardChallenge.length + 8];

		for (int i = 0; i < prnd2.length; i++) {

			hashData[i] = prnd2[i];
		}

		for (int i = 0; i < kIfd.length; i++) {

			hashData[prnd2.length + i] = kIfd[i];
		}

		for (int i = 0; i < cardChallenge.length; i++) {

			hashData[prnd2.length + kIfd.length + i] = cardChallenge[i];
		}

		int snPadding = 8 - snIcc.length;

		for (int i = 0; i < snPadding; i++) {

			hashData[prnd2.length + kIfd.length + cardChallenge.length + i] = (byte) 0x00;
		}

		for (int i = 0; i < snIcc.length; i++) {

			hashData[prnd2.length + kIfd.length + cardChallenge.length
					+ snPadding + i] = snIcc[i];
		}

		// System.out.println("HashData:");
		// printByteArray(hashData);

		byte[] digest = null;

		try {
			MessageDigest sha = MessageDigest.getInstance("SHA");

			sha.update(hashData);
			digest = sha.digest();

		} catch (NoSuchAlgorithmException e) {
			throw new CardException("Error computing hash.", e);
		}

		// System.out.println("Hash:");
		// printByteArray(digest);

		byte[] plain = new byte[2 + prnd2.length + kIfd.length + digest.length];

		plain[0] = (byte) 0x6A;

		for (int i = 0; i < prnd2.length; i++) {

			plain[i + 1] = prnd2[i];
		}

		for (int i = 0; i < kIfd.length; i++) {

			plain[i + 1 + prnd2.length] = kIfd[i];
		}

		for (int i = 0; i < digest.length; i++) {

			plain[i + 1 + prnd2.length + kIfd.length] = digest[i];
		}

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

		// System.out.println("SIG:");
		// printByteArray(encResult);

		// apply MIN function

		// ensure that created BigInteger is unsigned by adding leading 00 byte

		// BigInteger sig = new BigInteger(encResult);
		BigInteger sig = createUnsignedBigInteger(encResult);

		BigInteger mod = new BigInteger(TERMINAL_MODULO, 16);

		BigInteger diff = mod.subtract(sig);

		BigInteger sigMin = diff.min(sig);

		// System.out.println("SIGMIN:");
		// printByteArray(sigMin.toByteArray());

		PublicKey cardPubKey = null;
		// encrypt with card public key
		// try {
		// X509Certificate cert = new X509Certificate(componentCert);
		X509Certificate cert = createCertificate(componentCert);

		cardPubKey = cert.getPublicKey();
		// } catch (CertificateException e) {
		//
		// throw new CardException(
		// "Error retrieving public key from certificate.", e);
		// }

		byte[] authData = null;
		try {
			authData = rsaEncrypt(cardPubKey, sigMin.toByteArray());
		} catch (Exception e) {
			e.printStackTrace();
			throw new CardException("Error encrypting authentication data.");
		}

		// System.out.println("Authentication data:");
		// printByteArray(authData);

		// send auth data to card
		// BE CAREFUL WITH THAT!!! EXT-AUTH METHOD MAY GET BLOCKED!!!

		System.out.print("Sending authentication data to card..");
		CommandAPDU command_8 = new CommandAPDU((byte) 0x00, (byte) 0x82,
				(byte) 0x00, (byte) 0x00, authData);
		ResponseAPDU resp_8 = channel.transmit(command_8);

		System.out.println(Integer.toHexString(resp_8.getSW()));

		if (resp_8.getSW() == 0x9000) {

			this.kifd = kIfd;
		}

	}

	private boolean verifyInternalAuthenticationResponse(
			byte[] terminalChallenge, byte[] resp) throws CardException {

		System.out.println("Verifying card response..");

		// byte[] challenge = TEST_CHALLENGE;
		// byte[] response = TEST_RESPONSE;

		byte[] challenge = terminalChallenge;
		byte[] response = resp;

		// System.out.println("Cahllenge:");
		// printByteArray(challenge);
		//		
		// System.out.println("Response:");
		// printByteArray(response);

		// decrypt response with terminal private key
		byte[] plain = null;
		RSAPrivateKey terminalPrivateKey = createRSAPrivateKey(TERMINAL_MODULO,
				TERMINAL_PRIVEXP);
		try {
			plain = rsaDecrypt(terminalPrivateKey, response);
		} catch (Exception e) {
			throw new CardException("Error decrypting card response.", e);
		}

		// decrypt intermediate result with card's public key
		// CardChannel channel = setupCardChannel();
		// executeGetChipInfo(channel);

		// byte[] componentCert = executeReadCertificate(channel, new
		// byte[]{(byte)0x60, (byte)0x1F});

		PublicKey pubKey = null;

		// try {
		// X509Certificate cert = new X509Certificate(componentCert);
		X509Certificate cert = createCertificate(componentCert);

		pubKey = cert.getPublicKey();

		// } catch (CertificateException e) {
		//
		// throw new CardException(
		// "Error retrieving public key from certificate.", e);
		// }

		byte[] sig = null;

		try {
			sig = rsaDecrypt(pubKey, plain);

		} catch (Exception e) {

			throw new CardException("Error decrypting with card's public key",
					e);
		}

		if (sig == null) {

			throw new CardException("Computed value is null.");
		} else {

			// System.out.println("decrypted response (SIG):");
			// printByteArray(sig);

			if (sig[0] == (byte) 0x6A && sig[sig.length - 1] == (byte) 0xBC) {

				// Obtained response from card was obviously SIG - nothing else
				// to do here

			} else {

				// Obtained response from card was obviously N.ICC-SIG -
				// compute N.ICC-SIG and decrypt result again

				RSAPublicKey rsaPubKey = (RSAPublicKey) pubKey;
				BigInteger mod = rsaPubKey.getModulus();
				// BigInteger sigVal = new BigInteger(plain);
				BigInteger sigVal = createUnsignedBigInteger(plain);

				// System.out.println("MODULUS: " + mod);
				// System.out.println("SIGVAL: " + sigVal);
				//				
				BigInteger substractionResult = mod.subtract(sigVal);
				// System.out.println("DIFFERENCE: " + substractionResult);

				byte[] encrypted = substractionResult.toByteArray();

				// System.out.println("data to be decrypted:");
				// printByteArray(encrypted);

				// necessary as substraction result seems to contain one leading
				// zero byte
				byte[] trimmed = new byte[128];
				System.arraycopy(encrypted, encrypted.length - 128, trimmed, 0,
						128);

				try {
					sig = rsaDecrypt(pubKey, trimmed);

				} catch (Exception e) {

					throw new CardException("Error decrypting response.", e);
				}
			}
		}

		// extract data from decrypted response
		byte[] hash = new byte[20];
		byte[] kIcc = new byte[32];
		byte[] prnd1 = new byte[sig.length - 2 - 20 - 32];

		this.prndLength = prnd1.length;

		for (int i = 0; i < prnd1.length; i++) {

			prnd1[i] = sig[i + 1]; // 1 byte offset due to 6A padding
		}

		for (int i = 0; i < kIcc.length; i++) {

			kIcc[i] = sig[i + 1 + prnd1.length];
		}

		// System.out.println("Got KICC from card:");
		// printByteArray(kIcc);

		for (int i = 0; i < hash.length; i++) {

			hash[i] = sig[i + 1 + prnd1.length + kIcc.length];
		}

		// verify hash
		byte[] hashData = new byte[prnd1.length + kIcc.length
				+ challenge.length + 8];

		for (int i = 0; i < prnd1.length; i++) {

			hashData[i] = prnd1[i];
		}

		for (int i = 0; i < kIcc.length; i++) {

			hashData[i + prnd1.length] = kIcc[i];
		}

		for (int i = 0; i < challenge.length; i++) {

			hashData[i + prnd1.length + kIcc.length] = challenge[i];
		}

		for (int i = 0; i < 8; i++) {

			hashData[i + prnd1.length + kIcc.length + challenge.length] = RANDOM_TAIL[i];
		}

		byte[] digest = null;

		try {
			MessageDigest sha = MessageDigest.getInstance("SHA");

			sha.update(hashData);
			digest = sha.digest();

		} catch (NoSuchAlgorithmException e) {
			throw new CardException("Error computing hash.", e);
		}

		boolean internalAuthResult = Arrays.equals(hash, digest);

		if (internalAuthResult) {

			// if verification succeeded, remember kicc
			this.kicc = kIcc;
		}

		return internalAuthResult;

	}

	// for test purposes
	private void writeDataToFile(byte[] data, String filename) {

		FileOutputStream fos;
		try {
			fos = new FileOutputStream(filename);
			fos.write(data);
			fos.close();
		} catch (FileNotFoundException e) {
			System.out.println("Error writing File: " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Error writing File: " + e.getMessage());
			e.printStackTrace();
		}

	}

	// public static RSAPublicKey createRSAPublicKey(String mod, String
	// pubExponent) {
	//
	// BigInteger modulo = new BigInteger(mod, 16);
	// BigInteger pubExp = new BigInteger(pubExponent, 16);
	//
	// RSAPublicKey rsaPublicKey = new RSAPublicKey(modulo, pubExp);
	//
	// return rsaPublicKey;
	// }

	public static RSAPublicKey createRSAPublicKey(String mod, String pubExponent)
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

	public static RSAPrivateKey createRSAPrivateKey(String mod,
			String privExponent) throws CardException {

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

	public static byte[] rsaEncrypt(Key key, byte[] data)
			throws NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidKeyException, IllegalBlockSizeException, BadPaddingException {

		System.out.println("RSA ENCRYPTION:");
		BigInteger modulus = null;

		if (key instanceof RSAPublicKey) {
			RSAPublicKey pubKey = (RSAPublicKey) key;
			System.out.println("Key Modulus: " + pubKey.getModulus());
			modulus = pubKey.getModulus();
		}

		if (key instanceof RSAPrivateKey) {

			RSAPrivateKey privKey = (RSAPrivateKey) key;
			System.out.println("Key Modulus: " + privKey.getModulus());
			modulus = privKey.getModulus();
		}

		// BigInteger dataInt = new BigInteger(data);
		BigInteger dataInt = createUnsignedBigInteger(data);
		System.out.println("DATA: " + dataInt);

		if (dataInt.compareTo(modulus) > 0) {
			System.out.println("DATA IS LARGER!!!!");
		} else {
			System.out.println("DATA IS SMALLER - OK");
		}

		Cipher rsa = Cipher.getInstance("RSA/ECB/NoPadding");
		rsa.init(Cipher.ENCRYPT_MODE, key);
		byte[] encrypted = rsa.doFinal(data);

		return encrypted;

	}

	public static byte[] rsaDecrypt(Key key, byte[] cipher)
			throws NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidKeyException, IllegalBlockSizeException, BadPaddingException {

		Cipher rsa = Cipher.getInstance("RSA/ECB/NoPadding");
		rsa.init(Cipher.DECRYPT_MODE, key);
		byte[] decrypted = rsa.doFinal(cipher);

		return decrypted;
	}

	@Override
	public byte[] createSignature(InputStream input, KeyboxName keyboxName,
			PINGUI pinGUI, String alg) throws SignatureCardException,
			InterruptedException, IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte[] getCertificate(KeyboxName keyboxName, PINGUI provider)
			throws SignatureCardException, InterruptedException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte[] getInfobox(String infobox, PINGUI pinGUI, String domainId)
			throws SignatureCardException, InterruptedException {
		// TODO Auto-generated method stub
		return null;
	}

	private CardChannel setupCardChannel() throws CardException {

		// show the list of available terminals
		TerminalFactory factory = TerminalFactory.getDefault();
		List<CardTerminal> terminals = factory.terminals().list();
		System.out.println("Terminals: " + terminals);
		// get the first terminal
		CardTerminal terminal = terminals.get(0);
		// establish a connection with the card
		Card card = terminal.connect("*");
		System.out.println("card: " + card);
		CardChannel channel = card.getBasicChannel();

		return channel;
	}

	private static void printByteArray(byte[] data) {

		for (int i = 0; i < data.length; i++) {

			String s = Integer.toHexString(data[i]);

			if (s.length() == 1) {
				s = "0" + s;
			}

			if (s.length() > 2) {
				s = s.substring(s.length() - 2);
			}

			System.out.print(s + " ");
		}

		System.out.println();
	}

	private static byte[] intToHex(int val) {

		String hexString = Integer.toHexString(val);

		if (hexString.length() > 4) {
			System.out.println("Unexpected result. Quit.");
			return null;
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

	public byte[] getComponentCert() {
		return componentCert;
	}

	public void setComponentCert(byte[] componentCert) {
		this.componentCert = componentCert;
	}

	public byte[] getIntermediateCert() {
		return intermediateCert;
	}

	public void setIntermediateCert(byte[] intermediateCert) {
		this.intermediateCert = intermediateCert;
	}

	private int byteToInt(byte b) {

		return b < 0 ? b + 256 : b;

	}

	private int getRecordLength(byte[] data, int startOfLength) {

		byte lengthStartByte = data[startOfLength];

		if (lengthStartByte < 0) {
			// we have more than one length byte
			byte lengthOfLength = (byte) (lengthStartByte & (byte) 0x7F);

			byte[] lengthValues = new byte[lengthOfLength];
			System.arraycopy(data, startOfLength + 1, lengthValues, 0,
					lengthOfLength);

			int result = 0;

			for (int i = 0; i < lengthValues.length; i++) {

				result = (result + byteToInt(lengthValues[lengthValues.length
						- 1 - i])
						* (int) Math.pow(256, i));
			}

			return result + startOfLength + lengthOfLength + 1; // defined
			// length + tag
			// byte + length
			// bytes

		} else {

			return (int) lengthStartByte + startOfLength + 1; // defined length
			// + tag byte +
			// length byte
		}

	}

	private ASN1 getASN1WithinContextSpecific(byte[] data) {

		byte first = data[0];
		byte lengthOfLength = 0;

		if (first < 0) {

			lengthOfLength = (byte) (first & (byte) 0x7F);
			lengthOfLength = (byte) (lengthOfLength + 1);
		} else {

			lengthOfLength = 1;
		}

		byte[] asn1data = new byte[data.length - lengthOfLength];
		System.arraycopy(data, lengthOfLength, asn1data, 0, asn1data.length);

		try {
			return new ASN1(asn1data);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

}
