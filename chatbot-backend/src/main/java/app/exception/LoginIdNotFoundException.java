package app.exception;

public class LoginIdNotFoundException extends BaseException {
	
	public LoginIdNotFoundException(String message) {
        super(message);
    }

    @Override
    public Integer status() {
        return 404;
    }
}
