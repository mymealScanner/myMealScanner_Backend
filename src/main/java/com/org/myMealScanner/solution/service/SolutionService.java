package com.org.myMealScanner.solution.service;

import com.org.myMealScanner.solution.dto.NutritionRequestDto;
import com.org.myMealScanner.solution.dto.NutritionSolutionResponse;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SolutionService {
    private final WebClient azureOpenAiWebClient;

    @Value("${azure.oai.key}")
    private String apiKey;

    @Value("${azure.oai.deployment}")
    private String deployment;

    @Value("${azure.oai.api-version}")
    private String apiVersion;

    private static final String SYSTEM_MESSAGE = """
        너는 헬스/식품 등에 대해 전문가야.
        내가 준 영양정보를 기반으로 각 식사별로 건강관리하기 위해
        그 다음 해당 식사는 어떻게 먹어야 하는지,
        그 식사의 영양 분포 및 칼로리는 어느 정도였는지 분석해줘.
        그리고 추천 운동을 개인 맞춤형으로 건강관리 루틴 형태로 제공해줘.
        각 식사별 분석 및 추천 내용은 12줄 이내로 요약해서 답변해줘.
        """;

    public NutritionSolutionResponse getAdvice(NutritionRequestDto request) throws Exception {

        Map<String, Object> body = Map.of(
                "messages", List.of(
                        Map.of("role", "system", "content", SYSTEM_MESSAGE),
                        Map.of("role", "user", "content", request.getNutritionInfo())
                ),
                "temperature", 0.9,
                "max_tokens", 1000
        );

        // Azure OpenAI chat completions 엔드포인트
        String path = String.format(
                "/openai/deployments/%s/chat/completions?api-version=%s",
                deployment, apiVersion
        );

        String responseStr = azureOpenAiWebClient.post()
                .uri(path)
                .header("api-key", apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        // 응답 파싱
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(responseStr);
        String content = root.path("choices")
                .get(0)
                .path("message")
                .path("content")
                .asText();

        return NutritionSolutionResponse.builder().solutionInfo(content).build();
    }
}
