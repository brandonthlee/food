package app.exception;

import app.utils.ApiUtils;

public abstract class BaseException extends RuntimeException {
	public BaseException(String message) {
		super(message);
	}

	public abstract Integer status();

	public ApiUtils.Response<?> body() {
		return ApiUtils.error(getMessage(), status());
	}
}
