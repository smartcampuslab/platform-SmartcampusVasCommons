package eu.trentorise.smartcampus.exceptions;

public class AlreadyExistException extends Exception {

	private static final long serialVersionUID = 738618972168002078L;

	public AlreadyExistException() {
		super();
	}

	public AlreadyExistException(String message, Throwable cause) {
		super(message, cause);
	}

	public AlreadyExistException(String message) {
		super(message);
	}

	public AlreadyExistException(Throwable cause) {
		super(cause);
	}

}
