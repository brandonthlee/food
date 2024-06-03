package app.exception;

public class DateConversionException extends BaseException {

	public DateConversionException(String message) {
		super(message);
	}

	@Override
	public Integer status() {
		return 500;
	}
}
