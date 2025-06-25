package com.oozeander.mailer_back.exception;

public class SendEmailException extends RuntimeException {

    public SendEmailException(String message, Throwable cause) {
        super(message, cause);
    }
}
