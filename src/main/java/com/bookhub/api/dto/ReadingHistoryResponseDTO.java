package com.bookhub.api.dto;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;

@Data
@Builder
public class ReadingHistoryResponseDTO {

    private String bookId;
    private String title;
    private String coverImage;
    private Instant lastOpenedAt;
}
