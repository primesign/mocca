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


package at.gv.egiz.smcc.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

import at.gv.egiz.smcc.ChangeReferenceDataAPDUSpec;
import at.gv.egiz.smcc.NewReferenceDataAPDUSpec;
import at.gv.egiz.smcc.SecurityStatusNotSatisfiedException;
import at.gv.egiz.smcc.SignatureCardException;
import at.gv.egiz.smcc.VerifyAPDUSpec;

public class ISO7816Utils {

	/**
	 * file control information templates
	 */
	public static final byte TAG_FCP = 0x62;
	public static final byte TAG_FMD = 0x64;
	public static final byte TAG_FCI = 0x6f;

	/**
	 * file control informatino bitmasks (SELECT P2)
	 */
	public static final byte P2_FCI = 0x00;
	public static final byte P2_FCP = 0x04;
	public static final byte P2_FMD = 0x08;
	public static final byte P2_NORESP = 0x0c;

	public static TransparentFileInputStream openTransparentFileInputStream(
			final CardChannel channel, int maxSize) {

		// open stream with default chunkSize of 256
		return openTransparentFileInputStream(channel, maxSize, 256);
	}

	public static TransparentFileInputStream openTransparentFileInputStream(
			final CardChannel channel, int maxSize, int chunkSize) {

		TransparentFileInputStream file = new TransparentFileInputStream(
				maxSize, chunkSize) {

			@Override
			protected byte[] readBinary(int offset, int len) throws IOException {

				if(len < 1) {
					// nothing to read - return
					return new byte[0];
				}
								
				ResponseAPDU resp;
				try {
					resp = channel.transmit(new CommandAPDU(0x00, 0xB0,
							0x7F & (offset >> 8), offset & 0xFF, len));
				} catch (CardException e) {
					throw new IOException(e);
				}

				// handle case: wrong number of bytes requested from card
				// card indicates correct number of bytes available in SW2
				if (resp.getSW1() == 0x6c) {
					
					try {
						resp = channel.transmit(new CommandAPDU(0x00, 0xB0,
								0x7F & (offset >> 8), offset & 0xFF, resp
										.getSW2()));
					} catch (CardException e) {

						throw new IOException("Error reading bytes from card.",
								e);
					}
				}

				Throwable cause;
				if (resp.getSW() == 0x9000) {
					return resp.getData();
				} else if (resp.getSW() == 0x6982) {
					cause = new SecurityStatusNotSatisfiedException();
				} else {
					cause = new SignatureCardException(
							"Failed to read bytes (offset=" + offset + ",len="
									+ len + ") SW="
									+ Integer.toHexString(resp.getSW()) + ".");
				}
				throw new IOException(cause);

			}

		};

		return file;
	}

	private static byte[] readFromInputStream(TransparentFileInputStream is) throws CardException, SignatureCardException {
		
		try {

			ByteArrayOutputStream os = new ByteArrayOutputStream();

			int len;
			for (byte[] b = new byte[256]; (len = is.read(b)) != -1;) {
				os.write(b, 0, len);
			}

			return os.toByteArray();

		} catch (IOException e) {
			Throwable cause = e.getCause();
			if (cause instanceof CardException) {
				throw (CardException) cause;
			}
			if (cause instanceof SignatureCardException) {
				throw (SignatureCardException) cause;
			}
			throw new SignatureCardException(e);
		}
	}
	
	public static byte[] readTransparentFile(CardChannel channel, int maxSize, int chunkSize)
			throws CardException, SignatureCardException {

		TransparentFileInputStream is = openTransparentFileInputStream(channel,
				maxSize, chunkSize);

		return readFromInputStream(is);
	}

	public static byte[] readTransparentFile(CardChannel channel, int maxSize)
			throws CardException, SignatureCardException {

		TransparentFileInputStream is = openTransparentFileInputStream(channel,
				maxSize);

		return readFromInputStream(is);
	}

	public static byte[] readTransparentFileTLV(CardChannel channel,
			int maxSize, byte expectedType) throws CardException,
			SignatureCardException {

		TransparentFileInputStream is = openTransparentFileInputStream(channel,
				maxSize);

		return readTransparentFileTLV(is, expectedType);

	}

	public static byte[] readTransparentFileTLV(TransparentFileInputStream is,
			byte expectedType) throws CardException, SignatureCardException {

		try {

			is.mark(256);

			// check expected type
			int b = is.read();
			if (b == 0x00 || b == 0xFF) {
				return null;
			}
			if (b == -1 || expectedType != (0xFF & b)) {
				throw new SignatureCardException(
						"Unexpected TLV type. Expected "
								+ Integer.toHexString(expectedType)
								+ " but was " + Integer.toHexString(b) + ".");
			}

			// get actual length
			int actualSize = 2;
			b = is.read();
			if (b == -1) {
				return null;
			} else if ((0x80 & b) > 0) {
				int octets = (0x0F & b);
				actualSize += octets;
				for (int i = 1; i <= octets; i++) {
					b = is.read();
					if (b == -1) {
						return null;
					}
					actualSize += (0xFF & b) << ((octets - i) * 8);
				}
			} else {
				actualSize += 0xFF & b;
			}

			// set limit to actual size and read into buffer
			is.reset();
			is.setLimit(actualSize);
			byte[] buf = new byte[actualSize];
			if (is.read(buf) == actualSize) {
				return buf;
			} else {
				return null;
			}

		} catch (IOException e) {
			Throwable cause = e.getCause();
			if (cause instanceof CardException) {
				throw (CardException) cause;
			}
			if (cause instanceof SignatureCardException) {
				throw (SignatureCardException) cause;
			}
			throw new SignatureCardException(e);
		}

	}

	public static int getLengthFromFCx(byte[] fcx) {

		int len = -1;

		if (fcx.length != 0 && (fcx[0] == (byte) 0x62 || fcx[0] == (byte) 0x6F)) {
			int pos = 2;
			while (pos < (fcx[1] - 2)) {
				switch (fcx[pos]) {

				case (byte) 0x80:
				case (byte) 0x81: {
					len = 0xFF & fcx[pos + 2];
					for (int i = 1; i < fcx[pos + 1]; i++) {
						len <<= 8;
						len += 0xFF & fcx[pos + i + 2];
					}
				}

				default:
					pos += 0xFF & fcx[pos + 1] + 2;
				}
			}
		}

		return len;

	}

	public static byte[] readRecord(CardChannel channel, int record)
			throws CardException, SignatureCardException {

		ResponseAPDU resp = channel.transmit(new CommandAPDU(0x00, 0xB2,
				record, 0x04, 256));
		if (resp.getSW() == 0x9000) {
			return resp.getData();
		} else {
			throw new SignatureCardException("Failed to read records. SW="
					+ Integer.toHexString(resp.getSW()));
		}

	}

	public static void formatPIN(int pinFormat, int pinJustification,
			byte[] fpin, byte[] mask, char[] pin) {

		boolean left = (pinJustification == VerifyAPDUSpec.PIN_JUSTIFICATION_LEFT);

		int j = (left) ? 0 : fpin.length - 1;
		int step = (left) ? 1 : -1;
		switch (pinFormat) {
		case VerifyAPDUSpec.PIN_FORMAT_BINARY:
			if (fpin.length < pin.length) {
				throw new IllegalArgumentException();
			}
			for (int i = 0; i < pin.length; i++) {
				fpin[j] = (byte) Character.digit(pin[i], 10);
				mask[j] = (byte) 0xFF;
				j += step;
			}
			break;

		case VerifyAPDUSpec.PIN_FORMAT_BCD:
			if (fpin.length * 2 < pin.length) {
				throw new IllegalArgumentException();
			}
			for (int i = 0; i < pin.length; i++) {
				int digit = Character.digit(pin[i], 10);
				boolean h = (i % 2 == 0) ^ left;
				fpin[j] |= h ? digit : digit << 4;
				mask[j] |= h ? (byte) 0x0F : (byte) 0xF0;
				j += (i % 2) * step;
			}
			break;

		case VerifyAPDUSpec.PIN_FORMAT_ASCII:
			if (fpin.length < pin.length) {
				throw new IllegalArgumentException();
			}
			byte[] asciiPin = Charset.forName("ASCII").encode(
					CharBuffer.wrap(pin)).array();
			for (int i = 0; i < pin.length; i++) {
				fpin[j] = asciiPin[i];
				mask[j] = (byte) 0xFF;
				j += step;
			}
			break;
		}

	}

	public static void insertPIN(byte[] apdu, int pos, byte[] fpin, byte[] mask) {
		for (int i = 0; i < fpin.length; i++) {
			apdu[pos + i] &= ~mask[i];
			apdu[pos + i] |= fpin[i];
		}
	}

	public static void insertPINLength(byte[] apdu, int length, int lengthSize,
			int pos, int offset) {

		// use short (2 byte) to be able to shift the pin length
		// by the number of bits given by the pin length position
		short size = (short) (0x00FF & length);
		short sMask = (short) ((1 << lengthSize) - 1);
		// shift to the proper position
		int shift = 16 - lengthSize - (pos % 8);
		offset += (pos / 8) + 5;
		size <<= shift;
		sMask <<= shift;
		// insert upper byte
		apdu[offset] &= (0xFF & (~sMask >> 8));
		apdu[offset] |= (0xFF & (size >> 8));
		// insert lower byte
		apdu[offset + 1] &= (0xFF & ~sMask);
		apdu[offset + 1] |= (0xFF & size);

	}

	public static CommandAPDU createVerifyAPDU(VerifyAPDUSpec apduSpec,
			char[] pin) {

		// format pin
		int l = (apduSpec.getPinLength() > 0) ? apduSpec.getPinLength()
				: pin.length;
		byte[] fpin = new byte[l];
		byte[] mask = new byte[l];
		formatPIN(apduSpec.getPinFormat(), apduSpec.getPinJustification(),
				fpin, mask, pin);

		byte[] template = apduSpec.getApdu();
		byte[] apdu = new byte[Math.max(template.length, 5
				+ apduSpec.getPinPosition() + l)];
		System.arraycopy(template, 0, apdu, 0, template.length);
		if (template.length < 5) {
			apdu[4] = (byte) (apdu.length - 5);
		}

		// insert formated pin
		insertPIN(apdu, apduSpec.getPinPosition() + 5, fpin, mask);

		// insert pin length
		if (apduSpec.getPinLengthSize() != 0) {
			insertPINLength(apdu, pin.length, apduSpec.getPinLengthSize(),
					apduSpec.getPinLengthPos(), 0);
		}

		return new CommandAPDU(apdu);

	}

	public static CommandAPDU createChangeReferenceDataAPDU(
			ChangeReferenceDataAPDUSpec apduSpec, char[] oldPin, char[] newPin) {

		int lo = (apduSpec.getPinLength() > 0) ? apduSpec.getPinLength()
				: oldPin.length;
		int ln = (apduSpec.getPinLength() > 0) ? apduSpec.getPinLength()
				: newPin.length;

		// format old pin
		byte[] fpin = new byte[lo];
		byte[] mask = new byte[lo];
		formatPIN(apduSpec.getPinFormat(), apduSpec.getPinJustification(),
				fpin, mask, oldPin);

		byte[] template = apduSpec.getApdu();
		byte[] apdu = new byte[Math.max(template.length, 5
				+ apduSpec.getPinPosition()
				+ Math.max(apduSpec.getPinInsertionOffsetOld() + lo, apduSpec
						.getPinInsertionOffsetNew()
						+ ln))];
		System.arraycopy(template, 0, apdu, 0, template.length);
		if (template.length < 5) {
			apdu[4] = (byte) (apdu.length - 5);
		}

		// insert formated old pin
		insertPIN(apdu, apduSpec.getPinPosition()
				+ apduSpec.getPinInsertionOffsetOld() + 5, fpin, mask);

		// insert pin length
		if (apduSpec.getPinLengthSize() != 0) {
			insertPINLength(apdu, oldPin.length, apduSpec.getPinLengthSize(),
					apduSpec.getPinLengthPos(), apduSpec
							.getPinInsertionOffsetOld());
		}

		// format new pin
		fpin = new byte[ln];
		mask = new byte[ln];
		formatPIN(apduSpec.getPinFormat(), apduSpec.getPinJustification(),
				fpin, mask, newPin);

		// insert formated new pin
		insertPIN(apdu, apduSpec.getPinPosition()
				+ apduSpec.getPinInsertionOffsetNew() + 5, fpin, mask);

		// insert pin length
		if (apduSpec.getPinLengthSize() != 0) {
			insertPINLength(apdu, newPin.length, apduSpec.getPinLengthSize(),
					apduSpec.getPinLengthPos(), apduSpec
							.getPinInsertionOffsetNew());
		}

		return new CommandAPDU(apdu);

	}

	public static CommandAPDU createNewReferenceDataAPDU(
			NewReferenceDataAPDUSpec apduSpec, char[] newPin) {

		// format old pin
		int l = (apduSpec.getPinLength() > 0) ? apduSpec.getPinLength()
				: newPin.length;
		byte[] fpin = new byte[l];
		byte[] mask = new byte[l];
		formatPIN(apduSpec.getPinFormat(), apduSpec.getPinJustification(),
				fpin, mask, newPin);

		byte[] template = apduSpec.getApdu();
		byte[] apdu = new byte[Math.max(template.length, 5
				+ apduSpec.getPinPosition() + l)];
		System.arraycopy(template, 0, apdu, 0, template.length);
		if (template.length < 5) {
			apdu[4] = (byte) (apdu.length - 5);
		}

		// insert formated new pin
		insertPIN(apdu, apduSpec.getPinPosition()
				+ apduSpec.getPinInsertionOffsetNew() + 5, fpin, mask);

		// insert pin length
		if (apduSpec.getPinLengthSize() != 0) {
			insertPINLength(apdu, newPin.length, apduSpec.getPinLengthSize(),
					apduSpec.getPinLengthPos(), apduSpec
							.getPinInsertionOffsetNew());
		}

		return new CommandAPDU(apdu);

	}

}
