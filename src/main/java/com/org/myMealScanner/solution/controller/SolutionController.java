package com.org.myMealScanner.solution.controller;

import com.org.myMealScanner.solution.dto.NutritionRequestDto;
import com.org.myMealScanner.solution.dto.NutritionSolutionResponse;
import com.org.myMealScanner.solution.service.SolutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/solution")
@RequiredArgsConstructor
public class SolutionController {
    private final SolutionService solutionService;

    @PostMapping("/general")
    public NutritionSolutionResponse getAdvice(@RequestBody NutritionRequestDto request) throws Exception {
        return solutionService.getAdvice(request);
    }

    @PostMapping("")
    public NutritionSolutionResponse getAdviceWithRag(@RequestBody NutritionRequestDto request) throws Exception {
        return solutionService.getAdviceWithRag(request);
    }

}
