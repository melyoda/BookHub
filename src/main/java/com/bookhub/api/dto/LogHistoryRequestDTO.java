package com.bookhub.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LogHistoryRequestDTO {
    @NotBlank(message = "bookId is required")
    private String bookId;
}
