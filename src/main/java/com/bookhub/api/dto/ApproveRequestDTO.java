package com.bookhub.api.dto;

import lombok.Data;

@Data
public class ApproveRequestDTO {
    // This is only required when approving a LOOKUP request
    private String createdBookId;
}
