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

import at.gv.egiz.smcc.SignatureCardException;
import at.gv.egiz.smcc.util.TLVSequence;
import iaik.asn1.ASN1;
import iaik.asn1.ASN1Object;
import iaik.asn1.CodingException;
import iaik.asn1.DerCoder;
import iaik.security.ecc.ECCException;
import iaik.security.ecc.ecdsa.ECDSAParameter;
import iaik.security.ecc.ecdsa.ECPublicKey;
import iaik.security.ecc.math.ecgroup.AffineCoordinate;
import iaik.security.ecc.math.ecgroup.CoordinateTypes;
import iaik.security.ecc.math.ecgroup.ECGroupFactory;
import iaik.security.ecc.math.ecgroup.ECPoint;
import iaik.security.ecc.math.ecgroup.EllipticCurve;
import iaik.security.ecc.math.field.FieldElement;
import iaik.security.ecc.parameter.ECCParameterFactory;
import iaik.security.ecc.spec.ECCParameterSpec;
import iaik.security.ecc.util.PointFormatter;
import iaik.security.provider.IAIK;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import javax.smartcardio.TerminalFactory;

/**
 *
 * @author clemens
 */
public class Activation {

    /**
     * cf. kp_mk_ss_test.xml
     */
    static SecretKeySpec MK_QSig = new SecretKeySpec(new byte[]{
        (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05,
        (byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09, (byte) 0x0A,
        (byte) 0x0B, (byte) 0x0C, (byte) 0x0D, (byte) 0x0E, (byte) 0x0F,
        (byte) 0x10, (byte) 0x11, (byte) 0x12, (byte) 0x13, (byte) 0x14,
        (byte) 0x15, (byte) 0x16, (byte) 0x17, (byte) 0x18}, "3DES");

    /**
     * DES_CBC[MK_Appl](SHA-256(CIN|KeyNum|KeyVer))
     * 
     * @param masterKey
     * @param cin
     * @param keyNum
     * @param keyVer
     * @return
     */
    static SecretKeySpec deriveApplicationKey(SecretKeySpec masterKey, byte[] cin, byte keyNum, byte keyVer) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {

        System.out.println("derive application key for \n CIN "
                + toString(cin)
                + "\n key number 0x0"
                + Byte.toString(keyNum)
                + "\n key version 0x0"
                + Byte.toString(keyVer));

        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        
        sha256.update(cin);
        sha256.update(keyNum);
        sha256.update(keyVer);
        byte[] derivationParam = sha256.digest();
        
        return deriveKey(masterKey, Arrays.copyOf(derivationParam, 24));
    }

    static SecretKeySpec deriveKENC(SecretKeySpec k_appl) throws NoSuchAlgorithmException, UnsupportedEncodingException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {

        System.out.println("derive k_enc");

        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        byte[] derivationParam = sha256.digest("K_ENC".getBytes("ASCII"));

        return deriveKey(k_appl, Arrays.copyOf(derivationParam, 24));
    }

    static SecretKeySpec deriveKMAC(SecretKeySpec k_appl)  throws NoSuchAlgorithmException, UnsupportedEncodingException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
        System.out.println("derive k_mac");

        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        byte[] derivationParam = sha256.digest("K_MAC".getBytes("ASCII"));

        return deriveKey(k_appl, Arrays.copyOf(derivationParam, 24));
    }

    /**
     * DES_CBC[MK](derivationParam)
     * 3DES/CBC/NoPadding
     * 
     * @param masterKey
     * @param derivationParam
     * @return
     */
    static SecretKeySpec deriveKey(SecretKeySpec masterKey, byte[] derivationParam) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {

        if (derivationParam.length != 24) {
            throw new RuntimeException("invalid 3TDES derivation parameter: " + toString(derivationParam));
        }

        Cipher tdes = Cipher.getInstance("3DES/CBC/NoPadding");
        tdes.init(Cipher.ENCRYPT_MODE, masterKey, new IvParameterSpec(ZEROS));

        System.out.println(tdes.getAlgorithm());
        System.out.println("master key : " + toString(masterKey.getEncoded()));
        
        System.out.println("derivation parameter ("
                + derivationParam.length * 8 + "bit): "
                + toString(derivationParam));
        System.out.println("derivation key ("
                + masterKey.getAlgorithm() + ") :"
                + toString(masterKey.getEncoded()));

        byte[] x = tdes.doFinal(derivationParam);

        System.out.println("x (" + x.length * 8 + "bit): " + toString(x));

        if (x.length != 24) {
            throw new RuntimeException("invalid derived key: " + toString(x));
        }

        for (int offset = 0; offset < x.length; offset += 8) {
            adjustParityBit(x, offset);
        }

        SecretKeySpec derivedKey = new SecretKeySpec(x, masterKey.getAlgorithm());

        System.out.println("derived key ("
                + derivedKey.getAlgorithm() + ") :"
                + toString(derivedKey.getEncoded()));

        return derivedKey;

    }

    public final static byte[] AID_QSig = new byte[] {
        (byte) 0xd0, (byte) 0x40, (byte) 0x00, (byte) 0x00, (byte) 0x17,
        (byte) 0x00, (byte) 0x12, (byte) 0x01};

    public final static byte[] ZEROS = {
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
    };

		public final static byte[] ONES = {
        (byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x11,
				(byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x11
    };


    /** Bit mask for counting the ones. */
    private final static byte[] BIT_MASK = {
        0x01, 0x02, 0x04, 0x08, 0x10, 0x20, 0x40, (byte) 0x80
    };

    private static void adjustParityBit(byte[] x, int offset) {

        for (int i = 0; i < 8; i++) {
            int ones = 0;
            for (int j = 1; j < BIT_MASK.length; j++) {
                if ((x[i + offset] & BIT_MASK[j]) == BIT_MASK[j]) {
                    ones++;
                }
            }

            if ((ones & 0x1) > 0) {
                x[i + offset] &= (byte) 0xfe;  // odd
            } else {
                x[i + offset] |= 0x1;         // even
            }
        }
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


    CardTerminal ct;
    Card icc;
    CardChannel channel;

    public void setUp() throws NoSuchAlgorithmException, CardException {

        IAIK.addAsJDK14Provider();

        System.out.println("create terminalFactory...\n");
        TerminalFactory terminalFactory = TerminalFactory.getInstance("PC/SC", null);

        System.out.println("get supported terminals...\n");
        List<CardTerminal> terminals = terminalFactory.terminals().list();

        if (terminals.size() < 1) {
            throw new CardException("no terminals");
        }

        ct = terminals.get(0);
        System.out.println("found " + terminals.size() + " terminals, using " + ct.getName() + "\n");

        System.out.println("connecting " + ct.getName() + "\n");
        icc = ct.connect("*");
        byte[] atr = icc.getATR().getBytes();
        byte[] historicalBytes = icc.getATR().getHistoricalBytes();
        System.out.println("found card " + toString(atr) + " " + new String(historicalBytes, Charset.forName("ASCII")) + "\n\n");

        channel = icc.getBasicChannel();
    }

    public void activate() throws CardException, SignatureCardException, Exception {

        System.out.println("SELECT MF");
        CommandAPDU cmdAPDU = new CommandAPDU(0x00, 0xA4, 0x00, 0x0c, new byte[]{(byte) 0x3F, (byte) 0x00});
        System.out.println(" cmd apdu " + toString(cmdAPDU.getBytes()));
        ResponseAPDU resp = channel.transmit(cmdAPDU);
        System.out.println(" -> " + toString(resp.getBytes()) + "\n");

        byte[] cin = getCIN();

        byte[] k_qsigNumVer = getKApplNumberVersion(AID_QSig);

        SecretKeySpec k_qsig;
        try {
            k_qsig = deriveApplicationKey(MK_QSig, cin, k_qsigNumVer[0], k_qsigNumVer[1]);
            System.out.println("K_QS (" + k_qsig.getAlgorithm() + ")"
                    + toString(k_qsig.getEncoded()));
        } catch (Exception ex) {
            throw new SignatureCardException("failed to derive k_qs", ex);
        }

        System.out.println("SELECT EF.PuK_QS");
        // P1=0x00 -> 67:00 (wrong length)
        cmdAPDU = new CommandAPDU(0x00, 0xa4, 0x02, 0x04, new byte[]{(byte) 0x0e, (byte) 0x01}, 256);
        System.out.println(" cmd apdu " + toString(cmdAPDU.getBytes()));
        resp = channel.transmit(cmdAPDU);
        System.out.println(" -> " + toString(resp.getBytes()) + "\n");

        openSecureChannel(k_qsig, cin);

        System.out.println("READ BINARY");
        cmdAPDU = new CommandAPDU(0x00, 0xb0, 0x00, 0x00, 256);
        System.out.println(" cmd apdu " + toString(cmdAPDU.getBytes()));
        resp = channel.transmit(cmdAPDU);
        System.out.println(" -> " + toString(resp.getBytes()) + "\n");

        try{
            ASN1Object puk = DerCoder.decode(resp.getData());
            System.out.println("EF.PuK:\n" + ASN1.print(puk));

            byte[] oid = (byte[]) puk.getComponentAt(1).getComponentAt(1).getValue();
            if (oid == null || oid.length == 0) {
                System.out.println("assume P-256");
                oid = "1.2.840.10045.3.1.7".getBytes("ASCII");
            }

            System.out.println("OID: " + new String(oid));
            byte[] Q = (byte[]) puk.getComponentAt(1).getComponentAt(0).getValue();
//            byte[] Qx = Arrays.copyOfRange(Q, 0, Q.length/2);
//            byte[] Qy = Arrays.copyOfRange(Q, Q.length/2, Q.length);
//
//            System.out.println("Qx: " + toString(Qx));
//            System.out.println("Qy: " + toString(Qy));

            byte[] Q_ = new byte[Q.length + 1];
            Q_[0] = (byte) 0x04;
            System.arraycopy(Q, 0, Q_, 1, Q.length);
            ECPublicKey ecPuK = decodeECPublicKey(Q_, new String(oid));
            System.out.println("PuK: " + ecPuK);

        } catch (CodingException ex) {
            throw new SignatureCardException("failed to read EF.PuK", ex);
        }


				System.out.println("DUMMY APDU SELECT EF_C_X509.CH.DS");
        cmdAPDU = new CommandAPDU(0x00, 0xA4, 0x02, 0x00, new byte[]{(byte) 0xc0, (byte) 0x00}, 256);
        System.out.println(" cmd apdu " + toString(cmdAPDU.getBytes()));
        resp = channel.transmit(cmdAPDU);
        System.out.println(" -> " + toString(resp.getBytes()) + "\n");

				System.out.println("DUMMY APDU READ BINARY");
        cmdAPDU = new CommandAPDU(0x00, 0xb0, 0x00, 0x00, 256);
        System.out.println(" cmd apdu " + toString(cmdAPDU.getBytes()));
        resp = channel.transmit(cmdAPDU);
        System.out.println(" -> " + toString(resp.getBytes()) + "\n");

//				System.out.println("DELETE EF_C_X509.CH.DS *****************************");
//        cmdAPDU = new CommandAPDU(0x00, 0xE4, 0x02, 0x00, new byte[]{(byte) 0xC0, (byte) 0x00});
//        System.out.println(" cmd apdu " + toString(cmdAPDU.getBytes()));
//        resp = channel.transmit(cmdAPDU);
//        System.out.println(" -> " + toString(resp.getBytes()) + "\n");

        /*
        [a8:82:00:a8:b6:16:83:14:80:04:00:00:00:23:00:79:05:03:d0:40:00:00:17:00:12:01:02:00:
         7f:49:
         82:00: public exponent?
         44:
         86:40:
            d4:7c:12:55:e4:7b:0c:7d:4e:bb:17:e4:83:e5:3d:56:df:45:7e:99:cb:cc:93:d2:c2:5e:4d:91:27:6e:8b:e7:
            6d:23:53:f6:ab:2e:a6:dd:b7:1c:68:fb:59:cd:d0:45:2b:10:0e:27:00:6e:aa:1c:49:90:67:a9:9f:59:d1:97:
         c1:00:c0:01:80:9e:82:00:40:de:22:37:4c:41:e0:f7:94:9a:5a:e4:76:b8:9b:00:b8:23:7c:e9:4a
92:fd:b0:fb:25:4a:a7:0e:4d:5f:6f:3d:3a:54:28:f8:90:a1:7d:60:28:f8:72:b7:0f:9f:a6:a8:53:15:f2:9f
88:37:d4:6b:77:f7:69:c1:b9:e7:2a:43:90:00]

         <ResponseAPDU SW="9000" rc="0" sequence="7">A88200A8B616831480040000002300789901D04000001700120102007F49820044864084BA7BF0AF355A67E0C9064EE53A63859903C775199221494A430FFAE20F3F2DC283FEF3C8EEF21FBF75448DC7DB9649BAC504DE0C6416C91D62882438128CDFC100C001809E82004087506037D74C9DCE7454A2F561A19FF24ED03D097A0CD8D45F3CB2DCF51684195632F39D72381F64BA2DCB65524C54E94265CB9E5F43EBCC02D23C1D9A02D26E</ResponseAPDU>
        */
    }


    /**
     * @precondition MF
     * 
     * @return
     * @throws CardException
     * @throws SignatureCardException
     */
    byte[] getCIN() throws CardException, SignatureCardException {
        CommandAPDU cmdAPDU;
        ResponseAPDU resp;

        //alt. select 2f:02
        System.out.println("READ EF.GDO (SFI=02)");
        cmdAPDU = new CommandAPDU(0x00, 0xb0, 0x82, 0x00, 256);
        System.out.println(" cmd apdu " + toString(cmdAPDU.getBytes()));
        resp = channel.transmit(cmdAPDU);
        System.out.println(" -> " + toString(resp.getBytes()) + "\n");

        if (resp.getSW() == 0x9000) {
            byte[] cin = new TLVSequence(resp.getData()).getValue(0x5a);
            System.out.println("CIN: " + toString(cin));
            return cin;
        } else {
            throw new SignatureCardException("Failed to read EF.GDO: 0x" + Integer.toHexString(resp.getSW()));
        }
    }

    /**
     * @precondition MF
     * @postcondition AID
     * 
     * @param aid
     * @return
     * @throws CardException
     * @throws SignatureCardException
     */
    byte[] getKApplNumberVersion(byte[] aid) throws CardException, SignatureCardException {

        System.out.println("SELECT AID " + toString(aid));
        // P1=0x00 -> 67:00 (wrong length)
        CommandAPDU cmdAPDU = new CommandAPDU(0x00, 0xa4, 0x04, 0x00, aid, 256);
        System.out.println(" cmd apdu " + toString(cmdAPDU.getBytes()));
        ResponseAPDU resp = channel.transmit(cmdAPDU);
        System.out.println(" -> " + toString(resp.getBytes()) + "\n");

        byte[] keyNumVer = null;

        if (resp.getSW() == 0x9000) {
            byte[] fciBytes = new TLVSequence(resp.getData()).getValue(0x6f);

            TLVSequence fci = new TLVSequence(fciBytes);
            System.out.println("FCI AID " + toString(aid));
            System.out.println(fci);

            TLVSequence proprietary = new TLVSequence(fci.getValue(0xa5));
            System.out.println("proprietary information");
            System.out.println(proprietary);

            keyNumVer = proprietary.getValue(0x54);
            if (keyNumVer == null || keyNumVer.length != 2) {
                throw new SignatureCardException("invalid key number/version: "
                        + toString(keyNumVer));
            }
            return keyNumVer;

        } else {
            throw new SignatureCardException("Failed to read AID: 0x"
                    + Integer.toHexString(resp.getSW()));
        }
    }

    void openSecureChannel(SecretKeySpec k_appl, byte[] cin) throws SignatureCardException, NoSuchAlgorithmException, UnsupportedEncodingException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, CardException, NoSuchProviderException, GeneralSecurityException {
//        function openSecureChannelG3(card, crypto, iccsn, kappl, kid, algo) {

//	if (typeof(kid) == "undefined")
//		kid = 0x81;
//
//	if (typeof(algo) == "undefined")
//		algo = 0x54;
//
//	// Perform mutual authentication procedure
//	GPSystem.trace("Performing mutual authentication");
//
//	var kenc = deriveKENC(crypto, kappl);
//	var kmac = deriveKMAC(crypto, kappl);

        SecretKeySpec k_enc = deriveKENC(k_appl);
        SecretKeySpec k_mac = deriveKMAC(k_appl);

//
//	// Manage SE: Set K_Appl for Mutual Authentication with Session Key establishment
//	var bb = new ByteBuffer("8301", HEX);
//	bb.append(kid);
//	bb.append(0x80);
//	bb.append(0x01);
//	bb.append(algo);
//
//	card.sendApdu(0x00, 0x22, 0x81, 0xA4, bb.toByteString(), [0x9000])
//
//	var rndicc = card.sendApdu(0x00, 0x84, 0x00, 0x00, 0x08, [0x9000]);

        int dfSpecificKeyRef = 1;
        byte[] crt_at = new byte[]{
            (byte) 0x83, (byte) 0x01, (byte) (0x80 | (0x7f & dfSpecificKeyRef)),
            // algorithm id 0x54???
            (byte) 0x80, (byte) 0x01, (byte) 0x54
        };

        System.out.println("MSE SET AT for key agreement");
        CommandAPDU cmdAPDU = new CommandAPDU(0x00, 0x22, 0x81, 0xa4, crt_at);
        System.out.println(" cmd apdu " + toString(cmdAPDU.getBytes()));
        ResponseAPDU resp = channel.transmit(cmdAPDU);
        System.out.println(" -> " + toString(resp.getBytes()) + "\n");

        System.out.println("GET CHALLENGE");
        cmdAPDU = new CommandAPDU(0x00, 0x84, 0x00, 0x00, 0x08);
        System.out.println(" cmd apdu " + toString(cmdAPDU.getBytes()));
        resp = channel.transmit(cmdAPDU);
        System.out.println(" -> " + toString(resp.getBytes()) + "\n");
        
        byte[] rnd_icc = resp.getData();

        if (rnd_icc.length != 8) {
            throw new RuntimeException("invalid RND.ICC: " + toString(rnd_icc));
        }
        System.out.println("RND_ICC: " + toString(rnd_icc));

        if (cin.length != 10) {
            throw new RuntimeException("invalid CIN: " + toString(cin));
        }

        byte[] icc_id = Arrays.copyOfRange(cin, cin .length-8, cin.length);
        System.out.println("ICC_ID:  " + toString(icc_id));

//	var rndifd = crypto.generateRandom(8);
//	var snifd = crypto.generateRandom(8);
//	var kifd = crypto.generateRandom(64);	// 32 -> 64
//	var snicc = iccsn.bytes(2, 8);
//

        Random rand = new Random(System.currentTimeMillis());

        byte[] rnd_ifd = new byte[8];
        rand.nextBytes(rnd_ifd);
        byte[] ifd_id = new byte[8];
        rand.nextBytes(ifd_id);
        byte[] kd_ifd = new byte[64];
        rand.nextBytes(kd_ifd);

//	var plain = rndifd.concat(snifd).concat(rndicc).concat(snicc).concat(kifd);
//	GPSystem.trace("Plain Block  : " + plain);
//	print("Plain Block  : " + plain);
//
//        print("K_enc        : " + kenc.getComponent(Key.DES));
//
//	var cryptogram = crypto.encrypt(kenc, Crypto.DES_CBC, plain, new ByteString("0000000000000000", HEX));
//	GPSystem.trace("Cryptogram   : " + cryptogram);
//	print("Cryptogram   : " + cryptogram);

        Cipher tDES = Cipher.getInstance("3DES/CBC/NoPadding");
        tDES.init(Cipher.ENCRYPT_MODE, k_enc, new IvParameterSpec(ZEROS));

        byte[] sendData = new byte[4*8+64];
        System.arraycopy(rnd_ifd, 0, sendData, 0, 8);
        System.arraycopy(ifd_id, 0, sendData, 8, 8);
        System.arraycopy(rnd_icc, 0, sendData, 16, 8);
        System.arraycopy(icc_id, 0, sendData, 24, 8);
        System.arraycopy(kd_ifd, 0, sendData, 32, 64);

        System.out.println("cryptogram input (" + sendData.length + "byte): "
                + toString(sendData));

//        tDES.update(rnd_ifd); // 8 byte
//        tDES.update(ifd_id);  // 8 byte
//        tDES.update(rnd_icc); // 8 byte
//        tDES.update(icc_id);  // 8 byte
//        tDES.update(kd_ifd);  // 64 byte

        byte[] cryptogram = tDES.doFinal(sendData);
        System.out.println("cryptogram (" + cryptogram.length + "byte): "
                + toString(cryptogram));
    //
    //        print("K_mac        : " + kmac.getComponent(Key.DES));
    //
    //	var mac = crypto.sign(kmac, Crypto.DES_MAC_EMV, cryptogram.pad(Crypto.ISO9797_METHOD_2));
    //	GPSystem.trace("MAC          : " + mac);
    //	print("MAC          : " + mac);


        byte[] mac = RetailCBCMac.retailMac(cryptogram, RetailCBCMac.PADDING.ISO9797_2, "DES", "DESede", k_mac, 8, 8);
        System.out.println("mac (" + mac.length +"byte): " + toString(mac));
//
//
//	var autresp = card.sendApdu(0x00, 0x82, 0x00, 0x00, cryptogram.concat(mac), 0); // 81 -> 00
//
//	if (card.SW != 0x9000) {
//		GPSystem.trace("Mutual authenticate failed with " + card.SW.toString(16) + " \"" + card.SWMSG + "\"");
//		throw new GPError("MutualAuthentication", GPError.CRYPTO_FAILED, 0, "Card did not accept MAC");
//	}
//
//	cryptogram = autresp.bytes(0, 96);	// 64 -> 96
//	mac = autresp.bytes(96, 8); // 64 -> 96

        byte[] c = new byte[cryptogram.length + mac.length];
        System.arraycopy(cryptogram, 0, c, 0, cryptogram.length);
        System.arraycopy(mac, 0, c, cryptogram.length, mac.length);
        System.out.println("MUTUAL AUTHENTICATE (" + c.length + "bytes): " + toString(c));
        cmdAPDU = new CommandAPDU(0x00, 0x82, 0x00, 0x81, c, 256); // 81->00
        System.out.println(" cmd apdu " + toString(cmdAPDU.getBytes()));
        resp = channel.transmit(cmdAPDU);
        System.out.println(" -> " + toString(resp.getBytes()) + "\n");

//
//	if (!crypto.verify(kmac, Crypto.DES_MAC_EMV, cryptogram.pad(Crypto.ISO9797_METHOD_2), mac)) {
//		throw new GPError("MutualAuthentication", GPError.CRYPTO_FAILED, 0, "Card MAC did not verify correctly");
//	}

        System.out.println("ICC Response: " + resp.getData().length + " Bytes");
        System.arraycopy(resp.getData(), 0, cryptogram, 0, 96);
        System.arraycopy(resp.getData(), 96, mac, 0, 8);

        byte[] mac_ = RetailCBCMac.retailMac(cryptogram, RetailCBCMac.PADDING.ISO9797_2, "DES", "DESede", k_mac, 8, 8);

        if (!Arrays.equals(mac, mac_)) {
          throw new SignatureCardException("Failed to authenticate card, invalid MAC " + toString(mac));
        }
        System.out.println("icc MAC verified");

        
//
//	plain = crypto.decrypt(kenc, Crypto.DES_CBC, cryptogram, new ByteString("0000000000000000", HEX));
//	GPSystem.trace("Plain Block  : " + plain);
//
//	if (!plain.bytes(0, 8).equals(rndicc)) {
//		throw new GPError("MutualAuthentication", GPError.CRYPTO_FAILED, 0, "Card response does not contain matching RND.ICC");
//	}
//
//	if (!plain.bytes(16, 8).equals(rndifd)) {
//		throw new GPError("MutualAuthentication", GPError.CRYPTO_FAILED, 0, "Card response does not contain matching RND.IFD");
//	}

        tDES = Cipher.getInstance("3DES/CBC/NoPadding");
        tDES.init(Cipher.DECRYPT_MODE, k_enc, new IvParameterSpec(ZEROS));

        System.out.println("Decrypt cryptogram (" 
                + cryptogram.length + " Bytes) " + toString(cryptogram));

        byte[] plain = tDES.doFinal(cryptogram);

        if (!Arrays.equals(Arrays.copyOfRange(plain, 0, 8), rnd_icc)) {
          throw new SignatureCardException("Failed to authenticate ICC, wrong RND.ICC "
                  + toString(Arrays.copyOfRange(plain, 0, 8)));
        } else if (!Arrays.equals(Arrays.copyOfRange(plain, 16, 24), rnd_ifd)) {
          throw new SignatureCardException("Failed to authenticate ICC, wrong RND.IFD "
                  + toString(Arrays.copyOfRange(plain, 16, 24)));
        }

        System.out.println("successfully verified RND.ICC/IFD");

        //
        //	var kicc = plain.bytes(32, 64);	// 32 -> 64
        byte[] kd_icc = Arrays.copyOfRange(plain, 32, 96);

        channel = secureChannel(kd_icc, kd_ifd);

    }

    SecureChannel secureChannel(byte[] kd_icc, byte[] kd_ifd) throws NoSuchAlgorithmException {

        //	keyinp = kicc.xor(kifd);
        //
        // TDES session key negotiation according to E-Sign K [STARCOS,6.11]?
        System.out.println("derive key input...");
        byte[] kinp = new byte[kd_ifd.length];
        for (int i = 0; i < kd_ifd.length; i++) {
            kinp[i] = (byte) (kd_icc[i] ^ kd_ifd[i]);
        }

        System.out.println("session key negotiation key (key seed) (" + kinp.length + "B): " + toString(kinp));
        //	var hashin = keyinp.concat(new ByteString("00000001", HEX));
        //	var hashres = crypto.digest(Crypto.SHA_256, hashin);
        //	var kencval = hashres.bytes(0, 24);
        //	var kencssc = hashres.bytes(24, 8);
        //
        //	GPSystem.trace("Kenc         : " + kencval);
        //	GPSystem.trace("Kenc SSC     : " + kencssc);
        //	var kenc = new Key();
        //	kenc.setComponent(Key.DES, kencval);
        //
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        sha256.update(kinp);
        sha256.update(new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01});
        byte[] enc_ = sha256.digest();
        SecretKeySpec kenc = new SecretKeySpec(Arrays.copyOfRange(enc_, 0, 24), "3DES");
        byte[] kencssc = Arrays.copyOfRange(enc_, 24, 32);
        System.out.println("session key kenc: " + toString(kenc.getEncoded()));
        System.out.println("send sequence counter SSC_enc: " + toString(kencssc));
        //	var hashin = keyinp.concat(new ByteString("00000002", HEX));
        //	var hashres = crypto.digest(Crypto.SHA_256, hashin);
        //	var kmacval = hashres.bytes(0, 24);
        //	var kmacssc = hashres.bytes(24, 8);
        //
        //	GPSystem.trace("Kmac         : " + kmacval);
        //	GPSystem.trace("Kmac SSC     : " + kmacssc);
        //	var kmac = new Key();
        //	kmac.setComponent(Key.DES, kmacval);

        sha256.update(kinp);
        sha256.update(new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x02});
        enc_ = sha256.digest();
        SecretKeySpec kmac = new SecretKeySpec(Arrays.copyOfRange(enc_, 0, 24), "3DES");
        byte[] kmacssc = Arrays.copyOfRange(enc_, 24, 32);
        System.out.println("session key kmac: " + toString(kmac.getEncoded()));
        System.out.println("send sequence counter SSC_mac: " + toString(kmacssc));
        //
        //	var sc = new IsoSecureChannel(crypto);
        //	sc.setEncKey(kenc);
        //	sc.setMacKey(kmac);
        //
        //	sc.setMACSendSequenceCounter(kmacssc);
        //	sc.setEncryptionSendSequenceCounter(kencssc);
        //	return sc;
        //}

        return new SecureChannel(channel, kenc, kmac, 8, kencssc, kmacssc);
    }

     /**
   * Decodes the given encoded EC PublicKey according to the Octet-String-to-Point conversion
   * of ANSI X9.62 (1998), section 4.3.7.
   * <p>
   * This method is called on the client side to decode the public server key
   * contained in an ECDH ServerKeyExchange message received from the server.
   *
   * @param ecPoint the (client) public key ECPoint, encoded according to
   *                ANSI X9.62 (1998), section 4.3.6
   * @param oid the oid of the curve
   *
   * @return the decoded public EC key
   *
   * @exception Exception if an error occurs when decoding the key
   */
  public static ECPublicKey decodeECPublicKey(byte[] ecPoint, String oid) throws ECCException {

    ECCParameterFactory pFac = ECCParameterFactory.getInstance();
    ECCParameterSpec spec = pFac.getParameterByOID(oid);

    // Now, we need an instance of elliptic curve factory
    ECGroupFactory gfac = ECGroupFactory.getInstance();

    BigInteger r = spec.getR();

    FieldElement aElement = spec.getA();
    FieldElement bElement = spec.getB();

    // Now we get a curve of the form y^2 = x^3 + ax + b having order r from the
    // ECGroupFactory. We assume that the curve is non-singular.
    EllipticCurve ec = gfac.getCurve(aElement, bElement, r, CoordinateTypes.AFFINE_COORDINATES);

    AffineCoordinate coord = PointFormatter.getInstance().getPointCodec().decodePoint(ecPoint, ec);

    // With these coordinates, we can construct a point on the curve.
    ECPoint point = ec.newPoint(coord);

    ECDSAParameter params = new ECDSAParameter(spec);

    return new ECPublicKey(params, point);

  }

  public static void main(String[] args) {

    try {
      Activation test = new Activation();
      test.setUp();
      test.activate();
//      test.testMACProtection();
//      test.testENCProtection();
    } catch (Exception e) {
        e.printStackTrace();
    }
  }


/*
  static final byte[] kicc = new byte[] { (byte)0xB3, (byte)0xB1, (byte)0x0A, (byte)0x87, (byte)0x84, (byte)0xEC, (byte)0x26, (byte)0xE7, (byte)0x90, (byte)0xA1, (byte)0x14, (byte)0xFC, (byte)0x1A, (byte)0xA9, (byte)0x40, (byte)0xF9, (byte)0x7E, (byte)0xE9, (byte)0xAC, (byte)0x69, (byte)0x6D, (byte)0x64, (byte)0xAF, (byte)0xD8, (byte)0x24, (byte)0xCA, (byte)0x6A, (byte)0xB7, (byte)0x30, (byte)0x49, (byte)0x68, (byte)0xFE, (byte)0xFD, (byte)0x5A, (byte)0xC6, (byte)0x58, (byte)0xAD, (byte)0x33, (byte)0xC1, (byte)0xE4, (byte)0xDE, (byte)0x16, (byte)0xC0, (byte)0x06, (byte)0x66, (byte)0xA3, (byte)0x1E, (byte)0x86, (byte)0xEF, (byte)0xBF, (byte)0xAA, (byte)0x3B, (byte)0x4B, (byte)0xCA, (byte)0x47, (byte)0x81, (byte)0x33, (byte)0xE1, (byte)0x67, (byte)0x6A, (byte)0x69, (byte)0xA5, (byte)0x4E, (byte)0xB4 };
  static final byte[] kifd = new byte[] { (byte)0xB9, (byte)0xFD, (byte)0x50, (byte)0xF3, (byte)0x25, (byte)0x92, (byte)0xE9, (byte)0xF9, (byte)0x7C, (byte)0xF4, (byte)0x8F, (byte)0xEF, (byte)0xC4, (byte)0x8E, (byte)0x93, (byte)0x8F, (byte)0xC6, (byte)0x86, (byte)0x40, (byte)0xAF, (byte)0x4F, (byte)0x3E, (byte)0xFF, (byte)0x76, (byte)0xDE, (byte)0x1B, (byte)0xCE, (byte)0x87, (byte)0x8A, (byte)0x19, (byte)0xF9, (byte)0x2F, (byte)0xF9, (byte)0x94, (byte)0x9A, (byte)0x63, (byte)0x4E, (byte)0xED, (byte)0x84, (byte)0xDE, (byte)0x5B, (byte)0x47, (byte)0x53, (byte)0x12, (byte)0xBC, (byte)0xD6, (byte)0x24, (byte)0x1C, (byte)0xE0, (byte)0xC4, (byte)0xB1, (byte)0x83, (byte)0x24, (byte)0x33, (byte)0x11, (byte)0x49, (byte)0x51, (byte)0x3E, (byte)0xDC, (byte)0xB4, (byte)0x3F, (byte)0x7B, (byte)0xF2, (byte)0x71 };
  static final byte[] keyinp = new byte[] { (byte)0x0A, (byte)0x4C, (byte)0x5A, (byte)0x74, (byte)0xA1, (byte)0x7E, (byte)0xCF, (byte)0x1E, (byte)0xEC, (byte)0x55, (byte)0x9B, (byte)0x13, (byte)0xDE, (byte)0x27, (byte)0xD3, (byte)0x76, (byte)0xB8, (byte)0x6F, (byte)0xEC, (byte)0xC6, (byte)0x22, (byte)0x5A, (byte)0x50, (byte)0xAE, (byte)0xFA, (byte)0xD1, (byte)0xA4, (byte)0x30, (byte)0xBA, (byte)0x50, (byte)0x91, (byte)0xD1, (byte)0x04, (byte)0xCE, (byte)0x5C, (byte)0x3B, (byte)0xE3, (byte)0xDE, (byte)0x45, (byte)0x3A, (byte)0x85, (byte)0x51, (byte)0x93, (byte)0x14, (byte)0xDA, (byte)0x75, (byte)0x3A, (byte)0x9A, (byte)0x0F, (byte)0x7B, (byte)0x1B, (byte)0xB8, (byte)0x6F, (byte)0xF9, (byte)0x56, (byte)0xC8, (byte)0x62, (byte)0xDF, (byte)0xBB, (byte)0xDE, (byte)0x56, (byte)0xDE, (byte)0xBC, (byte)0xC5 };
  static final byte[] hashinp = new byte[] { (byte)0x0A, (byte)0x4C, (byte)0x5A, (byte)0x74, (byte)0xA1, (byte)0x7E, (byte)0xCF, (byte)0x1E, (byte)0xEC, (byte)0x55, (byte)0x9B, (byte)0x13, (byte)0xDE, (byte)0x27, (byte)0xD3, (byte)0x76, (byte)0xB8, (byte)0x6F, (byte)0xEC, (byte)0xC6, (byte)0x22, (byte)0x5A, (byte)0x50, (byte)0xAE, (byte)0xFA, (byte)0xD1, (byte)0xA4, (byte)0x30, (byte)0xBA, (byte)0x50, (byte)0x91, (byte)0xD1, (byte)0x04, (byte)0xCE, (byte)0x5C, (byte)0x3B, (byte)0xE3, (byte)0xDE, (byte)0x45, (byte)0x3A, (byte)0x85, (byte)0x51, (byte)0x93, (byte)0x14, (byte)0xDA, (byte)0x75, (byte)0x3A, (byte)0x9A, (byte)0x0F, (byte)0x7B, (byte)0x1B, (byte)0xB8, (byte)0x6F, (byte)0xF9, (byte)0x56, (byte)0xC8, (byte)0x62, (byte)0xDF, (byte)0xBB, (byte)0xDE, (byte)0x56, (byte)0xDE, (byte)0xBC, (byte)0xC5, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x01 };
//hashres      : A6 85 3E A7 A2 9E 2D 5C 00 CC 8D 0A E5 89 9E DF 6A 0A AD 65 47 C6 12 A2 19 D8 67 49 D9 F3 A0 6E
*
 */


  /*
   *
   Plain Block  : 3E 43 8A 71 9E 25 1E 87 33 1F D2 4A A7 79 12 18 5B 3D B2 93 3F E8 CE 7C 00 00 00 23 00 79 05 03 4A B5 FB 44 33 01 64 57 80 21 B5 B1 B6 FF D9 C3 5C 10 FB 4A A8 88 B7 F8 79 19 D6 3A 3E 5D F4 D7 1B CA 48 82 E9 9B 6E C7 4F AB 34 CC 0D AA 43 25 21 29 D0 78 31 A3 F2 26 F7 EF 52 70 F9 2E 84 2B
K_enc        : C7 61 DA 43 FD 89 89 9E 92 54 AD 08 5D 67 4F 8C 38 DC 32 7C A4 FB 67 07
Cryptogram   : B1 0C 9A F3 6D FA B0 70 D8 4D 6A 0B 3C E9 8E 5E 50 D0 2A 82 1E CD 70 77 6F CC D4 E8 4D 0E A3 E6 B8 87 2E 31 0F B1 0B 42 5F EB C6 36 B0 EC 18 86 94 0D 5B 67 6C 1A 96 8F C7 2B 8E 4B 85 2A 91 63 C9 E4 66 43 42 D9 55 FF 44 5C C9 DE A6 44 D3 46 37 DA 47 02 A3 63 BA E7 4D CB 52 64 5D F6 B4 94
K_mac        : 02 E5 8A D6 1A DA 98 EA E9 0B C4 68 0B C8 29 A4 31 26 F1 16 91 46 7F 97
MAC          : 6B 51 02 0A DD 8F 10 55
kicc         : 56 B8 10 10 B9 BE 09 34 AA DA 1A 0A 64 24 B7 35 5A 6C A0 5C 75 9F A8 B1 B6 1F 4E 5E 16 15 37 51 EA 9F 0B DE CC FC 31 0B 21 40 28 B5 75 63 86 79 6E 64 C8 14 4A C6 05 1E C1 FB 7E 86 EF 7F B9 9C
kifd         : 4A B5 FB 44 33 01 64 57 80 21 B5 B1 B6 FF D9 C3 5C 10 FB 4A A8 88 B7 F8 79 19 D6 3A 3E 5D F4 D7 1B CA 48 82 E9 9B 6E C7 4F AB 34 CC 0D AA 43 25 21 29 D0 78 31 A3 F2 26 F7 EF 52 70 F9 2E 84 2B
keyinp         : 1C 0D EB 54 8A BF 6D 63 2A FB AF BB D2 DB 6E F6 06 7C 5B 16 DD 17 1F 49 CF 06 98 64 28 48 C3 86 F1 55 43 5C 25 67 5F CC 6E EB 1C 79 78 C9 C5 5C 4F 4D 18 6C 7B 65 F7 38 36 14 2C F6 16 51 3D B7
hashinp      : 1C 0D EB 54 8A BF 6D 63 2A FB AF BB D2 DB 6E F6 06 7C 5B 16 DD 17 1F 49 CF 06 98 64 28 48 C3 86 F1 55 43 5C 25 67 5F CC 6E EB 1C 79 78 C9 C5 5C 4F 4D 18 6C 7B 65 F7 38 36 14 2C F6 16 51 3D B7 00 00 00 01
hashres      : 78 8B 53 B9 E0 3B 9B 87 11 42 4A 13 2D 03 7B AA 0E 9E 97 43 4E 4E FC 5A F1 BE 47 51 F3 EC C8 04
hashinp      : 1C 0D EB 54 8A BF 6D 63 2A FB AF BB D2 DB 6E F6 06 7C 5B 16 DD 17 1F 49 CF 06 98 64 28 48 C3 86 F1 55 43 5C 25 67 5F CC 6E EB 1C 79 78 C9 C5 5C 4F 4D 18 6C 7B 65 F7 38 36 14 2C F6 16 51 3D B7 00 00 00 02
hashres      : 4D BF FC AD 67 94 55 F9 7F DD 47 30 C7 74 B1 7D CF B8 B3 74 93 87 0F 21 ED 72 96 5D D6 04 66 58
   */


    /**
     * sniffed values (cf. activate-800400...790503.log lines 95-101, hashres = sk2 | ssc2
     * and apdu_ from activate activate-800400...790503-apdu.log lines 79-83, corresponding challenge)
     */
     static final byte[] kicc = new byte[] { (byte)0x56, (byte)0xB8, (byte)0x10, (byte)0x10, (byte)0xB9, (byte)0xBE, (byte)0x09, (byte)0x34, (byte)0xAA, (byte)0xDA, (byte)0x1A, (byte)0x0A, (byte)0x64, (byte)0x24, (byte)0xB7, (byte)0x35, (byte)0x5A, (byte)0x6C, (byte)0xA0, (byte)0x5C, (byte)0x75, (byte)0x9F, (byte)0xA8, (byte)0xB1, (byte)0xB6, (byte)0x1F, (byte)0x4E, (byte)0x5E, (byte)0x16, (byte)0x15, (byte)0x37, (byte)0x51, (byte)0xEA, (byte)0x9F, (byte)0x0B, (byte)0xDE, (byte)0xCC, (byte)0xFC, (byte)0x31, (byte)0x0B, (byte)0x21, (byte)0x40, (byte)0x28, (byte)0xB5, (byte)0x75, (byte)0x63, (byte)0x86, (byte)0x79, (byte)0x6E, (byte)0x64, (byte)0xC8, (byte)0x14, (byte)0x4A, (byte)0xC6, (byte)0x05, (byte)0x1E, (byte)0xC1, (byte)0xFB, (byte)0x7E, (byte)0x86, (byte)0xEF, (byte)0x7F, (byte)0xB9, (byte)0x9C };
     static final byte[] kifd = new byte[] { (byte)0x4A, (byte)0xB5, (byte)0xFB, (byte)0x44, (byte)0x33, (byte)0x01, (byte)0x64, (byte)0x57, (byte)0x80, (byte)0x21, (byte)0xB5, (byte)0xB1, (byte)0xB6, (byte)0xFF, (byte)0xD9, (byte)0xC3, (byte)0x5C, (byte)0x10, (byte)0xFB, (byte)0x4A, (byte)0xA8, (byte)0x88, (byte)0xB7, (byte)0xF8, (byte)0x79, (byte)0x19, (byte)0xD6, (byte)0x3A, (byte)0x3E, (byte)0x5D, (byte)0xF4, (byte)0xD7, (byte)0x1B, (byte)0xCA, (byte)0x48, (byte)0x82, (byte)0xE9, (byte)0x9B, (byte)0x6E, (byte)0xC7, (byte)0x4F, (byte)0xAB, (byte)0x34, (byte)0xCC, (byte)0x0D, (byte)0xAA, (byte)0x43, (byte)0x25, (byte)0x21, (byte)0x29, (byte)0xD0, (byte)0x78, (byte)0x31, (byte)0xA3, (byte)0xF2, (byte)0x26, (byte)0xF7, (byte)0xEF, (byte)0x52, (byte)0x70, (byte)0xF9, (byte)0x2E, (byte)0x84, (byte)0x2B };
     static final byte[] sk2 = new byte[] { (byte)0x4D, (byte)0xBF, (byte)0xFC, (byte)0xAD, (byte)0x67, (byte)0x94, (byte)0x55, (byte)0xF9, (byte)0x7F, (byte)0xDD, (byte)0x47, (byte)0x30, (byte)0xC7, (byte)0x74, (byte)0xB1, (byte)0x7D, (byte)0xCF, (byte)0xB8, (byte)0xB3, (byte)0x74, (byte)0x93, (byte)0x87, (byte)0x0F, (byte)0x21 };
     static final byte[] ssc2 = new byte[] { (byte)0xED, (byte)0x72, (byte)0x96, (byte)0x5D, (byte)0xD6, (byte)0x04, (byte)0x66, (byte)0x58 };
     static final byte[] apdu_ = new byte[] { (byte)0x0C, (byte)0xB0, (byte)0x00, (byte)0x00, (byte)0x0D, (byte)0x97, (byte)0x01, (byte)0xDF, (byte)0x8E, (byte)0x08, (byte)0xCE, (byte)0xBD, (byte)0xFA, (byte)0xEC, (byte)0xFA, (byte)0xB2, (byte)0xC5, (byte)0xD7, (byte)0x00 };


    void testMACProtection() throws IllegalArgumentException, InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException, GeneralSecurityException {

        SecureChannel channel = secureChannel(kicc, kifd);

        if (!Arrays.equals(channel.kmac.getEncoded(), sk2)) {
            System.out.println("derived session key kmac does not match");
            System.out.println("kmac orig:    " + toString(sk2));
            System.out.println("kmac test:    " + toString(channel.kmac.getEncoded()));
        }
        if (!Arrays.equals(channel.kmacssc, ssc2)) {
            System.out.println("derived send sequence counter kmacssc does not match");
            System.out.println("kmacssc orig: " + toString(ssc2));
            System.out.println("kmacssc test: " + toString(channel.kmacssc));
        }
        
        
				byte[] case2apdu = channel.protectNoCommandData(new byte[] { (byte)0x00, (byte)0xb0, (byte)0x00, (byte)0x00, (byte)0xdf });

				System.out.println("apdu orig:   " + toString(apdu_));
				System.out.println("apdu test:   " + toString(case2apdu));

				if (!Arrays.equals(apdu_, case2apdu)) {
						System.out.println(" ******************************* test failed ");
				}
				
				byte[] case3apdu = channel.protectCommandData(new byte[] { (byte)0x00, (byte)0x10, (byte)0x20, (byte)0x30, (byte)0x04, (byte)0x41, (byte)0x042, (byte)0x43, (byte)0x44 }, false);
				System.out.println("apdu test:   " + toString(case3apdu));
				
				case3apdu = channel.protectCommandData(new byte[] { (byte)0x00, (byte)0x10, (byte)0x20, (byte)0x30, (byte)0x04, (byte)0x41, (byte)0x042, (byte)0x43, (byte)0x44, (byte)0x80 }, false);
				System.out.println("apdu test:   " + toString(case3apdu));


    }


		/**
     * sniffed values (cf. activate-800400...790503.log lines 125-136, hashres = kenc | kencssc
     * and apdu_ from activate activate-800400...790503-apdu.log lines 96(corresponding challenge)
     */

		static final byte[] kenc = new byte[] { (byte)0x15, (byte)0xC7, (byte)0x63, (byte)0x2B, (byte)0xB7, (byte)0xBB, (byte)0x00, (byte)0x53, (byte)0xFA, (byte)0x3F, (byte)0xAC, (byte)0x2B, (byte)0x9B, (byte)0x59, (byte)0xCF, (byte)0xC3, (byte)0x04, (byte)0x50, (byte)0x51, (byte)0xE8, (byte)0x62, (byte)0xE8, (byte)0xD3, (byte)0xEE };
    static final byte[] kencssc = new byte[] { (byte)0x62, (byte)0x82, (byte)0xA5, (byte)0xBA, (byte)0xB2, (byte)0xDD, (byte)0x25, (byte)0x50 };
		static final byte[] kmac = new byte[] { (byte)0xD4, (byte)0x0B, (byte)0x7F, (byte)0x7E, (byte)0x9B, (byte)0x45, (byte)0x8C, (byte)0x39, (byte)0x80, (byte)0x8E, (byte)0x5B, (byte)0x2B, (byte)0x63, (byte)0x4B, (byte)0x0F, (byte)0xC4, (byte)0x0D, (byte)0xEE, (byte)0xB7, (byte)0x6D, (byte)0xC9, (byte)0xC1, (byte)0xFD, (byte)0xE9 };
    static final byte[] kmacssc = new byte[] { (byte)0x3E, (byte)0x09, (byte)0x6C, (byte)0x86, (byte)0xB4, (byte)0xBB, (byte)0x9F, (byte)0x30 };
    static final byte[] apduEnc_ = new byte[] { (byte) 0x0C, (byte)0xE4, (byte)0x02, (byte)0x00, (byte)0x15, (byte)0x87, (byte)0x09, (byte)0x01, (byte)0xB9, (byte)0x5A, (byte)0xCA, (byte)0xA5, (byte)0x69, (byte)0x55, (byte)0x46, (byte)0x56, (byte)0x8E, (byte)0x08, (byte)0x40, (byte)0x7D, (byte)0xEA, (byte)0xC4, (byte)0x30, (byte)0xE6, (byte)0xCF, (byte)0x94, (byte)0x00 };

		public void testENCProtection() throws GeneralSecurityException {

			SecureChannel sc = new SecureChannel(channel, 
							new SecretKeySpec(kenc, "3DES"), new SecretKeySpec(kmac, "3DES"),
							8, kencssc, kmacssc);

			byte[] apdu = new byte[] { (byte) 0x00, (byte) 0xe4, (byte) 0x02, (byte) 0x00, (byte) 0x02, (byte) 0xc0, (byte) 0x00 };
		  System.out.println("apdu test:    " + toString(apdu));
			byte[] apduTest = sc.protectCommandData(apdu, true);
			System.out.println("encrypt:      " + toString(apduTest));

			if (!Arrays.equals(apduEnc_, apduTest)) {
				System.out.println("************************************************** ERRROR ");
			}

		}

		public static final byte[] SM_RESP_APDU = new byte[] {
			(byte)0x81, (byte)0x81, (byte)0xac, (byte)0xa8, (byte)0x82, (byte)0x00, (byte)0xa8, (byte)0xb6, (byte)0x16, (byte)0x83, (byte)0x14, (byte)0x80, (byte)0x04, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x23, (byte)0x00, (byte)0x79, (byte)0x05, (byte)0x03, (byte)0xd0, (byte)0x40, (byte)0x00, (byte)0x00, (byte)0x17, (byte)0x00, (byte)0x12, (byte)0x01, (byte)0x02, (byte)0x00, (byte)0x7f,
			(byte)0x49, (byte)0x82, (byte)0x00, (byte)0x44, (byte)0x86, (byte)0x40, (byte)0xd4, (byte)0x7c, (byte)0x12, (byte)0x55, (byte)0xe4, (byte)0x7b, (byte)0x0c, (byte)0x7d, (byte)0x4e, (byte)0xbb, (byte)0x17, (byte)0xe4, (byte)0x83, (byte)0xe5, (byte)0x3d, (byte)0x56, (byte)0xdf, (byte)0x45, (byte)0x7e, (byte)0x99, (byte)0xcb, (byte)0xcc, (byte)0x93, (byte)0xd2, (byte)0xc2, (byte)0x5e,
			(byte)0x4d, (byte)0x91, (byte)0x27, (byte)0x6e, (byte)0x8b, (byte)0xe7, (byte)0x6d, (byte)0x23, (byte)0x53, (byte)0xf6, (byte)0xab, (byte)0x2e, (byte)0xa6, (byte)0xdd, (byte)0xb7, (byte)0x1c, (byte)0x68, (byte)0xfb, (byte)0x59, (byte)0xcd, (byte)0xd0, (byte)0x45, (byte)0x2b, (byte)0x10, (byte)0x0e, (byte)0x27, (byte)0x00, (byte)0x6e, (byte)0xaa, (byte)0x1c, (byte)0x49, (byte)0x90,
			(byte)0x67, (byte)0xa9, (byte)0x9f, (byte)0x59, (byte)0xd1, (byte)0x97, (byte)0xc1, (byte)0x00, (byte)0xc0, (byte)0x01, (byte)0x80, (byte)0x9e, (byte)0x82, (byte)0x00, (byte)0x40, (byte)0xde, (byte)0x22, (byte)0x37, (byte)0x4c, (byte)0x41, (byte)0xe0, (byte)0xf7, (byte)0x94, (byte)0x9a, (byte)0x5a, (byte)0xe4, (byte)0x76, (byte)0xb8, (byte)0x9b, (byte)0x00, (byte)0xb8, (byte)0x23,
			(byte)0x7c, (byte)0xe9, (byte)0x4a, (byte)0x92, (byte)0xfd, (byte)0xb0, (byte)0xfb, (byte)0x25, (byte)0x4a, (byte)0xa7, (byte)0x0e, (byte)0x4d, (byte)0x5f, (byte)0x6f, (byte)0x3d, (byte)0x3a, (byte)0x54, (byte)0x28, (byte)0xf8, (byte)0x90, (byte)0xa1, (byte)0x7d, (byte)0x60, (byte)0x28, (byte)0xf8, (byte)0x72, (byte)0xb7, (byte)0x0f, (byte)0x9f, (byte)0xa6, (byte)0xa8, (byte)0x53,
			(byte)0x15, (byte)0xf2, (byte)0x9f, (byte)0x88, (byte)0x37, (byte)0xd4, (byte)0x6b, (byte)0x77, (byte)0xf7, (byte)0x69, (byte)0xc1, (byte)0xb9, (byte)0xe7, (byte)0x2a, (byte)0x43, (byte)0x99, (byte)0x02, (byte)0x90, (byte)0x00, (byte)0x8e, (byte)0x08, (byte)0xbc, (byte)0xdc, (byte)0x25, (byte)0x67, (byte)0x5e, (byte)0xfd, (byte)0x6c, (byte)0xba }; //, (byte)0x90, (byte)0x00};

		
}
