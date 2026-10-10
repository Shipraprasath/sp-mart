package com.spmart.service;

import com.spmart.dto.CategoryDto;
import com.spmart.entity.Category;
import com.spmart.exception.BusinessRuleException;
import com.spmart.exception.ResourceNotFoundException;
import com.spmart.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public List<Category> getAllActiveCategories() {
        return categoryRepository.findAllByActiveTrueOrderByDisplayOrderAsc();
    }

    @Transactional(readOnly = true)
    public List<Category> getAllCategories() {
        return categoryRepository.findAllByOrderByDisplayOrderAsc();
    }

    @Transactional(readOnly = true)
    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public Category getCategoryBySlug(String slug) {
        return categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with slug: " + slug));
    }

    public Category createCategory(CategoryDto dto) {
        String slug = generateSlug(dto.getName());
        if (categoryRepository.existsByName(dto.getName().trim())) {
            throw new BusinessRuleException("A category with name '" + dto.getName() + "' already exists.");
        }
        if (categoryRepository.existsBySlug(slug)) {
            slug = slug + "-" + System.currentTimeMillis() % 1000;
        }

        Category category = new Category(
                dto.getName().trim(),
                slug,
                dto.getDescription(),
                dto.getImageUrl(),
                dto.getIcon() != null ? dto.getIcon() : "tag",
                dto.getDisplayOrder()
        );
        category.setActive(dto.isActive());
        return categoryRepository.save(category);
    }

    public Category updateCategory(Long id, CategoryDto dto) {
        Category category = getCategoryById(id);
        if (!category.getName().equalsIgnoreCase(dto.getName().trim()) && categoryRepository.existsByName(dto.getName().trim())) {
            throw new BusinessRuleException("Category name '" + dto.getName() + "' is already in use.");
        }

        category.setName(dto.getName().trim());
        category.setDescription(dto.getDescription());
        category.setImageUrl(dto.getImageUrl());
        category.setIcon(dto.getIcon());
        category.setDisplayOrder(dto.getDisplayOrder());
        category.setActive(dto.isActive());
        return categoryRepository.save(category);
    }

    public void deleteCategory(Long id) {
        Category category = getCategoryById(id);
        if (!category.getProducts().isEmpty()) {
            throw new BusinessRuleException("Cannot delete category containing products. Reassign or remove its products first.");
        }
        categoryRepository.delete(category);
    }

    public long getCategoryCount() {
        return categoryRepository.count();
    }

    private String generateSlug(String text) {
        return text.toLowerCase()
                .replaceAll("[^a-z0-9\\s]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("^-|-$", "");
    }
}
