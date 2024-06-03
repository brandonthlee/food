package app.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@DynamicInsert
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoom {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(length = 50, nullable = false)
	private String title;

	@ManyToOne(fetch = FetchType.LAZY)
	private User user;

	@ColumnDefault("now()")
	private LocalDateTime createdTime;

	@Builder
	public ChatRoom(Long id, String title, User user, LocalDateTime createdTime) {
		this.id = id;
		this.title = title;
		this.user = user;
		this.createdTime = createdTime;
	}

	public void updateTitle(String title) {
		this.title = title;
	}

	public Long getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public User getUser() {
		return user;
	}
}
