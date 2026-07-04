package com.autoblog.identity.application;

public class InvalidAuthRequestException extends RuntimeException {

    private final String field;

    public InvalidAuthRequestException(String field, String message) {
        super(message);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
