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

package at.gv.egiz.bku.smccstal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.gv.egiz.bku.gui.GetHardwareInfoGUIFacade;
import at.gv.egiz.stal.STALRequest;
import at.gv.egiz.stal.STALResponse;
import at.gv.egiz.stal.ext.GetHardwareInfoRequest;
import at.gv.egiz.stal.ext.GetHardwareInfoResponse;

/**
*
* @author Thomas Lenz <thomas.lenz@iaik.tugraz.at>
*/

public class GetHardwareInfoRequestHandler extends AbstractRequestHandler {

	private final Logger log = LoggerFactory.getLogger(GetHardwareInfoRequestHandler.class);
	
	@Override
	public boolean requireCard() {
		return true;
	}

	@Override
	public STALResponse handleRequest(STALRequest request)
			throws InterruptedException {
		
		log.debug("handle a get-hardware info request");
		
		if (request instanceof GetHardwareInfoRequest) {
			
			GetHardwareInfoGUIFacade gui = (GetHardwareInfoGUIFacade) this.gui;
			
			String terminal = card.getTerminalName();
			String smartcard = card.toString();
			String smartcard_ATR = toString(card.getCard().getATR().getBytes());
		 	
			gui.showHardwareInfoDialog(this, "back", terminal, smartcard, smartcard_ATR);
								
			while (true) {
			
				waitForAction();
				
				 if ("back".equals(actionCommand)) {
			          log.debug("show hardware info response back.");
			          return new GetHardwareInfoResponse();
			          
				 } else {
					 log.info("unknown command resolved.");
				 }
			}
					
		}
		return new GetHardwareInfoResponse();
	}
	
	  private static String toString(byte[] b) {
		    StringBuffer sb = new StringBuffer();
		    sb.append('[');
		    if (b != null && b.length > 0) {
		      sb.append(Integer.toHexString((b[0] & 240) >> 4));
		      sb.append(Integer.toHexString(b[0] & 15));
		      for (int i = 1; i < b.length; i++) {
		        sb.append((i % 32 == 0) ? '\n' : ':');
		        sb.append(Integer.toHexString((b[i] & 240) >> 4));
		        sb.append(Integer.toHexString(b[i] & 15));
		      }
		    }
		    sb.append(']');
		    return sb.toString();
		  }

}
