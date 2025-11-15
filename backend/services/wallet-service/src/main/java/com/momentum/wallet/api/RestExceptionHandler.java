package com.momentum.wallet.api;

import com.momentum.sharedkernel.error.DomainException;
import com.momentum.wallet.api.dto.ApiError;
import com.momentum.wallet.domain.service.ResourceNotFoundException;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex) {
        return error(HttpStatus.NOT_FOUND, ex.getMessage(), List.of());
    }

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ApiError> handleDomain(DomainException ex) {
        return error(HttpStatus.BAD_REQUEST, ex.getMessage(), List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(RestExceptionHandler::formatFieldError)
                .toList();
        return error(HttpStatus.BAD_REQUEST, "Validation failed", details);
    }

    private static ResponseEntity<ApiError> error(HttpStatus status, String message, List<String> details) {
        ApiError body = new ApiError(OffsetDateTime.now(), status.value(), message, details);
        return ResponseEntity.status(status).body(body);
    }

    private static String formatFieldError(FieldError error) {
        return "%s %s".formatted(error.getField(), error.getDefaultMessage());
    }
}
