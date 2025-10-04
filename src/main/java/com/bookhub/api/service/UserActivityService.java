package com.bookhub.api.service;

import com.bookhub.api.dto.ReadingHistoryResponseDTO;
import com.bookhub.api.dto.ReadingProgressDTO;
import com.bookhub.api.model.ReadingProgress;
import com.bookhub.api.repository.BookRepository;
import com.bookhub.api.repository.ReadingHistoryRepository;
import com.bookhub.api.repository.ReadingProgressRepository;
import com.bookhub.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.bookhub.api.exception.ResourceNotFoundException;
import com.bookhub.api.model.Book;
import com.bookhub.api.model.ReadingHistory;
import com.bookhub.api.model.User;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserActivityService {

    private final ReadingHistoryRepository readingHistoryRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepo;
    private final ReadingProgressRepository readingProgressRepository;

    /**
     * Logs that a user has opened a book.
     * Updates the lastOpenedAt timestamp if a record exists, otherwise creates a new one.
     */
    public void logHistory(String bookId) {
        User currentUser = getCurrentUser();

        // Find the book to get its details for denormalization
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with ID: " + bookId));

        // Use the repository method to find the specific history record
        Optional<ReadingHistory> existingHistoryOpt =
                readingHistoryRepository.findByUserIdAndBookId(currentUser.getId(), bookId);

        ReadingHistory historyRecord;
        if (existingHistoryOpt.isPresent()) {
            // --- UPDATE ---
            // If the record exists, just update the timestamp
            historyRecord = existingHistoryOpt.get();
            historyRecord.setLastOpenedAt(Instant.now());
        } else {
            // --- INSERT ---
            // If it's the first time, create a new record with all the details
            historyRecord = ReadingHistory.builder()
                    .userId(currentUser.getId())
                    .bookId(book.getId())
                    .bookTitle(book.getTitle()) // Denormalizing title
                    .bookCoverImage(book.getCoverImageUrl()) // Denormalizing cover image
                    .lastOpenedAt(Instant.now())
                    .build();
        }

        readingHistoryRepository.save(historyRecord);
    }
    public Page<ReadingHistoryResponseDTO> getHistory(Pageable pageable) {
        User currentUser = getCurrentUser();

        Page<ReadingHistory> historyPage = readingHistoryRepository
                .findByUserIdOrderByLastOpenedAtDesc(currentUser.getId(), pageable);

        // Use the .map() function on the Page object to convert each item
        return historyPage.map(this::toResponseDTO);
    }

    /// progress methods
    /**
     * Saves or updates the reading progress for a user and a specific book.
     */
    public ReadingProgressDTO saveOrUpdateProgress(String bookId, ReadingProgressDTO progressDTO) {
        User currentUser = getCurrentUser();

        // Find existing progress record or create a new one.
        ReadingProgress progress = readingProgressRepository
                .findByUserIdAndBookId(currentUser.getId(), bookId)
                .orElse(ReadingProgress.builder() // If not found, create a new instance
                        .userId(currentUser.getId())
                        .bookId(bookId)
                        .build());

        // Update the record with the new data from the DTO
        progress.setPercent(progressDTO.getPercent());
        progress.setFormat(progressDTO.getFormat());
        progress.setLocator(progressDTO.getLocator());

        ReadingProgress savedProgress = readingProgressRepository.save(progress);
        return toResponseDTO(savedProgress);
    }

    /**
     * Retrieves the reading progress for a user and a specific book.
     */
    public ReadingProgressDTO getProgress(String bookId) {
        User currentUser = getCurrentUser();

        return readingProgressRepository
                .findByUserIdAndBookId(currentUser.getId(), bookId)
                .map(this::toResponseDTO) // If found, map it to a DTO
                .orElseThrow(() -> new ResourceNotFoundException( // If not, throw an exception
                        "No progress found for user on book " + bookId));
    }

    // --- Helper Methods ---

    private User getCurrentUser() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
        String userEmail = userDetails.getUsername();
        return userRepo.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + userEmail));
    }

    // --- Helper Method for Mapping ---
    private ReadingHistoryResponseDTO toResponseDTO(ReadingHistory history) {
        return ReadingHistoryResponseDTO.builder()
                .bookId(history.getBookId())
                .title(history.getBookTitle())
                .coverImage(history.getBookCoverImage())
                .lastOpenedAt(history.getLastOpenedAt())
                .build();
    }

    private ReadingProgressDTO toResponseDTO(ReadingProgress progress) {
        ReadingProgressDTO dto = new ReadingProgressDTO();
        dto.setFormat(progress.getFormat());
        dto.setLocator(progress.getLocator());
        dto.setPercent(progress.getPercent());
        dto.setUpdatedAt(progress.getUpdatedAt());
        return dto;
    }
}