package com.spmart.service;

import com.spmart.entity.Cart;
import com.spmart.entity.CartItem;
import com.spmart.entity.Product;
import com.spmart.entity.User;
import com.spmart.exception.BusinessRuleException;
import com.spmart.exception.InsufficientStockException;
import com.spmart.exception.ResourceNotFoundException;
import com.spmart.repository.CartItemRepository;
import com.spmart.repository.CartRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@Transactional
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductService productService;

    @Value("${spmart.shipping.free-threshold:500.00}")
    private BigDecimal freeShippingThreshold;

    @Value("${spmart.shipping.flat-rate:50.00}")
    private BigDecimal flatShippingRate;

    public CartService(CartRepository cartRepository,
                       CartItemRepository cartItemRepository,
                       ProductService productService) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productService = productService;
    }

    public Cart getOrCreateCart(User user) {
        return cartRepository.findByUser(user)
                .orElseGet(() -> cartRepository.save(new Cart(user)));
    }

    public Cart addToCart(User user, Long productId, int quantity) {
        if (quantity <= 0) {
            throw new BusinessRuleException("Quantity must be at least 1.");
        }

        Product product = productService.getProductById(productId);

        if (!product.isActive()) {
            throw new BusinessRuleException("This product is currently inactive and cannot be purchased.");
        }

        if (product.getStock() < quantity) {
            throw new InsufficientStockException("Only " + product.getStock() + " units available in stock.");
        }

        Cart cart = getOrCreateCart(user);

        Optional<CartItem> existingItemOpt = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();

        if (existingItemOpt.isPresent()) {
            CartItem existingItem = existingItemOpt.get();
            int newQuantity = existingItem.getQuantity() + quantity;
            if (newQuantity > product.getStock()) {
                throw new InsufficientStockException("Cannot add more. You already have " + existingItem.getQuantity() 
                        + " in cart, and available stock is " + product.getStock() + ".");
            }
            existingItem.setQuantity(newQuantity);
            existingItem.setUnitPrice(product.getEffectivePrice());
            cartItemRepository.save(existingItem);
        } else {
            CartItem newItem = new CartItem(cart, product, quantity, product.getEffectivePrice());
            cart.getItems().add(newItem);
            cartItemRepository.save(newItem);
        }

        return cartRepository.save(cart);
    }

    public Cart updateQuantity(User user, Long cartItemId, int quantity) {
        Cart cart = getOrCreateCart(user);

        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new BusinessRuleException("Unauthorized modification of cart item.");
        }

        if (quantity <= 0) {
            cart.getItems().remove(item);
            cartItemRepository.delete(item);
        } else {
            Product product = item.getProduct();
            if (!product.isActive()) {
                throw new BusinessRuleException("Product is inactive and has been removed.");
            }
            if (quantity > product.getStock()) {
                throw new InsufficientStockException("Only " + product.getStock() + " units in stock.");
            }
            item.setQuantity(quantity);
            item.setUnitPrice(product.getEffectivePrice());
            cartItemRepository.save(item);
        }

        return cartRepository.save(cart);
    }

    public Cart removeFromCart(User user, Long cartItemId) {
        Cart cart = getOrCreateCart(user);
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new BusinessRuleException("Unauthorized modification of cart item.");
        }

        cart.getItems().remove(item);
        cartItemRepository.delete(item);
        return cartRepository.save(cart);
    }

    public void clearCart(User user) {
        Cart cart = getOrCreateCart(user);
        cart.getItems().clear();
        cartItemRepository.deleteByCart(cart);
        cartRepository.save(cart);
    }

    public BigDecimal calculateShippingFee(BigDecimal subtotal) {
        if (subtotal == null || subtotal.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        if (subtotal.compareTo(freeShippingThreshold) >= 0) {
            return BigDecimal.ZERO;
        }
        return flatShippingRate;
    }

    public BigDecimal getFreeShippingThreshold() {
        return freeShippingThreshold;
    }

    public BigDecimal getFlatShippingRate() {
        return flatShippingRate;
    }
}
