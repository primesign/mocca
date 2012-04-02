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


package at.gv.egiz.bku.gui;

import java.awt.event.ActionListener;

/**
 * 
 * @author Andreas Fitzek <andreas.fitzek@iaik.tugraz.at>
 */
public interface IdentityLinkGUIFacade extends BKUGUIFacade {

	public static final String HELP_IDENTITYLINK = "help.identity.link";
	
	public static final String FIRSTNAME = "identity.firstname";
	public static final String DATEOFBIRTH = "identity.dateofbirth";
	public static final String LASTNAME = "identity.lastname";
	public static final String TITLE_IDENITY = "title.identity";
	
	public static final String MESSAGE_IDENITY = "identity.msg";
	
	public static final String ERR_INFOBOX_INVALID = "err.infobox.invalid";
	
	/**
	 * Currently dummy method to display anything ...
	 * 
	 * @param firstName
	 * @param surName
	 */
	public void showIdentityLinkInformationDialog(
			ActionListener activateListener,
			String actionCommand,
			String firstName, 
			String surName,
			String birthdate);
}
