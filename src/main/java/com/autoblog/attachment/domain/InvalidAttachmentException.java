package com.autoblog.attachment.domain;

public class InvalidAttachmentException extends RuntimeException {

    private final String field;

    public InvalidAttachmentException(String field, String message) {
        super(message);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
