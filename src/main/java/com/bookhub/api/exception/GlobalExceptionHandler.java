package com.bookhub.api.exception;

import com.bookhub.api.dto.ApiErrorResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponseDto> handleResourceNotFoundException(ResourceNotFoundException exception,
                                                                               WebRequest webRequest) {
        ApiErrorResponseDto errorResponse = ApiErrorResponseDto.builder()
                .error(ApiErrorResponseDto.ErrorDetails.of(
                        "NOT_FOUND",
                        exception.getMessage()
                ))
                .path(webRequest.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiErrorResponseDto> handleValidationException(ValidationException exception,
                                                                         WebRequest webRequest) {
        ApiErrorResponseDto errorResponse = ApiErrorResponseDto.builder()
                .error(ApiErrorResponseDto.ErrorDetails.of(
                        "VALIDATION_ERROR",
                        exception.getMessage()
                ))
                .path(webRequest.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiErrorResponseDto> handleDuplicateResource(DuplicateResourceException exception,
                                                                       WebRequest webRequest) {
        ApiErrorResponseDto errorResponse = ApiErrorResponseDto.builder()
                .error(ApiErrorResponseDto.ErrorDetails.of(
                        "DUPLICATE_RESOURCE",
                        exception.getMessage()
                ))
                .path(webRequest.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiErrorResponseDto> handleBusinessException(BusinessException exception,
                                                                       WebRequest webRequest) {
        ApiErrorResponseDto errorResponse = ApiErrorResponseDto.builder()
                .error(ApiErrorResponseDto.ErrorDetails.of(
                        "BUSINESS_ERROR",
                        exception.getMessage()
                ))
                .path(webRequest.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ApiErrorResponseDto> handleInvalidRequest(InvalidRequestException exception,
                                                                    WebRequest webRequest) {
        ApiErrorResponseDto errorResponse = ApiErrorResponseDto.builder()
                .error(ApiErrorResponseDto.ErrorDetails.of(
                        "INVALID_REQUEST",
                        exception.getMessage()
                ))
                .path(webRequest.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UnauthorizedActionException.class)
    public ResponseEntity<ApiErrorResponseDto> handleUnauthorized(UnauthorizedActionException exception,
                                                                  WebRequest webRequest) {
        ApiErrorResponseDto errorResponse = ApiErrorResponseDto.builder()
                .error(ApiErrorResponseDto.ErrorDetails.of(
                        "UNAUTHORIZED",
                        exception.getMessage()
                ))
                .path(webRequest.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(FileUploadException.class)
    public ResponseEntity<ApiErrorResponseDto> handleFileUploadException(FileUploadException exception,
                                                                         WebRequest webRequest) {
        ApiErrorResponseDto errorResponse = ApiErrorResponseDto.builder()
                .error(ApiErrorResponseDto.ErrorDetails.of(
                        "FILE_UPLOAD_ERROR",
                        exception.getMessage()
                ))
                .path(webRequest.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Handle Spring's validation errors (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponseDto> handleValidationErrors(MethodArgumentNotValidException ex,
                                                                      WebRequest webRequest) {
        Map<String, Object> details = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            details.put(error.getField(), error.getDefaultMessage());
        });

        ApiErrorResponseDto errorResponse = ApiErrorResponseDto.builder()
                .error(ApiErrorResponseDto.ErrorDetails.of(
                        "VALIDATION_ERROR",
                        "Validation failed",
                        details
                ))
                .path(webRequest.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ApiErrorResponseDto> handleInvalidToken(InvalidTokenException exception) {
        ApiErrorResponseDto errorResponse = ApiErrorResponseDto.builder()
                .error(ApiErrorResponseDto.ErrorDetails.of(
                        "INVALID_TOKEN",
                        exception.getMessage()
                ))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    // Global fallback handler
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponseDto> handleGlobalException(Exception exception,
                                                                     WebRequest webRequest) {
        ApiErrorResponseDto errorResponse = ApiErrorResponseDto.builder()
                .error(ApiErrorResponseDto.ErrorDetails.of(
                        "INTERNAL_ERROR",
                        "An unexpected error occurred"
                ))
                .path(webRequest.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
