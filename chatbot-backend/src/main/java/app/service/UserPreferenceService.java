package app.service;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import app.dto.FoodPreference;
import app.exception.LoginIdNotFoundException;
import app.exception.NotFoundException;
import app.model.Food;
import app.model.User;
import app.model.UserPreference;
import app.repository.FoodRepository;
import app.repository.UserPreferenceRepository;
import app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service class for managing user's food preferences
 */
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class UserPreferenceService {
	final private UserPreferenceRepository preferenceRepository;
	final private FoodRepository foodRepository;
	final private UserRepository userRepository;

	@Transactional
	public void saveUserFoodPreference(Long id, FoodPreference.SaveUserFoodPreferenceDto requestDto) {
		List<Long> foodIds = requestDto.foodIds();
		User user = userRepository.findById(id).orElseThrow(() -> new LoginIdNotFoundException("User not found!"));

		preferenceRepository.deleteAllByUserId(id);

		List<Food> food = foodRepository.findByIdIn(foodIds);
		if (foodIds.size() != food.size())
			throw new BadRequestException("Food does not exist!");
		List<UserPreference> preference = food.stream().map(food -> UserPreference.builder().user(user).food(food).build())
				.collect(Collectors.toList());

		preferenceRepository.saveAll(preference);
		log.info("User preference succesfully saved. User ID: {}, Food ID: {}", id, foodIds);
	}
}
