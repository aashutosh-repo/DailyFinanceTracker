package com.finance.tracker.ai.controller;

import com.finance.tracker.ai.service.AiResearchProxyService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiResearchController {

    private final AiResearchProxyService proxyService;

    @PostMapping("/chat")
    public ResponseEntity<JsonNode> chat(@Valid @RequestBody AiQuestionRequest request) {
        return ResponseEntity.ok(proxyService.chat(request.question()));
    }

    @PostMapping("/chat/job")
    public ResponseEntity<JsonNode> submitJob(@Valid @RequestBody AiQuestionRequest request) {
        return ResponseEntity.accepted().body(proxyService.submitJob(request.question()));
    }

    @GetMapping("/chat/job/{jobId}")
    public ResponseEntity<JsonNode> getJob(@PathVariable String jobId) {
        return ResponseEntity.ok(proxyService.getJob(jobId));
    }

    public record AiQuestionRequest(@NotBlank String question) {
    }
}