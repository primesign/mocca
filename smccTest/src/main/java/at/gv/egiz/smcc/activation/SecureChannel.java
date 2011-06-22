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


package at.gv.egiz.smcc.activation;

import at.gv.egiz.smcc.util.TLVSequence;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 *
00002393 APDU: 00 A4 02 04 02 0E 01 00
00015613 SW: 62 19 80 02 01 2C C5 02 00 AC 82 01 41 83 02 0E 01 88 00 8A 01 05 A1 03 8B 01 05 90 00
 * 80: num data bytes: 01 2c
 * c5: ?
 * 82: fd=41 shareable, transparent structure
 * 83: fid=0e 01
 * 88: no short fid
 * 8a: activated
 * a1: security attribute template (proprietary format): 8b 01 05
00001770 APDU: 00 22 81 A4 06 83 01 81 80 01 54
00013461 SW: 90 00
00000592 APDU: 00 84 00 00 08
00024160 SW: 5B 3D B2 93 3F E8 CE 7C 90 00
00002153 APDU: 00 82 00 00 68 B1 0C 9A F3 6D FA B0 70 D8 4D 6A 0B 3C E9 8E 5E 50 D0 2A 82 1E CD 70 77 6F CC D4 E8 4D 0E A3 E6 B8 87 2E 31 0F B1 0B 42 5F EB C6 36 B0 EC 18 86 94 0D 5B 67 6C 1A 96 8F C7 2B 8E 4B 85 2A 91 63 C9 E4 66 43 42 D9 55 FF 44 5C C9 DE A6 44 D3 46 37 DA 47 02 A3 63 BA E7 4D CB 52 64 5D F6 B4 94 6B 51 02 0A DD 8F 10 55 00
00186875 SW: B2 72 5B 23 D2 F6 CB 09 9B 76 1D 62 2E 58 87 24 2A 51 8A 7B F3 79 D8 E9 A3 0B B9 B1 CB 72 77 DD B4 A1 C1 8E 22 5B 0F FA 28 F4 DC 00 B0 D0 96 8F 48 58 85 44 65 7F 11 46 A1 BB DA 17 F2 05 82 23 EC 5E B9 55 92 63 EF 41 A7 BE C8 7E C1 6F 83 82 7A A5 6E AD 94 15 35 37 43 A9 88 89 60 41 A8 87 76 41 EE DB 97 E3 70 CC 90 00
00003139 APDU: 0C B0 00 00 0D 97 01 DF 8E 08 CE BD FA EC FA B2 C5 D7 00
00041878 SW: 81 81 AC A8 82 00 A8 B6 16 83 14 80 04 00 00 00 23 00 79 05 03 D0 40 00 00 17 00 12 01 02 00 7F 49 82 00 44 86 40 D4 7C 12 55 E4 7B 0C 7D 4E BB 17 E4 83 E5 3D 56 DF 45 7E 99 CB CC 93 D2 C2 5E 4D 91 27 6E 8B E7 6D 23 53 F6 AB 2E A6 DD B7 1C 68 FB 59 CD D0 45 2B 10 0E 27 00 6E AA 1C 49 90 67 A9 9F 59 D1 97 C1 00 C0 01 80 9E 82 00 40 DE 22 37 4C 41 E0 F7 94 9A 5A E4 76 B8 9B 00 B8 23 7C E9 4A 92 FD B0 FB 25 4A A7 0E 4D 5F 6F 3D 3A 54 28 F8 90 A1 7D 60 28 F8 72 B7 0F 9F A6 A8 53 15 F2 9F 88 37 D4 6B 77 F7 69 C1 B9 E7 2A 43 99 02 62 82 8E 08 25 1C C9 6E 87 22 DD DB 62 82

 * @author clemens
 */
public class SecureChannel extends CardChannel {

	protected static final Logger log = LoggerFactory.getLogger(SecureChannel.class);

	//TODO access 
	CardChannel channel;
	SecretKeySpec kenc;
	SecretKeySpec kmac;
	byte[] kencssc;
	byte[] kmacssc;
	int blocksize;

	public SecureChannel(CardChannel basicChannel,
					SecretKeySpec kenc, SecretKeySpec kmac, int blocksize,
					byte[] kencssc, byte[] kmacssc) {
		this.channel = basicChannel;
		this.kenc = kenc;
		this.kmac = kmac;

		if (kencssc.length != kmacssc.length || kmacssc.length < blocksize) {
			throw new IllegalArgumentException("invalid ssc or blocksize");
		}
		this.kencssc = kencssc;
		this.kmacssc = kmacssc;
		this.blocksize = blocksize;

	}

	@Override
	public Card getCard() {
		return channel.getCard();
	}

	@Override
	public int getChannelNumber() {
		return channel.getChannelNumber();
	}

	@Override
	public ResponseAPDU transmit(CommandAPDU capdu) throws CardException {

		byte[] apdu = capdu.getBytes();

		try {
			if (apdu.length < 4) {
				throw new IllegalArgumentException("invalid Command APDU " + toString(apdu));
			} else if (apdu.length < 6) {
				CommandAPDU capdu_ = new CommandAPDU(
								protectNoCommandData(apdu));
				log.info("cmd apdu*: {}", toString(capdu_.getBytes()));
				ResponseAPDU resp_ = channel.transmit(capdu_);
				log.info(" -> resp*: {}", toString(resp_.getBytes()));
				return unProtectResponse(resp_);

			} else {
				CommandAPDU capdu_ = new CommandAPDU(
								protectCommandData(apdu, true));
				log.info("cmd apdu*: {}", toString(capdu_.getBytes()));
				ResponseAPDU resp_ = channel.transmit(capdu_);
				log.info(" -> resp*: {}", toString(resp_.getBytes()));
				return unProtectResponse(resp_);
				
			}
		} catch (Exception ex) {
			System.out.println("failed to transmit protected APDU: " + ex.getMessage());
			throw new CardException(ex);
		}
	}

	@Override
	public int transmit(ByteBuffer bb, ByteBuffer bb1) throws CardException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void close() throws CardException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	/**
	 * Case 1b/2 of command-response pair defined in ISO 7816-3
	 *
	 * CLA|INS|P1|P2    -> CLA'|INS|P1|P2|Lc'|TLmac|00
	 * CLA|INS|P1|P2|Le -> CLA'|INS|P1|P2|Lc'|TLle|TLmac|00
	 *
	 * @param apdu
	 * @return
	 * @throws GeneralSecurityException mac-calculation error
	 */
	byte[] protectNoCommandData(byte[] apdu) throws GeneralSecurityException {

		// whether Le Byte is present (response expected)
		int leLength = (apdu.length == 5) ? 1 : 0;
		
		byte[] apdu_ = new byte[16 + 3*leLength];
		// authenticate header: CLA** b3=1
		apdu_[0] = (byte) (apdu[0] | (byte) 0x0c); //CLA**: b8-6=000 b4-3=11
		apdu_[1] = apdu[1];
		apdu_[2] = apdu[2];
		apdu_[3] = apdu[3];

		// Lc': [TLle] TLmac
		apdu_[4] = (byte) (3*leLength + 2 + blocksize); //0x0a or 0x0d;

		// T*L Le
		if (leLength > 0) {
			apdu_[5] = (byte) 0x97;
			apdu_[6] = (byte) 0x01;
			apdu_[7] = apdu[4];
		}

		// cryptographic checksum
		apdu_[5 + 3*leLength] = (byte) 0x8e;
		apdu_[6 + 3*leLength] = (byte) blocksize; //0x08;

		// Le'
		apdu_[apdu_.length-1] = 0x00;

		// 3 data objects: SSC, header, Le
		byte[] mac_in = new byte[2*blocksize + blocksize*leLength];

		// SSC (rightmost blocksize bytes)
		incrementSSC();
		System.arraycopy(kmacssc, kmacssc.length-blocksize, mac_in, 0, blocksize);

		// CLA** INS P1 P2 padding
		byte[] paddedHeader = RetailCBCMac.pad(Arrays.copyOf(apdu_, 4), blocksize);
		System.arraycopy(paddedHeader, 0, mac_in, blocksize, blocksize);

		// TL Le
		if (leLength > 0) {
			byte[] paddedTLLe = RetailCBCMac.pad(Arrays.copyOfRange(apdu_, 5, 8), blocksize);
			System.arraycopy(paddedTLLe, 0, mac_in, 2*blocksize, blocksize);
		}

		byte[] mac = RetailCBCMac.retailMac(mac_in, RetailCBCMac.PADDING.NoPadding, 
						"DES", "DESede", kmac, blocksize, blocksize);

		log.debug("cryptographic checksum ({}): {}", toString(mac_in), toString(mac));

		// insert mac in 8E object
		System.arraycopy(mac, 0, apdu_, 7+3*leLength, blocksize);
		return apdu_;
	}

	/**
	 * Verify cryptographic checksum for cases 1b/2/3b/4 and decode response apdu
	 *
	 * T*Lsw1sw2 | TLcc            -> sw1sw2
	 * T*Lplain | T*Lsw1sw2 | TLcc -> plain | sw1sw2
	 *
	 * @param resp_
	 * @return
	 * @throws GeneralSecurityException
	 * @throws CardException
	 */
	ResponseAPDU unProtectResponse(ResponseAPDU resp_) throws GeneralSecurityException, CardException {

		byte[] respData = resp_.getData();

		if (respData != null && respData.length > 0) {
			
			int TLccInd = respData.length - (2 + blocksize);

			byte[] mac_in = new byte[respData.length - 2]; //+blocksize-blocksize

			// SSC (rightmost blocksize bytes)
			incrementSSC();
			System.arraycopy(kmacssc, kmacssc.length-blocksize, mac_in, 0, blocksize);

			// data
			System.arraycopy(respData, 0, mac_in, blocksize, TLccInd);
			
			byte[] mac_ = RetailCBCMac.retailMac(mac_in, RetailCBCMac.PADDING.ISO9797_2,
							"DES", "DESede", kmac, blocksize, blocksize);

			log.debug("cryptographic checksum ({}): {}", toString(mac_in), toString(mac_));

			TLVSequence respSeq = new TLVSequence(respData);

			byte[] cc = respSeq.getValue(0x8e);
			if (!Arrays.equals(mac_, cc)) {
				throw new CardException("invalid cryptographic checksum " + toString(cc));
			}

			byte[] sw = respSeq.getValue(0x99);
			if (sw[0] != (byte) 0x90 || sw[1] != (byte) 0x00) {
				throw new CardException("invalid status-word object " + toString(sw));
			}

			byte[] plain = respSeq.getValue(0x81);
			if (plain != null) {
				byte[] resp = new byte[plain.length + 2];
				System.arraycopy(plain, 0, resp, 0, plain.length);
				System.arraycopy(sw, 0, resp, plain.length, 2);
				return new ResponseAPDU(resp);
			} else {
				return new ResponseAPDU(sw);
			}
		}

		log.info("unexpected response " + toString(resp_.getBytes()));
		return resp_;
	}

	/**
	 * Case 3b/4 of command-response pair defined in ISO 7816-3
	 *
	 * CLA|INS|P1|P2|Lc|Data -> CLA'|INS|P1|P2|Lc'|TLplain|TLmac|00
	 * CLA|INS|P1|P2|Lc|Data|Le -> CLA'|INS|P1|P2|Lc'|TLplain|TLle|TLmac|00
	 * 
	 * @param apdu
	 * @return
	 * @throws GeneralSecurityException mac-calculation error
	 */
	byte[] protectCommandData(byte[] apdu, boolean encrypt) throws GeneralSecurityException {

		int contentLength = apdu[4];
		byte[] cryptogram = null;

		// whether Le Byte is present (response expected)
		int leLength = (apdu.length > 5+contentLength) ? 1 : 0;

		if (encrypt) {
			cryptogram = encrypt(Arrays.copyOfRange(apdu, 5, 5+apdu[4]));
			contentLength = cryptogram.length + 1; // + padding-content indicator byte
			log.info("cryptogram (" + cryptogram.length + "byte): "
							+ toString(cryptogram));
		}

		// header | Lc' | TLplain(TL-P-cryptogram) | [TLLe] | TLmac | 00
		byte[] apdu_= new byte[4 + 1 + 2+contentLength + 3*leLength + 2+blocksize + 1];

		// authenticate header: CLA** b3=1
		apdu_[0] = (byte) (apdu[0] | (byte) 0x0c); //CLA**: b8-6=000 b4-3=11
		apdu_[1] = apdu[1];
		apdu_[2] = apdu[2];
		apdu_[3] = apdu[3];

		// Lc': TLplain [TLLe] TLmac
		apdu_[4] = (byte) (2+contentLength + 3*leLength +  2+blocksize);

		if (encrypt) {
			// T*L paddingIndicatorByte crpytogram
			apdu_[5] = (byte) 0x87;
			apdu_[6] = (byte) contentLength;
			apdu_[7] = (byte) 0x01;
			System.arraycopy(cryptogram, 0, apdu_, 8, cryptogram.length);
			
		} else {
			// T*L plain
			apdu_[5] = (byte) 0x81;
			apdu_[6] = apdu[4];
			System.arraycopy(apdu, 5, apdu_, 7, apdu[4]);
		}

		// T*L Le
		if (leLength > 0) {
			apdu_[7+contentLength] = (byte) 0x97;
			apdu_[8+contentLength] = (byte) 0x01;
			apdu_[9+contentLength] = apdu[apdu.length-1];
		}

		// TL cc
		apdu_[7 + contentLength + 3*leLength] = (byte) 0x8e;
		apdu_[8 + contentLength + 3*leLength] = (byte) blocksize; //0x08;

		apdu_[apdu_.length-1] = (byte) 0x00;

		// TLplain [TLLe] Padding
		byte[] paddedPlainLe = RetailCBCMac.pad(
						Arrays.copyOfRange(apdu_, 5, 5+2+contentLength + 3*leLength), blocksize);
		log.trace("padded plain command data: " + toString(paddedPlainLe));

		// 3 blocks: SSC, header|padding, TLplain[TLLe]|padding
		byte[] mac_in = new byte[2*blocksize + paddedPlainLe.length];

		// SSC (rightmost blocksize bytes)
		incrementSSC();
		System.arraycopy(kmacssc, kmacssc.length-blocksize, mac_in, 0, blocksize);

		// CLA** INS P1 P2 padding
		byte[] paddedHeader = RetailCBCMac.pad(Arrays.copyOf(apdu_, 4), blocksize);
		System.arraycopy(paddedHeader, 0, mac_in, blocksize, blocksize);

		System.arraycopy(paddedPlainLe, 0, mac_in, 2*blocksize, paddedPlainLe.length);

		byte[] mac = RetailCBCMac.retailMac(mac_in, RetailCBCMac.PADDING.NoPadding,
						"DES", "DESede", kmac, blocksize, blocksize);

		log.debug("cryptographic checksum ({}): {}", toString(mac_in), toString(mac));

		// insert mac in 8E object
		System.arraycopy(mac, 0, apdu_, 9 + contentLength + 3*leLength, blocksize);
		return apdu_;
	}

	byte[] encrypt(byte[] plain) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {

		incrementKENCSSC();

		Cipher tDES = Cipher.getInstance("3DES/CBC/NoPadding");
		tDES.init(Cipher.ENCRYPT_MODE, kenc, new IvParameterSpec(kencssc)); //Activation.ZEROS));

		byte[] paddedPlain = RetailCBCMac.pad(plain, blocksize);
		
		log.info("cryptogram input (" + paddedPlain.length + "byte): "
						+ toString(paddedPlain));

		return tDES.doFinal(paddedPlain);
	}

	void incrementSSC() {
		//TODO
		kmacssc[7] += 1;
		log.info("incrementing kmac_ssc: " + toString(kmacssc));
	}

	void incrementKENCSSC() {
		//TODO
		kencssc[7] += 1;
		log.debug("incrementing kenc_ssc: " + toString(kencssc));
	}

	public static String toString(byte[] b) {
		StringBuilder sb = new StringBuilder();
		sb.append('[');
		if (b != null && b.length > 0) {
			sb.append(Integer.toHexString((b[0] & 240) >> 4));
			sb.append(Integer.toHexString(b[0] & 15));
			for (int i = 1; i < b.length; i++) {
				sb.append((i % 32 == 0) ? '\n' : ':');
				sb.append(Integer.toHexString((b[i] & 240) >> 4));
				sb.append(Integer.toHexString(b[i] & 15));
			}
		}
		sb.append(']');
		return sb.toString();
	}
}
