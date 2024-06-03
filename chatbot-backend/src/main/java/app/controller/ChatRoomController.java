package app.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import app.dto.ChatRoomRequest;
import app.dto.ChatRoomResponse;
import app.dto.ChatRoomResponse.CreateChatRoomDto;
import app.model.MyUserDetails;
import app.service.ChatRoomService;
import app.utils.ApiUtils;
import app.utils.cursor.CursorRequest;
import app.utils.cursor.PageCursor;
import io.netty.channel.unix.Errors;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class ChatRoomController {
	
	private final ChatRoomService chatRoomService = new ChatRoomService();

    @PostMapping("/chatroom")
    public ResponseEntity<?> create(@AuthenticationPrincipal MyUserDetails userDetails) {
    	CreateChatRoomDto createChatroomDto = chatRoomService.create(userDetails.getId());
        ApiUtils.Response<?> response = ApiUtils.success(createChatroomDto);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/chatroom")
    public ResponseEntity<?> get(@AuthenticationPrincipal MyUserDetails userDetails) {
        ChatRoomResponse.GetChatroomDto getChatroomDto = chatRoomService.get(userDetails.getId());
        ApiUtils.Response<?> response = ApiUtils.success(getChatroomDto);
        return ResponseEntity.ok().body(response);
    }

    @PutMapping("/chatroom/{chatroomId}")
    public ResponseEntity<?> changeTitle(@PathVariable Long chatroomId,
                                         @AuthenticationPrincipal MyUserDetails userDetails,
                                         @RequestBody @Valid ChatRoomRequest.ChangeTitleDto requestDto,
                                         Errors errors) {
    	chatRoomService.changeTitle(chatroomId, userDetails.getId(), requestDto);
        ApiUtils.Response<?> response = ApiUtils.success();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/chatroom/{chatroomId}")
    public ResponseEntity<?> deleteChatroom(@PathVariable Long chatroomId,
                                            @AuthenticationPrincipal MyUserDetails userDetails) {
    	chatRoomService.delete(userDetails.getId(), chatroomId);
        ApiUtils.Response<?> response = ApiUtils.success();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/chatroom/{chatroomId}/messages")
    public ResponseEntity<?> chatHistory(@PathVariable Long chatroomId,
                                         @AuthenticationPrincipal MyUserDetails userDetails,
                                         CursorRequest cursorRequest) {
        PageCursor<?> responseDto = chatRoomService.chatHistory(userDetails.getId(), chatroomId, cursorRequest);
        ApiUtils.Response<?> response = ApiUtils.success(responseDto);
        return ResponseEntity.ok(response);
    }
}
