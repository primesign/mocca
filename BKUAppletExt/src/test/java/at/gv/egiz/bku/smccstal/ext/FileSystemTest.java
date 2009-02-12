/*
 * Copyright 2008 Federal Chancellery Austria and
 * Graz University of Technology
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package at.gv.egiz.bku.smccstal.ext;

import at.gv.egiz.smcc.FileNotFoundException;
import at.gv.egiz.smcc.LockedException;
import at.gv.egiz.smcc.NotActivatedException;
import at.gv.egiz.smcc.PINProvider;
import at.gv.egiz.smcc.PINSpec;
import at.gv.egiz.smcc.SignatureCard;
import at.gv.egiz.smcc.SignatureCardException;
import at.gv.egiz.smcc.util.SMCCHelper;
import at.gv.egiz.smcc.util.SmartCardIO;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Clemens Orthacker <clemens.orthacker@iaik.tugraz.at>
 */
@Ignore
public class FileSystemTest {

  /** asign premium */
  public static final byte[] AID_DEC = new byte[] { (byte) 0xA0, (byte) 0x00,
      (byte) 0x00, (byte) 0x01, (byte) 0x18, (byte) 0x45, (byte) 0x4E };
  
  @Test
//  @Ignore
  public void testCard() throws CardException, SignatureCardException, InterruptedException {

    SMCCHelper smccHelper = new SMCCHelper();
    switch (smccHelper.getResultCode()) {
      case SMCCHelper.CARD_FOUND:
        System.out.println("card found ");
    }
    SignatureCard signatureCard = smccHelper.getSignatureCard(new Locale("de"));
    Card card = signatureCard.getCard();

//    SmartCardIO scIO = new SmartCardIO();
//    Map<CardTerminal, Card> terminalCardMap = scIO.getCards();
//
//    for (CardTerminal ct : terminalCardMap.keySet()) {
//      Card card = terminalCardMap.get(ct);
//      System.out.println("found card (" + ct.getName() + "): " + Formatter.byteArrayToHexString(card.getATR().getBytes()));

    System.out.println("found card " + Formatter.byteArrayToHexString(card.getATR().getBytes()));

    CardChannel cardchannel;

    //RESET
    System.out.println("RESET");
    signatureCard.reset();
    card = signatureCard.getCard();
//      card.disconnect(true);
//      card = ct.connect("*");

    System.out.println("begin exclusive");
    card.beginExclusive();
    System.out.println("get cardchannel");
    cardchannel = card.getBasicChannel();

    testECard(cardchannel, signatureCard, card);
//    testASignPremium(cardchannel, signatureCard, card);

//    }

  }

  public static class TestCard {

    protected CardChannel channel;
    protected int ifs_ = 254;

    public TestCard(CardChannel channel) {
      this.channel = channel;
    }

    protected byte[] readTLVFile(byte[] aid, byte[] ef, String pin, byte kid, int maxLength)
            throws SignatureCardException, InterruptedException, CardException {


      // SELECT FILE (AID)
      selectFileAID(aid);

      // SELECT FILE (EF)
      ResponseAPDU resp = selectFileFID(ef);
      if (resp.getSW() == 0x6a82) {
        // EF not found
        throw new FileNotFoundException("EF " + toString(ef) + " not found.");
      } else if (resp.getSW() != 0x9000) {
        throw new SignatureCardException("SELECT FILE with " + "FID=" + toString(ef) + " failed (" + "SW=" + Integer.toHexString(resp.getSW()) + ").");
      }

      // VERIFY
      if (pin != null) {
        int retries = verifyPIN(pin, kid);
        if (retries != -1) {
          throw new at.gv.egiz.smcc.VerificationFailedException(retries);
        }
      }

      return readBinaryTLV(maxLength, (byte) 0x30);
    }

    protected byte[] readBinary(CardChannel channel, int offset, int len)
            throws CardException, SignatureCardException {

      //transmit(channel,apdu)
      ResponseAPDU resp = channel.transmit(new CommandAPDU(0x00, 0xB0,
              0x7F & (offset >> 8), offset & 0xFF, len));
      if (resp.getSW() == 0x9000) {
        return resp.getData();
      } else if (resp.getSW() == 0x6982) {
        throw new at.gv.egiz.smcc.SecurityStatusNotSatisfiedException();
      } else {
        throw new SignatureCardException("Failed to read bytes (" + offset + "+" + len + "): SW=" + Integer.toHexString(resp.getSW()));
      }

    }

    protected byte[] readBinaryTLV(int maxSize, byte expectedType) throws CardException,
            SignatureCardException {

//      CardChannel channel = getCardChannel();

      // read first chunk
      int len = Math.min(maxSize, ifs_);
      byte[] chunk = readBinary(channel, 0, len);
      if (chunk.length > 0 && chunk[0] != expectedType) {
        return null;
      }
      int offset = chunk.length;
      int actualSize = maxSize;
      if (chunk.length > 3) {
        if ((chunk[1] & 0x80) > 0) {
          int octets = (0x0F & chunk[1]);
          actualSize = 2 + octets;
          for (int i = 1; i <= octets; i++) {
            actualSize += (0xFF & chunk[i + 1]) << ((octets - i) * 8);
          }
        } else {
          actualSize = 2 + chunk[1];
        }
      }
      ByteBuffer buffer = ByteBuffer.allocate(actualSize);
      buffer.put(chunk, 0, Math.min(actualSize, chunk.length));
      while (offset < actualSize) {
        len = Math.min(ifs_, actualSize - offset);
        chunk = readBinary(channel, offset, len);
        buffer.put(chunk);
        offset += chunk.length;
      }
      return buffer.array();

    }

    protected byte[] selectFileAID(byte[] dfName) throws CardException, SignatureCardException {
//      CardChannel channel = getCardChannel();
      ResponseAPDU resp = channel.transmit(new CommandAPDU(0x00, 0xA4, 0x04,
              0x00, dfName, 256));
      if (resp.getSW() != 0x9000) {
        throw new SignatureCardException("Failed to select application AID=" + toString(dfName) + ": SW=" + Integer.toHexString(resp.getSW()) + ".");
      } else {
        return resp.getBytes();
      }
    }

    protected ResponseAPDU selectFileFID(byte[] fid) throws CardException, SignatureCardException {
//      CardChannel channel = getCardChannel();
      return channel.transmit(new CommandAPDU(0x00, 0xA4, 0x02,
              0x04, fid, 256));
    }

    protected String toString(byte[] b) {
      StringBuffer sb = new StringBuffer();
      if (b != null && b.length > 0) {
        sb.append(Integer.toHexString((b[0] & 240) >> 4));
        sb.append(Integer.toHexString(b[0] & 15));
      }
      for (int i = 1; i < b.length; i++) {
        sb.append(':');
        sb.append(Integer.toHexString((b[i] & 240) >> 4));
        sb.append(Integer.toHexString(b[i] & 15));
      }
      return sb.toString();
    }

    protected int verifyPIN(String pin, byte kid) throws CardException, SignatureCardException {

//      CardChannel channel = getCardChannel();

      ResponseAPDU resp;
      if (pin == null) {
        //
        resp = channel.transmit(new CommandAPDU(0x00, 0x20, 0x00, kid));
      } else {
        // PIN length in bytes
        int len = (int) Math.ceil(pin.length() / 2);

        // BCD encode PIN and marshal PIN block
        byte[] pinBytes = new BigInteger(pin, 16).toByteArray();
        byte[] pinBlock = new byte[8];
        if (len < pinBytes.length) {
          System.arraycopy(pinBytes, pinBytes.length - len, pinBlock, 1, len);
        } else {
          System.arraycopy(pinBytes, 0, pinBlock, len - pinBytes.length + 1,
                  pinBytes.length);
        }
        pinBlock[0] = (byte) (0x20 + len * 2);
        Arrays.fill(pinBlock, len + 1, 8, (byte) 0xff);

        resp = channel.transmit(new CommandAPDU(0x00, 0x20, 0x00, kid, pinBlock));//, false);

      }

      if (resp.getSW() == 0x63c0) {
        throw new LockedException("PIN locked.");
      } else if (resp.getSW1() == 0x63 && resp.getSW2() >> 4 == 0xc) {
        // return number of possible retries
        return resp.getSW2() & 0x0f;
      } else if (resp.getSW() == 0x6983) {
        throw new LockedException();
      } else if (resp.getSW() == 0x6984) {
        // PIN LCS = "Initialized" (-> not activated)
        throw new NotActivatedException("PIN not set.");
      } else if (resp.getSW() == 0x9000) {
        return -1; // success
      } else {
        throw new SignatureCardException("Failed to verify pin: SW=" + Integer.toHexString(resp.getSW()));
      }
    }
  }

  public static class Formatter {

    private static String[] alphabet = {"0", "1", "2",
      "3", "4", "5", "6", "7", "8",
      "9", "A", "B", "C", "D", "E",
      "F"};

    public static String byteArrayToHexString(byte[] bytes) {

      if (bytes == null || bytes.length <= 0) {
        return null;
      }

      StringBuffer buf = new StringBuffer(2 * bytes.length);

      byte c = 0x00;

      for (int i = 0; i < bytes.length; i++) {

        // high nibble
        c = (byte) (bytes[i] & 0xf0);

        // shift down
        c = (byte) (c >>> 4);

        // cut high order bits
        c = (byte) (c & 0x0f);

        buf.append(alphabet[(int) c]);

        // low nibble
        c = (byte) (bytes[i] & 0x0f);

        buf.append(alphabet[(int) c]);
        if (i < bytes.length - 1) {
          buf.append(':');
        }
      }

      return buf.toString();

    }
  }

  protected void testASignPremium(CardChannel cardchannel, SignatureCard signatureCard, Card card) throws CardException {
    byte[] selectMF = new byte[]{(byte) 0x00, (byte) 0xA4, (byte) 0x00, (byte) 0x0C, (byte) 0x02, (byte) 0x3F, (byte) 0x00};
    byte[] selectDF_DEC = new byte[] { (byte) 0x00, (byte) 0xA4, (byte) 0x00, (byte) 0x0C, (byte) 0x02, (byte) 0xdf, (byte) 0x71 };
    byte[] selectAID_DEC = new byte[] { (byte) 0x00, (byte) 0xA4, (byte) 0x04, (byte) 0x00, (byte) 0x07, (byte) 0xA0, (byte) 0x00,
      (byte) 0x00, (byte) 0x01, (byte) 0x18, (byte) 0x45, (byte) 0x4E  };

    CommandAPDU cAPDU;
    ResponseAPDU rAPDU;
    byte[] sw;

    cAPDU = new CommandAPDU(selectMF);
    rAPDU = cardchannel.transmit(cAPDU);
    sw = new byte[]{(byte) (0xFF & rAPDU.getSW1()), (byte) (0xFF & rAPDU.getSW2())};
    System.out.println("cAPDU: " + Formatter.byteArrayToHexString(cAPDU.getBytes()));
    System.out.println("rAPDU (sw=" + Formatter.byteArrayToHexString(sw) + "): " + Formatter.byteArrayToHexString(rAPDU.getData()));

    cAPDU = new CommandAPDU(selectAID_DEC);
    rAPDU = cardchannel.transmit(cAPDU);
    sw = new byte[]{(byte) (0xFF & rAPDU.getSW1()), (byte) (0xFF & rAPDU.getSW2())};
    System.out.println("cAPDU: " + Formatter.byteArrayToHexString(cAPDU.getBytes()));
    System.out.println("rAPDU (sw=" + Formatter.byteArrayToHexString(sw) + "): " + Formatter.byteArrayToHexString(rAPDU.getData()));

    cAPDU = new CommandAPDU(selectDF_DEC);
    rAPDU = cardchannel.transmit(cAPDU);
    sw = new byte[]{(byte) (0xFF & rAPDU.getSW1()), (byte) (0xFF & rAPDU.getSW2())};
    System.out.println("cAPDU: " + Formatter.byteArrayToHexString(cAPDU.getBytes()));
    System.out.println("rAPDU (sw=" + Formatter.byteArrayToHexString(sw) + "): " + Formatter.byteArrayToHexString(rAPDU.getData()));


  }

  protected void testECard(CardChannel cardchannel, SignatureCard signatureCard, Card card) throws CardException, InterruptedException, SignatureCardException {
//      if (cardTerminal != null) {
//        card_ = cardTerminal.connect("*");
//      }
    byte[] selectMF = new byte[]{(byte) 0x00, (byte) 0xA4, (byte) 0x00, (byte) 0x0C, (byte) 0x02, (byte) 0x3F, (byte) 0x00};
    byte[] readEF_GDO = new byte[]{(byte) 0x00, (byte) 0xB0, (byte) 0x82, (byte) 0x00, (byte) 0x00};
    CommandAPDU cAPDU;
    ResponseAPDU rAPDU;
    byte[] sw;
    cAPDU = new CommandAPDU(selectMF);
    rAPDU = cardchannel.transmit(cAPDU);
    sw = new byte[]{(byte) (0xFF & rAPDU.getSW1()), (byte) (0xFF & rAPDU.getSW2())};
    System.out.println("cAPDU: " + Formatter.byteArrayToHexString(cAPDU.getBytes()));
    System.out.println("rAPDU (sw=" + Formatter.byteArrayToHexString(sw) + "): " + Formatter.byteArrayToHexString(rAPDU.getData()));
    cAPDU = new CommandAPDU(readEF_GDO);
    rAPDU = cardchannel.transmit(cAPDU);
    sw = new byte[]{(byte) (0xFF & rAPDU.getSW1()), (byte) (0xFF & rAPDU.getSW2())};
    System.out.println("cAPDU: " + Formatter.byteArrayToHexString(cAPDU.getBytes()));
    System.out.println("rAPDU (sw=" + Formatter.byteArrayToHexString(sw) + "): " + Formatter.byteArrayToHexString(rAPDU.getData()));
    byte[] EF_GDO = rAPDU.getData();
    //RESET
    System.out.println("RESET");
    signatureCard.reset();
    card = signatureCard.getCard();
//      card.disconnect(true);
//      card = ct.connect("*");
    System.out.println("begin exclusive");
    card.beginExclusive();
    System.out.println("get cardchannel");
    cardchannel = card.getBasicChannel();
    byte[] getCLC = new byte[]{(byte) 0x00, (byte) 0xCA, (byte) 0xDF, (byte) 0x20, (byte) 0x00};
    byte[] verifyKartenPIN = new byte[]{(byte) 0x00, (byte) 0x20, (byte) 0x00, (byte) 0x01};
    byte[] selectDF_SichereSignatur = new byte[]{(byte) 0x00, (byte) 0xA4, (byte) 0x04, (byte) 0x00, (byte) 0x08, (byte) 0xD0, (byte) 0x40, (byte) 0x00, (byte) 0x00, (byte) 0x17, (byte) 0x00, (byte) 0x12, (byte) 0x01, (byte) 0x00};
    byte[] verifySignaturPIN = new byte[]{(byte) 0x00, (byte) 0x20, (byte) 0x00, (byte) 0x81};
    cAPDU = new CommandAPDU(getCLC);
    rAPDU = cardchannel.transmit(cAPDU);
    sw = new byte[]{(byte) (0xFF & rAPDU.getSW1()), (byte) (0xFF & rAPDU.getSW2())};
    System.out.println("cAPDU: " + Formatter.byteArrayToHexString(cAPDU.getBytes()));
    System.out.println("rAPDU (sw=" + Formatter.byteArrayToHexString(sw) + "): " + Formatter.byteArrayToHexString(rAPDU.getData()));
    byte[] clc = rAPDU.getData();
    cAPDU = new CommandAPDU(verifyKartenPIN);
    rAPDU = cardchannel.transmit(cAPDU);
    sw = new byte[]{(byte) (0xFF & rAPDU.getSW1()), (byte) (0xFF & rAPDU.getSW2())};
    System.out.println("cAPDU: " + Formatter.byteArrayToHexString(cAPDU.getBytes()));
    System.out.println("rAPDU (sw=" + Formatter.byteArrayToHexString(sw) + "): " + Formatter.byteArrayToHexString(rAPDU.getData()));
    cAPDU = new CommandAPDU(selectDF_SichereSignatur);
    rAPDU = cardchannel.transmit(cAPDU);
    sw = new byte[]{(byte) (0xFF & rAPDU.getSW1()), (byte) (0xFF & rAPDU.getSW2())};
    System.out.println("cAPDU: " + Formatter.byteArrayToHexString(cAPDU.getBytes()));
    System.out.println("rAPDU (sw=" + Formatter.byteArrayToHexString(sw) + "): " + Formatter.byteArrayToHexString(rAPDU.getData()));
    cAPDU = new CommandAPDU(verifySignaturPIN);
    rAPDU = cardchannel.transmit(cAPDU);
    sw = new byte[]{(byte) (0xFF & rAPDU.getSW1()), (byte) (0xFF & rAPDU.getSW2())};
    System.out.println("cAPDU: " + Formatter.byteArrayToHexString(cAPDU.getBytes()));
    System.out.println("rAPDU (sw=" + Formatter.byteArrayToHexString(sw) + "): " + Formatter.byteArrayToHexString(rAPDU.getData()));
    //RESET
    System.out.println("RESET");
    signatureCard.reset();
    card = signatureCard.getCard();
    System.out.println("InfoboxReadRequests...");
    PINProvider pinProvider = new PINProvider() {

      @Override
      public String providePIN(PINSpec spec, int retries) throws InterruptedException {
        if (retries >= 3) {
          return "2540";
        } else {
          throw new InterruptedException("TOO FEW PIN RETRIES LEFT, ABORTING");
        }
      }
    };
    byte[] ehic = signatureCard.getInfobox("EHIC", pinProvider, null);
    System.out.println("EHIC: " + Formatter.byteArrayToHexString(ehic));
    byte[] grunddaten = signatureCard.getInfobox("Grunddaten", pinProvider, null);
    System.out.println("Grunddaten: " + Formatter.byteArrayToHexString(grunddaten));
    //RESET
    System.out.println("RESET");
    signatureCard.reset();
    card = signatureCard.getCard();
//      card.disconnect(true);
//      card = ct.connect("*");
    System.out.println("begin exclusive");
    card.beginExclusive();
    System.out.println("get cardchannel");
    cardchannel = card.getBasicChannel();
    cAPDU = new CommandAPDU(getCLC);
    rAPDU = cardchannel.transmit(cAPDU);
    sw = new byte[]{(byte) (0xFF & rAPDU.getSW1()), (byte) (0xFF & rAPDU.getSW2())};
    System.out.println("cAPDU: " + Formatter.byteArrayToHexString(cAPDU.getBytes()));
    System.out.println("rAPDU (sw=" + Formatter.byteArrayToHexString(sw) + "): " + Formatter.byteArrayToHexString(rAPDU.getData()));
    assertTrue(Arrays.equals(clc, rAPDU.getData()));
    cAPDU = new CommandAPDU(readEF_GDO);
    rAPDU = cardchannel.transmit(cAPDU);
    sw = new byte[]{(byte) (0xFF & rAPDU.getSW1()), (byte) (0xFF & rAPDU.getSW2())};
    System.out.println("cAPDU: " + Formatter.byteArrayToHexString(cAPDU.getBytes()));
    System.out.println("rAPDU (sw=" + Formatter.byteArrayToHexString(sw) + "): " + Formatter.byteArrayToHexString(rAPDU.getData()));
    assertTrue(Arrays.equals(EF_GDO, rAPDU.getData()));
//    }
  }
}
