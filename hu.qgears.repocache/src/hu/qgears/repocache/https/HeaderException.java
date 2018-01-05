package hu.qgears.repocache.https;

public class HeaderException extends Exception{
	private static final long serialVersionUID = 1L;
	public int code;
	public String message;
	public HeaderException(int code, String message) {
		super();
		this.code = code;
		this.message = message;
	}
}
