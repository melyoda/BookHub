package com.bookhub.api.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@Document(collection = "reading_progress")
// This unique index is the database rule for "one user, one book, one progress record".
@CompoundIndex(name = "user_book_progress_idx", def = "{'userId' : 1, 'bookId' : 1}", unique = true)
public class ReadingProgress {

    @Id
    private String id;

    // --- Core Relationship ---
    private String userId;
    private String bookId;

    // --- Progress Details ---
    private double percent; // e.g., 0.42 for 42%
    private String format; // "EPUB", "PDF", etc.

    // Using a Map is perfect for a flexible locator.
    // It can store {"cfi": "..."} for EPUBs or {"page": 42} for PDFs.
    private Map<String, Object> locator;

    @LastModifiedDate
    private Instant updatedAt; // The server's timestamp for the last update
}
