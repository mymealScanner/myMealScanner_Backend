package com.org.myMealScanner.edamamapi.service;

import com.org.myMealScanner.edamamapi.dto.EdamamRecipeRequestDto;
import com.org.myMealScanner.edamamapi.dto.EdamamResponseDto;
import com.org.myMealScanner.edamamapi.dto.MacroNutrientsDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.HashMap;
import java.util.Map;

@Service
public class EdamamRecipeService {

    @Value("${edamam.app-id}")
    private String appId;

    @Value("${edamam.app-key}")
    private String appKey;

    private final WebClient webClient;

    public EdamamRecipeService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl("https://api.edamam.com/api/nutrition-details")
                .build();
    }

    public EdamamResponseDto analyzeRecipeNutrition(EdamamRecipeRequestDto recipeDto) {
        try {
            return webClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .queryParam("app_id", appId)
                            .queryParam("app_key", appKey)
                            .build())
                    .bodyValue(recipeDto)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> clientResponse.createException().map(
                                    Exception -> new RuntimeException("Edamam API 오류: " + clientResponse.statusCode())
                            ))

                    .bodyToMono(EdamamResponseDto.class)
                    .block();

        } catch (Exception e) {
            System.err.println("Edamam API 호출 중 예외 발생: " + e.getMessage());
            return null;
        }
    }

    public MacroNutrientsDto getMacroNutritionSummary(EdamamRecipeRequestDto recipeDto) {
        EdamamResponseDto responseDto = analyzeRecipeNutrition(recipeDto);

        Map<String, Double> nutrientMap = extractMacroNutrients(responseDto);

        MacroNutrientsDto macroDto = new MacroNutrientsDto();
        macroDto.setCalorie(nutrientMap.getOrDefault("칼로리(kcal)", 0.0));
        macroDto.setProtein(nutrientMap.getOrDefault("단백질(g)", 0.0));
        macroDto.setCarbohydrate(nutrientMap.getOrDefault("탄수화물(g)", 0.0));
        macroDto.setFat(nutrientMap.getOrDefault("지방(g)", 0.0)); // 💡 지방(Fat) 추가

        return macroDto;
    }

    public Map<String, Double> extractMacroNutrients(EdamamResponseDto responseDto) {
        if (responseDto == null || responseDto.getIngredients() == null || responseDto.getIngredients().isEmpty()) {
            return new HashMap<>();
        }

        Map<String, Double> totalNutrients = new HashMap<>();

        totalNutrients.put("칼로리(kcal)", 0.0);
        totalNutrients.put("단백질(g)", 0.0);
        totalNutrients.put("탄수화물(g)", 0.0);
        totalNutrients.put("지방(g)", 0.0);

        for (EdamamResponseDto.IngredientWrapperDto ingredientWrapper : responseDto.getIngredients()) {

            if (ingredientWrapper.getParsed() == null || ingredientWrapper.getParsed().isEmpty()) {
                continue;
            }

            Map<String, EdamamResponseDto.NutrientDto> nutrientsMap = ingredientWrapper.getParsed().get(0).getNutrients();

            if (nutrientsMap == null) {
                continue;
            }

            double currentCalorie = totalNutrients.get("칼로리(kcal)");
            double newCalorie = nutrientsMap.getOrDefault("ENERC_KCAL", new EdamamResponseDto.NutrientDto()).getQuantity();
            totalNutrients.put("칼로리(kcal)", currentCalorie + newCalorie);

            double currentProtein = totalNutrients.get("단백질(g)");
            double newProtein = nutrientsMap.getOrDefault("PROCNT", new EdamamResponseDto.NutrientDto()).getQuantity();
            totalNutrients.put("단백질(g)", currentProtein + newProtein);

            double currentCarb = totalNutrients.get("탄수화물(g)");
            double newCarb = nutrientsMap.getOrDefault("CHOCDF", new EdamamResponseDto.NutrientDto()).getQuantity();
            totalNutrients.put("탄수화물(g)", currentCarb + newCarb);

            double currentFat = totalNutrients.get("지방(g)");
            double newFat = nutrientsMap.getOrDefault("FAT", new EdamamResponseDto.NutrientDto()).getQuantity();
            totalNutrients.put("지방(g)", currentFat + newFat);
        }
        return totalNutrients;
    }
}