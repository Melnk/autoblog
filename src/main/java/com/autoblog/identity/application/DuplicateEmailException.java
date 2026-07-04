package com.autoblog.identity.application;

public class DuplicateEmailException extends RuntimeException {

    public DuplicateEmailException(String email) {
        super("User with email " + email + " already exists");
    }
}
