package app.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ChatRoomRequest {
	
	public record ChangeTitleDto (
			@NotNull(message = "Please enter title for the chat room") @Size(max = 50, message = "Maximum allowed character: 50") String title) {
	}
}
