package com.org.myMealScanner.nutritionInfo.Repository;

import com.org.myMealScanner.nutritionInfo.dto.NutritionInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface NutritionRepository extends JpaRepository<NutritionInfo,String> {
    Optional<NutritionInfo> findByFoodName(String foodName);
}
