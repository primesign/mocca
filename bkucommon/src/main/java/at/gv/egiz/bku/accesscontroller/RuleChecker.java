/*
* Copyright 2008 Federal Chancellery Austria and
* Graz University of Technology
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package at.gv.egiz.bku.accesscontroller;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.gv.egiz.bku.slcommands.SLCommand;
import at.gv.egiz.bku.slexceptions.SLRuntimeException;

public class RuleChecker implements AccessChecker {

	private final Logger log = LoggerFactory.getLogger(RuleChecker.class);

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
	protected CommandParamChecker paramChecker;

	public RuleChecker(String id) {
		if (id == null) {
			throw new NullPointerException("Id argument must not be null");
		}
		this.id = id;
	}

	public void setAuthenticationClass(String ac) {
		if (ac != null) {
			AuthenticationClass tmp = AuthenticationClass.fromString(ac);
			if (tmp == null) {
				throw new SLRuntimeException("Unknown authentication class " + ac);
			}
			authenticationClass = tmp;
		}
	}

	public void setAction(String ac) {
		if (ac != null) {
			Action tmp = Action.fromString(ac);
			if (tmp == null) {
				throw new SLRuntimeException("Unknown action " + ac);
			}
			action = tmp;
		}
	}

	public void setUserAction(String uac) {
		if (uac != null) {
			UserAction tmp = UserAction.fromString(uac);
			if (tmp == null) {
				throw new SLRuntimeException("Unknown user action " + uac);
			}
			userAction = tmp;
		}
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
		paramChecker = AccessControllerFactory.getInstance().createParamChecker(
				commandName);
	}

	/**
	 * Make sure to set the commandName first
	 * 
	 * @param key
	 * @param value
	 */
	public void addParameter(String key, String value) {
		if (paramChecker == null) {
			throw new IllegalArgumentException("Cannot set parameters for command "
					+ commandName);
		}
		paramChecker.addParameter(key, value);
	}

	public String getId() {
		return id;
	}

	protected boolean matchAuthenticationClass(AuthenticationClass cls) {
		if ((this.authenticationClass == null) || (cls == null)) {
			return true;
		}
		return this.authenticationClass.compareTo(cls) <= 0;
	}

	protected boolean matchCommandName(SLCommand cmd) {
		if ((commandName == null) || (cmd == null)) {
			return true;
		}
		Matcher matcher = commandNamePattern.matcher(cmd.getName());
		if (matcher.matches()) {
			if (paramChecker != null) {
				return paramChecker.checkParameter(cmd);
			} else {
				return true;
			}
		} else {
			return false;
		}
	}

	protected boolean matchPeerId(String peerUrl) {
		if ((peerId == null) || (peerUrl == null)) {
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
						String hostName = InetAddress.getByName(host)
								.getCanonicalHostName();
						Matcher matcher = peerIdPattern.matcher(hostName);
						return matcher.matches();
					} catch (UnknownHostException e) {
						log.error("Cannot resolve hostname.", e);
						return false;
					}
				} else {
					try {
						String hostAddr = InetAddress.getByName(url.getHost())
								.getHostAddress();
						Matcher matcher = peerIdPattern.matcher(hostAddr);
						return matcher.matches();
					} catch (UnknownHostException e) {
						log.error("Cannot resolve host address.", e);
						return false;
					}
				}
			} catch (MalformedURLException e) {
				log.error("Cannot parse url.", e);
				return false;
			}
		}
	}

	@Override
	public RuleResult check(AccessCheckerContext checkCtx) {
		log.debug("Processing rule: {}.", id);
		if (matchAuthenticationClass(checkCtx.getAuthenticationClass())
				&& matchCommandName(checkCtx.getCommand())
				&& matchPeerId(checkCtx.getPeerUrl())) {
			log.debug("Match found for rule: {}.", id);
			return new RuleResult(action, userAction, true, chainId);
		}
		log.debug("No match found for rule: {}", id);
		return new RuleResult(action, userAction, false, chainId);
	}

	public String getChainId() {
		return chainId;
	}

}
