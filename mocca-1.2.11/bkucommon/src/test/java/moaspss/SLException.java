package moaspss;

public class SLException extends Exception {

	private static final long serialVersionUID = 1L;

	private int code;
	
	private String info;

	public SLException() {
		super();
	}

	public SLException(int code, String info) {
		super(code + ": " + info);
		this.code = code;
		this.info = info;
	}

	public SLException(Throwable cause, int code, String info) {
		super(code + ": " + info, cause);
		this.code = code;
		this.info = info;
	}

	public SLException(int code, Throwable cause) {
		super(code + ": " + cause.getMessage(), cause);
		this.code = code;
	}

	public int getCode() {
		return code;
	}

	public String getInfo() {
		return info;
	}
	
}
