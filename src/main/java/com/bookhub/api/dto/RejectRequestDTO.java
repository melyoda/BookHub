package com.bookhub.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RejectRequestDTO {
    @NotBlank(message = "A reason for rejection is required")
    private String reason;
}
