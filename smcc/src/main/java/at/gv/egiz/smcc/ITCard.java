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

import at.gv.egiz.smcc.pin.gui.PINGUI;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.gv.egiz.smcc.util.ISO7816Utils;
import at.gv.egiz.smcc.util.SMCCHelper;

public class ITCard extends AbstractSignatureCard {
  
  /**
   * Logging facility.
   */
  private final Logger log = LoggerFactory.getLogger(STARCOSCard.class);
  
  public static final byte[] MF = new byte[] { (byte) 0x3F, (byte) 0x00 };
  
  public static final byte[] DF1 = new byte[] { (byte) 0x11, (byte) 0x00 };
  
  public static final byte[] EF_C_Carta = new byte[] { (byte) 0x11, (byte) 0x01 };
  
  protected PinInfo ssPinInfo = new PinInfo(5, 8, "[0-9]",
          "at/gv/egiz/smcc/ITCard", "sig.pin", (byte) 0x10,
          new byte[] { (byte) 0x11, (byte) 0x00 }, PinInfo.UNKNOWN_RETRIES);
    
  @Override
  @Exclusive
  public byte[] getCertificate(KeyboxName keyboxName, PINGUI provider)
      throws SignatureCardException, InterruptedException {

    if (keyboxName != KeyboxName.SECURE_SIGNATURE_KEYPAIR) {
      throw new IllegalArgumentException("Keybox " + keyboxName
          + " not supported");
    }

    try {
      CardChannel channel = getCardChannel();
      // SELECT MF
      execSELECT_FID(channel, MF);
      // SELECT application
      execSELECT_FID(channel, DF1);
      // SELECT EF_C_Carta
      byte[] fcx = execSELECT_FID(channel, EF_C_Carta);
      int maxsize = ISO7816Utils.getLengthFromFCx(fcx);
      // READ BINARY
      byte[] certificate = ISO7816Utils.readTransparentFileTLV(channel, maxsize, (byte) 0x30);
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
  public byte[] getInfobox(String infobox, PINGUI provider, String domainId)
      throws SignatureCardException, InterruptedException {
      
    throw new IllegalArgumentException("Infobox '" + infobox
        + "' not supported.");
  }

  @Override
  @Exclusive
  public byte[] createSignature(InputStream input, KeyboxName keyboxName,
      PINGUI provider, String alg) throws SignatureCardException,
      InterruptedException, IOException {

    if (KeyboxName.SECURE_SIGNATURE_KEYPAIR != keyboxName) {
      throw new SignatureCardException("Card does not support key " + keyboxName + ".");
    }
    if (!"http://www.w3.org/2000/09/xmldsig#rsa-sha1".equals(alg)) {
      throw new SignatureCardException("Card does not support algorithm " + alg + ".");
    }

    byte[] dst = new byte[] {
        (byte) 0x83, // tag for algorithm reference
        (byte) 0x01, // algorithm reference
        (byte) 0x01  // private key reference
    };
    
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

    try {
      
      CardChannel channel = getCardChannel();

      // SELECT MF
      execSELECT_FID(channel, MF);
      // VERIFY
      verifyPINLoop(channel, ssPinInfo, provider);
      // MANAGE SECURITY ENVIRONMENT : RESTORE SE
      execMSE(channel, 0xF3, 0x03, null);
      // MANAGE SECURITY ENVIRONMENT : SET DST
      execMSE(channel, 0xF1, 0xB8, dst);
      // PERFORM SECURITY OPERATION : COMPUTE DIGITAL SIGNATURE
      return execPSO_COMPUTE_DIGITAL_SIGNATURE(channel, digest);

    } catch (CardException e) {
      log.warn("Failed to execute command.", e);
      throw new SignatureCardException("Failed to access card.", e);
    }

  }

  protected void verifyPINLoop(CardChannel channel, PinInfo spec,
      PINGUI provider) throws LockedException, NotActivatedException,
      SignatureCardException, InterruptedException, CardException {
    
    int retries = -1;
    do {
      retries = verifyPIN(channel, spec, provider, retries);
    } while (retries >= -1);
  }

  protected int verifyPIN(CardChannel channel, PinInfo pinSpec,
      PINGUI provider, int retries) throws SignatureCardException,
      LockedException, NotActivatedException, InterruptedException,
      CardException {
    
    VerifyAPDUSpec apduSpec = new VerifyAPDUSpec(
        new byte[] {
            (byte) 0x00, (byte) 0x20, (byte) 0x00, pinSpec.getKID(), (byte) 0x08,
            (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
            (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff }, 
        0, VerifyAPDUSpec.PIN_FORMAT_ASCII, 8);
    
    ResponseAPDU resp = reader.verify(channel, apduSpec, provider, pinSpec, retries);
    
    if (resp.getSW() == 0x9000) {
      return -2;
    }
    if (resp.getSW() >> 4 == 0x63c) {
      return 0x0f & resp.getSW();
    }
    
    switch (resp.getSW()) {
    case 0x6300:
      // incorrect PIN, number of retries not provided
      return -1;
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

  protected byte[] execSELECT_FID(CardChannel channel, byte[] fid)
      throws SignatureCardException, CardException {
    
    ResponseAPDU resp = channel.transmit(
        new CommandAPDU(0x00, 0xA4, 0x00, 0x00, fid, 256));
    
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
    
    ResponseAPDU resp;
    if (data == null) {
      resp = channel.transmit(new CommandAPDU(0x00, 0x22, p1, p2));
    } else {
      resp = channel.transmit(new CommandAPDU(0x00, 0x22, p1, p2, data));
    }
    
    if (resp.getSW() != 0x9000) {
      throw new SignatureCardException("MSE:SET failed: SW="
          + Integer.toHexString(resp.getSW()));
    }
  }

  protected byte[] execPSO_COMPUTE_DIGITAL_SIGNATURE(CardChannel channel,
      byte[] hash) throws CardException, SignatureCardException {
    
    byte[] oid = new byte[] { (byte) 0x30, (byte) 0x21, (byte) 0x30,
        (byte) 0x09, (byte) 0x06, (byte) 0x05, (byte) 0x2b,
        (byte) 0x0e, (byte) 0x03, (byte) 0x02, (byte) 0x1a,
        (byte) 0x05, (byte) 0x00, (byte) 0x04, (byte) 0x14 };
    
    ByteArrayOutputStream data = new ByteArrayOutputStream();
    
    try {
      // header
      data.write(new byte[] { (byte) 0x00, (byte) 0x00, (byte) 0x01 });
      // padding
      for (int i = 0, len = 125 - hash.length - oid.length; i < len; i++) {
        data.write((byte) 0xFF);
      }
      data.write((byte) 0x00);
      // oid
      data.write(oid);
      // hash
      data.write(hash);
    } catch (IOException e) {
      throw new SignatureCardException(e);
    }
    
    ResponseAPDU resp = channel
          .transmit(new CommandAPDU(0x00, 0x2A, 0x80, 0x86, data.toByteArray(), 0x81));

    
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

}
