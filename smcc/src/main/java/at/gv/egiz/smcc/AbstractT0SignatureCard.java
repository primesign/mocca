package at.gv.egiz.smcc;

import javax.smartcardio.CardChannel;

public abstract class AbstractT0SignatureCard extends AbstractSignatureCard
		implements SignatureCard {

	
	  protected CardChannel getCardChannel() {
		
		  return new T0CardChannel(getCard().getBasicChannel());
	  }	
	

	  
}
