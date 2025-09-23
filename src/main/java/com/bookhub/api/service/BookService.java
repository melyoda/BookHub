package com.bookhub.api.service;

import com.bookhub.api.dto.BookDTO;
import com.bookhub.api.dto.BookResponseDTO;
import com.bookhub.api.exception.FileUploadException;
import com.bookhub.api.exception.ResourceNotFoundException;
import com.bookhub.api.model.*;
import com.bookhub.api.repository.BookRepository;
import com.bookhub.api.repository.CategoryRepository;
import com.bookhub.api.repository.UserRepository;
import com.bookhub.api.utils.StoreResources;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class BookService {
    private final BookRepository bookRepo;
    private final UserRepository userRepo;
    private final CategoryRepository categoryRepo;
    private final StoreResources storeResources;
    private final CloudinaryService cloudinaryService;


    public BookResponseDTO createBook(BookDTO bookDto) {
        User creator = getCurrentUser();
        validateCategories(bookDto.getCategoryIds());

        Book book = Book.builder()
                .title(bookDto.getTitle())
                .author(bookDto.getAuthor())
                .description(bookDto.getDescription())
                .bookFileUrl(saveBookFiles(bookDto.getBookFile()))
                .coverImageUrl(saveCoverImage(bookDto.getCoverImage()))
                .categoryIds(bookDto.getCategoryIds()) //in the front-end creation of a book will have a list of categories that can be chosen from which will give these ids i thinks
                .isbn(bookDto.getIsbn())
                .relatedBooks(bookDto.getRelatedBooks())
                .publishedDate(bookDto.getPublishedDate())
                .addedBy(creator.getId())
                .addedOn(LocalDateTime.now())
                .build();

        Book savedBook = bookRepo.save(book);
        updateCategoryCounts(savedBook.getCategoryIds(), 1);

        return toBookResponseDTO(savedBook);
    }

    public void deleteBook(String bookId) {
        Book book = bookRepo.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with ID: " + bookId));

        try {
            // Delete the cover image if it exists
            if (book.getCoverImageUrl() != null && !book.getCoverImageUrl().isEmpty()) {
                cloudinaryService.deleteFile(book.getCoverImageUrl());
            }
            // Delete the book file if it exists
            if (book.getBookFileUrl() != null && !book.getBookFileUrl().isEmpty()) {
                for (Resource resource : book.getBookFileUrl()) {
                    if (resource.getContentUrl() != null && !resource.getContentUrl().isEmpty()) {
                        cloudinaryService.deleteFile(resource.getContentUrl());
                    }
                }
            }
        } catch (IOException e) {
            // Log a warning but don't stop the database deletion.
            log.warn("Failed to delete Cloudinary files for bookId {}: {}", bookId, e.getMessage());
        }

        updateCategoryCounts(book.getCategoryIds(), -1);
        bookRepo.delete(book);
    }

    @Transactional
    public BookResponseDTO updateBook(String bookId, BookDTO updateDto) {
        User updater = getCurrentUser();
        Book existingBook = bookRepo.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found..."));

        // Track category changes for count updates
        List<String> oldCategoryIds = existingBook.getCategoryIds();
        List<String> newCategoryIds = updateDto.getCategoryIds();

        try {
            // Handle cover image update
            if (updateDto.getCoverImage() != null && !updateDto.getCoverImage().isEmpty()) {
                // Delete the old cover image
                if (existingBook.getCoverImageUrl() != null) {
                    cloudinaryService.deleteFile(existingBook.getCoverImageUrl());
                }
                // Upload new cover image
                String newCoverUrl = saveCoverImage(updateDto.getCoverImage());
                existingBook.setCoverImageUrl(newCoverUrl);
            }

            // Handle book files update
            if (updateDto.getBookFile() != null && !updateDto.getBookFile().isEmpty()) {
                // Delete ALL old book files
                if (existingBook.getBookFileUrl() != null && !existingBook.getBookFileUrl().isEmpty()) {
                    for (Resource resource : existingBook.getBookFileUrl()) {
                        if (resource.getContentUrl() != null) {
                            cloudinaryService.deleteFile(resource.getContentUrl());
                        }
                    }
                }
                // Upload new book files
                List<Resource> newResources = saveBookFiles(updateDto.getBookFile());
                existingBook.setBookFileUrl(newResources);
            }
        } catch (IOException e) {
            log.error("File operation failed during book update: {}", e.getMessage());
            throw new FileUploadException("Failed to update files: " + e.getMessage());
        }

        // Update fields
        updateBookFields(existingBook, updateDto, updater.getId());

        Book updatedBook = bookRepo.save(existingBook);

        // Update category counts if categories changed
        if (newCategoryIds != null && !newCategoryIds.equals(oldCategoryIds)) {
            updateCategoryCounts(oldCategoryIds, -1); // Decrement old categories
            updateCategoryCounts(newCategoryIds, 1);  // Increment new categories
        }

        return toBookResponseDTO(updatedBook);
    }

    private void updateBookFields(Book book, BookDTO dto, String updaterId) {
        if (dto.getTitle() != null) book.setTitle(dto.getTitle());
        if (dto.getAuthor() != null) book.setAuthor(dto.getAuthor());
        if (dto.getDescription() != null) book.setDescription(dto.getDescription());
        if (dto.getIsbn() != null) book.setIsbn(dto.getIsbn());
        if (dto.getPublishedDate() != null) book.setPublishedDate(dto.getPublishedDate());
        if (dto.getRelatedBooks() != null) book.setRelatedBooks(dto.getRelatedBooks());

        // File updates - only if new files are provided
        if (dto.getBookFile() != null && !dto.getBookFile().isEmpty()) {
            book.setBookFileUrl(saveBookFiles(dto.getBookFile()));
        }
        if (dto.getCoverImage() != null && !dto.getCoverImage().isEmpty()) {
            book.setCoverImageUrl(saveCoverImage(dto.getCoverImage()));
        }
        if (dto.getCategoryIds() != null) {
            book.setCategoryIds(dto.getCategoryIds());
        }

        book.setUpdatedOn(LocalDateTime.now());
        book.setUpdatedBy(updaterId);
    }


    public BookResponseDTO getBookById(String id) {
        Book book= bookRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));
        return toBookResponseDTO(book);
    }

    public Page<BookResponseDTO> getAllBooks(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Book> bookPage = bookRepo.findAll(pageable);
        // Call your reusable helper method
        return toBookResponseDTOPage(bookPage);
    }

    // Better: Paginated search
    public Page<BookResponseDTO> searchBooksByTitle(String title, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Book> bookPage;
        if (title == null || title.trim().isEmpty()) {
            //return all books if no title entered
            bookPage = bookRepo.findAll(pageable);
        } else {
            // Find books by the given title
            bookPage = bookRepo.findByTitleContainingIgnoreCase(title.trim(), pageable);
        }
        return toBookResponseDTOPage(bookPage);
    }

    public Page<BookResponseDTO> getBooksByCategory(String catId, Pageable pageable) {
        Page<Book> bookPage = bookRepo.findByCategoryIdsContaining(catId, pageable);
        return toBookResponseDTOPage(bookPage);
    }

/*    public Page<BookResponseDTO> searchBooks(String query, String categoryId, String sort, Pageable pageable) {
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
        return toBookResponseDTOPage(bookRepo.findComplex(query, categoryId, sort, pageable)); // (You'd need a custom repository method for this)
    }*/

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

    public Page<BookResponseDTO> toBookResponseDTOPage(Page<Book> booksPage) {
        // The .map() function handles the conversion of the content
        // and preserves the page metadata (total elements, pageable, etc.)
        return booksPage.map(this::toBookResponseDTO);
    }

    private void validateCategories(List<String> categoryIds) {
        List<String> existingCategories = categoryRepo.findAllById(categoryIds)
                .stream()
                .map(Categories::getId)
                .toList();

        if (existingCategories.size() != categoryIds.size()) {
            List<String> missing = new ArrayList<>(categoryIds);
            missing.removeAll(existingCategories);
            throw new RuntimeException("Categories not found: " + missing);
        }
    }

    private String saveCoverImage(MultipartFile coverImage) {
        if (coverImage == null || coverImage.isEmpty()) {
            return null;
        }
        return storeResources.saveFile(coverImage);
    }

    private List<Resource> saveBookFiles(List<MultipartFile> bookFiles) {
        if (bookFiles == null || bookFiles.isEmpty()) {
            return Collections.emptyList();
        }

        return bookFiles.stream()
                .filter(file -> !file.isEmpty())
                .map(file -> {
                    String url = storeResources.saveFile(file);
                    ResourceType type = storeResources.determineResourceType(
                            Objects.requireNonNull(file.getOriginalFilename()));

                    return Resource.builder()
                            .type(type)
                            .contentUrl(url)
                            .sizeBytes(file.getSize())
                            .originalName(file.getOriginalFilename())
                            .contentType(file.getContentType())
                            .drmProtected(false) // Set based on your logic
                            .build();
                })
                .collect(Collectors.toList());
    }

    private void updateCategoryCounts(List<String> categoryIds, int delta) {
        if (categoryIds == null || categoryIds.isEmpty()) return;

        categoryIds.forEach(categoryId -> {
            categoryRepo.findById(categoryId).ifPresent(category -> {
                int newCount = Math.max(0, category.getBookCount() + delta);
                category.setBookCount(newCount);
                categoryRepo.save(category);
            });
        });
    }

//    private List<Resource> saveBookFile(BookDTO bookDTO) {
//        /// Upload resources and create embedded Resource objects
//        List<Resource> bookResources = List.of();
//        if (bookDTO.getBookFile() != null) {
//            bookResources = bookDTO.getBookFile().stream()
//                    .filter(file -> file != null && !file.isEmpty())
//                    .map(file -> {
//                        String url = storeResources.saveFiles(file);
//                        ResourceType type = storeResources.determineResourceType(Objects.requireNonNull(file.getOriginalFilename()));
//                        return Resource.builder()
//                                .type(type)
//                                .contentUrl(url)
//                                .build();
//                    })
//                    .toList();
//        }
//        return bookResources;
//    }
}
