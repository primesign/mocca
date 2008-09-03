package at.gv.egiz.bku.accesscontroller;

/**
 * Result of the access controller
 * 
 */
public class ChainResult {
	private UserAction userAction;
	private Action action;
	private boolean matchFound;

	public ChainResult(Action action, UserAction userAction, boolean matchFound) {
		this.action = action;
		this.userAction = userAction;
		this.matchFound = matchFound;
	}
	
	public Action getAction() {
		return action;
	}

	public UserAction getUserAction() {
		return userAction;
	}
	
	/**
	 * 
	 * @return true if a matching rule has been found
	 */
	public boolean matchFound() {
		return matchFound;
	}
}
