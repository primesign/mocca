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

import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

public class ACOSCard extends AbstractSignatureCard implements SignatureCard {

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

  byte[] selectFileAID(byte[] fid) throws CardException, SignatureCardException {
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

  byte[] selectFileFID(byte[] fid) throws CardException, SignatureCardException {
    CardChannel channel = getCardChannel();
    ResponseAPDU resp = transmit(channel, new CommandAPDU(0x00, 0xA4, 0x00,
        0x00, fid, 256));
    if (resp.getSW() == 0x6a82) {
      throw new SignatureCardException("Failed to select file (FID="
          + toString(fid) + "): SW=" + Integer.toHexString(resp.getSW()) + ")");
    } else {
      return resp.getBytes();
    }
  }

  /**
   * 
   * @param pinProvider
   * @param spec
   *          the PIN spec to be given to the pinProvider
   * @param kid
   *          the KID (key identifier) of the PIN to be verified
   * @param kfpc
   *          acutal value of the KFCP (key fault presentation counter) or less
   *          than 0 if actual value is unknown
   * 
   * @return -1 if the PIN has been verifyed successfully, or else the new value
   *         of the KFCP (key fault presentation counter)
   * 
   * @throws CancelledException
   *           if the user canceld the operation
   * @throws javax.smartcardio.CardException
   * @throws at.gv.egiz.smcc.SignatureCardException
   */
  int verifyPIN(PINProvider pinProvider, PINSpec spec, byte kid, int kfpc)
      throws CardException, CancelledException, SignatureCardException {

    CardChannel channel = getCardChannel();

    // get PIN
    String pin = pinProvider.providePIN(spec, kfpc);
    if (pin == null) {
      // User canceld operation
      // throw new CancelledException("User canceld PIN entry");
      return -2;
    }

    logger.finest("PIN=" + pin);

    byte[] asciiPIN = pin.getBytes(Charset.forName("ASCII"));
    byte[] encodedPIN = new byte[8];
    System.arraycopy(asciiPIN, 0, encodedPIN, 0, Math.min(asciiPIN.length,
        encodedPIN.length));

    ResponseAPDU resp = transmit(channel, new CommandAPDU(0x00, 0x20, 0x00,
        kid, encodedPIN));
    if (resp.getSW1() == (byte) 0x63 && resp.getSW2() >> 4 == (byte) 0xc) {
      return resp.getSW2() & (byte) 0x0f;
    } else if (resp.getSW() == 0x6983) {
      // PIN blocked
      throw new SignatureCardException(spec.getLocalizedName() + " blocked.");
    } else if (resp.getSW() != 0x9000) {
      throw new SignatureCardException("Failed to verify pin: SW="
          + Integer.toHexString(resp.getSW()) + ".");
    } else {
      return -1;
    }

  }

  void mseSetDST(byte[] dst) throws CardException, SignatureCardException {
    CardChannel channel = getCardChannel();
    ResponseAPDU resp = transmit(channel, new CommandAPDU(0x00, 0x22, 0x81,
        0xB6, dst));
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

  public byte[] getCertificate(KeyboxName keyboxName)
      throws SignatureCardException {

    if (keyboxName == KeyboxName.SECURE_SIGNATURE_KEYPAIR) {
      return readTLVFile(AID_SIG, EF_C_CH_DS, EF_C_CH_DS_MAX_SIZE);
    } else if (keyboxName == KeyboxName.CERITIFIED_KEYPAIR) {
      return readTLVFile(AID_DEC, EF_C_CH_EKEY, EF_C_CH_EKEY_MAX_SIZE);
    } else {
      throw new IllegalArgumentException("Keybox " + keyboxName
          + " not supported.");
    }

  }

  public byte[] getInfobox(String infobox, PINProvider provider, String domainId)
      throws SignatureCardException {

    if ("IdentityLink".equals(infobox)) {

      PINSpec spec = new PINSpec(4, 4, "[0-9]", getResourceBundle().getString(
          "inf.pin.name"));
      try {
        byte[] res = readTLVFilePIN(AID_DEC, EF_INFOBOX, KID_PIN_INF, provider,
            spec, EF_INFOBOX_MAX_SIZE);
        return res;
      } catch (Exception e) {
        throw new SecurityException(e);
      }

    } else {
      throw new IllegalArgumentException("Infobox '" + infobox
          + "' not supported.");
    }

  }

  public String toString() {
    return "a-sign premium";
  }

  public byte[] createSignature(byte[] hash, KeyboxName keyboxName,
      PINProvider provider) throws SignatureCardException {

    if (hash.length != 20) {
      throw new IllegalArgumentException("Hash value must be of length 20");
    }

    byte[] fid;
    byte kid;
    byte[] dst;
    PINSpec spec;
    if (KeyboxName.SECURE_SIGNATURE_KEYPAIR.equals(keyboxName)) {
      fid = DF_SIG;
      kid = KID_PIN_SIG;
      dst = DST_SIG;
      spec = new PINSpec(6, 10, "[0-9]", getResourceBundle().getString(
          "sig.pin.name"));

    } else if (KeyboxName.CERITIFIED_KEYPAIR.equals(keyboxName)) {
      fid = DF_DEC;
      kid = KID_PIN_DEC;
      dst = DST_DEC;
      spec = new PINSpec(6, 10, "[0-9]", getResourceBundle().getString(
          "dec.pin.name"));

    } else {
      throw new IllegalArgumentException("KeyboxName '" + keyboxName
          + "' not supported.");
    }

    try {

      // SELECT DF
      selectFileFID(fid);
      // VERIFY
      int kfpc = -1;
      while (true) {
        kfpc = verifyPIN(provider, spec, kid, kfpc);
        if (kfpc < -1) {
          return null;
        } else if (kfpc < 0) {
          break;
        }
      }
      // MSE: SET DST
      mseSetDST(dst);
      // PSO: HASH
      psoHash(hash);
      // PSO: COMPUTE DIGITAL SIGNATURE
      byte[] rs = psoComputDigitalSiganture();

      return rs;

    } catch (CardException e) {
      throw new SignatureCardException("Failed to create signature.", e);
    }
  }
}
