package com.bookhub.api.repository;

import com.bookhub.api.model.Categories;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface CategoryRepository extends MongoRepository<Categories, String> {

    /**
     * Finds a category by its unique name.
     * This will be useful for admin panels or when creating books.
     */
    Optional<Categories> findByName(String name);
}
