package app.dto;

import java.util.Objects;

/**
 * A message response class from the user, serves as a container for the nested records
 */
public class ChatUserResponse {

	public record MessageDto(String event, String response) {
		public MessageDto(ChatbotResponse.MessageDto messageDto) {
			this(messageDto.event(), Objects.equals(messageDto.event(), "stream_end") ? ""
					: messageDto.history().internal().get(messageDto.history().internal().size() - 1).get(1));
		}

		public static MessageDto createStreamMessage(String response) {
			return new MessageDto("text_stream", response);
		}
	}

	public record MessageEndDto(String event, ResponseDto response) {

		public MessageEndDto(Long userMessageId, Long chatbotMessageId) {
			this("stream_end", new ResponseDto(userMessageId, chatbotMessageId));
		}

		public record ResponseDto(Long userMessageId, Long chatbotMessageId) {
		}
	}
}