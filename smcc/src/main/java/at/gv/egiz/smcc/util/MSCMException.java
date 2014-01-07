package at.gv.egiz.smcc.util;

public class MSCMException extends Exception {
  private static final long serialVersionUID = 1L;

  private short hive;
	
	public MSCMException(short hive, String netException, String optional) {
		super(netException + " [ " + optional + " ]");
		this.setHive(hive);
	}
	
	public MSCMException(short hive, String netException) {
		super(netException);
		this.setHive(hive);
	}

	public short getHive() {
		return hive;
	}

	public void setHive(short hive) {
		this.hive = hive;
	}
}
