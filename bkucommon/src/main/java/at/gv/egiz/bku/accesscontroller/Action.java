package at.gv.egiz.bku.accesscontroller;

public enum Action {
	ALLOW("allow"), DENY("deny");
	private String name;

	Action(String name) {
		this.name = name;
	}

	public static Action fromString(String s) {
		for (Action ac : values()) {
			if (ac.name.equals(s)) {
				return ac;
			}
		}
		return null;
	}
}
