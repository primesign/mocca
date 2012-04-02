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
import java.io.File;

/**
*
* @author Thomas Lenz <thomas.lenz@iaik.tugraz.at>
*/

public interface GetCertificateGUIFacade extends BKUGUIFacade {

	 public static final String HELP_GETCERT = "help.get.certificate";
	
	 public static final String LABEL_SIM_CERT = "label.simple.certificate";
	 public static final String LABEL_QUAL_CERT = "label.qualified.certificate";
	 public static final String BUTTON_SAVE_AS = "button.certificate.saveas"; 
	 public static final String FILE_TYPE_NAME = "file.certificate"; 
	 public static final String TITEL_FILESAVE = "title.certificate.save";
	 public static final String TITLE_GETCERTIFICATE = "title.get.certificate";
	 
	 public static final String FILENAME_QUAL_CERT = "qualified.cer";
	 public static final String FILENAME_SIM_CERT =  "simple.cer";
	 
	
	public void showGetCertificateDialog(ActionListener certificateListener, String showGetQualCert, 
			String showGetSimCert, ActionListener cancelListener, String cancelCmd);
	
	public File showSaveDialog(String defaultfilename);
	
}
