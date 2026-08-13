package com.autoblog.security;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@Validated
@ConfigurationProperties(prefix = "autoblog.security")
public class SecurityProperties {

    @NotBlank
    @Size(min = 32)
    private String jwtSecret;

    @Min(5)
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
