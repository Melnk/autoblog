package com.autoblog.application;

public class DuplicateVinException extends RuntimeException {

    public DuplicateVinException(String vin) {
        super("Vehicle with VIN " + vin + " already exists");
    }
}
