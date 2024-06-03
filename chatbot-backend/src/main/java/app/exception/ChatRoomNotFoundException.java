package app.exception;

public class ChatRoomNotFoundException extends BaseException {
	
	public ChatRoomNotFoundException(String message) {
        super(message);
    }

    @Override
    public Integer status() {
        return 404;
    }
}
