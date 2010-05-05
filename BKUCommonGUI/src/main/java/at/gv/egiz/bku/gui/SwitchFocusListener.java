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
