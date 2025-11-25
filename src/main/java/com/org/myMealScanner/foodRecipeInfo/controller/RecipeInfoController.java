package com.org.myMealScanner.foodRecipeInfo.controller;

import com.org.myMealScanner.foodRecipeInfo.dto.RecipeResponseDto;
import com.org.myMealScanner.foodRecipeInfo.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RecipeInfoController {
    private final ChatService chatService;

    @GetMapping("/getFoodRecipe/{foodName}")
    public RecipeResponseDto getRecipeInfo(@PathVariable String foodName) {
        return chatService.generateRecipeJson(foodName);
    }
}
