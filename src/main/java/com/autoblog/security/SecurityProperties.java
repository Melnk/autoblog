package com.autoblog.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "autoblog.security")
public class SecurityProperties {

    private String jwtSecret;
    private long jwtTtlMinutes = 60;

    public String getJwtSecret() {
        return jwtSecret;
    }

    public void setJwtSecret(String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }

    public long getJwtTtlMinutes() {
        return jwtTtlMinutes;
    }

    public void setJwtTtlMinutes(long jwtTtlMinutes) {
        this.jwtTtlMinutes = jwtTtlMinutes;
    }
}
