package com.gasmanager.ia.controllers;

import com.gasmanager.ia.dto.ChatRequestDTO;
import com.gasmanager.ia.dto.ChatResponseDTO;
import com.gasmanager.ia.services.OllamaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ia")
@RequiredArgsConstructor
public class IAController {

    private final OllamaService ollamaService;

    @PostMapping("/chat")
    public ResponseEntity<ChatResponseDTO> chat(@Valid @RequestBody ChatRequestDTO request) {
        return ResponseEntity.ok(ollamaService.consultarIA(request));
    }
}
