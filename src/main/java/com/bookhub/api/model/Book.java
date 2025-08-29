package com.bookhub.api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Book {

    @Id
    private String id;

    private String title;

    private String author;

    private String description;

    private String bookUrl;

    private String coverImageUrl;

    private String category;

    private String isbn;

    private String publishedDate;

    private LocalDateTime addedOn;

    private String addedBy;

    private List<String> relatedBooks;
}
