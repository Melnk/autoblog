package com.autoblog.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Service;

@Service
public class JwtTokenService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final SecurityProperties properties;
    private final ObjectMapper objectMapper;
    private final Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
    private final Base64.Decoder decoder = Base64.getUrlDecoder();

    public JwtTokenService(SecurityProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    public String createToken(UUID userId, String email) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(expiresInSeconds());
        try {
            String header = encodeJson(Map.of("alg", "HS256", "typ", "JWT"));
            String payload = encodeJson(Map.of(
                    "sub", userId.toString(),
                    "email", email,
                    "iat", now.getEpochSecond(),
                    "exp", expiresAt.getEpochSecond()
            ));
            String unsignedToken = header + "." + payload;
            return unsignedToken + "." + sign(unsignedToken);
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to create JWT", exception);
        }
    }

    public JwtClaims validate(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new IllegalArgumentException("Invalid JWT format");
            }

            String unsignedToken = parts[0] + "." + parts[1];
            if (!MessageDigest.isEqual(sign(unsignedToken).getBytes(StandardCharsets.UTF_8), parts[2].getBytes(StandardCharsets.UTF_8))) {
                throw new IllegalArgumentException("Invalid JWT signature");
            }

            JsonNode payload = objectMapper.readTree(decoder.decode(parts[1]));
            long expiresAt = payload.path("exp").asLong(0);
            if (expiresAt <= Instant.now().getEpochSecond()) {
                throw new IllegalArgumentException("JWT is expired");
            }

            UUID userId = UUID.fromString(payload.path("sub").asText());
            String email = payload.path("email").asText();
            return new JwtClaims(userId, email);
        } catch (Exception exception) {
            throw new InvalidJwtException("Invalid access token", exception);
        }
    }

    public long expiresInSeconds() {
        return properties.getJwtTtlMinutes() * 60L;
    }

    private String encodeJson(Map<String, ?> value) throws Exception {
        return encoder.encodeToString(objectMapper.writeValueAsBytes(value));
    }

    private String sign(String value) throws Exception {
        byte[] secret = secretBytes();
        Mac mac = Mac.getInstance(HMAC_ALGORITHM);
        mac.init(new SecretKeySpec(secret, HMAC_ALGORITHM));
        return encoder.encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
    }

    private byte[] secretBytes() {
        String secret = properties.getJwtSecret();
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("autoblog.security.jwt-secret must be configured");
        }
        return secret.getBytes(StandardCharsets.UTF_8);
    }
}
