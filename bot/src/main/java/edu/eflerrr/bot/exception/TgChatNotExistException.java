package edu.eflerrr.bot.exception;

public class TgChatNotExistException extends RuntimeException {
    public TgChatNotExistException(String message) {
        super(message);
    }
}
