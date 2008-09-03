package at.gv.egiz.bku.accesscontroller;

public enum UserAction {
	NONE("none"), INFO("info"), CONFIRM("confirm"), CONFIRM_WITH_SECRET("confirmWithSecret");
	
	private String name;

	UserAction(String name) {
		this.name = name;
	}

	public static UserAction fromString(String s) {
		for (UserAction ac : values()) {
			if (ac.name.equals(s)) {
				return ac;
			}
		}
		return null;
	}
}
