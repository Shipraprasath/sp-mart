package com.spmart.controller;

import com.spmart.dto.AiChatMessageDto;
import com.spmart.dto.AiResponseDto;
import com.spmart.service.AiAssistantService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AiAssistantController {

    private final AiAssistantService aiAssistantService;

    public AiAssistantController(AiAssistantService aiAssistantService) {
        this.aiAssistantService = aiAssistantService;
    }

    @PostMapping({"/assistant/chat", "/chat"})
    public ResponseEntity<AiResponseDto> chat(@RequestBody AiChatMessageDto request) {
        AiResponseDto response = aiAssistantService.processChat(request);
        return ResponseEntity.ok(response);
    }
}
