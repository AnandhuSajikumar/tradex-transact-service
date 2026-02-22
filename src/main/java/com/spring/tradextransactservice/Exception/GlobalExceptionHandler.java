package com.spring.tradextransactservice.Exception;

import jakarta.persistence.OptimisticLockException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MarketUnavailableException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ApiError handleMarketUnavailable(MarketUnavailableException ex, HttpServletRequest request) {
        return new ApiError(
                409,
                "DEPENDENCY_FAILURE",
                ex.getMessage(),
                request.getRequestURI());
    }

    @ExceptionHandler(PortfolioUnavailableException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handlePortfolioUnavailable(PortfolioUnavailableException ex, HttpServletRequest request) {
        return new ApiError(
                409,
                "DEPENDENCY_FAILURE",
                ex.getMessage(),
                request.getRequestURI());
    }

    @ExceptionHandler(ConcurrentRequestException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConcurrentRequest(ConcurrentRequestException ex, HttpServletRequest request) {
        return new ApiError(
                409,
                "CONCURRENT_REQUEST",
                ex.getMessage(),
                request.getRequestURI());
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError illegalStateException(IllegalStateException ex, HttpServletRequest request) {
        return new ApiError(
                409,
                "CONFLICT",
                ex.getMessage(),
                request.getRequestURI());
    }

    @ExceptionHandler(OptimisticLockException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleVersion(OptimisticLockException ex, HttpServletRequest request) {
        return new ApiError(
                409,
                "CONCURRENT_UPDATE",
                "Task was modified by another request. Please retry.",
                request.getRequestURI());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        return new ApiError(
                400,
                "INVALID_REQUEST",
                ex.getMessage(),
                request.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidationErrors(MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        StringBuilder sb = new StringBuilder();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            sb.append(error.getField())
                    .append(": ")
                    .append(error.getDefaultMessage())
                    .append("; ");
        }

        return new ApiError(
                400,
                "VALIDATION_FAILED",
                sb.toString(),
                request.getRequestURI());
    }
}
