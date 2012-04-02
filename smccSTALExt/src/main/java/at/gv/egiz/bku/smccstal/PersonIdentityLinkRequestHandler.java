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

import iaik.me.asn1.ASN1;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.gv.egiz.bku.gui.PINManagementGUIFacade;
import at.gv.egiz.bku.gui.PersonIdentityLinkGUIFacade;
import at.gv.egiz.bku.pin.gui.VerifyPINGUI;
import at.gv.egiz.smcc.SignatureCardException;
import at.gv.egiz.stal.ErrorResponse;
import at.gv.egiz.stal.STALRequest;
import at.gv.egiz.stal.STALResponse;
import at.gv.egiz.stal.ext.PersonIdentityLinkRequest;
import at.gv.egiz.stal.ext.PersonIdentityLinkResponse;

/**
 * 
 * @author Andreas Fitzek <andreas.fitzek@iaik.tugraz.at>
 */
public class PersonIdentityLinkRequestHandler extends AbstractRequestHandler {

	private final static String IdentityInfoBox = "IdentityLink";
	
	private final Logger log = LoggerFactory.getLogger(PersonIdentityLinkRequestHandler.class);

	//PersonIdentityLinkGUIFacade person_gui = (PersonIdentityLinkGUIFacade) this.gui;
	
	@Override
	public boolean requireCard() {
		return true;
	}

	@Override
	public STALResponse handleRequest(STALRequest request)
			throws InterruptedException {
		if(request instanceof PersonIdentityLinkRequest)
		{
			try
			{
				byte[] identity_asn1 = card.getInfobox(IdentityInfoBox, new VerifyPINGUI(gui), "");
				
				ASN1 identity_object = new ASN1(identity_asn1);
				
				String firstname = IdentityLinkExtractor.getPersonFirstName(identity_object);
				String lastname = IdentityLinkExtractor.getPersonLastName(identity_object);
				String dateofBirth = IdentityLinkExtractor.getPersonDateOfBirth(identity_object);
				
				// TODO: correct error handling ...
				PersonIdentityLinkGUIFacade person_gui = null;
				
				if(gui instanceof PersonIdentityLinkGUIFacade)
				{
					person_gui = (PersonIdentityLinkGUIFacade) gui;
				}
				
				if(person_gui == null)
				{
					log.warn("Failed to cast gui to PersonIdentityLinkGUIFacade!");
					return new ErrorResponse(1000);
				}
				
				person_gui.showPersonIdentityLinkInformationDialog(this, "ok_action", 
						firstname, 
						lastname,
						dateofBirth);
				
				waitForAction();
				
				return new PersonIdentityLinkResponse();
			}
			catch(SignatureCardException ex)
			{
				log.error(ex.getMessage(), ex);
				gui.showErrorDialog(PINManagementGUIFacade.ERR_CARD_NOTACTIVATED,
			              null, this, "cancel");
				waitForAction();
				return new ErrorResponse(1000);
			} catch (IOException ex) {
				log.error(ex.getMessage(), ex);
				gui.showErrorDialog(PersonIdentityLinkGUIFacade.ERR_INFOBOX_INVALID,
			              null, this, "cancel");
				waitForAction();
				return new ErrorResponse(1000);
			}
		}
		else
		{
			log.error("Got unexpected STAL request: {}.", request);
			return new ErrorResponse(1000);
		}
	}
}
