package com.bookhub.api.dto;

import com.bookhub.api.model.RequestStatus;
import com.bookhub.api.model.RequestType;
import lombok.Data;
import java.time.Instant;
import java.util.List;

@Data
public class BookRequestResponseDTO {

    private String id;
    private RequestType requestType;
    private RequestStatus status;

    // User and Book Information
    private String title;
    private String author;
    private String description;
    private String isbn;
    private List<String> categoryIds;

    // Metadata
    private String userId; // ID of the user who submitted
    private Instant createdAt;
    private Instant updatedAt;

    // Fields for resolution
    private String rejectionReason; // Null if not rejected
    private String createdBookId;   // Null if not approved
}
