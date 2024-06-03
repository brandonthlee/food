package app.dto;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/**
 * A message request class for the chatbot
 */
public class ChatbotRequest {
	
	// A list of strings that will trigger the stopping of text generation when encountered
	private static final List<String> TEXT_GENERATION_STOP_LIST = Collections
			.unmodifiableList(List.of("\n###", "\nAnswer", "\nUser", ":"));
	
	// Max number of new tokens that can be generated
	private static final Integer MAX_NEW_TOKEN = 250;
	
	// Parameter to control the randomness of the chatbot's response
	private static final Float RANDOMNESS_TEMP = 0.7f;
	
	// Parameter for penalize repetition in the chatbot
	private static final Float REPETITION_PENALTY = 1.15f;
	
	// Parameter for nucleus sampling; controlling the diversity of the generated text from chatbot
	private static final Float TOP_P = 0.9f;
	
	// Parameter to limit the sampling pool to the top K tokens
	private static final Float TOP_K_TOKENS = 20.0f;
	
	// Early stopping during text generation; disabled by default
	private static final Boolean EARLY_STOPPING = false;
	
	// Instruction template for guiding the chatbot's behaviour
	private static final String INSTRUCTION_TEMPLATE = "Alpaca";
	
	// Identifier for the chatbot
	private static final String IDENTIFIER = "Chatbot";
	
	private static final String USER = "User";

	public record MessageDto(String user_input, HistoryDto history, Boolean regenerate, Integer max_new_token,
			String character, String instruction_template, String your_name, Float temperature, Float top_p,
			Float repetition_penalty, Float top_k, Boolean early_stopping, List<String> stopping_strings,
			Boolean is_favor_chat) {

		public MessageDto(ChatUserRequest.PublicMessageDto userPublicMessageDto) {
			this(userPublicMessageDto.input(), new HistoryDto(userPublicMessageDto.history()),
					userPublicMessageDto.regenerate() != null && userPublicMessageDto.regenerate(), MAX_NEW_TOKEN,
							IDENTIFIER, INSTRUCTION_TEMPLATE, USER, RANDOMNESS_TEMP, TOP_P, REPETITION_PENALTY, TOP_K_TOKENS,
					EARLY_STOPPING, TEXT_GENERATION_STOP_LIST, false);
		}

		public MessageDto(String input, Boolean regenerate, List<List<String>> history, String userName,
				Boolean isFavorChat) {
			this(input, new HistoryDto(history), regenerate, MAX_NEW_TOKEN, IDENTIFIER, INSTRUCTION_TEMPLATE, userName,
					RANDOMNESS_TEMP, TOP_P, REPETITION_PENALTY, TOP_K_TOKENS, EARLY_STOPPING,
					Stream.concat(TEXT_GENERATION_STOP_LIST.stream(), Stream.of("\n" + userName)).toList(), isFavorChat);
		}

		record HistoryDto(List<List<String>> internal, List<List<String>> visible) {
			public HistoryDto(List<List<String>> userRequestHistory) {
				this(userRequestHistory, userRequestHistory);
			}
		}
	}
}
