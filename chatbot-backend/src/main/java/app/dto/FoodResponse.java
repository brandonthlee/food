package app.dto;

import java.util.List;

import app.model.Food;

public class FoodResponse {

	public record FindAllDto(List<FoodDto> foods) {

		public static FindAllDto of(List<Food> foodList) {
			return new FindAllDto(foodList.stream()
					.map(food -> new FoodDto(food.getId(), food.getName(), food.getImageUrl())).toList());
		}
		
		public record FoodDto(Long id, String name, String imageUrl) {
		}
	}
}
