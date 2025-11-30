package com.org.myMealScanner.nutritionInfo.controller;

import com.org.myMealScanner.nutritionInfo.Repository.NutritionRepository;
import com.org.myMealScanner.nutritionInfo.dto.NutrientsDto;
import com.org.myMealScanner.nutritionInfo.dto.NutritionInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class NutritionInfoController {
    private final NutritionRepository nutritionRepository;

    @GetMapping("/nutritionInfo/{foodName}")
    public ResponseEntity<NutrientsDto> getNutritionInfo(@PathVariable String foodName) {
        Optional<NutritionInfo> nutritionInfo = nutritionRepository.findByFoodName(foodName);
        if (nutritionInfo.isPresent()) {
            NutrientsDto responseDto = NutrientsDto.builder()
                    .name(nutritionInfo.get().getFoodName())
                    .calorie(nutritionInfo.get().getEnergyKcal())
                    .protein(nutritionInfo.get().getProteinG())
                    .carbohydrate(nutritionInfo.get().getCarbohydrateG())
                    .fat(nutritionInfo.get().getFatG())
                    .build();
            return ResponseEntity.ok(responseDto);
        }else{
            return ResponseEntity.notFound().build();
        }
    }

}
