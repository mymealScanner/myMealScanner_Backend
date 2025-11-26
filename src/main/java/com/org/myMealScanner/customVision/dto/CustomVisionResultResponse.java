package com.org.myMealScanner.customVision.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class CustomVisionResultResponse {
    private String when;
    private String foodName;
    private Object prediction;
}
