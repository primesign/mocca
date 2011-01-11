/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package at.gv.egiz.smcc.activation;

import java.nio.ByteBuffer;
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
        return basicChannel.transmit(capdu);
    }

    @Override
    public int transmit(ByteBuffer bb, ByteBuffer bb1) throws CardException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void close() throws CardException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
