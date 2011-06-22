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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Thomas Zefferer <thomas.zefferer@iaik.tugraz.at>
 */
public class SwitchFocusFocusListener extends FocusAdapter {

	  private final Logger log = LoggerFactory.getLogger(SwitchFocusFocusListener.class);
	  
	  protected ActionListener swichFocusListener;

	  public SwitchFocusFocusListener(ActionListener externalSwitchFocusListener) {
	    super();
	    this.swichFocusListener = externalSwitchFocusListener;
	  }
	  
	  public ActionListener getActionListener() {
	    return swichFocusListener;
	  }
	  
	  @Override
	  public void focusGained(FocusEvent arg0) {
		  
		  log.debug("SwitchFocusFocusListener detected focusGained event!");
		  Component comp = arg0.getComponent();
		  log.debug("Component that caused event: {}.", comp.getName());	
		  comp.transferFocus();
		  
		  ActionEvent e = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null);
		  swichFocusListener.actionPerformed(e);
	  }
	
	
}
