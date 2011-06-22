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

import java.io.IOException;

import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

import at.gv.egiz.smcc.util.ISO7816Utils;
import at.gv.egiz.smcc.util.TLVSequence;

public class LtEIDCIOCertificateDirectory extends CIOCertificateDirectory {

	public LtEIDCIOCertificateDirectory(byte[] fid) {
		super(fid);
	}

    protected void addCIOCertificate(byte[] cio) throws IOException {

        LtEIDCIOCertificate cioCert = new LtEIDCIOCertificate(cio);

        log.debug("adding {}", cioCert);
        cios.add(cioCert);
    }	
	
	@Override
	protected byte[] executeSelect(CardChannel channel)
			throws CardException {

		byte[] finalPath = null;

		if (fid != null && fid.length > 2 && fid[0] == (byte) 0x3F
				&& fid[1] == (byte) 0x00) {

			// cut off MF identifier
			finalPath = new byte[fid.length - 2];
			System.arraycopy(fid, 2, finalPath, 0, fid.length - 2);
		} else {
			finalPath = fid;
		}

		CommandAPDU apdu = new CommandAPDU((byte) 0x00, (byte) 0xA4,
				(byte) 0x08, ISO7816Utils.P2_FCP, finalPath);

		ResponseAPDU resp = channel.transmit(apdu);

		if (resp.getSW() != 0x9000) {

			throw new CardException(
					"Error selecting File - unexpected response from card: "
							+ Integer.toHexString(resp.getSW()));
		}
		
        byte[] fcx = new TLVSequence(resp.getBytes()).getValue(ISO7816Utils.TAG_FCP);
        byte[] fd = new TLVSequence(fcx).getValue(0x82);
        
        return fd;
	}

	@Override
    protected byte[] doReadTransparentFile(CardChannel channel) throws CardException, SignatureCardException {
    	
    	return ISO7816Utils.readTransparentFile(channel, 0xEE);
    }		
}
