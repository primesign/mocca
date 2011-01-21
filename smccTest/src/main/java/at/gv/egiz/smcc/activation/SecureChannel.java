/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package at.gv.egiz.smcc.activation;

import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.spec.SecretKeySpec;
import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

/**
 *
 * @author clemens
 */
public class SecureChannel extends CardChannel {

    CardChannel basicChannel;

    SecretKeySpec kenc;
    SecretKeySpec kmac;
    byte[] kencssc;
    byte[] kmacssc;

    public SecureChannel(CardChannel basicChannel, 
            SecretKeySpec kenc, SecretKeySpec kmac,
            byte[] kencssc, byte[] kmacssc) {
        this.basicChannel = basicChannel;
        this.kenc = kenc;
        this.kmac = kmac;
        this.kencssc = kencssc;
        this.kmacssc = kmacssc;
    }

    
    @Override
    public Card getCard() {
        return basicChannel.getCard();
    }

    @Override
    public int getChannelNumber() {
        return basicChannel.getChannelNumber();
    }

    @Override
    public ResponseAPDU transmit(CommandAPDU capdu) throws CardException {
        try {
            kmacssc[7] += 1;
            System.out.println("incrementing kmac_ssc: " + toString(kmacssc));
            System.out.println("kmac: " + toString(kmac.getEncoded()));
            CommandAPDU capdu_ = new CommandAPDU(protectAPDU(capdu.getBytes()));
            System.out.println(" cmd apdu* " + toString(capdu_.getBytes()));
            return basicChannel.transmit(capdu_);
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
     *
     * @param apdu
     * @return
     * @throws IllegalArgumentException
     */
    protected byte[] protectAPDU(byte[] apdu) throws IllegalArgumentException, InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException, GeneralSecurityException {

        byte[] apdu_ = null;
        byte[] mac_in = null;
        int i_mac = 0;

        if (apdu.length < 4) {
            throw new IllegalArgumentException("invalid Command APDU " + toString(apdu));
        } else if (apdu.length == 4) {
            // no command data, no response data, status not protected
            // CLA|INS|P1|P2            -> CLA'|INS|P1|P2|Lc'|TLmac
            apdu_ = new byte[15];

            // authenticate header: CLA* b3=1
            apdu_[0] = (byte) (apdu[0] | (byte) 0x0c); // CLA*: b8-6=000 b4-3=11
            apdu_[1] = apdu[1];
            apdu_[2] = apdu[2];
            apdu_[3] = apdu[3];
            
            // Lc': TLmac (blocksize=8)
            apdu_[4] = (byte) 0x0a;
            // cryptographic checksum
            apdu_[5] = (byte) 0x8e;
            apdu_[6] = (byte) 0x08;
            i_mac = 7;

            // 2 data objects: SSC, header
            mac_in = new byte[2*8];

            //TODO increment SSC
            System.arraycopy(kmacssc, 0, mac_in, 0, 8);
            
            // CLA** INS P1 P2 padding
            byte[] header_ = RetailCBCMac.pad(Arrays.copyOf(apdu_, 4), 8);
            System.arraycopy(header_, 0, mac_in, 8, 8);

            System.out.println("data covered by cryptographic checksum: " + toString(mac_in));
            
        } else if (apdu.length == 5) {
            // CLA|INS|P1|P2|Le         -> CLA'|INS|P1|P2|Lc'|TLle|TLmac|00
            // no command data, response data
            apdu_ = new byte[19];

            // authenticate header: CLA* b3=1
            apdu_[0] = (byte) (apdu[0] | (byte) 0x0c); // CLA*: b8-6=000 b4-3=11
            apdu_[1] = apdu[1];
            apdu_[2] = apdu[2];
            apdu_[3] = apdu[3];

            // Lc': TLle TLmac
            apdu_[4] = (byte) 0x0d;

            // Ne
            apdu_[5] = (byte) 0x97;
            apdu_[6] = (byte) 0x01;
            apdu_[7] = apdu[4];

            // cryptographic checksum
            apdu_[8] = (byte) 0x8e;
            apdu_[9] = (byte) 0x08;
            i_mac = 10;
            apdu_[18] = 0x00;

            // 3 data objects: SSC, header, Le
            mac_in = new byte[3*8];

            //TODO increment SSC
            System.arraycopy(kmacssc, 0, mac_in, 0, 8);

            // CLA** INS P1 P2 padding
            byte[] header_ = RetailCBCMac.pad(Arrays.copyOf(apdu_, 4), 8);
            System.arraycopy(header_, 0, mac_in, 8, 8);

            System.arraycopy(RetailCBCMac.pad(Arrays.copyOfRange(apdu_, 5, 8), 8), 0, mac_in, 16, 8);
            System.out.println("data covered by cryptographic checksum: " + toString(mac_in));


        } else if (apdu.length == 5 + apdu[4]){
            // CLA|INS|P1|P2|Lc|Data    -> CLA'|INS|P1|P2|Lc'|TLplain|TLmac
            // 81 (plain value) 8e (cryptographic checksum)

        } else {
            // CLA|INS|P1|P2|Lc|Data|Le -> CLA'|INS|P1|P2|Lc'|TLplain|TLle|TLmac|00
            // 81 (plain value) 97 (Ne) 8e (cryptographic checksum)
            
            // TODO extended APDUs

        }


        byte[] mac = RetailCBCMac.retailMac(mac_in, "DES", "DESede", kmac, 8, 8);
        System.out.println("mac: " + toString(mac));
        System.arraycopy(mac, 0, apdu_, i_mac, 8);
        return apdu_;

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
