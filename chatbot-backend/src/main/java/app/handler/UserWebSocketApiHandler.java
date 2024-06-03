package app.handler;

import java.io.IOException;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import app.dto.ChatUserRequest;
import app.dto.ChatbotRequest;
import app.model.Role;
import app.security.JwtProvider;
import app.service.UserWebSocketService;

import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class UserWebSocketApiHandler extends WebSocketBaseHandler {

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		String token;
		try {
			token = userWebSocketService.extractTokenFromSession(session);
		} catch (Exception e) {
			session.close();
			return;
		}
		try {
			DecodedJWT decodedJWT = JwtProvider.verify(token);
			Role role = decodedJWT.getClaim("role").as(Role.class);
			if (role != Role.ROLE_USER)
				throw new RuntimeException();
		} catch (TokenExpiredException e) {
			log.error("Token expired!");
			session.close();
		} catch (Exception e) {
			log.error("Failed to verify token!");
			session.close();
		}
		super.afterConnectionEstablished(session);
	}

	public UserWebSocketApiHandler(UserWebSocketService userWebSocketService, ObjectMapper objMapper) {
		super(userWebSocketService, objMapper);
	}

	@Override
	protected ChatUserRequest.MessageDtoInterface toMessageDto(String payload) throws JsonProcessingException {
		return objMapper.readValue(payload, ChatUserRequest.MessageDto.class);
	}

	@Override
	protected ChatbotRequest.MessageDto toChatbotMessageDto(ChatUserRequest.MessageDtoInterface messageDto,
			WebSocketSession session) throws IOException {
		Long userId = userWebSocketService.getUserId(session);

		return userWebSocketService.makeChatbotRequestDto((ChatUserRequest.MessageDto) messageDto, userId);
	}

	@Override
	protected void requestToChatbot(ChatUserRequest.MessageDtoInterface messageDtoInterface,
			ChatbotRequest.MessageDto chatbotMessageDto, WebSocketSession session) throws IOException {
		var userMessageDto = (ChatUserRequest.MessageDto) messageDtoInterface;
		userWebSocketService.requestToChatbot(chatbotMessageDto, session, userMessageDto.chatroomId());
	}
}
