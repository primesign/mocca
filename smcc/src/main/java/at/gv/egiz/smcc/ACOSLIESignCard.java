package at.gv.egiz.smcc;

import at.gv.egiz.smcc.pin.gui.PINGUI;

public class ACOSLIESignCard extends AbstractACOSCard {

	  @Override
	  @Exclusive
	  public byte[] getInfobox(String infobox, PINGUI provider, String domainId)
	      throws SignatureCardException, InterruptedException {
	    
			throw new IllegalArgumentException("Infobox '" + infobox
					+ "' not supported.");
	  
	  }
	
}
