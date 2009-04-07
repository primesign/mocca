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

import at.gv.egiz.smcc.ccid.CCID;
import at.gv.egiz.smcc.util.SMCCHelper;
import java.util.Arrays;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class STARCOSCard extends AbstractSignatureCard {

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

  public static final int PINSPEC_CARD = 0;
  public static final int PINSPEC_SS = 1;

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
        boolean pinRequired = false;

        do {
          try {
            getCard().beginExclusive();
            if (pinRequired) {
              char[] pin = provider.providePIN(spec, retries);
              return readTLVFile(AID_INFOBOX, EF_INFOBOX, pin, spec.getKID(), 2000);
            } else {
              return readTLVFile(AID_INFOBOX, EF_INFOBOX, 2000);
            }
          } catch (FileNotFoundException e) {
            throw new NotActivatedException();
          } catch (SecurityStatusNotSatisfiedException e) {
            pinRequired = true;
            retries = verifyPIN(KID_PIN_CARD);
          } catch (VerificationFailedException e) {
            pinRequired = true;
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
        char[] pin = null;

        do {
          try {
            getCard().beginExclusive();
            selectFileAID(AID_DF_SS);
            retries = verifyPIN(KID_PIN_SS); //, null);
          } finally {
            getCard().endExclusive();
          }
          pin = provider.providePIN(spec, retries);
          try {
            getCard().beginExclusive();
            return createSignature(hash, AID_DF_SS, pin, KID_PIN_SS, DST_SS);
          } catch (VerificationFailedException e) {
            retries = e.getRetries();
          } catch (PINFormatException e) {
            log.debug("wrong pin size entered, retry");
          } finally {
            getCard().endExclusive();
          }
        } while (retries != 0);

        throw new LockedException();

 
      } else if (KeyboxName.CERITIFIED_KEYPAIR.equals(keyboxName)) {

        PINSpec spec = pinSpecs.get(PINSPEC_CARD);
        //new PINSpec(4, 4, "[0-9]", getResourceBundle().getString("card.pin.name"));
 
        int retries = -1;
        char[] pin = null;
        boolean pinRequiered = false;

        do {
          if (pinRequiered) {
            pin = provider.providePIN(spec, retries);
          }
          try {
            getCard().beginExclusive();
            return createSignature(hash, AID_DF_GS, pin, KID_PIN_CARD, DST_GS);
          } catch (FileNotFoundException e) {
            throw new NotActivatedException();
          } catch (SecurityStatusNotSatisfiedException e) {
            pinRequiered = true;
            retries = verifyPIN(KID_PIN_CARD);
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


  ////////////////////////////////////////////////////////////////////////
  // PROTECTED METHODS (assume exclusive card access)
  ////////////////////////////////////////////////////////////////////////

  protected ResponseAPDU selectFileFID(byte[] fid) throws CardException, SignatureCardException {
    CardChannel channel = getCardChannel();
    return transmit(channel, new CommandAPDU(0x00, 0xA4, 0x02,
        0x04, fid, 256));
  }

  private byte[] createSignature(byte[] hash, byte[] aid, char[] pin, byte kid,
      byte[] dst) throws CardException, SignatureCardException {
    
    // SELECT MF
    selectMF();
    // SELECT DF
    selectFileAID(aid);
    // VERIFY
    int retries = verifyPIN(kid, pin);
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

  @Override
  protected int verifyPIN(byte kid, char[] pin)
          throws LockedException, NotActivatedException, TimeoutException, CancelledException, PINFormatException, PINOperationAbortedException, SignatureCardException {
    try {
      byte[] sw;
      if (reader.hasFeature(CCID.FEATURE_VERIFY_PIN_DIRECT)) {
        log.debug("verify pin on cardreader");
        sw = reader.verifyPinDirect(getPINVerifyStructure(kid));
//        int sw = (resp[resp.length-2] & 0xff) << 8 | resp[resp.length-1] & 0xff;
      } else if (reader.hasFeature(CCID.FEATURE_VERIFY_PIN_START)) {
        log.debug("verify pin on cardreader");
        sw = reader.verifyPin(getPINVerifyStructure(kid));
      } else {
        byte[] pinBlock = encodePINBlock(pin);
        CardChannel channel = getCardChannel();
        ResponseAPDU resp = transmit(channel,
                new CommandAPDU(0x00, 0x20, 0x00, kid, pinBlock), false);
        sw = new byte[2];
        sw[0] = (byte) resp.getSW1();
        sw[1] = (byte) resp.getSW2();
      }

      if (sw[0] == (byte) 0x90 && sw[1] == (byte) 0x00) {
        return -1;
      } else if (sw[0] == (byte) 0x63 && sw[1] == (byte) 0xc0) {
        throw new LockedException("[63:c0]");
      } else if (sw[0] == (byte) 0x63 && (sw[1] & 0xf0) >> 4 == 0xc) {
        return sw[1] & 0x0f;
      } else if (sw[0] == (byte) 0x69 && sw[1] == (byte) 0x83) {
        //Authentisierungsmethode gesperrt
        throw new LockedException("[69:83]");
      } else if (sw[0] == (byte) 0x69 && sw[1] == (byte) 0x84) {
        //referenzierte Daten sind reversibel gesperrt (invalidated)
        throw new NotActivatedException("[69:84]");
      } else if (sw[0] == (byte) 0x69 && sw[1] == (byte) 0x85) {
        //Benutzungsbedingungen nicht erfüllt
        throw new NotActivatedException("[69:85]");
      } else if (sw[0] == (byte) 0x64 && sw[1] == (byte) 0x00) {
        throw new TimeoutException("[64:00]");
      } else if (sw[0] == (byte) 0x64 && sw[1] == (byte) 0x01) {
        throw new CancelledException("[64:01]");
      } else if (sw[0] == (byte) 0x64 && sw[1] == (byte) 0x03) {
        throw new PINFormatException("[64:03]");
      }
      log.error("Failed to verify pin: SW="
              + SMCCHelper.toString(sw));
      throw new SignatureCardException(SMCCHelper.toString(sw));
      
    } catch (CardException ex) {
      log.error("smart card communication failed: " + ex.getMessage());
      throw new SignatureCardException("smart card communication failed: " + ex.getMessage(), ex);
    }
  }

  @Override
  protected int verifyPIN(byte kid)
          throws LockedException, NotActivatedException, SignatureCardException {
    try {
      CardChannel channel = getCardChannel();
      ResponseAPDU resp = transmit(channel,
              new CommandAPDU(0x00, 0x20, 0x00, kid), false);

      if (resp.getSW() == 0x9000) {
        return -1;
      } else if (resp.getSW() == 0x63c0) {
        throw new LockedException("[63:c0]");
      } else if (resp.getSW1() == 0x63 && (resp.getSW2() & 0xf0) >> 4 == 0xc) {
        return resp.getSW2() & 0x0f;
      } else if (resp.getSW() == 0x6983) {
        //Authentisierungsmethode gesperrt
        throw new LockedException("[69:83]");
      } else if (resp.getSW() == 0x6984) {
        //referenzierte Daten sind reversibel gesperrt (invalidated)
        throw new NotActivatedException("[69:84]");
      } else if (resp.getSW() == 0x6985) {
        //Benutzungsbedingungen nicht erfüllt
        throw new NotActivatedException("[69:85]");
      }
      log.error("Failed to verify pin: SW="
              + Integer.toHexString(resp.getSW()));
      throw new SignatureCardException("[" + Integer.toHexString(resp.getSW()) + "]");

    } catch (CardException ex) {
      log.error("smart card communication failed: " + ex.getMessage());
      throw new SignatureCardException("smart card communication failed: " + ex.getMessage(), ex);
    }
  }

  @Override
  protected int changePIN(byte kid, char[] oldPin, char[] newPin)
          throws LockedException, CancelledException, PINFormatException, PINConfirmationException, TimeoutException, PINOperationAbortedException, SignatureCardException {
    try {
      byte[] sw;
      if (reader.hasFeature(CCID.FEATURE_MODIFY_PIN_DIRECT)) {
        log.debug("modify pin on cardreader");
        sw = reader.modifyPinDirect(getPINModifyStructure(kid));
      } else if (reader.hasFeature(CCID.FEATURE_MODIFY_PIN_START)) {
        log.debug("modify pin on cardreader");
        sw = reader.modifyPin(getPINModifyStructure(kid));
      } else {
        byte[] cmd = new byte[16];
        System.arraycopy(encodePINBlock(oldPin), 0, cmd, 0, 8);
        System.arraycopy(encodePINBlock(newPin), 0, cmd, 8, 8);

        CardChannel channel = getCardChannel();

        ResponseAPDU resp = transmit(channel,
                new CommandAPDU(0x00, 0x24, 0x00, kid, cmd), false);

        sw = new byte[2];
        sw[0] = (byte) resp.getSW1();
        sw[1] = (byte) resp.getSW2();
      }

      // activates pin (newPIN) if not active
      if (sw[0] == (byte) 0x90 && sw[1] == (byte) 0x00) {
        return -1;
      } else if (sw[0] == (byte) 0x63 && sw[1] == (byte) 0xc0) {
        throw new LockedException("[63:c0]");
      } else if (sw[0] == (byte) 0x63 && (sw[1] & 0xf0) >> 4 == 0xc) {
        return sw[1] & 0x0f;
      } else if (sw[0] == (byte) 0x69 && sw[1] == (byte) 0x83) {
        //Authentisierungsmethode gesperrt
        throw new LockedException("[69:83]");
//      } else if (sw[0] == (byte) 0x69 && sw[1] == (byte) 0x84) {
//        //referenzierte Daten sind reversibel gesperrt (invalidated)
//        throw new NotActivatedException("[69:84]");
//      } else if (sw[0] == (byte) 0x69 && sw[1] == (byte) 0x85) {
//        //Benutzungsbedingungen nicht erfüllt
//        throw new NotActivatedException("[69:85]");
      } else if (sw[0] == (byte) 0x64 && sw[1] == (byte) 0x00) {
        throw new TimeoutException("[64:00]");
      } else if (sw[0] == (byte) 0x64 && sw[1] == (byte) 0x01) {
        throw new CancelledException("[64:01]");
      } else if (sw[0] == (byte) 0x64 && sw[1] == (byte) 0x02) {
        throw new PINConfirmationException("[64:02]");
      } else if (sw[0] == (byte) 0x64 && sw[1] == (byte) 0x03) {
        throw new PINFormatException("[64:03]");
      } else if (sw[0] == (byte) 0x6a && sw[1] == (byte) 0x80) {
        log.info("invalid parameter, assume wrong pin size");
        throw new PINFormatException("[6a:80]");
      }
      log.error("Failed to change pin: SW="
              + SMCCHelper.toString(sw));
      throw new SignatureCardException(SMCCHelper.toString(sw));
    } catch (CardException ex) {
      log.error("smart card communication failed: " + ex.getMessage());
      throw new SignatureCardException("smart card communication failed: " + ex.getMessage(), ex);
    }
  }

  @Override
  protected void activatePIN(byte kid, char[] pin)
          throws CancelledException, PINFormatException, PINConfirmationException, TimeoutException, PINOperationAbortedException, SignatureCardException {
    try {
      byte[] sw;
      if (reader.hasFeature(CCID.FEATURE_MODIFY_PIN_DIRECT)) {
        log.debug("activate pin on cardreader");
        sw = reader.modifyPinDirect(getActivatePINModifyStructure(kid));
      } else if (reader.hasFeature(CCID.FEATURE_MODIFY_PIN_START)) {
        log.debug("activate pin on cardreader");
        sw = reader.modifyPin(getActivatePINModifyStructure(kid));
      } else {
        CardChannel channel = getCardChannel();
        ResponseAPDU resp = transmit(channel,
                new CommandAPDU(0x00, 0x24, 0x01, kid, encodePINBlock(pin)), false);

        sw = new byte[2];
        sw[0] = (byte) resp.getSW1();
        sw[1] = (byte) resp.getSW2();
        log.trace("activate pin returned SW=" + Integer.toHexString(resp.getSW()));
      }

      if (sw[0] == (byte) 0x90 && sw[1] == (byte) 0x00) {
        return;
      } else if (sw[0] == (byte) 0x69 && sw[1] == (byte) 0x83) {
        //Authentisierungsmethode gesperrt
        throw new LockedException("[69:83]");
//      } else if (sw[0] == (byte) 0x69 && sw[1] == (byte) 0x84) {
//        //referenzierte Daten sind reversibel gesperrt (invalidated)
//        throw new NotActivatedException("[69:84]");
//      } else if (sw[0] == (byte) 0x69 && sw[1] == (byte) 0x85) {
//        //Benutzungsbedingungen nicht erfüllt
//        throw new NotActivatedException("[69:85]");
      } else if (sw[0] == (byte) 0x64 && sw[1] == (byte) 0x00) {
        throw new TimeoutException("[64:00]");
      } else if (sw[0] == (byte) 0x64 && sw[1] == (byte) 0x01) {
        throw new CancelledException("[64:01]");
      } else if (sw[0] == (byte) 0x64 && sw[1] == (byte) 0x02) {
        throw new PINConfirmationException("[64:02]");
      } else if (sw[0] == (byte) 0x64 && sw[1] == (byte) 0x03) {
        throw new PINFormatException("[64:03]");
      }
      log.error("Failed to activate pin: SW="
              + SMCCHelper.toString(sw));
      throw new SignatureCardException(SMCCHelper.toString(sw));

    } catch (CardException ex) {
      log.error("smart card communication failed: " + ex.getMessage());
      throw new SignatureCardException("smart card communication failed: " + ex.getMessage(), ex);
    }
  }

  /**
   * BCD encodes the pin, pads with 0xFF and prepends the pins length
   * @param pin
   * @return a 8 byte pin block consisting of length byte (0x2X),
   * the BCD encoded pin and a 0xFF padding
   */
  @Override
  protected byte[] encodePINBlock(char[] pin) throws SignatureCardException {
    if (pin == null || pin.length > 12) {
      throw new SignatureCardException("invalid pin: " + pin);
    }
    int numDigits = pin.length;
    int numBytes = (int) Math.ceil(numDigits/2.0);

    byte[] pinBlock = new byte[8];
    pinBlock[0] = (byte) (0x20 | numDigits);

    for (int i = 0; i < numBytes; i++) {
      int p1 = 16*Character.digit(pin[i*2], 16);
      int p2 = (i*2+1 < numDigits) ? Character.digit(pin[i*2+1], 16) : 0xf;
      pinBlock[i+1] = (byte) (p1 + p2);
    }
    Arrays.fill(pinBlock, numBytes + 1, pinBlock.length, (byte) 0xff);
//    log.trace("BCD encoded PIN block: " + SMCCHelper.toString(pinBlock));

    return pinBlock;
  }
  
  private byte[] getPINVerifyStructure(byte kid) {
      byte bTimeOut = reader.getbTimeOut(); 
      byte bTimeOut2 = reader.getbTimeOut2(); // time out after first entry
      byte bmFormatString = (byte) 0x89;      // 1 0001 0 01 
                                              // ^------------ System unit = byte
                                              //   ^^^^------- PIN position in the frame = 1 byte
                                              //        ^----- PIN justification left
                                              //          ^^-- BCD format
      byte bmPINBlockString = (byte) 0x47;    // 0100 0111
                                              // ^^^^--------- PIN length size: 4 bits
                                              //      ^^^^---- Length PIN = 7 bytes
      byte bmPINLengthFormat = (byte) 0x04;   // 000 0 0100
                                              //     ^-------- System bit units is bit
                                              //       ^^^^--- PIN length is at the 4th position bit
      //TODO compare ints, not bytes
      byte wPINMaxExtraDigitL =               // Max=12 digits 
              (reader.getwPINMaxExtraDigitL() < (byte) 0x0c) ?
                reader.getwPINMaxExtraDigitL() : (byte) 0x0c;
      byte wPINMaxExtraDigitH =               // Min=4/6 digits TODO card/ss pin (min: 4/6)
              (reader.getwPINMaxExtraDigitH() > (byte) 0x04) ?
                reader.getwPINMaxExtraDigitH() : (byte) 0x04;
      byte bEntryValidationCondition = 
              reader.getbEntryValidationCondition();  
      byte bNumberMessage = (byte) 0x00;      // No message
      byte wLangIdL = (byte) 0x0C;            // - English?
      byte wLangIdH = (byte) 0x04;            // \
      byte bMsgIndex = (byte) 0x00;           // Default Msg

      byte[] apdu = new byte[] {
        (byte) 0x00, (byte) 0x20, (byte) 0x00, kid, (byte) 0x08,  // CLA INS P1 P2 LC
        (byte) 0x20, (byte) 0xff, (byte) 0xff, (byte) 0xff,               // Data
        (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff                // ...
      };

      int offset = 0;
      byte[] pinVerifyStructure = new byte[offset + 19 + apdu.length];
      pinVerifyStructure[offset++] = bTimeOut;
      pinVerifyStructure[offset++] = bTimeOut2;
      pinVerifyStructure[offset++] = bmFormatString;
      pinVerifyStructure[offset++] = bmPINBlockString;
      pinVerifyStructure[offset++] = bmPINLengthFormat;
      pinVerifyStructure[offset++] = wPINMaxExtraDigitL;
      pinVerifyStructure[offset++] = wPINMaxExtraDigitH;
      pinVerifyStructure[offset++] = bEntryValidationCondition;
      pinVerifyStructure[offset++] = bNumberMessage;
      pinVerifyStructure[offset++] = wLangIdL;
      pinVerifyStructure[offset++] = wLangIdH;
      pinVerifyStructure[offset++] = bMsgIndex;
      
      pinVerifyStructure[offset++] = 0x00;
      pinVerifyStructure[offset++] = 0x00;
      pinVerifyStructure[offset++] = 0x00;
      
      pinVerifyStructure[offset++] = (byte) apdu.length;
      pinVerifyStructure[offset++] = 0x00;
      pinVerifyStructure[offset++] = 0x00;
      pinVerifyStructure[offset++] = 0x00;
      System.arraycopy(apdu, 0, pinVerifyStructure, offset, apdu.length);

      return pinVerifyStructure;
  }

  private byte[] getPINModifyStructure(byte kid) {

      byte bTimeOut = reader.getbTimeOut();   // s.o.
      byte bTimeOut2 = reader.getbTimeOut2(); // s.o.
      byte bmFormatString = (byte) 0x89;      // s.o.
      byte bmPINBlockString = (byte) 0x47;    // s.o.
      byte bmPINLengthFormat = (byte) 0x04;   // s.o.
      byte bInsertionOffsetOld = (byte) 0x00; // insertion position offset in bytes
      byte bInsertionOffsetNew = (byte) 0x08; // (add 1 from bmFormatString b3)
      byte wPINMaxExtraDigitL = 
              (reader.getwPINMaxExtraDigitL() < (byte) 0x0c) ?
                reader.getwPINMaxExtraDigitL() : (byte) 0x0c;
      byte wPINMaxExtraDigitH =               // Min=4/6 digits TODO card/ss pin (min: 4/6)
              (reader.getwPINMaxExtraDigitH() > (byte) 0x04) ?
                reader.getwPINMaxExtraDigitH() : (byte) 0x04;
      byte bConfirmPIN = (byte) 0x03;         // current pin entry + confirmation
      byte bEntryValidationCondition =
              reader.getbEntryValidationCondition();
      byte bNumberMessage = (byte) 0x03;      // 3 messages
      byte wLangIdL = (byte) 0x0C;            
      byte wLangIdH = (byte) 0x04;            
      byte bMsgIndex1 = (byte) 0x00;          // insertion
      byte bMsgIndex2 = (byte) 0x01;          // modification
      byte bMsgIndex3 = (byte) 0x02;          // confirmation

      byte[] apdu = new byte[] {
        (byte) 0x00, (byte) 0x24, (byte) 0x00, kid, (byte) 0x10, 
        (byte) 0x20, (byte) 0xff, (byte) 0xff, (byte) 0xff,      
        (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,      
        (byte) 0x20, (byte) 0xff, (byte) 0xff, (byte) 0xff,      
        (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff       
      };

      int offset = 0;
      byte[] pinModifyStructure = new byte[offset + 24 + apdu.length];
      pinModifyStructure[offset++] = bTimeOut;
      pinModifyStructure[offset++] = bTimeOut2;
      pinModifyStructure[offset++] = bmFormatString;
      pinModifyStructure[offset++] = bmPINBlockString;
      pinModifyStructure[offset++] = bmPINLengthFormat;
      pinModifyStructure[offset++] = bInsertionOffsetOld;
      pinModifyStructure[offset++] = bInsertionOffsetNew;
      pinModifyStructure[offset++] = wPINMaxExtraDigitL;
      pinModifyStructure[offset++] = wPINMaxExtraDigitH;
      pinModifyStructure[offset++] = bConfirmPIN;
      pinModifyStructure[offset++] = bEntryValidationCondition;
      pinModifyStructure[offset++] = bNumberMessage;
      pinModifyStructure[offset++] = wLangIdL;
      pinModifyStructure[offset++] = wLangIdH;
      pinModifyStructure[offset++] = bMsgIndex1;
      pinModifyStructure[offset++] = bMsgIndex2;
      pinModifyStructure[offset++] = bMsgIndex3;

      pinModifyStructure[offset++] = 0x00;
      pinModifyStructure[offset++] = 0x00;
      pinModifyStructure[offset++] = 0x00;

      pinModifyStructure[offset++] = (byte) apdu.length;
      pinModifyStructure[offset++] = 0x00;
      pinModifyStructure[offset++] = 0x00;
      pinModifyStructure[offset++] = 0x00;
      System.arraycopy(apdu, 0, pinModifyStructure, offset, apdu.length);

//      log.debug("PIN MODIFY " + SMCCHelper.toString(pinModifyStructure));
      return pinModifyStructure;
  }
  private byte[] getActivatePINModifyStructure(byte kid) {

      byte bTimeOut = reader.getbTimeOut();   
      byte bTimeOut2 = reader.getbTimeOut2(); 
      byte bmFormatString = (byte) 0x89;      
      byte bmPINBlockString = (byte) 0x47;    
      byte bmPINLengthFormat = (byte) 0x04;   
      byte bInsertionOffsetOld = (byte) 0x00; // ignored
      byte bInsertionOffsetNew = (byte) 0x00; 
      byte wPINMaxExtraDigitL =
              (reader.getwPINMaxExtraDigitL() < (byte) 0x12) ?
                reader.getwPINMaxExtraDigitL() : (byte) 0x12;
      byte wPINMaxExtraDigitH =               // Min=4/6 digits TODO card/ss pin (min: 4/6)
              (reader.getwPINMaxExtraDigitH() > (byte) 0x04) ?
                reader.getwPINMaxExtraDigitH() : (byte) 0x04;
      byte bConfirmPIN = (byte) 0x01;         // confirm, no current pin entry
      byte bEntryValidationCondition =
              reader.getbEntryValidationCondition();
      byte bNumberMessage = (byte) 0x02;      // 2 messages
      byte wLangIdL = (byte) 0x0c;
      byte wLangIdH = (byte) 0x04;
      byte bMsgIndex1 = (byte) 0x01;          // modification prompt
      byte bMsgIndex2 = (byte) 0x02;          // confirmation prompt
      byte bMsgIndex3 = (byte) 0x00;           

      byte[] apdu = new byte[] {
        (byte) 0x00, (byte) 0x24, (byte) 0x01, kid, (byte) 0x08,
        (byte) 0x20, (byte) 0xff, (byte) 0xff, (byte) 0xff,
        (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff
      };

      int offset = 0;
      byte[] pinModifyStructure = new byte[offset + 24 + apdu.length];
      pinModifyStructure[offset++] = bTimeOut;
      pinModifyStructure[offset++] = bTimeOut2;
      pinModifyStructure[offset++] = bmFormatString;
      pinModifyStructure[offset++] = bmPINBlockString;
      pinModifyStructure[offset++] = bmPINLengthFormat;
      pinModifyStructure[offset++] = bInsertionOffsetOld;
      pinModifyStructure[offset++] = bInsertionOffsetNew;
      pinModifyStructure[offset++] = wPINMaxExtraDigitL;
      pinModifyStructure[offset++] = wPINMaxExtraDigitH;
      pinModifyStructure[offset++] = bConfirmPIN;
      pinModifyStructure[offset++] = bEntryValidationCondition;
      pinModifyStructure[offset++] = bNumberMessage;
      pinModifyStructure[offset++] = wLangIdL;
      pinModifyStructure[offset++] = wLangIdH;
      pinModifyStructure[offset++] = bMsgIndex1;
      pinModifyStructure[offset++] = bMsgIndex2;
      pinModifyStructure[offset++] = bMsgIndex3;

      pinModifyStructure[offset++] = 0x00;
      pinModifyStructure[offset++] = 0x00;
      pinModifyStructure[offset++] = 0x00;

      pinModifyStructure[offset++] = (byte) apdu.length;
      pinModifyStructure[offset++] = 0x00;
      pinModifyStructure[offset++] = 0x00;
      pinModifyStructure[offset++] = 0x00;
      System.arraycopy(apdu, 0, pinModifyStructure, offset, apdu.length);

//      log.debug("PIN MODIFY " + SMCCHelper.toString(pinModifyStructure));
      return pinModifyStructure;
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
}
