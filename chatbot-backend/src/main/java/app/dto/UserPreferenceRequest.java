package app.dto;

import java.util.List;

public class UserPreferenceRequest {
	public record SaveUserFoodPreferenceDto(List<Long> foodIds) {

	}
}
