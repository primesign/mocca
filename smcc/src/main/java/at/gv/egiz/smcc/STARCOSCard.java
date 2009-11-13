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

package at.gv.egiz.smcc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import at.gv.egiz.smcc.util.ISO7816Utils;
import at.gv.egiz.smcc.util.SMCCHelper;

public class STARCOSCard extends AbstractSignatureCard implements PINMgmtSignatureCard {

  /**
   * Logging facility.
   */
  private static Log log = LogFactory.getLog(STARCOSCard.class);

  public static final byte[] MF = new byte[] { (byte) 0x3F, (byte) 0x00 };
  
  public static final byte[] EF_VERSION = new byte[] { (byte) 0x00, (byte) 0x32 };

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

  public static final byte KID_PIN_SS = (byte) 0x81;

  // Gewöhnliche Signatur (GS)

  public static final byte[] AID_DF_GS = new byte[] { (byte) 0xd0, (byte) 0x40,
      (byte) 0x00, (byte) 0x00, (byte) 0x17, (byte) 0x00, (byte) 0x13,
      (byte) 0x01 };

  public static final byte[] EF_C_X509_CH_AUT = new byte[] { (byte) 0x2f,
      (byte) 0x01 };

  public static final byte[] EF_C_X509_CA_CS = new byte[] { (byte) 0x2f,
      (byte) 0x02 };

  public static final byte KID_PIN_CARD = (byte) 0x01;

  private static final PINSpec CARD_PIN_SPEC =
    new PINSpec(4, 12, "[0-9]", 
        "at/gv/egiz/smcc/STARCOSCard", "card.pin", KID_PIN_CARD, null);
  
  private static final PINSpec SS_PIN_SPEC =
    new PINSpec(6, 12, "[0-9]", 
        "at/gv/egiz/smcc/STARCOSCard", "sig.pin", KID_PIN_SS, AID_DF_SS);
  
  static {
    if (SignatureCardFactory.ENFORCE_RECOMMENDED_PIN_LENGTH) {
      CARD_PIN_SPEC.setRecLength(4);
      SS_PIN_SPEC.setRecLength(6);
    }
  }
  
  protected double version = 1.1;

  /**
   * Creates a new instance.
   */
  public STARCOSCard() {
    super("at/gv/egiz/smcc/STARCOSCard");
    pinSpecs.add(CARD_PIN_SPEC);
    pinSpecs.add(SS_PIN_SPEC);
  }
 
  /* (non-Javadoc)
   * @see at.gv.egiz.smcc.AbstractSignatureCard#init(javax.smartcardio.Card, javax.smartcardio.CardTerminal)
   */
  @Override
  public void init(Card card, CardTerminal cardTerminal) {
    super.init(card, cardTerminal);
    
    // determine application version
    CardChannel channel = getCardChannel();
    try {
      // SELECT MF
      execSELECT_MF(channel);
      // SELECT EF_VERSION
      execSELECT_FID(channel, EF_VERSION);
      // READ BINARY
      byte[] ver = ISO7816Utils.readRecord(channel, 1);
      if (ver[0] == (byte) 0xa5 && ver[2] == (byte) 0x53) {
        version = (0x0F & ver[4]) + (0xF0 & ver[5])/160.0 + (0x0F & ver[5])/100.0;
        String generation = (version < 1.2) ? "<= G2" : "G3";
        log.info("e-card version=" + version + " (" + generation + ")");
      }
    } catch (CardException e) {
      log.warn(e);
    } catch (SignatureCardException e) {
      log.warn(e);
    }
    
  }

  @Override
  @Exclusive
  public byte[] getCertificate(KeyboxName keyboxName)
      throws SignatureCardException, InterruptedException {

    byte[] aid;
    byte[] fid;
    if (keyboxName == KeyboxName.SECURE_SIGNATURE_KEYPAIR) {
      aid = AID_DF_SS;
      fid = EF_C_X509_CH_DS;
    } else if (keyboxName == KeyboxName.CERITIFIED_KEYPAIR) {
      aid = AID_DF_GS;
      fid = EF_C_X509_CH_AUT;
    } else {
      throw new IllegalArgumentException("Keybox " + keyboxName
          + " not supported.");
    }

    try {
      CardChannel channel = getCardChannel();
      // SELECT application
      execSELECT_AID(channel, aid);
      // SELECT file
      execSELECT_FID(channel, fid);
      // READ BINARY
      byte[] certificate = ISO7816Utils.readTransparentFileTLV(channel, -1, (byte) 0x30);
      if (certificate == null) {
        throw new NotActivatedException();
      }
      return certificate;
    } catch (FileNotFoundException e) {
      throw new NotActivatedException();
    } catch (CardException e) {
      log.info("Failed to get certificate.", e);
      throw new SignatureCardException(e);
    }

  }
  
  @Override
  @Exclusive
  public byte[] getInfobox(String infobox, PINProvider provider, String domainId)
      throws SignatureCardException, InterruptedException {
  
    try {
      if ("IdentityLink".equals(infobox)) {

        PINSpec spec = CARD_PIN_SPEC;
        
        CardChannel channel = getCardChannel();
        // SELECT application
        execSELECT_AID(channel, AID_INFOBOX);
        // SELECT file
        execSELECT_FID(channel, EF_INFOBOX);

        while (true) {
          try {
            return ISO7816Utils.readTransparentFileTLV(channel, -1, (byte) 0x30);
          } catch (SecurityStatusNotSatisfiedException e) {
            verifyPINLoop(channel, spec, provider);
          }
        }
        
      } else if ("Status".equals(infobox)) {
        
        CardChannel channel = getCardChannel();
        // SELECT application
        execSELECT_AID(channel, AID_SV_PERSONENDATEN);
        // SELECT file
        execSELECT_FID(channel, FID_STATUS);
        // READ RECORDS
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try {
          for (int record = 1; record <= 5; record++) {
            byte[] rb = ISO7816Utils.readRecord(channel, record);
            bytes.write(rb);
          }
        } catch (IOException e) {
          throw new SignatureCardException("Failed to read infobox '" + infobox
              + "'.", e);
        }
        return bytes.toByteArray();
        
      } else {

        byte[] fid;
        
        if ("EHIC".equals(infobox)) {
          fid = FID_EHIC;
        } else if ("Grunddaten".equals(infobox)) {
          fid = FID_GRUNDDATEN;
        } else if ("SV-Personenbindung".equals(infobox)) {
          fid = FID_SV_PERSONENBINDUNG;
        } else {
          throw new IllegalArgumentException("Infobox '" + infobox
              + "' not supported.");
        }
       
        CardChannel channel = getCardChannel();
        // SELECT application
        execSELECT_AID(channel, AID_SV_PERSONENDATEN);
        // SELECT file
        execSELECT_FID(channel, fid);
        // READ BINARY
        return ISO7816Utils.readTransparentFileTLV(channel, -1, (byte) 0x30);
        
      }
        
    } catch (CardException e) {
      log.warn(e);
      throw new SignatureCardException("Failed to access card.", e);
    }
  }

  @Override
  @Exclusive
  public byte[] createSignature(InputStream input, KeyboxName keyboxName,
      PINProvider provider, String alg) throws SignatureCardException, InterruptedException, IOException {
  
    ByteArrayOutputStream dst = new ByteArrayOutputStream();
    byte[] ht = null;
    
    MessageDigest md = null;
    try {
      if (alg == null || "http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha1".equals(alg)) {
        // local key ID '02' version '00'
        dst.write(new byte[] {(byte) 0x84, (byte) 0x03, (byte) 0x80, (byte) 0x02, (byte) 0x00});
        if (version < 1.2) {
          // algorithm ID ECDSA with SHA-1
          dst.write(new byte[] {(byte) 0x89, (byte) 0x03, (byte) 0x13, (byte) 0x35, (byte) 0x10});
        } else {
          // portable algorithm reference
          dst.write(new byte[] {(byte) 0x80, (byte) 0x01, (byte) 0x04});
          // hash template
          ht = new byte[] {(byte) 0x80, (byte) 0x01, (byte) 0x10};
        }
        md = MessageDigest.getInstance("SHA-1");
      } else if (version >= 1.2 && "http://www.w3.org/2000/09/xmldsig#rsa-sha1".equals(alg)) {
        // local key ID '03' version '00'
        dst.write(new byte[] {(byte) 0x84, (byte) 0x03, (byte) 0x80, (byte) 0x03, (byte) 0x00});
        // portable algorithm reference
        dst.write(new byte[] {(byte) 0x80, (byte) 0x01, (byte) 0x02});
        // hash template
        ht = new byte[] {(byte) 0x80, (byte) 0x01, (byte) 0x10};
        md = MessageDigest.getInstance("SHA-1");
      } else if (version >= 1.2 && "http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha256".equals(alg)) {
        // local key ID '02' version '00'
        dst.write(new byte[] {(byte) 0x84, (byte) 0x03, (byte) 0x80, (byte) 0x02, (byte) 0x00});
        // portable algorithm reference
        dst.write(new byte[] {(byte) 0x80, (byte) 0x01, (byte) 0x04});
        // hash template
        ht = new byte[] {(byte) 0x80, (byte) 0x01, (byte) 0x40};
        md = MessageDigest.getInstance("SHA256");
      } else if (version >= 1.2 && "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256".equals(alg)) {
        // local key ID '03' version '00'
        dst.write(new byte[] {(byte) 0x84, (byte) 0x03, (byte) 0x80, (byte) 0x03, (byte) 0x00});
        // portable algorithm reference
        dst.write(new byte[] {(byte) 0x80, (byte) 0x01, (byte) 0x02});
        // hash template
        ht = new byte[] {(byte) 0x80, (byte) 0x01, (byte) 0x40};
        md = MessageDigest.getInstance("SHA256");
      } else {
        throw new SignatureCardException("e-card version " + version + " does not support signature algorithm " + alg + ".");
      }
    } catch (NoSuchAlgorithmException e) {
      log.error("Failed to get MessageDigest.", e);
      throw new SignatureCardException(e);
    }
    
    // calculate message digest
    byte[] digest = new byte[md.getDigestLength()];
    for (int l; (l = input.read(digest)) != -1;) {
      md.update(digest, 0, l);
    }
    digest = md.digest();

    try {
      
      CardChannel channel = getCardChannel();
      
      if (KeyboxName.SECURE_SIGNATURE_KEYPAIR.equals(keyboxName)) {
        
        PINSpec spec = SS_PIN_SPEC;
        
        // SELECT MF
        execSELECT_MF(channel);
        // SELECT application
        execSELECT_AID(channel, AID_DF_SS);
        // VERIFY
        verifyPINLoop(channel, spec, provider);
        // MANAGE SECURITY ENVIRONMENT : SET DST
        execMSE(channel, 0x41, 0xb6, dst.toByteArray());
        if (version < 1.2) {
          // PERFORM SECURITY OPERATION : HASH
          execPSO_HASH(channel, digest);
          // PERFORM SECURITY OPERATION : COMPUTE DIGITAL SIGNATURE
          return execPSO_COMPUTE_DIGITAL_SIGNATURE(channel, null);
        } else {
          if (ht != null) {
            // PERFORM SECURITY OPERATION : SET HT
            execMSE(channel, 0x41, 0xaa, ht);
          }
          // PERFORM SECURITY OPERATION : COMPUTE DIGITAL SIGNATURE
          return execPSO_COMPUTE_DIGITAL_SIGNATURE(channel, digest);
        }
        
        
      } else if (KeyboxName.CERITIFIED_KEYPAIR.equals(keyboxName)) {

        PINSpec spec = CARD_PIN_SPEC;
        
        // SELECT application
        execSELECT_AID(channel, AID_DF_GS);
        // MANAGE SECURITY ENVIRONMENT : SET DST
        execMSE(channel, 0x41, 0xb6, dst.toByteArray());
        if (version >= 1.2 && ht != null) {
          // PERFORM SECURITY OPERATION : SET HT
          execMSE(channel, 0x41, 0xaa, ht);
        }
        // PERFORM SECURITY OPERATION : HASH
        execPSO_HASH(channel, digest);
        while (true) {
          try {
            // PERFORM SECURITY OPERATION : COMPUTE DIGITAL SIGNATURE
            return execPSO_COMPUTE_DIGITAL_SIGNATURE(channel, null);
          } catch (SecurityStatusNotSatisfiedException e) {
            verifyPINLoop(channel, spec, provider);
          }
        }
        
      } else {
        throw new IllegalArgumentException("KeyboxName '" + keyboxName
            + "' not supported.");
      }

    } catch (CardException e) {
      log.warn(e);
      throw new SignatureCardException("Failed to access card.", e);
    }
  
  }

  /* (non-Javadoc)
   * @see at.gv.egiz.smcc.AbstractSignatureCard#verifyPIN(at.gv.egiz.smcc.PINSpec, at.gv.egiz.smcc.PINProvider)
   */
  @Override
  @Exclusive
  public void verifyPIN(PINSpec pinSpec, PINProvider pinProvider)
      throws LockedException, NotActivatedException, CancelledException,
      TimeoutException, SignatureCardException, InterruptedException {
    
    CardChannel channel = getCardChannel();
    
    try {
      if (pinSpec.getContextAID() != null) {
        // SELECT application
        execSELECT_AID(channel, pinSpec.getContextAID());
      }
      log.debug("*** verifyPIN loop");
      verifyPINLoop(channel, pinSpec, pinProvider);
//      log.debug("*** verifyPIN 0");
//      int retries = verifyPIN(channel, pinSpec, null, 0);
//      log.debug("*** verifyPIN " + retries + " tries");
//      verifyPIN(channel, pinSpec, pinProvider, retries);
    } catch (CardException e) {
      log.info("Failed to verify PIN.", e);
      throw new SignatureCardException("Failed to verify PIN.", e);
    }
    
  }
  
  /* (non-Javadoc)
   * @see at.gv.egiz.smcc.AbstractSignatureCard#changePIN(at.gv.egiz.smcc.PINSpec, at.gv.egiz.smcc.ChangePINProvider)
   */
  @Override
  @Exclusive
  public void changePIN(PINSpec pinSpec, ChangePINProvider pinProvider)
      throws LockedException, NotActivatedException, CancelledException,
      TimeoutException, SignatureCardException, InterruptedException {
    
    CardChannel channel = getCardChannel();
    
    try {
      if (pinSpec.getContextAID() != null) {
        // SELECT application
        execSELECT_AID(channel, pinSpec.getContextAID());
      }
      changePINLoop(channel, pinSpec, pinProvider);
//      int retries = verifyPIN(channel, pinSpec, null, 0);
//      changePIN(channel, pinSpec, pinProvider, retries);
    } catch (CardException e) {
      log.info("Failed to change PIN.", e);
      throw new SignatureCardException("Failed to change PIN.", e);
    }
    
  }

  /* (non-Javadoc)
   * @see at.gv.egiz.smcc.AbstractSignatureCard#activatePIN(at.gv.egiz.smcc.PINSpec, at.gv.egiz.smcc.PINProvider)
   */
  @Override
  @Exclusive
  public void activatePIN(PINSpec pinSpec, PINProvider pinProvider)
      throws CancelledException, SignatureCardException, CancelledException,
      TimeoutException, InterruptedException {

    CardChannel channel = getCardChannel();

    try {
      if (pinSpec.getContextAID() != null) {
        // SELECT application
        execSELECT_AID(channel, pinSpec.getContextAID());
      }
      activatePIN(channel, pinSpec, pinProvider);
    } catch (CardException e) {
      log.info("Failed to activate PIN.", e);
      throw new SignatureCardException("Failed to activate PIN.", e);
    }
    
  }

  /* (non-Javadoc)
   * @see at.gv.egiz.smcc.PINMgmtSignatureCard#unblockPIN(at.gv.egiz.smcc.PINSpec, at.gv.egiz.smcc.PINProvider)
   */
  @Override
  public void unblockPIN(PINSpec pinSpec, PINProvider pukProvider)
      throws CancelledException, SignatureCardException, InterruptedException {
    throw new SignatureCardException("Unblock PIN is not supported.");
  }

  @Override
  public void reset() throws SignatureCardException {
    try {
      super.reset();
      log.debug("select MF (e-card workaround)");
      CardChannel channel = getCardChannel();
      ResponseAPDU resp = channel.transmit(new CommandAPDU(0x00, 0xA4, 0x00, 0x0C));
      if (resp.getSW() != 0x9000) {
        throw new SignatureCardException("Failed to select MF after RESET: SW=" + Integer.toHexString(resp.getSW()) + ".");
      }
    } catch (CardException ex) {
      log.error("Failed to select MF after RESET: " + ex.getMessage(), ex);
      throw new SignatureCardException("Failed to select MF after RESET");
    }
  }

  /* (non-Javadoc)
   * @see at.gv.egiz.smcc.PINMgmtSignatureCard#getPINSpecs()
   */
  @Override
  public List<PINSpec> getPINSpecs() {
    return Arrays.asList(new PINSpec[] {CARD_PIN_SPEC, SS_PIN_SPEC});
  }

  /* (non-Javadoc)
   * @see at.gv.egiz.smcc.PINMgmtSignatureCard#getPINStatus(at.gv.egiz.smcc.PINSpec)
   */
  @Override
  public PIN_STATE getPINState(PINSpec pinSpec) throws SignatureCardException {
    
    CardChannel channel = getCardChannel();
    
    try {
      if (pinSpec.getContextAID() != null) {
        // SELECT AID
        execSELECT_AID(channel, pinSpec.getContextAID());
      }
      verifyPIN(channel, pinSpec, null, 0);
      return PIN_STATE.ACTIV;
    } catch (InterruptedException e) {
      return PIN_STATE.UNKNOWN;
    } catch (LockedException e) {
      return PIN_STATE.BLOCKED;
    } catch (NotActivatedException e) {
      return PIN_STATE.NOT_ACTIV;
    } catch (CardException e) {
      log.error("Failed to get PIN status.", e);
      throw new SignatureCardException("Failed to get PIN status.", e);
    }
    
  }

  public String toString() {
    return "e-card";
  }
  
  ////////////////////////////////////////////////////////////////////////
  // PROTECTED METHODS (assume exclusive card access)
  ////////////////////////////////////////////////////////////////////////
  
  protected void verifyPINLoop(CardChannel channel, PINSpec spec, PINProvider provider)
      throws LockedException, NotActivatedException, SignatureCardException,
      InterruptedException, CardException {
    
    int retries = verifyPIN(channel, spec, null, -1);
    do {
      retries = verifyPIN(channel, spec, provider, retries);
    } while (retries > 0);
  }

  protected void changePINLoop(CardChannel channel, PINSpec spec, ChangePINProvider provider)
      throws LockedException, NotActivatedException, SignatureCardException,
      InterruptedException, CardException {

    int retries = verifyPIN(channel, spec, null, -1);
    do {
      retries = changePIN(channel, spec, provider, retries);
    } while (retries > 0);
  }
  
  protected int verifyPIN(CardChannel channel, PINSpec pinSpec,
      PINProvider provider, int retries) throws SignatureCardException,
      LockedException, NotActivatedException, InterruptedException,
      CardException {
    
    VerifyAPDUSpec apduSpec = new VerifyAPDUSpec(
        new byte[] {
            (byte) 0x00, (byte) 0x20, (byte) 0x00, pinSpec.getKID(), (byte) 0x08,
            (byte) 0x20, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
            (byte) 0xff, (byte) 0xff, (byte) 0xff }, 
        1, VerifyAPDUSpec.PIN_FORMAT_BCD, 7, 4, 4);
    
    ResponseAPDU resp;
    if (provider != null) {
      resp = reader.verify(channel, apduSpec, pinSpec, provider, retries);
    } else {
      resp = channel.transmit(new CommandAPDU(0x00, 0x20, 0x00, pinSpec.getKID()));
    }
    
    if (resp.getSW() == 0x9000) {
      return -1;
    }
    if (resp.getSW() >> 4 == 0x63c) {
      return 0x0f & resp.getSW();
    }
    
    switch (resp.getSW()) {
    case 0x6983:
      // authentication method blocked
      throw new LockedException();
    case 0x6984:
      // reference data not usable
      throw new NotActivatedException();
    case 0x6985:
      // conditions of use not satisfied
      throw new NotActivatedException();
  
    default:
      String msg = "VERIFY failed. SW=" + Integer.toHexString(resp.getSW()); 
      log.info(msg);
      throw new SignatureCardException(msg);
    }
    
  }
  
  protected int changePIN(CardChannel channel, PINSpec pinSpec,
      ChangePINProvider pinProvider, int retries) throws CancelledException,
      InterruptedException, CardException, SignatureCardException {
    
    ChangeReferenceDataAPDUSpec apduSpec = new ChangeReferenceDataAPDUSpec(
        new byte[] { 
            (byte) 0x00, (byte) 0x24, (byte) 0x00, pinSpec.getKID(), (byte) 0x10, 
            (byte) 0x20, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, 
            (byte) 0xff, (byte) 0xff, (byte) 0xff, 
            (byte) 0x20, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, 
            (byte) 0xff, (byte) 0xff, (byte) 0xff }, 
        1, VerifyAPDUSpec.PIN_FORMAT_BCD, 7, 4, 4, 8);
    
    ResponseAPDU resp = reader.modify(channel, apduSpec, pinSpec, pinProvider, retries);
    
    if (resp.getSW() == 0x9000) {
      return -1;
    }
    if (resp.getSW() >> 4 == 0x63c) {
      return 0x0f & resp.getSW();
    }
    
    switch (resp.getSW()) {
    case 0x6983:
      // authentication method blocked
      throw new LockedException();
  
    default:
      String msg = "CHANGE REFERENCE DATA failed. SW=" + Integer.toHexString(resp.getSW()); 
      log.info(msg);
      throw new SignatureCardException(msg);
    }
   
    
  }

  protected int activatePIN(CardChannel channel, PINSpec pinSpec,
      PINProvider provider) throws SignatureCardException,
      InterruptedException, CardException {
    
    NewReferenceDataAPDUSpec apduSpec = new NewReferenceDataAPDUSpec(
        new byte[] {
            (byte) 0x00, (byte) 0x24, (byte) 0x01, pinSpec.getKID(), (byte) 0x08,
            (byte) 0x20, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
            (byte) 0xff, (byte) 0xff, (byte) 0xff }, 
        1, VerifyAPDUSpec.PIN_FORMAT_BCD, 7, 4, 4);
    
    ResponseAPDU resp = reader.activate(channel, apduSpec, pinSpec, provider);
    
    switch (resp.getSW()) {
    
    case 0x9000:
      return -1;

    case 0x6983:
      // authentication method blocked
      throw new LockedException();

    default:
      String msg = "CHANGE REFERENCE DATA failed. SW=" + Integer.toHexString(resp.getSW()); 
      log.info(msg);
      throw new SignatureCardException(msg);
    }
    
  }
  
  protected void execSELECT_MF(CardChannel channel) throws CardException, SignatureCardException {
    ResponseAPDU resp = channel.transmit(
        new CommandAPDU(0x00, 0xA4, 0x00, 0x0C));
    if (resp.getSW() != 0x9000) {
      throw new SignatureCardException("Failed to select MF: SW="
          + Integer.toHexString(resp.getSW()) + ".");
    }
  }

  protected byte[] execSELECT_AID(CardChannel channel, byte[] aid)
      throws SignatureCardException, CardException {
    
    ResponseAPDU resp = channel.transmit(
        new CommandAPDU(0x00, 0xA4, 0x04, 0x00, aid, 256));
    
    if (resp.getSW() == 0x6A82) {
      String msg = "File or application not found AID="
          + SMCCHelper.toString(aid) + " SW="
          + Integer.toHexString(resp.getSW()) + ".";
      log.info(msg);
      throw new FileNotFoundException(msg);
    } else if (resp.getSW() != 0x9000) {
      String msg = "Failed to select application AID="
          + SMCCHelper.toString(aid) + " SW="
          + Integer.toHexString(resp.getSW()) + ".";
      log.info(msg);
      throw new SignatureCardException(msg);
    } else {
      return resp.getBytes();
    }
    
  }
  
  protected byte[] execSELECT_FID(CardChannel channel, byte[] fid)
      throws SignatureCardException, CardException {
    
    ResponseAPDU resp = channel.transmit(
        new CommandAPDU(0x00, 0xA4, 0x02, 0x04, fid, 256));
    
    if (resp.getSW() == 0x6A82) {
      String msg = "File or application not found FID="
          + SMCCHelper.toString(fid) + " SW="
          + Integer.toHexString(resp.getSW()) + ".";
      log.info(msg);
      throw new FileNotFoundException(msg);
    } else if (resp.getSW() != 0x9000) {
      String msg = "Failed to select application FID="
          + SMCCHelper.toString(fid) + " SW="
          + Integer.toHexString(resp.getSW()) + ".";
      log.error(msg);
      throw new SignatureCardException(msg);
    } else {
      return resp.getBytes();
    }
    
  }
  
  protected void execMSE(CardChannel channel, int p1, int p2, byte[] data)
      throws CardException, SignatureCardException {
    ResponseAPDU resp = channel.transmit(
        new CommandAPDU(0x00, 0x22, p1, p2, data));
    if (resp.getSW() != 0x9000) {
      throw new SignatureCardException("MSE:SET failed: SW="
          + Integer.toHexString(resp.getSW()));
    }
  }
  
  protected void execPSO_HASH(CardChannel channel, byte[] hash) throws CardException, SignatureCardException {
    byte[] data = new byte[hash.length + 2];
    data[0] = (byte) 0x90; // tag
    data[1] = (byte) (hash.length); // length
    System.arraycopy(hash, 0, data, 2, hash.length);

    ResponseAPDU resp = channel.transmit(
        new CommandAPDU(0x00, 0x2A, 0x90, 0xA0, data));
    if (resp.getSW() != 0x9000) {
      throw new SignatureCardException("PSO:HASH failed: SW="
          + Integer.toHexString(resp.getSW()));
    }
  }

  protected void execPSO_HASH(CardChannel channel, InputStream input)
      throws SignatureCardException, CardException {
    ResponseAPDU resp;
    int blockSize = 64;
    byte[] b = new byte[blockSize];
    try {
      ByteArrayOutputStream data = new ByteArrayOutputStream();
      // initialize
      data.write((byte) 0x90);
      data.write((byte) 0x00);
      resp = channel.transmit(
          new CommandAPDU(0x10, 0x2A, 0x90, 0xA0, data.toByteArray()));
      data.reset();
      for (int l; (l = input.read(b)) != -1;) {
        data.write((byte) 0x80);
        data.write(l);
        data.write(b, 0, l);
        resp = channel.transmit(
            new CommandAPDU((l == blockSize) ? 0x10 : 0x00, 0x2A, 0x90, 0xA0, data.toByteArray()));
        if (resp.getSW() != 0x9000) {
          throw new SignatureCardException("PSO:HASH failed: SW="
              + Integer.toHexString(resp.getSW()));
        }
        data.reset();
      }
    } catch (IOException e) {
      throw new SignatureCardException(e);
    }
    
  }

  protected byte[] execPSO_COMPUTE_DIGITAL_SIGNATURE(CardChannel channel, byte[] hash)
      throws CardException, SignatureCardException {
    ResponseAPDU resp;
    if (hash != null) {
      resp = channel.transmit(
          new CommandAPDU(0x00, 0x2A, 0x9E, 0x9A, hash, 256));
    } else {
      resp = channel.transmit(
          new CommandAPDU(0x00, 0x2A, 0x9E, 0x9A, 256));
    }
    if (resp.getSW() == 0x6982) {
      throw new SecurityStatusNotSatisfiedException();
    } else if (resp.getSW() == 0x6983) {
      throw new LockedException();
    } else if (resp.getSW() != 0x9000) {
      throw new SignatureCardException(
          "PSO: COMPUTE DIGITAL SIGNATRE failed: SW="
              + Integer.toHexString(resp.getSW()));
    } else {
      return resp.getData();
    }
  }
}
