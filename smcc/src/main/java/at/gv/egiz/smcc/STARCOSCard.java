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

  /**
   * Application ID <em>SV-Personendaten</em>.
   */
  public static final byte[] AID_SV_PERSONENDATEN = new byte[] { 
    (byte) 0xD0, (byte) 0x40, (byte) 0x00, (byte) 0x00, 
    (byte) 0x17, (byte) 0x01, (byte) 0x01, (byte) 0x01
  };
  
  /**
   * File ID <em>Grunddaten</em> ({@link #AID_SV_PERSONENDATEN}).
   */
  public static final byte[] FID_GRUNDDATEN = new byte[] {
    (byte) 0xEF, (byte) 0x01
  };

  /**
   * File ID <em>EHIC</em> ({@link #AID_SV_PERSONENDATEN}).
   */
  public static final byte[] FID_EHIC = new byte[] {
    (byte) 0xEF, (byte) 0x02
  };
  
  /**
   * File ID <em>Status</em> ({@link #AID_SV_PERSONENDATEN}).
   */
  public static final byte[] FID_SV_PERSONENBINDUNG = new byte[] {
    (byte) 0xEF, (byte) 0x03
  };

  /**
   * File ID <em>Status</em> ({@link #AID_SV_PERSONENDATEN}).
   */
  public static final byte[] FID_STATUS = new byte[] {
    (byte) 0xEF, (byte) 0x04
  };

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

  private static final int PINSPEC_CARD = 0;
  private static final int PINSPEC_SS = 1;

  /**
   * Creates an new instance.
   */
  public STARCOSCard() {
    super("at/gv/egiz/smcc/STARCOSCard");
    pinSpecs.add(PINSPEC_CARD, 
            new PINSpec(4, 12, "[0-9]",
              getResourceBundle().getString("card.pin.name"),
              KID_PIN_CARD, null));
    pinSpecs.add(PINSPEC_SS,
            new PINSpec(6, 12, "[0-9]",
              getResourceBundle().getString("sig.pin.name"),
              KID_PIN_SS, AID_DF_SS));
  }
 
  @Override
  public byte[] getCertificate(KeyboxName keyboxName)
      throws SignatureCardException, InterruptedException {

    try {
      
      if (keyboxName == KeyboxName.SECURE_SIGNATURE_KEYPAIR) {
        
        try {
          getCard().beginExclusive();
          return readTLVFile(AID_DF_SS, EF_C_X509_CH_DS, 2000);
        } catch (FileNotFoundException e) {
          throw new NotActivatedException();
        } finally {
          getCard().endExclusive();
        }
        
      } else if (keyboxName == KeyboxName.CERITIFIED_KEYPAIR) {

        try {
          getCard().beginExclusive();
          return readTLVFile(AID_DF_GS, EF_C_X509_CH_AUT, 2000);
        } catch (FileNotFoundException e) {
          throw new NotActivatedException();
        } finally {
          getCard().endExclusive();
        }

      } else {
        throw new IllegalArgumentException("Keybox " + keyboxName
            + " not supported.");
      }
      
    } catch (CardException e) {
      log.warn(e);
      throw new SignatureCardException("Failed to access card.", e);
    }

  }
  
  @Override
  public byte[] getInfobox(String infobox, PINProvider provider, String domainId)
      throws SignatureCardException, InterruptedException {
  
    try {
      if ("IdentityLink".equals(infobox)) {
 
        PINSpec spec = pinSpecs.get(PINSPEC_CARD);
        //new PINSpec(4, 4, "[0-9]", getResourceBundle().getString("card.pin.name"));
        
        int retries = -1;
        String pin = null;
        boolean pinRequiered = false;

        do {
          if (pinRequiered) {
            pin = provider.providePIN(spec, retries);
            if (pin == null) {
              throw new CancelledException();
            }
          }
          try {
            getCard().beginExclusive();
            return readTLVFile(AID_INFOBOX, EF_INFOBOX, pin, KID_PIN_CARD, 2000);
          } catch (FileNotFoundException e) {
            throw new NotActivatedException();
          } catch (SecurityStatusNotSatisfiedException e) {
            pinRequiered = true;
            retries = verifyPIN(null, KID_PIN_CARD);
          } catch (VerificationFailedException e) {
            pinRequiered = true;
            retries = e.getRetries();
          } finally {
            getCard().endExclusive();
          }
        } while (retries != 0);

        throw new LockedException();

        
      } else if ("EHIC".equals(infobox)) {
        
        try {
          getCard().beginExclusive();
          return readTLVFile(AID_SV_PERSONENDATEN, FID_EHIC, 126);
        } finally {
          getCard().endExclusive();
        }
        
      } else if ("Grunddaten".equals(infobox)) {
        
        try {
          getCard().beginExclusive();
          return readTLVFile(AID_SV_PERSONENDATEN, FID_GRUNDDATEN, 550);
        } finally {
          getCard().endExclusive();
        }
        
      } else if ("SV-Personenbindung".equals(infobox)) {
        
        try {
          getCard().beginExclusive();
          return readTLVFile(AID_SV_PERSONENDATEN, FID_SV_PERSONENBINDUNG, 500);
        } finally {
          getCard().endExclusive();
        }
        
      } else if ("Status".equals(infobox)) {
        
        try {
          getCard().beginExclusive();
          return readRecords(AID_SV_PERSONENDATEN, FID_STATUS, 1, 5);
        } finally {
          getCard().endExclusive();
        }
        
      } else {
        throw new IllegalArgumentException("Infobox '" + infobox
            + "' not supported.");
      }
      
    } catch (CardException e) {
      log.warn(e);
      throw new SignatureCardException("Failed to access card.", e);
    }
  
  }

  @Override
  public byte[] createSignature(byte[] hash, KeyboxName keyboxName,
      PINProvider provider) throws SignatureCardException, InterruptedException {
  
    if (hash.length != 20) {
      throw new IllegalArgumentException("Hash value must be of length 20.");
    }
  
    try {
      
      if (KeyboxName.SECURE_SIGNATURE_KEYPAIR.equals(keyboxName)) {

        PINSpec spec = pinSpecs.get(PINSPEC_SS);
        //new PINSpec(6, 10, "[0-9]", getResourceBundle().getString("sig.pin.name"));
        
        int retries = -1;
        String pin = null;

        do {
          try {
            getCard().beginExclusive();
            selectFileAID(AID_DF_SS);
            retries = verifyPIN(null, KID_PIN_SS);
          } finally {
            getCard().endExclusive();
          }
          pin = provider.providePIN(spec, retries);
          if (pin == null) {
            throw new CancelledException();
          }
          try {
            getCard().beginExclusive();
            return createSignature(hash, AID_DF_SS, pin, KID_PIN_SS, DST_SS);
          } catch (VerificationFailedException e) {
            retries = e.getRetries();
          } finally {
            getCard().endExclusive();
          }
        } while (retries != 0);

        throw new LockedException();

 
      } else if (KeyboxName.CERITIFIED_KEYPAIR.equals(keyboxName)) {

        PINSpec spec = pinSpecs.get(PINSPEC_CARD);
        //new PINSpec(4, 4, "[0-9]", getResourceBundle().getString("card.pin.name"));
 
        int retries = -1;
        String pin = null;
        boolean pinRequiered = false;

        do {
          if (pinRequiered) {
            pin = provider.providePIN(spec, retries);
            if (pin == null) {
              throw new CancelledException();
            }
          }
          try {
            getCard().beginExclusive();
            return createSignature(hash, AID_DF_GS, pin, KID_PIN_CARD, DST_GS);
          } catch (FileNotFoundException e) {
            throw new NotActivatedException();
          } catch (SecurityStatusNotSatisfiedException e) {
            pinRequiered = true;
            retries = verifyPIN(null, KID_PIN_CARD);
          } catch (VerificationFailedException e) {
            pinRequiered = true;
            retries = e.getRetries();
          } finally {
            getCard().endExclusive();
          }
        } while (retries != 0);

        throw new LockedException();
        
      } else {
        throw new IllegalArgumentException("KeyboxName '" + keyboxName
            + "' not supported.");
      }

    } catch (CardException e) {
      log.warn(e);
      throw new SignatureCardException("Failed to access card.", e);
    }
  
  }

  protected ResponseAPDU selectFileFID(byte[] fid) throws CardException, SignatureCardException {
    CardChannel channel = getCardChannel();
    return transmit(channel, new CommandAPDU(0x00, 0xA4, 0x02,
        0x04, fid, 256));
  }

  private byte[] createSignature(byte[] hash, byte[] aid, String pin, byte kid,
      byte[] dst) throws CardException, SignatureCardException {
    
    // SELECT MF
    selectMF();
    // SELECT DF
    selectFileAID(aid);
    // VERIFY
    int retries = verifyPIN(pin, kid);
    if (retries != -1) {
      throw new VerificationFailedException(retries);
    }
    // MSE: SET DST
    mseSetDST(dst);
    // PSO: HASH
    psoHash(hash);
    // PSO: COMPUTE DIGITAL SIGNATURE
    return psoComputDigitalSiganture();

    
  }

  
  private void selectMF() throws CardException, SignatureCardException {
    CardChannel channel = getCardChannel();
    ResponseAPDU resp = transmit(channel, new CommandAPDU(0x00, 0xA4, 0x00,
        0x0C));
    if (resp.getSW() != 0x9000) {
      throw new SignatureCardException("Failed to select MF: SW="
          + Integer.toHexString(resp.getSW()) + ".");
    }
  }

  private void mseSetDST(byte[] dst) throws CardException, SignatureCardException {
    CardChannel channel = getCardChannel();
    ResponseAPDU resp = transmit(channel, new CommandAPDU(0x00, 0x22, 0x41,
        0xB6, dst));
    if (resp.getSW() != 0x9000) {
      throw new SignatureCardException("MSE:SET DST failed: SW="
          + Integer.toHexString(resp.getSW()));
    }
  }

  private void psoHash(byte[] hash) throws CardException, SignatureCardException {
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

  private byte[] psoComputDigitalSiganture() throws CardException,
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
   * @throws LockedException
   *            if the pin is locked
   * @throws NotActivatedException
   *           if the card application has not been activated
   * @throws SignatureCardException
   *           if VERIFY PIN fails for some other reason (card communication error)
   */
  @Override
  public int verifyPIN(String pin, byte kid) throws LockedException, NotActivatedException, SignatureCardException {
    try {
      CardChannel channel = getCardChannel();

      ResponseAPDU resp;
      if (pin == null) {
        resp = transmit(channel, new CommandAPDU(0x00, 0x20, 0x00, kid));
      } else {
        // BCD encode PIN and marshal PIN block
        byte[] pinBlock = encodePINBlock(pin);
        resp = transmit(channel, new CommandAPDU(0x00, 0x20, 0x00, kid, pinBlock), false);

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
        throw new NotActivatedException();
      } else if (resp.getSW() == 0x9000) {
        return -1; // success
      } else {
        throw new SignatureCardException("Failed to verify pin: SW="
            + Integer.toHexString(resp.getSW()));
      }
    } catch (CardException ex) {
      log.error("smart card communication failed: " + ex.getMessage());
      throw new SignatureCardException("smart card communication failed: " + ex.getMessage(), ex);
    }
  }

  @Override
  public void reset() throws SignatureCardException {
    try {
      super.reset();
      log.debug("select MF (e-card workaround)");
      CardChannel channel = getCardChannel();
      ResponseAPDU resp = transmit(channel, new CommandAPDU(0x00, 0xA4, 0x00, 0x0C));
      if (resp.getSW() != 0x9000) {
        throw new SignatureCardException("Failed to select MF after RESET: SW=" + Integer.toHexString(resp.getSW()) + ".");
      }
    } catch (CardException ex) {
      log.error("Failed to select MF after RESET: " + ex.getMessage(), ex);
      throw new SignatureCardException("Failed to select MF after RESET");
    }
  }


  public String toString() {
    return "e-card";
  }

  /**
   * BCD encodes the pin, pads with 0xFF and prepends the pins length
   * @param pin
   * @return a 8 byte pin block consisting of length byte (0x2X),
   * the BCD encoded pin and a 0xFF padding
   */
  private byte[] encodePINBlock(String pin) {
    char[] pinChars = pin.toCharArray();
    int numDigits = pinChars.length;
    int numBytes = (int) Math.ceil(numDigits/2.0);

    byte[] pinBlock = new byte[8];
    pinBlock[0] = (byte) (0x20 | numDigits);

    for (int i = 0; i < numBytes; i++) {
      int p1 = 16*Character.digit(pinChars[i*2], 16);
      int p2 = (i*2+1 < numDigits) ? Character.digit(pinChars[i*2+1], 16) : 0xf;
      pinBlock[i+1] = (byte) (p1 + p2);
    }
    Arrays.fill(pinBlock, numBytes + 1, pinBlock.length, (byte) 0xff);
//    log.trace("BCD encoded PIN block: " + SMCCHelper.toString(pinBlock));

    return pinBlock;
  }

  @Override
  public void activatePIN(PINSpec pinSpec, String pin)
          throws SignatureCardException {
    Card icc = getCard();
    try {
      icc.beginExclusive();
      CardChannel channel = icc.getBasicChannel();

      if (pinSpec.getContextAID() != null) {
        ResponseAPDU responseAPDU = transmit(channel,
                new CommandAPDU(0x00, 0xa4, 0x04, 0x0c, pinSpec.getContextAID()));
        if (responseAPDU.getSW() != 0x9000) {
          icc.endExclusive();
          String msg = "Select AID " + SMCCHelper.toString(pinSpec.getContextAID()) +
                  ": SW=" + Integer.toHexString(responseAPDU.getSW());
          log.error(msg);
          throw new SignatureCardException(msg);
        }
      }

      ResponseAPDU responseAPDU = transmit(channel,
              new CommandAPDU(0x00, 0x24, 0x01, pinSpec.getKID(), encodePINBlock(pin)),
              false);

      icc.endExclusive();

      log.debug("activate pin returned SW=" + Integer.toHexString(responseAPDU.getSW()));

       if (responseAPDU.getSW() != 0x9000) {
        String msg = "Failed to activate " + pinSpec.getLocalizedName() +
                ": SW=" + Integer.toHexString(responseAPDU.getSW());
        log.error(msg);
        throw new SignatureCardException(msg);
      }
    } catch (CardException ex) {
      log.error("Failed to activate " + pinSpec.getLocalizedName() +
              ": " + ex.getMessage());
      throw new SignatureCardException(ex.getMessage(), ex);
    }
  }

  /**
   * activates pin (newPIN) if not active
   * @param pinSpec
   * @param oldPIN
   * @param newPIN
   * @throws at.gv.egiz.smcc.LockedException
   * @throws at.gv.egiz.smcc.VerificationFailedException
   * @throws at.gv.egiz.smcc.NotActivatedException
   * @throws at.gv.egiz.smcc.SignatureCardException
   */
  @Override
  public void changePIN(PINSpec pinSpec, String oldPIN, String newPIN)
          throws LockedException, VerificationFailedException, NotActivatedException, SignatureCardException {
    Card icc = getCard();
    try {
      icc.beginExclusive();
      CardChannel channel = icc.getBasicChannel();

      if (pinSpec.getContextAID() != null) {
        ResponseAPDU responseAPDU = transmit(channel,
                new CommandAPDU(0x00, 0xa4, 0x04, 0x0c, pinSpec.getContextAID()));
        if (responseAPDU.getSW() != 0x9000) {
          icc.endExclusive();
          String msg = "Select AID " + SMCCHelper.toString(pinSpec.getContextAID()) +
                  ": SW=" + Integer.toHexString(responseAPDU.getSW());
          log.error(msg);
          throw new SignatureCardException(msg);
        }
      }

      byte[] cmd = new byte[16];
      System.arraycopy(encodePINBlock(oldPIN), 0, cmd, 0, 8);
      System.arraycopy(encodePINBlock(newPIN), 0, cmd, 8, 8);

      ResponseAPDU responseAPDU = transmit(channel,
              new CommandAPDU(0x00, 0x24, 0x00, pinSpec.getKID(), cmd), false);

      icc.endExclusive();

      log.debug("change pin returned SW=" + Integer.toHexString(responseAPDU.getSW()));

      // activates pin (newPIN) if not active
      if (responseAPDU.getSW() == 0x63c0) {
        log.error(pinSpec.getLocalizedName() + " locked");
        throw new LockedException();
      } else if (responseAPDU.getSW1() == 0x63 && responseAPDU.getSW2() >> 4 == 0xc) {
        int retries = responseAPDU.getSW2() & 0x0f;
        log.error("wrong " + pinSpec.getLocalizedName() + ", " + retries + " retries");
        throw new VerificationFailedException(retries);
      } else if (responseAPDU.getSW() == 0x6983) {
        log.error(pinSpec.getLocalizedName() + " locked");
        throw new LockedException();
      } else if (responseAPDU.getSW() != 0x9000) {
        String msg = "Failed to change " + pinSpec.getLocalizedName() +
                ": SW=" + Integer.toHexString(responseAPDU.getSW());
        log.error(msg);
        throw new SignatureCardException(msg);
      }
    } catch (CardException ex) {
      log.error("Failed to change " + pinSpec.getLocalizedName() +
              ": " + ex.getMessage());
      throw new SignatureCardException(ex.getMessage(), ex);
    }
  }

}
