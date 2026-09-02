package com.nomesh.projects.lovable_clone.exception;

import com.nomesh.projects.lovable_clone.dto.exception.ApiError;
import com.nomesh.projects.lovable_clone.dto.exception.ApiFieldError;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiError> handleBadRequest(BadRequestException exception) {
        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, exception.getMessage());
        log.error(apiError.toString(), exception);
        return ResponseEntity.status(apiError.status()).body(apiError);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleResourceNotFoundRequest(ResourceNotFoundException exception) {
        ApiError apiError = new ApiError(HttpStatus.NOT_FOUND, "Resource not found: " + exception.getResourceName() + " with id " + exception.getResourceId() + " not found");
        log.error(apiError.toString(), exception);
        return ResponseEntity.status(apiError.status()).body(apiError);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleInputValidationError(MethodArgumentNotValidException exception) {
        List<ApiFieldError> errors = exception
                .getBindingResult()
                .getAllErrors()
                .stream()
                .map(error -> new ApiFieldError(
                        error instanceof FieldError fieldError ? fieldError.getField() : error.getObjectName(),
                        error.getDefaultMessage()
                ))
                .toList();

        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, "Input validation failed: ", errors);
        log.error(apiError.toString(), exception);
        return ResponseEntity.status(apiError.status()).body(apiError);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiError> handleIllegalStateException(IllegalStateException exception) {
        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, exception.getMessage());
        log.error(apiError.toString(), exception);
        return ResponseEntity.status(apiError.status()).body(apiError);
    }

    @ExceptionHandler(IncorrectResultSizeDataAccessException.class)
    public ResponseEntity<ApiError> handleIncorrectResultSizeDataAccessException(IncorrectResultSizeDataAccessException exception) {
        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, exception.getMessage());
        log.error(apiError.toString(), exception);
        return ResponseEntity.status(apiError.status()).body(apiError);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ApiError> handleUsernameNotFoundException(UsernameNotFoundException exception) {
        ApiError apiError = new ApiError(HttpStatus.NOT_FOUND, "Username not found: " + exception.getMessage());
        log.error(apiError.toString(), exception);
        return ResponseEntity.status(apiError.status()).body(apiError);
    }

    @ExceptionHandler(MalformedJwtException.class)
    public ResponseEntity<ApiError> handleMalformedJwtException(MalformedJwtException exception) {
        ApiError apiError = new ApiError(HttpStatus.UNAUTHORIZED, "Invalid or malformed authentication token: " + exception.getMessage());
        log.error(apiError.toString(), exception);
        return ResponseEntity.status(apiError.status()).body(apiError);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> handleAuthenticationException(AuthenticationException exception) {
        ApiError apiError = new ApiError(HttpStatus.UNAUTHORIZED, "Authentication failed: " + exception.getMessage());
        log.error(apiError.toString(), exception);
        return ResponseEntity.status(apiError.status()).body(apiError);
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ApiError> handleJwtException(JwtException exception) {
        ApiError apiError = new ApiError(HttpStatus.UNAUTHORIZED, "Invalid jwt token: " + exception.getMessage());
        log.error(apiError.toString(), exception);
        return ResponseEntity.status(apiError.status()).body(apiError);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDeniedException(AccessDeniedException exception) {
        ApiError apiError = new ApiError(HttpStatus.FORBIDDEN, "Access denied - Insufficient permissions: " + exception.getMessage());
        log.error(apiError.toString(), exception);
        return ResponseEntity.status(apiError.status()).body(apiError);
    }

    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<ApiError> handlePaymentException(PaymentException exception) {
        ApiError apiError = new ApiError(HttpStatus.BAD_GATEWAY, "Payment processing failed: " + exception.getMessage());
        log.error(apiError.toString(), exception);
        return ResponseEntity.status(apiError.status()).body(apiError);
    }
}
