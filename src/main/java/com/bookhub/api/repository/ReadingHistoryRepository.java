package com.bookhub.api.repository;
import com.bookhub.api.model.ReadingHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ReadingHistoryRepository extends MongoRepository<ReadingHistory, String> {

    // This method is key for our upsert logic
    Optional<ReadingHistory> findByUserIdAndBookId(String userId, String bookId);
    Page<ReadingHistory> findByUserIdOrderByLastOpenedAtDesc(String userId, Pageable pageable);
}
