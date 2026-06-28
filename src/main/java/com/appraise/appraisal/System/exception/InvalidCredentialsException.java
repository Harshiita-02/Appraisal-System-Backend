package com.appraise.appraisal.System.exception;

/**
 * Thrown when login fails — wrong email, wrong password, or both.
 * Mapped to HTTP 401 in GlobalExceptionHandler, distinct from the
 * generic 500 fallback that plain RuntimeException used to fall into.
 */
public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException(String message) {
        super(message);
    }
}