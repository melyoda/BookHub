package com.bookhub.api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Resource {

    private ResourceType type;

    private String content;

    private String contentUrl;
}
