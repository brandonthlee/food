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
public class UserVerification {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(length = 100, nullable = false)
	private String email;

	@Column(length = 10, nullable = false)
	private String verificationCode;

	@ColumnDefault("now()")
	private LocalDateTime createdTime;

	@Builder
	public UserVerification(Long id, String email, String verificationCode, LocalDateTime createdTime) {
		this.id = id;
		this.email = email;
		this.verificationCode = verificationCode;
		this.createdTime = createdTime;
	}
}
