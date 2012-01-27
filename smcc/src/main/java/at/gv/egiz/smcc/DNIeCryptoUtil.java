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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.KeySpec;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.smartcardio.CardException;

public class DNIeCryptoUtil {

	private final static byte[] IV = new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00,
			0x00, 0x00, 0x00 };

	public static X509Certificate createCertificate(byte[] certData)
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

	public static byte[] getRandomBytes(int length) {

		byte[] result = new byte[length];

		for (int i = 0; i < length; i++) {

			SecureRandom rand = new SecureRandom();
			byte current = (byte) rand.nextInt(255);
			result[i] = current;
		}

		return result;
	}

	public static byte[] computeSHA1Hash(byte[] data) throws CardException {

		try {
			MessageDigest sha = MessageDigest.getInstance("SHA");

			sha.update(data);
			return sha.digest();

		} catch (NoSuchAlgorithmException e) {
			throw new CardException("Error computing SHA1 hash.", e);
		}

	}

	public static byte[] rsaEncrypt(Key key, byte[] data)
			throws NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidKeyException, IllegalBlockSizeException, BadPaddingException {

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

	public static byte[] applyPadding(int blockSize, byte[] data) {

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

	public static byte[] removePadding(byte[] paddedData) throws CardException {

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

	public static byte[] calculateAPDUMAC(byte[] data, byte[] key, byte[] ssc,
			int blockLength) throws CardException {

		SecretKeySpec desSingleKey = new SecretKeySpec(key, 0, blockLength,
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

		byte[] dataBlock = new byte[blockLength];

		for (int i = 0; i < dataLen - blockLength; i = i + blockLength) {

			System.arraycopy(data, i, dataBlock, 0, blockLength);
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

		System.arraycopy(data, data.length - blockLength, dataBlock, 0,
				blockLength);
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

	public static byte[] xorByteArrays(byte[] array1, byte[] array2)
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

	public static byte[] perform3DESCipherOperation(byte[] data,
			byte[] keyData, int mode) throws NoSuchAlgorithmException,
			NoSuchProviderException, NoSuchPaddingException,
			InvalidKeyException, InvalidAlgorithmParameterException,
			ShortBufferException, IllegalBlockSizeException,
			BadPaddingException {

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

	public static int getCutOffLength(byte[] data, int blockSize)
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
}
