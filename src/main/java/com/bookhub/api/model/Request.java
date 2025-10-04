package com.bookhub.api.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@Document(collection = "requests") // Specifies the MongoDB collection name
public class Request {

    @Id
    private String id; // MongoDB's default ObjectId will be stored here as a String

    // --- Core Request Details ---
    private String title;
    private String author;
    private String description;
    private String isbn;

    // --- Enums for State Management ---
    // MongoDB stores enums as Strings by default, which is perfect.
    private RequestStatus status;
    private RequestType type;

    // --- Relationships & Lists ---
    private String userId; // The ID of the user who made the request

    // Natively supported! No converter needed.
    private List<String> categoryIds;

    // --- Fields for CONTRIBUTION type requests ---
    private String coverImageUrl;

    // Natively supported! No converter needed.
    private List<String> bookFileUrls;

    // --- Fields for Admin Resolution ---
    private String rejectionReason;
    private String createdBookId;

    // --- Auditing Timestamps ---
    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}