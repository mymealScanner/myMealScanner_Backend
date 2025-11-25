package com.org.myMealScanner.edamamapi.dto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EdamamResponseDto {
    private List<IngredientWrapperDto> ingredients;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class IngredientWrapperDto {
        private List<ParsedIngredientDto> parsed;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ParsedIngredientDto {
        private Map<String, NutrientDto> nutrients;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class NutrientDto {
        private String label;
        private double quantity;
        private String unit;
    }
}