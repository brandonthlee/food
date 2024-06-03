package app.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@DynamicInsert
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatLog {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(length = 20, nullable = false)
	private String ipAddr;

	@Column(columnDefinition = "TEXT", nullable = false)
	private String requestMessage;

	@Column(columnDefinition = "TEXT", nullable = false)
	private String output;

	@ColumnDefault(value = "now()")
	private LocalDateTime createdTime;

	@Builder
	public ChatLog(Long id, String ipAddr, String requestMessage, String output, LocalDateTime createdTime) {
		this.id = id;
		this.ipAddr = ipAddr;
		this.requestMessage = requestMessage;
		this.output = output;
		this.createdTime = createdTime;
	}
}