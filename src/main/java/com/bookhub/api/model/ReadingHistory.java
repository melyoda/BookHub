package com.bookhub.api.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@Document(collection = "reading_history")
@CompoundIndex(name = "user_book_idx", def = "{'userId' : 1, 'bookId' : 1}", unique = true)
public class ReadingHistory {

    @Id
    private String id;

    // --- Core Relationship ---
    private String userId;
    private String bookId;

    // --- Denormalized Book Data (for fast reads) ---
    // We copy these from the original Book document to speed up API responses.
    private String bookTitle;
    private String bookCoverImage;

    // --- Timestamps ---
    private Instant lastOpenedAt; // This will be updated every time the user opens the book.

    @CreatedDate
    private Instant firstOpenedAt; // The first time the user ever opened this book.

    @LastModifiedDate
    private Instant updatedAt; // When this record was last modified in the DB.
}