//Copyright (C) 2002 IAIK
//http://jce.iaik.at
//
//Copyright (C) 2003 Stiftung Secure Information and 
//                 Communication Technologies SIC
//http://www.sic.st
//
//All rights reserved.
//
//This source is provided for inspection purposes and recompilation only,
//unless specified differently in a contract with IAIK. This source has to
//be kept in strict confidence and must not be disclosed to any third party
//under any circumstances. Redistribution in source and binary forms, with
//or without modification, are <not> permitted in any case!
//
//THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS'' AND
//ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
//IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
//ARE DISCLAIMED.  IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS BE LIABLE
//FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
//DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
//OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
//HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
//LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
//OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
//SUCH DAMAGE.
//
//
package at.gv.egiz.smcc;

import at.gv.egiz.smcc.util.SMCCHelper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.smartcardio.ATR;
import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class AbstractSignatureCard implements SignatureCard {

  private static Log log = LogFactory.getLog(AbstractSignatureCard.class);

  protected List<PINSpec> pinSpecs = new ArrayList<PINSpec>();

  private ResourceBundle i18n;
  private String resourceBundleName;

  private Locale locale = Locale.getDefault();

  int ifs_ = 254;

  private Card card_;
  
  /**
   * The card terminal that connects the {@link #card_}.  
   */
  private CardTerminal cardTerminal;

  protected AbstractSignatureCard(String resourceBundleName) {
    this.resourceBundleName = resourceBundleName;
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

  /**
   * Select an application using AID as DF name according to ISO/IEC 7816-4
   * section 8.2.2.2.
   * 
   * @param dfName
   *          AID of the application to be selected
   * 
   * @return the response data of the response APDU if SW=0x9000
   * 
   * @throws CardException
   *           if card communication fails
   * 
   * @throws SignatureCardException
   *           if application selection fails (e.g. an application with the
   *           given AID is not present on the card)
   */
  protected byte[] selectFileAID(byte[] dfName) throws CardException, SignatureCardException {
    CardChannel channel = getCardChannel();
    ResponseAPDU resp = transmit(channel, new CommandAPDU(0x00, 0xA4, 0x04,
        0x00, dfName, 256));
    if (resp.getSW() != 0x9000) {
      throw new SignatureCardException("Failed to select application AID="
          + toString(dfName) + ": SW=" + Integer.toHexString(resp.getSW()) + ".");
    } else {
      return resp.getBytes();
    }
  }

  protected abstract ResponseAPDU selectFileFID(byte[] fid) throws CardException,
      SignatureCardException;

  protected abstract int verifyPIN(String pin, byte kid) 
          throws LockedException, NotActivatedException, SignatureCardException;

  
  protected byte[] readRecord(int recordNumber) throws SignatureCardException, CardException {
    return readRecord(getCardChannel(), recordNumber);
  }

  protected byte[] readRecord(CardChannel channel, int recordNumber) throws SignatureCardException, CardException {
    
    ResponseAPDU resp = transmit(channel, new CommandAPDU(0x00, 0xB2,
        recordNumber, 0x04, 256));
    if (resp.getSW() == 0x9000) {
      return resp.getData();
    } else {
      throw new SignatureCardException("Failed to read records. SW=" + Integer.toHexString(resp.getSW()));
    }
     
  }
  
  protected byte[] readBinary(CardChannel channel, int offset, int len)
      throws CardException, SignatureCardException {

    ResponseAPDU resp = transmit(channel, new CommandAPDU(0x00, 0xB0,
        0x7F & (offset >> 8), offset & 0xFF, len));
    if (resp.getSW() == 0x9000) {
      return resp.getData();
    } else if (resp.getSW() == 0x6982) {
      throw new SecurityStatusNotSatisfiedException();
    } else {
      throw new SignatureCardException("Failed to read bytes (" + offset + "+"
          + len + "): SW=" + Integer.toHexString(resp.getSW()));
    }

  }

  protected int readBinary(int offset, int len, byte[] b) throws CardException,
      SignatureCardException {

    if (b.length < len) {
      throw new IllegalArgumentException(
          "Length of b must not be less than len.");
    }

    CardChannel channel = getCardChannel();

    ResponseAPDU resp = transmit(channel, new CommandAPDU(0x00, 0xB0,
        0x7F & (offset >> 8), offset & 0xFF, len));
    if (resp.getSW() == 0x9000) {
      System.arraycopy(resp.getData(), 0, b, 0, len);
    }

    return resp.getSW();

  }

  protected byte[] readBinaryTLV(int maxSize, byte expectedType) throws CardException,
      SignatureCardException {

    CardChannel channel = getCardChannel();

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

  protected byte[] readRecords(byte[] aid, byte[] ef, int start, int end) throws SignatureCardException, InterruptedException {
    
    try {
      
      // SELECT FILE (AID)
      byte[] rb = selectFileAID(aid);
      if (rb[rb.length - 2] != (byte) 0x90 || rb[rb.length - 1] != (byte) 0x00) {

        throw new SignatureCardException("SELECT FILE with "
            + "AID="
            + toString(aid)
            + " failed ("
            + "SW="
            + Integer.toHexString((0xFF & (int) rb[rb.length - 1])
                | (0xFF & (int) rb[rb.length - 2]) << 8) + ").");

      }

      // SELECT FILE (EF)
      ResponseAPDU resp = selectFileFID(ef);
      if (resp.getSW() == 0x6a82) {
        
        // EF not found
        throw new FileNotFoundException("EF " + toString(ef) + " not found.");
        
      } else if (resp.getSW() != 0x9000) {

        throw new SignatureCardException("SELECT FILE with "
            + "FID="
            + toString(ef)
            + " failed ("
            + "SW="
            + Integer.toHexString(resp.getSW()) + ").");
        
      }
      
      ByteArrayOutputStream bytes = new ByteArrayOutputStream();
      
      for (int i = start; i <= end; i++) {
        bytes.write(readRecord(i));
      }
      
      return bytes.toByteArray();
      
    } catch (CardException e) {
      throw new SignatureCardException("Failed to acces card.", e);
    } catch (IOException e) {
      throw new SignatureCardException("Failed to read records.", e);
    }
    
  }
  
  /**
   * Read the content of a TLV file.
   * 
   * @param aid the application ID (AID)
   * @param ef the elementary file (EF)
   * @param maxLength the maximum length of the file
   * 
   * @return the content of the file
   * 
   * @throws SignatureCardException
   * @throws CardException 
   */
  protected byte[] readTLVFile(byte[] aid, byte[] ef, int maxLength)
      throws SignatureCardException, InterruptedException, CardException {
    return readTLVFile(aid, ef, null, (byte) 0, maxLength);
  }

  /**
   * Read the content of a TLV file wich may require a PIN.
   * 
   * @param aid the application ID (AID)
   * @param ef the elementary file (EF)
   * @param kid the key ID (KID) of the corresponding PIN
   * @param provider the PINProvider
   * @param spec the PINSpec
   * @param maxLength the maximum length of the file
   * 
   * @return the content of the file
   * 
   * @throws SignatureCardException
   * @throws CardException 
   */
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
      throw new SignatureCardException("SELECT FILE with "
          + "FID="
          + toString(ef)
          + " failed ("
          + "SW="
          + Integer.toHexString(resp.getSW()) + ").");
    }

    // VERIFY
    if (pin != null) {
      int retries = verifyPIN(pin, kid);
      if (retries != -1) {
        throw new VerificationFailedException(retries);
      }
    }
   
    return readBinaryTLV(maxLength, (byte) 0x30);
      
    
  }
  
  /**
   * Transmit the given command APDU using the given card channel.
   * 
   * @param channel
   *          the card channel
   * @param commandAPDU
   *          the command APDU
   * @param logData
   *          <code>true</code> if command APDU data may be logged, or
   *          <code>false</code> otherwise
   * 
   * @return the corresponding response APDU
   * 
   * @throws CardException
   *           if smart card communication fails
   */
  protected ResponseAPDU transmit(CardChannel channel, CommandAPDU commandAPDU, boolean logData)
      throws CardException {
    
    if (log.isTraceEnabled()) {
      log.trace(commandAPDU 
          + (logData ? "\n" + toString(commandAPDU.getBytes()) : ""));
      long t0 = System.currentTimeMillis();
      ResponseAPDU responseAPDU = channel.transmit(commandAPDU);
      long t1 = System.currentTimeMillis();
      log.trace(responseAPDU + "\n[" + (t1 - t0) + "ms] "
          + (logData ? "\n" + toString(responseAPDU.getBytes()) : ""));
      return responseAPDU;
    } else {
      return channel.transmit(commandAPDU);
    }
    
  }

  /**
   * Transmit the given command APDU using the given card channel.
   * 
   * @param channel the card channel
   * @param commandAPDU the command APDU
   * 
   * @return the corresponding response APDU
   * 
   * @throws CardException if smart card communication fails
   */
  protected ResponseAPDU transmit(CardChannel channel, CommandAPDU commandAPDU)
      throws CardException {
    return transmit(channel, commandAPDU, true);
  }

  
  public void init(Card card, CardTerminal cardTerminal) {
    card_ = card;
    this.cardTerminal = cardTerminal;
    ATR atr = card.getATR();
    byte[] atrBytes = atr.getBytes();
    if (atrBytes.length >= 6) {
      ifs_ = 0xFF & atr.getBytes()[6];
      log.trace("Setting IFS (information field size) to " + ifs_);
    }
  }
  
  @Override
  public Card getCard() {
    return card_;
  }

  protected CardChannel getCardChannel() {
    return card_.getBasicChannel();
  }

  @Override
  public void setLocale(Locale locale) {
    if (locale == null) {
      throw new NullPointerException("Locale must not be set to null");
    }
    this.locale = locale;
  }

  protected ResourceBundle getResourceBundle() {
    if (i18n == null) {
      i18n = ResourceBundle.getBundle(resourceBundleName, locale);
    }
    return i18n;
  }

  @Override
  public void disconnect(boolean reset) {
    log.debug("Disconnect called");
    if (card_ != null) {
      try {
        card_.disconnect(reset);
      } catch (Exception e) {
        log.info("Error while resetting card", e);
      }
    }
  }

  @Override
  public void reset() throws SignatureCardException {
    try {
      log.debug("Disconnect and reset smart card.");
      card_.disconnect(true);
      log.debug("Reconnect smart card.");
      if (cardTerminal != null) {
        card_ = cardTerminal.connect("*");
      }
    } catch (CardException e) {
      throw new SignatureCardException("Failed to reset card.", e);
    }
  }

  @Override
  public List<PINSpec> getPINSpecs() {
    return pinSpecs;
  }

  @Override
  public int verifyPIN(PINSpec pinSpec, String pin) throws LockedException, NotActivatedException, SignatureCardException {

    Card icc = getCard();
    try {
      icc.beginExclusive();
      CardChannel channel = icc.getBasicChannel();

      if (pinSpec.getContextAID() != null) {
        ResponseAPDU responseAPDU = transmit(channel,
                new CommandAPDU(0x00, 0xa4, 0x04, 0x0c, pinSpec.getContextAID()));
        if (responseAPDU.getSW() != 0x9000) {
          icc.endExclusive();
          String msg = "Failed to verify PIN " +
                  SMCCHelper.toString(new byte[]{pinSpec.getKID()}) +
                  ": Failed to verify AID " +
                  SMCCHelper.toString(pinSpec.getContextAID()) +
                  ": " + SMCCHelper.toString(responseAPDU.getBytes());
          log.error(msg);
          throw new SignatureCardException(msg);
        }
      }
      return verifyPIN(pin, pinSpec.getKID());

    } catch (CardException ex) {
      log.error("failed to verify pinspec: " + ex.getMessage(), ex);
      throw new SignatureCardException(ex);
    } finally {
      try {
        icc.endExclusive();
      } catch (CardException ex) {
        log.trace("failed to end exclusive card access: " + ex.getMessage());
      }

    }
  }
}
