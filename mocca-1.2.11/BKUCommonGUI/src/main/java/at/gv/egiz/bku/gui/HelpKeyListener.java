package at.gv.egiz.bku.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author Thomas Zefferer <thomas.zefferer@iaik.tugraz.at>
 */
public class HelpKeyListener extends KeyAdapter {

	  protected static final Log log = LogFactory.getLog(HelpKeyListener.class);
	  
	  protected ActionListener helpListener;
	  protected String locale;
	  protected String topic;

	  public HelpKeyListener(ActionListener externalHelpListener) {
	    super();
	    this.helpListener = externalHelpListener;
	  }

	  public void setHelpTopic(String topic) {
	    log.trace("setting help topic: " + topic);
	    this.topic = topic;
	  }
	  
	  public ActionListener getActionListener() {
	    return helpListener;
	  }

	  @Override
	  public void keyPressed(KeyEvent arg0) {
		  
		  if(arg0.getKeyCode() == KeyEvent.VK_ENTER) { 
			  ActionEvent e = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, topic);
			  helpListener.actionPerformed(e);
		  }
	  }
	
}
