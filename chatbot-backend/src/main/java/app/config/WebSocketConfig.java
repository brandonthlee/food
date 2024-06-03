package app.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import app.handler.UserWebSocketApiHandler;
import app.handler.WebSocketPublicApiHandler;

@EnableWebSocket
@Configuration
public class WebSocketConfig implements WebSocketConfigurer {

	private final UserWebSocketApiHandler webSocketApiHandler;

	private final WebSocketPublicApiHandler webSocketPublicApiHandler;

	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		registry.addHandler(webSocketApiHandler, "/api/chat").addHandler(webSocketPublicApiHandler, "/api/public-chat")
				.setAllowedOrigins(Configs.CORS.toArray(new String[0])); // allow CORS
	}
}
