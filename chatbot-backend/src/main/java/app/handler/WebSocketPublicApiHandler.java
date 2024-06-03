package app.handler;

import java.io.IOException;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import app.dto.ChatUserRequest;
import app.dto.ChatbotRequest;
import app.service.UserWebSocketService;

@Component
public class WebSocketPublicApiHandler extends WebSocketBaseHandler {

	public WebSocketPublicApiHandler(UserWebSocketService webSocketService, ObjectMapper objMapper) {
		super(webSocketService, objMapper);
	}

	@Override
	protected ChatUserRequest.MessageDtoInterface toMessageDto(String payload) throws JsonProcessingException {
		return objMapper.readValue(payload, ChatUserRequest.PublicMessageDto.class);
	}

	@Override
	protected ChatbotRequest.MessageDto toChatbotMessageDto(ChatUserRequest.MessageDtoInterface messageDto,
			WebSocketSession session) {

		return new ChatbotRequest.MessageDto((ChatUserRequest.PublicMessageDto) messageDto);
	}

	@Override
	protected void requestToChatbot(ChatUserRequest.MessageDtoInterface messageDtoInterface,
			ChatbotRequest.MessageDto chatbotMessageDto, WebSocketSession session) throws IOException {
		var messageDto = (ChatUserRequest.PublicMessageDto) messageDtoInterface;
		String requestMessage;
		requestMessage = objMapper.writeValueAsString(messageDto);
		userWebSocketService.requestToChatbotPublic(chatbotMessageDto, session, requestMessage);
	}
}
