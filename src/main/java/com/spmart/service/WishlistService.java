package com.spmart.service;

import com.spmart.entity.Product;
import com.spmart.entity.User;
import com.spmart.entity.WishlistItem;
import com.spmart.exception.BusinessRuleException;
import com.spmart.repository.WishlistItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class WishlistService {

    private final WishlistItemRepository wishlistRepository;
    private final ProductService productService;
    private final CartService cartService;

    public WishlistService(WishlistItemRepository wishlistRepository,
                           ProductService productService,
                           CartService cartService) {
        this.wishlistRepository = wishlistRepository;
        this.productService = productService;
        this.cartService = cartService;
    }

    @Transactional(readOnly = true)
    public List<WishlistItem> getUserWishlist(User user) {
        return wishlistRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public void addToWishlist(User user, Long productId) {
        Product product = productService.getProductById(productId);
        if (!wishlistRepository.existsByUserAndProduct(user, product)) {
            WishlistItem item = new WishlistItem(user, product);
            wishlistRepository.save(item);
        }
    }

    public void removeFromWishlist(User user, Long productId) {
        Product product = productService.getProductById(productId);
        wishlistRepository.deleteByUserAndProduct(user, product);
    }

    public void moveToCart(User user, Long productId) {
        Product product = productService.getProductById(productId);
        if (!product.isInStock()) {
            throw new BusinessRuleException("Product is currently out of stock.");
        }
        cartService.addToCart(user, productId, 1);
        wishlistRepository.deleteByUserAndProduct(user, product);
    }

    @Transactional(readOnly = true)
    public boolean isProductInWishlist(User user, Long productId) {
        Product product = productService.getProductById(productId);
        return wishlistRepository.existsByUserAndProduct(user, product);
    }

    @Transactional(readOnly = true)
    public long getWishlistCount(User user) {
        return wishlistRepository.countByUser(user);
    }
}
