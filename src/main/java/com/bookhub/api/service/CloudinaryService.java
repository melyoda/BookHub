package com.bookhub.api.service;

import com.bookhub.api.model.ResourceType;
import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public String uploadFile(MultipartFile file, ResourceType resourceType) throws IOException {
        // Generate a better public ID with folder structure
        String publicId = generatePublicId(file.getOriginalFilename(), resourceType);

        Map<String, Object> uploadOptions = new HashMap<>();
        uploadOptions.put("public_id", publicId);
        uploadOptions.put("overwrite", true);

        // Set correct Cloudinary resource types
        switch (resourceType) {
            case IMAGE:
                uploadOptions.put("resource_type", "image");
                // Add image optimizations
                uploadOptions.put("quality", "auto");
                uploadOptions.put("fetch_format", "auto");
                break;
            case EBOOK:
            case DOCUMENT:
                uploadOptions.put("resource_type", "raw"); // Important: eBooks are "raw" files
                break;
        }

        try {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadOptions);
            String secureUrl = uploadResult.get("secure_url").toString();
            log.info("File uploaded successfully: {} -> {}", file.getOriginalFilename(), secureUrl);
            return secureUrl;

        } catch (Exception e) {
            log.error("Cloudinary upload failed for file: {}", file.getOriginalFilename(), e);
            throw new IOException("Cloudinary upload failed: " + e.getMessage(), e);
        }
    }

    public void deleteFile(String fileUrl) throws IOException {
        try {
            String publicId = extractPublicIdFromUrl(fileUrl);
            if (publicId != null) {
                String resourceType = "image";
                if (publicId.startsWith("ebooks/") || publicId.startsWith("documents/")) {
                    resourceType = "raw";
                }

                Map<String, String> options = ObjectUtils.asMap("resource_type", resourceType);
                Map<?, ?> result = cloudinary.uploader().destroy(publicId, options);

                if ("ok".equals(result.get("result"))) {
                    log.info("File deleted successfully: {}", publicId);
                } else {
                    log.warn("File deletion failed for publicId: {}. Result: {}", publicId, result);
                    // ✅ No exception thrown - just log and continue
                }
            }
        } catch (Exception e) {
            log.error("Failed to delete file from Cloudinary: {}", fileUrl, e);
            throw new IOException("File deletion failed: " + e.getMessage(), e);
        }
    }

    private String generatePublicId(String originalFilename, ResourceType type) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String baseName = getBaseName(originalFilename)
                .replaceAll("[^a-zA-Z0-9-_]", "_")
                .toLowerCase();

        // Organized folder structure
        switch (type) {
            case IMAGE:
                return String.format("book-covers/%s_%s", baseName, timestamp);
            case EBOOK:
                return String.format("ebooks/%s_%s", baseName, timestamp);
            case DOCUMENT:
                return String.format("documents/%s_%s", baseName, timestamp);
            default:
                return String.format("uploads/%s_%s", baseName, timestamp);
        }
    }

    private String getBaseName(String filename) {
        if (filename == null) {
            return "file";
        }

        // Remove path information if present
        int lastSeparator = Math.max(filename.lastIndexOf('/'), filename.lastIndexOf('\\'));
        String name = (lastSeparator >= 0) ? filename.substring(lastSeparator + 1) : filename;

        // Remove extension
        int extensionIndex = name.lastIndexOf('.');
        if (extensionIndex > 0) {
            return name.substring(0, extensionIndex);
        }

        return name;
    }

    private String extractPublicIdFromUrl(String url) {
        try {
            // Cloudinary URL pattern: https://res.cloudinary.com/cloudname/resource_type/type/version/public_id.extension
            Pattern pattern = Pattern.compile("cloudinary\\.com/.*?/(image|raw|video)/upload/(?:v\\d+/)?(.*?)(\\.[^.]+)?$");
            Matcher matcher = pattern.matcher(url);

            if (matcher.find()) {
                return matcher.group(2); // Returns the public_id without extension
            }
        } catch (Exception e) {
            log.warn("Failed to extract public ID from URL: {}", url, e);
        }
        return null;
    }
}