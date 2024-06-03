package app.dto;

import java.util.List;

import app.model.ChatRoom;
import app.model.Message;

public class ChatRoomResponse {
	
	public record GetChatroomDto(List<ChatRoomDto> chatrooms) {

		public static GetChatroomDto of(List<ChatRoom> chatrooms) {
			return new GetChatroomDto(chatrooms.stream().map(ChatRoomDto::new).toList());
		}

		public record ChatRoomDto(Long id, String title) {
			public ChatRoomDto(ChatRoom chatRoom) {
				this(chatRoom.getId(), chatRoom.getTitle());
			}
		}
	}

	/**
	 * Retrieves the content and the indicator of whether the message is from the chatbot from a list of messages
	 */
	public record getMessagesDto(List<MessageDto> messages) {

		public static getMessagesDto of(List<Message> messageList) {
			return new getMessagesDto(messageList.stream()
					.map(message -> new MessageDto(message.getId(), message.getContent(), message.isFromChatbot()))
					.toList());
		}

		public record MessageDto(Long id, String content, Boolean isFromChatbot) {

		}
	}

	public record CreateChatRoomDto(Long chatroomId) {
		public CreateChatRoomDto(ChatRoom chatRoom) {
			this(chatRoom.getId());
		}
	}
}
