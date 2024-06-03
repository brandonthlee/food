package app.model;

public record ChatSessionInfo(String chatSessionId, Long expirationTime) {
	private static final Long CONNECTION_TIMEOUT = 10 * 60 * 1000L; // 10 min

	public ChatSessionInfo(String chatRoomId) {
		this(chatRoomId, System.currentTimeMillis() + CONNECTION_TIMEOUT);
	}
}
