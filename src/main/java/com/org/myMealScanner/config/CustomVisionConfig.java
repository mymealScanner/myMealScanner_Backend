package com.org.myMealScanner.config;

import com.microsoft.azure.cognitiveservices.vision.customvision.prediction.CustomVisionPredictionClient;
import com.microsoft.azure.cognitiveservices.vision.customvision.prediction.CustomVisionPredictionManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class CustomVisionConfig {

    @Value("${azure.customvision.prediction-key}")
    private String predictionKey;

    @Value("${azure.customvision.endpoint}")
    private String endpoint;

    @Bean
    public CustomVisionPredictionClient customVisionPredictionClient() {

        return CustomVisionPredictionManager
                .authenticate(predictionKey)
                .withEndpoint(endpoint);
    }
}
