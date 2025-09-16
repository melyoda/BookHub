package com.bookhub.api.service;

import com.bookhub.api.dto.BookDTO;
import com.bookhub.api.dto.BookResponseDTO;
import com.bookhub.api.exception.ResourceNotFoundException;
import com.bookhub.api.model.Book;
import com.bookhub.api.model.User;
import com.bookhub.api.repository.BookRepository;
import com.bookhub.api.repository.CategoryRepository;
import com.bookhub.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class BookService {
    private final BookRepository bookRepo;
    private final UserRepository userRepo;
    private final CategoryRepository categoryRepo;

    public Book createBook(BookDTO bookDto) {
        User creator = getCurrentUser();

//        String coverImageUrl = fileStorageService.uploadFile(coverImageFile); // later change implement file storage system

        Book book = Book.builder()
                .title(bookDto.getTitle())
                .author(bookDto.getAuthor())
                .description(bookDto.getDescription())
//                .bookFileUrl(bookDto.getBookFile())
//                .coverImageUrl(coverImageUrl)
                .categoryIds(bookDto.getCategoryIds())
                .isbn(bookDto.getIsbn())
                .relatedBooks(bookDto.getRelatedBooks())
                .publishedDate(bookDto.getPublishedDate())
                .addedBy(creator.getId())
                .addedOn(LocalDateTime.now())
                .build();

        Book savedBook = bookRepo.save(book);

        if (savedBook.getCategoryIds() != null) {
            for (String categoryId : savedBook.getCategoryIds()) {
                categoryRepo.findById(categoryId).ifPresent(category -> {
                    category.setBookCount(category.getBookCount() + 1);
                    categoryRepo.save(category);
                });
            }
        }

        return savedBook;
    }

    public void deleteBook(String bookId) {
        Book book = bookRepo.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with ID: " + bookId));

        // Decrement count for each category
        if (book.getCategoryIds() != null) {
            for (String categoryId : book.getCategoryIds()) {
                categoryRepo.findById(categoryId).ifPresent(category -> {
                    category.setBookCount(Math.max(0, category.getBookCount() - 1)); // prevent negative
                    categoryRepo.save(category);
                });
            }
        }

        bookRepo.delete(book);
    }

    public Book updateBook(String bookId, BookDTO updateDto) {
        User updater = getCurrentUser();
        Book existingBook = bookRepo.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found..."));

        // Update fields if they are provided in the DTO
        if (updateDto.getTitle() != null) {
            existingBook.setTitle(updateDto.getTitle());
        }
        if (updateDto.getAuthor() != null) {
            existingBook.setAuthor(updateDto.getAuthor());
        }
        if (updateDto.getDescription() != null) {
            existingBook.setDescription(updateDto.getDescription());
        }
        //change those to use the file uploadService implemented later
//        if (updateDto.getBookFile() != null) {
//            existingBook.setBookFileUrl(updateDto.getBookFile());
//        }
//        if (updateDto.getCoverImage() != null) {
//            existingBook.setCoverImageUrl(updateDto.getCoverImage());
//        }
        if (updateDto.getIsbn() != null) {
            existingBook.setIsbn(updateDto.getIsbn());
        }
        if (updateDto.getPublishedDate() != null) {
            existingBook.setPublishedDate(updateDto.getPublishedDate());
        }
        if (updateDto.getRelatedBooks() != null) {
            existingBook.setRelatedBooks(updateDto.getRelatedBooks());
        }
        existingBook.setUpdatedOn(LocalDateTime.now());
        existingBook.setUpdatedBy(updater.getId());

        return bookRepo.save(existingBook);
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

    public Page<Book> getBooksForCategory(String categoryId, Pageable pageable) {
        return bookRepo.findByCategoryIdsContaining(categoryId, pageable);
    }

//    public Page<Book> searchBooks(String query, String categoryId, String sort, Pageable pageable) {
        // This is a complex query. You would typically use the
        // MongoRepository's query capabilities, or the Criteria API
        // to build the query dynamically based on which parameters are present.

        // Example logic:
        // 1. If categoryId is present, filter by it.
        // 2. If query (q) is present, search in title, author, description.
        // 3. Apply sorting based on the 'sort' parameter ("recent" or "popularity").
        // 4. Return the paginated result.

        // This would replace getAllBooks, searchBooksByTitle, and getBooksForCategory
        // for the main book list view.
//        return bookRepo.findComplex(query, categoryId, sort, pageable); // (You'd need a custom repository method for this)
//    }

    private User getCurrentUser() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        String userEmail = userDetails.getUsername(); // assuming email is username
        return userRepo.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private BookResponseDTO toBookResponseDTO(Book book) {
        return BookResponseDTO.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .description(book.getDescription())
                .bookFileUrl(book.getBookFileUrl())
                .coverImage(book.getCoverImageUrl())
                .categoryIds(book.getCategoryIds())
                .isbn(book.getIsbn())
                .publishedDate(book.getPublishedDate())
                .relatedBooks(book.getRelatedBooks())
                .build();
    }

    public List<BookResponseDTO> getBookResponseDTOs(List<Book> books) {
        return books.stream()
                .map(this::toBookResponseDTO)
                .collect(Collectors.toList());
    }
}
