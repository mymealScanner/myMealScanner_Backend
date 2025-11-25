package com.org.myMealScanner.customVision.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CustomVisionService {

    @Value("${azure.customvision.endpoint}")
    private String endpoint;

    @Value("${azure.customvision.prediction-key}")
    private String predictionKey;

    @Value("${azure.customvision.project-id}")
    private String projectId;

    @Value("${azure.customvision.iteration-name}")
    private String iterationName;

    public Map<String, Object> classifyImage(byte[] imageBytes) throws Exception {

        // REST API URL 구성
        String url = String.format(
                "%s/customvision/v3.0/Prediction/%s/detect/iterations/%s/image",
                endpoint, projectId, iterationName
        );

        // 헤더 설정 : azure portal에서 하라는대로
        HttpHeaders headers = new HttpHeaders();
        headers.set("Prediction-Key", predictionKey);
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

        // Body = byte[]으로, 이미지 자체를 받는거이기에
        HttpEntity<byte[]> entity = new HttpEntity<>(imageBytes, headers);

        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, String.class
        );

        // 결과 파싱
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response.getBody());
        JsonNode predictions = root.get("predictions");

        List<Map<String, Object>> result = new ArrayList<>();

        for (JsonNode p : predictions) {
            Map<String, Object> map = new HashMap<>();
            map.put("tagName", p.get("tagName").asText());
            map.put("probability", p.get("probability").asDouble());
            result.add(map);
        }

        // probability 가장 큰 값 하나만 반환
        Map<String, Object> bestPrediction = result.stream()
                .max(Comparator.comparingDouble(m -> (Double) m.get("probability")))
                .orElseThrow(() -> new RuntimeException("Prediction list is empty"));

        return bestPrediction;


    }
}