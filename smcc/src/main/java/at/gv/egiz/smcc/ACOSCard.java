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

import java.nio.charset.Charset;

import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ACOSCard extends AbstractSignatureCard implements SignatureCard {
  
  private static Log log = LogFactory.getLog(ACOSCard.class);

  public static final byte[] AID_DEC = new byte[] { (byte) 0xA0, (byte) 0x00,
      (byte) 0x00, (byte) 0x01, (byte) 0x18, (byte) 0x45, (byte) 0x4E };

  public static final byte[] DF_DEC = new byte[] { (byte) 0xdf, (byte) 0x71 };

  public static final byte[] AID_SIG = new byte[] { (byte) 0xA0, (byte) 0x00,
      (byte) 0x00, (byte) 0x01, (byte) 0x18, (byte) 0x45, (byte) 0x43 };

  public static final byte[] DF_SIG = new byte[] { (byte) 0xdf, (byte) 0x70 };

  public static final byte[] EF_C_CH_EKEY = new byte[] { (byte) 0xc0,
      (byte) 0x01 };

  public static final int EF_C_CH_EKEY_MAX_SIZE = 2000;

  public static final byte[] EF_C_CH_DS = new byte[] { (byte) 0xc0, (byte) 0x02 };

  public static final int EF_C_CH_DS_MAX_SIZE = 2000;

  public static final byte[] EF_PK_CH_EKEY = new byte[] { (byte) 0xb0,
      (byte) 0x01 };

  public static final byte[] EF_INFOBOX = new byte[] { (byte) 0xc0, (byte) 0x02 };

  public static final int EF_INFOBOX_MAX_SIZE = 1500;

  public static final byte KID_PIN_SIG = (byte) 0x81;

  public static final byte KID_PIN_DEC = (byte) 0x81;

  public static final byte KID_PIN_INF = (byte) 0x83;

  public static final byte[] DST_SIG = new byte[] { (byte) 0x84, (byte) 0x01, // tag
      // ,
      // length
      // (
      // key
      // ID
      // )
      (byte) 0x88, // SK.CH.SIGN
      (byte) 0x80, (byte) 0x01, // tag, length (algorithm ID)
      (byte) 0x14 // ECDSA
  };

  public static final byte[] DST_DEC = new byte[] { (byte) 0x84, (byte) 0x01, // tag
      // ,
      // length
      // (
      // key
      // ID
      // )
      (byte) 0x88, // SK.CH.EKEY
      (byte) 0x80, (byte) 0x01, // tag, length (algorithm ID)
      (byte) 0x01 // RSA // TODO: Not verified yet
  };

  public ACOSCard() {
    super("at/gv/egiz/smcc/ACOSCard");
  }

  /* (non-Javadoc)
   * @see at.gv.egiz.smcc.SignatureCard#getCertificate(at.gv.egiz.smcc.SignatureCard.KeyboxName)
   */
  public byte[] getCertificate(KeyboxName keyboxName)
      throws SignatureCardException {
  
    byte[] aid;
    byte[] efc;
    int maxsize;
    if (keyboxName == KeyboxName.SECURE_SIGNATURE_KEYPAIR) {
      aid = AID_SIG;
      efc = EF_C_CH_DS;
      maxsize = EF_C_CH_DS_MAX_SIZE;
    } else if (keyboxName == KeyboxName.CERITIFIED_KEYPAIR) {
      aid = AID_DEC;
      efc = EF_C_CH_EKEY;
      maxsize = EF_C_CH_EKEY_MAX_SIZE;
    } else {
      throw new IllegalArgumentException("Keybox " + keyboxName
          + " not supported.");
    }

    log.debug("Get certificate for keybox '" + keyboxName.getKeyboxName() + "'" +
        " (AID=" + toString(aid) + " EF=" + toString(efc) + ").");
    
    try {
      Card card = getCardChannel().getCard();
      try {
        card.beginExclusive();
        return readTLVFile(aid, efc, maxsize + 15000);
      } catch (FileNotFoundException e) {
        // if certificate is not present, 
        // the citizen card application has not been activated
        throw new NotActivatedException();
      } finally {
        card.endExclusive();
      }
    } catch (CardException e) {
      throw new SignatureCardException("Failed to get exclusive card access.");
    }

    
  }

  /* (non-Javadoc)
   * @see at.gv.egiz.smcc.SignatureCard#getInfobox(java.lang.String, at.gv.egiz.smcc.PINProvider, java.lang.String)
   */
  public byte[] getInfobox(String infobox, PINProvider provider, String domainId)
      throws SignatureCardException {
  
    if ("IdentityLink".equals(infobox)) {
  
      PINSpec spec = new PINSpec(4, 4, "[0-9]", getResourceBundle().getString("inf.pin.name"));
      
      try {
        Card card = getCardChannel().getCard();
        try {
          card.beginExclusive();
          return readTLVFilePIN(AID_DEC, EF_INFOBOX, KID_PIN_INF, provider,
              spec, EF_INFOBOX_MAX_SIZE);
        } catch (FileNotFoundException e) {
          // if certificate is not present, 
          // the citizen card application has not been activated
          throw new NotActivatedException();
        } finally {
          card.endExclusive();
        }
      } catch (CardException e) {
        throw new SignatureCardException("Failed to get exclusive card access.");
      }
  
    } else {
      throw new IllegalArgumentException("Infobox '" + infobox
          + "' not supported.");
    }
  
  }

  public byte[] createSignature(byte[] hash, KeyboxName keyboxName,
      PINProvider provider) throws SignatureCardException {
  
    if (hash.length != 20) {
      throw new IllegalArgumentException("Hash value must be of length 20.");
    }
  
    try {
      Card card = getCardChannel().getCard();
      try {
        card.beginExclusive();
        
        if (KeyboxName.SECURE_SIGNATURE_KEYPAIR.equals(keyboxName)) {

          // SELECT DF
          selectFileFID(DF_SIG);
          // VERIFY
          verifyPIN(provider, new PINSpec(6, 10, "[0-9]", getResourceBundle()
              .getString("sig.pin.name")), KID_PIN_SIG);
          // MSE: SET DST
          mseSetDST(0x81, 0xb6, DST_SIG);
          // PSO: HASH
          psoHash(hash);
          // PSO: COMPUTE DIGITAL SIGNATURE
          return psoComputDigitalSiganture();
      
        } else if (KeyboxName.CERITIFIED_KEYPAIR.equals(keyboxName)) {

          // SELECT DF
          selectFileFID(DF_DEC);
          // VERIFY
          verifyPIN(provider, new PINSpec(4, 4, "[0-9]", getResourceBundle()
              .getString("dec.pin.name")), KID_PIN_DEC);
          // MSE: SET DST
          mseSetDST(0x41, 0xa4, DST_DEC);
          // INTERNAL AUTHENTICATE
          return internalAuthenticate(hash);

          
          // 00 88 10 00 23 30 21 30 09 06 05 2B 0E 03 02 1A 05 00 04 14 54 26 F0 EA  AF EA F0 4E D4 A1 AD BF 66 D4 A5 9B 45 6F AF 79 00
          // 00 88 10 00 23 30 21 30 09 06 05 2B 0E 03 02 1A 05 00 04 14 DF 8C AB 8F E2 AD AC 7B 5A AF BE E9 44 5E 95 99 FA AF 2F 48 00
          
        } else {
          throw new IllegalArgumentException("KeyboxName '" + keyboxName
              + "' not supported.");
        }
        
      } catch (FileNotFoundException e) {
        // if certificate is not present, 
        // the citizen card application has not been activated
        throw new NotActivatedException();
      } finally {
        card.endExclusive();
      }
    } catch (CardException e) {
      throw new SignatureCardException("Failed to get exclusive card access.");
    }

  }

  protected byte[] selectFileAID(byte[] fid) throws CardException, SignatureCardException {
    CardChannel channel = getCardChannel();
    ResponseAPDU resp = transmit(channel, new CommandAPDU(0x00, 0xA4, 0x04,
        0x00, fid, 256));
    if (resp.getSW() != 0x9000) {
      throw new SignatureCardException("Failed to select file (AID="
          + toString(fid) + "): SW=" + Integer.toHexString(resp.getSW()) + ".");
    } else {
      return resp.getBytes();
    }
  }

  protected ResponseAPDU selectFileFID(byte[] fid) throws CardException, SignatureCardException {
    CardChannel channel = getCardChannel();
    return transmit(channel, new CommandAPDU(0x00, 0xA4, 0x00,
        0x00, fid, 256));
  }

  protected int verifyPIN(String pin, byte kid) throws CardException, SignatureCardException {
    
    CardChannel channel = getCardChannel();

    byte[] asciiPIN = pin.getBytes(Charset.forName("ASCII"));
    byte[] encodedPIN = new byte[8];
    System.arraycopy(asciiPIN, 0, encodedPIN, 0, Math.min(asciiPIN.length,
        encodedPIN.length));

    ResponseAPDU resp = transmit(channel, new CommandAPDU(0x00, 0x20, 0x00,
        kid, encodedPIN), false);
    
    if (resp.getSW() == 0x63c0) {
      throw new LockedException("PIN locked.");
    } else if (resp.getSW1() == 0x63 && resp.getSW2() >> 4 == 0xc) {
      // return number of possible retries
      return resp.getSW2() & 0x0f;
    } else if (resp.getSW() == 0x6983) {
      throw new NotActivatedException();
    } else if (resp.getSW() == 0x9000) {
      return -1;
    } else {
      throw new SignatureCardException("Failed to verify pin: SW="
          + Integer.toHexString(resp.getSW()) + ".");
    }

  }
  
  /**
   * 
   * @param pinProvider
   * @param spec
   *          the PIN spec to be given to the pinProvider
   * @param kid
   *          the KID (key identifier) of the PIN to be verified
   * @throws CancelledException
   *           if the user canceld the operation
   * @throws javax.smartcardio.CardException
   * @throws at.gv.egiz.smcc.SignatureCardException
   */
  protected void verifyPIN(PINProvider pinProvider, PINSpec spec, byte kid)
      throws CardException, CancelledException, SignatureCardException {

    int retries = -1;
    do {
      String pin = pinProvider.providePIN(spec, retries);
      if (pin == null) {
        // user canceled operation
        throw new CancelledException("User canceled operation");
      } 
      retries = verifyPIN(pin, kid);
    } while (retries > 0);
      
  }
    
  void mseSetDST(int p1, int p2, byte[] dst) throws CardException, SignatureCardException {
    CardChannel channel = getCardChannel();
    ResponseAPDU resp = transmit(channel, new CommandAPDU(0x00, 0x22, p1,
        p2, dst));
    if (resp.getSW() != 0x9000) {
      throw new SignatureCardException("MSE:SET DST failed: SW="
          + Integer.toHexString(resp.getSW()));
    }
  }

  void psoHash(byte[] hash) throws CardException, SignatureCardException {
    CardChannel channel = getCardChannel();
    ResponseAPDU resp = transmit(channel, new CommandAPDU(0x00, 0x2A, 0x90,
        0x81, hash));
    if (resp.getSW() != 0x9000) {
      throw new SignatureCardException("PSO:HASH failed: SW="
          + Integer.toHexString(resp.getSW()));
    }
  }

  byte[] psoComputDigitalSiganture() throws CardException,
      SignatureCardException {
    CardChannel channel = getCardChannel();
    ResponseAPDU resp = transmit(channel, new CommandAPDU(0x00, 0x2A, 0x9E,
        0x9A, 256));
    if (resp.getSW() != 0x9000) {
      throw new SignatureCardException(
          "PSO: COMPUTE DIGITAL SIGNATRE failed: SW="
              + Integer.toHexString(resp.getSW()));
    } else {
      return resp.getData();
    }
  }
  
  byte[] internalAuthenticate(byte[] hash) throws CardException, SignatureCardException {
    byte[] digestInfo = new byte[] {
        (byte) 0x30, (byte) 0x21, (byte) 0x30, (byte) 0x09, (byte) 0x06, (byte) 0x05, (byte) 0x2B, (byte) 0x0E, 
        (byte) 0x03, (byte) 0x02, (byte) 0x1A, (byte) 0x05, (byte) 0x00, (byte) 0x04
    };
    
    byte[] data = new byte[digestInfo.length + hash.length + 1];
    
    System.arraycopy(digestInfo, 0, data, 0, digestInfo.length);
    data[digestInfo.length] = (byte) hash.length;
    System.arraycopy(hash, 0, data, digestInfo.length + 1, hash.length);
    
    CardChannel channel = getCardChannel();
    
    ResponseAPDU resp = transmit(channel, new CommandAPDU(0x00, 0x88, 0x10, 0x00, data, 256));
    if (resp.getSW() != 0x9000) {
      throw new SignatureCardException("INTERNAL AUTHENTICATE failed: SW=" + Integer.toHexString(resp.getSW()));
    } else {
      return resp.getData();
    }
  }

  public String toString() {
    return "a-sign premium";
  }
}
