package com.org.myMealScanner.customVision.controller;

import com.org.myMealScanner.customVision.service.CustomVisionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/vision")
public class CustomVisionController {

    @Autowired
    private CustomVisionService customVisionService;

    @PostMapping(value = "/image-detect")
    public ResponseEntity<?> classifyImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            byte[] imageBytes = file.getBytes();  // MultipartFile을 byte[]로 변환
            Map<String, Object> predictions = customVisionService.classifyImage(imageBytes);
            return ResponseEntity.ok(predictions);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}