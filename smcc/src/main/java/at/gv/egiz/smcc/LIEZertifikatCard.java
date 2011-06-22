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

import at.gv.egiz.smcc.cio.CIOCertificate;
import at.gv.egiz.smcc.cio.ObjectDirectory;
import at.gv.egiz.smcc.pin.gui.PINGUI;
import at.gv.egiz.smcc.util.ISO7816Utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.gv.egiz.smcc.util.SMCCHelper;
import java.util.Arrays;
import javax.smartcardio.Card;
import javax.smartcardio.CardTerminal;

public class LIEZertifikatCard extends AbstractSignatureCard implements SignatureCard {
  
  /**
   * Logging facility.
   */
  private final Logger log = LoggerFactory.getLogger(LIEZertifikatCard.class);

  public static final byte[] MF = new byte[] { (byte) 0x3F, (byte) 0x00 };

  // DF.CIA
  public static final byte[] AID_SIG = new byte[] {
    (byte) 0xA0, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x63,
    (byte) 0x50, (byte) 0x4B, (byte) 0x43, (byte) 0x53, (byte) 0x2D,
    (byte) 0x31, (byte) 0x35 };

  public static final byte[] EF_CD = new byte[] { (byte) 0x44, (byte) 0x04};

  public static final byte[] PKCS1_PADDING = new byte[] {
    (byte) 0x30, (byte) 0x21, (byte) 0x30, (byte) 0x09, (byte) 0x06,
    (byte) 0x05, (byte) 0x2b, (byte) 0x0e, (byte) 0x03, (byte) 0x02,
    (byte) 0x1a, (byte) 0x05, (byte) 0x00, (byte) 0x04, (byte) 0x14 };


  public static final byte KID = (byte) 0x82;

  //TODO should be part of PinInfo
  public static final int STORED_LENGTH = 8;

  protected PinInfo pinInfo = new PinInfo(4, 8, "[0-9]",
          "at/gv/egiz/smcc/LIEZertifikatCard", "pin", KID, AID_SIG, 3);
  protected String name = "LIEZertifikat";

  ObjectDirectory ef_od = new ObjectDirectory();
  CIOCertificate cioQCert;
  
  @Override
  public void init(Card card, CardTerminal cardTerminal) {
    super.init(card, cardTerminal);
    log.debug("initializing {} for ATR {}", name, toString(card.getATR().getBytes()));
  }

  @Override
  public String toString() {
    return name;
  }

  @Override
  @Exclusive
  public byte[] getCertificate(KeyboxName keyboxName, PINGUI pinGUI)
      throws SignatureCardException {

    if (keyboxName != KeyboxName.SECURE_SIGNATURE_KEYPAIR) {
      throw new IllegalArgumentException("Keybox " + keyboxName
          + " not supported");
    }

    try {
      CardChannel channel = getCardChannel();
      // SELECT DF.CIA
      execSELECT_AID(channel, AID_SIG);

      ensureCIOQCertificate(channel);

      // SELECT CERT, assume efid
      execSELECT_EF(channel, cioQCert.getEfidOrPath());

      // READ BINARY
      byte[] certificate = ISO7816Utils.readTransparentFileTLV(channel, -1, (byte) 0x30);
      if (certificate == null) {
        throw new NotActivatedException();
      }
      return certificate;
      
    } catch (FileNotFoundException e) {
      throw new NotActivatedException();
      } catch (IOException ex) {
          log.warn("failed to get certificate info", ex);
          throw new SignatureCardException(ex);
    } catch (CardException e) {
      log.info("Failed to get certificate.", e);
      throw new SignatureCardException(e);
    }

  }

  @Override
  @Exclusive
  public byte[] getInfobox(String infobox, PINGUI provider, String domainId)
      throws SignatureCardException, InterruptedException {
      
    throw new IllegalArgumentException("Infobox '" + infobox
        + "' not supported.");
  }

  @Override
  @Exclusive
  public byte[] createSignature(InputStream input, KeyboxName keyboxName,
      PINGUI provider, String alg) throws SignatureCardException, InterruptedException, IOException {
    
    if (KeyboxName.SECURE_SIGNATURE_KEYPAIR != keyboxName) {
      throw new SignatureCardException("Card does not support key " + keyboxName + ".");
    }
    if (!"http://www.w3.org/2000/09/xmldsig#rsa-sha1".equals(alg)) {
      throw new SignatureCardException("Card does not support algorithm " + alg + ".");
    }

    
    MessageDigest md;
    try {
      md = MessageDigest.getInstance("SHA-1");
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

    ByteArrayOutputStream data = new ByteArrayOutputStream();
    
    try {
      // oid
      data.write(PKCS1_PADDING);
      // hash
      data.write(digest);
    } catch (IOException e) {
      throw new SignatureCardException(e);
    }
    
    try {
      
      CardChannel channel = getCardChannel();

      // SELECT AID
      execSELECT_AID(channel, AID_SIG);
      // VERIFY
      verifyPINLoop(channel, pinInfo, provider);
      // MANAGE SECURITY ENVIRONMENT : SET SE
      execMSE_SET(channel, getCRT_AT(channel));
      // PERFORM SECURITY OPERATION : COMPUTE DIGITAL SIGNATURE
      return execINTERNAL_AUTHENTICATE(channel, data.toByteArray());

    } catch (CardException e) {
      log.warn("Failed to execute command.", e);
      throw new SignatureCardException("Failed to access card.", e);
    }

  }

  protected void verifyPINLoop(CardChannel channel, PinInfo spec,
      PINGUI provider) throws LockedException, NotActivatedException,
      SignatureCardException, InterruptedException, CardException {
    
    int retries = -1; //verifyPIN(channel, spec, null, -1);
    do {
      retries = verifyPIN(channel, spec, provider, retries);
    } while (retries > 0);
  }

  protected int verifyPIN(CardChannel channel, PinInfo pinSpec,
      PINGUI provider, int retries) throws SignatureCardException,
      LockedException, NotActivatedException, InterruptedException,
      CardException {
    
    VerifyAPDUSpec apduSpec = new VerifyAPDUSpec(
        new byte[] {
            (byte) 0x00, (byte) 0x20, (byte) 0x00, pinSpec.getKID()},
        0, VerifyAPDUSpec.PIN_FORMAT_ASCII, STORED_LENGTH);
    
    ResponseAPDU resp = reader.verify(channel, apduSpec, provider, pinSpec, retries);
    
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
  
  protected byte[] execSELECT_AID(CardChannel channel, byte[] aid)
      throws SignatureCardException, CardException {

    // add Ne, otherwise 67:00
    ResponseAPDU resp = channel.transmit(
        new CommandAPDU(0x00, 0xA4, 0x04, 0x00, aid, 256));
    
    if (resp.getSW() == 0x6A82) {
      String msg = "File or application not found AID="
          + SMCCHelper.toString(aid) + " SW="
          + Integer.toHexString(resp.getSW()) + ".";
      log.info(msg);
      throw new FileNotFoundException(msg);
    } else if (resp.getSW() != 0x9000) {
      String msg = "Failed to select application FID="
          + SMCCHelper.toString(aid) + " SW="
          + Integer.toHexString(resp.getSW()) + ".";
      log.error(msg);
      throw new SignatureCardException(msg);
    } else {
      return resp.getBytes();
    }
    
  }

  protected byte[] execSELECT_EF(CardChannel channel, byte[] fid)
          throws SignatureCardException, CardException {

    ResponseAPDU resp = channel.transmit(
      new CommandAPDU(0x00, 0xA4, 0x02, 0x00, fid, 256));

    if (resp.getSW() == 0x6A82) {
      String msg = "File or application not found FID="
          + SMCCHelper.toString(fid) + " SW="
          + Integer.toHexString(resp.getSW()) + ".";
      log.info(msg);
      throw new FileNotFoundException(msg);
    } else if (resp.getSW() != 0x9000) {
      String msg = "Failed to select FID="
          + SMCCHelper.toString(fid) + " SW="
          + Integer.toHexString(resp.getSW()) + ".";
      log.error(msg);
      throw new SignatureCardException(msg);
    } else {
      return resp.getBytes();
    }

  }


  protected void execMSE_SET(CardChannel channel, byte[] at)
      throws CardException, SignatureCardException {

    // don't add Ne, causes 67:00
    ResponseAPDU resp = channel.transmit(
        new CommandAPDU(0x00, 0x22, 0x41, 0xa4, at));
    if (resp.getSW() != 0x9000) {
      throw new SignatureCardException("MSE:SET failed: SW="
          + Integer.toHexString(resp.getSW()));
    }
  }

  protected byte[] execINTERNAL_AUTHENTICATE(CardChannel channel, byte[] AI)
      throws CardException, SignatureCardException {
    ResponseAPDU resp;
    resp = channel.transmit(
        new CommandAPDU(0x00, 0x88, 0x00, 0x00, AI, 256));
    if (resp.getSW() == 0x6982) {
      throw new SecurityStatusNotSatisfiedException();
    } else if (resp.getSW() == 0x6983) {
      throw new LockedException();
    } else if (resp.getSW() != 0x9000) {
      throw new SignatureCardException(
          "INTERNAL AUTHENTICATE failed: SW="
              + Integer.toHexString(resp.getSW()));
    } else {
      return resp.getData();
    }
  }

    private void ensureCIOQCertificate(CardChannel channel) throws IOException, CardException, NotActivatedException, SignatureCardException {

        if (cioQCert != null) {
            return;
        }
        
        List<CIOCertificate> certCIOs = ef_od.getCD(channel).getCIOs(channel);

        for (CIOCertificate cio : certCIOs) {

            String label = cio.getLabel();
            //"Name (qualified signature"
            if (label != null && label.toLowerCase().contains("qualified signature")) {
                log.debug("found certificate: {} (fid={})", label,
                        toString(cio.getEfidOrPath()));
                cioQCert = cio;
            }
        }
        //fallback for old cards
        if (cioQCert == null) {
            for (CIOCertificate cio : certCIOs) {
                String label = cio.getLabel();
                //"TEST LLV APO 2s Liechtenstein Post Qualified CA ID"
                if (label != null && label.toLowerCase().contains("liechtenstein post qualified ca id")) {
                    log.debug("found certificate: {} (fid={})", label,
                        toString(cio.getEfidOrPath()));
                    cioQCert = cio;
                }
            }
        }

        if (cioQCert == null) {
            throw new NotActivatedException();
        }
    }

    protected byte[] getCRT_AT(CardChannel channel) throws CardException, SignatureCardException, IOException {

        ensureCIOQCertificate(channel);
        List<CIOCertificate> keyCIOs = ef_od.getPrKD(channel).getCIOs(channel);

      int i = 1;
      for (CIOCertificate cio : keyCIOs) {
        if (Arrays.equals(cio.getiD(), cioQCert.getiD())) {

            byte[] CRT_AT = new byte[] {
                  // 1-byte keyReference
                  (byte) 0x84, (byte) 0x01, (byte) (0x80 | (0x7f & i)),
                  // 3-byte keyReference
        //          (byte) 0x84, (byte) 0x03, (byte) 0x80, (byte) 0x01, (byte) 0xff,
                  //RSA Authentication
                  (byte) 0x89, (byte) 0x02, (byte) 0x23, (byte) 0x13
              };

              return CRT_AT;
        }
        i++;
      }

      log.error("no PrK CIO corresponding to QCert {} found", toString(cioQCert.getiD()));
      throw new SignatureCardException("could not determine PrK for QCert " + toString(cioQCert.getiD()));
      
  }


}
