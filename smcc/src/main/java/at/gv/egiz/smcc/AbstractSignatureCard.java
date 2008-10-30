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

import java.nio.ByteBuffer;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.smartcardio.ATR;
import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class AbstractSignatureCard implements SignatureCard {

  private static Log log = LogFactory.getLog(AbstractSignatureCard.class);

  private ResourceBundle i18n;
  private String resourceBundleName;

  private Locale locale = Locale.getDefault();

  int ifs_ = 254;

  Card card_;

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

  protected abstract byte[] selectFileAID(byte[] fid) throws CardException,
      SignatureCardException;

  protected abstract ResponseAPDU selectFileFID(byte[] fid) throws CardException,
      SignatureCardException;

  /**
   * VERIFY PIN
   * 
   * <p>
   * Implementations of this method should call
   * {@link PINProvider#providePIN(PINSpec, int)} to retrieve the PIN entered by
   * the user and VERIFY PIN on the smart card until the PIN has been
   * successfully verified.
   * </p>
   * 
   * @param pinProvider
   *          the PINProvider
   * @param spec
   *          the PINSpec
   * @param kid
   *          the key ID (KID) of the PIN to verify
   * 
   * @throws CardException
   *           if smart card communication fails
   * 
   * @throws CancelledException
   *           if the PINProvider indicated that the user canceled the PIN entry
   * @throws NotActivatedException
   *           if the card application has not been activated
   * @throws LockedException
   *           if the card application is locked
   * 
   * @throws SignatureCardException
   *           if VERIFY PIN fails
   */
  protected abstract void verifyPIN(PINProvider pinProvider, PINSpec spec,
      byte kid) throws CardException, SignatureCardException;

  protected byte[] readBinary(CardChannel channel, int offset, int len)
      throws CardException, SignatureCardException {

    ResponseAPDU resp = transmit(channel, new CommandAPDU(0x00, 0xB0,
        0x7F & (offset >> 8), offset & 0xFF, len));
    if (resp.getSW() == 0x9000) {
      return resp.getData();
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
   */
  protected byte[] readTLVFile(byte[] aid, byte[] ef, int maxLength)
      throws SignatureCardException {
    return readTLVFilePIN(aid, ef, (byte) 0, null, null, maxLength);
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
   */
  protected byte[] readTLVFilePIN(byte[] aid, byte[] ef, byte kid,
      PINProvider provider, PINSpec spec, int maxLength)
      throws SignatureCardException {

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

      // try to READ BINARY
      byte[] b = new byte[1];
      int sw = readBinary(0, 1, b);
      
      if (provider != null && sw == 0x6982) {

        // VERIFY
        verifyPIN(provider, spec, kid);
        
      } else if (sw == 0x9000) {
        // not expected type
        if (b[0] != 0x30) {
          throw new NotActivatedException();
        }
      } else {
        throw new SignatureCardException("READ BINARY failed (SW="
            + Integer.toHexString(sw) + ").");
      }

      // READ BINARY
      byte[] data = readBinaryTLV(maxLength, (byte) 0x30);

      return data;

    } catch (CardException e) {
      throw new SignatureCardException("Failed to acces card.", e);
    }

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

  
  public void init(Card card) {
    card_ = card;
    ATR atr = card.getATR();
    byte[] atrBytes = atr.getBytes();
    if (atrBytes.length >= 6) {
      ifs_ = 0xFF & atr.getBytes()[6];
      log.trace("Setting IFS (information field size) to " + ifs_);
    }
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

}
