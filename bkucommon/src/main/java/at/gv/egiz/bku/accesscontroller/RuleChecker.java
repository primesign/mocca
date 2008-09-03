package at.gv.egiz.bku.accesscontroller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import at.gv.egiz.bku.slexceptions.SLRuntimeException;

public class RuleChecker implements AccessChecker {
	
	private static Log log = LogFactory.getLog(RuleChecker.class);
	
	public static enum PEER_TYPE {HOST, IP, URL};

	protected String id;
	protected AuthenticationClass authenticationClass;
	protected String commandName;
	protected String peerId;
	protected PEER_TYPE peerType;
	protected Action action;
	protected UserAction userAction;

	public RuleChecker(String id) {
		if (id == null) {
			throw new NullPointerException("Id argument must not be null");
		}
		this.id = id;
	}

	public void setAuthenticationClass(String ac) {
		AuthenticationClass tmp = AuthenticationClass.fromString(ac); 
		if (tmp == null) {
			throw new SLRuntimeException("Unknown authentication class "+ac);
		}
		authenticationClass = tmp;
	}
	
	public void setAction(String ac) {
		Action tmp = Action.fromString(ac);
		if (tmp == null) {
			throw new SLRuntimeException("Unknown action "+ac);
		}
		action = tmp;
	}
	
	public void setUserAction(String uac) {
		 UserAction tmp = UserAction.fromString(uac);
		if (tmp == null) {
			throw new SLRuntimeException("Unknown user action "+uac);
		}
		userAction = tmp;
	}
	
	public void setPeerId(String peerId, PEER_TYPE type) {
		this.peerType = type;
		this.peerId = peerId;
	}
	
	public String getId() {
		return id;
	}

	@Override
	public RuleResult check(AccessCheckerContext checkCtx) {
		log.debug("Processing rule: "+id);
		// TODO Auto-generated method stub
		return null;
	}

}
