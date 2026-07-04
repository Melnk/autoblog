package com.autoblog.access.application;

public class InvalidVehicleAccessException extends RuntimeException {

    public InvalidVehicleAccessException(String message) {
        super(message);
    }
}
