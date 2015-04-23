/*
 * Copyright 2011 by Graz University of Technology, Austria
 * MOCCA has been developed by the E-Government Innovation Center EGIZ, a joint
 * initiative of the Federal Chancellery Austria and Graz University of Technology.
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * http://www.osor.eu/eupl/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 *
 * This product combines work with different licenses. See the "NOTICE" text
 * file for details on the various modules and licenses.
 * The "NOTICE" text file is part of the distribution. Any derivative works
 * that you distribute must include a readable copy of the "NOTICE" text file.
 */

package at.gv.egiz.smcc;

import iaik.me.security.CryptoException;
import iaik.me.security.MessageDigest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.gv.egiz.smcc.pin.gui.ModifyPINGUI;
import at.gv.egiz.smcc.pin.gui.PINGUI;
import at.gv.egiz.smcc.util.ISO7816Utils;
import at.gv.egiz.smcc.util.SMCCHelper;
import at.gv.egiz.smcc.util.TransparentFileInputStream;

public class STARCOSCard extends AbstractSignatureCard implements PINMgmtSignatureCard {

  /**
   * Logging facility.
   */
  private final Logger log = LoggerFactory.getLogger(STARCOSCard.class);

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

  public static final byte[] EF_INFOBOX_LEGACY = new byte[] { (byte) 0xef, (byte) 0x01 };
  
  public static final byte[] EF_INFOBOX = { (byte) 0xc0, (byte) 0x02 };

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
  public static final byte KID_PUK_CARD = (byte) 0x02;

  protected double version = 1.1;
  protected int generation = 2;

  protected String friendlyName = "G1";

  protected PinInfo cardPinInfo;
  protected PinInfo cardPukInfo = null;
  protected PinInfo ssPinInfo;

  /* (non-Javadoc)
   * @see at.gv.egiz.smcc.AbstractSignatureCard#init(javax.smartcardio.Card, javax.smartcardio.CardTerminal)
   */
  @Override
  public void init(Card card, CardTerminal cardTerminal) {
    super.init(card, cardTerminal);

    log.info("STARCOS card found");

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
        friendlyName = (version < 1.2) ? "<= G2" : "G3+";
        if (version == 1.2)
        {
          // Get Card Generation from ATR historical bytes
          byte[] hb = card.getATR().getHistoricalBytes();
          for (int i = 1; i < hb.length; ++i) {
            if ((hb[i] & 0xf0) == 0x50) {
              generation = hb[i + 2];
              break;
            } else {
              i += hb[i] & 0x0f;
            }
          }
          friendlyName = "G" + generation;

          if (generation == 3) {
            // SELECT application
            execSELECT_AID(channel, AID_INFOBOX);
            // SELECT file
            try {
              // the file identifier has changed with version G3b
              execSELECT_FID(channel, EF_INFOBOX);
              friendlyName = "G3b";
            } catch (FileNotFoundException e) {
              friendlyName = "G3a";
            }
          }
        }
        log.info("e-card version=" + version + " (" + friendlyName + ")");
      }
    } catch (CardException e) {
      log.warn("Failed to execute command.", e);
    } catch (SignatureCardException e) {
      log.warn("Failed to execute command.", e);
    }

    cardPinInfo = new PinInfo(4, 12, "[0-9]",
        "at/gv/egiz/smcc/STARCOSCard", "card.pin", KID_PIN_CARD, null, 10);
    // Currently not used
    // if (generation == 4) {
    //   cardPukInfo = new PinInfo(8, 12, "[0-9]",
    //       "at/gv/egiz/smcc/STARCOSCard", "card.puk", KID_PUK_CARD, null, 10);
    // }
    ssPinInfo = new PinInfo(6, 12, "[0-9]",
        "at/gv/egiz/smcc/STARCOSCard", "sig.pin", KID_PIN_SS, AID_DF_SS,
        (version < 1.2) ? 3 : 10);

    if (SignatureCardFactory.ENFORCE_RECOMMENDED_PIN_LENGTH) {
      cardPinInfo.setRecLength(4);
      if (cardPukInfo != null)
        cardPukInfo.setRecLength(10);
      ssPinInfo.setRecLength(6);
    }
  }

  @Override
  @Exclusive
  public byte[] getCertificate(KeyboxName keyboxName, PINGUI provider)
      throws SignatureCardException {

    byte[] aid;
    byte[] fid;
    if (keyboxName == KeyboxName.SECURE_SIGNATURE_KEYPAIR) {
      aid = AID_DF_SS;
      fid = EF_C_X509_CH_DS;
    } else if (keyboxName == KeyboxName.CERTIFIED_KEYPAIR) {
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
  public byte[] getInfobox(String infobox, PINGUI pinGUI, String domainId)
      throws SignatureCardException, InterruptedException {
  
    try {
      if ("IdentityLink".equals(infobox)) {

        CardChannel channel = getCardChannel();
        // SELECT application
        execSELECT_AID(channel, AID_INFOBOX);
        // SELECT file
        try {
          // the file identifier has changed with version G3b
          execSELECT_FID(channel, EF_INFOBOX);
        } catch (FileNotFoundException e) {
          // fallback for < G3b 
          log.debug("Not an eCard G3b, falling back to legacy FID for EF_Infobox.");
          execSELECT_FID(channel, EF_INFOBOX_LEGACY);
        }

        InfoboxContainer infoboxContainer = null;
        while (infoboxContainer == null) {
          try {
            
            TransparentFileInputStream is = ISO7816Utils
                .openTransparentFileInputStream(channel, -1);
            infoboxContainer = new InfoboxContainer(is, (byte) 0x30);

          } catch (IOException e) {
            if (e.getCause() instanceof SecurityStatusNotSatisfiedException) {
              verifyPINLoop(channel, cardPinInfo, pinGUI);
            } else {
              log.warn("Failed to read infobox.", e);
              throw new SignatureCardException("Failed to read infobox.", e);
            }
          }
        }

        for (Infobox box : infoboxContainer.getInfoboxes()) {
          if (box.getTag() == 0x01) {
            if (box.isEncrypted()) {

              execSELECT_AID(channel, AID_DF_GS);

              byte[] tlv;
              if (generation < 4)
                tlv = new byte[] {
                    (byte) 0x84, (byte) 0x03, (byte) 0x80, (byte) 0x03, (byte) 0x00,
                    (byte) 0x80, (byte) 0x01, (byte) 0x81};
              else
                tlv = new byte[] {
                    (byte) 0x84, (byte) 0x01, (byte) 0x83,
                    (byte) 0x95, (byte) 0x01, (byte) 0x40,
                    (byte) 0x80, (byte) 0x01, (byte) 0x10};

              execMSE(channel, 0x41, 0xb8, tlv);

              byte[] plainKey = null;

              while (true) {
                try {
                  plainKey = execPSO_DECIPHER(channel, box.getEncryptedKey());
                  break;
                } catch(SecurityStatusNotSatisfiedException e) {
                  verifyPINLoop(channel, cardPinInfo, pinGUI);
                }
              }

              return box.decipher(plainKey);
              
            } else {
              return box.getData();
            }
          }
        }

        // empty
        throw new NotActivatedException();

        
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
    } catch (FileNotFoundException e) {
      throw new NotActivatedException(e);
    } catch (CardException e) {
      log.warn("Failed to execute command.", e);
      throw new SignatureCardException("Failed to access card.", e);
    }
  }

  @Override
  @Exclusive
  public byte[] createSignature(InputStream input, KeyboxName keyboxName,
      PINGUI provider, String alg) throws SignatureCardException, InterruptedException, IOException {
  
    ByteArrayOutputStream dst = new ByteArrayOutputStream();
    byte[] ht = null;
    
    MessageDigest md = null;
    byte[] digestInfo = null;

    if (generation < 4)
      dst.write(new byte[] {(byte) 0x84, (byte) 0x03, (byte) 0x80});
    try {
      if (alg == null || "http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha1".equals(alg)) {
        // local key ID '02' version '00'
        dst.write(new byte[] {(byte) 0x02, (byte) 0x00});
        if (version < 1.2) {
          // algorithm ID ECDSA with SHA-1
          dst.write(new byte[] {(byte) 0x89, (byte) 0x03, (byte) 0x13, (byte) 0x35, (byte) 0x10});
        } else if (generation < 4) {
          // portable algorithm reference
          dst.write(new byte[] {(byte) 0x80, (byte) 0x01, (byte) 0x04});
          // hash template
          ht = new byte[] {(byte) 0x80, (byte) 0x01, (byte) 0x10};
        } else {
          // ECC-Key-ID
          dst.write(new byte[] { (byte) 0x84, (byte) 0x01, (byte) 0x82 });
          // usage qualifier
          dst.write(new byte[] { (byte) 0x95, (byte) 0x01, (byte) 0x40 });
        }
        md = MessageDigest.getInstance("SHA-1");
      } else if (version >= 1.2 && "http://www.w3.org/2000/09/xmldsig#rsa-sha1".equals(alg)) {
        if (generation < 4) {
          // local key ID '03' version '00'
          dst.write(new byte[] {(byte) 0x03, (byte) 0x00});
          // portable algorithm reference
          dst.write(new byte[] {(byte) 0x80, (byte) 0x01, (byte) 0x02});
          // hash template
          ht = new byte[] {(byte) 0x80, (byte) 0x01, (byte) 0x10};
        } else {
          // RSA-Key-ID
          dst.write(new byte[] { (byte) 0x84, (byte) 0x01, (byte) 0x83 });
          // usage qualifier
          dst.write(new byte[] { (byte) 0x95, (byte) 0x01, (byte) 0x40 });
          // algorithm reference
          dst.write(new byte[] { (byte) 0x80, (byte) 0x01, (byte) 0x10 });
          // digestInfo template (SEQUENCE{SEQUENCE{OID, NULL}, OCTET STRING Hash)
          digestInfo = new byte[] {
              (byte) 0x30, (byte) 0x21, (byte) 0x30, (byte) 0x09, (byte) 0x06,
              (byte) 0x05, (byte) 0x2B, (byte) 0x0E, (byte) 0x03, (byte) 0x02,
              (byte) 0x1A, (byte) 0x05, (byte) 0x00, (byte) 0x04, (byte) 0x14
          };
        }
        md = MessageDigest.getInstance("SHA-1");
      } else if (version >= 1.2 && "http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha256".equals(alg)) {
        if (generation < 4) {
          // local key ID '02' version '00'
          dst.write(new byte[] {(byte) 0x02, (byte) 0x00});
          // portable algorithm reference
          dst.write(new byte[] {(byte) 0x80, (byte) 0x01, (byte) 0x04});
          // hash template
          ht = new byte[] {(byte) 0x80, (byte) 0x01, (byte) 0x40};
        } else {
          // ECC-Key-ID
          dst.write(new byte[] { (byte) 0x84, (byte) 0x01, (byte) 0x82 });
          // usage qualifier
          dst.write(new byte[] { (byte) 0x95, (byte) 0x01, (byte) 0x40 });
        }
        md = MessageDigest.getInstance("SHA-256");
      } else if (version >= 1.2 && "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256".equals(alg)) {
        if (generation < 4) {
          // local key ID '03' version '00'
          dst.write(new byte[] {(byte) 0x03, (byte) 0x00});
          // portable algorithm reference
          dst.write(new byte[] {(byte) 0x80, (byte) 0x01, (byte) 0x02});
          // hash template
          ht = new byte[] {(byte) 0x80, (byte) 0x01, (byte) 0x40};
        } else {
          // RSA-Key-ID
          dst.write(new byte[] { (byte) 0x84, (byte) 0x01, (byte) 0x83 });
          // usage qualifier
          dst.write(new byte[] { (byte) 0x95, (byte) 0x01, (byte) 0x40 });
          // algorithm reference
          dst.write(new byte[] { (byte) 0x80, (byte) 0x01, (byte) 0x10 });
          // digestInfo template (SEQUENCE{SEQUENCE{OID, NULL}, OCTET STRING Hash)
          digestInfo = new byte[] {
           (byte) 0x30, (byte) 0x31, (byte) 0x30, (byte) 0x0d, (byte) 0x06,
           (byte) 0x09, (byte) 0x60, (byte) 0x86, (byte) 0x48, (byte) 0x01,
           (byte) 0x65, (byte) 0x03, (byte) 0x04, (byte) 0x02, (byte) 0x01,
           (byte) 0x05, (byte) 0x00, (byte) 0x04, (byte) 0x20
          };
        }
        md = MessageDigest.getInstance("SHA-256");
      } else if ("http://www.w3.org/2007/05/xmldsig-more#ecdsa-ripemd160".equals(alg)) {
        // local key ID '02' version '00'
        dst.write(new byte[] {(byte) 0x02, (byte) 0x00});
        if (version < 1.2) {
          // algorithm ID ECDSA with RIPEMD160 doesn't work
          //dst.write(new byte[] {(byte) 0x89, (byte) 0x03, (byte) 0x13, (byte) 0x35, (byte) 0x20});
          // algorithm ID ECDSA with SHA-1
          dst.write(new byte[] {(byte) 0x89, (byte) 0x03, (byte) 0x13, (byte) 0x35, (byte) 0x10});
        } else if (generation < 4) {
          // portable algorithm reference
          dst.write(new byte[] {(byte) 0x80, (byte) 0x01, (byte) 0x04});
          // hash template (SHA-1 - no EF_ALIAS for RIPEMD160)
          //ht = new byte[] {(byte) 0x80, (byte) 0x01, (byte) 0x10};
          // hash template for RIPEMD160
          ht = new byte[] {(byte) 0x89, (byte) 0x02, (byte) 0x14, (byte) 0x30};
        } else {
          throw new SignatureCardException("e-card " + friendlyName + " does not support signature algorithm " + alg + ".");
        }
        md = MessageDigest.getInstance("RIPEMD160");
      } else {
        throw new SignatureCardException("e-card " + friendlyName + " does not support signature algorithm " + alg + ".");
      }
    } catch (CryptoException e) {
      log.error("Failed to get MessageDigest.", e);
      throw new SignatureCardException(e);
    }

    // calculate message digest
    byte[] digest = new byte[md.getDigestLength()];
    for (int l; (l = input.read(digest)) != -1;) {
      md.update(digest, 0, l);
    }
    digest = md.digest();
    if (digestInfo != null) {
      // convert into DigestInfo structure for G4
      byte[] d = new byte[digestInfo.length + digest.length];
      System.arraycopy(digestInfo, 0, d, 0, digestInfo.length);
      System.arraycopy(digest, 0, d, digestInfo.length, digest.length);
      digest = d;
    }

    try {
      
      CardChannel channel = getCardChannel();
      
      if (KeyboxName.SECURE_SIGNATURE_KEYPAIR.equals(keyboxName)) {
        
        // SELECT MF
        execSELECT_MF(channel);
        // SELECT application
        execSELECT_AID(channel, AID_DF_SS);
        // VERIFY
        verifyPINLoop(channel, ssPinInfo, provider);
        // MANAGE SECURITY ENVIRONMENT : SET DST
        if (generation < 4) // not necessary for G4
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
        
        
      } else if (KeyboxName.CERTIFIED_KEYPAIR.equals(keyboxName)) {

        // SELECT application
        execSELECT_AID(channel, AID_DF_GS);
        // MANAGE SECURITY ENVIRONMENT : SET DST
        execMSE(channel, 0x41, 0xb6, dst.toByteArray());
        if (version >= 1.2 && ht != null) {
          // PERFORM SECURITY OPERATION : SET HT
          execMSE(channel, 0x41, 0xaa, ht);
        }
        if (generation < 4) {
          // PERFORM SECURITY OPERATION : HASH
          execPSO_HASH(channel, digest);
        }
        while (true) {
          try {
            // PERFORM SECURITY OPERATION : COMPUTE DIGITAL SIGNATURE
            if (generation < 4)
              return execPSO_COMPUTE_DIGITAL_SIGNATURE(channel, null);
            else
              return execPSO_COMPUTE_DIGITAL_SIGNATURE(channel, digest);
          } catch (SecurityStatusNotSatisfiedException e) {
            verifyPINLoop(channel, cardPinInfo, provider);
          }
        }
        
      } else {
        throw new IllegalArgumentException("KeyboxName '" + keyboxName
            + "' not supported.");
      }

    } catch (CardException e) {
      log.warn("Failed to execute command.", e);
      throw new SignatureCardException("Failed to access card.", e);
    }
  
  }

  /* (non-Javadoc)
   * @see at.gv.egiz.smcc.AbstractSignatureCard#verifyPIN(at.gv.egiz.smcc.PinInfo, at.gv.egiz.smcc.PINProvider)
   */
  @Override
  @Exclusive
  public void verifyPIN(PinInfo pinInfo, PINGUI pinProvider)
      throws LockedException, NotActivatedException, CancelledException,
      TimeoutException, SignatureCardException, InterruptedException {
    
    CardChannel channel = getCardChannel();
    
    try {
      if (pinInfo.getContextAID() != null) {
        // SELECT application
        execSELECT_AID(channel, pinInfo.getContextAID());
      }
      verifyPINLoop(channel, pinInfo, pinProvider);
    } catch (CardException e) {
      log.info("Failed to verify PIN.", e);
      throw new SignatureCardException("Failed to verify PIN.", e);
    }
    
  }
  
  /* (non-Javadoc)
   * @see at.gv.egiz.smcc.AbstractSignatureCard#changePIN(at.gv.egiz.smcc.PinInfo, at.gv.egiz.smcc.ChangePINProvider)
   */
  @Override
  @Exclusive
  public void changePIN(PinInfo pinInfo, ModifyPINGUI pinGUI)
      throws LockedException, NotActivatedException, CancelledException,
      TimeoutException, SignatureCardException, InterruptedException {
    
    CardChannel channel = getCardChannel();
    
    try {
      if (pinInfo.getContextAID() != null) {
        // SELECT application
        execSELECT_AID(channel, pinInfo.getContextAID());
      }
      changePINLoop(channel, pinInfo, pinGUI);
    } catch (CardException e) {
      log.info("Failed to change PIN.", e);
      throw new SignatureCardException("Failed to change PIN.", e);
    }
    
  }

  /* (non-Javadoc)
   * @see at.gv.egiz.smcc.AbstractSignatureCard#activatePIN(at.gv.egiz.smcc.PinInfo, at.gv.egiz.smcc.PINProvider)
   */
  @Override
  @Exclusive
  public void activatePIN(PinInfo pinInfo, ModifyPINGUI activatePINGUI)
      throws CancelledException, SignatureCardException, CancelledException,
      TimeoutException, InterruptedException {

    CardChannel channel = getCardChannel();

    try {
      if (pinInfo.getContextAID() != null) {
        // SELECT application
        execSELECT_AID(channel, pinInfo.getContextAID());
      }
      activatePIN(channel, pinInfo, activatePINGUI);
    } catch (CardException e) {
      log.info("Failed to activate PIN.", e);
      throw new SignatureCardException("Failed to activate PIN.", e);
    }
    
  }

  /* (non-Javadoc)
   * @see at.gv.egiz.smcc.PINMgmtSignatureCard#unblockPIN(at.gv.egiz.smcc.PinInfo, at.gv.egiz.smcc.PINProvider)
   */
  @Override
  public void unblockPIN(PinInfo pinInfo, ModifyPINGUI pukProvider)
      throws CancelledException, SignatureCardException, InterruptedException {
    CardChannel channel = getCardChannel();

    try {
      unblockPINLoop(channel, pinInfo, pukProvider);
    } catch (CardException e) {
      log.info("Failed to activate PIN.", e);
      throw new SignatureCardException("Failed to activate PIN.", e);
    }
  }

  @Override
  public void reset() throws SignatureCardException {
    try {
      super.reset();
      log.debug("Select MF (e-card workaround).");
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
   * @see at.gv.egiz.smcc.PINMgmtSignatureCard#getPinInfos()
   */
  @Override
  public PinInfo[] getPinInfos() throws SignatureCardException {

    if (version >= 1.2) {
      //check if card is activated
      getCertificate(KeyboxName.SECURE_SIGNATURE_KEYPAIR, null);
    }

    PinInfo[] pinInfos;
    if (cardPukInfo != null)
      pinInfos = new PinInfo[] { cardPinInfo, cardPukInfo, ssPinInfo };
    else
      pinInfos = new PinInfo[] { cardPinInfo, ssPinInfo };

    CardChannel channel = getCardChannel();
    for (PinInfo pinInfo : pinInfos) {
      if (pinInfo.getState() == PinInfo.STATE.UNKNOWN ) {
        try {
          log.debug("Query pin status for {}.", pinInfo.getLocalizedName());
          if (pinInfo.getContextAID() != null) {
            execSELECT_AID(channel, pinInfo.getContextAID());
          }
          verifyPIN(channel, pinInfo, null, 0);
        } catch (Exception e) {
          log.trace("Failed to execute command.", e);
          // status already set by verifyPIN
        }
      } else if (log.isTraceEnabled()) {
        log.trace("assume pin status {} to be up to date", pinInfo.getState());
      }
    }
    return pinInfos;
  }

  @Override
  public String toString() {
    return ("e-card version " + version + " (" + friendlyName + ")");
  }
  
  ////////////////////////////////////////////////////////////////////////
  // PROTECTED METHODS (assume exclusive card access)
  ////////////////////////////////////////////////////////////////////////
  
  protected void verifyPINLoop(CardChannel channel, PinInfo pinInfo, PINGUI provider)
      throws LockedException, NotActivatedException, SignatureCardException,
      InterruptedException, CardException {
    
    int retries = verifyPIN(channel, pinInfo, null, -1);
    do {
      retries = verifyPIN(channel, pinInfo, provider, retries);
    } while (retries > 0);
  }

  protected void changePINLoop(CardChannel channel, PinInfo pinInfo, ModifyPINGUI provider)
      throws LockedException, NotActivatedException, SignatureCardException,
      InterruptedException, CardException {

    int retries = verifyPIN(channel, pinInfo, null, -1);
    do {
      retries = changePIN(channel, pinInfo, provider, retries);
    } while (retries > 0);
  }
  
  protected void unblockPINLoop(CardChannel channel, PinInfo pinInfo, ModifyPINGUI provider)
      throws LockedException, NotActivatedException, SignatureCardException,
      InterruptedException, CardException {

    //TODO get PUK retry counter from EF FID 0036 in MF
    int retries = -1;
    do {
      retries = unblockPIN(channel, pinInfo, provider, retries);
    } while (retries > 0);
  }

  protected int verifyPIN(CardChannel channel, PinInfo pinInfo,
      PINGUI provider, int retries) throws SignatureCardException,
      LockedException, NotActivatedException, InterruptedException,
      CardException {
    
    VerifyAPDUSpec apduSpec = new VerifyAPDUSpec(
        new byte[] {
            (byte) 0x00, (byte) 0x20, (byte) 0x00, pinInfo.getKID(), (byte) 0x08,
            (byte) 0x20, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
            (byte) 0xff, (byte) 0xff, (byte) 0xff }, 
        1, VerifyAPDUSpec.PIN_FORMAT_BCD, 7, 4, 4);
    
    ResponseAPDU resp;
    if (provider != null) {
      resp = reader.verify(channel, apduSpec, provider, pinInfo, retries);
    } else {
      resp = channel.transmit(new CommandAPDU(0x00, 0x20, 0x00, pinInfo.getKID()));
    }


    if (resp.getSW() == 0x9000) {
      pinInfo.setActive(pinInfo.maxRetries);
      return -1;
    } else if (resp.getSW() == 0x6983 || resp.getSW() == 0x63c0) {
      // authentication method blocked (0x63c0 returned by 'short' VERIFY)
      pinInfo.setBlocked();
      throw new LockedException();
    } else if (resp.getSW() == 0x6984 || resp.getSW() == 0x6985) {
      // reference data not usable; conditions of use not satisfied
      pinInfo.setNotActive();
      throw new NotActivatedException();
    } else if (resp.getSW() >> 4 == 0x63c) {
      pinInfo.setActive(0x0f & resp.getSW());
      return 0x0f & resp.getSW();
    } else if (version >= 1.2 && resp.getSW() == 0x6400) {
      String msg = "VERIFY failed, card not activated. SW=0x6400";
      log.error(msg);
      pinInfo.setNotActive();
      throw new SignatureCardException(msg);
    } else {
      String msg = "VERIFY failed. SW=" + Integer.toHexString(resp.getSW()); 
      log.error(msg);
      pinInfo.setUnknown();
      throw new SignatureCardException(msg);
    }
  }
  
  protected int changePIN(CardChannel channel, PinInfo pinInfo,
      ModifyPINGUI pinProvider, int retries) throws CancelledException,
      InterruptedException, CardException, SignatureCardException {

      ChangeReferenceDataAPDUSpec apduSpec = new ChangeReferenceDataAPDUSpec(
        new byte[] {
            (byte) 0x00, (byte) 0x24, (byte) 0x00, pinInfo.getKID(), (byte) 0x10,
            (byte) 0x20, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
            (byte) 0xff, (byte) 0xff, (byte) 0xff,
            (byte) 0x20, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
            (byte) 0xff, (byte) 0xff, (byte) 0xff },
        1, VerifyAPDUSpec.PIN_FORMAT_BCD, 7, 4, 4, 8);

    ResponseAPDU resp = reader.modify(channel, apduSpec, pinProvider, pinInfo, retries);

    if (resp.getSW() == 0x9000) {
      pinInfo.setActive(pinInfo.maxRetries);
      return -1;
    } else if (resp.getSW() == 0x6983) {
      // authentication method blocked
      pinInfo.setBlocked();
      throw new LockedException();
    } else if (resp.getSW() == 0x6984) {
      pinInfo.setNotActive();
      throw new NotActivatedException();
    } else if (resp.getSW() >> 4 == 0x63c) {
      pinInfo.setActive(0x0f & resp.getSW());
      return 0x0f & resp.getSW();
    } else {
      String msg = "CHANGE REFERENCE DATA failed. SW=" + Integer.toHexString(resp.getSW()); 
      log.error(msg);
      pinInfo.setUnknown();
      throw new SignatureCardException(msg);
    }
  }

  protected int activatePIN(CardChannel channel, PinInfo pinInfo,
      ModifyPINGUI provider) throws SignatureCardException,
      InterruptedException, CardException {
    
    ResponseAPDU resp;
    if (version < 1.2) {
      NewReferenceDataAPDUSpec apduSpec = new NewReferenceDataAPDUSpec(
          new byte[] {
              (byte) 0x00, (byte) 0x24, (byte) 0x01, pinInfo.getKID(), (byte) 0x08,
              (byte) 0x20, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
              (byte) 0xff, (byte) 0xff, (byte) 0xff },
          1, VerifyAPDUSpec.PIN_FORMAT_BCD, 7, 4, 4);

      resp = reader.modify(channel, apduSpec, provider, pinInfo);
    } else {
      NewReferenceDataAPDUSpec apduSpec;
      if (generation < 4) {
        apduSpec = new NewReferenceDataAPDUSpec(
            new byte[] {
                (byte) 0x00, (byte) 0x24, (byte) 0x00, pinInfo.getKID(), (byte) 0x10,
                (byte) 0x26, (byte) 0x12, (byte) 0x34, (byte) 0x56, (byte) 0xff,
                (byte) 0xff, (byte) 0xff, (byte) 0xff,
                (byte) 0x20, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
                (byte) 0xff, (byte) 0xff, (byte) 0xff },
            1, VerifyAPDUSpec.PIN_FORMAT_BCD, 7, 4, 4);
      } else {
        apduSpec = new NewReferenceDataAPDUSpec(
            new byte[] {
                (byte) 0x00, (byte) 0x24, (byte) 0x00, pinInfo.getKID(), (byte) 0x10,
                (byte) 0x25, (byte) 0x12, (byte) 0x34, (byte) 0x5f, (byte) 0xff,
                (byte) 0xff, (byte) 0xff, (byte) 0xff,
                (byte) 0x20, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
                (byte) 0xff, (byte) 0xff, (byte) 0xff },
            1, VerifyAPDUSpec.PIN_FORMAT_BCD, 7, 4, 4);
      }
      apduSpec.setPinInsertionOffsetNew(8);
      resp = reader.modify(channel, apduSpec, provider, pinInfo);
    }

    if (resp.getSW() == 0x9000) {
      pinInfo.setActive(pinInfo.maxRetries);
      return -1;
    } else {
      String msg = "CHANGE REFERENCE DATA failed. SW=" + Integer.toHexString(resp.getSW());
      log.error(msg);
      pinInfo.setUnknown();
      throw new SignatureCardException(msg);
    }
  }
  
  protected int unblockPIN(CardChannel channel, PinInfo pinInfo,
      ModifyPINGUI provider, int retries) throws SignatureCardException,
      InterruptedException, CardException {

    if (version < 1.2) {
      // would return 0x6982 (Security status not satisfied)
      throw new SignatureCardException("RESET RETRY COUNTER is not supported by this card.");
    }

    ResetRetryCounterAPDUSpec apduSpec = new ResetRetryCounterAPDUSpec(
        new byte[] {
            (byte) 0x00, (byte) 0x2c, (byte) 0x00, pinInfo.getKID(), (byte) 0x10,
            (byte) 0x20, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
            (byte) 0xff, (byte) 0xff, (byte) 0xff,
            (byte) 0x20, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
            (byte) 0xff, (byte) 0xff, (byte) 0xff },
        1, VerifyAPDUSpec.PIN_FORMAT_BCD, 7, 4, 4, 8);

    ResponseAPDU resp = reader.modify(channel, apduSpec, provider, pinInfo, retries);

    if (resp.getSW() == 0x9000) {
      pinInfo.setActive(pinInfo.maxRetries);
      return -1;
    } else if (resp.getSW() == 0x6983) {
      // PUK blocked
      throw new LockedException();
    } else if (resp.getSW() == 0x6984) {
      // PIN not active
      pinInfo.setNotActive();
      throw new NotActivatedException();
    } else if (resp.getSW() >> 4 == 0x63c) {
      // wrong PUK, return PUK retries
      return 0x0f & resp.getSW();
    } else {
      String msg = "RESET RETRY COUNTER failed. SW=" + Integer.toHexString(resp.getSW());
      log.error(msg);
      pinInfo.setUnknown();
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
          "PSO: COMPUTE DIGITAL SIGNATURE failed: SW="
              + Integer.toHexString(resp.getSW()));
    } else {
      return resp.getData();
    }
  }
  
  protected byte[] execPSO_DECIPHER(CardChannel channel, byte [] cipher) throws CardException, SignatureCardException {
    
    byte[] data = new byte[cipher.length + 1];
    data[0] = (byte) 0x81;
    System.arraycopy(cipher, 0, data, 1, cipher.length);
    ResponseAPDU resp = channel.transmit(new CommandAPDU(0x00, 0x2A, 0x80, 0x86, data, 256));
    if (resp.getSW() == 0x6982) {
      throw new SecurityStatusNotSatisfiedException();
    } else if (resp.getSW() != 0x9000) {
      throw new SignatureCardException(
          "PSO - DECIPHER failed: SW="
          + Integer.toHexString(resp.getSW()));
    }
    
    return resp.getData();
    
  }

}
