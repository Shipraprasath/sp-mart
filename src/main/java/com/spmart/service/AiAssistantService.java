package com.spmart.service;

import com.spmart.dto.AiChatMessageDto;
import com.spmart.dto.AiProductCardDto;
import com.spmart.dto.AiResponseDto;
import com.spmart.entity.Category;
import com.spmart.entity.Product;
import com.spmart.repository.CategoryRepository;
import com.spmart.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Value;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AiAssistantService {

    private static final Logger log = LoggerFactory.getLogger(AiAssistantService.class);

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Value("${spmart.ai.api-key:}")
    private String aiApiKey;

    @Value("${spmart.ai.model:gemini-1.5-flash}")
    private String aiModel;

    public AiAssistantService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    public String getEngineModeDescription() {
        if (aiApiKey != null && !aiApiKey.isBlank()) {
            return "SP MART Generative AI Engine (" + aiModel + " + Live Catalogue Grounding)";
        }
        return "SP MART Rule-Based Catalogue Intelligence (Grounded Fallback)";
    }

    public AiResponseDto processChat(AiChatMessageDto request) {
        String query = request.getMessage() != null ? request.getMessage().trim() : "";
        if (query.isEmpty()) {
            AiResponseDto emptyResponse = new AiResponseDto("Hi! I'm your SP MART shopping assistant. Tell me your budget, preferred brand, or the device you're looking for.");
            emptyResponse.setEngineMode(getEngineModeDescription());
            emptyResponse.setSuggestedPrompts(getDefaultPrompts());
            return emptyResponse;
        }

        String lowerQuery = query.toLowerCase();

        // 1. Check for comparison query (e.g. "compare X and Y", "X vs Y")
        if (lowerQuery.contains(" vs ") || lowerQuery.contains(" versus ") || lowerQuery.contains("compare")) {
            AiResponseDto comparisonResponse = handleComparisonQuery(lowerQuery, query);
            if (comparisonResponse != null) {
                return comparisonResponse;
            }
        }

        // 2. Check for technical concept / specification explanation
        if (isSpecExplanationQuery(lowerQuery)) {
            AiResponseDto specResponse = handleSpecExplanation(lowerQuery);
            if (specResponse != null) {
                return specResponse;
            }
        }

        // 3. Extract budget, brand, and category
        BigDecimal budget = extractBudget(lowerQuery);
        String detectedBrand = extractBrand(lowerQuery);
        Long categoryId = extractCategoryId(lowerQuery);

        // 4. Query grounded catalogue products
        List<Product> matchedProducts = productRepository.searchForAiAssistant(
                extractCleanSearchTerm(lowerQuery, detectedBrand),
                null,
                budget,
                categoryId,
                detectedBrand,
                PageRequest.of(0, 4)
        );

        if (matchedProducts.isEmpty() && budget != null) {
            // Try without budget limit to provide alternatives
            matchedProducts = productRepository.searchForAiAssistant(
                    null,
                    null,
                    null,
                    categoryId,
                    detectedBrand,
                    PageRequest.of(0, 3)
            );
        }

        if (matchedProducts.isEmpty()) {
            // Fallback to top featured products
            matchedProducts = productRepository.findTop8ByActiveTrueOrderByAverageRatingDesc()
                    .stream().limit(3).collect(Collectors.toList());
        }

        AiResponseDto response = new AiResponseDto();
        response.setProducts(matchedProducts.stream().map(this::toCardDto).collect(Collectors.toList()));

        // Build conversational reply
        StringBuilder reply = new StringBuilder();
        if (budget != null && detectedBrand != null) {
            reply.append("Here are the top **").append(detectedBrand).append("** options within your budget of **₹")
                    .append(String.format("%,.0f", budget)).append("** available at SP MART:\n\n");
        } else if (budget != null) {
            reply.append("Here are our top-rated recommendations within your budget of **₹")
                    .append(String.format("%,.0f", budget)).append("**:\n\n");
        } else if (detectedBrand != null) {
            reply.append("Here are the best **").append(detectedBrand).append("** devices currently in stock:\n\n");
        } else {
            reply.append("Based on our catalogue, here are the best matching electronics for your search:\n\n");
        }

        for (Product p : matchedProducts) {
            reply.append("• **").append(p.getName()).append("** (₹")
                    .append(String.format("%,.0f", p.getEffectivePrice())).append(")");
            if (p.hasDiscount()) {
                reply.append(" — *").append(p.getDiscountPercentage()).append("% OFF*");
            }
            if (p.getSpecifications() != null && !p.getSpecifications().isBlank()) {
                String firstSpec = p.getSpecifications().split("\\|")[0].trim();
                reply.append(". Key highlight: ").append(firstSpec);
            }
            reply.append("\n");
        }

        reply.append("\n💡 *Need more details? Tap any card below to view full specifications, customer reviews, or add directly to your cart.*");
        response.setEngineMode(getEngineModeDescription());
        response.setReply(reply.toString());
        response.setSuggestedPrompts(generateContextualPrompts(matchedProducts, detectedBrand, categoryId));
        return response;
    }

    private AiResponseDto handleComparisonQuery(String lowerQuery, String originalQuery) {
        List<Product> allActive = productRepository.findAll();
        if (allActive.size() < 2) {
            allActive = productRepository.findTop8ByActiveTrueOrderByAverageRatingDesc();
        }

        Product prodA = null;
        Product prodB = null;

        // Try to identify products mentioned in query
        for (Product p : allActive) {
            if (!p.isActive()) continue;
            String pName = p.getName().toLowerCase();
            String pBrand = p.getBrand().toLowerCase();

            boolean matches = lowerQuery.contains(pBrand);
            if (!matches) {
                for (String token : pName.split("\\s+")) {
                    if (token.length() >= 4 && lowerQuery.contains(token)) {
                        matches = true;
                        break;
                    }
                }
            }

            if (matches) {
                if (prodA == null) {
                    prodA = p;
                } else if (!p.getId().equals(prodA.getId())) {
                    prodB = p;
                    break;
                }
            }
        }

        // Fallback to active catalog items if comparison requested
        if (prodA == null && allActive.size() >= 2) {
            prodA = allActive.get(0);
            prodB = allActive.get(1);
        } else if (prodA != null && prodB == null && allActive.size() >= 2) {
            for (Product p : allActive) {
                if (p.isActive() && !p.getId().equals(prodA.getId())) {
                    prodB = p;
                    break;
                }
            }
        }

        if (prodA != null && prodB != null) {
            AiResponseDto resp = new AiResponseDto();
            resp.setComparisonMode(true);
            resp.setProductA(toCardDto(prodA));
            resp.setProductB(toCardDto(prodB));
            resp.setProducts(List.of(toCardDto(prodA), toCardDto(prodB)));

            StringBuilder sb = new StringBuilder();
            sb.append("### ⚖️ Side-by-Side Comparison\n\n");
            sb.append("| Feature | **").append(prodA.getName()).append("** | **").append(prodB.getName()).append("** |\n");
            sb.append("|---|---|---|\n");
            sb.append("| **Brand** | ").append(prodA.getBrand()).append(" | ").append(prodB.getBrand()).append(" |\n");
            sb.append("| **Effective Price** | ₹").append(String.format("%,.0f", prodA.getEffectivePrice())).append(" | ₹").append(String.format("%,.0f", prodB.getEffectivePrice())).append(" |\n");
            sb.append("| **Discount** | ").append(prodA.getDiscountPercentage()).append("% OFF | ").append(prodB.getDiscountPercentage()).append("% OFF |\n");
            sb.append("| **Rating** | ⭐ ").append(prodA.getAverageRating()).append(" (").append(prodA.getReviewCount()).append(" reviews) | ⭐ ")
                    .append(prodB.getAverageRating()).append(" (").append(prodB.getReviewCount()).append(" reviews) |\n");
            sb.append("| **Stock Status** | ").append(prodA.isInStock() ? "In Stock" : "Out of Stock").append(" | ")
                    .append(prodB.isInStock() ? "In Stock" : "Out of Stock").append(" |\n\n");

            sb.append("**Our AI Recommendation:** ");
            if (prodA.getEffectivePrice().compareTo(prodB.getEffectivePrice()) < 0) {
                sb.append("**").append(prodA.getName()).append("** offers higher price-to-performance value, saving you ₹")
                        .append(String.format("%,.0f", prodB.getEffectivePrice().subtract(prodA.getEffectivePrice())))
                        .append(". Choose **").append(prodB.getName()).append("** if you prioritize brand ecosystem or specific premium specs.");
            } else {
                sb.append("**").append(prodB.getName()).append("** is more budget-friendly. However, **")
                        .append(prodA.getName()).append("** delivers premium specifications and build quality.");
            }

            resp.setEngineMode(getEngineModeDescription());
            resp.setReply(sb.toString());
            resp.setSuggestedPrompts(List.of("Show accessories for " + prodA.getBrand(), "Filter under ₹50,000", "Top rated electronics"));
            return resp;
        }

        return null;
    }

    private boolean isSpecExplanationQuery(String query) {
        return query.contains("what is") || query.contains("explain") || query.contains("difference between")
                || query.contains("oled") || query.contains("inverter") || query.contains("refresh rate")
                || query.contains("ram") || query.contains("ssd");
    }

    private AiResponseDto handleSpecExplanation(String query) {
        StringBuilder explanation = new StringBuilder();
        String searchCategory = null;

        if (query.contains("oled") || query.contains("qled")) {
            explanation.append("### 📺 OLED vs QLED Technology Explained\n\n")
                    .append("• **OLED (Organic LED):** Each pixel emits its own light, delivering absolute true blacks, infinite contrast, and ultra-wide viewing angles. Ideal for movies and dark rooms.\n")
                    .append("• **QLED (Quantum Dot LED):** Uses Quantum Dots with a high-intensity backlight for extreme brightness and vibrant color saturation. Ideal for bright daytime living rooms.\n\n")
                    .append("Here are premium Smart TVs at SP MART featuring advanced display panels:");
            searchCategory = "televisions";
        } else if (query.contains("inverter")) {
            explanation.append("### ❄️ Inverter vs Non-Inverter Technology\n\n")
                    .append("• **Inverter Compressors** adjust motor speed dynamically based on ambient room temperature. This saves up to 30-50% electricity, operates silently, and prolongs lifespan.\n")
                    .append("• **Non-Inverter Compressors** run at fixed full speed and switch on/off repeatedly, causing power spikes and higher energy bills.\n\n")
                    .append("Here are energy-efficient inverter appliances available at SP MART:");
            searchCategory = "air-conditioners";
        } else if (query.contains("ssd") || query.contains("ram")) {
            explanation.append("### 💻 RAM vs SSD Explained\n\n")
                    .append("• **RAM (Memory):** Temporary high-speed workspace for running apps, browser tabs, and gaming simultaneously. 16GB is the sweet spot for modern multitaskers.\n")
                    .append("• **SSD (Storage):** Permanent storage that loads Windows, games, and large files up to 10x faster than traditional hard drives.\n\n")
                    .append("Check out high-performance laptops configured with 16GB+ RAM and NVMe SSDs:");
            searchCategory = "laptops-computers";
        } else {
            return null;
        }

        Long catId = null;
        if (searchCategory != null) {
            Optional<Category> cat = categoryRepository.findBySlug(searchCategory);
            if (cat.isPresent()) {
                catId = cat.get().getId();
            }
        }

        List<Product> products = productRepository.searchForAiAssistant(null, null, null, catId, null, PageRequest.of(0, 3));
        AiResponseDto resp = new AiResponseDto(explanation.toString());
        resp.setEngineMode(getEngineModeDescription());
        resp.setProducts(products.stream().map(this::toCardDto).collect(Collectors.toList()));
        resp.setSuggestedPrompts(List.of("Best laptops under ₹70,000", "Top 55-inch 4K TVs", "Energy-saving ACs"));
        return resp;
    }

    private BigDecimal extractBudget(String query) {
        Pattern pattern = Pattern.compile("(?:under|below|less than|budget|within|upto|up to)\\s*(?:rs\\.?|inr|₹)?\\s*([0-9]+(?:,[0-9]+)*)(?:\\s*(k|thousand|lakh|lac))?", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(query);
        if (matcher.find()) {
            String numStr = matcher.group(1).replace(",", "");
            String unit = matcher.group(2);
            try {
                double val = Double.parseDouble(numStr);
                if (unit != null) {
                    unit = unit.toLowerCase();
                    if (unit.equals("k") || unit.startsWith("thous")) {
                        val *= 1000;
                    } else if (unit.startsWith("lak") || unit.startsWith("lac")) {
                        val *= 100000;
                    }
                } else if (val < 200) {
                    val *= 1000; // e.g. "under 50" -> 50,000
                }
                return BigDecimal.valueOf(val);
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private String extractBrand(String query) {
        String[] brands = {"Apple", "Samsung", "Sony", "Dell", "HP", "Lenovo", "LG", "Realme", "boAt", "Whirlpool", "IFB", "Prestige", "Philips", "Xiaomi", "OnePlus", "Motorola", "Vivo", "Oppo"};
        for (String b : brands) {
            if (query.contains(b.toLowerCase())) {
                return b;
            }
        }
        return null;
    }

    private Long extractCategoryId(String query) {
        Map<String, String> categoryKeywords = Map.ofEntries(
                Map.entry("phone", "smartphones"),
                Map.entry("smartphone", "smartphones"),
                Map.entry("mobile", "smartphones"),
                Map.entry("laptop", "laptops-computers"),
                Map.entry("computer", "laptops-computers"),
                Map.entry("macbook", "laptops-computers"),
                Map.entry("pc", "laptops-computers"),
                Map.entry("tv", "televisions"),
                Map.entry("television", "televisions"),
                Map.entry("ac", "air-conditioners"),
                Map.entry("air conditioner", "air-conditioners"),
                Map.entry("fridge", "refrigerators"),
                Map.entry("refrigerator", "refrigerators"),
                Map.entry("washing machine", "washing-machines"),
                Map.entry("speaker", "speakers"),
                Map.entry("soundbar", "speakers"),
                Map.entry("headphone", "headphones-earbuds"),
                Map.entry("earbuds", "headphones-earbuds"),
                Map.entry("earphone", "headphones-earbuds"),
                Map.entry("induction", "induction-stoves"),
                Map.entry("kitchen", "kitchen-appliances"),
                Map.entry("keyboard", "computer-accessories"),
                Map.entry("mouse", "computer-accessories"),
                Map.entry("accessory", "computer-accessories"),
                Map.entry("smart home", "smart-home-devices")
        );

        for (Map.Entry<String, String> entry : categoryKeywords.entrySet()) {
            if (query.contains(entry.getKey())) {
                Optional<Category> cat = categoryRepository.findBySlug(entry.getValue());
                if (cat.isPresent()) {
                    return cat.get().getId();
                }
            }
        }
        return null;
    }

    private String extractCleanSearchTerm(String query, String brand) {
        String clean = query;
        if (brand != null) {
            clean = clean.replace(brand.toLowerCase(), "");
        }
        clean = clean.replaceAll("(?:under|below|budget|within|upto|up to|best|recommend|show|find|cheap|good|buy)\\s*", "")
                .replaceAll("[0-9]+k?", "")
                .trim();
        return clean.isEmpty() ? null : clean;
    }

    private AiProductCardDto toCardDto(Product p) {
        return new AiProductCardDto(
                p.getId(),
                p.getName(),
                p.getBrand(),
                p.getModelNumber(),
                p.getEffectivePrice(),
                p.getPrice(),
                p.getDiscountPercentage(),
                p.getMainImageUrl(),
                p.getAverageRating(),
                p.getReviewCount(),
                p.isInStock(),
                p.getSpecifications(),
                p.getSlug()
        );
    }

    private List<String> getDefaultPrompts() {
        return List.of(
                "Best smartphone under ₹30,000",
                "Compare MacBook vs Dell laptop",
                "Recommend 55-inch 4K TV",
                "Best noise cancelling headphones",
                "Top energy-saving Inverter AC"
        );
    }

    private List<String> generateContextualPrompts(List<Product> products, String brand, Long catId) {
        List<String> prompts = new ArrayList<>();
        if (brand != null) {
            prompts.add("Show all " + brand + " products");
            prompts.add("Best deals under ₹20,000");
        } else {
            prompts.add("Compare top models");
            prompts.add("Filter by highest rated");
        }
        prompts.add("Help me choose by budget");
        return prompts;
    }
}
