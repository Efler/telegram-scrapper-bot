package edu.eflerrr.scrapper.exception.retry;

public final class RetryableRequestException extends RuntimeException {
    public RetryableRequestException(String message) {
        super(message);
    }
}
