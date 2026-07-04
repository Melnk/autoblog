package com.autoblog.publicreport.domain;

public class PublicReportNotFoundException extends RuntimeException {

    public PublicReportNotFoundException(String publicToken) {
        super("Public report " + publicToken + " was not found");
    }
}
