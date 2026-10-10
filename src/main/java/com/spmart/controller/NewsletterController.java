package com.spmart.controller;

import com.spmart.service.NewsletterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/newsletter")
public class NewsletterController {

    private final NewsletterService newsletterService;

    public NewsletterController(NewsletterService newsletterService) {
        this.newsletterService = newsletterService;
    }

    @PostMapping("/subscribe")
    public ResponseEntity<Map<String, Object>> subscribe(@RequestParam("email") String email) {
        if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Please enter a valid email address."
            ));
        }

        boolean newlySubscribed = newsletterService.subscribe(email);
        if (newlySubscribed) {
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Thank you for subscribing! Check your inbox for exclusive electronics deals."
            ));
        } else {
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "You are already subscribed to SP MART updates!"
            ));
        }
    }
}
