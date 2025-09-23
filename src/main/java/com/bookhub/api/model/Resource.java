package com.bookhub.api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Resource {
    private ResourceType type;
    private String contentUrl;
    private long sizeBytes;
    private String originalName;
    private String checksumSha256; // For file verification
    private String contentType;    // e.g., "application/epub+zip"
    private boolean drmProtected;  // For future use

    @Builder.Default
    private LocalDateTime uploadedAt = LocalDateTime.now();
}
