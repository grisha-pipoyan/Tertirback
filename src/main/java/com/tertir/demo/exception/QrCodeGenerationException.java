package com.tertir.demo.exception;

public class QrCodeGenerationException extends RuntimeException{
    public QrCodeGenerationException(String message) {
        super(message);
    }

    public QrCodeGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}

