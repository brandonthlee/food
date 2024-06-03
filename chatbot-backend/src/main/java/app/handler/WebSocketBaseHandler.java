package app.handler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import app.dto.ChatSessionInfo;
import app.dto.ChatUserRequest;
import app.dto.ChatbotRequest;
import app.exception.ResouceLimitException;
import app.service.UserWebSocketService;
import lombok.extern.slf4j.Slf4j;

/**
 * Handles WebSocket connections, processing incoming messages, and managing
 * chat sessions
 */
@Slf4j
@Component
public abstract class WebSocketBaseHandler extends TextWebSocketHandler {

	protected final UserWebSocketService userWebSocketService;

	protected final Map<WebSocketSession, ChatSessionInfo> chatSessions = new ConcurrentHashMap<>();

	protected final ObjectMapper objMapper;

	public WebSocketBaseHandler(UserWebSocketService userWebSocketService, ObjectMapper objMapper) {
		this.userWebSocketService = userWebSocketService;
		this.objMapper = objMapper;
		ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
		scheduler.scheduleAtFixedRate(this::checkExpiredConnections, 1, 1, TimeUnit.SECONDS);
	}

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		// When a client requests to join a chat room, match the user with the chat room
		// and store it
		String sessionId = session.getId(); // Set the user ID appropriately (session ID can be used)
		String chatSessionId = "chatSession_" + sessionId; // Set an individual chat room ID (e.g., chatRoom_userId)
		chatSessions.put(session, new ChatSessionInfo(chatSessionId));
		log.info(session + " client connected");
	}

	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
		String payload = message.getPayload();
		log.info("Received message: " + payload);

		// Map to Object
		ChatUserRequest.MessageDtoInterface messageDtoInterface;
		try {
			messageDtoInterface = toMessageDto(payload);
			if (messageDtoInterface.notValidate()) {
				throw new RuntimeException();
			}
		} catch (Exception e) {
			session.sendMessage(userWebSocketService.createErrorMessage("The message format is not correct."));
			session.close();
			return;
		}

		ChatbotRequest.MessageDto chatbotMessageDto;
		try {
			chatbotMessageDto = toChatbotMessageDto(messageDtoInterface, session);
		} catch (Exception e) {
			session.sendMessage(userWebSocketService.createErrorMessage("Invalid input."));
			session.close();
			return;
		}

		try {
			requestToChatbot(messageDtoInterface, chatbotMessageDto, session);
		} catch (ResouceLimitException e) {
			session.sendMessage(userWebSocketService.createErrorMessage(e.getMessage()));
		} catch (JsonProcessingException e) {
			session.sendMessage(
					userWebSocketService.createErrorMessage("An error occurred while converting the message!"));
		} catch (Exception e) {
			session.sendMessage(
					userWebSocketService.createErrorMessage("An error occurred while requesting to the chatbot!"));
		} finally {
			session.close(); // Automatically close the session
		}
	}

	protected abstract ChatUserRequest.MessageDtoInterface toMessageDto(String payload) throws JsonProcessingException;

	protected abstract ChatbotRequest.MessageDto toChatbotMessageDto(ChatUserRequest.MessageDtoInterface messageDto,
			WebSocketSession session) throws IOException;

	protected abstract void requestToChatbot(ChatUserRequest.MessageDtoInterface messageDtoInterface,
			ChatbotRequest.MessageDto chatbotMessageDto, WebSocketSession session) throws IOException;

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		log.info(session + " client disconnected");
		chatSessions.remove(session);
	}

	private void checkExpiredConnections() {
		long currentTime = System.currentTimeMillis();
		for (WebSocketSession session : chatSessions.keySet()) {
			ChatSessionInfo chatSessionInfo = chatSessions.get(session);
			long expirationTime = chatSessionInfo.expirationTime();
			if (expirationTime <= currentTime) {
				try {
					session.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				chatSessions.remove(session);
			}
		}
	}
}
