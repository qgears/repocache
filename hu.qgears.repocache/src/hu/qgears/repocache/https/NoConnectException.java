package hu.qgears.repocache.https;

public class NoConnectException extends Exception{
	private static final long serialVersionUID = 1L;

	public NoConnectException() {
		super();
	}

	public NoConnectException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public NoConnectException(String message, Throwable cause) {
		super(message, cause);
	}

	public NoConnectException(String message) {
		super(message);
	}

	public NoConnectException(Throwable cause) {
		super(cause);
	}

}
