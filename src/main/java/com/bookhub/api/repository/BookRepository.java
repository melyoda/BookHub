package com.bookhub.api.repository;

import com.bookhub.api.model.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends MongoRepository<Book, String> {

//    @Override
//    Optional<Book> findById(String id);

    // For title search (multiple results)
    List<Book> findByTitleContainingIgnoreCase(String title);

    // Even better: Paginated search for large libraries
    Page<Book> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    // Additional search methods you might want:
    List<Book> findByAuthorContainingIgnoreCase(String authorName);

    Page<Book> findByCategoryIdsContaining(String categoryId, Pageable pageable);


    //I think this is wrongly made it should be a custom query based on which are present
    //soooooooooooooooo again TODO: fix this and make it a good boi
//    Page<Book> findComplex(String query, String categoryId, String sort, Pageable pageable);

    // This finds all books where the 'savedBy' array field contains the given userId.
    Page<Book> findBySavedByContains(String userId, Pageable pageable);
}
