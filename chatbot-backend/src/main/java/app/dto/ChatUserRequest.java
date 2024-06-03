package app.dto;

import java.util.List;

/**
 * A message request class from the user
 */
public class ChatUserRequest {

	public interface MessageDtoInterface {
		public boolean notValidate();
	};

	// Record to hold user input, chat history, and regeneration of the response
	public record PublicMessageDto(String input, List<List<String>> history, Boolean regenerate)
			implements MessageDtoInterface {
		/**
		 * Validates whether the input length is less than or equal to 500 characters <br>
		 * Validates whether the history size is less than equal to 20 and each message pair in the history=2
		 */
		public boolean notValidate() {
			var validInput = input.length() <= 500;
			var validHistory = history.size() <= 20
					&& history.stream().allMatch(messagePair -> messagePair.size() == 2);
			var validRegenerate = regenerate || !input.isEmpty();
			return !validInput || !validHistory || !validRegenerate;
		}
	}

	public record MessageDto(String input, Long chatroomId, Boolean regenerate) implements MessageDtoInterface {
		public boolean notValidate() {
			var validInput = input.length() <= 500;
			var validRegenerate = regenerate || !input.isEmpty();
			return !validInput || !validRegenerate;
		}
	}
}