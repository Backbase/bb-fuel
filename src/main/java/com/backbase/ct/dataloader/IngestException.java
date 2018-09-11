package com.backbase.ct.dataloader;

public class IngestException extends RuntimeException {

    /**
     * Constructs a new ingest exception with the specified detail message.
     */
    public IngestException(String message) {
        super(message);
    }

    /**
     * Constructs a new runtime exception with the specified detail message and cause.
     */
    public IngestException(String message, Throwable cause) {
        super(message, cause);
    }
}
