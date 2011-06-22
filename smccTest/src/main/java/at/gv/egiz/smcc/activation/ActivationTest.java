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
import at.gv.egiz.smcc.util.ISO7816Utils;
import at.gv.egiz.smcc.util.TLVSequence;
import iaik.asn1.ASN1;
import iaik.asn1.ASN1Object;
import iaik.asn1.CodingException;
import iaik.asn1.DerCoder;
import iaik.security.provider.IAIK;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.Charset;
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
import javax.crypto.Mac;
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
public class ActivationTest {

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

    public void getSVNr() throws CardException {
        CommandAPDU cmdAPDU;
        ResponseAPDU resp;

        System.out.println("SELECT MF");
        cmdAPDU = new CommandAPDU(0x00, 0xA4, 0x00, 0x0c, new byte[]{(byte) 0x3f, (byte) 0x00});
        System.out.println(" cmd apdu " + toString(cmdAPDU.getBytes()));
        resp = channel.transmit(cmdAPDU);
        System.out.println(" -> " + toString(resp.getBytes()) + "\n");

        System.out.println("SELECT DF_SVPersonendaten");
        // P1=0x00 -> 6a:80 (incorrect cmd data)
        // no Le -> 67:00 (wrong length)
        cmdAPDU = new CommandAPDU(0x00, 0xa4, 0x01, 0x00, new byte[]{(byte) 0x3f, (byte) 0x01}, 256);
        System.out.println(" cmd apdu " + toString(cmdAPDU.getBytes()));
        resp = channel.transmit(cmdAPDU);
        System.out.println(" -> " + toString(resp.getBytes()) + "\n");

        if (resp.getSW() == 0x9000) {
            System.out.println(new TLVSequence(new TLVSequence(resp.getData()).getValue(0x6f)));
        }

        System.out.println("SELECT EF.Grunddaten");
        // P2=00, Le present -> 67:00
        cmdAPDU = new CommandAPDU(0x00, 0xa4, 0x02, 0x0c, new byte[]{(byte) 0xef, (byte) 0x01});
        System.out.println(" cmd apdu " + toString(cmdAPDU.getBytes()));
        resp = channel.transmit(cmdAPDU);
        System.out.println(" -> " + toString(resp.getBytes()) + "\n");

        System.out.println("READ EF.Grunddaten");
        // 7.2.2 (case2), offset=0
        cmdAPDU = new CommandAPDU(0x00, 0xb0, 0x00, 0x00, 256);
        System.out.println(" cmd apdu " + toString(cmdAPDU.getBytes()));
        resp = channel.transmit(cmdAPDU);
        System.out.println(" -> " + toString(resp.getBytes()) + "\n");

        try {
            ASN1Object grunddaten = DerCoder.decode(resp.getData());
            System.out.println(ASN1.print(grunddaten));

            // ensure OID 1.2.40.0.10.1.4.1.1
            System.out.println("oid: " + (String) grunddaten.getComponentAt(0).getComponentAt(0).getValue());
            System.out.println("svNr: " + (String) grunddaten.getComponentAt(0).getComponentAt(1).getComponentAt(0).getValue());
        } catch (CodingException ex) {
            ex.printStackTrace();
        }
    }

    public byte[] getCIN() throws CardException, SignatureCardException {
        CommandAPDU cmdAPDU;
        ResponseAPDU resp;

        System.out.println("SELECT MF");
        cmdAPDU = new CommandAPDU(0x00, 0xA4, 0x00, 0x0c, new byte[]{(byte) 0x3F, (byte) 0x00});
        System.out.println(" cmd apdu " + toString(cmdAPDU.getBytes()));
        resp = channel.transmit(cmdAPDU);
        System.out.println(" -> " + toString(resp.getBytes()) + "\n");

//        System.out.println("SELECT EF.GDO");
//        // alternative: read with SFI=02: 00 b0 82 00 fa
//        // P1=0x00 -> 6a:80 (incorrect cmd data)
//        // no Le -> 67:00 (wrong length)
//        cmdAPDU = new CommandAPDU(0x00, 0xa4, 0x02, 0x00, new byte[] {(byte) 0x2f, (byte) 0x02}, 256);
//        System.out.println(" cmd apdu " + toString(cmdAPDU.getBytes()));
//        resp = channel.transmit(cmdAPDU);
//        System.out.println(" -> " + toString(resp.getBytes()) + "\n");
//
//        System.out.println("READ EF.GDO");
//        // 7.2.2 (case2), offset=0
//        cmdAPDU = new CommandAPDU(0x00, 0xb0, 0x00, 0x00, 256);
//        System.out.println(" cmd apdu " + toString(cmdAPDU.getBytes()));
//        resp = channel.transmit(cmdAPDU);
//        System.out.println(" -> " + toString(resp.getBytes()) + "\n");

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

    public ASN1Object getCIO_PrK_SS() throws CardException, CodingException {
        CommandAPDU cmdAPDU;
        ResponseAPDU resp;

        System.out.println("SELECT MF");
        cmdAPDU = new CommandAPDU(0x00, 0xA4, 0x00, 0x0c, new byte[]{(byte) 0x3f, (byte) 0x00});
        System.out.println(" cmd apdu " + toString(cmdAPDU.getBytes()));
        resp = channel.transmit(cmdAPDU);
        System.out.println(" -> " + toString(resp.getBytes()) + "\n");

        System.out.println("SELECT DF.QualifizierteSignatur");
        // P1=0x00 -> 67:00 (wrong length)
        cmdAPDU = new CommandAPDU(0x00, 0xa4, 0x04, 0x00, new byte[]{(byte) 0xd0, (byte) 0x40, (byte) 0x00, (byte) 0x00, (byte) 0x17, (byte) 0x00, (byte) 0x12, (byte) 0x01}, 256);
        System.out.println(" cmd apdu " + toString(cmdAPDU.getBytes()));
        resp = channel.transmit(cmdAPDU);
        System.out.println(" -> " + toString(resp.getBytes()) + "\n");

        System.out.println("SELECT EF.PrKD");
        // P1=0x00 -> 67:00 (wrong length)
        cmdAPDU = new CommandAPDU(0x00, 0xa4, 0x02, 0x04, new byte[]{(byte) 0x50, (byte) 0x35}, 256);
        System.out.println(" cmd apdu " + toString(cmdAPDU.getBytes()));
        resp = channel.transmit(cmdAPDU);
        System.out.println(" -> " + toString(resp.getBytes()) + "\n");

        System.out.println("READ EF.PrKD");
        cmdAPDU = new CommandAPDU(0x00, 0xb0, 0x00, 0x00, 256);
        System.out.println(" cmd apdu " + toString(cmdAPDU.getBytes()));
        resp = channel.transmit(cmdAPDU);
        System.out.println(" -> " + toString(resp.getBytes()) + "\n");

        ASN1Object efPrK_QS = DerCoder.decode(resp.getData());
        System.out.println(ASN1.print(efPrK_QS));

        String label = (String) efPrK_QS.getComponentAt(0).getComponentAt(0).getValue();
        System.out.println("PrK_QS label: " + label);
        byte[] authObjId = (byte[]) efPrK_QS.getComponentAt(0).getComponentAt(2).getValue();
        System.out.println("PrK_QS authObjId: " + toString(authObjId));
        byte[] id = (byte[]) efPrK_QS.getComponentAt(1).getComponentAt(0).getValue();
        System.out.println("PrK_QS Id: " + toString(id));
        BigInteger keyRef = (BigInteger) efPrK_QS.getComponentAt(1).getComponentAt(4).getValue();
        System.out.println("PrK_QS keyRef: 0x" + Integer.toHexString(keyRef.intValue()) + "\n");

        return efPrK_QS;
    }

    public void getPuK_SicSig(byte[] cin) throws CardException, SignatureCardException, CodingException, InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        CommandAPDU cmdAPDU;
        ResponseAPDU resp;

        System.out.println("SELECT MF");
        cmdAPDU = new CommandAPDU(0x00, 0xA4, 0x00, 0x0c, new byte[]{(byte) 0x3f, (byte) 0x00});
        System.out.println(" cmd apdu " + toString(cmdAPDU.getBytes()));
        resp = channel.transmit(cmdAPDU);
        System.out.println(" -> " + toString(resp.getBytes()) + "\n");

        System.out.println("SELECT DF.QualifizierteSignatur");
        // P1=0x00 -> 67:00 (wrong length)
        cmdAPDU = new CommandAPDU(0x00, 0xa4, 0x04, 0x00, new byte[]{(byte) 0xd0, (byte) 0x40, (byte) 0x00, (byte) 0x00, (byte) 0x17, (byte) 0x00, (byte) 0x12, (byte) 0x01}, 256);
        System.out.println(" cmd apdu " + toString(cmdAPDU.getBytes()));
        resp = channel.transmit(cmdAPDU);
        System.out.println(" -> " + toString(resp.getBytes()) + "\n");

        byte[] keyNumVer = null;

        if (resp.getSW() == 0x9000) {
            byte[] fciBytes = new TLVSequence(resp.getData()).getValue(ISO7816Utils.TAG_FCI);

            TLVSequence fci = new TLVSequence(fciBytes);
            System.out.println("FCI DF.QualifizierteSignatur");
            System.out.println(fci);

            TLVSequence proprietary = new TLVSequence(fci.getValue(0xa5));
            System.out.println("proprietary information");
            System.out.println(proprietary);

            keyNumVer = proprietary.getValue(0x54);
            if (keyNumVer == null || keyNumVer.length != 2) {
                throw new SignatureCardException("invalid key number/version: " + toString(keyNumVer));
            }

            System.out.println("key number: 0x" + Byte.toString(keyNumVer[0]));
            System.out.println("key version: 0x" + Byte.toString(keyNumVer[1]));
        } else {
            throw new SignatureCardException("Failed to read DF.QualifizierteSignatur: 0x" + Integer.toHexString(resp.getSW()));
        }

        SecretKeySpec kp_mk_ss = getKP_MK_SS();

        SecretKeySpec kp_ss = deriveApplicationKey(kp_mk_ss, cin, keyNumVer);

        int dfSpecificKeyRef = 1;
        byte[] crt_at = new byte[]{
            (byte) 0x83, (byte) 0x01, (byte) (0x80 | (0x7f & dfSpecificKeyRef)),
            // algorithm id 0x54???
            (byte) 0x80, (byte) 0x01, (byte) 0x54
        };

        System.out.println("MSE SET AT for key agreement");
        cmdAPDU = new CommandAPDU(0x00, 0x22, 0x81, 0xa4, crt_at);
        System.out.println(" cmd apdu " + toString(cmdAPDU.getBytes()));
        resp = channel.transmit(cmdAPDU);
        System.out.println(" -> " + toString(resp.getBytes()) + "\n");

        System.out.println("GET CHALLENGE");
        cmdAPDU = new CommandAPDU(0x00, 0x84, 0x00, 0x00, 0x08);
        System.out.println(" cmd apdu " + toString(cmdAPDU.getBytes()));
        resp = channel.transmit(cmdAPDU);
        System.out.println(" -> " + toString(resp.getBytes()) + "\n");
        byte[] rnd_icc = resp.getData();
        
        SecretKeySpec k_enc = deriveKey(kp_ss, "3DES/CBC/NoPadding", iv, K_ENC);
        SecretKeySpec k_mac = deriveKey(kp_ss, "3DES/CBC/NoPadding", iv, K_MAC);

        mutualAuth(cin, rnd_icc, k_enc, k_mac);

    }

    private SecretKeySpec getKP_MK_SS() {
        byte[] kp_mk_ss_Bytes = new byte[]{(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09, (byte) 0x0A, (byte) 0x0B, (byte) 0x0C, (byte) 0x0D, (byte) 0x0E, (byte) 0x0F, (byte) 0x10, (byte) 0x11, (byte) 0x12, (byte) 0x13, (byte) 0x14, (byte) 0x15, (byte) 0x16, (byte) 0x17, (byte) 0x18};
        SecretKeySpec kp_mk_ss = new SecretKeySpec(kp_mk_ss_Bytes, "3DES");
        return kp_mk_ss;
    }

    protected SecretKeySpec deriveApplicationKey(SecretKeySpec keySpec, byte[] cin, byte[] keyNumVer) throws NoSuchProviderException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {

        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");

        sha256.update(cin);
        sha256.update(keyNumVer);
        byte[] derivationParam = sha256.digest();
        System.out.println("derivationParam = SHA-256(CIN|kNum|kVer) (" + derivationParam.length * 8 + "bit) = " + toString(derivationParam));
        derivationParam = Arrays.copyOf(derivationParam, 24);
        System.out.println("derivationParam (" + derivationParam.length * 8 + "bit) = " + toString(derivationParam));

        //        DESedeKeySpec kp_mk_ssSpec = new DESedeKeySpec(kp_mk_ss);
        System.out.println("Application Master Key KP_MK_SS (DES-EDE): " + toString(keySpec.getEncoded()));
        System.out.println("Derive application key KP_SS");
        Cipher tripleDES = Cipher.getInstance("3DES/CBC/NoPadding", "IAIK");
        tripleDES.init(Cipher.ENCRYPT_MODE, keySpec, new IvParameterSpec(iv));
        System.out.println("derivationParam (" + derivationParam.length * 8 + "bit) = " + toString(derivationParam));
        byte[] x = tripleDES.doFinal(derivationParam);
        System.out.println("kp_ss (" + x.length * 8 + "bit): " + toString(x));
        for (int key_i = 0; key_i < x.length; key_i += 8) {
            for (int i = 0; i < 8; i++) {
                int ones = 0;
                for (int j = 1; j < BIT_MASK.length; j++) {
                    if ((x[i + key_i] & BIT_MASK[j]) == BIT_MASK[j]) {
                        ones++;
                    }
                }
                if ((ones & 0x1) > 0) {
                    x[i + key_i] &= (byte) 0xfe; // odd
                } else {
                    x[i + key_i] |= 0x1; // even
                }
            }
        }
        System.out.println("kp_ss (parity adjusted): " + toString(x));
        SecretKeySpec kp_ss = new SecretKeySpec(x, "3DES");
        return kp_ss;
    }

    private void mutualAuth(byte[] cin, byte[] rnd_icc, SecretKeySpec kenc, SecretKeySpec kmac) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, CardException {
        if (rnd_icc.length != 8) {
            throw new RuntimeException("invalid RND.ICC: " + toString(rnd_icc));
        }

        if (cin.length != 10) {
            throw new RuntimeException("invalid CIN: " + toString(cin));
        }

        Random rand = new Random(System.currentTimeMillis());

        byte[] rnd_ifd = new byte[8];
        rand.nextBytes(rnd_ifd);
        byte[] icc_id = Arrays.copyOfRange(cin, cin.length - 8, cin.length);
        byte[] ifd_id = new byte[8];
        rand.nextBytes(ifd_id);
        byte[] kd_ifd = new byte[64];
        rand.nextBytes(kd_ifd);

        Cipher tDES = Cipher.getInstance("3DES/CBC/NoPadding");
        tDES.init(Cipher.ENCRYPT_MODE, kenc, new IvParameterSpec(iv));

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

        Mac retailMac = Mac.getInstance("CMacDESede");
        retailMac.init(kmac);
        byte[] mac = retailMac.doFinal(cryptogram);
        System.out.println("MAC: " + toString(mac));

        byte[] c = new byte[cryptogram.length + mac.length];
        System.arraycopy(cryptogram, 0, c, 0, cryptogram.length);
        System.arraycopy(mac, 0, c, cryptogram.length, mac.length);
        System.out.println(c.length + "bytes :" + toString(c));
        CommandAPDU cmdAPDU = new CommandAPDU(0x00, 0x82, 0x00, 0x81, c, 256); // 81->00
        System.out.println(" cmd apdu " + toString(cmdAPDU.getBytes()));
        ResponseAPDU resp = channel.transmit(cmdAPDU);
        System.out.println(" -> " + toString(resp.getBytes()) + "\n");
    }


    private SecretKeySpec deriveKey(SecretKeySpec masterkey, String cipherAlias, byte[] iv, byte[] derivationParam) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {

        if (derivationParam.length != 24) {
            throw new RuntimeException("invalid 3TDES derivation parameter: " + toString(derivationParam));
        }

        //3DES/CBC/NoPadding
        Cipher cipher = Cipher.getInstance(cipherAlias);
        cipher.init(Cipher.ENCRYPT_MODE, masterkey, new IvParameterSpec(iv));

        System.out.println("derivation parameter ("
                + derivationParam.length * 8 + "bit): "
                + toString(derivationParam));
        System.out.println("derivation key ("
                + masterkey.getAlgorithm() + ") :"
                + toString(masterkey.getEncoded()));

        byte[] x = cipher.doFinal(derivationParam);

        System.out.println("x (" + x.length * 8 + "bit): " + toString(x));

        if (x.length != 24) {
            throw new RuntimeException("invalid derived key: " + toString(x));
        }
        
        for (int offset = 0; offset < x.length; offset += 8) {
            adjustParityBit(x, offset);
        }

        SecretKeySpec derivedKey = new SecretKeySpec(x, masterkey.getAlgorithm());

        System.out.println("derived key ("
                + derivedKey.getAlgorithm() + ") :"
                + toString(derivedKey.getEncoded()));
        
        return derivedKey;
    }

    private final static byte[] K_ENC;
    private final static byte[] K_MAC;

    static {
        byte[] encBytes;
        byte[] macBytes;
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            encBytes = sha256.digest("K_ENC".getBytes("ASCII"));
            macBytes = sha256.digest("K_MAC".getBytes("ASCII"));
        } catch (NoSuchAlgorithmException ex) {
            encBytes = new byte[] {(byte)0xe0};
            macBytes = new byte[] {(byte)0xe0};
        } catch (UnsupportedEncodingException ex) {
            encBytes = new byte[] {(byte)0xe1};
            macBytes = new byte[] {(byte)0xe1};
        }
        K_ENC = Arrays.copyOf(encBytes, 24);
        K_MAC = Arrays.copyOf(macBytes, 24);
    }
    
    private final static byte[] iv = {
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
    };
    
    /** Bit mask for counting the ones. */
    private final static byte[] BIT_MASK = {
        0x01, 0x02, 0x04, 0x08, 0x10, 0x20, 0x40, (byte) 0x80
    };
    
    private void adjustParityBit(byte[] x, int offset) {

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

    public static void main(String[] args) throws NoSuchAlgorithmException, CardException, SignatureCardException, CodingException, InvalidKeyException, NoSuchProviderException, NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {

        ActivationTest test = new ActivationTest();
        test.setUp();
        byte[] cin = test.getCIN();

        test.getCIO_PrK_SS();
        
//        test.getSVNr();
        test.getPuK_SicSig(cin);
    }
}
