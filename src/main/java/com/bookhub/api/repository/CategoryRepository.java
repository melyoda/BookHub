package com.bookhub.api.repository;

import com.bookhub.api.model.Categories;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface CategoryRepository extends MongoRepository<Categories, String> {
    Optional<Categories> findByName(String name);
    Optional<Categories> findByNameIgnoreCase(String name);

    // Check if category exists by name (for validation)
    boolean existsByName(String name);
    boolean existsByNameAndIdNot(String name, String id);
}
