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
import at.gv.egiz.smcc.util.SMCCHelper;
import at.gv.egiz.smcc.util.TLVSequence;

public class DNIeCIOCertificateDirectory extends CIOCertificateDirectory {

	protected static final boolean RETRIEVE_AUTH_ID_FROM_ASN1 = Boolean.FALSE;
	
	public DNIeCIOCertificateDirectory(byte[] fid) {
		
		super(fid);		
	}

	@Override
    public void selectAndRead(CardChannel channel) throws CardException, SignatureCardException, IOException {
      
        CommandAPDU cmd = new CommandAPDU(0x00, 0xA4, 0x00, 0x00, fid, 256);        
        ResponseAPDU resp = channel.transmit(cmd);

        if(resp.getSW() != 0x9000) {
        	
        	throw new CardException("Error selecting DNIeCIOCertificateDeirectory: " + Integer.toHexString(resp.getSW()));
        }
        
        byte[] fcx = new TLVSequence(resp.getBytes()).getValue(ISO7816Utils.TAG_FCI);
        byte[] fd = new TLVSequence(fcx).getValue(0x85); // proprietary
        
        if(fd.length < 5 ||
           fd[0] != (byte)0x01 ||
           fd[1] != fid[0] ||
           fd[2] != fid[1]) {
        	
        	throw new CardException("Error reading CDF - invalid FCI: " + SMCCHelper.toString(resp.getData()));
        }
        
        readCIOCertificatesFromTransparentFile(channel);
    }
    
    protected byte[] doReadTransparentFile(CardChannel channel) throws CardException, SignatureCardException {
    	
    	return ISO7816Utils.readTransparentFile(channel, -1, 0xef);
    }
	
	protected boolean retrieveAuthIdFromASN1() {
		
		return RETRIEVE_AUTH_ID_FROM_ASN1;
	}
    
}
