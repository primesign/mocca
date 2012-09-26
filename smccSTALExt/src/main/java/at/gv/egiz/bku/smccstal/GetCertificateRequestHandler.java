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

/**
*
* @author Thomas Lenz <thomas.lenz@iaik.tugraz.at>
*/

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.gv.egiz.bku.gui.GetCertificateGUIFacade;
import at.gv.egiz.bku.pin.gui.VerifyPINGUI;
import at.gv.egiz.smcc.SignatureCard;
import at.gv.egiz.smcc.SignatureCardException;
import at.gv.egiz.stal.STALRequest;
import at.gv.egiz.stal.STALResponse;
import at.gv.egiz.stal.ext.GetCertificateRequest;
import at.gv.egiz.stal.ext.GetCertificateResponse;

public class GetCertificateRequestHandler extends AbstractRequestHandler {

	private final Logger log = LoggerFactory.getLogger(GetCertificateRequestHandler.class);
	
	@Override
	public boolean requireCard() {
		return true;
	}

	@Override
	public STALResponse handleRequest(STALRequest request)
			throws InterruptedException {
		
		log.debug("handle a GetCertificateRequest");
		
		if (request instanceof GetCertificateRequest) {
			
			GetCertificateGUIFacade gui = (GetCertificateGUIFacade) this.gui;
			
			while (true) {
			
				gui.showGetCertificateDialog(this, "getqualcert", "getsimcert", this, "cancel");
				
				waitForAction();
				
				try {
					
					 if ("cancel".equals(actionCommand)) {
				          log.debug("get certificate response cancel.");
				          return new GetCertificateResponse();
				          
				     } else if ("getqualcert".equals(actionCommand)) {
				        	
			        	File file = gui.showSaveDialog(GetCertificateGUIFacade.FILENAME_QUAL_CERT);
			        	byte[] cert = card.getCertificate(SignatureCard.KeyboxName.SECURE_SIGNATURE_KEYPAIR, 
			        			new VerifyPINGUI(gui));	        		
			        	
			        	FileOutputStream fstream = new FileOutputStream(file);
			        	fstream.write(cert);
			        	fstream.close();
			        	
			        	log.debug("qualified certificate saved to " + file.getAbsolutePath() + ".");
			        					        			        	
				     } else if ("getsimcert".equals(actionCommand)) {
							
			        	File file = gui.showSaveDialog(GetCertificateGUIFacade.FILENAME_SIM_CERT);
			        
			        	FileOutputStream fstream = new FileOutputStream(file);
			        	fstream.write(card.getCertificate(SignatureCard.KeyboxName.CERTIFIED_KEYPAIR, 
			        			new VerifyPINGUI(gui)));
			        	fstream.close();
			        
			        	log.debug("simple certificate saved to " + file.getAbsolutePath() + ".");
			        										
					}
				    else {
				       	log.info("unknown command resolved.");
				    }
				 
	        	} catch (FileNotFoundException e) {
	        		log.error("file to save the certificate to could not be found.", e);
	        		
	        	} catch (SignatureCardException e) {
	        		log.error("Card not activated or certificate is not available.", e);
	                gui.showErrorDialog(GetCertificateGUIFacade.ERR_CARD_NOTACTIVATED,
	                    null, this, "cancel");
	        		
	        	} catch (NullPointerException e) {
	        		log.error("save certificate file selection aborted.", e);
	        		
	        	} catch (Exception e) {
	        		log.error("a general error occur during the certificate save operation.", e);
	        	}
			}
		}
		
		return new GetCertificateResponse();
	}
	
}
