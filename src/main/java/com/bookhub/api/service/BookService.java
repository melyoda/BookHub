package com.bookhub.api.service;

import com.bookhub.api.dto.BookDTO;
import com.bookhub.api.exception.ResourceNotFoundException;
import com.bookhub.api.model.Book;
import com.bookhub.api.model.User;
import com.bookhub.api.repository.BookRepository;
import com.bookhub.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BookService {
    private final BookRepository bookRepo;
    private final UserRepository userRepo;

    public Book createBook(BookDTO bookDto) {
        User creator = getCurrentUser();

        String coverImageUrl = null;

        Book book = Book.builder()
                .title(bookDto.getTitle())
                .author(bookDto.getAuthor())
                .description(bookDto.getDescription())
//                .bookUrl(bookDto.getBookFile())
                .coverImageUrl(coverImageUrl)
                .category(bookDto.getCategory())
                .isbn(bookDto.getIsbn())
                .relatedBooks(bookDto.getRelatedBooks())
                .publishedDate(bookDto.getPublishedDate())
                .addedBy(creator.getId())
                .addedOn(LocalDateTime.now())
                .build();

        return bookRepo.save(book);
    }

    public void deleteBook(String bookId) {
        Book book = bookRepo.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with ID: " + bookId));

        bookRepo.delete(book);
    }

    public Book getBookById(String id) {
        return bookRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));
    }

    public Page<Book> getAllBooks(int page, int size) {
        return bookRepo.findAll(PageRequest.of(page, size));
    }

    // Better: Paginated search
    public Page<Book> searchBooksByTitle(String title, int page, int size) {
        if (title == null || title.trim().isEmpty()) {
            //return all books if no title entered
            return bookRepo.findAll(PageRequest.of(page, size));
        }
        return bookRepo.findByTitleContainingIgnoreCase(
                title.trim(), PageRequest.of(page, size));
    }

    private User getCurrentUser() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        String userEmail = userDetails.getUsername(); // assuming email is username
        return userRepo.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
