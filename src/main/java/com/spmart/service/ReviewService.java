package com.spmart.service;

import com.spmart.dto.ReviewDto;
import com.spmart.entity.Product;
import com.spmart.entity.Review;
import com.spmart.entity.User;
import com.spmart.exception.BusinessRuleException;
import com.spmart.exception.ResourceNotFoundException;
import com.spmart.repository.ProductRepository;
import com.spmart.repository.ReviewRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@Transactional
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;

    public ReviewService(ReviewRepository reviewRepository, ProductRepository productRepository) {
        this.reviewRepository = reviewRepository;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public List<Review> getProductReviews(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
        return reviewRepository.findByProductAndApprovedTrueOrderByCreatedAtDesc(product);
    }

    public void approveReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId));
        review.setApproved(true);
        reviewRepository.save(review);
        recalculateProductRating(review.getProduct());
    }

    public void rejectReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId));
        review.setApproved(false);
        reviewRepository.save(review);
        recalculateProductRating(review.getProduct());
    }

    @Transactional(readOnly = true)
    public List<Review> getSellerReviews(User seller) {
        return reviewRepository.findByProductSellerOrderByCreatedAtDesc(seller);
    }

    @Transactional(readOnly = true)
    public boolean canUserReviewProduct(User user, Long productId) {
        if (user == null) return false;
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) return false;
        return !reviewRepository.existsByUserAndProduct(user, product);
    }

    public Review addReview(User user, ReviewDto dto) {
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + dto.getProductId()));

        if (reviewRepository.existsByUserAndProduct(user, product)) {
            throw new BusinessRuleException("You have already submitted a review for this product.");
        }

        Review review = new Review(
                user,
                product,
                dto.getRating(),
                dto.getTitle().trim(),
                dto.getComment().trim()
        );
        Review saved = reviewRepository.save(review);

        // Recalculate average rating and review count
        recalculateProductRating(product);

        return saved;
    }

    public void deleteReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId));
        Product product = review.getProduct();
        reviewRepository.delete(review);
        recalculateProductRating(product);
    }

    @Transactional(readOnly = true)
    public Page<Review> getAllReviews(Pageable pageable) {
        return reviewRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    private void recalculateProductRating(Product product) {
        List<Review> reviews = reviewRepository.findByProductOrderByCreatedAtDesc(product);
        if (reviews.isEmpty()) {
            product.setAverageRating(0.0);
            product.setReviewCount(0);
        } else {
            double sum = 0.0;
            for (Review r : reviews) {
                sum += r.getRating();
            }
            double avg = BigDecimal.valueOf(sum / reviews.size())
                    .setScale(1, RoundingMode.HALF_UP)
                    .doubleValue();
            product.setAverageRating(avg);
            product.setReviewCount(reviews.size());
        }
        productRepository.save(product);
    }
}
