package app.handler;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import app.dto.ChatbotResponse;
import app.service.ChatbotWebSocketService;
import lombok.extern.slf4j.Slf4j;

/**
 * Handles thread-safe incoming messages from WebSocket client
 */
@Slf4j
public class ChatbotWebSocketHandler extends TextWebSocketHandler {

	private final BlockingQueue<ChatbotResponse.MessageDto> messageQueue = new LinkedBlockingQueue<>();

	private final ObjectMapper objMapper = new ObjectMapper();

	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) {
		try {
			String receivedMessage = message.getPayload();

			var receivedMessageDto = objMapper.readValue(receivedMessage, ChatbotResponse.MessageDto.class);

			messageQueue.put(receivedMessageDto); // Add received message to the queue

			if (ChatbotWebSocketService.isStreamEndEvent(receivedMessageDto)) {
				try {
					session.close();
				} catch (Exception e) {
					log.error("Unable to close the connection with the chatbot!");
				}
			}
		} catch (InterruptedException e) {
			log.error("An error occurred while processing the chatbot message!");
		} catch (JsonProcessingException e) {
			log.error("Received a response with an invalid format from the chatbot!");
		}
	}

	public ChatbotResponse.MessageDto receiveMessage() throws InterruptedException {
		return messageQueue.take(); // Retrieve message from the queue (wait if there are no messages)
	}
}