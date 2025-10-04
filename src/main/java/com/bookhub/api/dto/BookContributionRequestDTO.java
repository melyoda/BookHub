package com.bookhub.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class BookContributionRequestDTO {

    @NotBlank
    private String title;
    @NotBlank
    private String author;
    private String description;

    @NotNull // The book file is required for a contribution
    private List<MultipartFile> bookFile;

    @NotNull // The cover image is required for a contribution
    private MultipartFile coverImage;

    private List<String> categoryIds;
    private String isbn;
}
