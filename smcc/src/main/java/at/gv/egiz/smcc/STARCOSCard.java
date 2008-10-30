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

import java.math.BigInteger;
import java.util.Arrays;

import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class STARCOSCard extends AbstractSignatureCard implements SignatureCard {
  
  /**
   * Logging facility.
   */
  private static Log log = LogFactory.getLog(STARCOSCard.class);

  public static final byte[] MF = new byte[] { (byte) 0x3F, (byte) 0x00 };

  public static final byte[] AID_INFOBOX = new byte[] { (byte) 0xd0,
      (byte) 0x40, (byte) 0x00, (byte) 0x00, (byte) 0x17, (byte) 0x00,
      (byte) 0x18, (byte) 0x01 };

  public static final byte[] EF_INFOBOX = new byte[] { (byte) 0xef, (byte) 0x01 };

  public static final byte[] AID_SVSIG_CERT = new byte[] { (byte) 0xd0,
      (byte) 0x40, (byte) 0x00, (byte) 0x00, (byte) 0x17, (byte) 0x00,
      (byte) 0x10, (byte) 0x01 };

  public static final byte[] EF_SVSIG_CERT_CA = new byte[] { (byte) 0x2f,
      (byte) 0x01 };

  public static final byte[] EF_SVSIG_CERT = new byte[] { (byte) 0x2f,
      (byte) 0x02 };

  // Sichere Signatur (SS)

  public static final byte[] AID_DF_SS = new byte[] { (byte) 0xd0, (byte) 0x40,
      (byte) 0x00, (byte) 0x00, (byte) 0x17, (byte) 0x00, (byte) 0x12,
      (byte) 0x01 };

  public static final byte[] EF_C_X509_CH_DS = new byte[] { (byte) 0xc0,
      (byte) 0x00 };

  public static final byte[] EF_C_X509_CA_CS_DS = new byte[] { (byte) 0xc6,
      (byte) 0x08 };

  public static final byte[] DST_SS = new byte[] { (byte) 0x84, (byte) 0x03, // tag
      // ,
      // length
      // (
      // key
      // desc
      // .
      // )
      (byte) 0x80, (byte) 0x02, (byte) 0x00, // local, key ID, key version
      (byte) 0x89, (byte) 0x03, // tag, length (algorithm ID)
      (byte) 0x13, (byte) 0x35, (byte) 0x10 // ECDSA
  };

  public static final byte KID_PIN_SS = (byte) 0x81;

  // Gewöhnliche Signatur (GS)

  public static final byte[] AID_DF_GS = new byte[] { (byte) 0xd0, (byte) 0x40,
      (byte) 0x00, (byte) 0x00, (byte) 0x17, (byte) 0x00, (byte) 0x13,
      (byte) 0x01 };

  public static final byte[] EF_C_X509_CH_AUT = new byte[] { (byte) 0x2f,
      (byte) 0x01 };

  public static final byte[] EF_C_X509_CA_CS = new byte[] { (byte) 0x2f,
      (byte) 0x02 };

  public static final byte[] DST_GS = new byte[] { (byte) 0x84, (byte) 0x03, // tag
      // ,
      // length
      // (
      // key
      // desc
      // .
      // )
      (byte) 0x80, (byte) 0x02, (byte) 0x00, // local, key ID, key version
      (byte) 0x89, (byte) 0x03, // tag, length (algorithm ID)
      (byte) 0x13, (byte) 0x35, (byte) 0x10 // ECDSA
  };

  public static final byte KID_PIN_CARD = (byte) 0x01;
  
  /**
   * Creates an new instance.
   */
  public STARCOSCard() {
    super("at/gv/egiz/smcc/STARCOSCard");
  }
 
  /* (non-Javadoc)
   * @see at.gv.egiz.smcc.SignatureCard#getCertificate(at.gv.egiz.smcc.SignatureCard.KeyboxName)
   */
  public byte[] getCertificate(KeyboxName keyboxName)
      throws SignatureCardException {

    byte[] aid;
    byte[] efc;
    if (keyboxName == KeyboxName.SECURE_SIGNATURE_KEYPAIR) {
      aid = AID_DF_SS;
      efc = EF_C_X509_CH_DS;
    } else if (keyboxName == KeyboxName.CERITIFIED_KEYPAIR) {
      aid = AID_DF_GS;
      efc = EF_C_X509_CH_AUT;
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
        return readTLVFile(aid, efc, 2000);
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
  
      PINSpec spec = new PINSpec(4, 4, "[0-9]", getResourceBundle().getString("card.pin.name"));
      
      try {
        Card card = getCardChannel().getCard();
        try {
          card.beginExclusive();
          return readTLVFilePIN(AID_INFOBOX, EF_INFOBOX, KID_PIN_CARD,
              provider, spec, 2000);
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

  /* (non-Javadoc)
   * @see at.gv.egiz.smcc.SignatureCard#createSignature(byte[], at.gv.egiz.smcc.SignatureCard.KeyboxName, at.gv.egiz.smcc.PINProvider)
   */
  public byte[] createSignature(byte[] hash, KeyboxName keyboxName,
      PINProvider provider) throws SignatureCardException {
  
    if (hash.length != 20) {
      throw new IllegalArgumentException("Hash value must be of length 20.");
    }
  
    byte[] aid;
    byte kid;
    byte[] dst;
    PINSpec spec;
    if (KeyboxName.SECURE_SIGNATURE_KEYPAIR.equals(keyboxName)) {
      aid = AID_DF_SS;
      kid = KID_PIN_SS;
      dst = DST_SS;
      spec = new PINSpec(6, 10, "[0-9]", getResourceBundle().getString("sig.pin.name"));
  
    } else if (KeyboxName.CERITIFIED_KEYPAIR.equals(keyboxName)) {
      aid = AID_DF_GS;
      kid = KID_PIN_CARD;
      dst = DST_GS;
      spec = new PINSpec(4, 4, "[0-9]", getResourceBundle().getString("card.pin.name"));
  
    } else {
      throw new IllegalArgumentException("KeyboxName '" + keyboxName
          + "' not supported.");
    }
  
    try {
      Card card = getCardChannel().getCard();
      try {
        card.beginExclusive();

        // SELECT MF
        selectMF();
        // SELECT DF
        selectFileAID(aid);
        // VERIFY
        verifyPIN(provider, spec, kid);
        // MSE: SET DST
        mseSetDST(dst);
        // PSO: HASH
        psoHash(hash);
        // PSO: COMPUTE DIGITAL SIGNATURE
        return psoComputDigitalSiganture();


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
        0x04, fid, 256));
    if (resp.getSW() != 0x9000) {
      throw new SignatureCardException("Failed to select file (AID="
          + toString(fid) + "): SW=" + Integer.toHexString(resp.getSW()) + ".");
    } else {
      return resp.getBytes();
    }
  }

  void selectMF() throws CardException, SignatureCardException {
    CardChannel channel = getCardChannel();
    ResponseAPDU resp = transmit(channel, new CommandAPDU(0x00, 0xA4, 0x00,
        0x0C));
    if (resp.getSW() != 0x9000) {
      throw new SignatureCardException("Failed to select MF: SW="
          + Integer.toHexString(resp.getSW()) + ".");
    }
  }

  protected ResponseAPDU selectFileFID(byte[] fid) throws CardException, SignatureCardException {
    CardChannel channel = getCardChannel();
    return transmit(channel, new CommandAPDU(0x00, 0xA4, 0x02,
        0x04, fid, 256));
  }

  void mseSetDST(byte[] dst) throws CardException, SignatureCardException {
    CardChannel channel = getCardChannel();
    ResponseAPDU resp = transmit(channel, new CommandAPDU(0x00, 0x22, 0x41,
        0xB6, dst));
    if (resp.getSW() != 0x9000) {
      throw new SignatureCardException("MSE:SET DST failed: SW="
          + Integer.toHexString(resp.getSW()));
    }
  }

  void psoHash(byte[] hash) throws CardException, SignatureCardException {
    byte[] data = new byte[hash.length + 2];
    data[0] = (byte) 0x90; // tag
    data[1] = (byte) (hash.length); // length
    System.arraycopy(hash, 0, data, 2, hash.length);

    CardChannel channel = getCardChannel();
    ResponseAPDU resp = transmit(channel, new CommandAPDU(0x00, 0x2A, 0x90,
        0xA0, data));
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

  /**
   * VERIFY PIN
   * <p>
   * If <code>pin</code> is <code>null</code> only the PIN status is checked and
   * returned.
   * </p>
   * 
   * @param pin
   *          the PIN (may be <code>null</code>)
   * @param kid
   *          the KID of the PIN to be verified
   * 
   * @return -1 if VERIFY PIN was successful, or the number of possible retries 
   * 
   * @throws CardException
   *           if communication with the smart card fails.
   * @throws NotActivatedException
   *           if the card application has not been activated
   * @throws SignatureCardException
   *           if VERIFY PIN fails
   */
  private int verifyPIN(String pin, byte kid) throws CardException, SignatureCardException {
    
    CardChannel channel = getCardChannel();

    ResponseAPDU resp;
    if (pin == null) {
      resp = transmit(channel, new CommandAPDU(0x00, 0x20, 0x00, kid));
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

      resp = transmit(channel, new CommandAPDU(0x00, 0x20, 0x00, kid, pinBlock), false);
      
    }

    if (resp.getSW() == 0x63c0) {
      throw new LockedException("PIN locked.");
    } else if (resp.getSW1() == 0x63 && resp.getSW2() >> 4 == 0xc) {
      // return number of possible retries
      return resp.getSW2() & 0x0f;
    } else if (resp.getSW() == 0x6984) {
      // PIN LCS = "Initialized" (-> not activated)
      throw new NotActivatedException("PIN not set.");
    } else if (resp.getSW() == 0x9000) {
      return -1; // success
    } else {
      throw new SignatureCardException("Failed to verify pin: SW="
          + Integer.toHexString(resp.getSW()));
    }
    
  }
  
  /* (non-Javadoc)
   * @see at.gv.egiz.smcc.AbstractSignatureCard#verifyPIN(at.gv.egiz.smcc.PINProvider, at.gv.egiz.smcc.PINSpec, byte, int)
   */
  protected void verifyPIN(PINProvider pinProvider, PINSpec spec, byte kid)
      throws CardException, SignatureCardException {

    int retries = verifyPIN(null, kid);
    do {
      String pin = pinProvider.providePIN(spec, retries);
      if (pin == null) {
        // user canceled operation
        throw new CancelledException("User canceld operation.");
      }
      retries = verifyPIN(pin, kid);
    } while (retries > 0);

  }

  public String toString() {
    return "eCard";
  }

 

}
