package at.gv.egiz.bku.gui;

import java.applet.AppletContext;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author Thomas Zefferer <thomas.zefferer@iaik.tugraz.at>
 */
public class SwitchFocusListener implements ActionListener {

	protected final static Log log = LogFactory.getLog(SwitchFocusListener.class);
	
	protected AppletContext ctx;
	protected String javascriptFunction;
	
	  public SwitchFocusListener(AppletContext ctx, String javascriptFunction) {
		    
		    this.ctx = ctx;
		    this.javascriptFunction = javascriptFunction;
		  }
	
	@Override
	public void actionPerformed(ActionEvent e) {
		
	    try {
	        ctx.showDocument
	          (new URL("javascript:" + javascriptFunction));
	        }
	      catch (MalformedURLException me) {

	    	  log.warn("Unable to call external javascript function.", me);
	      }
		

	}

}
