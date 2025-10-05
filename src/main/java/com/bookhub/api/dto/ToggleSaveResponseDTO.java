package com.bookhub.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor // A handy lombok annotation for a simple constructor
public class ToggleSaveResponseDTO {
    private String bookId;
    private boolean isSaved;
}
