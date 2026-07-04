package com.autoblog.access.application;

public class VehicleAccessDeniedException extends RuntimeException {

    public VehicleAccessDeniedException(String message) {
        super(message);
    }
}
