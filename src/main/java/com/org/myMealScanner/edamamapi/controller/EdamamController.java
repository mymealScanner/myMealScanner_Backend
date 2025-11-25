package com.org.myMealScanner.edamamapi.controller;

import com.org.myMealScanner.edamamapi.dto.EdamamRecipeRequestDto;
import com.org.myMealScanner.edamamapi.dto.EdamamResponseDto;
import com.org.myMealScanner.edamamapi.dto.MacroNutrientsDto;
import com.org.myMealScanner.edamamapi.service.EdamamRecipeService;
import com.org.myMealScanner.edamamapi.service.EdamamService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class EdamamController {
    private final EdamamService edamamService;
    private final EdamamRecipeService edamamRecipeService;

    @GetMapping("/nutrition/{foodName}")
    public EdamamResponseDto analyzeNutrition(@PathVariable String foodName) {
        return edamamService.getNutritionInfo(foodName);
    }

    @GetMapping("/analysis/{foodName}")
    public MacroNutrientsDto analyzeAnalysis(@PathVariable String foodName) {
        return edamamService.getMacroNutrition(foodName);
    }
    @PostMapping("/nutrition/analyze-recipe")
    public EdamamResponseDto analyzeRecipeNutrition(
            @RequestBody EdamamRecipeRequestDto requestDto) {
        return edamamRecipeService.analyzeRecipeNutrition(requestDto);
    }
    @PostMapping("/nutrition/summary-recipe")
    public MacroNutrientsDto analyzeRecipeSummary(
            @RequestBody EdamamRecipeRequestDto requestDto) {
        return edamamRecipeService.getMacroNutritionSummary(requestDto);
    }

}
