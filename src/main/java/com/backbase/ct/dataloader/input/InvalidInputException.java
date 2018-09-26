package com.backbase.ct.dataloader.input;

public class InvalidInputException extends RuntimeException {

    /**
     * Constructs a new exception with the specified detail message.
     */
    public InvalidInputException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified detail message and cause.
     */
    public InvalidInputException(String message, Throwable cause) {
        super(message, cause);
    }

}
