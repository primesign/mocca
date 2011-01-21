/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
import java.security.PublicKey;
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

    private final static byte[] ZEROS = {
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
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

//        openSecureChannel(k_qsig, cin);

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

//            ECPublicKey ecPuK = decodeECPublicKey(Q, new String(oid));
//            System.out.println("PuK: " + ecPuK);

        } catch (CodingException ex) {
            throw new SignatureCardException("failed to read EF.PuK", ex);
        }
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


        byte[] mac = RetailCBCMac.retailMac(cryptogram, "DES", "DESede", k_mac, 8, 8);
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

        byte[] mac_ = RetailCBCMac.retailMac(cryptogram, "DES", "DESede", k_mac, 8, 8);

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
        System.out.println("session key negotiation key (key seed): " + toString(kinp));
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

        return new SecureChannel(channel, kenc, kmac, kencssc, kmacssc);
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
