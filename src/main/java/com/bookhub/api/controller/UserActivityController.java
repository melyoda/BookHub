package com.bookhub.api.controller;

import com.bookhub.api.dto.ApiResponse; // Your custom response wrapper
import com.bookhub.api.dto.LogHistoryRequestDTO;
import com.bookhub.api.dto.ReadingHistoryResponseDTO;
import com.bookhub.api.dto.ReadingProgressDTO;
import com.bookhub.api.service.UserActivityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class UserActivityController {

    private final UserActivityService userActivityService;

    @PostMapping("/history")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Void>> logReadingHistory(
            @RequestBody @Valid LogHistoryRequestDTO historyDto) {

        try {
            userActivityService.logHistory(historyDto.getBookId());
            ApiResponse<Void> response = ApiResponse.<Void>builder()
                    .status(HttpStatus.OK)
                    .message("Reading history updated successfully.")
                    .build();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<Void> errorResponse = ApiResponse.<Void>builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .message("Failed to update history: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/history")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Page<ReadingHistoryResponseDTO>>> getReadingHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<ReadingHistoryResponseDTO> historyPage = userActivityService.getHistory(pageable);

            ApiResponse<Page<ReadingHistoryResponseDTO>> response = ApiResponse.<Page<ReadingHistoryResponseDTO>>builder()
                    .status(HttpStatus.OK)
                    .message("Reading history retrieved successfully.")
                    .data(historyPage)
                    .build();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<Page<ReadingHistoryResponseDTO>> errorResponse = ApiResponse.<Page<ReadingHistoryResponseDTO>>builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .message("Failed to retrieve history: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /// progress endpoints

    @PutMapping("/progress/{bookId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<ReadingProgressDTO>> saveOrUpdateProgress(
            @PathVariable String bookId,
            @RequestBody @Valid ReadingProgressDTO progressDTO) {

        try {
            ReadingProgressDTO savedProgress = userActivityService.saveOrUpdateProgress(bookId, progressDTO);
            ApiResponse<ReadingProgressDTO> response = ApiResponse.<ReadingProgressDTO>builder()
                    .status(HttpStatus.OK)
                    .message("Progress saved successfully.")
                    .data(savedProgress)
                    .build();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<ReadingProgressDTO> errorResponse = ApiResponse.<ReadingProgressDTO>builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .message("Failed to retrieve history: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/progress/{bookId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<ReadingProgressDTO>> getProgress(@PathVariable String bookId) {
        try {
            ReadingProgressDTO progress = userActivityService.getProgress(bookId);
            ApiResponse<ReadingProgressDTO> response = ApiResponse.<ReadingProgressDTO>builder()
                    .status(HttpStatus.OK)
                    .message("Progress retrieved successfully.")
                    .data(progress)
                    .build();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<ReadingProgressDTO> errorResponse = ApiResponse.<ReadingProgressDTO>builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .message("Failed to retrieve history: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

}
