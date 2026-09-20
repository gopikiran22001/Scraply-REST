package com.scraply.rest.security;

import com.scraply.rest.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JwtService {

    @Value("${jwt.expiration.user:15552000000}")
    private long userTokenExpiration;

    @Value("${jwt.expiration.picker:2592000000}")
    private long pickerTokenExpiration;

    @Value("${jwt.expiration.admin:86400000}")
    private long adminTokenExpiration;

    @Value("${jwt.secret:1234567890}")
    private String secretKey;


    public String generateToken(User user) {
        return switch (user.getUserRole()) {
            case ADMIN -> buildToken(user, adminTokenExpiration);
            case PICKER -> buildToken(user, pickerTokenExpiration);
            default -> buildToken(user, userTokenExpiration);
        };
    }

    public String extractUsername(String token) {
        return parseClaims(token).get("mail");
    }

    public UUID extractUserId(String token) {
        String userId = parseClaims(token).get("userId");
        return userId == null ? null : UUID.fromString(userId);
    }

    private String buildToken(User user, long expirationSeconds) {
        Map<String, String> claims = new LinkedHashMap<>();
        claims.put("userId", user.getId() != null ? user.getId().toString() : null);
        claims.put("role", user.getUserRole().name());
        Instant now = Instant.now();
        String payload = String.join("|",
                user.getEmail(),
                Long.toString(now.getEpochSecond()),
                Long.toString(now.plusSeconds(expirationSeconds).getEpochSecond()),
                claims.get("userId"),
                claims.get("role")
        );
        return sign(payload) + "." + Base64.getUrlEncoder().withoutPadding().encodeToString(payload.getBytes(StandardCharsets.UTF_8));
    }

    private boolean isTokenExpired(String token) {
        return Instant.ofEpochSecond(Long.parseLong(parseClaims(token).get("exp"))).isBefore(Instant.now());
    }

    private Map<String, String> parseClaims(String token) {
        String[] parts = splitToken(token);
        String signature = parts[0];
        String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
        if (!signature.equals(sign(payload))) {
            throw new IllegalArgumentException("Invalid token signature");
        }
        String[] values = payload.split("\\|", -1);
        Map<String, String> claims = new LinkedHashMap<>();
        claims.put("mail", values[0]);
        claims.put("iat", values[1]);
        claims.put("exp", values[2]);
        claims.put("userId", values[3].isBlank() ? null : values[3]);
        claims.put("role", values[4]);
        return claims;
    }

    private String sign(String payload) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secretBytes(), "HmacSHA256"));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to sign token", ex);
        }
    }

    private String[] splitToken(String token) {
        String[] parts = token.split("\\.", 2);
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid token format");
        }
        return parts;
    }

    private byte[] secretBytes() {
        String secret = secretKey;
        try {
            return Base64.getUrlDecoder().decode(secret);
        } catch (IllegalArgumentException ex) {
            return secret.getBytes(StandardCharsets.UTF_8);
        }
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        return !isTokenExpired(token) && userDetails != null && userDetails.isEnabled();
    }
}
