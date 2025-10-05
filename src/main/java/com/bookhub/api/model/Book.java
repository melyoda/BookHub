package com.bookhub.api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "books")
public class Book {

    @Id
    private String id;

    private String title;

    private String author;

    private String description;

    private List<Resource> bookFileUrl;

    private String coverImageUrl;

    private List<String> categoryIds;

    private String isbn;

    private String publishedDate;

    private LocalDateTime addedOn;

    private String addedBy;

    private LocalDateTime updatedOn;

    private String updatedBy;

    private List<String> relatedBooks; // later will add a list of books ids related to this books

    @Builder.Default
    private List<String> savedBy = new ArrayList<>();
}
