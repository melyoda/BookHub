package com.bookhub.api.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiErrorResponseDto {
    private ErrorDetails error;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorDetails {
        private String code;
        private String message;
        private Map<String, Object> details;

        // Helper method to create with just code and message
        public static ErrorDetails of(String code, String message) {
            return ErrorDetails.builder()
                    .code(code)
                    .message(message)
                    .build();
        }

        public static ErrorDetails of(String code, String message, Map<String, Object> details) {
            return ErrorDetails.builder()
                    .code(code)
                    .message(message)
                    .details(details)
                    .build();
        }
    }

    // Optional: Keep the original fields for logging/internal use if needed
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    @Builder.Default
    private String path = "";
}