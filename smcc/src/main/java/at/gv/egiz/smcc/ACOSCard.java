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

import iaik.me.asn1.ASN1;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import iaik.me.security.CryptoException;
import iaik.me.security.MessageDigest;

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

public class ACOSCard extends AbstractSignatureCard implements
PINMgmtSignatureCard {

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
	  
	  public static final byte[] EF_INFO = new byte[] { (byte) 0xd0, (byte) 0x02 };

	  public static final int EF_INFOBOX_MAX_SIZE = 1500;

	  public static final byte KID_PIN_SIG = (byte) 0x81;

	  public static final byte KID_PUK_SIG = (byte) 0x83;

	  public static final byte KID_PIN_DEC = (byte) 0x81;

	  public static final byte KID_PUK_DEC = (byte) 0x82;

	  public static final byte KID_PIN_INF = (byte) 0x83;

	  public static final byte KID_PUK_INF = (byte) 0x84;

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

	  public static final byte[] AT_DEC = new byte[] { (byte) 0x84, (byte) 0x01, // tag
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

	  private final Logger log = LoggerFactory.getLogger(ACOSCard.class);
	  
	  protected PinInfo decPinInfo, sigPinInfo, infPinInfo;
	  
	  private AbstractACOSCardInfoboxHandler _infoboxHandler;
	  
	  /**
	   * The version of the card's digital signature application.
	   */
	  protected int appVersion = -1;
	
	  /**
	   * The detected type of ACOS card.
	   */
	  protected String cardType;
	
	  @Override
	  public void init(Card card, CardTerminal cardTerminal) {
	    super.init(card, cardTerminal);

	    log.info("ACOS card found");

	    // determine application version
	    try {
	      CardChannel channel = getCardChannel();
	      // SELECT application
	      execSELECT_AID(channel, AID_SIG);
	      // SELECT file
	      execSELECT_FID(channel, EF_INFO);
	      // READ BINARY
	      TransparentFileInputStream is = ISO7816Utils.openTransparentFileInputStream(channel, 8);
	      appVersion = is.read();
	      log.info("a-sign premium application version = " + appVersion);
	    } catch (FileNotFoundException e) {
	      appVersion = 1;
	      log.info("a-sign premium application version = " + appVersion);
	    } catch (SignatureCardException e) {
	      log.warn("Failed to execute command.", e);
	      appVersion = 0;
	    } catch (IOException e) {
	      log.warn("Failed to execute command.", e);
	      appVersion = 0;
	    } catch (CardException e) {
	      log.warn("Failed to execute command.", e);
	      appVersion = 0;
	    }

	    decPinInfo = new PinInfo(0, 8, "[0-9]",
	      "at/gv/egiz/smcc/ACOSCard", "dec.pin", KID_PIN_DEC, AID_DEC, 10);

	    sigPinInfo = new PinInfo(0, 8, "[0-9]",
	      "at/gv/egiz/smcc/ACOSCard", "sig.pin", KID_PIN_SIG, AID_SIG, 10);

	    infPinInfo= new PinInfo(0, 8, "[0-9]",
	      "at/gv/egiz/smcc/ACOSCard", "inf.pin", KID_PIN_INF, AID_DEC, 10);

	    if (SignatureCardFactory.ENFORCE_RECOMMENDED_PIN_LENGTH) {
	      decPinInfo.setRecLength(4);
	      sigPinInfo.setRecLength(6);
	      infPinInfo.setRecLength(4);
	    }

		// read signing certificate to distinguish between Austrian and Liechtenstein cards
		try {
			byte[] cert = getCertificate(KeyboxName.SECURE_SIGNATURE_KEYPAIR, null); 

			ASN1 cert_asn1 = new ASN1(cert);
			// certificate
			// +-tbsCertificate(0)
			//   +-...
			//   +-issuer(3)
			//     +-[RelativeDistinguishedName]
			//       +-[AttributeValueAssertion]
			//         +-attributeType
			//         +-attributeValue
			ASN1 issuer = cert_asn1.getElementAt(0).getElementAt(3);
			String countryID = null;
			for (int i = 0; i < issuer.getSize(); ++i) {
				if (issuer.getElementAt(i).getElementAt(0).getElementAt(0).gvString().equals(ASN1.OID_NAME_C)) {
					countryID = issuer.getElementAt(i).getElementAt(0).getElementAt(1).gvString();
					break;
				}
			}

			if (countryID == null) {
				log.debug("No Country found! - default to ACOS Austria.");
				cardType = "A-Sign premium";
				_infoboxHandler = new ACOSATCard(this);
			}
			else if (countryID.equalsIgnoreCase("LI")) {
				log.debug("Identified lisign card.");
				cardType = "lisign";
				_infoboxHandler = new ACOSLIESignCard(this);
			} else {
				log.debug("No lisign card - default to ACOS Austria.");
				cardType = "A-Sign premium";
				_infoboxHandler = new ACOSATCard(this);
			}
			log.info("Identified {} card", cardType);
		} catch (SignatureCardException e) {
			log.warn("Cannot determine card type by certificate. Using default.", e);
			_infoboxHandler = new ACOSATCard(this);
		} catch (IOException e) {
			log.warn("Cannot determine card type by certificate. Using default.", e);
			_infoboxHandler = new ACOSATCard(this);
		} catch (RuntimeException e) {
			log.warn("Cannot determine card type by certificate. Using default.", e);
			_infoboxHandler = new ACOSATCard(this);
		}
	}

	  @Override
	  @Exclusive
	  public byte[] getCertificate(KeyboxName keyboxName, PINGUI provider)
	      throws SignatureCardException {
	    
	      byte[] aid;
	      byte[] fid;
	      if (keyboxName == KeyboxName.SECURE_SIGNATURE_KEYPAIR) {
	        aid = AID_SIG;
	        fid = EF_C_CH_DS;
	      } else if (keyboxName == KeyboxName.CERTIFIED_KEYPAIR) {
	        aid = AID_DEC;
	        fid = EF_C_CH_EKEY;
	      } else {
	        throw new IllegalArgumentException("Keybox " + keyboxName
	            + " not supported.");
	      }

	      try {
	        CardChannel channel = getCardChannel();
	        // SELECT application
	        execSELECT_AID(channel, aid);
	        // SELECT file
	        byte[] fcx = execSELECT_FID(channel, fid);
	        int maxSize = -1;
	        if (getAppVersion() < 2) {
	          maxSize = ISO7816Utils.getLengthFromFCx(fcx);
	          log.debug("Size of selected file = {}.", maxSize);
	        }
	        // READ BINARY
	        byte[] certificate = ISO7816Utils.readTransparentFileTLV(channel, maxSize, (byte) 0x30);
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
	    
		  return _infoboxHandler.getInfobox(infobox, provider, domainId);
	  }


	  
	  @Override
	  @Exclusive
	  public byte[] createSignature(InputStream input, KeyboxName keyboxName,
	      PINGUI provider, String alg) throws SignatureCardException, InterruptedException, IOException {
	  
	    ByteArrayOutputStream dst = new ByteArrayOutputStream();
	    // key ID
	    dst.write(new byte[]{(byte) 0x84, (byte) 0x01, (byte) 0x88});
	    // algorithm ID
	    dst.write(new byte[]{(byte) 0x80, (byte) 0x01});
	    
	    MessageDigest md;
	    try {
	      if (KeyboxName.SECURE_SIGNATURE_KEYPAIR.equals(keyboxName)
	          && (alg == null || "http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha1".equals(alg))) {
	        dst.write((byte) 0x14); // SHA-1/ECC
	        md = MessageDigest.getInstance("SHA-1");
	      } else if (KeyboxName.CERTIFIED_KEYPAIR.equals(keyboxName)
	          && (alg == null || "http://www.w3.org/2000/09/xmldsig#rsa-sha1".equals(alg))) {
	        dst.write((byte) 0x12); // SHA-1 with padding according to PKCS#1 block type 01
	        md = MessageDigest.getInstance("SHA-1");
	      } else if (KeyboxName.SECURE_SIGNATURE_KEYPAIR.equals(keyboxName)
	          && appVersion >= 2
	          && "http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha256".equals(alg)) {
	        dst.write((byte) 0x44); // SHA-256/ECC
	        md = MessageDigest.getInstance("SHA-256");
	      } else if (KeyboxName.CERTIFIED_KEYPAIR.equals(keyboxName)
	          && appVersion >= 2
	          && "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256".equals(alg)) {
	        dst.write((byte) 0x41); // SHA-256 with padding according to PKCS#1
	        md = MessageDigest.getInstance("SHA-256");
	      } else if (KeyboxName.SECURE_SIGNATURE_KEYPAIR.equals(keyboxName)
	          && "http://www.w3.org/2007/05/xmldsig-more#ecdsa-ripemd160".equals(alg)) {
	        dst.write((byte) 0x14); // No RIPEMD support - use SHA-1/ECC
	        md = MessageDigest.getInstance("RIPEMD160");
	      } else {
	        throw new SignatureCardException("Card does not support signature algorithm " + alg + ".");
	      }
	    } catch (CryptoException e) {
	      log.error("Failed to get MessageDigest.", e);
	      throw new SignatureCardException(e);
	    }
	    
	    byte[] digest = new byte[md.getDigestLength()];
	    for (int l; (l = input.read(digest)) != -1;) {
	      md.update(digest, 0, l);
	    }
	    digest = md.digest();
	  
	    try {
	      
	      CardChannel channel = getCardChannel();

	      if (KeyboxName.SECURE_SIGNATURE_KEYPAIR.equals(keyboxName)) {

	        // SELECT application
	        execSELECT_AID(channel, AID_SIG);
	        // MANAGE SECURITY ENVIRONMENT : SET DST
	        execMSE(channel, 0x41, 0xb6, dst.toByteArray());
	        // VERIFY
	        verifyPINLoop(channel, sigPinInfo, provider);
	        // PERFORM SECURITY OPERATION : HASH
	        execPSO_HASH(channel, digest);
	        // PERFORM SECURITY OPERATION : COMPUTE DIGITAL SIGNATURE
	        return execPSO_COMPUTE_DIGITAL_SIGNATURE(channel);
	    
	      } else if (KeyboxName.CERTIFIED_KEYPAIR.equals(keyboxName)) {
	        
	        // SELECT application
	        execSELECT_AID(channel, AID_DEC);
	        // MANAGE SECURITY ENVIRONMENT : SET AT
	        execMSE(channel, 0x41, 0xa4, AT_DEC);
	        
	        while (true) {
	          try {
	            // INTERNAL AUTHENTICATE
	            return execINTERNAL_AUTHENTICATE(channel, digest);
	          } catch (SecurityStatusNotSatisfiedException e) {
	            verifyPINLoop(channel, decPinInfo, provider);
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
	  
	  public int getAppVersion() {
	    return appVersion;
	  }

	  /* (non-Javadoc)
	   * @see at.gv.egiz.smcc.AbstractSignatureCard#verifyPIN(at.gv.egiz.smcc.pinInfo, at.gv.egiz.smcc.PINProvider)
	   */
	  @Override
	  public void verifyPIN(PinInfo pinInfo, PINGUI pinProvider)
	      throws LockedException, NotActivatedException, CancelledException,
	      TimeoutException, SignatureCardException, InterruptedException {

	    CardChannel channel = getCardChannel();
	    
	    try {
	      // SELECT application
	      execSELECT_AID(channel, pinInfo.getContextAID());
	      // VERIFY
	      verifyPINLoop(channel, pinInfo, pinProvider);
	    } catch (CardException e) {
	      log.info("Failed to verify PIN.", e);
	      throw new SignatureCardException("Failed to verify PIN.", e);
	    }

	  }
	  
	  /* (non-Javadoc)
	   * @see at.gv.egiz.smcc.AbstractSignatureCard#changePIN(at.gv.egiz.smcc.pinInfo, at.gv.egiz.smcc.ChangePINProvider)
	   */
	  @Override
	  public void changePIN(PinInfo pinInfo, ModifyPINGUI pinProvider)
	      throws LockedException, NotActivatedException, CancelledException,
	      TimeoutException, SignatureCardException, InterruptedException {

	    CardChannel channel = getCardChannel();
	    
	    try {
	      // SELECT application
	      execSELECT_AID(channel, pinInfo.getContextAID());
	      // CHANGE REFERENCE DATA
	      changePINLoop(channel, pinInfo, pinProvider);
	    } catch (CardException e) {
	      log.info("Failed to change PIN.", e);
	      throw new SignatureCardException("Failed to change PIN.", e);
	    }

	  }

	  @Override
	  public void activatePIN(PinInfo pinInfo, ModifyPINGUI pinGUI)
	      throws CancelledException, SignatureCardException, CancelledException,
	      TimeoutException, InterruptedException {
	    log.error("ACTIVATE PIN not supported by ACOS");
	    throw new SignatureCardException("PIN activation not supported by this card.");
	  }

	  @Override
	  public void unblockPIN(PinInfo pinInfo, ModifyPINGUI pinGUI)
	      throws CancelledException, SignatureCardException, InterruptedException {
	    throw new SignatureCardException("Unblock PIN not supported.");
	  }
	  
	  /* (non-Javadoc)
	   * @see at.gv.egiz.smcc.PINMgmtSignatureCard#getpinInfos()
	   */
	  @Override
	  public PinInfo[] getPinInfos() throws SignatureCardException {
	    
	    //check if card is activated
	    getCertificate(KeyboxName.SECURE_SIGNATURE_KEYPAIR, null);
	    
	    if (appVersion < 2) {
	      return new PinInfo[] {decPinInfo, sigPinInfo, infPinInfo };
	    }
	    return new PinInfo[] {decPinInfo, sigPinInfo };
	  }

	  @Override
	  public String toString() {
	    String ret = "ACOS card: " + cardType;
	    if (getAppVersion() > 0)
	      ret += " (application version " + getAppVersion() + ")";
	    return ret;
	  }

	  ////////////////////////////////////////////////////////////////////////
	  // PROTECTED METHODS (assume exclusive card access)
	  ////////////////////////////////////////////////////////////////////////

	  protected void verifyPINLoop(CardChannel channel, PinInfo spec, PINGUI provider)
	      throws InterruptedException, CardException, SignatureCardException {
	    
	    int retries = -1;
	    do {
	      retries = verifyPIN(channel, spec, provider, retries);
	    } while (retries > 0);
	  }

	  protected void changePINLoop(CardChannel channel, PinInfo spec, ModifyPINGUI provider)
	      throws InterruptedException, CardException, SignatureCardException {

	    int retries = -1;
	    do {
	      retries = changePIN(channel, spec, provider, retries);
	    } while (retries > 0);
	  }

	  protected int verifyPIN(CardChannel channel, PinInfo pinInfo,
	      PINGUI provider, int retries) throws InterruptedException, CardException, SignatureCardException {
	    
	    VerifyAPDUSpec apduSpec = new VerifyAPDUSpec(
	        new byte[] {
	            (byte) 0x00, (byte) 0x20, (byte) 0x00, pinInfo.getKID(), (byte) 0x08,
	            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
	            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00 }, 
	        0, VerifyAPDUSpec.PIN_FORMAT_ASCII, 8);
	    
	    ResponseAPDU resp = reader.verify(channel, apduSpec, provider, pinInfo, retries);
	    
	    if (resp.getSW() == 0x9000) {
	      pinInfo.setActive(pinInfo.maxRetries);
	      return -1;
	    }
	    if (resp.getSW() >> 4 == 0x63c) {
	      pinInfo.setActive(0x0f & resp.getSW());
	      return 0x0f & resp.getSW();
	    }

	    switch (resp.getSW()) {
	    case 0x6983:
	      // authentication method blocked
	      pinInfo.setBlocked();
	      throw new LockedException();
	  
	    default:
	      String msg = "VERIFY failed. SW=" + Integer.toHexString(resp.getSW()); 
	      log.info(msg);
	      pinInfo.setUnknown();
	      throw new SignatureCardException(msg);
	    }

	  }

	  protected int changePIN(CardChannel channel, PinInfo pinInfo,
	      ModifyPINGUI pinProvider, int retries) throws CancelledException, InterruptedException, CardException, SignatureCardException {

	    ChangeReferenceDataAPDUSpec apduSpec = new ChangeReferenceDataAPDUSpec(
	        new byte[] {
	            (byte) 0x00, (byte) 0x24, (byte) 0x00, pinInfo.getKID(), (byte) 0x10,
	            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,       
	            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,       
	            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,       
	            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00        
	        }, 
	        0, VerifyAPDUSpec.PIN_FORMAT_ASCII, 8);
	    
	    
	    
	    ResponseAPDU resp = reader.modify(channel, apduSpec, pinProvider, pinInfo, retries);
	    
	    if (resp.getSW() == 0x9000) {
	      pinInfo.setActive(pinInfo.maxRetries);
	      return -1;
	    }
	    if (resp.getSW() >> 4 == 0x63c) {
	      pinInfo.setActive(0x0f & resp.getSW());
	      return 0x0f & resp.getSW();
	    }
	    
	    switch (resp.getSW()) {
	    case 0x6983:
	      // authentication method blocked
	      pinInfo.setBlocked();
	      throw new LockedException();
	  
	    default:
	      String msg = "CHANGE REFERENCE DATA failed. SW=" + Integer.toHexString(resp.getSW()); 
	      log.info(msg);
	      pinInfo.setUnknown();
	      throw new SignatureCardException(msg);
	    }
	    
	  }
	  
	  protected void execMSE(CardChannel channel, int p1,
		      int p2, byte[] data) throws SignatureCardException, CardException {

		    ResponseAPDU resp = channel.transmit(
		        new CommandAPDU(0x00, 0x22, p1, p2, data));

		    if (resp.getSW() != 0x9000) {
		      String msg = "MSE failed: SW="
		          + Integer.toHexString(resp.getSW());
		      log.error(msg);
		      throw new SignatureCardException(msg);
		    } 
		    
		  }
		  
		  protected byte[] execPSO_DECIPHER(CardChannel channel, byte [] cipher) throws CardException, SignatureCardException {
		    
		    byte[] data = new byte[cipher.length + 1];
		    data[0] = 0x00;
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
		  
		  protected void execPSO_HASH(CardChannel channel, byte[] hash) throws CardException, SignatureCardException {
		    
		    ResponseAPDU resp = channel.transmit(
		        new CommandAPDU(0x00, 0x2A, 0x90, 0x81, hash));
		    if (resp.getSW() != 0x9000) {
		      throw new SignatureCardException("PSO - HASH failed: SW="
		          + Integer.toHexString(resp.getSW()));
		    }
		    
		  }
		  
		  protected byte[] execPSO_COMPUTE_DIGITAL_SIGNATURE(CardChannel channel) throws CardException,
		      SignatureCardException {

		    ResponseAPDU resp = channel.transmit(
		        new CommandAPDU(0x00, 0x2A, 0x9E, 0x9A, 256));
		    if (resp.getSW() == 0x6982) {
		      throw new SecurityStatusNotSatisfiedException();
		    }
		    if (resp.getSW() != 0x9000) {
		      throw new SignatureCardException(
		          "PSO - COMPUTE DIGITAL SIGNATURE failed: SW="
		              + Integer.toHexString(resp.getSW()));
		    } else {
		      return resp.getData();
		    }

		  }
		  
		  protected byte[] execINTERNAL_AUTHENTICATE(CardChannel channel, byte[] hash) throws CardException,
		      SignatureCardException {

		    byte[] digestInfo = new byte[] { (byte) 0x30, (byte) 0x21, (byte) 0x30,
		        (byte) 0x09, (byte) 0x06, (byte) 0x05, (byte) 0x2B, (byte) 0x0E,
		        (byte) 0x03, (byte) 0x02, (byte) 0x1A, (byte) 0x05, (byte) 0x00,
		        (byte) 0x04 };
		    
		    byte[] data = new byte[digestInfo.length + hash.length + 1];
		    
		    System.arraycopy(digestInfo, 0, data, 0, digestInfo.length);
		    data[digestInfo.length] = (byte) hash.length;
		    System.arraycopy(hash, 0, data, digestInfo.length + 1, hash.length);
		    
		    ResponseAPDU resp = channel.transmit(new CommandAPDU(0x00, 0x88, 0x10, 0x00, data, 256));
		    if (resp.getSW() == 0x6982) {
		      throw new SecurityStatusNotSatisfiedException();
		    } else if (resp.getSW() == 0x6983) {
		      throw new LockedException();
		    } else if (resp.getSW() != 0x9000) {
		      throw new SignatureCardException("INTERNAL AUTHENTICATE failed: SW="
		          + Integer.toHexString(resp.getSW()));
		    } else {
		      return resp.getData();
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
	
}
