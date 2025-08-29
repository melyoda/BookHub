package com.bookhub.api.repository;

import com.bookhub.api.model.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends MongoRepository<Book, String> {

    Optional<Book> findById(String id);

    // For title search (multiple results)
    List<Book> findByTitleContainingIgnoreCase(String title);

    // Even better: Paginated search for large libraries
    Page<Book> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    // Additional search methods you might want:
    List<Book> findByAuthorContainingIgnoreCase(String authorName);
}
