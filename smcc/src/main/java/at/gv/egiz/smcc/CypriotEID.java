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

import iaik.me.asn1.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.gv.egiz.smcc.SignatureCard.KeyboxName;
import at.gv.egiz.smcc.cio.ObjectDirectory;
import at.gv.egiz.smcc.pin.gui.ModifyPINGUI;
import at.gv.egiz.smcc.pin.gui.PINGUI;
import at.gv.egiz.smcc.util.ISO7816Utils;
import at.gv.egiz.smcc.util.SMCCHelper;
import at.gv.egiz.smcc.util.TransparentFileInputStream;

public class CypriotEID extends AbstractSignatureCard implements
		PINMgmtSignatureCard {

	private final Logger log = LoggerFactory.getLogger(ACOSCard.class);

	public static final byte KID_PUK_SIG = (byte) 0x02;

	public static final byte KID_PIN_SIG = (byte) 0x01;

	public static final byte[] CD_ID = new byte[] { (byte) 0x70, (byte) 0x05 };
	
	public static final byte[] MF_ID = new byte[] { (byte) 0x3f, (byte) 0x00 };

	public static final byte[] ADF_AWP_ID = new byte[] { (byte) 0xad,
			(byte) 0xf1 };

	public static final byte[] AID_SIG = new byte[] { (byte) 0xA0, (byte) 0x00,
			(byte) 0x00, (byte) 0x00, (byte) 0x77, (byte) 0x01, (byte) 0x08,
			(byte) 0x00, (byte) 0x07, (byte) 0x00, (byte) 0x00, (byte) 0xFE,
			(byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00 };

	PinInfo pinPinInfo, pukPinInfo;

	ObjectDirectory od;

	protected byte[] cert_id;
	
	@Override
	public void init(Card card, CardTerminal cardTerminal) {
		super.init(card, cardTerminal);

		log.info("Cypriot EID found");

		pinPinInfo = new PinInfo(4, 8, "[0-9]", "at/gv/egiz/smcc/CypriotEID",
				"sig.pin", KID_PIN_SIG, AID_SIG, 3);

		//pinPinInfo.setActive(3);
		
		pukPinInfo = new PinInfo(4, 8, "[0-9]", "at/gv/egiz/smcc/CypriotEID",
				"sig.puk", KID_PUK_SIG, AID_SIG, 3);
		
		try {
			this.exec_readcd(getCardChannel());
		} catch (CardException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SignatureCardException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public byte[] getCertificate(KeyboxName keyboxName, PINGUI pinGUI)
			throws SignatureCardException, InterruptedException {
		CardChannel channel = getCardChannel();

		try {
			return exec_readcert(channel);
		} catch (CardException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}

	@Override
	public byte[] getInfobox(String infobox, PINGUI pinGUI, String domainId)
			throws SignatureCardException, InterruptedException {
		throw new IllegalArgumentException("Infobox '" + infobox
				+ "' not supported.");
	}

	@Override
	public byte[] createSignature(InputStream input, KeyboxName keyboxName,
			PINGUI pinGUI, String alg) throws SignatureCardException,
			InterruptedException, IOException {
		
		byte AlgID = 0;
	    
	    MessageDigest md;
	    try {
	      if (KeyboxName.SECURE_SIGNATURE_KEYPAIR.equals(keyboxName)
	          && (alg == null || "http://www.w3.org/2000/09/xmldsig#rsa-sha1".equals(alg))) {
	    	  AlgID = (byte) 0x12; // SHA-1 with padding according to PKCS#1 block type 01
	        md = MessageDigest.getInstance("SHA-1");
	      } else if (KeyboxName.SECURE_SIGNATURE_KEYPAIR.equals(keyboxName)
	          && "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256".equals(alg)) {
	    	  AlgID = (byte) 0x41; // SHA-256 with padding according to PKCS#1
	        md = MessageDigest.getInstance("SHA256");
	      }  else {
	        throw new SignatureCardException("Card does not support signature algorithm " + alg + ".");
	      }
	    } catch (NoSuchAlgorithmException e) {
	      log.error("Failed to get MessageDigest.", e);
	      throw new SignatureCardException(e);
	    }
	    
	    byte[] digest = new byte[md.getDigestLength()];
	    for (int l; (l = input.read(digest)) != -1;) {
	      md.update(digest, 0, l);
	    }
	    digest = md.digest();
	    
	    try
	    {
	    	CardChannel channel = getCardChannel();

	        // SELECT application
	        exec_selectADF(channel);
	        
	        // MANAGE SECURITY ENVIRONMENT : SET DST
	        exec_MSE(channel, AlgID);
	        // VERIFY
	        verifyPINLoop(channel, pinPinInfo, pinGUI);
	        
	        // PERFORM SECURITY OPERATION : COMPUTE DIGITAL SIGNATRE
	        return exec_sign(channel, digest);
	    }
	    catch (Exception e) {
	    	e.printStackTrace();
	    	return null;
		}
	}

	@Override
	public PinInfo[] getPinInfos() throws SignatureCardException {
		// TODO Auto-generated method stub
		return new PinInfo[] { pinPinInfo };
	}

	@Override
	public void verifyPIN(PinInfo pinInfo, PINGUI pinGUI)
			throws LockedException, NotActivatedException, CancelledException,
			SignatureCardException, InterruptedException {
		CardChannel channel = getCardChannel();

		try {
			// SELECT application
			exec_selectADF(channel);
			// VERIFY
			verifyPINLoop(channel, pinInfo, pinGUI);
		} catch (CardException e) {
			log.info("Failed to verify PIN.", e);
			throw new SignatureCardException("Failed to verify PIN.", e);
		}
	}

	@Override
	public void changePIN(PinInfo pinInfo, ModifyPINGUI changePINGUI)
			throws LockedException, NotActivatedException, CancelledException,
			PINFormatException, SignatureCardException, InterruptedException {
		CardChannel channel = getCardChannel();

		exec_unblockPIN(channel, changePINGUI);
	}

	@Override
	public void activatePIN(PinInfo pinInfo, ModifyPINGUI activatePINGUI)
			throws CancelledException, SignatureCardException,
			InterruptedException {
		log.error("ACTIVATE PIN not supported by Cypriotic EID");
	    throw new SignatureCardException("PIN activation not supported by this card.");
	}

	@Override
	public void unblockPIN(PinInfo pinInfo, ModifyPINGUI pukGUI)
			throws CancelledException, SignatureCardException,
			InterruptedException {
		CardChannel channel = getCardChannel();

		exec_unblockPIN(channel, pukGUI);
	}

	// //////////////////////////////////////////////////////////////////////
	// PROTECTED METHODS (assume exclusive card access)
	// //////////////////////////////////////////////////////////////////////

	protected void verifyPINLoop(CardChannel channel, PinInfo spec,
			PINGUI provider) throws InterruptedException, CardException,
			SignatureCardException {

		int retries = -1;
		do {
			retries = verifyPIN(channel, spec, provider, retries);
		} while (retries > 0);
	}

	protected int verifyPIN(CardChannel channel, PinInfo pinInfo,
			PINGUI provider, int retries) throws InterruptedException,
			CardException, SignatureCardException {

		char[] pin = provider.providePIN(pinInfo, pinInfo.retries);
		
		byte[] ascii_pin = encodePIN(pin);
		exec_selectADF(channel);

		ResponseAPDU resp = channel.transmit(new CommandAPDU(0x00, 0x20, 0x00, 
				pinInfo.getKID(), ascii_pin));

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
			String msg = "VERIFY failed. SW="
					+ Integer.toHexString(resp.getSW());
			log.info(msg);
			pinInfo.setUnknown();
			throw new SignatureCardException(msg);
		}

	}

	private byte[] encodePIN(char[] pin)
	{
		return  Charset.forName("ASCII").encode(
				CharBuffer.wrap(pin)).array();
	}
	
	protected void exec_unblockPIN(CardChannel channel, ModifyPINGUI changePINGUI) throws CancelledException, InterruptedException
	{
		char[] PUK = changePINGUI.provideCurrentPIN(pukPinInfo, pukPinInfo.retries);
		char[] newPIN = changePINGUI.provideNewPIN(pinPinInfo);
		
		byte[] ascii_puk = encodePIN(PUK);
		
		byte[] ascii_pin = encodePIN(newPIN);
		
		try {
			log.debug("PUK: " + new String(PUK) + "(" + getHexString(ascii_puk) + ") NEW PIN: " + 
			new String(newPIN) + "(" + getHexString(ascii_pin) + ")");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// TODO: INPUT checking PIN SIZES etc.
		/*
		try {
			exec_selectADF(channel);

			ResponseAPDU resp = channel.transmit(new CommandAPDU(0x00, 0x20, 0x00, 
					pukPinInfo.getKID(), ascii_puk));
			
			if (resp.getSW() == 0x9000) {
				pukPinInfo.setActive(pukPinInfo.maxRetries);
			}
			else
			{
				log.debug("WRONG PUK CODE!! SW=" + resp.getSW());
				return;
			}
			
			resp = channel.transmit(new CommandAPDU(0x00, 0x2C, 0x02, 
					pinPinInfo.getKID(), ascii_pin));
			
			if (resp.getSW() == 0x9000) {
				pinPinInfo.setActive(pinPinInfo.maxRetries);
			}
			else
			{
				log.debug("FAILED TO SET PIN! SW=" + resp.getSW());
			}
			
		} catch (CardException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SignatureCardException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
	}
	
	protected byte[] exec_readcert(CardChannel channel) throws CardException,
	SignatureCardException, IOException {
		if(cert_id == null)
		{
			exec_readcd(channel);
		}
		
		/*if(cert_id == null)
		{
			throw CardException("Failed to read the certificate id");
		}*/
		
		exec_selectADF(channel);
		exec_selectFILE(channel, cert_id);
		
		return exec_readBinary(channel);
	}
	
	protected void exec_readcd(CardChannel channel) throws CardException,
		SignatureCardException, IOException
	{
		exec_selectADF(channel);
		exec_selectFILE(channel, CD_ID);
		
		byte[] cd_buffer = exec_readBinary(channel);
		
		// TODO interpret CD => get CERT ID
		
		cert_id = new byte[] { (byte) 0x34, (byte) 0x01 };
	}
	
	protected void exec_selectADF(CardChannel channel) throws CardException,
			SignatureCardException {

		exec_selectFILE(channel, MF_ID);
		exec_selectFILE(channel, ADF_AWP_ID);

	}

	
	
	protected byte[] exec_selectFILE(CardChannel channel, byte[] file_id)
			throws CardException, SignatureCardException {
		ResponseAPDU resp = channel.transmit(new CommandAPDU(0x00, 0xA4, 0x01,
				0x0C, file_id));

		if (resp.getSW() != 0x9000) {
			String msg = "Failed to select File="
					+ SMCCHelper.toString(file_id) + " SW="
					+ Integer.toHexString(resp.getSW()) + ".";
			log.info(msg);
			throw new SignatureCardException(msg);
		}

		return resp.getBytes();
	}

	protected void exec_MSE(CardChannel channel, byte algoID) throws CardException
	{
		byte[] secure_setup = new byte[] { (byte) 0x80, (byte) 0x01, algoID, // Algorithm setup
				(byte) 0x84, (byte) 0x01, (byte) 0x81 }; // Key setup
		
		ResponseAPDU resp = channel.transmit(new CommandAPDU(0x00, 0x22,
				0x41, 0xB6, secure_setup));
	}
	
	protected byte[] exec_sign(CardChannel channel, byte[] hash) throws CardException
	{
		ResponseAPDU resp = channel.transmit(new CommandAPDU(0x00, 0x2A,
				0x9E, 0x9A, hash));
		
		return resp.getData();
	}

	protected byte[] exec_readBinary(CardChannel channel) throws CardException,
			IOException, SignatureCardException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		boolean repeat = true;

		int offset = 0;

		do {
			int offset_lo = offset % 0xFF;
			int offset_hi = offset / 0xFF;

			ResponseAPDU resp = channel.transmit(new CommandAPDU(0x00, 0xB0,
					offset_hi, offset_lo, 0x00));

			if (resp.getSW() != 0x9000) {
				String msg = "Failed to read binary SW="
						+ Integer.toHexString(resp.getSW()) + ".";
				log.info(msg);
				throw new SignatureCardException(msg);
			}

			byte[] data = resp.getData();

			buffer.write(data);

			repeat = data.length == 231;

			offset += data.length;
		} while (repeat);

		byte[] buf = buffer.toByteArray();
		
		log.debug("BINARY READ: ");
		
		try {
			log.debug(getHexString(buf));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return buf;
	}
	
	
	public static String getHexString(byte[] b) throws Exception {
		  String result = "";
		  for (int i=0; i < b.length; i++) {
		    result += ":" +
		          Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 );
		  }
		  return result;
		}
}
