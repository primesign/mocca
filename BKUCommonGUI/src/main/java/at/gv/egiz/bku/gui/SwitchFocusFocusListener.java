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
