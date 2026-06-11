package com.fooddelivery.common.web;

import com.fooddelivery.common.error.ErrorResponse;
import com.fooddelivery.common.error.FieldErrorDetails;
import com.fooddelivery.common.error.ValidationErrorResponse;
import com.fooddelivery.common.exception.BusinessException;
import com.fooddelivery.common.exception.ForbiddenException;
import com.fooddelivery.common.exception.ResourceNotFoundException;
import com.fooddelivery.common.exception.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;

@RestControllerAdvice
public class CommonExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(CommonExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                                HttpServletRequest request) {
        List<FieldErrorDetails> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new FieldErrorDetails(error.getField(), error.getDefaultMessage()))
                .toList();
        log.warn("Validation error: path={} errors={}", request.getRequestURI(), errors);
        ValidationErrorResponse response = buildValidationErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), errors, request);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ValidationErrorResponse> handleConstraintViolation(ConstraintViolationException ex,
                                                                             HttpServletRequest request) {
        List<FieldErrorDetails> errors = ex.getConstraintViolations().stream()
                .map(violation -> new FieldErrorDetails(violation.getPropertyPath().toString(), violation.getMessage()))
                .toList();
        log.warn("Constraint violation: path={} errors={}", request.getRequestURI(), errors);
        ValidationErrorResponse response = buildValidationErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), errors, request);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                                                                      HttpServletRequest request) {
        log.warn("Malformed JSON: path={}", request.getRequestURI(), ex);
        ErrorResponse response = buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex,
                                                                        HttpServletRequest request) {
        log.warn("Illegal argument: path={} message={}", request.getRequestURI(), ex.getMessage());
        ErrorResponse response = buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex,
                                                                HttpServletRequest request) {
        log.warn("Not found: path={} message={}", request.getRequestURI(), ex.getMessage());
        ErrorResponse response = buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex,
                                                                 HttpServletRequest request) {
        HttpStatus status = ex.getStatus();
        if (status.is5xxServerError()) {
            log.error("Business exception: path={} status={} message={}", request.getRequestURI(), ex.getStatus(), ex.getMessage(), ex);
        } else {
            log.warn("Business exception: path={} status={} message={}", request.getRequestURI(), ex.getStatus(), ex.getMessage());
        }
        ErrorResponse response = buildErrorResponse(status, ex.getMessage(), request);
        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedException(UnauthorizedException ex,
                                                                     HttpServletRequest request) {
        log.warn("Unauthorized: path={}", request.getRequestURI());
        ErrorResponse response = buildErrorResponse(HttpStatus.UNAUTHORIZED, ex.getMessage(), request);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleForbiddenException(ForbiddenException ex,
                                                                  HttpServletRequest request) {
        log.warn("Forbidden: path={}", request.getRequestURI());
        ErrorResponse response = buildErrorResponse(HttpStatus.FORBIDDEN, ex.getMessage(), request);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex,
                                                                HttpServletRequest request) {
        log.error("Runtime exception: path={}", request.getRequestURI(), ex);
        ErrorResponse response = buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), request);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex,
                                                         HttpServletRequest request) {
        log.error("Unhandled exception: path={}", request.getRequestURI(), ex);
        ErrorResponse response = buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), request);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    private ErrorResponse buildErrorResponse(HttpStatus status, String message, HttpServletRequest request) {
        return new ErrorResponse(Instant.now(), status.value(), status.getReasonPhrase(), message, request.getRequestURI());
    }

    private ValidationErrorResponse buildValidationErrorResponse(HttpStatus status, String message,
                                                                 List<FieldErrorDetails> errors,
                                                                 HttpServletRequest request) {
        ErrorResponse base = buildErrorResponse(status, message, request);
        ValidationErrorResponse response = new ValidationErrorResponse();
        response.setTimestamp(base.getTimestamp());
        response.setStatus(base.getStatus());
        response.setError(base.getError());
        response.setMessage(base.getMessage());
        response.setPath(base.getPath());
        response.setErrors(errors);
        return response;
    }
}
