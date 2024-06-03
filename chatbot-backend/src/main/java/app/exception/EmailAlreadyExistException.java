package app.exception;

public class EmailAlreadyExistException extends BaseException {
	
	public EmailAlreadyExistException(String message) {
		super(message);
	}

	@Override
	public Integer status() {
		return 462;
	}
}
