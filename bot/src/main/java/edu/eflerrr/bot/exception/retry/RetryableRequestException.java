package edu.eflerrr.bot.exception.retry;

public final class RetryableRequestException extends RuntimeException {
    public RetryableRequestException(String message) {
        super(message);
    }
}
