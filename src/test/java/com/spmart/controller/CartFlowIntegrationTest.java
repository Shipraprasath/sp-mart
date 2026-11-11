package com.spmart.controller;

import com.spmart.entity.Address;
import com.spmart.entity.Product;
import com.spmart.entity.User;
import com.spmart.repository.AddressRepository;
import com.spmart.repository.ProductRepository;
import com.spmart.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CartFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AddressRepository addressRepository;

    private Product inStockProduct;

    @BeforeEach
    void setUp() {
        inStockProduct = productRepository.findAll().stream()
                .filter(p -> p.isInStock() && p.getStock() != null && p.getStock() >= 5)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No seeded in-stock product found"));
    }

    @Test
    void homeCatalogAndDetailsRenderBothAddToCartAndBuyNow() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Add to Cart")))
                .andExpect(content().string(containsString("Buy Now")))
                .andExpect(content().string(containsString("/cart/buy-now")));

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Add to Cart")))
                .andExpect(content().string(containsString("Buy Now")));

        mockMvc.perform(get("/products/" + inStockProduct.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Add to Cart")))
                .andExpect(content().string(containsString("Buy Now")))
                .andExpect(content().string(containsString("/cart/buy-now")));
    }

    @Test
    @WithUserDetails("buyer@spmart.com")
    void continuousAddToCartDoesNotRedirectToCartAndIncrementsCounts() throws Exception {
        mockMvc.perform(post("/cart/clear").with(csrf()))
                .andExpect(status().is3xxRedirection());

        // 1st AJAX add keeps user on page and returns updated counts
        mockMvc.perform(post("/cart/add")
                        .with(csrf())
                        .header("X-Requested-With", "XMLHttpRequest")
                        .param("productId", String.valueOf(inStockProduct.getId()))
                        .param("quantity", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.cartItemCount").value(1))
                .andExpect(jsonPath("$.productQuantity").value(1));

        // 2nd AJAX add continuously increments quantity without redirecting to /cart
        mockMvc.perform(post("/cart/add")
                        .with(csrf())
                        .header("X-Requested-With", "XMLHttpRequest")
                        .param("productId", String.valueOf(inStockProduct.getId()))
                        .param("quantity", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.cartItemCount").value(2))
                .andExpect(jsonPath("$.productQuantity").value(2));

        // Non-AJAX form submit stays on Referer instead of jumping to /cart
        mockMvc.perform(post("/cart/add")
                        .with(csrf())
                        .header("Referer", "http://localhost/products")
                        .param("productId", String.valueOf(inStockProduct.getId()))
                        .param("quantity", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/products"));
    }

    @Test
    @WithUserDetails("buyer@spmart.com")
    void buyNowAndPlaceOrderShowsOrderConfirmedBanner() throws Exception {
        mockMvc.perform(post("/cart/clear").with(csrf()))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(post("/cart/buy-now")
                        .with(csrf())
                        .param("productId", String.valueOf(inStockProduct.getId()))
                        .param("quantity", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/checkout"));

        User buyer = userRepository.findByEmail("buyer@spmart.com").orElseThrow();
        Address address = addressRepository.findByUserOrderByIsDefaultDescCreatedAtDesc(buyer)
                .stream()
                .findFirst()
                .orElseThrow();

        MvcResult orderResult = mockMvc.perform(post("/checkout/place-order")
                        .with(csrf())
                        .param("addressId", String.valueOf(address.getId()))
                        .param("paymentMethod", "COD"))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        String confirmationUrl = orderResult.getResponse().getRedirectedUrl();
        mockMvc.perform(get(confirmationUrl))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("ORDER CONFIRMED")));
    }
}
