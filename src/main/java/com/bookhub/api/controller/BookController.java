package com.bookhub.api.controller;

import com.bookhub.api.dto.ApiResponse;
import com.bookhub.api.dto.BookDTO;
import com.bookhub.api.exception.ResourceNotFoundException;
import com.bookhub.api.model.Book;
import com.bookhub.api.service.BookService;
import jakarta.validation.Valid;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<Book>>> getAllBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        try {
            Page<Book> booksPage = bookService.getAllBooks(page, size);

            ApiResponse<Page<Book>> response = ApiResponse.<Page<Book>>builder()
                    .status(HttpStatus.OK)
                    .message("Books retrieved successfully")
                    .data(booksPage)
                    .build();

            return ResponseEntity.ok(response);
        }catch (Exception e) {
            ApiResponse<Page<Book>> errorResponse = ApiResponse.<Page<Book>>builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .message("Failed to retrieve books: " + e.getMessage())
                    .data(null)
                    .build();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<Book>>> searchBooks(
            @RequestParam(required = false) String title,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        try {
            Page<Book> booksPage = bookService.searchBooksByTitle(title, page, size);

            String message = booksPage.getTotalElements() == 0
                    ? "No books found matching your search"
                    : "Books retrieved successfully";

            ApiResponse<Page<Book>> response = ApiResponse.<Page<Book>>builder()
                    .status(HttpStatus.OK)
                    .message(message)
                    .data(booksPage)
                    .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            ApiResponse<Page<Book>> errorResponse = ApiResponse.<Page<Book>>builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .message("Search failed: " + e.getMessage())
                    .data(null)
                    .build();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Book>> getBookById(@PathVariable String id) {

        try {
            Book book = bookService.getBookById(id);

            ApiResponse<Book> response = ApiResponse.<Book>builder()
                    .status(HttpStatus.OK)
                    .message("Book retrieved successfully")
                    .data(book)
                    .build();

            return ResponseEntity.ok(response);

        }catch (Exception e) {
            ApiResponse<Book> errorResponse = ApiResponse.<Book>builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .message("Failed to retrieve book: " + e.getMessage())
                    .data(null)
                    .build();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/admin/add-book")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Book>> createBook(@ModelAttribute @Valid BookDTO bookDto) {

        try {
            Book savedBook = bookService.createBook(bookDto);

            ApiResponse<Book> response = ApiResponse.<Book>builder()
                    .status(HttpStatus.CREATED)
                    .message("Book created successfully")
                    .data(savedBook)
                    .build();

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            ApiResponse<Book> errorResponse = ApiResponse.<Book>builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .message("Failed to create book: " + e.getMessage())
                    .data(null)
                    .build();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/admin/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteBook(@PathVariable String id) {
        bookService.deleteBook(id);

        ApiResponse<String> response = ApiResponse.<String>builder()
                .status(HttpStatus.OK)
                .message("Topic deleted")
                .data(null)
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}