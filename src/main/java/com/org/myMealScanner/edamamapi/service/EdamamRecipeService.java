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
        macroDto.setCalorie(nutrientMap.getOrDefault("calorie", 0.0));
        macroDto.setProtein(nutrientMap.getOrDefault("protein", 0.0));
        macroDto.setCarbohydrate(nutrientMap.getOrDefault("carbohydrate", 0.0));

        return macroDto;
    }


    public Map<String, Double> extractMacroNutrients(EdamamResponseDto responseDto) {
        if (responseDto == null || responseDto.getIngredients() == null || responseDto.getIngredients().isEmpty()) {
            return new HashMap<>();
        }

        Map<String, Double> totalNutrients = new HashMap<>();

        totalNutrients.put("calorie", 0.0);
        totalNutrients.put("protein", 0.0);
        totalNutrients.put("carbohydrate", 0.0);

        for (EdamamResponseDto.IngredientWrapperDto ingredientWrapper : responseDto.getIngredients()) {

            if (ingredientWrapper.getParsed() == null || ingredientWrapper.getParsed().isEmpty()) {
                continue;
            }

            Map<String, EdamamResponseDto.NutrientDto> nutrientsMap = ingredientWrapper.getParsed().get(0).getNutrients();

            if (nutrientsMap == null) {
                continue;
            }

            double currentCalorie = totalNutrients.get("calorie");
            double newCalorie = nutrientsMap.getOrDefault("ENERC_KCAL", new EdamamResponseDto.NutrientDto()).getQuantity();
            totalNutrients.put("calorie", currentCalorie + newCalorie);

            double currentProtein = totalNutrients.get("protein");
            double newProtein = nutrientsMap.getOrDefault("PROCNT", new EdamamResponseDto.NutrientDto()).getQuantity();
            totalNutrients.put("protein", currentProtein + newProtein);

            double currentCarb = totalNutrients.get("carbohydrate");
            double newCarb = nutrientsMap.getOrDefault("CHOCDF", new EdamamResponseDto.NutrientDto()).getQuantity();
            totalNutrients.put("carbohydrate", currentCarb + newCarb);
        }

        return totalNutrients;
    }
}