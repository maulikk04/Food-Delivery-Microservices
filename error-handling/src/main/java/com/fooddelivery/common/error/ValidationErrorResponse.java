package com.fooddelivery.common.error;

import java.time.Instant;
import java.util.List;

public class ValidationErrorResponse extends ErrorResponse {
    private List<FieldErrorDetails> errors;

    public ValidationErrorResponse() {
        super();
    }

    public ValidationErrorResponse(Instant timestamp, int status, String error, String message, String path, List<FieldErrorDetails> errors) {
        super(timestamp, status, error, message, path);
        this.errors = errors;
    }

    public List<FieldErrorDetails> getErrors() {
        return errors;
    }

    public void setErrors(List<FieldErrorDetails> errors) {
        this.errors = errors;
    }
}
