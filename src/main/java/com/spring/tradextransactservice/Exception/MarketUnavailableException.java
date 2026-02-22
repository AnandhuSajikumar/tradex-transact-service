package com.spring.tradextransactservice.Exception;

public class MarketUnavailableException extends RuntimeException {
    public MarketUnavailableException(String message) {
        super(message);
    }
}
