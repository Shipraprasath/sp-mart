package com.spmart.repository;

import com.spmart.entity.SellerProfile;
import com.spmart.entity.SellerStatus;
import com.spmart.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SellerProfileRepository extends JpaRepository<SellerProfile, Long> {

    Optional<SellerProfile> findByUser(User user);

    Optional<SellerProfile> findByUserId(Long userId);

    List<SellerProfile> findByStatus(SellerStatus status);

    Page<SellerProfile> findByStatus(SellerStatus status, Pageable pageable);

    Page<SellerProfile> findAllByOrderByCreatedAtDesc(Pageable pageable);

    long countByStatus(SellerStatus status);

    boolean existsByGstin(String gstin);

    boolean existsByStoreName(String storeName);

    @Query("SELECT s FROM SellerProfile s WHERE " +
           "(:status IS NULL OR s.status = :status) AND " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           " LOWER(s.storeName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(s.businessEmail) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(s.user.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<SellerProfile> searchSellers(
            @Param("keyword") String keyword,
            @Param("status") SellerStatus status,
            Pageable pageable
    );
}
