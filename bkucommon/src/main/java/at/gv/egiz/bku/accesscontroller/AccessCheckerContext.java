package at.gv.egiz.bku.accesscontroller;

import at.gv.egiz.bku.slcommands.SLCommand;

public class AccessCheckerContext {
	private SLCommand command;
	private AuthenticationClass authenticationClass;
	private String peerUrl;

	public AccessCheckerContext(SLCommand cmd, AuthenticationClass ac, String url) {
		this.command = cmd;
		this.authenticationClass = ac;
		this.peerUrl = url;
	}

	public SLCommand getCommand() {
		return command;
	}

	public AuthenticationClass getAuthenticationClass() {
		return authenticationClass;
	}

	public String getPeerUrl() {
		return peerUrl;
	}
}
