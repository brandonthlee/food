package app.dto;

import java.util.List;

public class FoodPreference {
	
	public record SaveUserFoodPreferenceDto(List<Long> foodIds) {
	}
}
