package com.org.myMealScanner.nutritionInfo.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class NutrientsDto {
    private String name;
    private Integer calorie;
    private Float protein;
    private Float carbohydrate;
    private Float fat;
}
