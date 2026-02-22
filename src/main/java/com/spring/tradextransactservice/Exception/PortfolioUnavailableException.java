package com.spring.tradextransactservice.Exception;

public class PortfolioUnavailableException extends RuntimeException {
    public PortfolioUnavailableException(String message) {
        super(message);
    }
}
