package app.dto;

import java.util.List;

/**
 * A message response class from the chatbot
 */
public class ChatbotResponse {
	public record MessageDto(String event, int messageNum, HistoryDto history) {
		record HistoryDto(List<List<String>> internal, List<List<String>> visible) {
		}
	}
}
