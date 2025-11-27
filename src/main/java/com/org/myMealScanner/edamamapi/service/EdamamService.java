package com.org.myMealScanner.edamamapi.service;

import com.org.myMealScanner.edamamapi.dto.EdamamResponseDto;
import com.org.myMealScanner.edamamapi.dto.MacroNutrientsDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tools.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;

@Service
public class EdamamService {

    @Value("${edamam.app-id}")
    private String appId;

    @Value("${edamam.app-key}")
    private String appKey;

    private final WebClient webClient;

    public EdamamService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl("https://api.edamam.com/api/nutrition-data")
                .build();
    }


    public EdamamResponseDto getNutritionInfo(String foodName) {
        String query = "100g " +foodName;

        try {
            return webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .queryParam("app_id", appId)
                            .queryParam("app_key", appKey)
                            .queryParam("ingr", query)
                            .build())
                    .retrieve()

                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> clientResponse.createException().map(
                                    Exception -> new RuntimeException("Edamam API 오류: " + clientResponse.statusCode())
                            ))
                    .bodyToMono(String.class)
                    .doOnNext(responseString -> {
                        System.out.println("--- Edamam Raw Response ---");
                        System.out.println(responseString);
                        System.out.println("---------------------------");
                        System.out.println("API ID: " + appId);
                        System.out.println("API Key: " + appKey);
                    })

                    .map(responseString -> {
                        try {
                            return new ObjectMapper().readValue(responseString, EdamamResponseDto.class);
                        } catch (Exception e) {
                            throw new RuntimeException("DTO 매핑 실패: " + e.getMessage());
                        }
                    })
                    .block();

        } catch (Exception e) {
            System.err.println("Edamam API 호출 중 예외 발생: " + e.getMessage());
            return null;
        }
    }

    public Map<String, Double> extractMacroNutrients(EdamamResponseDto responseDto) {
        if (responseDto == null || responseDto.getIngredients() == null || responseDto.getIngredients().isEmpty()) {
            return new HashMap<>();
        }

        Map<String, EdamamResponseDto.NutrientDto> nutrientsMap =
                responseDto.getIngredients().get(0).getParsed().get(0).getNutrients();

        if (nutrientsMap == null) {
            return new HashMap<>();
        }

        Map<String, Double> nutrients = new HashMap<>();

        nutrients.put("칼로리(kcal)", nutrientsMap.getOrDefault("ENERC_KCAL", new EdamamResponseDto.NutrientDto()).getQuantity());

        nutrients.put("단백질(g)", nutrientsMap.getOrDefault("PROCNT", new EdamamResponseDto.NutrientDto()).getQuantity());

        nutrients.put("탄수화물(g)", nutrientsMap.getOrDefault("CHOCDF", new EdamamResponseDto.NutrientDto()).getQuantity());

        nutrients.put("지방(g)", nutrientsMap.getOrDefault("FAT", new EdamamResponseDto.NutrientDto()).getQuantity());


        return nutrients;
    }

    public MacroNutrientsDto getMacroNutrition(String foodName) {
        EdamamResponseDto responseDto = getNutritionInfo(foodName);
        Map<String, Double> nutrientMap = extractMacroNutrients(responseDto);

        MacroNutrientsDto macroDto = new MacroNutrientsDto();

        macroDto.setCalorie(nutrientMap.getOrDefault("칼로리(kcal)", 0.0));
        macroDto.setProtein(nutrientMap.getOrDefault("단백질(g)", 0.0));
        macroDto.setCarbohydrate(nutrientMap.getOrDefault("탄수화물(g)", 0.0));
        macroDto.setFat(nutrientMap.getOrDefault("지방(g)", 0.0));

        return macroDto;
    }
}