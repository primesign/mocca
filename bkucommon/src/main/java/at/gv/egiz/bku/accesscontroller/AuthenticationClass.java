package at.gv.egiz.bku.accesscontroller;

public enum AuthenticationClass {
	ANONYMOUS("anonymous"), PSEUDO_ANONYMOUS("pseudoanonymous"), CERTIFIED(
			"certified"), CERTIFIED_GOV_AGENCY("certifiedGovAgency");

	private String name;

	AuthenticationClass(String name) {
		this.name = name;
	}

	public static AuthenticationClass fromString(String s) {
		for (AuthenticationClass ac : values()) {
			if (ac.name.equals(s)) {
				return ac;
			}
		}
		return null;
	}
}
