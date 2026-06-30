package com.autoblog.application;

public class InvalidVinException extends RuntimeException {

    public InvalidVinException(String message) {
        super(message);
    }
}
