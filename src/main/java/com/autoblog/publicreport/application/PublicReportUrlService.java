package com.autoblog.publicreport.application;

import org.springframework.stereotype.Service;

@Service
public class PublicReportUrlService {

    private static final String PUBLIC_REPORT_PATH = "/api/v1/public/reports/";

    private final AutoblogPublicProperties properties;

    public PublicReportUrlService(AutoblogPublicProperties properties) {
        this.properties = properties;
    }

    public String publicReportUrl(String publicToken) {
        String path = PUBLIC_REPORT_PATH + publicToken;
        String baseUrl = properties.getPublicBaseUrl();
        if (baseUrl == null || baseUrl.isBlank()) {
            return path;
        }
        return baseUrl.replaceAll("/+$", "") + path;
    }
}
