package app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import app.model.Food;

public interface FoodRepository extends JpaRepository<Food, Long> {
    List<Food> findByIdIn(List<Long> ids);
}
