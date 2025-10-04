package com.bookhub.api.service;

import com.bookhub.api.dto.BookContributionRequestDTO;
import com.bookhub.api.dto.BookLookupRequestDTO;
import com.bookhub.api.dto.BookRequestResponseDTO;
import com.bookhub.api.exception.ResourceNotFoundException;
import com.bookhub.api.model.*;
import com.bookhub.api.repository.BookRepository;
import com.bookhub.api.repository.CategoryRepository;
import com.bookhub.api.repository.RequestRepository;
import com.bookhub.api.repository.UserRepository;
import com.bookhub.api.utils.StoreResources;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
// Other necessary imports

@Service
@RequiredArgsConstructor
@Slf4j
public class RequestsService {

    private final UserRepository userRepo;
    private final RequestRepository requestRepository;
    private final StoreResources storeResources;
    private final CloudinaryService cloudinaryService;
    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepo;

    /**
     * Creates a new book contribution request from a user, including file uploads.
     */
    public BookRequestResponseDTO createContributionRequest(BookContributionRequestDTO dto) {
        // 1. Get the current user, just like in your BookService
        User currentUser = getCurrentUser();

        // 2. Handle file uploads using private helper methods that leverage your StoreResources util
        String coverImageUrl = saveRequestCoverImage(dto.getCoverImage());
        List<String> bookFileUrls = saveRequestBookFiles(dto.getBookFile());

        // 3. Create the Request entity using the builder pattern
        Request newRequest = Request.builder()
                .title(dto.getTitle())
                .author(dto.getAuthor())
                .description(dto.getDescription())
                .isbn(dto.getIsbn())
                .categoryIds(dto.getCategoryIds())
                .userId(currentUser.getId()) // <-- Storing the stable User ID
                .status(RequestStatus.PENDING)
                .type(RequestType.CONTRIBUTION)
                .coverImageUrl(coverImageUrl)
                .bookFileUrls(bookFileUrls)
                .build();

        // 4. Save the new request to the database
        Request savedRequest = requestRepository.save(newRequest);

        // 5. Map the saved entity to a response DTO and return
        return toResponseDTO(savedRequest);
    }

    /**
     * Creates a new book lookup request (metadata only).
     */
    public BookRequestResponseDTO createLookupRequest(BookLookupRequestDTO dto) {
        User currentUser = getCurrentUser();

        Request newRequest = Request.builder()
                .title(dto.getTitle())
                .author(dto.getAuthor())
                .description(dto.getDescription())
                .isbn(dto.getIsbn())
                .userId(currentUser.getId())
                .status(RequestStatus.PENDING)
                .type(RequestType.LOOKUP)
                .build();

        Request savedRequest = requestRepository.save(newRequest);
        return toResponseDTO(savedRequest);
    }


    //method to get request flexible based on request statue / type
    public Page<BookRequestResponseDTO> getRequests(RequestType type, RequestStatus status, Pageable pageable) {
        Page<Request> requestsPage;
        if (type != null) {
            requestsPage = requestRepository.findByTypeAndStatus(type, status, pageable);
        } else {
            requestsPage = requestRepository.findByStatus(status, pageable);
        }
        return requestsPage.map(this::toResponseDTO); // Reusing our mapper
    }

    // ... other methods for approve, reject, get, etc.
    // these will be changed they are placeholder for now

    /**
     * Approves a request. The logic will differ based on the request type.
     */
    public BookRequestResponseDTO approveRequest(String requestId, String createdBookId) {
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found with ID: " + requestId));

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new IllegalStateException("Only pending requests can be approved.");
        }

        switch (request.getType()) {
            case LOOKUP:
                // For LOOKUP, the admin created the book manually. We just link it.
                if (createdBookId == null || createdBookId.isBlank()) {
                    throw new IllegalArgumentException("createdBookId is required to approve a LOOKUP request.");
                }
                request.setCreatedBookId(createdBookId);
                break;

            case CONTRIBUTION:
                // For CONTRIBUTION, we automatically create the book.
                Book newBook = createBookFromContribution(request);
                Book savedBook = bookRepository.save(newBook);
                updateCategoryCounts(savedBook.getCategoryIds(), 1); // Reusing your BookService logic
                request.setCreatedBookId(savedBook.getId());
                break;
        }

        request.setStatus(RequestStatus.APPROVED);
        Request updatedRequest = requestRepository.save(request);
        return toResponseDTO(updatedRequest);
    }

    private Book createBookFromContribution(Request request) {
        validateCategories(request.getCategoryIds()); // A helper method to ensure categories exist

        // Convert the List<String> of URLs into the List<Resource> your Book model expects
        List<Resource> bookResources = request.getBookFileUrls().stream()
                .map(url -> Resource.builder()
                        .contentUrl(url)
                        .type(ResourceType.EBOOK) // We can infer this or determine it more robustly if needed
                        .originalName(extractFileNameFromUrl(url)) // Helper to get a clean name
                        .build())
                .collect(Collectors.toList());

        return Book.builder()
                .title(request.getTitle())
                .author(request.getAuthor())
                .description(request.getDescription())
                .isbn(request.getIsbn())
                .categoryIds(request.getCategoryIds())
                .coverImageUrl(request.getCoverImageUrl())
                .bookFileUrl(bookResources)
                .addedBy(request.getUserId()) // The user who submitted it
                .build();
    }

    /**
     * Rejects a request.
     */
    public BookRequestResponseDTO rejectRequest(String requestId, String reason) {
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found with ID: " + requestId));

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new IllegalStateException("Only pending requests can be rejected.");
        }

        if (request.getType() == RequestType.CONTRIBUTION) {
            // If it's a contribution, delete the uploaded files from Cloudinary
            try {
                if (request.getCoverImageUrl() != null) {
                    cloudinaryService.deleteFile(request.getCoverImageUrl());
                }
                if (request.getBookFileUrls() != null) {
                    for (String url : request.getBookFileUrls()) {
                        cloudinaryService.deleteFile(url);
                    }
                }
            } catch (IOException e) {
                // Log a warning but continue with the rejection. The DB state is most important.
                log.warn("Failed to delete Cloudinary files for rejected request {}: {}", requestId, e.getMessage());
            }
        }

        request.setStatus(RequestStatus.REJECTED);
        request.setRejectionReason(reason);
        Request updatedRequest = requestRepository.save(request);
        return toResponseDTO(updatedRequest);
    }

    /**
     *helper methods
     */
    private User getCurrentUser() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        String userEmail = userDetails.getUsername(); // assuming email is username
        return userRepo.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private BookRequestResponseDTO toResponseDTO(Request request) {
        BookRequestResponseDTO dto = new BookRequestResponseDTO();
        dto.setId(request.getId());
        dto.setRequestType(request.getType());
        dto.setStatus(request.getStatus());
        dto.setTitle(request.getTitle());
        dto.setAuthor(request.getAuthor());
        dto.setDescription(request.getDescription());
        dto.setIsbn(request.getIsbn());
        dto.setCategoryIds(request.getCategoryIds());
        dto.setUserId(request.getUserId());
        dto.setCreatedAt(request.getCreatedAt());
        dto.setUpdatedAt(request.getUpdatedAt());
        return dto;
    }

    private String saveRequestCoverImage(MultipartFile coverImage) {
        if (coverImage == null || coverImage.isEmpty()) {
            return null;
        }
        // Leveraging your existing, validated file saving utility
        return storeResources.saveFile(coverImage);
    }

    private List<String> saveRequestBookFiles(List<MultipartFile> bookFiles) {
        if (bookFiles == null || bookFiles.isEmpty()) {
            return Collections.emptyList();
        }
        return bookFiles.stream()
                .filter(file -> file != null && !file.isEmpty())
                .map(storeResources::saveFile) // Elegant method reference to your utility
                .collect(Collectors.toList());
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

    private String extractFileNameFromUrl(String url) {
        if (url == null || url.isEmpty()) {
            return "unknown_file";
        }

        // 1. Get the full filename from the URL (e.g., "clean_code_16645321.epub")
        String fullFileName = url.substring(url.lastIndexOf('/') + 1);

        // 2. Remove the timestamp (e.g., "_16645321") before the file extension
        // This regex looks for an underscore followed by 10 or more digits
        String cleanFileName = fullFileName.replaceAll("_\\d{10,}", "");

        return cleanFileName;
    }

}