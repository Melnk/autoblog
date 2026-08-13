package com.autoblog.api.pagination;

public class InvalidPaginationException extends RuntimeException {

    private final String field;

    public InvalidPaginationException(String field, String message) {
        super(message);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
