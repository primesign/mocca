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

import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

import at.gv.egiz.smcc.util.ISO7816Utils;
import at.gv.egiz.smcc.util.TLVSequence;

public class FINEIDCIOCertificateDirectory extends CIOCertificateDirectory {

	public FINEIDCIOCertificateDirectory(byte[] fid) {
		
		super(fid);
		this.fid = FINEIDUtil.removeMFPath(fid);
	}
	
	@Override
    protected byte[] executeSelect(CardChannel channel) throws CardException {
    	  
          CommandAPDU cmd = new CommandAPDU(0x00, 0xA4, 0x08, ISO7816Utils.P2_FCI, fid, 256);
          ResponseAPDU resp = channel.transmit(cmd);

          byte[] fcx = new TLVSequence(resp.getBytes()).getValue(ISO7816Utils.TAG_FCI);
          byte[] fd = new TLVSequence(fcx).getValue(0x82);
          
          return fd;
      }
}
