package com.bookhub.api.utils;

import com.bookhub.api.exception.FileUploadException;
import com.bookhub.api.exception.ValidationException;
import com.bookhub.api.model.ResourceType;
import com.bookhub.api.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class StoreResources {

    private final CloudinaryService cloudinaryService;

    // Size limits for your eBook app
    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024; // 5MB for covers
    private static final long MAX_EBOOK_SIZE = 50 * 1024 * 1024; // 50MB for eBooks
    private static final long MAX_DOCUMENT_SIZE = 10 * 1024 * 1024; // 10MB for other docs

    // Supported formats
    private static final Set<String> SUPPORTED_IMAGES = Set.of("jpg", "jpeg", "png", "webp");
    private static final Set<String> SUPPORTED_EBOOKS = Set.of("pdf", "epub", "mobi");
    private static final Set<String> SUPPORTED_DOCUMENTS = Set.of("doc", "docx", "txt");

    public String saveFile(MultipartFile file) {
        try {
            validateFile(file);
            ResourceType type = determineResourceType(file.getOriginalFilename());
            checkFileSize(file, type);

            return cloudinaryService.uploadFile(file, type);
        } catch (IOException e) {
            log.error("File upload failed: {}", file.getOriginalFilename(), e);
            throw new FileUploadException("File upload failed: " + e.getMessage());
        }
    }

    public ResourceType determineResourceType(String filename) {
        if (filename == null) {
            throw new ValidationException("Filename cannot be null");
        }

        String extension = getFileExtension(filename).toLowerCase();

        if (SUPPORTED_IMAGES.contains(extension)) {
            return ResourceType.IMAGE;
        } else if (SUPPORTED_EBOOKS.contains(extension)) {
            return ResourceType.EBOOK;
        } else if (SUPPORTED_DOCUMENTS.contains(extension)) {
            return ResourceType.DOCUMENT;
        } else {
            throw new ValidationException("Unsupported file type: " + extension);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ValidationException("File cannot be empty");
        }

        if (file.getOriginalFilename() == null || file.getOriginalFilename().trim().isEmpty()) {
            throw new ValidationException("Filename cannot be empty");
        }
    }

    private void checkFileSize(MultipartFile file, ResourceType type) {
        long size = file.getSize();

        switch (type) {
            case IMAGE:
                if (size > MAX_IMAGE_SIZE) {
                    throw new ValidationException(
                            String.format("Image size exceeds %dMB limit", MAX_IMAGE_SIZE / (1024 * 1024))
                    );
                }
                break;
            case EBOOK:
                if (size > MAX_EBOOK_SIZE) {
                    throw new ValidationException(
                            String.format("eBook size exceeds %dMB limit", MAX_EBOOK_SIZE / (1024 * 1024))
                    );
                }
                break;
            case DOCUMENT:
                if (size > MAX_DOCUMENT_SIZE) {
                    throw new ValidationException(
                            String.format("Document size exceeds %dMB limit", MAX_DOCUMENT_SIZE / (1024 * 1024))
                    );
                }
                break;
        }
    }

    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < filename.length() - 1) {
            return filename.substring(lastDotIndex + 1).toLowerCase();
        }
        throw new ValidationException("Invalid filename: " + filename);
    }
}