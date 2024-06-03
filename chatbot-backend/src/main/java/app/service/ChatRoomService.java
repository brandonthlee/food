package app.service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Objects;

import org.hibernate.mapping.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpServerErrorException;

import app.dto.ChatRoomRequest;
import app.dto.ChatRoomResponse;
import app.dto.ChatRoomResponse.CreateChatRoomDto;
import app.dto.ChatRoomResponse.GetChatroomDto;
import app.exception.ChatRoomNotFoundException;
import app.exception.InternalServerErrorException;
import app.exception.LoginIdNotFoundException;
import app.exception.ResouceLimitException;
import app.exception.UserForbiddenException;
import app.model.ChatRoom;
import app.model.Message;
import app.model.User;
import app.repository.ChatRoomRepository;
import app.repository.MessageRepository;
import app.repository.UserRepository;
import app.utils.DateUtils;
import app.utils.cursor.CursorRequest;
import app.utils.cursor.PageCursor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class ChatRoomService {

	private final private ChatRoomRepository chatRoomRepository;

	private final private UserRepository userRepository;

	private final private MessageRepository messageRepository;

	@Transactional
	public CreateChatRoomDto create(Long userId) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new LoginIdNotFoundException("LoginId not found!"));

		String title = DateUtils.convertDateTimeToString(LocalDateTime.now()) + " chat";

		ChatRoom chatRoom = ChatRoom.builder().title(title).user(user).build();

		try {
			ChatRoom createdChatRoom = chatRoomRepository.save(chatRoom);
			return new ChatRoomResponse.CreateChatRoomDto(createdChatRoom);

		} catch (Exception e) {
			throw new ResouceLimitException("An error occurred during chat room creation!");
		}
	}

	public GetChatroomDto get(Long userId) {
		List<ChatRoom> chatRooms = chatRoomRepository.findAllByUserIdOrderByIdDesc(userId);
		return ChatRoomResponse.GetChatroomDto.of(chatRooms);
	}

	@Transactional
	public void changeTitle(Long chatRoomId, Long userId, ChatRoomRequest.ChangeTitleDto requestDto) {
		ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
				.orElseThrow(() -> new ChatRoomNotFoundException("Chat room not found!"));

		if (!Objects.equals(chatRoom.getUser().getId(), userId))
			throw new UserForbiddenException("This is not the user's chat room!");

		chatRoom.updateTitle(requestDto.title());
	}

	@Transactional
	public void delete(Long userId, Long chatRoomId) {
		ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
				.orElseThrow(() -> new ChatRoomNotFoundException("Chat room not found!"));

		if (!Objects.equals(userId, chatRoom.getUser().getId()))
			throw new UserForbiddenException("This is not the user's chat room!");

		messageRepository.deleteAllByChatroomId(chatRoomId);
		chatRoomRepository.delete(chatRoom);
	}

	public PageCursor<ChatRoomResponse.getMessagesDto> chatHistory(Long userId, Long chatroomId,
			CursorRequest cursorRequest) {

		ChatRoom chatRoom = chatRoomRepository.findById(chatroomId)
				.orElseThrow(() -> new ChatRoomNotFoundException("Chat room not found!"));

		// Checks if the users of the chat room and the currently logged-in user are the
		// same
		if (!Objects.equals(chatRoom.getUser().getId(), userId)) {
			throw new UserForbiddenException("Invalid user chatroom!");
		}

		var messages = getMessages(chatroomId, cursorRequest);
		Collections.reverse(messages);

		var response = ChatRoomResponse.getMessagesDto.of(messages);

		var nextKey = messages.stream().mapToLong(Message::getId).min().orElse(CursorRequest.NONE_KEY);

		return new PageCursor<>(cursorRequest.next(nextKey), response);
	}

	private List getMessages(Long chatroomId, CursorRequest cursorRequest) {

		if (cursorRequest.hasSize()) {
			Pageable pageable = PageRequest.of(0, cursorRequest.size());

			if (cursorRequest.hasKey())
				return messageRepository.findAllByChatroomIdAndIdLessThanOrderByIdDesc(chatroomId, cursorRequest.key(),
						pageable);
			return messageRepository.findAllByChatroomIdOrderByIdDesc(chatroomId, pageable);
		}
		return messageRepository.findAllByChatroomIdOrderByIdDesc(chatroomId);
	}
}
