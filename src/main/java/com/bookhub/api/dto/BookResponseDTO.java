package com.bookhub.api.dto;


import com.bookhub.api.model.Resource;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookResponseDTO {

    private String id;

    private String title;

    private String author;

    private String description;

    private List<Resource> bookFileUrl;

    private String coverImage;

    private List<String> categoryIds;

    private String isbn;

    private String publishedDate;

    private List<String> relatedBooks;
}
