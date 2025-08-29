package com.bookhub.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookDTO {

    @NotBlank(message = "Title is required")
    private String title;
    @NotBlank(message = "Author is required")
    private String author;
    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    private MultipartFile bookFile;

    private MultipartFile coverImage;

    private String category;

    private String isbn;

    private String publishedDate;

    private List<String> relatedBooks;
}
