package app.dto;

import java.util.List;

import app.model.User;
import app.model.UserPreference;

public class UserResponse {
	public record GetUserDto(Long id, String loginId, String name, Boolean gender, String birth, String email,
			List<UserPreferenceDto> preference) {

		public GetUserDto(User user, List<UserPreference> favors) {
			this(user.getId(), user.getLoginId(), user.getName(), user.getGender(), user.getBirthDate().toString(),
					user.getEmail(), favors.stream().map(UserPreferenceDto::new).toList());
		}

		public record UserPreferenceDto(Long foodId, String foodName) {
			public UserPreferenceDto(UserPreference userPreference) {
				this(userPreference.getFood().getId(), userPreference.getFood().getName());
			}
		}
	}

	public record FindUserIdDto(String loginId) {
		public FindUserIdDto(String loginId) {
			this.loginId = loginId.substring(0, 2) + "***" + loginId.substring(loginId.length() - 1, loginId.length());
		}

	}

	public record TokensDto(String access, String refresh) {
	}
}
