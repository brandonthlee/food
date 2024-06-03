package app.service;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import app.dto.FoodResponse;
import app.model.Food;
import app.repository.FoodRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class FoodService {
	
    private final FoodRepository foodRepository = null;

    @Transactional
    public FoodResponse.FindAllDto getRandomFood(Integer size) {
        List<Food> allFoods = foodRepository.findAll();

        Collections.shuffle(allFoods);
        List<Food> randomFoods = allFoods.subList(0, Math.min(size, allFoods.size()));

        return FoodResponse.FindAllDto.of(randomFoods);
    }
}
