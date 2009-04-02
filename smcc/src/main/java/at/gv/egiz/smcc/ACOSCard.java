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
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ACOSCard extends AbstractSignatureCard {

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

  protected static final int PINSPEC_INF = 0;
  protected static final int PINSPEC_DEC = 1;
  protected static final int PINSPEC_SIG = 2;

  public ACOSCard() {
    super("at/gv/egiz/smcc/ACOSCard");
    pinSpecs.add(PINSPEC_INF,
            new PINSpec(0, 8, "[0-9]", getResourceBundle().getString("inf.pin.name"), KID_PIN_INF, AID_DEC));
    pinSpecs.add(PINSPEC_DEC, 
            new PINSpec(0, 8, "[0-9]", getResourceBundle().getString("dec.pin.name"), KID_PIN_DEC, AID_DEC));
    pinSpecs.add(PINSPEC_SIG, 
            new PINSpec(0, 8, "[0-9]", getResourceBundle().getString("sig.pin.name"), KID_PIN_SIG, AID_SIG));
  }

  /* (non-Javadoc)
   * @see at.gv.egiz.smcc.SignatureCard#getCertificate(at.gv.egiz.smcc.SignatureCard.KeyboxName)
   */
  @Override
  public byte[] getCertificate(KeyboxName keyboxName)
      throws SignatureCardException, InterruptedException {
  
    try {
      
      if (keyboxName == KeyboxName.SECURE_SIGNATURE_KEYPAIR) {
        
        try {
          getCard().beginExclusive();
          byte[] certificate = readTLVFile(AID_SIG, EF_C_CH_DS, EF_C_CH_DS_MAX_SIZE);
          if (certificate == null) {
            throw new NotActivatedException();
          }
          return certificate;
        } catch (FileNotFoundException e) {
          throw new NotActivatedException();
        } finally {
          getCard().endExclusive();
        }
        
      } else if (keyboxName == KeyboxName.CERITIFIED_KEYPAIR) {
        
        try {
          getCard().beginExclusive();
          byte[] certificate = readTLVFile(AID_DEC, EF_C_CH_EKEY, EF_C_CH_EKEY_MAX_SIZE);
          if (certificate == null) {
            throw new NotActivatedException();
          }
          return certificate;
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

  /* (non-Javadoc)
   * @see at.gv.egiz.smcc.SignatureCard#getInfobox(java.lang.String, at.gv.egiz.smcc.PINProvider, java.lang.String)
   */
  @Override
  public byte[] getInfobox(String infobox, PINProvider provider, String domainId)
      throws SignatureCardException, InterruptedException {
  
    try {
      if ("IdentityLink".equals(infobox)) {
 
        PINSpec spec = pinSpecs.get(PINSPEC_INF);
        //new PINSpec(4, 4, "[0-9]", getResourceBundle().getString("inf.pin.name"));
        
        int retries = -1;
        boolean pinRequired = false;

        do {
          try {
            getCard().beginExclusive();
            if (pinRequired) {
              char[] pin = provider.providePIN(spec, retries);
              return readTLVFile(AID_DEC, EF_INFOBOX, pin, spec.getKID(), EF_INFOBOX_MAX_SIZE);
            } else {
              return readTLVFile(AID_DEC, EF_INFOBOX, EF_INFOBOX_MAX_SIZE);
            }
          } catch (FileNotFoundException e) {
            throw new NotActivatedException();
          } catch (SecurityStatusNotSatisfiedException e) {
            pinRequired = true;
          } catch (VerificationFailedException e) {
            pinRequired = true;
            retries = e.getRetries();
          } finally {
            getCard().endExclusive();
          }
        } while (retries != 0);

        throw new LockedException();

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

        PINSpec spec = pinSpecs.get(PINSPEC_SIG);
        //new PINSpec(6, 10, "[0-9]", getResourceBundle().getString("sig.pin.name"));
        
        int retries = -1;
        char[] pin = null;

        do {
          pin = provider.providePIN(spec, retries);
          try {
            getCard().beginExclusive();
            
            // SELECT DF
            selectFileFID(DF_SIG);
            // VERIFY
            retries = verifyPIN(KID_PIN_SIG, pin);
            if (retries != -1) {
              throw new VerificationFailedException(retries);
            }
            // MSE: SET DST
            mseSetDST(0x81, 0xb6, DST_SIG);
            // PSO: HASH
            psoHash(hash);
            // PSO: COMPUTE DIGITAL SIGNATURE
            return psoComputDigitalSiganture();

          } catch (SecurityStatusNotSatisfiedException e) {
            retries = verifyPIN(KID_PIN_SIG);
          } catch (VerificationFailedException e) {
            retries = e.getRetries();
          } finally {
            getCard().endExclusive();
          }
        } while (retries != 0);

        throw new LockedException();
        
    
      } else if (KeyboxName.CERITIFIED_KEYPAIR.equals(keyboxName)) {
        
        PINSpec spec = pinSpecs.get(PINSPEC_DEC);
        //new PINSpec(4, 4, "[0-9]", getResourceBundle().getString("dec.pin.name"));

        int retries = -1;
        char[] pin = null;
        boolean pinRequired = false;

        do {
          if (pinRequired) {
            pin = provider.providePIN(spec, retries);
          }
          try {
            getCard().beginExclusive();
            
            // SELECT DF
            selectFileFID(DF_DEC);
            // VERIFY
            retries = verifyPIN(KID_PIN_DEC, pin);
            if (retries != -1) {
              throw new VerificationFailedException(retries);
            }
            // MSE: SET DST
            mseSetDST(0x41, 0xa4, DST_DEC);
            // INTERNAL AUTHENTICATE
            return internalAuthenticate(hash);
            
          } catch (FileNotFoundException e) {
            throw new NotActivatedException();
          } catch (SecurityStatusNotSatisfiedException e) {
            pinRequired = true;
            retries = verifyPIN(KID_PIN_DEC);
          } catch (VerificationFailedException e) {
            pinRequired = true;
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
    return transmit(channel, new CommandAPDU(0x00, 0xA4, 0x00,
        0x00, fid, 256));
  }

  private void mseSetDST(int p1, int p2, byte[] dst) throws CardException, SignatureCardException {
    CardChannel channel = getCardChannel();
    ResponseAPDU resp = transmit(channel, new CommandAPDU(0x00, 0x22, p1,
        p2, dst));
    if (resp.getSW() != 0x9000) {
      throw new SignatureCardException("MSE:SET DST failed: SW="
          + Integer.toHexString(resp.getSW()));
    }
  }

  private void psoHash(byte[] hash) throws CardException, SignatureCardException {
    CardChannel channel = getCardChannel();
    ResponseAPDU resp = transmit(channel, new CommandAPDU(0x00, 0x2A, 0x90,
        0x81, hash));
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
  
  private byte[] internalAuthenticate(byte[] hash) throws CardException, SignatureCardException {
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

  /**
   *
   * @param kid
   * @return -1
   */
  @Override
  protected int verifyPIN(byte kid) {
    log.debug("VERIFY PIN without PIN BLOCK not supported by ACOS");
    return -1;
  }

  @Override
  protected int verifyPIN(byte kid, char[] pin)
          throws LockedException, NotActivatedException, CancelledException, TimeoutException, PINFormatException, PINOperationAbortedException, SignatureCardException {
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

      //6A 00 (falshe P1/P2) nicht in contextAID
      //69 85 (nutzungsbedingungen nicht erfüllt) in DF_Sig und nicht sigpin

      if (sw[0] == (byte) 0x90 && sw[1] == (byte) 0x00) {
        return -1;
      } else if (sw[0] == (byte) 0x63 && sw[1] == (byte) 0xc0) {
        throw new LockedException("[63:c0]");
      } else if (sw[0] == (byte) 0x63 && (sw[1] & 0xf0) >> 4 == 0xc) {
        return sw[1] & 0x0f;
      } else if (sw[0] == (byte) 0x69 && sw[1] == (byte) 0x83) {
        //Authentisierungsmethode gesperrt
        throw new NotActivatedException("[69:83]");
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

  /**
   * SCARD_E_NOT_TRANSACTED inf/dec PIN not active (pcsc crash)
   * @param kid
   * @param oldPin
   * @param newPin
   * @return
   * @throws at.gv.egiz.smcc.LockedException
   * @throws at.gv.egiz.smcc.NotActivatedException
   * @throws at.gv.egiz.smcc.SignatureCardException
   */
  @Override
  protected int changePIN(byte kid, char[] oldPin, char[] newPin)
          throws LockedException, NotActivatedException, CancelledException, PINFormatException, PINConfirmationException, TimeoutException, PINOperationAbortedException, SignatureCardException {
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
        // sig-pin only (card not transacted for inf/dec pin)
        throw new NotActivatedException("[69:83]");
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

  /**
   * throws SignatureCardException (PIN activation not supported by ACOS)
   * @throws at.gv.egiz.smcc.SignatureCardException
   */
  @Override
  public void activatePIN(byte kid, char[] pin)
          throws SignatureCardException {
    log.error("ACTIVATE PIN not supported by ACOS");
    throw new SignatureCardException("PIN activation not supported by this card");
  }

  /**
   * ASCII encoded pin, padded with 0x00
   * @param pin
   * @return a 8 byte pin block
   */
  @Override
  protected byte[] encodePINBlock(char[] pin) {
//    byte[] asciiPIN = new String(pin).getBytes(Charset.forName("ASCII"));
    CharBuffer chars = CharBuffer.wrap(pin);
    ByteBuffer bytes = Charset.forName("ASCII").encode(chars);
    byte[] asciiPIN = bytes.array();
    byte[] encodedPIN = new byte[8];
    System.arraycopy(asciiPIN, 0, encodedPIN, 0, Math.min(asciiPIN.length,
        encodedPIN.length));
//    System.out.println("ASCII encoded PIN block: " + SMCCHelper.toString(encodedPIN));
    return encodedPIN;
  }
  
  private byte[] getPINVerifyStructure(byte kid) {
      
      byte bTimeOut = reader.getbTimeOut();   
      byte bTimeOut2 = reader.getbTimeOut2(); 
      byte bmFormatString = (byte) 0x82;      // 1 0000 0 10
                                              // ^------------ System unit = byte
                                              //   ^^^^------- PIN position in the frame = 1 byte
                                              //        ^----- PIN justification left
                                              //          ^^-- ASCII format
      byte bmPINBlockString = (byte) 0x08;    // 0000 1000
                                              // ^^^^--------- PIN length size: 0 bits
                                              //      ^^^^---- Length PIN = 8 bytes
      byte bmPINLengthFormat = (byte) 0x00;   // 000 0 0000
                                              //     ^-------- System bit units is bit
                                              //       ^^^^--- no PIN length
      byte wPINMaxExtraDigitL = //TODO compare ints, not bytes
              (reader.getwPINMaxExtraDigitL() < (byte) 0x08) ?
                reader.getwPINMaxExtraDigitL() : (byte) 0x08;
      byte wPINMaxExtraDigitH =               
              (reader.getwPINMaxExtraDigitH() > (byte) 0x00) ?
                reader.getwPINMaxExtraDigitH() : (byte) 0x00;
      byte bEntryValidationCondition = 
              reader.getbEntryValidationCondition();
      byte bNumberMessage = (byte) 0x00;      // No message
      byte wLangIdL = (byte) 0x0C;            
      byte wLangIdH = (byte) 0x04;            
      byte bMsgIndex = (byte) 0x00;           

      byte[] apdu = new byte[] {
        (byte) 0x00, (byte) 0x20, (byte) 0x00, kid, (byte) 0x08, 
        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,      
        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00       
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
  
  public byte[] getPINModifyStructure(byte kid) {

      byte bTimeOut = reader.getbTimeOut();
      byte bTimeOut2 = reader.getbTimeOut2();
      byte bmFormatString = (byte) 0x82;      // 1 0000 0 10
                                              // ^------------ System unit = byte
                                              //   ^^^^------- PIN position in the frame = 1 byte
                                              //        ^----- PIN justification left
                                              //          ^^-- ASCII format
      byte bmPINBlockString = (byte) 0x08;    // 0000 1000
                                              // ^^^^--------- PIN length size: 0 bits
                                              //      ^^^^---- Length PIN = 8 bytes
      byte bmPINLengthFormat = (byte) 0x00;   // 000 0 0000
                                              //     ^-------- System bit units is bit
                                              //       ^^^^--- no PIN length 
      byte bInsertionOffsetOld = (byte) 0x00; // insertion position offset in bytes
      byte bInsertionOffsetNew = (byte) 0x08; 
      byte wPINMaxExtraDigitL =
              (reader.getwPINMaxExtraDigitL() < (byte) 0x08) ?
                reader.getwPINMaxExtraDigitL() : (byte) 0x08;
      byte wPINMaxExtraDigitH =
              (reader.getwPINMaxExtraDigitH() > (byte) 0x00) ?
                reader.getwPINMaxExtraDigitH() : (byte) 0x00;
      byte bConfirmPIN = (byte) 0x03;
      byte bEntryValidationCondition =
              reader.getbEntryValidationCondition();
      byte bNumberMessage = (byte) 0x03;      
      byte wLangIdL = (byte) 0x0C;            
      byte wLangIdH = (byte) 0x04;            
      byte bMsgIndex1 = (byte) 0x00;           
      byte bMsgIndex2 = (byte) 0x01;           
      byte bMsgIndex3 = (byte) 0x02;           

      byte[] apdu = new byte[] {
        (byte) 0x00, (byte) 0x24, (byte) 0x00, kid, (byte) 0x10,  
        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,       
        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,       
        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,       
        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00        
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

      return pinModifyStructure;
  }

  @Override
  public String toString() {
    return "a-sign premium";
  }
}
