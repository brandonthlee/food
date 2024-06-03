package app.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@DynamicInsert
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Message {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	private ChatRoom chatRoom;

	@Column(nullable = false)
	private boolean isFromChatbot;

	@Column(columnDefinition = "TEXT", nullable = false)
	private String content;

	@ColumnDefault(value = "now()")
	private LocalDateTime createdTime;

	@Builder
	public Message(Long id, ChatRoom chatRoom, boolean isFromChatbot, String content, LocalDateTime createdTime) {
		this.id = id;
		this.chatRoom = chatRoom;
		this.isFromChatbot = isFromChatbot;
		this.content = content;
		this.createdTime = createdTime;
	}

	public void updateContent(String content) {
		this.content = content;
	}

	public Long getId() {
		return id;
	}

	public String getContent() {
		return content;
	}

	public Boolean isFromChatbot() {
		return isFromChatbot;
	}
}
