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
  
  /**
   * Creates an new instance.
   */
  public STARCOSCard() {
    super("at/gv/egiz/smcc/STARCOSCard");
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
 
        PINSpec spec = new PINSpec(4, 4, "[0-9]", getResourceBundle().getString("card.pin.name"));
        
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

        PINSpec spec = new PINSpec(6, 10, "[0-9]", getResourceBundle().getString("sig.pin.name"));
        
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

        PINSpec spec = new PINSpec(4, 4, "[0-9]", getResourceBundle().getString("card.pin.name"));
 
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
   * @throws CardException
   *           if communication with the smart card fails.
   * @throws NotActivatedException
   *           if the card application has not been activated
   * @throws SignatureCardException
   *           if VERIFY PIN fails
   */
  @Override
  protected int verifyPIN(String pin, byte kid) throws CardException, SignatureCardException {
    
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
    } else if (resp.getSW() == 0x6983) {
      throw new LockedException();
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
  
  public String toString() {
    return "e-card";
  }

 

}
