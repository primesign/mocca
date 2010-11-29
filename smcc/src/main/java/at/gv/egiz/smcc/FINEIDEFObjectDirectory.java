/*
 * Copyright 2008 Federal Chancellery Austria and
 * Graz University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package at.gv.egiz.smcc;

import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

public class FINEIDEFObjectDirectory extends EFObjectDirectory {

	public FINEIDEFObjectDirectory(int padding) {

		super(padding);
	}	
	
	@Override
	protected void executeSelect(CardChannel channel)
			throws SignatureCardException, CardException {		
		
		CommandAPDU cmd = new CommandAPDU(0x00, 0xA4, 0x00, 0x00, fid, 256);
		ResponseAPDU resp = channel.transmit(cmd);

		if (resp.getSW() != 0x9000) {
			throw new SignatureCardException("SELECT EF.OD failed: SW=0x"
					+ Integer.toHexString(resp.getSW()));
		}
	}
}
