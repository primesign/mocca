package at.gv.egiz.bku.accesscontroller;


public class RuleResult extends ChainResult {
	private String chainId;
	
	public RuleResult(Action action, UserAction userAction, boolean matchFound, String chainId) {
		super(action, userAction, matchFound);
		this.chainId = chainId;
	}

	public String getDelegateChainId() {
		return chainId;
	}

}
