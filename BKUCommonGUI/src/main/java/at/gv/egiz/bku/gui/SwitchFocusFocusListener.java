package at.gv.egiz.bku.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author Thomas Zefferer <thomas.zefferer@iaik.tugraz.at>
 */
public class SwitchFocusFocusListener extends FocusAdapter {

	  protected static final Log log = LogFactory.getLog(SwitchFocusFocusListener.class);
	  
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
		  
		  ActionEvent e = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null);
		  swichFocusListener.actionPerformed(e);
	  }
	
	
}
