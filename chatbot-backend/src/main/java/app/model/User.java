package app.model;

import java.time.LocalDate;
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
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(length = 40, nullable = false, unique = true)
	private String loginId;

	@Column(length = 100, nullable = false)
	private String password;

	@Column(length = 40)
	@ColumnDefault("'name'")
	private String name;

	@ColumnDefault("false")
	private Boolean gender;

	@ColumnDefault("'1990-01-01'")
	private LocalDate birthDate;

	@Column(length = 100, nullable = false, unique = true)
	private String email;

	@Enumerated(EnumType.STRING)
	@ColumnDefault("'ROLE_PENDING'")
	@Column(length = 50)
	private Role role;

	@ColumnDefault("now()")
	private LocalDateTime createdTime;

	@Builder
	public User(Long id, String loginId, String password, String name, Boolean gender, LocalDate birthDate,
			String email, Role role, LocalDateTime createdTime) {
		this.id = id;
		this.loginId = loginId;
		this.password = password;
		this.name = name;
		this.gender = gender;
		this.birthDate = birthDate;
		this.email = email;
		this.role = role;
		this.createdTime = createdTime;
	}

	public void updateLoginId(String loginId) {
		this.loginId = loginId;
	}

	public void updatePassword(String password) {
		this.password = password;
	}

	public void updateName(String name) {
		this.name = name;
	}

	public void updateGender(Boolean gender) {
		this.gender = gender;
	}

	public void updateBirth(LocalDate birthDate) {
		this.birthDate = birthDate;
	}

	public void updateRole(Role role) {
		this.role = role;
	}

	public void updateEmail(String email) {
		this.email = email;
	}

	public Long getId() {
		return id;
	}

	public String getLoginId() {
		return loginId;
	}

	public String getName() {
		return name;
	}

	public Boolean getGender() {
		return gender;
	}

	public Object getBirthDate() {
		return birthDate;
	}

	public String getEmail() {
		return email;
	}

	public Role getRole() {
		return role;
	}
}
