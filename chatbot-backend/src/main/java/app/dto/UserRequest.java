package app.dto;

import app.model.Role;
import app.model.User;
import app.utils.DateUtils;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * User-related requests including login, registration, and profile updates
 */
public class UserRequest {
	
	public record LoginDto(@NotEmpty @Size(max = 40, message = "Maximum allowed characters: 40") String loginId,
			@NotEmpty @Size(max = 64, message = "Maximum allowed characters: 64") String password) {
	}

	public record JoinDto(
			@NotEmpty @Size(min = 4, max = 40, message = "Must be between 4 to 40 characters") @Pattern(regexp = "^[\\w.]+$", message = "Only letters, numbers, _, and . are allowed") String loginId,
			@NotEmpty @Size(min = 8, max = 64, message = "Must be between 8 to 64 characters") @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*[\\d@#$%^&!])[a-zA-Z\\d@#$%^&!]+$", message = "Must include at least two of the following types: letters, numbers, or special characters") String password,
			@NotEmpty String passwordCheck, @Size(max = 40, message = "Maximum allowed characters: 40") String name,
			Boolean gender,
			@Pattern(regexp = "^[12]\\d{3}-(0?[1-9]|1[0-2])-(0?[1-9]|[1-2][0-9]|3[01])$", message = "Invalid date format (ex. 1990-01-01)") String birth,
			@NotEmpty @Size(max = 100, message = "Max size is 100") @Pattern(regexp = "^[\\w.%+-]+@[\\w.-]+\\.[A-Za-z]{2,}$", message = "Invalid email address format") String email) {

		public User createUser(String EncodedPassword) {
			return User.builder().loginId(loginId).password(EncodedPassword).name(name.isEmpty() ? null : name)
					.gender(gender).birth(birth == null ? null : DateUtils.convertStringToDate(birth)).email(email)
					.role(Role.ROLE_PENDING).build();
		}
	}

	public record UpdateDto(
			@Size(min = 4, max = 40, message = "Must be between 4 to 40 characters") @Pattern(regexp = "^[\\w.]+$", message = "Only letters, numbers, _, and . are allowed") String loginId,
			@Size(min = 8, max = 64, message = "Must be between 8 to 64 characters") @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*[\\d@#$%^&!])[a-zA-Z\\d@#$%^&!]+$", message = "Must include at least two of the following types: letters, numbers, or special characters") String password,
			String passwordCheck, @Size(max = 40, message = "Maximum allowed characters: 40") String name,
			Boolean gender,
			@Pattern(regexp = "^[12]\\d{3}-(0?[1-9]|1[0-2])-(0?[1-9]|[1-2][0-9]|3[01])$", message = "Invalid date format (ex. 1990-01-01)") String birth,
			@Size(max = 100, message = "Maximum allowed characters: 100") @Pattern(regexp = "^[\\w.%+-]+@[\\w.-]+\\.[A-Za-z]{2,}$", message = "Invalid email format") String email) {
	}

	public record ValidateLoginIdDto(String loginId) {
	}

	public record ValidateEmailDto(String email) {
	}

	public record FindUserIdDto(String email) {
	}

	public record ResetPasswordDto(String loginId, String email) {
	}
}