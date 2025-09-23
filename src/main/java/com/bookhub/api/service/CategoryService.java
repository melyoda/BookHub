package com.bookhub.api.service;

import com.bookhub.api.dto.CategoryDTO;
import com.bookhub.api.exception.BusinessException;
import com.bookhub.api.exception.DuplicateResourceException;
import com.bookhub.api.exception.ResourceNotFoundException;
import com.bookhub.api.exception.ValidationException;
import com.bookhub.api.model.Categories;
import com.bookhub.api.repository.BookRepository;
import com.bookhub.api.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoryService{
    private final BookRepository bookRepo;
//    private final UserRepository userRepo;
    private final CategoryRepository categoryRepo;

    // CRUD // Create // Read // Update // Delete

    //Starting with Create new caaaats
    public CategoryDTO createCategory(CategoryDTO categoryDTO) {
        validateCategoryName(categoryDTO.getName());
        checkForDuplicateCategory(categoryDTO.getName());

        Categories category = Categories.builder()
                .name(categoryDTO.getName().trim())
                .bookCount(0) // Explicitly set initial count
                .build();

        Categories savedCategory = categoryRepo.save(category);
        return toCategoryDTO(savedCategory);
    }

    public Page<CategoryDTO> getAllCategories(int page, int size) {
        Page<Categories> categoriesPage = categoryRepo.findAll(PageRequest.of(page, size));
        return categoriesPage.map(this::toCategoryDTO);
    }

    public CategoryDTO getCategoryByName(String name) {
        Categories category = categoryRepo.findByName(name.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + name));
        return toCategoryDTO(category);
    }

    //used for internal operations
    public CategoryDTO getCategoryById(String id) {
        Categories category = categoryRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));
        return toCategoryDTO(category);
    }

    public CategoryDTO updateCategory(String categoryId, CategoryDTO updateDTO) {
        Categories existingCategory = getCategoryEntityById(categoryId);

        if (updateDTO.getName() != null && !updateDTO.getName().equals(existingCategory.getName())) {
            validateCategoryName(updateDTO.getName());
            checkForDuplicateCategory(updateDTO.getName(), categoryId); // Exclude current category
            existingCategory.setName(updateDTO.getName().trim());
        }

        Categories updatedCategory = categoryRepo.save(existingCategory);
        return toCategoryDTO(updatedCategory);
    }

    public void deleteCategory(String categoryId) {
        Categories category = getCategoryEntityById(categoryId);

        // Safety check: Prevent deletion if category has books
        if (category.getBookCount() > 0) {
            throw new BusinessException(
                    "Cannot delete category '" + category.getName() + "' because it contains " +
                            category.getBookCount() + " books. Please reassign books first."
            );
        }

        categoryRepo.delete(category);
    }

    //helper methods
    private Categories getCategoryEntityById(String id) {
        return categoryRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));
    }

    private void validateCategoryName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("Category name cannot be empty");
        }
        if (name.trim().length() < 2) {
            throw new ValidationException("Category name must be at least 2 characters long");
        }
        if (name.trim().length() > 50) {
            throw new ValidationException("Category name cannot exceed 50 characters");
        }
    }

    private void checkForDuplicateCategory(String name) {
        checkForDuplicateCategory(name, null);
    }

    private void checkForDuplicateCategory(String name, String excludeCategoryId) {
        Optional<Categories> existingCategory = categoryRepo.findByName(name.trim());
        if (existingCategory.isPresent()) {
            // If we're updating, allow the same name for the same category
            if (excludeCategoryId == null ||
                    !existingCategory.get().getId().equals(excludeCategoryId)) {
                throw new DuplicateResourceException("Category already exists: " + name);
            }
        }
    }

    private CategoryDTO toCategoryDTO(Categories category) {
        return CategoryDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .bookCount(category.getBookCount())
                .build();
    }

/*    private CategoryWithStatsDTO toCategoryWithStatsDTO(Categories category) {
        return CategoryWithStatsDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .bookCount(category.getBookCount())
                // Add more stats if needed
                .build();
    }*/


}
