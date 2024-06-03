package app.exception;

public class LoginIdAlreadyExistException extends BaseException {
	
	public LoginIdAlreadyExistException(String message) {
        super(message);
    }

    @Override
    public Integer status() {
        return 461;
    }
}
