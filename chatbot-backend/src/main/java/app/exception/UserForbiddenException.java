package app.exception;

public class UserForbiddenException extends BaseException {
	
	public UserForbiddenException(String message) {
        super(message);
    }

    @Override
    public Integer status() {
        return 403;
    }
}
