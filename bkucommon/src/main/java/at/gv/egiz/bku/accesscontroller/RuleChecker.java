package at.gv.egiz.bku.accesscontroller;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import at.gv.egiz.bku.slcommands.SLCommand;
import at.gv.egiz.bku.slexceptions.SLRuntimeException;

public class RuleChecker implements AccessChecker {

	private static Log log = LogFactory.getLog(RuleChecker.class);

	public static enum PEER_TYPE {
		HOST, IP, URL
	};

	protected String id;
	protected AuthenticationClass authenticationClass;
	protected String commandName;
	protected Pattern commandNamePattern;
	protected String peerId;
	protected Pattern peerIdPattern;
	protected PEER_TYPE peerType;
	protected Action action;
	protected UserAction userAction;
	protected String chainId;

	public RuleChecker(String id) {
		if (id == null) {
			throw new NullPointerException("Id argument must not be null");
		}
		this.id = id;
	}

	public void setAuthenticationClass(String ac) {
		AuthenticationClass tmp = AuthenticationClass.fromString(ac);
		if (tmp == null) {
			throw new SLRuntimeException("Unknown authentication class " + ac);
		}
		authenticationClass = tmp;
	}

	public void setAction(String ac) {
		Action tmp = Action.fromString(ac);
		if (tmp == null) {
			throw new SLRuntimeException("Unknown action " + ac);
		}
		action = tmp;
	}

	public void setUserAction(String uac) {
		UserAction tmp = UserAction.fromString(uac);
		if (tmp == null) {
			throw new SLRuntimeException("Unknown user action " + uac);
		}
		userAction = tmp;
	}

	public void setChainId(String chainId) {
		this.chainId = chainId;
	}

	public void setPeerId(String peerId, PEER_TYPE type) {
		this.peerType = type;
		this.peerId = peerId;
		peerIdPattern = Pattern.compile(peerId);
	}

	public void setCommandName(String commandName) {
		this.commandName = commandName;
		commandNamePattern = Pattern.compile(commandName);
	}

	public String getId() {
		return id;
	}

	protected boolean matchAuthenticationClass(AuthenticationClass cls) {
		if (this.authenticationClass == null) {
			return true;
		}
		return this.authenticationClass.compareTo(cls) <= 0;
	}

	protected boolean matchCommandName(SLCommand cmd) {
		if (commandName == null) {
			return true;
		}
		Matcher matcher = commandNamePattern.matcher(cmd.getName());
		return matcher.matches();
	}

	protected boolean matchPeerId(String peerUrl) {
		if (peerId == null) {
			return true;
		}
		if (peerType == PEER_TYPE.URL) {
			Matcher matcher = peerIdPattern.matcher(peerUrl);
			return matcher.matches();
		} else {
			try {
				URL url = new URL(peerUrl);
				if (peerType == PEER_TYPE.HOST) {
					try {
						String host = url.getHost();
						String hostName = InetAddress.getByName(host).getCanonicalHostName();
						Matcher matcher = peerIdPattern.matcher(hostName);
						return matcher.matches();
					} catch (UnknownHostException e) {
						log.error("Cannot resolve hostname", e);
						return false;
					}
				} else {
					try {
						String hostAddr = InetAddress.getByName(url.getHost())
								.getHostAddress();
						Matcher matcher = peerIdPattern.matcher(hostAddr);
						return matcher.matches();
					} catch (UnknownHostException e) {
						log.error("Cannot resolve host address", e);
						return false;
					}
				}
			} catch (MalformedURLException e) {
				log.error("Cannot parse url", e);
				return false;
			}
		}
	}

	@Override
	public RuleResult check(AccessCheckerContext checkCtx) {
		log.debug("Processing rule: " + id);
		if (matchAuthenticationClass(checkCtx.getAuthenticationClass())
				&& matchCommandName(checkCtx.getCommand())
				&& matchPeerId(checkCtx.getPeerUrl())) {
			log.debug("Match found for rule: " + id);
			return new RuleResult(action, userAction, true, chainId);
		} 
		log.debug("No match found for rule: " + id);
		return new RuleResult(action, userAction, false, chainId);
	}

}
