package com.org.myMealScanner.edamamapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EdamamRecipeRequestDto {

    private String title;

    private List<String> ingr;
}