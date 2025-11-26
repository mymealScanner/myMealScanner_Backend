package com.org.myMealScanner.foodRecipeInfo.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class RecipeResponseDto {
    private String title;
    private List<String> ingr;
}
