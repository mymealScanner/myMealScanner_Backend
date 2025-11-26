package com.org.myMealScanner.foodRecipeInfo.service;

import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.ai.openai.models.*;
import com.azure.core.credential.AzureKeyCredential;
import com.org.myMealScanner.foodRecipeInfo.dto.RecipeResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.Arrays;
import java.util.List;

@Service
public class ChatService {

    @Value("${azure.openai.deployment-name}")
    private String deploymentName;

    private final OpenAIClient openAIClient;

    private final ObjectMapper objectMapper;

    public ChatService(@Value("${azure.openai.api-key}") String apiKey,
                       @Value("${azure.openai.endpoint}") String endpoint) {

        this.openAIClient = new OpenAIClientBuilder()
                .credential(new AzureKeyCredential(apiKey))
                .endpoint(endpoint)
                .buildClient();

        this.objectMapper = new ObjectMapper();
    }

    public RecipeResponseDto generateRecipeJson(String foodName) {

        List<ChatRequestMessage> chatMessages = Arrays.asList(
                new ChatRequestSystemMessage(
                        "You are a helpful assistant specialized in generating 1-person serving Korean food recipes in JSON format. "
                                + "Your output must be ONLY a valid JSON object matching this schema: "
                                + "{\"title\": \"Recipe Title (Korean/English)\", \"ingr\": [\"Amount Unit Ingredient Description\"]}. "
                                + "Respond ONLY in English within the JSON structure. "
                                + "You must respond with the JSON only, no other text or explanation."
                ),

                new ChatRequestUserMessage(foodName + " recipe (1 serving size) please.")
        );

        ChatCompletionsOptions chatCompletionsOptions = new ChatCompletionsOptions(chatMessages);
        chatCompletionsOptions.setMaxTokens(4096);
        chatCompletionsOptions.setTemperature(1d);
        chatCompletionsOptions.setTopP(1d);

        ChatCompletions chatCompletions = openAIClient.getChatCompletions(deploymentName, chatCompletionsOptions);

        if (chatCompletions.getChoices() != null && !chatCompletions.getChoices().isEmpty()) {
            String jsonString = chatCompletions.getChoices().get(0).getMessage().getContent();

            try {
                return objectMapper.readValue(jsonString, RecipeResponseDto.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("GPT 응답 JSON 파싱 실패. 응답 내용: " + jsonString, e);
            }
        }

        throw new RuntimeException("GPT 응답에서 레시피를 찾을 수 없습니다.");
    }
}