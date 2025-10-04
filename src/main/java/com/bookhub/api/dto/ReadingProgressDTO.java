package com.bookhub.api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.Instant;
import java.util.Map;

@Data
public class ReadingProgressDTO {

    @NotNull
    private String format;

    @NotNull
    private Map<String, Object> locator;

    @NotNull
    private Double percent;

    // The frontend sends this, but we'll rely on the server's timestamp for our database record.
    private Instant updatedAt;
}
