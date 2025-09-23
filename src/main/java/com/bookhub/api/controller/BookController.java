package com.bookhub.api.controller;

import com.bookhub.api.dto.ApiResponse;
import com.bookhub.api.dto.BookDTO;
import com.bookhub.api.dto.BookResponseDTO;
import com.bookhub.api.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<BookResponseDTO>>> getAllBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        try {
            Page<BookResponseDTO> booksPage = bookService.getAllBooks(page, size);

            ApiResponse<Page<BookResponseDTO>> response = ApiResponse.<Page<BookResponseDTO>>builder()
                    .status(HttpStatus.OK)
                    .message("Books retrieved successfully")
                    .data(booksPage)
                    .build();

            return ResponseEntity.ok(response);
        }catch (Exception e) {
            ApiResponse<Page<BookResponseDTO>> errorResponse = ApiResponse.<Page<BookResponseDTO>>builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .message("Failed to retrieve books: " + e.getMessage())
                    .data(null)
                    .build();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<BookResponseDTO>>> searchBooks(
            @RequestParam(required = false) String title,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        try {
            Page<BookResponseDTO> booksPage = bookService.searchBooksByTitle(title, page, size);

            String message = booksPage.getTotalElements() == 0
                    ? "No books found matching your search"
                    : "Books retrieved successfully";

            ApiResponse<Page<BookResponseDTO>> response = ApiResponse.<Page<BookResponseDTO>>builder()
                    .status(HttpStatus.OK)
                    .message(message)
                    .data(booksPage)
                    .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            ApiResponse<Page<BookResponseDTO>> errorResponse = ApiResponse.<Page<BookResponseDTO>>builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .message("Search failed: " + e.getMessage())
                    .data(null)
                    .build();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/{bookId}")
    public ResponseEntity<ApiResponse<BookResponseDTO>> getBookById(@PathVariable String bookId) {

            BookResponseDTO book = bookService.getBookById(bookId);

            ApiResponse<BookResponseDTO> response = ApiResponse.<BookResponseDTO>builder()
                    .status(HttpStatus.OK)
                    .message("Book retrieved successfully")
                    .data(book)
                    .build();

            return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BookResponseDTO>> createBook(@ModelAttribute @Valid BookDTO bookDto) {

            BookResponseDTO savedBook = bookService.createBook(bookDto);

            ApiResponse<BookResponseDTO> response = ApiResponse.<BookResponseDTO>builder()
                    .status(HttpStatus.CREATED)
                    .message("Book created successfully")
                    .data(savedBook)
                    .build();

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }

    @DeleteMapping("/{bookId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteBook(@PathVariable String bookId) {
        bookService.deleteBook(bookId);

        ApiResponse<String> response = ApiResponse.<String>builder()
                .status(HttpStatus.OK)
                .message("Topic deleted")
                .data(null)
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/{bookId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BookResponseDTO>> updateBook(
            @PathVariable String bookId,
            @ModelAttribute @Valid BookDTO bookDto) {

            BookResponseDTO updatedBook = bookService.updateBook(bookId,bookDto);

            ApiResponse<BookResponseDTO> response = ApiResponse.<BookResponseDTO>builder()
                    .status(HttpStatus.CREATED)
                    .message("Book created successfully")
                    .data(updatedBook)
                    .build();

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

}