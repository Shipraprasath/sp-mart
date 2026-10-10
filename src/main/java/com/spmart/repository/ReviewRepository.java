package com.spmart.repository;

import com.spmart.entity.Product;
import com.spmart.entity.Review;
import com.spmart.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByProductOrderByCreatedAtDesc(Product product);

    List<Review> findByProductAndApprovedTrueOrderByCreatedAtDesc(Product product);

    Optional<Review> findByUserAndProduct(User user, Product product);

    boolean existsByUserAndProduct(User user, Product product);

    Page<Review> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<Review> findByApprovedOrderByCreatedAtDesc(boolean approved, Pageable pageable);

    List<Review> findByProductSellerOrderByCreatedAtDesc(User seller);

    long countByProduct(Product product);
}
