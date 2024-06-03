package app.service;

import lombok.extern.slf4j.Slf4j;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import app.dto.ChatUserResponse;
import app.dto.ChatbotResponse;
import app.exception.ResouceLimitException;
import app.handler.ChatbotWebSocketHandler;
import app.model.MyFunction;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;

@Slf4j
public class ChatbotWebSocketService {

	final private WebSocketClient webSocketClient = new StandardWebSocketClient();

	final private String serverUri;

	final private ChatbotWebSocketHandler chatbotWebSocketHandler = new ChatbotWebSocketHandler();

	final private ExecutorService executorService = Executors.newSingleThreadExecutor();

	final private ObjectMapper om = new ObjectMapper();

	public ChatbotWebSocketService(String serverUri) {
		this.serverUri = serverUri;
	}

	public void sendMessage(String message) throws Exception {
		// Send message to server
		WebSocketSession session = webSocketClient.execute(chatbotWebSocketHandler, serverUri).get();
		log.info("Message to send to chatbot: " + message);
		session.sendMessage(new TextMessage(message));
	}

	public void listenForMessages(WebSocketSession user, MyFunction function) {

		Future<?> future = executorService.submit(() -> {
			String finalResponse = "";
			try {
				while (!Thread.currentThread().isInterrupted()) {
					try {
						ChatbotResponse.MessageDto chatbotMessageDto = chatbotWebSocketHandler.receiveMessage();

						var userMessageDto = new ChatUserResponse.MessageDto(chatbotMessageDto);

						if (isStreamEndEvent(chatbotMessageDto)) {
							List<Integer> endIndexes = List.of(finalResponse.indexOf(".\n"),
									finalResponse.indexOf("?\n"), finalResponse.indexOf("!\n"),
									finalResponse.indexOf("~\n"));

							var endIndex = Collections.max(endIndexes);

							if (endIndex != -1) {
								finalResponse = finalResponse.substring(0, endIndex + 1);
								var lastMessageDto = ChatUserResponse.MessageDto.createStreamMessage(finalResponse);
								TextMessage textMessage = new TextMessage(om.writeValueAsString(lastMessageDto));
								user.sendMessage(textMessage);
							}

							var messageIds = function.apply(finalResponse);
							var endMessageDto = new ChatUserResponse.MessageEndDto(messageIds.userMessageId(),
									messageIds.chatbotMessageId());
							TextMessage textMessage = new TextMessage(om.writeValueAsString(endMessageDto));

							user.sendMessage(textMessage);
							break;
						}
						TextMessage textMessage = new TextMessage(om.writeValueAsString(userMessageDto));

						user.sendMessage(textMessage);

						finalResponse = userMessageDto.response();
					} catch (InterruptedException e) {
						throw new ResouceLimitException("An error occurred while listening for the chatbot's response!");
					} catch (JsonProcessingException e) {
						throw new ResouceLimitException("An error occurred while parsing the chatbot's response!");
					} catch (IOException e) {
						throw new ResouceLimitException("An error occurred while delivering the chatbot's response!");
					}
				}
			} catch (ResouceLimitException e) {
				throw new ResouceLimitException(e.getMessage());
			} finally {
				log.debug("Thread terminated");
			}
		});

		try {
			// Terminate the thread after 10 minutes
			future.get(10, TimeUnit.MINUTES);
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			log.error("Connection with the chatbot exceeded 10 minutes and was forcibly terminated!");
			future.cancel(true); // Forcefully terminate the thread in case of an exception
			throw new ResouceLimitException("Connection with the chatbot exceeded 10 minutes and was forcibly terminated!");
		} catch (ResouceLimitException e) {
			throw new ResouceLimitException(e.getMessage());
		} finally {
			executorService.shutdown();
		}
	}

	public static boolean isStreamEndEvent(ChatbotResponse.MessageDto messageDto) {
		return Objects.equals(messageDto.event(), "stream_end");
	}
}