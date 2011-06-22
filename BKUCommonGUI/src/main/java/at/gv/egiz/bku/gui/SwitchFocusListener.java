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

import java.applet.AppletContext;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Thomas Zefferer <thomas.zefferer@iaik.tugraz.at>
 */
public class SwitchFocusListener implements ActionListener {

	private final Logger log = LoggerFactory.getLogger(SwitchFocusListener.class);
	
	protected String functionName;
	protected AppletContext ctx;
	protected String javascriptFunction;
	
	  public SwitchFocusListener(AppletContext ctx, String javascriptFunctionName) {
		    
		    this.ctx = ctx;
		    this.functionName = javascriptFunctionName;
		    buildJSFunction();
		  }
	
	@Override
	public void actionPerformed(ActionEvent e) {
		
		log.debug("SwitchFocusListener fires!");
		
	    try {
	        ctx.showDocument
	          (new URL("javascript:" + javascriptFunction));
	        }
	      catch (MalformedURLException me) { 
	    	  
	    	  log.warn("Unable to call external javascript function.", me);
	      }
		

	}

	protected void buildJSFunction() {
		
		this.javascriptFunction =  functionName + "()";
		
	}
	
}
