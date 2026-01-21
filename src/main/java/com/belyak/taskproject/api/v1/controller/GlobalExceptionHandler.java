package com.belyak.taskproject.api.v1.controller;

import com.belyak.taskproject.api.v1.dto.ApiErrorResponse;
import com.belyak.taskproject.domain.exception.CategoryAlreadyExistsException;
import com.belyak.taskproject.domain.exception.CategoryDeletionException;
import com.belyak.taskproject.domain.exception.TagAlreadyExistsException;
import com.belyak.taskproject.domain.exception.TagDeletionException;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        List<ApiErrorResponse.FieldError> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new ApiErrorResponse.FieldError(
                        error.getField(),
                        error.getDefaultMessage()))
                .toList();

        log.warn("Validation failed: {} errors", errors.size());
        return buildResponse(HttpStatus.BAD_REQUEST, "Validation failed", errors);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleEntityNotFoundException(EntityNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, "Resource not found", List.of());
    }

    @ExceptionHandler(CategoryAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleCategoryAlreadyExistsException(CategoryAlreadyExistsException ex) {
        log.warn("Category conflict: {}", ex.getMessage());
        return buildResponse(HttpStatus.CONFLICT, "Category with this name already exists", List.of());
    }

    @ExceptionHandler(TagAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleTagAlreadyExistsException(TagAlreadyExistsException ex) {
        log.warn("Tag conflict: {}", ex.getMessage());
        return buildResponse(HttpStatus.CONFLICT, "Tag with this name already exists", List.of());
    }

    @ExceptionHandler(CategoryDeletionException.class)
    public ResponseEntity<ApiErrorResponse> handleCategoryDeletionException(CategoryDeletionException ex) {
        log.warn("Category deletion blocked: {}", ex.getMessage());
        return buildResponse(HttpStatus.CONFLICT, "Category deletion blocked. It has tasks", List.of());
    }

    @ExceptionHandler(TagDeletionException.class)
    public ResponseEntity<ApiErrorResponse> handleTagDeletionException(TagDeletionException ex) {
        log.warn("Tag deletion blocked: {}", ex.getMessage());
        return buildResponse(HttpStatus.CONFLICT, "Tag deletion blocked. It has tasks", List.of());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleBadCredentialsException(BadCredentialsException ex) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "Invalid email or password", List.of());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        log.error("Database conflict: {}", ex.getMessage());
        return buildResponse(HttpStatus.CONFLICT, "Resource already exists", List.of());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.error("Tag duplication: {}", ex.getMessage());
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), List.of());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiErrorResponse> handleBusinessException(IllegalStateException ex) {
        log.warn("Business rule violation: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), List.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneralException(Exception ex) {
        log.error("Unexpected error: ", ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", List.of());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDeniedException(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());
        return buildResponse(HttpStatus.FORBIDDEN, "Access denied. You do not have permission to perform this action.", List.of());
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(
            HttpStatus status,
            String message,
            List<ApiErrorResponse.FieldError> errors) {

        return ResponseEntity
                .status(status)
                .body(ApiErrorResponse.of(
                        status.value(),
                        message,
                        errors
                ));
    }
}
