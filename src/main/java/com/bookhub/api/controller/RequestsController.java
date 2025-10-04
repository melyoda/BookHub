package com.bookhub.api.controller;

import com.bookhub.api.dto.*;
import com.bookhub.api.model.RequestStatus;
import com.bookhub.api.model.RequestType;
import com.bookhub.api.service.RequestsService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/requests")
@RequiredArgsConstructor
public class RequestsController {

    private final RequestsService requestsService;

    @PostMapping("/contribute")
    @PreAuthorize("hasRole('USER')") // Or whatever role can contribute
    public ResponseEntity<ApiResponse<BookRequestResponseDTO>> submitContributionRequest(
            @ModelAttribute @Valid BookContributionRequestDTO dto) {
        try {
            BookRequestResponseDTO newRequest = requestsService.createContributionRequest(dto);
            ApiResponse<BookRequestResponseDTO> response = ApiResponse.<BookRequestResponseDTO>builder()
                    .status(HttpStatus.CREATED)
                    .message("Contribution request submitted successfully. It is now pending review.")
                    .data(newRequest)
                    .build();
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            ApiResponse<BookRequestResponseDTO> errorResponse = ApiResponse.<BookRequestResponseDTO>builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .message("Failed to submit request: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/lookup")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<BookRequestResponseDTO>> submitLookupRequest(
            @RequestBody @Valid BookLookupRequestDTO dto) {
        try {
            BookRequestResponseDTO newRequest = requestsService.createLookupRequest(dto);
            ApiResponse<BookRequestResponseDTO> response = ApiResponse.<BookRequestResponseDTO>builder()
                    .status(HttpStatus.CREATED)
                    .message("Lookup request submitted successfully. It is now pending review.")
                    .data(newRequest)
                    .build();
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            ApiResponse<BookRequestResponseDTO> errorResponse = ApiResponse.<BookRequestResponseDTO>builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .message("Failed to submit request: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }


    //getting requests
    // In RequestsController.java

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<BookRequestResponseDTO>>> getRequests(
            @RequestParam(required = false) RequestType type,
            @RequestParam(defaultValue = "PENDING") RequestStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<BookRequestResponseDTO> requestsPage = requestsService.getRequests(type, status, pageable);
            ApiResponse<Page<BookRequestResponseDTO>> response = ApiResponse.<Page<BookRequestResponseDTO>>builder()
                    .status(HttpStatus.OK)
                    .message("Requests retrieved successfully")
                    .data(requestsPage)
                    .build();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Your standard error handling
            ApiResponse<Page<BookRequestResponseDTO>> errorResponse = ApiResponse.<Page<BookRequestResponseDTO>>builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .message("Failed to retrieve requests: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PatchMapping("/{requestId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BookRequestResponseDTO>> approveRequest(
            @PathVariable String requestId,
            @RequestBody(required = false) ApproveRequestDTO dto) { // DTO can be optional
        try {
            String createdBookId = (dto != null) ? dto.getCreatedBookId() : null;
            BookRequestResponseDTO updatedRequest = requestsService.approveRequest(requestId, createdBookId);
            ApiResponse<BookRequestResponseDTO> response = ApiResponse.<BookRequestResponseDTO>builder()
                    .status(HttpStatus.OK)
                    .message("Request approved successfully.")
                    .data(updatedRequest)
                    .build();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Your standard error handling
            ApiResponse<BookRequestResponseDTO> errorResponse = ApiResponse.<BookRequestResponseDTO>builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .message("Failed to approve request: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @PatchMapping("/{requestId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BookRequestResponseDTO>> rejectRequest(
            @PathVariable String requestId,
            @RequestBody @Valid RejectRequestDTO dto) {
        try {
            BookRequestResponseDTO updatedRequest = requestsService.rejectRequest(requestId, dto.getReason());
            ApiResponse<BookRequestResponseDTO> response = ApiResponse.<BookRequestResponseDTO>builder()
                    .status(HttpStatus.OK)
                    .message("Request rejected successfully.")
                    .data(updatedRequest)
                    .build();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Your standard error handling
            ApiResponse<BookRequestResponseDTO> errorResponse = ApiResponse.<BookRequestResponseDTO>builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .message("Failed to reject request: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
}