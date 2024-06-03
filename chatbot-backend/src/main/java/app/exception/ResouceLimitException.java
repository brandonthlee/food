package app.exception;

public class ResouceLimitException extends BaseException {

	public ResouceLimitException(String message) {
		super(message);
	}

	@Override
	public Integer status() {
		return 500;
	}
}
