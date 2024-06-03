package app.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import app.dto.FoodResponse;
import app.service.FoodService;
import app.utils.ApiUtils;

@RequestMapping("/api")
@RestController
public class FoodController {
	
	private final FoodService foodService = new FoodService();

    @GetMapping("/food/random")
    public ResponseEntity<?> getRandomFood(@RequestParam(value = "size",defaultValue = "30")
                                                                  Integer size) {
        FoodResponse.FindAllDto responseDto = foodService.getRandomFood(size);
        ApiUtils.Response<?> response = ApiUtils.success(responseDto);
        return ResponseEntity.ok().body(response);
    }

}
