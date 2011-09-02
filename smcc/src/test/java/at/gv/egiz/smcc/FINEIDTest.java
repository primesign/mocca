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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Locale;

import org.junit.Ignore;

import at.gv.egiz.smcc.pin.gui.PINGUI;
import at.gv.egiz.smcc.util.ISO7816Utils;
import at.gv.egiz.smcc.util.SMCCHelper;
import at.gv.egiz.smcc.util.TLVSequence;

import javax.smartcardio.*;

@Ignore
public class FINEIDTest extends AbstractSignatureCard {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		FINEIDTest tester = new FINEIDTest();
		tester.runTest();

	}

	public void runTest() {

		SMCCHelper helper = new SMCCHelper();

		SignatureCard signatureCard = helper.getSignatureCard(Locale
				.getDefault());

		System.out.println("Found card: " + signatureCard.toString());

		// TODO: replace this by already implemented getCardChannel() method
		CardChannel channel = new T0CardChannel(signatureCard.getCard()
				.getBasicChannel());

		try {

			selectAID(channel);
//			readCardInfo(channel);
			testPIN();

		} catch (Exception e) {

			e.printStackTrace();
		}

	}

	public void testPIN() {
		
//		PinInfo pinInfo = new PinInfo(6, 8, "[0-9]",
//				"at/gv/egiz/smcc/FINEIDCard", "sig.pin", (byte) 0x00,
//				new byte[] {}, PinInfo.UNKNOWN_RETRIES);
		
		VerifyAPDUSpec apduSpec = new VerifyAPDUSpec(new byte[] { (byte) 0x00,
				(byte) 0x20, (byte) 0x00, (byte)0x82 }, 0,
				VerifyAPDUSpec.PIN_FORMAT_ASCII, 8);
		
		CommandAPDU apdu = ISO7816Utils.createVerifyAPDU(apduSpec, new char[]{'1','2','3','4','5','6'});
		
		System.out.println("APDU: " + SMCCHelper.toString(apdu.getBytes()));
		
	}
	
	public void selectAID(CardChannel channel) throws CardException {

		byte[] aid = new byte[] { (byte) 0xA0, (byte) 0x00, (byte) 0x00,
				(byte) 0x00, (byte) 0x63, (byte) 0x50, (byte) 0x4B,
				(byte) 0x43, (byte) 0x53, (byte) 0x2D, (byte) 0x31, (byte) 0x35 };

		CommandAPDU apdu = new CommandAPDU((byte) 0x00, (byte) 0xA4,
				(byte) 0x04, (byte) 0x00, aid);

		ResponseAPDU resp = channel.transmit(apdu);

		System.out.println("Response: " + SMCCHelper.toString(resp.getBytes()));

	}

	public void readCardInfo(CardChannel channel) throws CardException,
			SignatureCardException, IOException {

		byte[] efQcert = null;

		FINEIDEFObjectDirectory ef_od = new FINEIDEFObjectDirectory(0xFF);
		ef_od.selectAndRead(channel);

		// **** READ CERT ****

		for (int i = 0; i < ef_od.getCDReferences().size(); i++) {

			FINEIDCIOCertificateDirectory ef_cd = new FINEIDCIOCertificateDirectory(
					ef_od.getCDReferences().get(i));

			try {
				ef_cd.selectAndRead(channel);
			} catch (IOException e) {
				System.out
						.println("Error reading EF.CD - try next if available.");
				e.printStackTrace();
				continue;
			}

			for (CIOCertificate cioCertificate : ef_cd.getCIOs()) {
				String label = cioCertificate.getLabel();
				if (label != null
						&& label.toLowerCase().contains(
								"allekirjoitusvarmenne".toLowerCase())) {
					efQcert = cioCertificate.getEfidOrPath();
				}
			}
		}

		System.out.println("Read certificate path: "
				+ SMCCHelper.toString(efQcert));

		byte[] certPath = null;
		// remove MF path
		if (efQcert[0] == 0x3F && efQcert[1] == 0x00) {

			certPath = new byte[efQcert.length - 2];
			System.arraycopy(efQcert, 2, certPath, 0, efQcert.length - 2);
		} else {

			certPath = efQcert;
		}

		CommandAPDU apdu = new CommandAPDU((byte) 0x00, (byte) 0xA4,
				(byte) 0x08, (byte) 0x00, certPath);
		ResponseAPDU resp = channel.transmit(apdu);

		System.out.println("Response: " + SMCCHelper.toString(resp.getBytes()));

		byte[] fcx = new TLVSequence(resp.getBytes())
				.getValue(ISO7816Utils.TAG_FCI);
		byte[] fileDataLength = new TLVSequence(fcx).getValue(0x81);

		System.out.println("Data length: "
				+ SMCCHelper.toString(fileDataLength));

		System.out.println("MaxSize: "
				+ computeLengthFromByteArray(fileDataLength));

		byte[] cert = ISO7816Utils.readTransparentFile(channel,
				computeLengthFromByteArray(fileDataLength));

		System.out.println("Read cert: " + SMCCHelper.toString(cert));

		toFile(cert, "F:/fin_cert.cer");

		// **** VERIFY PIN ****

		byte[] prkdPath = ef_od.getPrKDReferences().get(0);
		System.out.println("PRKD path: " + SMCCHelper.toString(prkdPath));

		FINEIDCIOKeyDirectory ef_prkd = new FINEIDCIOKeyDirectory(ef_od
				.getPrKDReferences().get(0));
		ef_prkd.selectAndRead(channel);

		byte[] efKey = null;
		byte[] authID = null;
		for (CIOCertificate cioCertificate : ef_prkd.getCIOs()) {
			String label = cioCertificate.getLabel();
			if (label != null
					&& label.toLowerCase().contains(
							"allekirjoitusavain".toLowerCase())) {
				efKey = cioCertificate.getEfidOrPath();
				System.out.println("AUTH ID of this key: "
						+ SMCCHelper.toString(cioCertificate.getAuthId()));
				authID = cioCertificate.getAuthId();
			}
		}

		System.out.println("Key path: " + SMCCHelper.toString(efKey));

		byte[] aod = ef_od.getAODReferences().get(0);
		System.out.println("AOD path: " + SMCCHelper.toString(aod));

		FINEIDAODirectory ef_aod = new FINEIDAODirectory(ef_od.getAODReferences().get(0));
		ef_aod.selectAndRead(channel);

		byte[] pinPath = null;
		byte[] pwdRef = null;
		for (FINEIDAuthenticationObject ao : ef_aod.getAOs()) {

			byte[] id = ao.getAuthId();
			if (id != null && Arrays.equals(id, authID)) {
				pinPath = ao.getPath();
				pwdRef = ao.getPwdReference();
			}
		}

		System.out.println("PIN path: " + SMCCHelper.toString(pinPath));
		System.out.println("PWD Ref: " + SMCCHelper.toString(pwdRef));

		CommandAPDU verifySelect = new CommandAPDU((byte) 0x00, (byte) 0xA4,
				(byte) 0x08, (byte) 0x00, removeMFFromPath(pinPath));
		ResponseAPDU r1 = channel.transmit(verifySelect);

		if (r1.getSW() != 0x9000) {

			System.out.println("Error executing Verify Select: "
					+ Integer.toHexString(r1.getSW()));
		}

		CommandAPDU verify = new CommandAPDU((byte) 0x00, (byte) 0x20,
				(byte) 0x00, pwdRef[pwdRef.length - 1], new byte[] {
						(byte) 0x36, (byte) 0x35, (byte) 0x38, (byte) 0x30,
						(byte) 0x36, (byte) 0x36, (byte) 0x00, (byte) 0x00 });
		ResponseAPDU r2 = channel.transmit(verify);

		if (r2.getSW() != 0x9000) {

			System.err.println("Error executing Verify: "
					+ Integer.toHexString(r2.getSW()));
		}

		// **** SIGN ****

		CommandAPDU selectKeyPath = new CommandAPDU((byte) 0x00, (byte) 0xA4,
				(byte) 0x08, (byte) 0x00, removeMFFromPath(efKey));
		ResponseAPDU r3 = channel.transmit(selectKeyPath);

		if (r3.getSW() != 0x9000) {

			System.err.println("Error executing select keypath: "
					+ Integer.toHexString(r3.getSW()));
		}

		// MSE RESTORE
		CommandAPDU mseRestore = new CommandAPDU((byte) 0x00, (byte) 0x22,
				(byte) 0xF3, (byte) 0x00);
		ResponseAPDU r4 = channel.transmit(mseRestore);

		if (r4.getSW() != 0x9000) {

			System.err.println("Error executing restore mse: "
					+ Integer.toHexString(r4.getSW()));
		}

		// MSE SET

		byte[] dst = new byte[] { (byte) 0x80, (byte) 0x01, (byte) 0x12,
				(byte) 0x81, (byte) 0x02, efKey[efKey.length - 2],
				efKey[efKey.length - 1] };

		CommandAPDU mseSet = new CommandAPDU((byte) 0x00, (byte) 0x22,
				(byte) 0x41, (byte) 0xB6, dst);
		ResponseAPDU r5 = channel.transmit(mseSet);

		if (r5.getSW() != 0x9000) {

			System.err.println("Error executing set mse: "
					+ Integer.toHexString(r5.getSW()));
		}

		// SIGN

		byte[] hash = new byte[] { (byte) 0x00, (byte) 0x01, (byte) 0x02,
				(byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06,
				(byte) 0x07, (byte) 0x08, (byte) 0x09, (byte) 0x0A,
				(byte) 0x0B, (byte) 0x0C, (byte) 0x0D, (byte) 0x0E,
				(byte) 0x0F, (byte) 0x10, (byte) 0x11, (byte) 0x12, (byte) 0x13 };

		CommandAPDU sign = new CommandAPDU((byte) 0x00, (byte) 0x2A,
				(byte) 0x9E, (byte) 0x9A, hash);
		ResponseAPDU r6 = channel.transmit(sign);

		if (r6.getSW() != 0x9000) {

			System.err.println("Error executing sign: "
					+ Integer.toHexString(r6.getSW()));
		}
		
		System.out.println("Signature value: " + SMCCHelper.toString(r6.getData()));
		
	}

	@Override
	public byte[] createSignature(InputStream input, KeyboxName keyboxName,
			PINGUI pinGUI, String alg) throws SignatureCardException,
			InterruptedException, IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte[] getCertificate(KeyboxName keyboxName, PINGUI pinGUI)
			throws SignatureCardException, InterruptedException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte[] getInfobox(String infobox, PINGUI pinGUI, String domainId)
			throws SignatureCardException, InterruptedException {
		// TODO Auto-generated method stub
		return null;
	}

	private int computeLengthFromByteArray(byte[] input) {

		int result = 0;

		for (int i = 0; i < input.length; i++) {

			int current = input[input.length - 1 - i];

			result = result + (int) (current * Math.pow(256, i));
		}

		return result;

	}

	private void toFile(byte[] data, String filename) {

		try {
			FileOutputStream fos = new FileOutputStream(filename);

			fos.write(data);

			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private byte[] removeMFFromPath(byte[] path) {

		byte[] result = null;

		if (path[0] == 0x3F && path[1] == 0x00) {

			result = new byte[path.length - 2];
			System.arraycopy(path, 2, result, 0, path.length - 2);
		} else {

			result = path;
		}

		return result;
	}

}
