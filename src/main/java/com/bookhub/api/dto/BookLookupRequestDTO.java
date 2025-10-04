package com.bookhub.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BookLookupRequestDTO {

    @NotBlank(message = "Title is required")
    private String title;

    private String author;

    private String description; // For user notes

    private String isbn;
}