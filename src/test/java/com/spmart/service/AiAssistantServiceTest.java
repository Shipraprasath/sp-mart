package com.spmart.service;

import com.spmart.dto.AiChatMessageDto;
import com.spmart.dto.AiResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class AiAssistantServiceTest {

    @Autowired
    private AiAssistantService aiAssistantService;

    @Test
    @DisplayName("AI Assistant handles budget query for smartphones")
    void testBudgetQuery() {
        AiChatMessageDto query = new AiChatMessageDto("Find smartphones under 30000");
        AiResponseDto response = aiAssistantService.processChat(query);

        assertNotNull(response);
        assertNotNull(response.getReply());
        assertNotNull(response.getProducts());
    }

    @Test
    @DisplayName("AI Assistant handles device comparison request")
    void testDeviceComparison() {
        AiChatMessageDto query = new AiChatMessageDto("Compare iPhone 15 Pro vs Galaxy S24 Ultra");
        AiResponseDto response = aiAssistantService.processChat(query);

        assertNotNull(response);
        assertNotNull(response.getReply());
        assertTrue(response.isComparisonMode());
        assertTrue(response.getReply().toLowerCase().contains("comparison") || response.getReply().contains("Apple"));
    }

    @Test
    @DisplayName("AI Assistant explains technical concepts like OLED vs QLED")
    void testSpecExplanation() {
        AiChatMessageDto query = new AiChatMessageDto("Explain OLED vs QLED display technology");
        AiResponseDto response = aiAssistantService.processChat(query);

        assertNotNull(response);
        assertNotNull(response.getReply());
        assertTrue(response.getReply().toLowerCase().contains("oled") || response.getReply().toLowerCase().contains("pixel"));
    }

    @Test
    @DisplayName("AI Assistant handles general electronics greeting and recommendations")
    void testGeneralGreeting() {
        AiChatMessageDto query = new AiChatMessageDto("Hello! What are your top electronics deals?");
        AiResponseDto response = aiAssistantService.processChat(query);

        assertNotNull(response);
        assertNotNull(response.getReply());
    }
}
