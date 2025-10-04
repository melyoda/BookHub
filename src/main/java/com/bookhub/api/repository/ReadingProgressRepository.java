package com.bookhub.api.repository;

import com.bookhub.api.model.ReadingProgress;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ReadingProgressRepository extends MongoRepository<ReadingProgress, String> {

    // This method will power both our GET and PUT logic.
    Optional<ReadingProgress> findByUserIdAndBookId(String userId, String bookId);
}
