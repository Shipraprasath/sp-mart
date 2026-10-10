package com.spmart.service;

import com.spmart.dto.SellerRegistrationDto;
import com.spmart.entity.*;
import com.spmart.exception.BusinessRuleException;
import com.spmart.exception.ResourceNotFoundException;
import com.spmart.repository.ProductRepository;
import com.spmart.repository.ReviewRepository;
import com.spmart.repository.SellerProfileRepository;
import com.spmart.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class SellerService {

    private final UserRepository userRepository;
    private final SellerProfileRepository sellerProfileRepository;
    private final ProductRepository productRepository;
    private final ReviewRepository reviewRepository;
    private final OrderService orderService;
    private final PasswordEncoder passwordEncoder;

    public SellerService(UserRepository userRepository,
                         SellerProfileRepository sellerProfileRepository,
                         ProductRepository productRepository,
                         ReviewRepository reviewRepository,
                         OrderService orderService,
                         PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.sellerProfileRepository = sellerProfileRepository;
        this.productRepository = productRepository;
        this.reviewRepository = reviewRepository;
        this.orderService = orderService;
        this.passwordEncoder = passwordEncoder;
    }

    public SellerProfile registerSeller(SellerRegistrationDto dto) {
        String email = dto.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new BusinessRuleException("An account with email " + email + " already exists.");
        }

        if (sellerProfileRepository.existsByStoreName(dto.getStoreName().trim())) {
            throw new BusinessRuleException("Store name '" + dto.getStoreName() + "' is already registered.");
        }

        User user = new User(
                dto.getFullName().trim(),
                email,
                passwordEncoder.encode(dto.getPassword()),
                dto.getPhone().trim(),
                Role.ROLE_SELLER
        );
        user = userRepository.save(user);

        SellerProfile profile = new SellerProfile(
                user,
                dto.getStoreName().trim(),
                email,
                dto.getPhone().trim(),
                dto.getGstin() != null ? dto.getGstin().trim() : null,
                dto.getBusinessAddress() != null ? dto.getBusinessAddress().trim() : null,
                dto.getDescription() != null ? dto.getDescription().trim() : null,
                SellerStatus.PENDING_APPROVAL
        );

        return sellerProfileRepository.save(profile);
    }

    @Transactional(readOnly = true)
    public SellerProfile getProfileByUser(User user) {
        return sellerProfileRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Seller profile not found for user: " + user.getEmail()));
    }

    @Transactional(readOnly = true)
    public Optional<SellerProfile> findProfileByUser(User user) {
        return sellerProfileRepository.findByUser(user);
    }

    @Transactional(readOnly = true)
    public boolean isApprovedSeller(User user) {
        return sellerProfileRepository.findByUser(user)
                .map(SellerProfile::isApproved)
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public Page<SellerProfile> getAllSellers(Pageable pageable) {
        return sellerProfileRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    @Transactional(readOnly = true)
    public Page<SellerProfile> searchSellers(String keyword, SellerStatus status, Pageable pageable) {
        return sellerProfileRepository.searchSellers(keyword, status, pageable);
    }

    public SellerProfile approveSeller(Long profileId) {
        SellerProfile profile = sellerProfileRepository.findById(profileId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller profile not found with id: " + profileId));
        profile.setStatus(SellerStatus.APPROVED);
        profile.setRejectionReason(null);
        return sellerProfileRepository.save(profile);
    }

    public SellerProfile rejectSeller(Long profileId, String reason) {
        SellerProfile profile = sellerProfileRepository.findById(profileId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller profile not found with id: " + profileId));
        profile.setStatus(SellerStatus.REJECTED);
        profile.setRejectionReason(reason != null ? reason : "Application does not meet marketplace standards.");
        return sellerProfileRepository.save(profile);
    }

    public SellerProfile suspendSeller(Long profileId, String reason) {
        SellerProfile profile = sellerProfileRepository.findById(profileId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller profile not found with id: " + profileId));
        profile.setStatus(SellerStatus.SUSPENDED);
        profile.setRejectionReason(reason != null ? reason : "Account suspended by administrator.");
        return sellerProfileRepository.save(profile);
    }

    @Transactional(readOnly = true)
    public long getPendingSellerCount() {
        return sellerProfileRepository.countByStatus(SellerStatus.PENDING_APPROVAL);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getSellerDashboardMetrics(User seller) {
        Map<String, Object> metrics = new HashMap<>();
        long totalProducts = productRepository.countBySeller(seller);
        long lowStockCount = productRepository.countBySellerAndStockLessThanEqualAndActiveTrue(seller, 10);
        long totalOrders = orderService.getSellerOrderCount(seller);
        long pendingOrders = orderService.getSellerPendingOrderCount(seller);
        BigDecimal totalRevenue = orderService.getSellerRevenue(seller);

        metrics.put("totalProducts", totalProducts);
        metrics.put("lowStockCount", lowStockCount);
        metrics.put("totalOrders", totalOrders);
        metrics.put("pendingOrders", pendingOrders);
        metrics.put("totalRevenue", totalRevenue != null ? totalRevenue : BigDecimal.ZERO);

        SellerProfile profile = sellerProfileRepository.findByUser(seller).orElse(null);
        metrics.put("sellerRating", profile != null ? profile.getRating() : 4.8);
        metrics.put("storeName", profile != null ? profile.getStoreName() : seller.getFullName());

        return metrics;
    }
}
