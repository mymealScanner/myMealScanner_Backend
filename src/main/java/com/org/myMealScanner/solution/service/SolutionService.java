package com.org.myMealScanner.solution.service;

import com.org.myMealScanner.solution.dto.NutritionRequestDto;
import com.org.myMealScanner.solution.dto.NutritionSolutionResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class SolutionService {
    private final WebClient azureOpenAiWebClient;

    @Value("${azure.oai.key}")
    private String apiKey;

    @Value("${azure.oai.deployment}")
    private String deployment;

    @Value("${azure.oai.api-version}")
    private String apiVersion;

    // RAG 용도
    @Value("${azure.search.endpoint}")
    private String searchEndpoint;

    @Value("${azure.search.key}")
    private String searchKey;

    @Value("${azure.search.index}")
    private String searchIndex;

    private static final String SYSTEM_MESSAGE = """
        너는 영양,식품 그리고 헬스/운동쪽에서 전문가이자 조교 역할이야.
        내가 준 영양정보를 기반으로 각 식사별로 건강관리하기 위해 부족한 영양분이 있다면,
        그 다음 날 해당 식사는 어떻게 먹어야 하는지, 더 채워야하는 영양분으로 구성된 새로운 음식으로 구체적으로 조언해줘.
        이때 음식을 몇g 먹어야하는지 구체적으로 안 작성해도 돼.
        그리고 추천 운동을 개인 맞춤형으로 건강관리를 루틴 형태로 구체적으로 운동소요시간과 운동이름과 함께 제공해줘.
        운동 효과 또한 사용자가 운동을 할만하게 효과를 구체적으로 작성해줘.
        해당 요청 정보에 대한 답만 하고 그외 다른 추가적인 정보는 요청하지 마.
        대답내용은 일관성이 있게 가장 최적의 베스트 정보로만 뽑아서 보여줘.
        해당 내용 분석시 핵심 내용으로만 4줄에서 6줄 정도는 채워줘.
        주어진 정보를 보고 똑같이 어떤것을 몇g 섭취했다고 따라하지 않아도 돼.
        한 문장이 끝날때마다 줄바꿈 또한 추가해줘. 그리고 다음 식사 권장해줄떄 등장하는 아침, 점심, 저녁이라는 단어가 나올때 가독성이 좋게
        무조건 한줄바꿔서 꼭 문장 생성해줘. 마지막으로 멘트를 검증할때 *와 같은거는 무조건 다 필터하고 출력해.
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

    // RAG 버전 메서드
    public NutritionSolutionResponse getAdviceWithRag(NutritionRequestDto request) throws Exception {

        try {
            var messages = List.of(
                    Map.of("role", "system", "content", SYSTEM_MESSAGE),
                    Map.of("role", "user", "content", request.getNutritionInfo())
            );

            // API 키 방식으로 인증
            Map<String, Object> auth = Map.of(
                    "type", "api_key",
                    "key", searchKey
            );


            Map<String, Object> searchParams = Map.of(
                    "endpoint", searchEndpoint,
                    "index_name", searchIndex,
                    "authentication", auth
            );

            Map<String, Object> dataSource = Map.of(
                    "type", "azure_search",
                    "parameters", searchParams
            );

            // 최종 요청 바디
            Map<String, Object> body = Map.of(
                    "messages", messages,
                    "temperature", 0.6,
                    "top_p", 1,
                    "max_tokens", 1500,
                    "data_sources", List.of(dataSource)
            );

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
                    .retryWhen(
                            Retry.backoff(3, Duration.ofMillis(500))     // 최대 3번 재시도, 0.5s, 1s, 2s
                                    .filter(ex -> ex instanceof IOException
                                            || ex instanceof WebClientRequestException)
                    )
                    .block();

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(responseStr);

            String content = root.path("choices")
                    .get(0)
                    .path("message")
                    .path("content")
                    .asText();

            return NutritionSolutionResponse.builder()
                    .solutionInfo(content)
                    .build();
        } catch (WebClientResponseException e) {
            log.info("Azure OpenAI error status: {}", e.getStatusCode());
            log.info("Azure OpenAI error body: {}", e.getResponseBodyAsString());
            throw e;
        }
    }

}
