package com.spmart.service;

import com.spmart.entity.NewsletterSubscription;
import com.spmart.repository.NewsletterSubscriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class NewsletterService {

    private final NewsletterSubscriptionRepository subscriptionRepository;

    public NewsletterService(NewsletterSubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    public boolean subscribe(String email) {
        String cleanEmail = email.trim().toLowerCase();
        if (subscriptionRepository.existsByEmail(cleanEmail)) {
            return false; // Already subscribed
        }
        subscriptionRepository.save(new NewsletterSubscription(cleanEmail));
        return true;
    }
}
