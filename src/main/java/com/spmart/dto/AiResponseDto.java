package com.spmart.dto;

import java.util.ArrayList;
import java.util.List;

public class AiResponseDto {
    private String reply;
    private List<AiProductCardDto> products = new ArrayList<>();
    private boolean comparisonMode = false;
    private AiProductCardDto productA;
    private AiProductCardDto productB;
    private List<String> suggestedPrompts = new ArrayList<>();
    private String engineMode = "SP MART AI Knowledge Engine (Catalogue-Grounded)";

    public AiResponseDto() {
    }

    public AiResponseDto(String reply) {
        this.reply = reply;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public List<AiProductCardDto> getProducts() {
        return products;
    }

    public void setProducts(List<AiProductCardDto> products) {
        this.products = products;
    }

    public boolean isComparisonMode() {
        return comparisonMode;
    }

    public void setComparisonMode(boolean comparisonMode) {
        this.comparisonMode = comparisonMode;
    }

    public AiProductCardDto getProductA() {
        return productA;
    }

    public void setProductA(AiProductCardDto productA) {
        this.productA = productA;
    }

    public AiProductCardDto getProductB() {
        return productB;
    }

    public void setProductB(AiProductCardDto productB) {
        this.productB = productB;
    }

    public List<String> getSuggestedPrompts() {
        return suggestedPrompts;
    }

    public void setSuggestedPrompts(List<String> suggestedPrompts) {
        this.suggestedPrompts = suggestedPrompts;
    }

    public String getEngineMode() {
        return engineMode;
    }

    public void setEngineMode(String engineMode) {
        this.engineMode = engineMode;
    }
}
