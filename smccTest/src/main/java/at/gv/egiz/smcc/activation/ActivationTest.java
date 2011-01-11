/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Arrays;
import java.util.List;
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

        System.out.println("SELECT EF.GDO");
        // alternative: read with SFI=02: 00 b0 82 00 fa
        // P1=0x00 -> 6a:80 (incorrect cmd data)
        // no Le -> 67:00 (wrong length)
        cmdAPDU = new CommandAPDU(0x00, 0xa4, 0x02, 0x00, new byte[] {(byte) 0x2f, (byte) 0x02}, 256);
        System.out.println(" cmd apdu " + toString(cmdAPDU.getBytes()));
        resp = channel.transmit(cmdAPDU);
        System.out.println(" -> " + toString(resp.getBytes()) + "\n");

        System.out.println("READ EF.GDO");
        // 7.2.2 (case2), offset=0
        cmdAPDU = new CommandAPDU(0x00, 0xb0, 0x00, 0x00, 256);
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

    public static String toString(byte[] b) {
        StringBuffer sb = new StringBuffer();
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

    public void getPuK_GewSig() throws CardException, SignatureCardException, CodingException {
        CommandAPDU cmdAPDU;
        ResponseAPDU resp;

        System.out.println("SELECT MF");
        cmdAPDU = new CommandAPDU(0x00, 0xA4, 0x00, 0x0c, new byte[]{(byte) 0x3f, (byte) 0x00});
        System.out.println(" cmd apdu " + toString(cmdAPDU.getBytes()));
        resp = channel.transmit(cmdAPDU);
        System.out.println(" -> " + toString(resp.getBytes()) + "\n");

        System.out.println("SELECT DF.QualifizierteSignatur");
        // P1=0x00 -> 67:00 (wrong length)
        cmdAPDU = new CommandAPDU(0x00, 0xa4, 0x04, 0x0c, new byte[] {(byte) 0xd0, (byte) 0x40, (byte) 0x00, (byte) 0x00, (byte) 0x17, (byte) 0x00, (byte) 0x12, (byte) 0x01});
        System.out.println(" cmd apdu " + toString(cmdAPDU.getBytes()));
        resp = channel.transmit(cmdAPDU);
        System.out.println(" -> " + toString(resp.getBytes()) + "\n");

        System.out.println("SELECT EF.PrKD");
        // P1=0x00 -> 67:00 (wrong length)
        cmdAPDU = new CommandAPDU(0x00, 0xa4, 0x02, 0x04, new byte[] {(byte) 0x50, (byte) 0x35}, 256);
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

        int dfSpecificKeyRef = 1;
        byte[] crt_at = new byte[] {
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
        // eg. RNDICC = [ed:9b:a3:78:83:2f:d3:6c:90:00]
        cmdAPDU = new CommandAPDU(0x00, 0x84, 0x00, 0x00, 0x08);
        System.out.println(" cmd apdu " + toString(cmdAPDU.getBytes()));
        resp = channel.transmit(cmdAPDU);
        System.out.println(" -> " + toString(resp.getBytes()) + "\n");


        // ICCSN + RNDICC => Kryptogramm_CRS (STARCOS31, p357 authentication according to e-SignK)
        // MUTUALAUTH -> Kryptogramm_Karte -> prüfung -> session key
        System.out.println("MUTUAL AUTHENTICATE TODO...");
        

        System.out.println("SELECT EF.PuK_QS");
        // P1=0x00 -> 67:00 (wrong length)
        cmdAPDU = new CommandAPDU(0x00, 0xa4, 0x02, 0x04, new byte[] {(byte) 0x0e, (byte) 0x01}, 256);
        System.out.println(" cmd apdu " + toString(cmdAPDU.getBytes()));
        resp = channel.transmit(cmdAPDU);
        System.out.println(" -> " + toString(resp.getBytes()) + "\n");

        System.out.println("READ EF.PuK_QS");
        // 7.2.2 (case2), offset=0
        cmdAPDU = new CommandAPDU(0x00, 0xb0, 0x00, 0x00, 256);
        System.out.println(" cmd apdu " + toString(cmdAPDU.getBytes()));
        resp = channel.transmit(cmdAPDU);
        System.out.println(" -> " + toString(resp.getBytes()) + "\n");

        System.out.println("PuK_QS:\n" + toString(resp.getData()));


    }
    

    public static void main(String[] args) throws NoSuchAlgorithmException, CardException, SignatureCardException, CodingException {

        ActivationTest test = new ActivationTest();
        test.setUp();
//        test.getCIN();
//        test.getSVNr();
        test.getPuK_GewSig();
    }
}
