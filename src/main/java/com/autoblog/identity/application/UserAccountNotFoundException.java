package com.autoblog.identity.application;

public class UserAccountNotFoundException extends RuntimeException {

    public UserAccountNotFoundException(String email) {
        super("User with email " + email + " was not found");
    }
}
