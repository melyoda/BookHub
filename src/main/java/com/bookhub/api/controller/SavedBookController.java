package com.bookhub.api.controller;

import com.bookhub.api.dto.ApiResponse;
import com.bookhub.api.dto.BookResponseDTO;
import com.bookhub.api.dto.ToggleSaveResponseDTO;
import com.bookhub.api.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/saved")
@RequiredArgsConstructor
public class SavedBookController {

    private final BookService bookService;

    @PutMapping("/{bookId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<ToggleSaveResponseDTO>> toggleSavedBook(@PathVariable String bookId) {
            boolean isSaved = bookService.toggleSaveForBook(bookId);
            ToggleSaveResponseDTO data = new ToggleSaveResponseDTO(bookId, isSaved);

            ApiResponse<ToggleSaveResponseDTO> response = ApiResponse.<ToggleSaveResponseDTO>builder()
                    .status(HttpStatus.OK)
                    .message(isSaved ? "Book saved successfully." : "Book removed from saved list.")
                    .data(data)
                    .build();
            return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Page<BookResponseDTO>>> getSavedBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
            Pageable pageable = PageRequest.of(page, size);
            Page<BookResponseDTO> savedBooks = bookService.getSavedBooksForCurrentUser(pageable);

            ApiResponse<Page<BookResponseDTO>> response = ApiResponse.<Page<BookResponseDTO>>builder()
                    .status(HttpStatus.OK)
                    .message("Saved books retrieved successfully.")
                    .data(savedBooks)
                    .build();
            return ResponseEntity.ok(response);
    }
}
